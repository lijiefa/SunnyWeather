package com.sunnyweather.android.logic.network

import android.util.Log
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {

    private val placeService = ServiceCreator.create<PlaceService>()
    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getDailyWeather(lng: String, lat: String): DailyResponse {
        Log.d("SunnyWeatherNetwork", "请求每日天气: $lng, $lat")
        return weatherService.getDailyWeather(lng, lat).await()
    }

    suspend fun getRealtimeWeather(lng: String, lat: String): RealtimeResponse {
        Log.d("SunnyWeatherNetwork", "请求实时天气: $lng, $lat")
        return weatherService.getRealtimeWeather(lng, lat).await()
    }

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val url = call.request().url().toString()
                    val httpCode = response.code()
                    val isSuccessful = response.isSuccessful

                    Log.d("SunnyWeatherNetwork", "========================================")
                    Log.d("SunnyWeatherNetwork", "URL: $url")
                    Log.d("SunnyWeatherNetwork", "HTTP Code: $httpCode")
                    Log.d("SunnyWeatherNetwork", "Is Successful: $isSuccessful")

                    if (isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Log.d("SunnyWeatherNetwork", "Response Body: $body")
                            continuation.resume(body)
                        } else {
                            val error = "HTTP $httpCode: Response body is null"
                            Log.e("SunnyWeatherNetwork", error)
                            continuation.resumeWithException(RuntimeException(error))
                        }
                    } else {
                        // 获取错误响应体
                        val errorBody = response.errorBody()?.string()
                        val error = "HTTP Error $httpCode: $errorBody"
                        Log.e("SunnyWeatherNetwork", error)
                        continuation.resumeWithException(RuntimeException(error))
                    }
                    Log.d("SunnyWeatherNetwork", "========================================")
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    val url = call.request().url().toString()
                    Log.e("SunnyWeatherNetwork", "请求失败: $url")
                    Log.e("SunnyWeatherNetwork", "错误: ${t.message}", t)
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}