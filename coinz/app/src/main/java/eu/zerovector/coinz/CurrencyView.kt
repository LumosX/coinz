package eu.zerovector.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import eu.zerovector.coinz.Data.Currency
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Extras.Companion.MakeToast


class CurrencyView : LinearLayout {

    private var currentView: View
    private lateinit var currency: Currency
    private var btnBuy: Button
    private var btnSell: Button

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

        btnBuy = currentView.findViewById(R.id.lblCurrencyBuy)
        btnSell = currentView.findViewById(R.id.lblCurrencySell)

        UpdateData()

    }


    @SuppressLint("SetTextI18n")
    public fun UpdateData() {
        // Set variables based on the current currency
        currentView.findViewById<ImageView>(R.id.imgCurrency).setImageResource(currency.iconID)
        currentView.findViewById<TextView>(R.id.lblCurrencyName).text = currency.name
        currentView.findViewById<TextView>(R.id.lblCurrencyBalance).text = DataManager.GetBalance(currency).toString()

        // Fancily set label visibility
        btnBuy.visibility = if (currency.showBuySell) View.VISIBLE else View.INVISIBLE
        btnSell.visibility = btnBuy.visibility

        btnBuy.text = "BUY: " + DataManager.GetBuyPrice(currency)
        btnSell.text = "SELL: " + DataManager.GetSellPrice(currency)

        btnBuy.setOnClickListener { OnBuyClicked() }
        btnSell.setOnClickListener { OnSellClicked() }

    }


    fun OnBuyClicked() {
        MakeToast(context, "BUY CLICKED FOR CURRENCY $currency")
    }

    fun OnSellClicked() {
        MakeToast(context, "SELL CLICKED FOR CURRENCY $currency")
    }

}

