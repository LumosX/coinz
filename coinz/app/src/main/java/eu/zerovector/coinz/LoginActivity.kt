package eu.zerovector.coinz

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "COINZ Login"

    }

}
