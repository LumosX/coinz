package eu.zerovector.coinz.Activities

import android.os.Bundle
import eu.zerovector.coinz.R


class GameActivity : BaseFullscreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        title = "COINZ"

    }

}
