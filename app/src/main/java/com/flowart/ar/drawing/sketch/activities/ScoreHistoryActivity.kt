package com.flowart.ar.drawing.sketch.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.ai.ScoreChartView
import com.flowart.ar.drawing.sketch.ai.ScoreDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ScoreHistoryActivity — Màn hình hiển thị lịch sử tiến bộ.
 *
 * Hiển thị:
 * - 3 stat cards (Best, Average, Total)
 * - Line chart tiến bộ qua thời gian
 */
class ScoreHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_history)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        loadData()
    }

    private fun loadData() {
        val scoreDao = ScoreDatabase.getInstance(this).scoreDao()

        lifecycleScope.launch {
            val best = withContext(Dispatchers.IO) { scoreDao.getHighestScore() }
            val avg = withContext(Dispatchers.IO) { scoreDao.getAverageScore() }
            val total = withContext(Dispatchers.IO) { scoreDao.getTotalDrawings() }
            val allScores = withContext(Dispatchers.IO) { scoreDao.getAll() }

            if (total == 0) {
                // Empty state
                findViewById<TextView>(R.id.tvEmpty).visibility = View.VISIBLE
                return@launch
            }

            // Stat cards
            findViewById<TextView>(R.id.tvBestScore).text = "${((best ?: 0f) * 100).toInt()}%"
            findViewById<TextView>(R.id.tvAvgScore).text = "${((avg ?: 0f) * 100).toInt()}%"
            findViewById<TextView>(R.id.tvTotalDrawings).text = "$total"

            // Chart — đảo thứ tự để cũ → mới (reversed vì getAll() = DESC)
            val chartData = allScores.reversed().map { it.overallScore }
            findViewById<ScoreChartView>(R.id.scoreChart).setScores(chartData)
        }
    }
}
