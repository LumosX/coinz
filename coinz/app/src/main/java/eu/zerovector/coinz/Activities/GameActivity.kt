package eu.zerovector.coinz.Activities

import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import eu.zerovector.coinz.Activities.Fragments.MapFragment
import eu.zerovector.coinz.R
import kotlinx.android.synthetic.main.activity_game.*


class GameActivity : BaseFullscreenActivity() {

    private lateinit var fbAuth: FirebaseAuth

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
                .add("Bank", MapFragment::class.java)
                .add("War", MapFragment::class.java)
                .add("Stats", MapFragment::class.java)
                .add("Mail", MapFragment::class.java)
                .create())

        viewPager.adapter = adapter
        tabLayout.setViewPager(viewPager)


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
    }

}
