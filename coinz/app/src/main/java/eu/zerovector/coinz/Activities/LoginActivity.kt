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
                                    startActivity(Intent(this@LoginActivity, GameActivity::class.java))
                                }
                            }

                        } else {

                            failLogin("Login failed!\n${task.exception?.message}")
                        }

                    }

                    fun failLogin(message: String) {
                        waitDialog.dismiss()
                        dialogBuilder = AlertDialog.Builder(this@LoginActivity)
                                .setMessage(message)
                                .setNeutralButton("Close", null)
                        val failureDialog = dialogBuilder.create()
                        failureDialog.show()
                    }
                })
    }

}
