package eu.zerovector.coinz

import android.os.Bundle


class GameActivity : BaseFullscreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        title = "COINZ"

    }

}
