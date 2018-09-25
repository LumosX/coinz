package eu.zerovector.coinz.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import eu.zerovector.coinz.R
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : BaseFullscreenActivity() {

    var splashAnimating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main_menu)
        title = "COINZ"

        // Set up the user interaction to manually show or hide the system UI.
        layoutSplash.setOnClickListener { onSplashScreenClicked() }

        // Also set the visibility layouts up
        layoutMainMenu.visibility = View.INVISIBLE
        layoutSplash.visibility = View.VISIBLE

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


    fun onLoginClicked(view : View) {
        startActivity(Intent(this@MainMenuActivity, LoginActivity::class.java))
    }

    fun onRegisterClicked(view : View) {
        startActivity(Intent(this@MainMenuActivity, RegisterActivity::class.java))
    }
}
