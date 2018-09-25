package eu.zerovector.coinz

import android.os.Bundle


class RegisterActivity : BaseFullscreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        title = "COINZ Registration"

    }

}
