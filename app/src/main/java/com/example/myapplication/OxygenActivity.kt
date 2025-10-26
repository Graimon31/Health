package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityOxygenBinding

class OxygenActivity : AppCompatActivity() {
    private lateinit var b: ActivityOxygenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityOxygenBinding.inflate(layoutInflater)
        setContentView(b.root)

        supportActionBar?.apply {
            title = "Oxygen Level"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
