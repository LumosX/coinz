package eu.zerovector.coinz

import android.content.Context
import android.widget.Toast
import eu.zerovector.coinz.Data.bool
import kotlin.math.pow

class Extras {

    companion object {

        // A faster and better toaster
        fun MakeToast(context: Context, text: String, shortDuration: bool = true) {
            Toast.makeText(context, text, if (shortDuration) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
        }


        // Extension method to round Doubles to a number of decimal digits (useful for currencies)
        fun Double.toString(numDecimals: Int): String {
            // The next line throws an InvocationTargetException for some unknown fucking reason.
            // return "%.$numDecimals".format(this)
            // I guess we'll have to HACK IT then:
            val factor = 10.0.pow(numDecimals)
            return (Math.round(this * factor) / factor).toString()
        }


    }





}