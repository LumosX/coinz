package eu.zerovector.coinz.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import eu.zerovector.coinz.R
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : BaseFullscreenActivity() {

    private lateinit var fbAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "COINZ Login"

        fbAuth = FirebaseAuth.getInstance()
    }


    fun onLoginClicked(view: View) {
        // If the data validates, register.
        var dialogBuilder = AlertDialog.Builder(this@LoginActivity)
                .setMessage("Signing in... Please wait.")
        val waitDialog = dialogBuilder.create()
        waitDialog.show()

        fbAuth
                .signInWithEmailAndPassword(tbEmail.text.toString(), tbPassword.text.toString())
                .addOnCompleteListener { task ->
                    // No matter the result, hide the waiting dialog box.
                    waitDialog.dismiss()

                    if (task.isSuccessful) {
                        // If the login is successful, we just need to move to the new activity.
                        finish()
                        startActivity(Intent(this@LoginActivity, GameActivity::class.java))

                    } else {
                        waitDialog.hide()
                        dialogBuilder = AlertDialog.Builder(this@LoginActivity)
                                .setMessage("Login failed!\n${task.exception?.message}")
                                .setNeutralButton("Close", null)
                        val failureDialog = dialogBuilder.create()
                        failureDialog.show()
                    }
                }
    }

}
