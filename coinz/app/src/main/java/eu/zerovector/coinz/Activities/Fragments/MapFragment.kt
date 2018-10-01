package eu.zerovector.coinz.Activities.Fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import eu.zerovector.coinz.Data.Currency
import eu.zerovector.coinz.Data.DataManager
import eu.zerovector.coinz.Extras.Companion.toString
import eu.zerovector.coinz.R



class MapFragment : Fragment(), OnMapReadyCallback, LocationEngineListener {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationEngine: LocationEngine
    private lateinit var grabRadiusMarker: Marker

    private lateinit var lblDolr: TextView
    private lateinit var lblPeny: TextView
    private lateinit var lblShil: TextView
    private lateinit var lblQuid: TextView


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

    }

    private fun showBoundsArea() {
        // For this one we actually need to generate four polygons.
        // We don't want to make the play area red, we want to make the EXTERNAL area red.
        // Polygons with holes make this quite easy, actually...
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
        locationEngine.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine.fastestInterval = 1000
        locationEngine.addLocationEngineListener(this)
        locationEngine.activate()

        // Create an instance of the plugin. Adding in LocationLayerOptions is also an optional parameter
        val locationLayerPlugin = LocationLayerPlugin(mapView, mapboxMap)
        // also enable location tracking
        locationLayerPlugin.isLocationLayerEnabled = true
        locationLayerPlugin.cameraMode = CameraMode.TRACKING
        lifecycle.addObserver(locationLayerPlugin)
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location?) {
        // TODO draw circle around the current location marker
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
            locationEngine.addLocationEngineListener(this)
            if (locationEngine.isConnected) {
                locationEngine.requestLocationUpdates()
            } else {
                locationEngine.activate()
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
            locationEngine.removeLocationEngineListener(this)
            locationEngine.removeLocationUpdates()
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
            locationEngine.deactivate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}
