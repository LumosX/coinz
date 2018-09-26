package eu.zerovector.coinz.Activities

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import eu.zerovector.coinz.R






class GameActivity : BaseFullscreenActivity() {

    private lateinit var fbAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        title = "COINZ"


        // Get authentication reference, again
        fbAuth = FirebaseAuth.getInstance()
    }




    // Override the "back" button, so that we can safely log out the current account.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.repeatCount == 0) {
            Log.d("CDA", "onKeyDown Called")
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onBackPressed() {
        Log.d("CDA", "onBackPressed Called")

        // This should always work fine, because we don't allow access to this activity if the user isn't signed in yet
        if (fbAuth.currentUser != null) {
            fbAuth.signOut()
            Toast.makeText(applicationContext, "Logged out!", Toast.LENGTH_SHORT).show()
        }

        super.onBackPressed()
    }

}
