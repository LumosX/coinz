package eu.zerovector.coinz.Data

import android.content.Context
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.BITS_PER_DIFFICULTY
import eu.zerovector.coinz.Utils.Companion.MakeToast
import java.util.*
import kotlin.math.min


class DataManager {

    // Stuff inside the companion object is static, apparently
    companion object {
        // SharedPrefs constant: we do use this at one point
        const val PREFS_NAME = "CoinZConfig"

        private var UIListeners: MutableList<() -> Unit> = mutableListOf()


        private lateinit var currentUserData: AccountData
        var dailyTimestamp: String = ""
        var currentCoinsMap: HashMap<String, Map.CoinInfo> = hashMapOf() // the set of all coin IDs taken today.
        var dailyPureRates: Map.Rates = Map.Rates(1.0, 1.0, 1.0, 1.0)

        var coinSetDirty: bool = false
        var coinSetUpdateListener: (() -> Unit)? = null

        // Use a single instance of the message settings for all our purposes. We don't want them to change.
        private var cryptosettings: CryptoSettings? = null
        // If the current settings (in the backing field) are null, generate a new one.
        // The seed is formed by the current user ID and the current daily timestamp.
        // Thus every user gets different messages, but they remain the same during the day.
        val dailyCryptoSettings: CryptoSettings
            get() {
                if (cryptosettings == null) {
                    cryptosettings = CryptoSettings.Generate(
                            (FirebaseAuth.getInstance().currentUser!!.uid + dailyTimestamp).hashCode().toLong()
                    )
                }
                return cryptosettings!!
            }


        fun SetCurrentAccountData(userData: AccountData) {
            currentUserData = userData
        }

        fun GetCurrentAccountData(): AccountData {
            return currentUserData
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

            val firestore = FirebaseFirestore.getInstance()
            val fbAuth = FirebaseAuth.getInstance()
            val usersCol = firestore.collection("Users")

            // Reset daily deposit quota if the user is logging in on a new day.
            if (currentUserData.lastLoginTimestamp != dailyTimestamp) {
                currentUserData.lastLoginTimestamp = dailyTimestamp
                currentUserData.dailyDepositsLeft = GetDailyDepositLimit()
                // Reset the "decrypted messages" field as well.
                currentUserData.dailyMessagesDecrypted = 0
                cryptosettings = null // Reset the cryptosettings as well, just to be safe. It's a new day after all...

                // Update these values in the database right now, because I don't feel particularly safe not doing it.
                val curUserDoc = usersCol.document(fbAuth.currentUser!!.uid)
                val batch = firestore.batch()
                batch.update(curUserDoc, "lastLoginTimestamp", dailyTimestamp)
                batch.update(curUserDoc, "dailyDepositsLeft", currentUserData.dailyDepositsLeft)
                batch.update(curUserDoc, "dailyMessagesDecrypted", 0)
                batch.commit()
            }


            //
            // MAP DATA WORKFLOW:
            // Every user "document" in the Firebase "users" collection will keep a subcollection called "Coins".
            // This subcollection will consist of documents, each ID'd with the current daily timestamp.
            // Every field in this document will be the ID of a taken coin. If a coin's in there, it's been taken already.

            // First, get current coin situations from the Firebase.
            // If the login is successful, we update local data.
            val coinDoc = usersCol
                    .document(fbAuth.currentUser!!.uid)
                    .collection("Coins")
                    .document(dailyTimestamp)

            // Well, we need to get it from Firebase first:
            coinDoc.get().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    MakeToast(context, "Could not retrieve coin data.\n${task.exception?.message}")
                } else {
                    val coinsTaken = mutableListOf<String>()
                    // Get all field names from the collection. They are actually the coin IDs we need.
                    task.result.data?.entries?.mapTo(coinsTaken) { it.key }

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
                val amount = (coin.value.value * 100).toInt() // cast to int to keep rounding errors at bay
                val walletSize = GetWalletSize() // And forbid coins from exceeding wallet sizes
                when (coin.value.currency) {
                    Currency.GOLD -> {
                    } // Again, this should never happen
                    Currency.DOLR -> currentUserData.spares.dolr = min(amount + currentUserData.spares.dolr, walletSize)
                    Currency.PENY -> currentUserData.spares.peny = min(amount + currentUserData.spares.peny, walletSize)
                    Currency.SHIL -> currentUserData.spares.shil = min(amount + currentUserData.spares.shil, walletSize)
                    Currency.QUID -> currentUserData.spares.quid = min(amount + currentUserData.spares.quid, walletSize)
                }
                // Remember to give some XP for collecting coins.
                currentUserData.experience += coin.value.value.toInt() // the amount of the coin, rounded down.

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
                    .collection("Coins")
                    .document(dailyTimestamp)
            coinDoc.set(coinsTakenMap, SetOptions.merge()) // We shouldn't need a listener.
            UpdateFirebaseData()

            //Log.d("AYYYYYY", "GRABBED COINS! currentMapDat")
        }

        // Deposit an amount of "spare change" into the respective bank balance and update quota.
        fun DepositCoins(currency: Currency, amount: Int) {
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

        fun BuySellCoins(currency: Currency, currencyDelta: Int, goldDelta: Int) {

            // We need to round precision off to two decimals for Gold, to prevent rounding errors in the future.
            //val goldDeltaClean = (goldDelta * 100).toInt() / 100.0
            // No longer a factor when migrating to ints

            currentUserData.balanceGold += goldDelta
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

        fun TriggerUIUpdates() {
            for (listener in UIListeners) {
                listener.invoke()
            }
        }


        ////// ACCESS FUNCTIONS
        fun GetUsername(): String {
            return currentUserData.username
        }

        fun GetTeam(): Team {
            return currentUserData.team
        }

        fun GetXP(): Int {
            return currentUserData.experience
        }

        fun GetGrabRadiusInMetres(): Int {
            return 25
        }

        fun GetCompute(): Int {
            return currentUserData.compute
        }

        fun SetCompute(compute: Int) {
            currentUserData.compute = compute
        }

        fun GetBalance(currency: Currency): Int {
            return when (currency) {
                Currency.GOLD -> currentUserData.balanceGold // gold balance is separate
                Currency.DOLR -> currentUserData.balances.dolr
                Currency.PENY -> currentUserData.balances.peny
                Currency.SHIL -> currentUserData.balances.shil
                Currency.QUID -> currentUserData.balances.quid
            }
        }

        fun GetChange(currency: Currency): Int {
            return when (currency) {
                Currency.GOLD -> currentUserData.balanceGold // gold has no spare change, so we'll go with this instead
                Currency.DOLR -> currentUserData.spares.dolr
                Currency.PENY -> currentUserData.spares.peny
                Currency.SHIL -> currentUserData.spares.shil
                Currency.QUID -> currentUserData.spares.quid
            }
        }

        // How many coins of each type (except GOLD) can be held at any one time.
        fun GetWalletSize(): Int =
                if (currentUserData.team == Team.EleventhEchelon)
                    Experience.GetLevelE11(currentUserData.experience).walletSize * 100
                else Experience.GetLevelCD(currentUserData.experience).walletSize * 100


        fun GetDailyDepositsLeft(): Int {
            return currentUserData.dailyDepositsLeft
        }

        fun GetDailyDepositLimit(): Int {
            return 2500
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


        ////// STUFF FOR THE CRYPTOMESSAGES
        // Gets the encryption status of a daily message.
        fun GetMessageDecrypted(difficulty: MessageDifficulty, index: Int): bool {
            if (index < 0) return false
            // In this case, we 'AND' the number and the message offset to check if the "flag" has been set.
            val offset = 2.toLong() shl (index + BITS_PER_DIFFICULTY * difficulty.ordinal)
            return (currentUserData.dailyMessagesDecrypted and offset) > 0
        }

        // Updates a message status and returns the resulting mask. TO BE USED IN THE UPDATE TRANSACTION (does not update local data).
        fun PretendSetMessageDecrypted(difficulty: MessageDifficulty, index: Int): Long {
            if (index < 0) return currentUserData.dailyMessagesDecrypted

            // Mask individual messages as bits in the larger integer. Just set the "bit" we need.
            // Note that I've not implemented the ability to RESET bits, so be careful with this.
            // (We don't ever NEED to reset bits, which is why that's the case)
            val offset = 2.toLong() shl (index + BITS_PER_DIFFICULTY * difficulty.ordinal)
            // To set a bit, just 'OR' it.
            return (currentUserData.dailyMessagesDecrypted or offset)
        }

        // Force-sets the message to "decrypted" and updates local data, as well as the database. To be used in extreme cases.
        fun ForceSetMessageDecrypted(difficulty: MessageDifficulty, index: Int) {
            currentUserData.dailyMessagesDecrypted = PretendSetMessageDecrypted(difficulty, index)

            // Update the DB just in case the user's trying to pretend he's smart.
            val firestore = FirebaseFirestore.getInstance()
            val fbAuth = FirebaseAuth.getInstance()
            val usersCol = firestore.collection("Users")
            val curUserDoc = usersCol.document(fbAuth.currentUser!!.uid)
            curUserDoc.update("dailyMessagesDecrypted", currentUserData.dailyMessagesDecrypted)

        }


    }


}