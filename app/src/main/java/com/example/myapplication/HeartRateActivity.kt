package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityHeartRateBinding

class HeartRateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeartRateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeartRateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Пока никаких BLE-операций, только сменим заголовок
        title = "Heart Rate"
    }
}