package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication: Application() {
    companion object{
        @SuppressLint
        lateinit var context: Context

        const val TOKEN="TAkhjf8d1nlSlspN"
    }

    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }
}