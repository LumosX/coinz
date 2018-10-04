package eu.zerovector.coinz.Data

import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.Json


// result generated from /json
// used http://www.jsonschema2pojo.org/ w/ moshi annotations, then converted them to kotlin by hand

class Map {

    data class Data(
            @Json(name = "type")
            var type: String,
            @Json(name = "date-generated")
            var dateGenerated: String,
            @Json(name = "time-generated")
            var timeGenerated: String,
            @Json(name = "approximate-time-remaining")
            var approximateTimeRemaining: String,
            @Json(name = "rates")
            var rates: Rates,
            @Json(name = "features")
            var features: List<Feature>
    )

    data class Feature(
            @Json(name = "type")
            var type: String,
            @Json(name = "properties")
            var properties: Properties,
            @Json(name = "geometry")
            var geometry: Geometry
    )

    data class Geometry(
            @Json(name = "type")
            var type: String,
            @Json(name = "coordinates")
            var coordinates: List<Double>
    )

    data class Properties(
            @Json(name = "id")
            var id: String,
            @Json(name = "value")
            var value: Double,
            @Json(name = "currency")
            var currency: String,
            @Json(name = "marker-symbol")
            var markerSymbol: String,
            @Json(name = "marker-color")
            var markerColor: String
    )

    // This one wasn't serialised from the map file, but is used for more efficient storage of the map coin markers
    data class CoinInfo(
            var currency: Currency,
            var value: Double,
            var coords: LatLng
    )

    // And this one gets the special treatment of default values, because we use it as a data structure for other purposes
    data class Rates(
            @Json(name = "SHIL")
            var shil: Double = 0.0,
            @Json(name = "DOLR")
            var dolr: Double = 0.0,
            @Json(name = "QUID")
            var quid: Double = 0.0,
            @Json(name = "PENY")
            var peny: Double = 0.0
    )

    // And this will allow us to hold balances more easily...
    data class Wallet(
            @Json(name = "SHIL")
            var shil: Int = 0,
            @Json(name = "DOLR")
            var dolr: Int = 0,
            @Json(name = "QUID")
            var quid: Int = 0,
            @Json(name = "PENY")
            var peny: Int = 0
    )

}