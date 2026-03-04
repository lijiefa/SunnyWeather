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
    private var isRequesting = false

    val weatherLiveData = locationLiveData.switchMap { location ->
        Log.d("WeatherViewModel", "switchMap 触发: ${location.lng}, ${location.lat}")
        // 发起请求时标记状态
        isRequesting = true
        Repository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String) {
        // 核心修复2：防抖逻辑 - 如果正在请求，直接返回
        if (isRequesting) {
            Log.d("WeatherViewModel", "请求正在进行中，跳过重复请求")
            return
        }
        // 更新经纬度并触发请求
        locationLiveData.value = Location(lng, lat)
    }

    // 核心修复3：提供重置请求状态的方法（Repository 请求结束后调用）
    // 注意：需要配合 Repository 的代码修改，见下方说明
    fun onRequestCompleted() {
        isRequesting = false
        Log.d("WeatherViewModel", "请求完成，重置请求状态")
    }
}