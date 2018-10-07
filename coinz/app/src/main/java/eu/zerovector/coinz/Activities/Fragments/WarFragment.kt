package eu.zerovector.coinz.Activities.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import eu.zerovector.coinz.Components.CryptomessageView
import eu.zerovector.coinz.Data.*
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.BONUS_VALUE_MULTIPLIER
import eu.zerovector.coinz.R
import eu.zerovector.coinz.Utils.Companion.MakeToast
import eu.zerovector.coinz.Utils.Companion.toString

class WarFragment : Fragment() {

    private lateinit var pbReadinessE11: ProgressBar
    private lateinit var lblReadinessE11: TextView
    private lateinit var pbReadinessCD: ProgressBar
    private lateinit var lblReadinessCD: TextView
    private lateinit var lblComputeAvailable: TextView
    private lateinit var btnBuyCompute: Button
    private lateinit var cryptoContainer: LinearLayout

    private lateinit var messageViews: MutableList<Triple<CryptomessageView, Int, MessageDifficulty>>

    private var progressE11: Double = 11.0
    private var progressCD: Double = 11.0

    private var isDecryptingMessage = false // again, a flag to prevent us from retrying the same transaction.


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_war, container, false)

        // Get UI bits
        pbReadinessE11 = view.findViewById(R.id.pbReadinessE11)
        lblReadinessE11 = view.findViewById(R.id.lblReadinessE11)
        pbReadinessCD = view.findViewById(R.id.pbReadinessCD)
        lblReadinessCD = view.findViewById(R.id.lblReadinessCD)
        lblComputeAvailable = view.findViewById(R.id.lblComputeAvailable)
        btnBuyCompute = view.findViewById(R.id.btnBuyCompute)
        cryptoContainer = view.findViewById(R.id.layoutCryptoMessages)

        btnBuyCompute.setOnClickListener { OnBuyComputeClicked() }

        isDecryptingMessage = false

        // Now hook this up to the War status in Firebase
        val warDoc = FirebaseFirestore.getInstance()
                .collection("War")
                .document("Status")
        warDoc.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, exception ->
            // Abort if an exception occurred
            if (exception != null) return@EventListener

            // If we've retrieved any data at all, update the message list and then update the UI.
            if (snapshot != null && snapshot.exists()) {
                // Firebase sometimes returns these as LONGS if they've got no decimal places (instead of the doubles that they should be), which is dumb as fuck.
                val e11 = snapshot.get("E11")
                val cd = snapshot.get("CD")
                // ... all right, this is weird. But it works.
                progressE11 = (e11 as? Long)?.toDouble() ?: e11 as Double
                progressCD = (cd as? Long)?.toDouble() ?: cd as Double

                UpdateUI()
            }
        })


        CreateMessageViews()

        return view
    }

    // This creates the actual views for the messages and populates the container with them.
    // More of a mess, but it's more efficient than creating new ones all the time.
    fun CreateMessageViews() {
        messageViews = mutableListOf()

        // But let's get a little helper first:
        fun makeMessageView(index: Int, diff: MessageDifficulty, data: CryptoSettings.MessageInfo) {
            val view = CryptomessageView(context!!)
            view.SetData(index, diff, data)

            // Safety check for re-logs: if the message's already been decrypted, flag it as such.
            if (DataManager.GetMessageDecrypted(diff, index)) view.SetDecrypted()

            // Link listeners up.
            view.SetDecryptionListener { OnMessageDecryptionAttempted(view, index, diff, data) }

            messageViews.add(Triple(view, index, diff))
            cryptoContainer.addView(view)
        }

        // Get settings, then create views for all of them necessary
        val settings = DataManager.dailyCryptoSettings
        for (i in settings.easyMessages.indices) {
            makeMessageView(i, MessageDifficulty.Easy, settings.easyMessages[i])
        }
        for (i in settings.mediMessages.indices) {
            makeMessageView(i, MessageDifficulty.Medium, settings.mediMessages[i])
        }
        for (i in settings.hardMessages.indices) {
            makeMessageView(i, MessageDifficulty.Hard, settings.hardMessages[i])
        }

    }

    @SuppressLint("SetTextI18n")
    fun UpdateUI() {
        // Update progress bars and labels
        // Handle the "Victory" cases first
        if (progressE11 >= progressCD && progressE11 >= 100) {
            pbReadinessE11.progress = 100
            pbReadinessCD.progress = 0
            lblReadinessE11.text = "ELEVENTH ECHELON: VICTORY!"
            lblReadinessCD.text = "CRIMSON DAWN: DEFEAT!"
        } else if (progressCD >= progressE11 && progressCD >= 100) {
            pbReadinessE11.progress = 0
            pbReadinessCD.progress = 100
            lblReadinessE11.text = "ELEVENTH ECHELON: DEFEAT!"
            lblReadinessCD.text = "CRIMSON DAWN: VICTORY!"
        } else {
            pbReadinessE11.progress = progressE11.toInt()
            pbReadinessCD.progress = progressCD.toInt()
            lblReadinessE11.text = "ELEVENTH ECHELON: (${progressE11.toString(3)}%)"
            lblReadinessCD.text = "CRIMSON DAWN: (${progressCD.toString(3)}%)"
        }

        // And the current Compute amount
        lblComputeAvailable.text = "Compute available: ☄" + DataManager.GetCompute()

        // Also update any errant decryption buttons
        for (v in messageViews) {
            if (DataManager.GetMessageDecrypted(v.third, v.second)) v.first.SetDecrypted()
        }

    }


    fun OnBuyComputeClicked() {

        // todo create an alert dialog with a spinner that allows for the selection of a "Provider" (partly faction-specific).
        // Providers probably charge a fixed price for a fixed amount of compute provided.
        // todo consider giving different messages of the same difficulty slightly different readiness bonuses. It's just weird otherwise.

    }



    fun OnMessageDecryptionAttempted(view: CryptomessageView, index: Int, diff: MessageDifficulty, data: CryptoSettings.MessageInfo) {
        if (isDecryptingMessage) return

        // If the message has already been decrypted (and somehow not flagged as such?), flag it:
        if (DataManager.GetMessageDecrypted(diff, index)) {
            view.SetDecrypted()
            MakeToast(context!!, "Message already decrypted.")
            return
        }
        if (DataManager.GetCompute() < data.price) {
            MakeToast(context!!, "Insufficient compute available.")
            return
        }

        // If all's well, run a transaction to update the war status and to update compute amounts for us.
        val firestore = FirebaseFirestore.getInstance()
        val curUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val curUserDoc = firestore.collection("Users").document(curUserID)
        val warDoc = firestore.collection("War").document("Status")

        MakeToast(context!!, "Decrypting message...")

        isDecryptingMessage = true // "Lock" operations until the transaction succeeds or fails

        val curTeam = DataManager.GetTeam()

        firestore.runTransaction { transaction ->
            // A document must exist for both users, so I can unwrap!! the nullable type
            val currentAccount = transaction.get(curUserDoc).toObject(AccountData::class.java)!!
            val warDocFieldName = if (curTeam == Team.EleventhEchelon) "E11" else "CD"
            val rawProgress = transaction.get(warDoc)[warDocFieldName]

            // SET MESSAGE DECRYPTED "BIT" AND UPDATE COMPUTE BALANCE
            val newMessageMask = DataManager.PretendSetMessageDecrypted(diff, index)
            currentAccount.dailyMessagesDecrypted = newMessageMask
            currentAccount.compute -= data.price
            val XPBonus = data.bonusNoMultiplier * 2 // add XP equal to 2x the int value of the bonus (i.e. 6-80)
            currentAccount.experience += XPBonus
            transaction.set(curUserDoc, currentAccount)


            // UPDATE TEAM'S READINESS
            // "decode" value type first (it might be Long or Double, we don't know)
            var teamProgress = (rawProgress as? Long)?.toDouble() ?: rawProgress as Double
            teamProgress += data.bonusNoMultiplier * BONUS_VALUE_MULTIPLIER
            transaction.update(warDoc, warDocFieldName, teamProgress)


            // And if the transaction goes well, might as well use the updated values of the current account for UI updates.
            // All of this should be safe, since transactions are atomic, right?
            Pair(currentAccount, XPBonus)
        }.addOnCompleteListener {
            val newData = it.result!!.first
            val gainedXP = it.result!!.second
            DataManager.SetCurrentAccountData(newData)
            MakeToast(context!!, "Message decrypted! $gainedXP XP received!")
            UpdateUI() // no need for a global UI update, because this screen is pretty much on its own
            isDecryptingMessage = false

        }.addOnFailureListener {
            MakeToast(context!!, "Could not decrypt message. " + (it.message ?: "An unknown error occurred."))
            isDecryptingMessage = false
        }


}


}
