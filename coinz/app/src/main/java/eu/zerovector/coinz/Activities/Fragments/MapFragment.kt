package eu.zerovector.coinz.Activities.Fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import eu.zerovector.coinz.R




class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

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
        view.setOnTouchListener { _, _ ->
            Toast.makeText(context, "touch event intercepted by mapfragment", Toast.LENGTH_LONG).show()
            false }

        // Set our maps up
        Mapbox.getInstance(context!!, getString(R.string.mapbox_access_token))

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap -> onMapReady(mapboxMap) }

        // Inflate the layout
        return view
    }


    // Mapbox location plugin stuff
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        // Permissions are handled elsewhere and should be available
        enableLocationPlugin()


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
        boundsPolygon.alpha(0.25f)
        boundsPolygon.fillColor(Color.RED)
        mapboxMap.addPolygon(boundsPolygon)
    }


    fun enableLocationPlugin() {
        // Create an instance of the plugin. Adding in LocationLayerOptions is also an optional parameter
        val locationLayerPlugin = LocationLayerPlugin(mapView, mapboxMap)

        // also enable location tracking
        locationLayerPlugin.isLocationLayerEnabled = true

        // Set the plugin's camera mode
        locationLayerPlugin.cameraMode = CameraMode.TRACKING
        lifecycle.addObserver(locationLayerPlugin)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
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
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}
