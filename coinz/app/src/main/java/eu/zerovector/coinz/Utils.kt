package eu.zerovector.coinz

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.Toast
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import eu.zerovector.coinz.Data.bool
import java.util.*


class Utils {

    companion object {

        // A faster and easier-to-use toaster
        fun MakeToast(context: Context, text: String, shortDuration: bool = true) {
            Toast.makeText(context, text, if (shortDuration) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
        }


        // Extension method to round Doubles to a number of decimal digits (useful for currencies)
        fun Double.toString(numDecimals: Int): String {
            // The next line throws an InvocationTargetException for some unknown fucking reason.
            // return "%.$numDecimals".format(this)

            // I guess we'll have to HACK IT then:
            // val factor = 10.0.pow(numDecimals)
            // return (Math.round(this * factor) / factor).toString()

            // Actually, this seems to work:
            return java.lang.String.format("%.${numDecimals}f", this)
        }


        // Based on https://github.com/mapbox/mapbox-gl-native/issues/8185
        fun GenerateCoinIcon(context: Context, @DrawableRes id: Int, @ColorInt colorRes: Int): Icon {
            val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, context.theme)!!.mutate()
            /*val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                    vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)*/
            val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            DrawableCompat.setTint(vectorDrawable, colorRes)
            vectorDrawable.draw(canvas)
            vectorDrawable.alpha = 220
            return IconFactory.getInstance(context).fromBitmap(bitmap)
        }

        fun DrawRadiusPolygon(curPos: Location, radiusInKilometers: Double = .05, sides: Int = 64): PolygonOptions {
            // here, currentPosition is a class property, get your lat & long as you'd like
            val latitude = curPos.latitude
            val longitude = curPos.longitude

            // these are conversion constants
            val distanceX: Double = radiusInKilometers / (111.319 * Math.cos(latitude * Math.PI / 180))
            val distanceY: Double = radiusInKilometers / 110.574

            val slice = (2 * Math.PI) / sides

            val circlePoly = PolygonOptions()

            var theta: Double
            var x: Double
            var y: Double
            var position: Point
            for (i in 0..sides) {
                theta = i * slice
                x = distanceX * Math.cos(theta)
                y = distanceY * Math.sin(theta)

                //position = Point.fromLngLat(longitude + x, latitude + y)!!
                circlePoly.add(LatLng(latitude + y, longitude + x))
            }
            circlePoly.alpha(0.25f)
            circlePoly.fillColor(Color.BLUE)

            return circlePoly
        }


        // And a cheeky little extension method to give us integers with boundaries
        fun Random.nextInt(min: Int, max: Int): Int {
            if (min >= max) {
                throw IllegalArgumentException("max must be greater than min")
            }
            return this.nextInt(max - min + 1) + min
        }

    }

    // from https://stackoverflow.com/a/18015071
    class ProgressBarAnimation(private val progressBar: ProgressBar, private val from: Float, private val to: Float) : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            val value = from + (to - from) * interpolatedTime
            progressBar.progress = value.toInt()
        }

    }


}