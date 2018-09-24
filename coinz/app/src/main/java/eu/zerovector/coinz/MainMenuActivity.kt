package eu.zerovector.coinz

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    var splashAnimating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "COINZ"

        // Set up the user interaction to manually show or hide the system UI.
        layoutSplash.setOnClickListener { onSplashScreenClicked() }


        // Set up the swipe detection for the registration.

    }

    // Hide splash and display main menu instead
    private fun onSplashScreenClicked() {
        if (splashAnimating) return

        splashAnimating = true
        layoutMainMenu.visibility = View.VISIBLE
        layoutMainMenu.alpha = 0.0f
        layoutMainMenu.animate().alpha(1.0f).duration = 1000
        layoutSplash.animate().alpha(0.0f).setDuration(1000).withEndAction { layoutSplash.visibility = View.INVISIBLE }
    }




    fun onLoginClicked() {
        startActivity(Intent(this@MainMenuActivity, LoginActivity::class.java))
    }

    fun onRegisterClicked() {
        startActivity(Intent(this@MainMenuActivity, RegisterActivity::class.java))
    }



    ///////// UI HIDING GARBAGE
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        hide()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) delayedHide(100)
    }


    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()

        mHideHandler.postDelayed(mHidePart2Runnable, 300.toLong())

        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private val mHideRunnable = Runnable { hide() }

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        layoutMainMenu.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION /*or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION*/
    }
}
