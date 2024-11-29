package com.example.yolbil_jetpack_sample

import android.util.Log
import androidx.lifecycle.ViewModel
import com.basarsoft.yolbil.core.MapPos
import com.basarsoft.yolbil.datasources.HTTPTileDataSource
import com.basarsoft.yolbil.layers.RasterTileLayer
import com.basarsoft.yolbil.layers.TileLoadListener
import com.basarsoft.yolbil.projections.EPSG4326
import com.basarsoft.yolbil.ui.MapView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class YolbilViewModel @Inject constructor() : ViewModel() {

    // Define appCode and accId at the top for easier configuration
    private val appCode = "YOUR_APP_CODE"  // Replace with actual appCode.
    private val accId = "YOUR_ACCID"      // Replace with actual accId

    private var mapView: MapView? = null


    private val navigationUsage:YolbilNavigationUsage  = YolbilNavigationUsage()
    var startPos:MapPos? = null
    var endPos:MapPos? = null

    fun initializeMapView(mapView: MapView) {

        this.mapView = mapView
        this.startPos = MapPos(32.8597, 39.9334) // Kızılay, Ankara

        this.endPos = MapPos(32.8547, 39.9250)   // Anıtkabir, Ankara

        // Set map projection and initial position
        mapView.options.baseProjection = EPSG4326()
        mapView.setFocusPos(MapPos(32.836262, 39.960160), 0f)
        mapView.setZoom(17.0f, 0.0f)

        // Define tile data source using appCode and accId
        val tileDataSource = HTTPTileDataSource(
            0, 18, "https://bms.basarsoft.com.tr/service/api/v1/map/Default?appcode=$appCode&accid=$accId&x={x}&y={y}&z={zoom}"
        )

        // Create and add tile layer to MapView
        val rasterLayer = RasterTileLayer(tileDataSource)
        mapView.layers.add(rasterLayer)

        // Monitor tile load events
        rasterLayer.tileLoadListener = object : TileLoadListener() {
            override fun onVisibleTilesLoaded() {
                Log.d("YolbilViewModel", "Visible tiles loaded")
            }
        }
    }
    fun createRoute() {
        // Check if dependencies are initialized


        val mapView = this.mapView
        if (mapView == null) {
            Log.e("YolbilViewModel", "MapView not initialized")
            return
        }

        // Use YolbilNavigationUsage to create and display the route
        val navigationResult = navigationUsage.fullExample(mapView, this.startPos, this.endPos, false)

        if (navigationResult != null) {
            val routePoints = navigationResult.points

            // Check if `routePoints` has a size method or equivalent
            val pointsCount = try {
                routePoints.size() // Replace with actual method if not `size()`
            } catch (e: Exception) {
                Log.e("YolbilViewModel", "Error accessing route points size: ${e.message}")
                return
            }

            Log.d(
                "YolbilViewModel",
                "Route created successfully with $pointsCount points"
            )

            // Further processing, such as displaying route details or instructions
        } else {
            Log.e("YolbilViewModel", "Failed to create route. NavigationResult is null.")
        }
    }




}