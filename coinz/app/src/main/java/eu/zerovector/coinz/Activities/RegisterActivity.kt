package eu.zerovector.coinz.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import eu.zerovector.coinz.Data.Team
import eu.zerovector.coinz.OnSwipeListener
import eu.zerovector.coinz.R
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : BaseFullscreenActivity(), View.OnTouchListener {
    private var teamSelectorVisible = true
    private var visibleTeamE11 = false
    private lateinit var gestureDetector: GestureDetector

    // Firebase stuff
    private lateinit var userDBBranch: DatabaseReference
    private lateinit var fbDB: FirebaseDatabase
    private lateinit var fbAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        title = "COINZ Registration"

        // Set up the swipe detection for team selection.
        // First thing visible is the selector.
        teamSelectorVisible = true
        toggleSelectedTeam(true) // select eleventh echelon by default

        gestureDetector = GestureDetector(this, object: OnSwipeListener() {
            override fun onSwipe(direction: Direction): Boolean {
                // Swipe left for Eleventh Echelon, swipe right for Crimson Dawn
                if (direction == Direction.left) toggleSelectedTeam(true)
                if (direction == Direction.right) toggleSelectedTeam(false)
                return true
            }
        })
        layoutTeamSelector.setOnTouchListener(this)

        layoutTeamSelector.visibility = View.VISIBLE
        layoutRegisterDetails.visibility = View.INVISIBLE

        // Fix team selector margins for the navigation bar.
        // for some reason this screws the "layoutCD"'s position, so we're not using it
        /*(layoutTeamSelector.layoutParams as ViewGroup.MarginLayoutParams)
            .setMargins(0, 0, 0, getNavigationBarSize(applicationContext).y)*/

        // Grab firebase instances
        fbAuth = FirebaseAuth.getInstance()
        fbDB = FirebaseDatabase.getInstance()
        userDBBranch = fbDB.reference.child("Users")

    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    // Override the back button if the team selector is NOT visible (to allow a player to return to the team selector)
    override fun onBackPressed() {
        if (teamSelectorVisible) super.onBackPressed()
        else toggleSelectorScreen(true)
    }


    // if the confirm button is clicked, shift view to the team selector
    fun onTeamSelected(view: View) {
        // Hide selector, show final registration
        toggleSelectorScreen(false)
    }

    fun onConfirmClicked(view: View) {
        // Validate data.
        // username must be 3+ chars.
        var error: String? = null
        if (!tbEmail.text.toString().matches("^[A-Za-z](.*)([@])(.+)(\\.)(.+)".toRegex())) error = "Email is not valid!"
        else if (tbPassword.text.length < 6) error = "Password too short (must be 6+ chars)!"
        else if (!tbPassword.text.contains(".*\\d+.*".toRegex())) error = "Password must contain a digit!"
        else if (tbPassword.text.toString() != tbConfirmPassword.text.toString()) error = "Passwords don't match!"
        else if (tbUsername.text.length < 3) error = "Username too short (must be 3+ chars)!"

        if (error != null) {
            Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
            return
        }


        // If the data "validates", register.
        var dialogBuilder = AlertDialog.Builder(this@RegisterActivity)
                .setMessage("Attempting to register... Please wait.")
        val waitDialog = dialogBuilder.create()
        waitDialog.show()

        fbAuth
                .createUserWithEmailAndPassword(tbEmail.text.toString(), tbPassword.text.toString())
                .addOnCompleteListener { task ->
                    // No matter the result, hide the waiting dialog box.
                    waitDialog.hide()

                    // If all's well, make a toast.
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_SHORT).show()

                        // Add the user data to the database
                        val userBranch = userDBBranch.child(fbAuth.currentUser!!.uid)
                        userBranch.child("username").setValue(tbUsername.text.toString())
                        userBranch.child("team").setValue((if (visibleTeamE11) Team.EleventhEchelon else Team.CrimsonDawn))

                        // Move to the new activity as well.
                        startActivity(Intent(this@RegisterActivity, GameActivity::class.java))

                    } else {
                        waitDialog.hide()
                        dialogBuilder = AlertDialog.Builder(this@RegisterActivity)
                                .setMessage("Registration failed!\n${task.exception?.message}")
                                .setNeutralButton("Close", null)
                        val failureDialog = dialogBuilder.create()
                        failureDialog.show()
                    }
                }
    }


    // This function shows/hides the team selector and the registration screen
    private fun toggleSelectorScreen(showTeamSelector: bool) {

        teamSelectorVisible = showTeamSelector

        val activatedLayout = if (teamSelectorVisible) layoutTeamSelector else layoutRegisterDetails
        val deactivatedLayout = if (teamSelectorVisible) layoutRegisterDetails else layoutTeamSelector

        activatedLayout.visibility = View.VISIBLE
        activatedLayout.alpha = 0.0f
        activatedLayout.animate().alpha(1.0f).duration = 1000
        deactivatedLayout.animate().alpha(0.0f).setDuration(1000).withEndAction { deactivatedLayout.visibility = View.INVISIBLE }

    }

    // This function shows/hides elements when the selected team (in the active team selector) changes
    @SuppressLint("SetTextI18n")
    private fun toggleSelectedTeam(isE11: bool) {
        if (visibleTeamE11 == isE11) return // don't need to re-animate if it's already selected

        // this only works if the team selector is currently active
        if (!teamSelectorVisible) return

        visibleTeamE11 = isE11
        val activatedLayout = if (isE11) layoutE11 else layoutCD
        val deactivatedLayout = if (isE11) layoutCD else layoutE11

        // Fix labels here. Might as well.
        val teamName = if (isE11) "Eleventh Echelon" else "Crimson Dawn"
        btnSelectTeam.text = "JOIN the $teamName"
        lblChosenTeam.text = "SELECTED TEAM: $teamName"

        activatedLayout.visibility = View.VISIBLE
        activatedLayout.alpha = 0.0f
        activatedLayout.animate().alpha(1.0f).duration = 1000
        deactivatedLayout.animate().alpha(0.0f).setDuration(1000).withEndAction { deactivatedLayout.visibility = View.INVISIBLE }
    }

}
