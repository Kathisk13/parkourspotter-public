package com.example.maps.utils

import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo

lateinit var mapboxMap: MapboxMap

fun setCameraPos(point: Point){
    mapboxMap.flyTo(
        cameraOptions {
            center(point) // Sets the new camera position
            zoom(17.0) // Sets the zoom
        },
        MapAnimationOptions.mapAnimationOptions {
            duration(4000)
        }
    )
}