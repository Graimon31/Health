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

        // 1) Пока НЕ вызываем setSupportActionBar — тема уже NoActionBar
        // setSupportActionBar(b.toolbar)

        // 2) Клик по карточкам оставим
        setupCards()

        // 3) BottomNavigation: «безопасно» — с запасным вариантом
        try {
            b.bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true
                    R.id.nav_stats -> { startActivity(Intent(this, StatsActivity::class.java)); true }
                    R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); true }
                    R.id.nav_analysis -> { startActivity(Intent(this, AnalysisActivity::class.java)); true }
                    else -> false
                }
            }
        } catch (t: Throwable) {
            // если вдруг у тебя старая версия design-библиотеки — используем старый лисенер
            try {
                @Suppress("DEPRECATION")
                b.bottomNav.setOnNavigationItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.nav_home -> true
                        R.id.nav_stats -> { startActivity(Intent(this, StatsActivity::class.java)); true }
                        R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); true }
                        R.id.nav_analysis -> { startActivity(Intent(this, AnalysisActivity::class.java)); true }
                        else -> false
                    }
                }
            } catch (_: Throwable) {
                // если даже так что-то не так — не валим активити
            }
        }
    }

    private fun setupCards() { // как у тебя и было
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