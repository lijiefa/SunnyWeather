package com.sunnyweather.android.logic

import android.content.Context
import android.util.Log
import androidx.lifecycle.liveData
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

object Repository {

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            Result.success(placeResponse.places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        Log.d("Repository", "开始请求天气: lng=$lng, lat=$lat")

        try {
            // 1. 先请求实时天气（不用 async，直接挂起等待）
            Log.d("Repository", "请求实时天气...")
            val realtimeResponse = SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            Log.d("Repository", "实时天气完成: ${realtimeResponse.status}")

            // 2. 延迟 500ms，避免限流
            delay(1000)

            // 3. 再请求每日天气
            Log.d("Repository", "请求每日天气...")
            val dailyResponse = SunnyWeatherNetwork.getDailyWeather(lng, lat)
            Log.d("Repository", "每日天气完成: ${dailyResponse.status}")

            // 4. 判断结果
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                Log.d("Repository", "组装 Weather 成功")
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                val error = "realtime=${realtimeResponse.status}, daily=${dailyResponse.status}"
                Log.e("Repository", "API 返回错误: $error")
                Result.failure(RuntimeException(error))
            }
        } catch (e: Exception) {
            Log.e("Repository", "请求异常: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
    fun savePlace(place: Place)=PlaceDao.savePlace(place)
    fun getSavePlace()= PlaceDao.getSavePlace()
    fun isPlaceSaved()= PlaceDao.isPlaceSaved()
}