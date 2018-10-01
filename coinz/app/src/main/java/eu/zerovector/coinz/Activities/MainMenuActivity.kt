package eu.zerovector.coinz.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.zerovector.coinz.Data.AccountData
import eu.zerovector.coinz.Data.DataManager
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


    fun onLoginClicked(view: View) {
        startActivity(Intent(this@MainMenuActivity, LoginActivity::class.java))
    }

    fun onRegisterClicked(view: View) {
        startActivity(Intent(this@MainMenuActivity, RegisterActivity::class.java))
    }

    // Add a third button for quick logins for testing reasons
    // DELETE THIS ONCE DONE
    fun onTestLoginClicked(view: View) {
        // If the data validates, register.
        var dialogBuilder = AlertDialog.Builder(this@MainMenuActivity)
                .setMessage("Signing in... Please wait.")
        val waitDialog = dialogBuilder.create()
        waitDialog.show()

        val fbAuth = FirebaseAuth.getInstance()
        fbAuth
                .signInWithEmailAndPassword("test@test.com", "123456")
                .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        // No matter the result, hide the waiting dialog box.
                        waitDialog.dismiss()

                        if (task.isSuccessful) {

                            // If the login is successful, we update local data.
                            val firestore = FirebaseFirestore.getInstance()
                            val usersCol = firestore.collection("Users")
                            val curUserDoc = usersCol.document(fbAuth.currentUser!!.uid)

                            // Well, we need to get it from Firebase first:
                            curUserDoc.get().addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    failLogin("Login failed!\n" + task.exception?.message)
                                } else {
                                    val userData = task.result.toObject(AccountData::class.java)
                                    DataManager.SetCurrentAccountData(userData!!)

                                    // Then we just need to move to the new activity.
                                    finish()
                                    startActivity(Intent(this@MainMenuActivity, GameActivity::class.java))
                                }
                            }

                        } else {

                            failLogin("Login failed!\n${task.exception?.message}")
                        }

                    }

                    fun failLogin(message: String) {
                        waitDialog.dismiss()
                        dialogBuilder = AlertDialog.Builder(this@MainMenuActivity)
                                .setMessage(message)
                                .setNeutralButton("Close", null)
                        val failureDialog = dialogBuilder.create()
                        failureDialog.show()
                    }
                })
    }


}
