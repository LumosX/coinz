package eu.zerovector.coinz.Activities.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import eu.zerovector.coinz.Components.ExpandableMessageView
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Data.Team
import eu.zerovector.coinz.R

class MailFragment : Fragment() {

    private lateinit var messageContainer: LinearLayout
    private lateinit var transactionMessageList: MutableList<MessageDetails>
    private var updateListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val curView = inflater.inflate(R.layout.fragment_mail, container, false)

        messageContainer = curView.findViewById(R.id.layoutMessages)

        transactionMessageList = mutableListOf()

        UpdateMessages() // do this manually once in case no transaction messages show up ("blank events" don't trigger)
        // Hook transaction messages into Firebase.
        var curUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val transactionMessageDoc = FirebaseFirestore.getInstance().collection("Users").document(curUserID)
                .collection("Messages")
                .document("Transactions")
        transactionMessageDoc.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, exception ->
            // Abort if an exception occurred
            if (exception != null) return@EventListener

            // If we've retrieved any data at all, update the message list and then update the UI.
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data!!
                transactionMessageList.clear()
                for (message in data) {
                    val msgVal = (message.value as String).split("*")
                    transactionMessageList.add(MessageDetails(message.key, msgVal[0], msgVal[1].toDouble()))
                }

                UpdateMessages()
            }
        })



        return curView

    }

    fun UpdateMessages() {
        // Compose messages. Contents depend on the team.
        val isE11 = DataManager.GetTeam() == Team.EleventhEchelon
        val transactionSender = if (isE11) "Echelon OpsControl" else "Crimson Mother"
        val transactionSubject = if (isE11) "Funding received" else "Cash from a mate"

        // Purge layout, then add transaction messages individually.
        messageContainer.removeAllViews()
        for (message in transactionMessageList) {
            val newMsgView = ExpandableMessageView(context!!)
            newMsgView.SetSenderAndSubject(transactionSender, "$transactionSubject [${message.timestamp}]")
            val msgContent = if (isE11)
                "You have received a transaction from ${message.sender}. Sum received: GOLD ${message.amountGold}. Remember to send additional funds to your colleagues in order to ensure maximum progress."
            else
                "Your buddy ${message.sender} has sent you some money, namely GOLD ${message.amountGold} in total. Now be a good boy and make Mother happy by sending your extra cash around to your other pals."
            newMsgView.SetMessageText(msgContent)
            messageContainer.addView(newMsgView)
        }

        // Finally, add the "first message"
        val firstMsgView = ExpandableMessageView(context!!)
        val firstMsg = if (isE11) firstMessageE11 else firstMessageCD
        firstMsgView.SetSenderAndSubject(firstMsg.first, firstMsg.second)
        firstMsgView.SetMessageText(firstMsg.third)
        messageContainer.addView(firstMsgView)
    }


    data class MessageDetails(val sender: String, val timestamp: String, val amountGold: Double)

    // And now for some great FLAVOUR:
    // (You might wanna turn word wrap on for this one...)
    private val firstMessageE11 = Triple("Col. James Hendricks", "The nature of this operation.", """Analyst,

        We've received word that a Soviet-era nuclear warhead, codenamed "Project Nocturnal Ziggurat", has recently become "lost" by the Kyrgyzstani government. We don't know who has it or where it is, but we're tracking lots of dark cryptotraffic surrounding it, located around the centre of beautiful Edinburgh. We need all boots on the ground at this point, and your participation may likely set you up for a rapid promotion in the Echelon.

        Our task is simple: we must decrypt as many of those secret messages as we can. However, we're short on money since the Brexit, and you'll have to buy your own processing power ("Compute"). Collect cryptocoins scattered around the hot zone, then use them to purchase Compute and decrypt the messages. Every message decrypted will bring us closer to locating that bomb and handling it safely without any danger to the public.

        We're using a certain bank to facilitate all financial transactions. They're giving our agents a special commission rate on cryptocurrency exchange rates, so make good use of that. However, the bank has a "daily deposit quota", and their systems are not built to work around it. The only way to circumvent this delay -- in other words, to speed up your information collection rate -- is to send your excess coins to your colleagues. Operations Control will notify you when someone has sent you a sum.

        We are also aware that a mercenary band known as the "Crimson Dawn" is also operating in the area. They do NOT hold the bomb, this much we've established, but they're trying to find us before we do, and if they manage that, there's no telling where the bomb will find itself, or when it'll go off. That said, DO NOT ENGAGE any Crimson Dawn agents in the area. A direct conflict would likely cause the party holding the bomb to lay low, and we'll lose them if they do.

        You have your orders, Analyst. Find me that bomb before it's too late. God save the Queen, and God be with us all.

        Sincerely,
        Colonel James Hendricks
        Eleventh Echelon Commander-in-Chief
    """.trimIndent())

    private val firstMessageCD = Triple("Boss Jackal", "Your f▩cking orders", """Oi, recruit,

        You're rolling with the big boys now, so you better not disappoint us. Pleased to have you with us. Anyway, here's the deal: some scunner stole a nuke from some fake Russians. The nuke was called "Project Nocturnal Ziggurat", and it's apparently a pretty powerful little thing. I don't need to tell you that we're getting paid A LOT of money to find that baby and take her someplace safe. We don't know who has it, but our little birds are catching a LOT of secret comms around the Edinburgh city centre. I hate this city, but I also hate losing money. I want YOU ALL to FIND MY BOMB. And then we can all get a nice bonus for our trouble and drink cocktails at the Bahamas, right?

        Your job's easy. Decrypt me those messages. I don't care how you do it, buy your own damn Compute. You can do that by collecting the crypto that's literally lying around on the street. Then you pay some hackers off (we've cut deals for cheaper Compute with 'em) and Bob's your damn uncle.

        It's worth mentioning that some f▩cking government agents are snooping around the place as well. They don't have the nuke yet, but they're trying to get it. DO NOT ENGAGE THEM DIRECTLY, or I'll f▩cking shoot you myself. Work around them. Find the nuke before they do. Get rich in the process. Idea's simple, no need for stupid things. (Also, their software's apparently rubbish, so you'll be able to gather more money than an agent at any one time. It ALWAYS pays off to be one of us, recruit.)

        By the way, we're using some Swiss bank to handle all the money. Bastards are ripping us off with their commission rates, but they're the only ones we could use. And since they're probably running on an SQL-based system from the '90s, they've also got a "daily deposit quota" to stop you from collecting all the money too quickly. Go around that by sending your extra dosh to your mates. That helps you get more cash faster, i.e. get the nuke faster than the gov't pigs. All transactions are done instantly, and your good Mother is looking out for all of you. Don't mess with your Mother. She guides and protects all of us in this pack. She sees and knows all. ;)

        Sincerely yours, and don't piss me off, JACKAL (your f▩cking boss)
    """.trimIndent())


    override fun onDetach() {
        super.onDetach()

    }
}
