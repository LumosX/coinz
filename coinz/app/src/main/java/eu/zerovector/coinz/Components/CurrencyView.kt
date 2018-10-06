package eu.zerovector.coinz.Components

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
import eu.zerovector.coinz.Utils.Companion.MakeToast
import eu.zerovector.coinz.Utils.Companion.toString
import eu.zerovector.coinz.R
import kotlin.math.min


@SuppressLint("SetTextI18n")
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

        DataManager.SubscribeForUIUpdates { UpdateData() }

    }


    @SuppressLint("SetTextI18n")
    fun UpdateData() {
        // Set variables based on the current currency
        image.setImageResource(currency.iconID)
        lblName.text = currency.name
        lblBalance.text = "BANK: " + DataManager.GetBalance(currency) / 100.0
        lblChange.text = "CHANGE: " + DataManager.GetChange(currency) / 100.0 +
                "/" + DataManager.GetWalletSize() / 100.0

        // Fancily set button visibility
        btnBuy.visibility = if (currency.showBuySell) View.VISIBLE else View.INVISIBLE
        btnSell.visibility = btnBuy.visibility
        btnDeposit.visibility = btnBuy.visibility
        // applies to the "spare change" label as well, since GOLD doesn't have "change"
        lblChange.visibility = btnBuy.visibility

        btnBuy.text = "BUY: " + DataManager.GetBuyPrice(currency).toString(4)
        btnSell.text = "SELL: " + DataManager.GetSellPrice(currency).toString(4)

        btnBuy.setOnClickListener { OnBuyClicked() }
        btnSell.setOnClickListener { OnSellClicked() }
        btnDeposit.setOnClickListener { OnDepositClicked() }

    }


    // BUYING: Converting GOLD into currency
    fun OnBuyClicked() {
        val curPrice = DataManager.GetBuyPrice(currency) * 1000 / 1000 // artificial rounding to 4 decimals
        val curGold = DataManager.GetBalance(Currency.GOLD)

        // First, find out how much of this currency we can buy.
        val maxPurchaseableAmount = curGold / curPrice
        // Then we get the amount in "pennies" and floor to int, because the seek bar works with ints.
        //val maxBuyAmountTimes100 = (maxPurchaseableAmount * 100).toInt()
        val maxBuyAmountTimes100 = maxPurchaseableAmount.toInt() // when using ints by default, we simply need to bind this.

        // If you can't buy a single "penny" of the currency, abort.
        if (maxBuyAmountTimes100 < 1) {
            MakeToast(context, "You don't have enough GOLD to purchase any $currency.")
            return
        }

        val alert = AlertDialog.Builder(context)

        alert.setTitle("BUYING $currency WITH GOLD")
        alert.setMessage("Current price: $currency 1 = GOLD ${curPrice.toString(4)}")

        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL

        val text = TextView(context)
        text.text = "Buying $currency 0.01 " +
                "(GOLD left: ${(curGold / 100.0 - curPrice * 0.01).toString(2)})"
        text.gravity = Gravity.CENTER

        val seek = SeekBar(context)
        seek.min = 1
        seek.max = maxBuyAmountTimes100 + 1
        seek.keyProgressIncrement = 1
        seek.setPadding(100, 10, 100, 10)
        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // the progress value is effectively in pennies. Divide by 100 and update texts.
                var curAmt = min(progress, maxBuyAmountTimes100) / 100.0
                var curAmtStr = curAmt.toString(2)

                var remainingGold = (curGold / 100.0 - curPrice * curAmt).toString(2)

                text.text = "Buying $currency $curAmtStr (GOLD left: $remainingGold)"
            }
        })

        linear.addView(seek)
        linear.addView(text)
        linear.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        alert.setView(linear)

        alert.setPositiveButton("BUY") { _, _ ->
            // Deposit the cash.
            var boughtCurrency = min(seek.progress, maxBuyAmountTimes100)
            var goldDelta = (-curPrice * boughtCurrency).toInt() // NEGATIVE!
            DataManager.BuySellCoins(currency, boughtCurrency, goldDelta)
            MakeToast(getApplicationContext(), "Purchase complete!")
        }

        alert.setNegativeButton("CANCEL") { _, _ -> }

        alert.show()
    }


    // SELLING: Converting currency into GOLD
    fun OnSellClicked() {
        val curPrice = DataManager.GetSellPrice(currency) * 1000 / 1000 // artificial rounding to 4 decimals
        val curBalance = DataManager.GetBalance(currency)

        // First, find out how much of this currency we can sell.
        // Then we do the multiplication trick again
        //val maxSellingAmountTimes100 = (curBalance * 100).toInt()
        val maxSellingAmountTimes100 = curBalance

        // Like above, If you can't sell a single "penny" of the currency, abort.
        if (maxSellingAmountTimes100 < 1) {
            MakeToast(context, "You don't have enough $currency to sell.")
            return
        }

        val alert = AlertDialog.Builder(context)

        alert.setTitle("SELLING $currency FOR GOLD")
        alert.setMessage("Current price: $currency 1 = GOLD ${curPrice.toString(4)}")

        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL

        val text = TextView(context)
        text.text = "Selling $currency 0.01 for GOLD ${(curPrice * 0.01).toString(2)}"
        text.gravity = Gravity.CENTER

        val seek = SeekBar(context)
        seek.min = 1
        seek.max = maxSellingAmountTimes100 + 1
        seek.keyProgressIncrement = 1
        seek.setPadding(100, 10, 100, 10)
        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // the progress value is effectively in pennies. Divide by 100 and update texts.
                var curAmt = min(progress, maxSellingAmountTimes100)/ 100.0
                var curAmtStr = curAmt.toString(2)

                var goldGain = (curPrice * curAmt).toString(2)

                text.text = "Selling $currency $curAmtStr for GOLD $goldGain"
            }
        })

        linear.addView(seek)
        linear.addView(text)
        linear.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        alert.setView(linear)

        alert.setPositiveButton("SELL") { _, _ ->
            // Deposit the cash.
            var soldCurrency = min(seek.progress, maxSellingAmountTimes100)
            var goldDelta = (curPrice * soldCurrency).toInt() // Gold is positive this time...
            DataManager.BuySellCoins(currency, -soldCurrency, goldDelta) // ... and the currency delta is negative
            MakeToast(getApplicationContext(), "Sale complete!")
        }

        alert.setNegativeButton("CANCEL") { _, _ -> }

        alert.show()
    }


    // DEPOSITING: moving spares into balances
    fun OnDepositClicked() {
        val sparesLeft = DataManager.GetChange(currency)
        val remainingQuota = DataManager.GetDailyDepositsLeft()

        // Again, we do the conversion trick to facilitate the seekbar working with "pennies"
        val sparesTimes100 = (sparesLeft).toInt()
        val quotaTimes100 = (remainingQuota).toInt()

        if (sparesTimes100 == 0) {
            MakeToast(context, "You have no coins to deposit!")
            return
        } else if (quotaTimes100 <= 0) {
            MakeToast(context, "You may not deposit more coins today.")
            return
        }

        // The max amount of coins you can deposit is the smaller of what's in the bag and the quota left
        val maxAmount = min(sparesTimes100, quotaTimes100)


        val alert = AlertDialog.Builder(context)

        alert.setTitle("DEPOSITING SPARE $currency")
        alert.setMessage("The sum will be deposited to your $currency balance immediately.\nYour deposit quota will be reset tomorrow.")

        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL

        val text = TextView(context)
        text.text = "Depositing $currency 0.01/${maxAmount / 100.0} (Remaining quota: ${(remainingQuota - 1) / 100.0})"
        text.gravity = Gravity.CENTER

        val seek = SeekBar(context)
        seek.min = 1
        seek.max = maxAmount + 1
        seek.keyProgressIncrement = 1
        //seek.setPadding(100, 10, 100, 10)
        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val curAmt = min(progress, maxAmount) / 100.0
                val maxAmt = maxAmount / 100.0

                val potentialRemaining = ((quotaTimes100 - curAmt * 100) / 100.0).toString(2)
                text.text = "Depositing $currency $curAmt/$maxAmt (Remaining quota: $potentialRemaining)"
            }
        })

        linear.addView(seek)
        linear.addView(text)
        linear.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        alert.setView(linear)

        alert.setPositiveButton("DEPOSIT") { _, _ ->
            // Deposit the cash.
            DataManager.DepositCoins(currency, min(seek.progress, maxAmount))
            MakeToast(getApplicationContext(), "Sum deposited!")
        }

        alert.setNegativeButton("CANCEL") { _, _ -> }

        alert.show()

    }

}

