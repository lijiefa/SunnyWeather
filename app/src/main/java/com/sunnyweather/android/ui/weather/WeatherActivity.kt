package com.sunnyweather.android.ui.weather

import android.content.Context
import android.hardware.input.InputManager
import android.inputmethodservice.InputMethodService
import android.media.Image
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastBinding
import com.sunnyweather.android.databinding.LifeIndexBinding
import com.sunnyweather.android.databinding.NowBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.logging.SimpleFormatter
import kotlin.concurrent.timer

//private lateinit var nowBinding: NowBinding
//private lateinit var forecastBinding: ForecastBinding
//private lateinit var lifeIndexBinding: LifeIndexBinding

class WeatherActivity : AppCompatActivity() {

    val viewModel: WeatherViewModel by viewModels()

    private lateinit var binding: ActivityWeatherBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        binding= ActivityWeatherBinding.inflate(layoutInflater)
//        nowBinding= NowBinding.bind(binding.root)
//        forecastBinding= ForecastBinding.bind(binding.root)
//        lifeIndexBinding= LifeIndexBinding.bind(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng=intent.getStringExtra("location_lng")?:""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat=intent.getStringExtra("location_lat")?:""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName=intent.getStringExtra("place_name")?:""
        }

        viewModel.weatherLiveData.observe(this, Observer{
            result ->
            val weather=result.getOrNull()
            if(weather!=null){
                showWeatherInfo(weather)
            }else{
                val exception=result.exceptionOrNull()
                val errorMsg=exception?.message?:"未知错误"
                Toast.makeText(this,"无法获取天气信息，${errorMsg}", Toast.LENGTH_LONG).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing=false
        })


        binding.nowLayout.btnNav.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.START)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerClosed(drawerView: View) {
                val manager=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {

            }

        })





        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

    }
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        binding.swipeRefresh.isRefreshing=true
    }
    private fun showWeatherInfo(weather: Weather) {
        binding.nowLayout.placeName.text=viewModel.placeName
        val realtime=weather.realtime
        val daily=weather.daily
        //填充now.xml的数据
        binding.nowLayout.currentTemp.text="${realtime.temperature.toInt()}℃"
        binding.nowLayout.currentSky.text= getSky(realtime.skycon)?.info
        val currentPM25Text="空气指数${realtime.airQuality.aqi.chn}"
        binding.nowLayout.currentAQI.text=currentPM25Text
        binding.nowLayout.nowLayout.setBackgroundResource(getSky(realtime.skycon)?.bg!!)
        //填充forecast.xml
        binding.forecastLayout.forecastLayout.removeAllViews()
        val days=daily.skycon.size
        for(i in 0 until days){
            val skycon=daily.skycon[i]
            val temperature=daily.temperature[i]
            val view= LayoutInflater.from(this).inflate(R.layout.forecast_item,binding.forecastLayout.forecastLayout,false)
            val dateInfo=view.findViewById<TextView>(R.id.date_info)
            val skyInfo=view.findViewById<TextView>(R.id.sky_info)
            val skyIcon=view.findViewById<ImageView>(R.id.sky_icon)
            val temperatureInfo=view.findViewById<TextView>(R.id.temperature_info)
            val simpleDateFormat= SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text=simpleDateFormat.format(skycon.date)
            val sky=getSky(skycon.value)
            skyIcon.setImageResource(sky?.icon!!)
            skyInfo.text=sky.info
            val tempText="${temperature.min} to ${temperature.max}"
            temperatureInfo.text=tempText
            binding.forecastLayout.forecastLayout.addView(view)

        }
        //填充；lifeIndex.xml
        val lifeIndex=daily.lifeIndex
        binding.lifeIndexLayout.coldRiskText.text=lifeIndex.coldRisk[0].desc
        binding.lifeIndexLayout.dressingText.text=lifeIndex.dressing[0].desc
        binding.lifeIndexLayout.carWashingText.text=lifeIndex.carWashing[0].desc
        binding.lifeIndexLayout.ultravioletText.text=lifeIndex.ultraviolet[0].desc
        binding.weatherLayout.visibility=View.VISIBLE

    }
}