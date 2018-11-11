package eu.zerovector.coinz.Activities

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import eu.zerovector.coinz.Activities.Fragments.*
import eu.zerovector.coinz.BuildConfig
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Data.DataManager.Companion.PREFS_NAME
import eu.zerovector.coinz.Data.Team
import eu.zerovector.coinz.R
import eu.zerovector.coinz.Utils.Companion.MakeToast
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity : BaseFullscreenActivity(), PermissionsListener {

    private lateinit var fbAuth: FirebaseAuth
    private lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        title = "COINZ"

        // Get authentication reference, again
        fbAuth = FirebaseAuth.getInstance()

        // Set the main viewPager up.
        val adapter = FragmentPagerItemAdapter(
                supportFragmentManager, FragmentPagerItems.with(this)
                .add("Map", MapFragment::class.java)
                .add("Bank", BankFragment::class.java)
                .add("Ops", WarFragment::class.java)
                .add("Stats", StatsFragment::class.java)
                .add("Mail", MailFragment::class.java)
                .create())
        viewPager.adapter = adapter
        tabLayout.setViewPager(viewPager)
        viewPager.setOnTouchListener { _, event -> (event?.action == MotionEvent.ACTION_MOVE) }

        // Also request map permissions RIGHT NOW
        checkRequestLocPermissions()

        // Immediately start downloading map data.
        DataManager.UpdateLocalMap(baseContext)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        //MakeToast(this, "precheck first run")
        checkFirstRun()
    }


    private fun checkRequestLocPermissions() {
        //Toast.makeText(this, "MAP READY TIGGERED", Toast.LENGTH_SHORT).show()
        // Check if permissions are enabled and if not request
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        //Toast.makeText(this, "The game cannot be played without this permission", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            checkRequestLocPermissions()
        } else {
            Toast.makeText(this, "The game can't function without the requested permission.", Toast.LENGTH_LONG).show()
            finish()
        }
    }


    // Based on https://stackoverflow.com/a/30274315/668143
    private fun checkFirstRun() {

        // Get current version code
        val currentVersionCode = BuildConfig.VERSION_CODE

        // Get saved version code for this particular user
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(fbAuth.currentUser!!.uid, -1)

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            return
        } else {

            val isE11 = DataManager.GetTeam() == Team.EleventhEchelon
            val alert = AlertDialog.Builder(this)
            alert.setTitle("TUTORIAL")
            alert.setMessage("Welcome to COINZ, a game of spies. A nuclear warhead has gone missing and your mission is to retrieve it " +
                    (if (isE11) "in order to prevent it from falling into the wrong hands." else "and get rich in process.") +
                    "\nWould you like a brief tutorial?")
            alert.setPositiveButton("PROCEED") { _, _ -> ShowMapTabTutorial() }
            alert.setNegativeButton("SKIP") { _, _ -> }
            alert.show()


        }


        // Update the shared preferences with the current version code
        prefs.edit().putInt(fbAuth.currentUser!!.uid, currentVersionCode).apply()
    }

    private fun ShowMapTabTutorial() {
        viewPager.currentItem = 0
        val alert = AlertDialog.Builder(this)
        alert.setTitle("MAP SCREEN (1/5)")
        alert.setMessage("Your mission in the game is to walk around the map and pick \"coins\" up. This is the Map screen, " +
                "which displays your location, the location of all coins you can pick up, and the radius of your reach.\n" +
                "You can tap on every individual coin marker to check its value. To collect a coin, simply walk approach its location, " +
                "and if you have free space in your wallet (wallet balances are displayed at the top), you will pick the coin up " +
                "and add it to your wallet.")
        alert.setPositiveButton("PROCEED") { _, _ ->
            ShowBankTabTutorial()
        }
        alert.setNegativeButton("CLOSE") { _, _ -> }
        alert.show()
    }

    private fun ShowBankTabTutorial() {
        viewPager.currentItem = 1
        val isE11 = DataManager.GetTeam() == Team.EleventhEchelon
        val alert = AlertDialog.Builder(this)
        alert.setTitle("BANK SCREEN (2/5)")
        alert.setMessage("This is the Bank screen, which lists all of your currency balances, as well as your \"spare change\": the coins " +
                "in your wallet.\nYou can buy and sell currencies from here, deposit your \"spare change\" into your bank account, and " +
                "send extra \"spare change\" to teammates.\n" +
                "Your wallet sizes are limited, and you can only deposit up to 25 coins in total per day to your bank account. However, coins " +
                "you send to your friends are not restricted in any way. Make sure to send them all your extra change to maximise efficiency.\n" +
                if (isE11) "As a member of the Eleventh Echelon, you receive discounted bank commission rates."
                else "As a member of the Crimson Dawn, your \"spare change\" wallet is always larger than normal.")
        alert.setPositiveButton("PROCEED") { _, _ ->
            ShowWarTabTutorial()
        }
        alert.setNegativeButton("CLOSE") { _, _ -> }
        alert.show()
    }

    private fun ShowWarTabTutorial() {
        viewPager.currentItem = 2
        val isE11 = DataManager.GetTeam() == Team.EleventhEchelon
        val alert = AlertDialog.Builder(this)
        alert.setTitle("OPERATIONS SCREEN (3/5)")
        alert.setMessage("This is the Operations screen, which shows you the current global status of the operation, the amount of computing " +
                "power (\"Compute\") at your disposal, and the list of encrypted messages available to you to decrypt today.\n" +
                "Both your team and the " +
                (if (isE11) "dangerous mercenaries of the Crimson Dawn" else "government agents of the Eleventh Echelon") +
                " are looking for the bomb. The only way to find it is to decrypt secret messages containing clues of its whereabouts. To do this, " +
                "you will need to purchase Compute from various entities using your coins. Different providers take different currencies " +
                "in exchange for different amounts of Compute. In addition, your team gives you access to some unique providers.\n" +
                "As a member of the " + if (isE11)
            "Eleventh Echelon, you get somewhat cheaper Compute rates from most legitimate sources." else
            "Crimson Dawn, you get cheaper Compute prices for some providers (because of your group's hackers).")
        alert.setPositiveButton("PROCEED") { _, _ ->
            ShowStatsTabTutorial()
        }
        alert.setNegativeButton("CLOSE") { _, _ -> }
        alert.show()
    }

    private fun ShowStatsTabTutorial() {
        viewPager.currentItem = 3
        //val isE11 = DataManager.GetTeam() == Team.EleventhEchelon
        val alert = AlertDialog.Builder(this)
        alert.setTitle("STATS SCREEN (4/5)")
        alert.setMessage("This is the Stats screen, which shows the daily currency rates, your current level, experience, and " +
                "bonuses you get at your current and next level.\n" +
                "In addition, you may choose to show this tutorial again next time by pressing the button at the bottom of the screen.\n" +
                "Experience is gained by collecting coins, sending coins to teammates, and decrypting messages. Reaching a higher level improves the " +
                "benefits you receive as a member of your team.")
        alert.setPositiveButton("PROCEED") { _, _ ->
            ShowMailTabTutorial()
        }
        alert.setNegativeButton("CLOSE") { _, _ -> }
        alert.show()
    }

    private fun ShowMailTabTutorial() {
        viewPager.currentItem = 4
        //val isE11 = DataManager.GetTeam() == Team.EleventhEchelon
        val alert = AlertDialog.Builder(this)
        alert.setTitle("OPERATIONS SCREEN (5/5)")
        alert.setMessage("This is the Mail screen, which lists your messages. Whenever someone sends you coins, the transaction will be recorded " +
                "here. Furthermore, your team's commander has sent you a message to introduce you to more details of the operation. " +
                "You might want to read that...\n\nThis is the end of the tutorial. Good luck!")
        alert.setPositiveButton("CLOSE TUTORIAL") { _, _ -> }
        alert.show()
    }


    //////////////////////// BACK BUTTON SHENANIGANS
    // Override the "back" button, so that we can safely log out the current account after a double-tap.
    private var lastTimeBackPressed: Long = 0

    override fun onBackPressed() {
        // Handle double-tap-to-exit functionality
        if (lastTimeBackPressed + 1000 < System.currentTimeMillis()) {
            MakeToast(applicationContext, "Double-tap \"Back\" quickly to log out.", false)
            lastTimeBackPressed = System.currentTimeMillis()
            return
        }


        super.onBackPressed()
        // This should always work fine, because we don't allow access to this activity if the user isn't signed in yet
        if (fbAuth.currentUser != null) {
            fbAuth.signOut()
        }

        super.onBackPressed()
        kill()
    }

    // Same thing if the activity is stopping.
    override fun onStop() {
        if (fbAuth.currentUser != null) {
            fbAuth.signOut()
        }

        super.onStop()
        kill()
    }

    // Firebase was leaking threads somehow, preventing the app from closing properly. As a result, I had to use this:
    private fun kill() {
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
