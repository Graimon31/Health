package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySleepBinding

class SleepActivity : AppCompatActivity() {
    private lateinit var b: ActivitySleepBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySleepBinding.inflate(layoutInflater)
        setContentView(b.root)

        supportActionBar?.apply {
            title = "Sleep"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
