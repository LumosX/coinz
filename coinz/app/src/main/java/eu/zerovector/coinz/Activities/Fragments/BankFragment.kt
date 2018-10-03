package eu.zerovector.coinz.Activities.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import eu.zerovector.coinz.Data.AccountData
import eu.zerovector.coinz.Data.Currency
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Data.Experience.Companion.GetLevelName
import eu.zerovector.coinz.Data.bool
import eu.zerovector.coinz.Extras.Companion.MakeToast
import eu.zerovector.coinz.Extras.Companion.toString
import eu.zerovector.coinz.R

class BankFragment : Fragment() {

    private lateinit var view: ViewGroup
    private lateinit var lblQuota: TextView
    private lateinit var lblCommission: TextView


    // We need this to prevent players from creating interleaving transactions.
    private var sendingTransactionInProgress: bool = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bank, container, false) as ViewGroup

        // And set username.
        view.findViewById<TextView>(R.id.lblWelcome).text = "Welcome, " + DataManager.GetUsername()
        lblQuota = view.findViewById(R.id.lblDepositQuota)
        lblCommission = view.findViewById(R.id.lblCommission)
        view.findViewById<Button>(R.id.btnSendMoney).setOnClickListener(::onSendSpareChangeClicked)
        SetQuotaText()

        DataManager.SubscribeForUIUpdates { SetQuotaText() }

        return view
    }


    @SuppressLint("SetTextI18n")
    private fun SetQuotaText() {
        lblQuota.text = "Daily deposit quota: " + DataManager.GetDailyDepositsLeft() + "/" + DataManager.GetDailyDepositLimit();
        lblCommission.text = "Current commission rate: " + DataManager.GetBankCommissionRate().toString(1) + "%"
    }


    @SuppressLint("SetTextI18n")
// Here we'll build an alert dialog and handle the money-sending aspect.
    fun onSendSpareChangeClicked(view: View) {
        if (sendingTransactionInProgress) return


        val dolr = DataManager.GetChange(Currency.DOLR)
        val peny = DataManager.GetChange(Currency.PENY)
        val shil = DataManager.GetChange(Currency.SHIL)
        val quid = DataManager.GetChange(Currency.QUID)

        if (dolr < 1 && peny < 1 && shil < 1 && quid < 1) {
            MakeToast(context!!, "Not enough change of any currency to send. Minimum amount = 1 coin.")
            return
        }

        // I'll be using the same strategy I used for the currency buttons. Messy, but I like it a lot.

        val alert = AlertDialog.Builder(context)
        alert.setTitle("SENDING SPARE CHANGE")
        alert.setMessage("You may send your extra spare change to any teammate to free up wallet space.\n" +
                "The minimum transaction amount is 1 unit of currency.\n" +
                "The sent coins will be converted to GOLD through your exchange rate, and will be delivered to the recipient instantly.\n" +
                "In addition, you receive experience worth twice the amount of coins (rounded down) sent for doing so.")

        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL

        val tbRecipient = EditText(context)
        tbRecipient.hint = "Recipient username (case-sensitive)"
        tbRecipient.inputType = InputType.TYPE_CLASS_TEXT
        linear.addView(tbRecipient)

        val menu = Spinner(context)
        val availableChoices = mutableListOf<String>()
        if (dolr >= 1) availableChoices.add("DOLR (max: ${dolr.toString(2)})")
        if (peny >= 1) availableChoices.add("PENY (max: ${peny.toString(2)})")
        if (shil >= 1) availableChoices.add("SHIL (max: ${shil.toString(2)})")
        if (quid >= 1) availableChoices.add("QUID (max: ${quid.toString(2)})")
        menu.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, availableChoices)

        linear.addView(menu)

        val tbAmount = EditText(context)
        tbAmount.hint = "Amount to send"
        tbAmount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        linear.addView(tbAmount)

        val text = TextView(context)
        text.text = "Input a valid amount to calculate result..."
        text.gravity = Gravity.CENTER
        linear.addView(text)

        linear.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        linear.setPadding(50, 10, 50, 10)

        // Adding padding on the editText itself makes it bad, so I'm using two layouts within each other to achieve the same result.
        val mainContainer = LinearLayout(context)
        mainContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mainContainer.addView(linear)
        alert.setView(mainContainer)

        // Attach a listener to recalculate the amount of GOLD sent as input changes
        tbAmount.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val amt = s.toString().toDoubleOrNull()
                if (amt == null || amt < 1) text.text = "Input a valid amount to calculate result"
                else {
                    // This is a horrible mess, I know, I know...
                    val selectedCurrency =
                            when {
                                menu.selectedItem.toString().startsWith("DOLR") -> Currency.DOLR
                                menu.selectedItem.toString().startsWith("PENY") -> Currency.PENY
                                menu.selectedItem.toString().startsWith("SHIL") -> Currency.SHIL
                                menu.selectedItem.toString().startsWith("QUID") -> Currency.QUID
                                else -> Currency.GOLD // impossible, but still
                            }
                    val amtToGold = (amt * DataManager.GetSellPrice(selectedCurrency) * 100).toInt() / 100.0
                    text.text = "Will send GOLD $amtToGold"
                }


            }
        })

        // And also handle what happens when the menu selection changes.
        menu.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                tbAmount.text.clear()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tbAmount.text.clear()
            }

        }




        alert.setPositiveButton("SEND") { _, _ ->
            // "Decipher" data first.
            val selectedAmount = tbAmount.text.toString().toDoubleOrNull()
            // Parse selected menu item
            val selectedCurrency =
                    when {
                        menu.selectedItem.toString().startsWith("DOLR") -> Currency.DOLR
                        menu.selectedItem.toString().startsWith("PENY") -> Currency.PENY
                        menu.selectedItem.toString().startsWith("SHIL") -> Currency.SHIL
                        menu.selectedItem.toString().startsWith("QUID") -> Currency.QUID
                        else -> null
                    }
            val maxCurrencyAmt = if (selectedCurrency == null) 0.0 else DataManager.GetChange(selectedCurrency)
            val recipientName = tbRecipient.text.toString().trim()
            // Handle all the error cases in here, he-he.
            when {
                recipientName == DataManager.GetUsername() -> {
                    MakeToast(context!!, "You don't need to send money to yourself!")
                    return@setPositiveButton
                }
                selectedCurrency == null -> {
                    MakeToast(context!!, "Invalid currency specified. How did you do that?")
                    return@setPositiveButton
                }
                selectedAmount == null -> {
                    MakeToast(context!!, "Invalid amount specified.")
                    return@setPositiveButton
                }
                selectedAmount < 1 -> {
                    MakeToast(context!!, "The minumum amount to send is 1 coin.")
                    return@setPositiveButton
                }
                selectedAmount > maxCurrencyAmt -> {
                    MakeToast(context!!, "You can't send more change than you have in your wallet.")
                    return@setPositiveButton
                }
            }


            // If nothing bad happened, try sending the ca$h to the recipient
            // First, "lock" this functionality until the transaction is completed.
            sendingTransactionInProgress = true

            val firestore = FirebaseFirestore.getInstance()
            val usersCol = firestore.collection("Users")
            // Only allow sending to teammates
            usersCol.whereEqualTo("username", tbRecipient.text.toString().trim())
                    .whereEqualTo("team", DataManager.GetTeam().toString())
                    .get()
                    .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                override fun onComplete(task: Task<QuerySnapshot>) {

                    // If the username doesn't exist, we can't send anything.
                    if (!task.isSuccessful || task.result.documents.size == 0) {
                        val errorMessage = task.exception?.message
                                ?: "Recipient username doesn't exist or recipient not on the same team."
                        failTransaction(errorMessage)
                    }
                    // Otherwise, however, DO send the cash.
                    else {
                        val recipientID = task.result.documents[0].id // pretty sure that this is safe, as we've already checked whether a doc exists.
                        val curUserID = FirebaseAuth.getInstance().currentUser!!.uid

                        // Decrement spare change on this account, increment gold on the recipient, and send the recipient a message.
                        // Better to do it here than in the DataManager, because we need to "unlock" this (sendingTransactionInProgress) at the end.
                        val batch = firestore.batch()

                        // INCREMENT GOLD FOR THE RECIPIENT
                        // all nulls are handled at this point
                        val selectedAmtInGold = (selectedAmount!! * DataManager.GetSellPrice(selectedCurrency!!) * 100).toInt() / 100.0
                        val recipientAccount = task.result.documents[0].toObject(AccountData::class.java)!!
                        recipientAccount.balanceGold += selectedAmtInGold
                        val recipientDoc = usersCol.document(recipientID)
                        batch.set(recipientDoc, recipientAccount)


                        // DECREMENT SPARES BALANCE FOR US
                        val curUserData = DataManager.GetCurrentAccountData()
                        curUserData.experience += selectedAmount.toInt() * 2 // Add some XP as well, why not.
                        when (selectedCurrency) {
                            Currency.GOLD -> {} // Again, this should never happen
                            Currency.DOLR -> curUserData.spares.dolr -= selectedAmount
                            Currency.PENY -> curUserData.spares.peny -= selectedAmount
                            Currency.SHIL -> curUserData.spares.shil -= selectedAmount
                            Currency.QUID -> curUserData.spares.quid -= selectedAmount
                        }
                        val curUserDoc = firestore.collection("Users").document(curUserID)
                        batch.set(curUserDoc, curUserData)
                        DataManager.SetCurrentAccountData(curUserData)


                        // LEAVE A MESSAGE FOR THE RECIPIENT
                        val recipientMessageDoc = usersCol
                                .document(recipientID)
                                .collection("messages")
                                .document("transactions")
                        // the message has an ID equal to "rank" + "username" and a value of the amount sent in GOLD
                        val str = GetLevelName(curUserData.team, curUserData.experience) + " " + curUserData.username
                        val newMessage = hashMapOf(Pair(str, selectedAmtInGold)) as Map<String, Any>
                        batch.set(recipientMessageDoc, newMessage, SetOptions.merge())



                        // Now commit the batch and pray it works.
                        batch.commit().addOnCompleteListener {
                            if (it.isSuccessful) {
                                MakeToast(context!!, "Coins sent successfully!")
                                DataManager.TriggerUIUpdates()
                                sendingTransactionInProgress = false
                            }
                            else {
                                failTransaction(it.exception?.message ?: "Failed to send coins. Unknown error.")
                            }
                        }
                    }
                }


                // and because this is essentially java-like, we can hack this cheeky function in the listener.
                fun failTransaction(innerError: String) {
                    MakeToast(context!!, innerError)
                    sendingTransactionInProgress = false
                }

            })

        }

        alert.setNegativeButton("CANCEL") { _, _ -> }

        alert.show()


    }

}




