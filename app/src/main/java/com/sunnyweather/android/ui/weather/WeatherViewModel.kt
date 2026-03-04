package com.sunnyweather.android.ui.weather

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Weather
import kotlinx.coroutines.launch
import java.lang.Exception

class WeatherViewModel: ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()
    var locationLng = ""
    var locationLat = ""
    var placeName = ""

    // 核心修复1：标记是否正在请求，避免重复请求


    val weatherLiveData = locationLiveData.switchMap { location ->
        Log.d("WeatherViewModel", "switchMap 触发: ${location.lng}, ${location.lat}")

        Repository.refreshWeather(location.lng, location.lat)
    }


    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }

}