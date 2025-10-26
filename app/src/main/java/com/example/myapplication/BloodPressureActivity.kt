package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityBloodPressureBinding

class BloodPressureActivity : AppCompatActivity() {
    private lateinit var b: ActivityBloodPressureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBloodPressureBinding.inflate(layoutInflater)
        setContentView(b.root)

        supportActionBar?.apply {
            title = "Blood Pressure"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
