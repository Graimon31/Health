package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Если хотите использовать свой Toolbar — тема NoActionBar позволяет это.
        // setSupportActionBar(b.toolbar)

        // Инициализируем клики по карточкам
        setupCards()

        // Устанавливаем слушатель нижнего меню
        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_stats -> { startActivity(Intent(this, StatsActivity::class.java)); true }
                R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); true }
                R.id.nav_analysis -> { startActivity(Intent(this, AnalysisActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun setupCards() {
        b.cardHeart.setOnClickListener { startActivity(Intent(this, HeartRateActivity::class.java)) }
        b.cardSteps.setOnClickListener { startActivity(Intent(this, StepsActivity::class.java)) }
        b.cardCalories.setOnClickListener { startActivity(Intent(this, CaloriesActivity::class.java)) }
        b.cardPressure.setOnClickListener { startActivity(Intent(this, BloodPressureActivity::class.java)) }
        b.cardWeight.setOnClickListener { startActivity(Intent(this, WeightActivity::class.java)) }
        b.cardOxygen.setOnClickListener { startActivity(Intent(this, OxygenActivity::class.java)) }
        b.cardSleep.setOnClickListener { startActivity(Intent(this, SleepActivity::class.java)) }
        b.cardRespiratory.setOnClickListener { startActivity(Intent(this, RespiratoryActivity::class.java)) }
    }
}