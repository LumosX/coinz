package eu.zerovector.coinz.Data

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import eu.zerovector.coinz.Extras.Companion.MakeToast
import java.util.*
import kotlin.math.min



class DataManager {

    // Stuff inside the companion object is static, apparently
    companion object {
        // SharedPrefs constants
        const val PREFS_NAME = "CoinZConfig"
        const val PREF_VERSION_CODE_KEY = "version_code"
        const val DOESNT_EXIST = -1

        private var UIListeners: MutableList<() -> Unit> = mutableListOf()


        private lateinit var currentUserData: AccountData
        private var dailyTimestamp: String = ""
        var currentCoinsMap: HashMap<String, Map.CoinInfo> = hashMapOf() // the set of all coin IDs taken today.
        var dailyPureRates: Map.Rates = Map.Rates(1.0, 1.0, 1.0, 1.0)

        var coinSetDirty: bool = false
        var coinSetUpdateListener: (() -> Unit)? = null


        fun SetCurrentAccountData(userData: AccountData) {
            currentUserData = userData
        }


        fun UpdateLocalMap(context: Context) {
            // Get current date, then compose json file URL address.
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1) // java calendar months are zero-based for some ungodly reason
            val year = calendar.get(Calendar.YEAR)

            val currentMapURL = "http://homepages.inf.ed.ac.uk/stg/coinz/$year/$month/$day/coinzmap.geojson"

            //Log.d("DEBUG", "pre download " + currentMapURL)

            currentMapURL.httpGet().responseString { request, response, result ->
                //do something with response
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        MakeToast(context, ex.message!!)
                    }
                    is Result.Success -> {
                        val dataJson = result.get()

                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()

                        // And just like magic, this ACTUALLY WORKS
                        val mapData = moshi.adapter(Map.Data::class.java).fromJson(dataJson)
                        ProcessMapData(mapData!!, context)

                    }
                }
            }

            //Log.d("DEBUG", "post download call " + currentMapURL)
        }

        // This method processes the map data recovered from the geoJSON file and takes care of any Firebase updates necessary.
        fun ProcessMapData(data: Map.Data, context: Context) {
            // First, do the easy stuff and update the "pure" buy/sell rates for the day. We don't need to keep those in the DB.
            dailyPureRates = data.rates
            // Also grab the timestamp. We need it.
            dailyTimestamp = data.dateGenerated

            // Reset daily deposit quota if the user is logging in on a new day.
            if (currentUserData.lastLoginTimestamp != dailyTimestamp) {
                currentUserData.lastLoginTimestamp = dailyTimestamp
                currentUserData.dailyDepositsLeft = GetDailyDepositLimit()
            }


            //
            // MAP DATA WORKFLOW:
            // Every user "document" in the Firebase "users" collection will keep a subcollection called "Coins".
            // This subcollection will consist of documents, each ID'd with the current daily timestamp.
            // Every field in this document will be the ID of a taken coin. If a coin's in there, it's been taken already.

            // First, get current coin situations from the Firebase.
            // If the login is successful, we update local data.
            val firestore = FirebaseFirestore.getInstance()
            val fbAuth = FirebaseAuth.getInstance()
            val usersCol = firestore.collection("Users")
            val coinDoc = usersCol
                    .document(fbAuth.currentUser!!.uid)
                    .collection("coins")
                    .document(dailyTimestamp)

            // Well, we need to get it from Firebase first:
            coinDoc.get().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    MakeToast(context, "Could not retrieve coin data.\n${task.exception?.message}")
                } else {
                    val coinsTaken = mutableListOf<String>()
                    // Get all field names from the collection. They are actually the coin IDs we need.
                    task.result.data!!.entries.mapTo(coinsTaken) { it.key }

                    currentCoinsMap.clear()
                    for (coin in data.features) {
                        val coinID = coin.properties.id
                        // Skip any coins already taken
                        if (coinsTaken.contains(coinID)) continue

                        // Add all others to map
                        val info = Map.CoinInfo(
                                Currency.valueOf(coin.properties.currency),
                                coin.properties.value,
                                LatLng(coin.geometry.coordinates[1], coin.geometry.coordinates[0])
                        )
                        currentCoinsMap[coinID] = info

                        // Notify the map (whether or not it's listening) that it needs to refresh itself.
                        coinSetDirty = true
                        coinSetUpdateListener?.invoke() // this really does work
                    }

                }
            }


            TriggerUIUpdates()

        }

        // Called when coins are to be grabbed off the map
        // what a type!... but it is easiest to work with it like this.
        fun GrabCoins(coinIDs: HashSet<String>) {
            // Yes, I realise we enumerate the map twice. However, there's only 50 coins on it at most, and I'd rather be safe.
            // Fancy things aren't always necessary... especially when we can remove while iterating!
            val iter = currentCoinsMap.entries.iterator()
            val coinsTakenMap = mutableMapOf<String, Any>()
            while (iter.hasNext()) {
                val coin = iter.next()

                // Ignore all coins we're not grabbing
                if (!coinIDs.contains(coin.key)) continue

                // If the coin is to be taken, remove it from the map and increment respective currency by its amount.
                val amount = (coin.value.value * 100).toInt() / 100.0 // doing the rounding now to keep errors at bay
                val walletSize = GetWalletSize() // And forbid coins from exceeding wallet sizes
                when (coin.value.currency) {
                    Currency.GOLD -> {} // Again, this should never happen
                    Currency.DOLR -> currentUserData.spares.dolr = min(amount + currentUserData.spares.dolr, walletSize)
                    Currency.PENY -> currentUserData.spares.peny = min(amount + currentUserData.spares.peny, walletSize)
                    Currency.SHIL -> currentUserData.spares.shil = min(amount + currentUserData.spares.shil, walletSize)
                    Currency.QUID -> currentUserData.spares.quid = min(amount + currentUserData.spares.quid, walletSize)
                }

                // Firestore doesn't want to use lists, so we need to make ANOTHER map instead.
                coinsTakenMap[coin.key] = true // just set a bool for no reason

                // And finally, don't forget to DELETE THE COIN FROM THE MAP!
                iter.remove()
            }

            // Update coin set and trigger regular UI updates.
            coinSetDirty = true
            coinSetUpdateListener?.invoke()
            TriggerUIUpdates()

            // Save new details to Firebase.
            val firestore = FirebaseFirestore.getInstance()
            val fbAuth = FirebaseAuth.getInstance()
            val usersCol = firestore.collection("Users")
            val coinDoc = usersCol
                    .document(fbAuth.currentUser!!.uid)
                    .collection("coins")
                    .document(dailyTimestamp)
            coinDoc.set(coinsTakenMap, SetOptions.merge()) // We shouldn't need a listener.
            UpdateFirebaseData()

            Log.d("AYYYYYY", "GRABBED COINS! currentMapDat")
        }

        // Deposit an amount of "spare change" into the respective bank balance and update quota.
        fun DepositCoins(currency: Currency, amount: Double) {
            currentUserData.dailyDepositsLeft -= amount
            when (currency) {
                Currency.GOLD -> {
                } // Again, this should never happen
                Currency.DOLR -> {
                    currentUserData.balances.dolr += amount
                    currentUserData.spares.dolr -= amount
                }
                Currency.PENY -> {
                    currentUserData.balances.peny += amount
                    currentUserData.spares.peny -= amount
                }
                Currency.SHIL -> {
                    currentUserData.balances.shil += amount
                    currentUserData.spares.shil -= amount
                }
                Currency.QUID -> {
                    currentUserData.balances.quid += amount
                    currentUserData.spares.quid -= amount
                }
            }

            UpdateFirebaseData()
            TriggerUIUpdates()
        }

        fun BuySellCoins(currency: Currency, currencyDelta: Double, goldDelta: Double) {

            // We need to round precision off to two decimals for Gold, to prevent rounding errors in the future.
            val goldDeltaClean = (goldDelta * 100).toInt() / 100.0

            currentUserData.balanceGold += goldDeltaClean
            when (currency) {
                Currency.GOLD -> {
                } // Again, this should never happen
                Currency.DOLR -> {
                    currentUserData.balances.dolr += currencyDelta
                }
                Currency.PENY -> {
                    currentUserData.balances.peny += currencyDelta
                }
                Currency.SHIL -> {
                    currentUserData.balances.shil += currencyDelta
                }
                Currency.QUID -> {
                    currentUserData.balances.quid += currencyDelta
                }
            }

            UpdateFirebaseData()
            TriggerUIUpdates()
        }

        fun UpdateFirebaseData() {
            val firestore = FirebaseFirestore.getInstance()
            val curUserID = FirebaseAuth.getInstance().currentUser!!.uid
            firestore.collection("Users").document(curUserID).set(currentUserData)
        }


        fun SubscribeForUIUpdates(function: () -> Unit) {
            UIListeners.add(function)
        }

        private fun TriggerUIUpdates() {
            for (listener in UIListeners) {
                listener.invoke()
            }
        }


        fun GetUsername(): String {
            return currentUserData.username
        }


        fun GetGrabRadiusInMetres(): Int {
            return 25
        }

        fun GetBalance(currency: Currency): Double {
            return when (currency) {
                Currency.GOLD -> currentUserData.balanceGold // gold balance is separate
                Currency.DOLR -> currentUserData.balances.dolr
                Currency.PENY -> currentUserData.balances.peny
                Currency.SHIL -> currentUserData.balances.shil
                Currency.QUID -> currentUserData.balances.quid
            }
        }

        fun GetChange(currency: Currency): Double {
            return when (currency) {
                Currency.GOLD -> currentUserData.balanceGold // gold has no spare change, so we'll go with this instead
                Currency.DOLR -> currentUserData.spares.dolr
                Currency.PENY -> currentUserData.spares.peny
                Currency.SHIL -> currentUserData.spares.shil
                Currency.QUID -> currentUserData.spares.quid
            }
        }

        // How many coins of each type (except GOLD) can be held at any one time.
        fun GetWalletSize(): Double =
                if (currentUserData.team == Team.EleventhEchelon)
                    Experience.GetLevelE11(currentUserData.experience).walletSize.toDouble()
                else Experience.GetLevelCD(currentUserData.experience).walletSize.toDouble()


        fun GetDailyDepositsLeft(): Double {
            return currentUserData.dailyDepositsLeft
        }

        fun GetDailyDepositLimit(): Double {
            return 25.0
        }

        // The rates of currencies to gold, as per the map file, without any "bank penalties".
        fun GetPureRate(currency: Currency): Double {
            return when (currency) {
                Currency.GOLD -> 1.0 // this should never be triggered anyway
                Currency.DOLR -> dailyPureRates.dolr
                Currency.PENY -> dailyPureRates.peny
                Currency.SHIL -> dailyPureRates.shil
                Currency.QUID -> dailyPureRates.quid
            }
        }

        fun GetBankCommissionRate(): Double =
                if (currentUserData.team == Team.EleventhEchelon)
                    Experience.GetLevelE11(currentUserData.experience).bankCommissionPercent
                else 4.5


        // Buying currency is more expensive; selling currency is cheaper, i.e. always done at worse prices.
        fun GetBuyPrice(currency: Currency): Double {
            return GetPureRate(currency) * (1 + (GetBankCommissionRate() / 100.0))
        }

        fun GetSellPrice(currency: Currency): Double {
            return GetPureRate(currency) * (1 - (GetBankCommissionRate() / 100.0))
        }

    }


}