package eu.zerovector.coinz.Components

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import eu.zerovector.coinz.Data.CryptoSettings
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Data.MessageDifficulty
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.BONUS_VALUE_MULTIPLIER
import eu.zerovector.coinz.R
import eu.zerovector.coinz.Utils.Companion.toString


@SuppressLint("SetTextI18n")
class CryptomessageView : LinearLayout {

    private var currentView: View
    private val btnDecrypt: Button
    private val lblGibberish: TextView
    private val lblDifficulty: TextView

    private var messageIndex: Int = -1

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        // Inflate layout
        currentView = inflate(context, R.layout.layout_cryptomessage, this)

        btnDecrypt = currentView.findViewById(R.id.btnMsgDecrypt)
        lblGibberish = currentView.findViewById(R.id.lblMsgGibberish)
        lblDifficulty = currentView.findViewById(R.id.lblMsgDifficulty)

        // Init some fake data here for no reason
        SetData(-1, MessageDifficulty.Easy, CryptoSettings.MessageInfo(169, 1))
    }

    // And this sets the onClick listener for the "decrypt" button.
    fun SetDecryptionListener(listener: () -> Unit) {
        btnDecrypt.setOnClickListener { listener() }
    }

    // Makes the message visually different and prevents button clicks
    fun SetDecrypted() {
        btnDecrypt.text = "DECRYPTED"
        btnDecrypt.isEnabled = false
        lblGibberish.setTextColor(ContextCompat.getColor(context!!, R.color.darkGrey))
    }

    // Public message setup listener.
    fun SetData(identityIndex: Int, diff: MessageDifficulty, data: CryptoSettings.MessageInfo) {
        // Liking up the price and the difficulty is easy.
        btnDecrypt.text = "DECRYPT\n☄${data.price}"
        lblDifficulty.text = "${diff.name.toUpperCase()} (Readiness +${(data.bonusNoMultiplier * BONUS_VALUE_MULTIPLIER).toString(3)}%)"

        // This is the value that determines WHICH message this specific one is.
        messageIndex = identityIndex

        // Now generate some gibberish as the "message text"; it must always be the same.
        // Why is this language allowing me to write this in this manner?!
        lblGibberish.text = RandomAlphaNumeric(when (diff) {
            MessageDifficulty.Easy -> 12
            MessageDifficulty.Medium -> 24
            MessageDifficulty.Hard -> 36
        })
        val colourRes = when (diff) {
            MessageDifficulty.Easy -> R.color.Yellow
            MessageDifficulty.Medium -> R.color.Orange
            MessageDifficulty.Hard -> R.color.Red
        }
        lblGibberish.setTextColor(ContextCompat.getColor(context, colourRes))

    }


    // This creates a random alphanumeric string of variable length (using the shared generator).
    private val charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    fun RandomAlphaNumeric(count: Int): String {
        var count = count
        val builder = StringBuilder()
        val rand = DataManager.dailyCryptoSettings.generator
        while (count-- != 0) {
            val character = (rand.nextDouble() * charset.length).toInt()
            builder.append(charset[character])
        }
        return builder.toString()
    }
}

