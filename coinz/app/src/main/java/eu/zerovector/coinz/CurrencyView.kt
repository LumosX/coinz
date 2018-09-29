package eu.zerovector.coinz

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.mapbox.mapboxsdk.Mapbox.getApplicationContext
import eu.zerovector.coinz.Data.Currency
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Extras.Companion.MakeToast


class CurrencyView : LinearLayout {

    private var currentView: View
    private lateinit var currency: Currency

    private var btnBuy: Button
    private var btnSell: Button
    private var btnDeposit: Button

    private var image: ImageView
    private var lblName: TextView
    private var lblBalance: TextView
    private var lblChange: TextView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        // Inflate layout
        currentView = inflate(context, R.layout.layout_currency_view, this)
        context.obtainStyledAttributes(attrs, R.styleable.CurrencyView, 0, 0).apply {
            var t = getString(R.styleable.CurrencyView_currency)
            if (t == null) t = "GOLD"
            currency = Currency.valueOf(t)
            recycle()
        }

        // Bind views here. It's the most effective way to do it.
        btnBuy = currentView.findViewById(R.id.btnCurrencyBuy)
        btnSell = currentView.findViewById(R.id.btnCurrencySell)
        btnDeposit = currentView.findViewById(R.id.btnCurrencyDeposit)

        image = currentView.findViewById(R.id.imgCurrency)
        lblName = currentView.findViewById(R.id.lblCurrencyName)
        lblBalance = currentView.findViewById(R.id.lblCurrencyBalance)
        lblChange = currentView.findViewById(R.id.lblCurrencyChange)

        UpdateData()

    }


    @SuppressLint("SetTextI18n")
    public fun UpdateData() {
        // Set variables based on the current currency
        image.setImageResource(currency.iconID)
        lblName.text = currency.name
        lblBalance.text = "BANK: " + DataManager.GetBalance(currency).toString()
        lblChange.text = "CHANGE: " + DataManager.GetChange(currency).toString() + "/" + DataManager.GetChangeLimit()

        // Fancily set button visibility
        btnBuy.visibility = if (currency.showBuySell) View.VISIBLE else View.INVISIBLE
        btnSell.visibility = btnBuy.visibility
        btnDeposit.visibility = btnBuy.visibility
        // applies to the "spare change" label as well, since GOLD doesn't have "change"
        lblChange.visibility = btnBuy.visibility

        btnBuy.text = "BUY: " + DataManager.GetBuyPrice(currency)
        btnSell.text = "SELL: " + DataManager.GetSellPrice(currency)

        btnBuy.setOnClickListener { OnBuyClicked() }
        btnSell.setOnClickListener { OnSellClicked() }
        btnDeposit.setOnClickListener { OnDepositClicked() }

    }


    fun OnBuyClicked() {
        MakeToast(context, "BUY CLICKED FOR CURRENCY $currency")
    }

    fun OnSellClicked() {
        MakeToast(context, "SELL CLICKED FOR CURRENCY $currency")
    }

    @SuppressLint("SetTextI18n")
    fun OnDepositClicked() {
        val alert = AlertDialog.Builder(context)

        alert.setTitle("DEPOSITING SPARE $currency")
        alert.setMessage("The sum will be deposited to your $currency balance immediately.\nYour deposit quota will be reset tomorrow.")

        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL

        val text = TextView(context)
        text.text = "Depositing 0/" + DataManager.GetDepositQuota()
        text.gravity = Gravity.CENTER

        val seek = SeekBar(context)
        seek.min = 0
        seek.max = DataManager.GetBalance(currency)
        seek.keyProgressIncrement = 1
        seek.setPadding(100, 10, 100, 10)
        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val remainingQuota = DataManager.GetDepositQuota() - seek.max
                text.text = "Depositing $currency $progress/${seek.max}\nRemaining daily quota: $remainingQuota"
            }
        })

        linear.addView(seek)
        linear.addView(text)
        linear.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        alert.setView(linear)



        alert.setPositiveButton("DEPOSIT") { dialog, id ->
            Toast.makeText(getApplicationContext(), "OK Pressed", Toast.LENGTH_LONG).show()
        }

        alert.setNegativeButton("CANCEL") { dialog, id ->
            Toast.makeText(getApplicationContext(), "Cancel Pressed", Toast.LENGTH_LONG).show()
        }

        alert.show()

    }

}

