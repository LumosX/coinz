package eu.zerovector.coinz

import android.content.Context
import android.widget.Toast
import eu.zerovector.coinz.Data.bool

class Extras {

    companion object {

        // A faster and better toaster
        public fun MakeToast(context: Context, text: String, shortDuration: bool = true) {
            Toast.makeText(context, text, if (shortDuration) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
        }




    }





}