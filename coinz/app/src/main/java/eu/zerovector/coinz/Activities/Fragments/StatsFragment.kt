package eu.zerovector.coinz.Activities.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Data.Experience
import eu.zerovector.coinz.Data.Team
import eu.zerovector.coinz.R
import eu.zerovector.coinz.Utils
import eu.zerovector.coinz.Utils.Companion.MakeToast
import eu.zerovector.coinz.Utils.Companion.toString

class StatsFragment : Fragment() {

    // "Header"
    private lateinit var imgIconCD: ImageView
    private lateinit var imgIconE11: ImageView
    private lateinit var lblNameFaction: TextView
    private lateinit var lblCurrentLevel: TextView
    private lateinit var lblXP: TextView
    private lateinit var pbXP: ProgressBar

    // Interbank rates
    private lateinit var lblDolrPureRate: TextView
    private lateinit var lblPenyPureRate: TextView
    private lateinit var lblShilPureRate: TextView
    private lateinit var lblQuidPureRate: TextView

    // Team benefits
    private lateinit var lblBenefit1: TextView
    private lateinit var lblBenefit1Cur: TextView
    private lateinit var lblBenefit1Next: TextView
    private lateinit var lblBenefit2: TextView
    private lateinit var lblBenefit2Cur: TextView
    private lateinit var lblBenefit2Next: TextView

    // Show tutorial again
    private lateinit var btnResetTutorial: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // This fragment does nothing but show some UI info.
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        // As a result, we only need to bind component IDs and that's pretty much that.
        // I don't use the newfangled Kotlin data binding because I don't know
        // how that works with fragments and I'm afraid it might mess something up.
        imgIconE11 = view.findViewById(R.id.imgIconE11)
        imgIconCD = view.findViewById(R.id.imgIconCD)
        lblNameFaction = view.findViewById(R.id.lblNameFaction)
        lblCurrentLevel = view.findViewById(R.id.lblCurrentLevel)
        lblXP = view.findViewById(R.id.lblXP)
        pbXP = view.findViewById(R.id.pbExperience)

        lblDolrPureRate = view.findViewById(R.id.lblGoldDolr)
        lblPenyPureRate = view.findViewById(R.id.lblGoldPeny)
        lblShilPureRate = view.findViewById(R.id.lblGoldShil)
        lblQuidPureRate = view.findViewById(R.id.lblGoldQuid)

        lblBenefit1 = view.findViewById(R.id.lblBenefit1)
        lblBenefit1Cur = view.findViewById(R.id.lblBenefit1CurLevel)
        lblBenefit1Next = view.findViewById(R.id.lblBenefit1NextLevel)
        lblBenefit2 = view.findViewById(R.id.lblBenefit2)
        lblBenefit2Cur = view.findViewById(R.id.lblBenefit2CurLevel)
        lblBenefit2Next = view.findViewById(R.id.lblBenefit2NextLevel)

        btnResetTutorial = view.findViewById(R.id.btnResetTutorial)
        btnResetTutorial.setOnClickListener(::onResetFirstRunClicked)


        // And as normal, register for dynamic updates, of course.
        UpdateUI()
        DataManager.SubscribeForUIUpdates { UpdateUI() }

        return view
    }

    // Reset the SharedPrefs setting so that the tutorial shows up again.
    private fun onResetFirstRunClicked(view: View) {

        // Delete the key corresponding to the current user.
        val prefs = activity!!.getSharedPreferences(DataManager.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(FirebaseAuth.getInstance().currentUser!!.uid).apply()

        // Disable the button and report progress.
        btnResetTutorial.isEnabled = false
        MakeToast(context!!, "Done! You will see the introductory tutorial again on your next login.")
    }


    @SuppressLint("SetTextI18n")
    private fun UpdateUI() {
        // What this function does is self-explanatory, really.

        val curTeam = DataManager.GetTeam()

        // Pictures and colours
        // Run-time tinting didn't seem to do the trick, so I resorted to using two imageViews instead.
        imgIconE11.visibility = if (curTeam == Team.EleventhEchelon) View.VISIBLE else View.INVISIBLE
        imgIconCD.visibility = if (curTeam == Team.EleventhEchelon) View.INVISIBLE else View.VISIBLE
        // set the colour of the progress bar while we're here:
        val progressBarRes = if (curTeam == Team.EleventhEchelon) R.drawable.progressbar_e11 else R.drawable.progressbar_cd
        pbXP.progressDrawable = ContextCompat.getDrawable(context!!, progressBarRes)

        // Player and XP stuff
        var teamNameAllCaps = if (curTeam == Team.EleventhEchelon) "ELEVENTH ECHELON" else "CRIMSON DAWN"
        lblNameFaction.text = "${DataManager.GetUsername()}, $teamNameAllCaps"
        val curXP = DataManager.GetXP()
        val curLevel = Experience.GetLevelRankFromXP(curXP)
        val curLevelName = Experience.GetLevelName(curTeam, curXP)
        lblCurrentLevel.text = "Level $curLevel | $curLevelName"

        val curLevelMinXP = Experience.GetMinXPForLevel(curLevel)
        val nextLevelMinXP = Experience.GetMinXPForLevel(curLevel + 1)
        // TODO: Animate the progress bar. Maybe.
        val targetProgress = (((curXP - curLevelMinXP) / (nextLevelMinXP - curLevelMinXP).toDouble()) * 100).toInt()
        val anim = Utils.ProgressBarAnimation(pbXP, pbXP.progress / 100.0f, targetProgress / 100.0f)
        anim.duration = 500
        anim.start()



        lblXP.text = "$curXP/$nextLevelMinXP"

        // Currency rates
        val rates = DataManager.dailyPureRates
        lblDolrPureRate.text = "GOLD/DOLR ${rates.dolr.toString(4)}"
        lblPenyPureRate.text = "GOLD/PENY ${rates.peny.toString(4)}"
        lblShilPureRate.text = "GOLD/SHIL ${rates.shil.toString(4)}"
        lblQuidPureRate.text = "GOLD/QUID ${rates.quid.toString(4)}"

        // Level data
        val levelData = Experience.GetTeamBenefitData(curTeam, curXP)
        lblBenefit1.text = levelData[0]
        lblBenefit1Cur.text = "Current level: ${levelData[1]}"
        lblBenefit1Next.text = "(Next level: ${levelData[2]})"
        lblBenefit2.text = levelData[3]
        lblBenefit2Cur.text = "Current level: ${levelData[4]}"
        lblBenefit2Next.text = "(Next level: ${levelData[5]})"


    }
}
