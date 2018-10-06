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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import eu.zerovector.coinz.Components.CryptomessageView
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Data.MessageDifficulty
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
        fun makeMessageView(index: Int, diff: MessageDifficulty, price: Int) {
            val view = CryptomessageView(context!!)
            view.SetData(index, diff, price)

            // Safety check for re-logs: if the message's already been decrypted, flag it as such.
            if (DataManager.GetMessageDecrypted(diff, index)) view.SetDecrypted()

            // Link listeners up.
            view.SetDecryptionListener { OnMessageDecryptionAttempted(view, index, diff, price) }

            messageViews.add(Triple(view, index, diff))
            cryptoContainer.addView(view)
        }

        // Get settings, then create views for all of them necessary
        val settings = DataManager.dailyCryptoSettings
        for (i in settings.easyMessagePrices.indices) {
            makeMessageView(i, MessageDifficulty.Easy, settings.easyMessagePrices[i])
        }
        for (i in settings.mediumMessagePrices.indices) {
            makeMessageView(i, MessageDifficulty.Medium, settings.mediumMessagePrices[i])
        }
        for (i in settings.hardMessagePrices.indices) {
            makeMessageView(i, MessageDifficulty.Hard, settings.hardMessagePrices[i])
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
        }
        else if (progressCD >= progressE11 && progressCD >= 100) {
            pbReadinessE11.progress = 0
            pbReadinessCD.progress = 100
            lblReadinessE11.text = "ELEVENTH ECHELON: DEFEAT!"
            lblReadinessCD.text = "CRIMSON DAWN: VICTORY!"
        }
        else {
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


    fun OnMessageDecryptionAttempted(view: CryptomessageView, index: Int, diff: MessageDifficulty, price: Int) {
        // If the message has already been decrypted (and somehow not flagged as such?), flag it:
        if (DataManager.GetMessageDecrypted(diff, index)) {
            view.SetDecrypted()
            MakeToast(context!!, "Message already decrypted.")
            return
        }
        if (DataManager.GetCompute() < price) {
            MakeToast(context!!, "Insufficient compute available.")
            return
        }

        // If all's well...
        // TODO run firestore transaction to increment faction readiness amount and to update compute amounts for us.

    }


}
