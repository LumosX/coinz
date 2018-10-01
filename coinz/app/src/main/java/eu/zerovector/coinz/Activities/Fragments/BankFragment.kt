package eu.zerovector.coinz.Activities.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Extras.Companion.toString
import eu.zerovector.coinz.R

class BankFragment : Fragment() {

    private lateinit var view: ViewGroup
    private lateinit var lblQuota: TextView
    private lateinit var lblCommission: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bank, container, false) as ViewGroup

        // And set username.
        view.findViewById<TextView>(R.id.lblWelcome).text = "Welcome, " + DataManager.GetUsername()
        lblQuota = view.findViewById(R.id.lblDepositQuota)
        lblCommission = view.findViewById(R.id.lblCommission)
        SetQuotaText()

        DataManager.SubscribeForUIUpdates { SetQuotaText() }

        return view
    }


    @SuppressLint("SetTextI18n")
    private fun SetQuotaText() {
        lblQuota.text = "Daily deposit quota: " + DataManager.GetDailyDepositsLeft() + "/" + DataManager.GetDailyDepositLimit();
        lblCommission.text = "Current commission rate: " + DataManager.GetBankCommissionRate().toString(1) + "%"
    }


}
