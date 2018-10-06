package eu.zerovector.coinz.Activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.Window

open class BaseFullscreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Lock application to portrait view
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
    }


    override fun onDestroy() {
        super.onDestroy()

      /*  // force kill app at the end in order to prevent issues with Firebase
        finishAffinity()
        //System.exit(0)
        android.os.Process.killProcess(android.os.Process.myPid())*/
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

    override fun onResume() {
        super.onResume()
        hide()
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


    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private val mHideRunnable = Runnable { hide() }

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Set a bunch of flags for the root viewGroup.
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        rootView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION /*or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION*/
    }

}
