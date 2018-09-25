package eu.zerovector.coinz.Activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import eu.zerovector.coinz.R


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "COINZ Login"

    }

}
