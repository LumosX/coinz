package eu.zerovector.coinz.Activities

import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import eu.zerovector.coinz.Activities.Fragments.*
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

        viewPager

        // Also request map permissions RIGHT NOW
        checkRequestLocPermissions()

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
