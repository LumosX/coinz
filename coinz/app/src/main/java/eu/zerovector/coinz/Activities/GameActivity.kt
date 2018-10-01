package eu.zerovector.coinz.Activities

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
import eu.zerovector.coinz.Data.DataManager.Companion.DOESNT_EXIST
import eu.zerovector.coinz.Data.DataManager.Companion.PREFS_NAME
import eu.zerovector.coinz.Data.DataManager.Companion.PREF_VERSION_CODE_KEY
import eu.zerovector.coinz.R
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
                .add("War", WarFragment::class.java)
                .add("Stats", StatsFragment::class.java)
                .add("Mail", MailFragment::class.java)
                .create())

        viewPager.adapter = adapter
        tabLayout.setViewPager(viewPager)

        viewPager.setOnTouchListener { v, event -> (event?.action == MotionEvent.ACTION_MOVE) }


        checkFirstRun()

        // Also request map permissions RIGHT NOW
        checkRequestLocPermissions()

        // Immediately start downloading map data.
        DataManager.UpdateLocalMap(baseContext)

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

        // Get saved version code
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST)

        // Check for first run or upgrade
        //if (currentVersionCode == savedVersionCode) {
        if (false) {

            // This is just a normal run
            return

        } /*else if (savedVersionCode == DOESNT_EXIST) {
            // New install, or the user purged the preferences
        } else if (currentVersionCode > savedVersionCode) {
            // Upgrade
        }*/
        else {
            // TODO fix strings and so on, to make this a tutorial
          /*  SpotlightView.Builder(this)
                    .introAnimationDuration(400)
                    .enableRevealAnimation(true)
                    .performClick(true)
                    .fadeinTextDuration(400)
                    .headingTvColor(Color.parseColor("#eb273f"))
                    .headingTvSize(32)
                    .headingTvText("Love")
                    .subHeadingTvColor(Color.parseColor("#ffffff"))
                    .subHeadingTvSize(16)
                    .subHeadingTvText("Like the picture?\nLet others know.")
                    .maskColor(Color.parseColor("#dc000000"))
                    .target(findViewById(R.id.tabLayout))
                    .lineAnimDuration(400)
                    .lineAndArcColor(Color.parseColor("#eb273f"))
                    .dismissOnTouch(true)
                    .dismissOnBackPress(true)
                    .enableDismissAfterShown(true)
                    .usageId("welcome 1") //UNIQUE ID
                    .show()*/

        }



        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply()
    }


    //////////////////////// BACK BUTTON SHENANIGANS
    // Override the "back" button, so that we can safely log out the current account.
    override fun onBackPressed() {
        // This should always work fine, because we don't allow access to this activity if the user isn't signed in yet
        if (fbAuth.currentUser != null) {
            fbAuth.signOut()
            Toast.makeText(applicationContext, "Logged out!", Toast.LENGTH_SHORT).show()
        }

        super.onBackPressed()
        finish() // also end activity after logging the user out.
    }

}
