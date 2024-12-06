package com.example.yolbil_jetpack_sample

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.basarsoft.yolbil.core.MapPos
import com.basarsoft.yolbil.datasources.BlueDotDataSource
import com.basarsoft.yolbil.datasources.HTTPTileDataSource
import com.basarsoft.yolbil.layers.RasterTileLayer
import com.basarsoft.yolbil.layers.TileLoadListener
import com.basarsoft.yolbil.layers.VectorLayer
import com.basarsoft.yolbil.location.GPSLocationSource
import com.basarsoft.yolbil.location.Location
import com.basarsoft.yolbil.location.LocationListener
import com.basarsoft.yolbil.navigation.YolbilNavigationBundle
import com.basarsoft.yolbil.projections.EPSG4326
import com.basarsoft.yolbil.routing.NavigationResult
import com.basarsoft.yolbil.ui.MapView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.basarsoft.yolbil.location.LocationBuilder

import com.basarsoft.yolbil.location.LocationSource

@HiltViewModel
class YolbilViewModel @Inject constructor() : ViewModel() {

    private val appCode = "YOUR_APPCODE" // Replace with actual appCode
    private val accId = "YOUR_ACCID"     // Replace with actual accId
    private var mapView: MapView? = null
    val navigationUsage: YolbilNavigationUsage = YolbilNavigationUsage()
    var startPos: MapPos? = null
    var endPos: MapPos? = null
    private var gpsLocationSource: GPSLocationSource? = null
     var navigationResult: NavigationResult? = null

    private fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    fun initializeMapView(mapView: MapView) {

        this.mapView = mapView
        this.startPos = MapPos(32.8597, 39.9334) // Kızılay, Ankara
        this.endPos = MapPos(32.8547, 39.9250)   // Anıtkabir, Ankara

        // Set map projection and initial position
        mapView.options.baseProjection = EPSG4326()
        mapView.setFocusPos(MapPos(32.836262, 39.960160), 0f)
        mapView.setZoom(17.0f, 0.0f)

        // Initialize GPS Location Source
        gpsLocationSource = GPSLocationSource(mapView.context)

        // Define tile data source
        val tileDataSource = HTTPTileDataSource(
            0, 18, "https://bms.basarsoft.com.tr/service/api/v1/map/Default?appcode=$appCode&accid=$accId&x={x}&y={y}&z={zoom}"
        )

        // Create and add tile layer
        val rasterLayer = RasterTileLayer(tileDataSource)
        mapView.layers.add(rasterLayer)

        // Monitor tile load events
        rasterLayer.tileLoadListener = object : TileLoadListener() {
            override fun onVisibleTilesLoaded() {
                Log.d("YolbilViewModel", "Visible tiles loaded")
            }
        }
        focusOnUserLocation()
    }

    fun createRoute() {
        val mapView = this.mapView
        if (mapView == null || startPos == null || endPos == null) {
            Log.e("YolbilViewModel", "MapView or positions not initialized")
            return
        }

        // Create route using YolbilNavigationUsage
        navigationResult = navigationUsage.fullExample(mapView, startPos, endPos, false)
        if (navigationResult != null) {
            Log.d("YolbilViewModel", "Route created successfully with ${navigationResult?.points?.size()} points")
        } else {
            Log.e("YolbilViewModel", "Failed to create route")
        }
    }

    fun startNavigation() {
        val mapView = this.mapView ?: return
        val gpsLocationSource = this.gpsLocationSource ?: return
        val endPos = this.endPos ?: return

        try {
            // GPS konum güncellemelerini başlat
            gpsLocationSource.startLocationUpdates()

            // Dinleyiciyi oluştur ve ekle
            val listener = MyNavigationListener(mapView, gpsLocationSource, navigationUsage, endPos)
            gpsLocationSource.addListener(listener)

            Log.d("YolbilViewModel", "Navigasyon başlatıldı")
        } catch (e: SecurityException) {
            Log.e("YolbilViewModel", "Lokasyon izni gerekiyor: ${e.message}")
        }
    }




    private var bundle: YolbilNavigationBundle? = null

    fun addBlueDotToMap() {
        val mapView = this.mapView ?: return
        val gpsLocationSource = this.gpsLocationSource ?: return

        try {
            // GPS konum güncellemelerini başlat
            gpsLocationSource.startLocationUpdates()

            // Konum dinleyicisini ekle
            val listener = MyLocationListener(mapView)
            gpsLocationSource.addListener(listener)

            // Blue Dot için veri kaynağı ve katman ekle
            val blueDotDataSource = BlueDotDataSource(EPSG4326(), gpsLocationSource)
            val blueDotVectorLayer = VectorLayer(blueDotDataSource)
            mapView.layers.add(blueDotVectorLayer)

            Log.d("YolbilViewModel", "Blue Dot successfully added to the map")
        } catch (e: SecurityException) {
            Log.e("YolbilViewModel", "Permission is required for location updates: ${e.message}")
        }
    }

    fun focusOnUserLocation() {
        val mapView = this.mapView ?: return
        val gpsLocationSource = this.gpsLocationSource ?: return

        try {
            gpsLocationSource.startLocationUpdates()

            // Konum dinleyicisini oluştur ve ekle
            val listener = MyLocationListener(mapView)
            gpsLocationSource.addListener(listener)

            Log.d("YolbilViewModel", "Kamera kullanıcı konumuna odaklanmak için hazırlanıyor")


        } catch (e: SecurityException) {
            Log.e("YolbilViewModel", "Lokasyon izni gerekiyor: ${e.message}")
        }
    }

}

class MyLocationListener(private val mapView: MapView) : LocationListener() {
    override fun onLocationChange(location: Location) {
        // Konum alındığında kamerayı bu konuma odakla
        val userLocation = location.coordinate
        mapView.setFocusPos(userLocation, 1.0f) // Kamerayı kullanıcı konumuna odakla
        mapView.setZoom(17.0f, 1.0f) // Yakınlaştırma seviyesi
        Log.d("MyLocationListener", "Kamera konuma odaklandı: $userLocation")
    }
}

class MyNavigationListener(
    private val mapView: MapView,
    private val gpsLocationSource: GPSLocationSource,
    private val navigationUsage: YolbilNavigationUsage,
    private val endPos: MapPos
) : LocationListener() {
    override fun onLocationChange(location: Location) {
        val userLocation = location.coordinate
        Log.d("MyNavigationListener", "Kullanıcı konumu: $userLocation")

        // Rota oluştur
        val startPos = userLocation
        val navigationResult = navigationUsage.fullExample(mapView, startPos, endPos, false)

        if (navigationResult != null) {
            Log.d("MyNavigationListener", "Rota başarıyla oluşturuldu. Nokta sayısı: ${navigationResult.points.size()}")
        } else {
            Log.e("MyNavigationListener", "Rota oluşturulamadı.")
        }

        // Dinleyiciyi kaldır
        gpsLocationSource.removeListener(this)
    }
}
