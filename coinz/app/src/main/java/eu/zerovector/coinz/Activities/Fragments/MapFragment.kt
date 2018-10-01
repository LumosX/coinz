package eu.zerovector.coinz.Activities.Fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import eu.zerovector.coinz.Data.Currency
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Extras.Companion.GenerateCoinIcon
import eu.zerovector.coinz.Extras.Companion.GetPerimeterFeature
import eu.zerovector.coinz.Extras.Companion.toString
import eu.zerovector.coinz.R



class MapFragment : Fragment(), OnMapReadyCallback, LocationEngineListener {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var locationEngine: LocationEngine? = null
    private lateinit var grabRadiusMarker: Marker
    private var coinMarkers: MutableList<Marker> = mutableListOf()

    private lateinit var lblDolr: TextView
    private lateinit var lblPeny: TextView
    private lateinit var lblShil: TextView
    private lateinit var lblQuid: TextView

    private lateinit var iconDolr: Icon
    private lateinit var iconPeny: Icon
    private lateinit var iconShil: Icon
    private lateinit var iconQuid: Icon

    private val minLat = 55.942617
    private val maxLat = 55.946233
    private val minLon = -3.192473
    private val maxLon = -3.184319

    // Set bounds as per the coursework
    private val bounds = LatLngBounds.Builder()
            .include(LatLng(maxLat, minLon))
            .include(LatLng(minLat, maxLon))
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Set our maps up
        Mapbox.getInstance(context!!, getString(R.string.mapbox_access_token))

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap -> onMapReady(mapboxMap) }

        lblDolr = view.findViewById(R.id.lblDolr)
        lblPeny = view.findViewById(R.id.lblPeny)
        lblShil = view.findViewById(R.id.lblShil)
        lblQuid = view.findViewById(R.id.lblQuid)


        // Generate icons for the four currencies
        val iconColour = ContextCompat.getColor(context!!, R.color.DarkGoldenrod)
        iconDolr = GenerateCoinIcon(context!!, R.drawable.icon_dolr, iconColour)
        iconPeny = GenerateCoinIcon(context!!, R.drawable.icon_peny, iconColour)
        iconShil = GenerateCoinIcon(context!!, R.drawable.icon_shil, iconColour)
        iconQuid = GenerateCoinIcon(context!!, R.drawable.icon_quid, iconColour)



        UpdateCoinLabels()
        DataManager.SubscribeForUIUpdates { UpdateCoinLabels() }

        // Inflate the layout
        return view
    }

    // Mapbox location plugin stuff
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        // TODO: add circle for grabbing radius


        // Permissions are handled elsewhere and should be available
        enableLocationEvents()

        // Now show bounds and restrict map
        showBoundsArea()
        mapboxMap.setLatLngBoundsForCameraTarget(bounds)
        mapboxMap.setMinZoomPreference(14.5)

        // And finally, add all the markers we need on the map. In addition, register for map updates with the data manager
        UpdateMarkers()
        DataManager.coinSetUpdateListener = ::UpdateMarkers


        // Add circle:
        mapboxMap.addSource(GeoJsonSource("grab_radius_src"))
        val layer = CircleLayer("grab_radius", "grab_radius_src")
        layer.withProperties(
                circleRadius(50f),
                circleOpacity(.4f),
                circleColor(Color.BLUE)
        )
        mapboxMap.addLayer(layer)


    }

    private fun CurrencyToIcon(currency: Currency) = when (currency) {
        Currency.DOLR -> iconDolr
        Currency.PENY -> iconPeny
        Currency.SHIL -> iconShil
        Currency.QUID -> iconQuid
        Currency.GOLD -> iconDolr // this is irrelevant, as we never need a coin for gold
    }

    // This function is the one which actually updates all the markers.
    private fun UpdateMarkers() {
        if (!DataManager.coinSetDirty) return

        // If the set of coin markers needs updating, do it
        for (marker in coinMarkers) marker.remove()
        coinMarkers.clear()

        for (coin in DataManager.currentCoinsMap) {
            // Set individual markers up here. Tapping on the marker shows the currency and the amount present.
            val currency = coin.value.currency
            val icon = CurrencyToIcon(currency)

            val markerOptions = MarkerOptions()
                    .position(LatLng(coin.value.latitude, coin.value.longitude))
                    .setTitle("$currency ${coin.value.value}")
                    .setIcon(icon)

            val newMarker = mapboxMap.addMarker(markerOptions) // just to keep the nesting a little more readable
            coinMarkers.add(newMarker)
        }

        DataManager.coinSetDirty = false
    }


    private fun showBoundsArea() {
        // We generate one massive rectangular polygon with a hole the size of the play area in it.
        val offset = 0.010000 // 1 km?

        val largerBounds = LatLngBounds.Builder()
                .include(LatLng(maxLat + offset, minLon - offset))
                .include(LatLng(minLat - offset, maxLon + offset)).build()

        val boundsPolygon = PolygonOptions()
                .add(largerBounds.northWest)
                .add(largerBounds.northEast)
                .add(largerBounds.southEast)
                .add(largerBounds.southWest)
                .addHole(mutableListOf(bounds.northWest, bounds.northEast, bounds.southEast, bounds.southWest))
        boundsPolygon.alpha(0.5f)
        boundsPolygon.fillColor(Color.RED)
        mapboxMap.addPolygon(boundsPolygon)
    }


    private fun enableLocationEvents() {
        // Get the LocationEngine running first.
        locationEngine = LocationEngineProvider(context).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.fastestInterval = 1000
        locationEngine?.addLocationEngineListener(this)
        locationEngine?.activate()

        // Create an instance of the plugin. Adding in LocationLayerOptions is also an optional parameter
        val locationLayerPlugin = LocationLayerPlugin(mapView, mapboxMap)
        // also enable location tracking
        locationLayerPlugin.isLocationLayerEnabled = true
        locationLayerPlugin.cameraMode = CameraMode.TRACKING
        lifecycle.addObserver(locationLayerPlugin)
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) return

        var radiusFeature = GetPerimeterFeature(location, 0.100)
        mapboxMap.addPolygon(radiusFeature)

    }



    @SuppressLint("SetTextI18n")
    private fun UpdateCoinLabels() {
        lblDolr.text = DataManager.GetChange(Currency.DOLR).toString(2) + "/" + DataManager.GetWalletSize()
        lblPeny.text = DataManager.GetChange(Currency.PENY).toString(2) + "/" + DataManager.GetWalletSize()
        lblShil.text = DataManager.GetChange(Currency.SHIL).toString(2) + "/" + DataManager.GetWalletSize()
        lblQuid.text = DataManager.GetChange(Currency.QUID).toString(2) + "/" + DataManager.GetWalletSize()
    }



    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView.onStart()
        if (locationEngine != null) {
            locationEngine?.addLocationEngineListener(this)
            if (locationEngine!!.isConnected) {
                locationEngine?.requestLocationUpdates()
            } else {
                locationEngine?.activate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        if (locationEngine != null) {
            locationEngine?.removeLocationEngineListener(this)
            locationEngine?.removeLocationUpdates()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        if (locationEngine != null) {
            locationEngine?.deactivate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}
