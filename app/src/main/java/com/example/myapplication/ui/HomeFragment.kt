// app/src/main/java/com/example/myapplication/ui/HomeFragment.kt
package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val summaryItems = listOf(
        SummaryItem("Heart Rate",   "72 bpm",         "Relaxed · Active · High", R.drawable.ic_heart),
        SummaryItem("Steps",        "7 520",          "5.4 km · 302 kcal",       R.drawable.ic_steps),
        SummaryItem("Calories",     "2 300 kcal",     "2 600 eaten · –300",      R.drawable.ic_calories),
        SummaryItem("Weight",       "72.4 kg",        "+0.2 kg since last",      R.drawable.ic_weight),
        SummaryItem("SpO₂",         "97 %",           "Normal",                  R.drawable.ic_spo2),
        SummaryItem("Activity",     "57 min",         "Last night: 8h 5m",       R.drawable.ic_activity),
        SummaryItem("Blood Press.", "118 / 76 mmHg",  "Last: 120/80",            R.drawable.ic_pressure),
        SummaryItem("Sleep",        "7.8 h",          "Last night: Deep 1h 12m", R.drawable.ic_sleep),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSummary.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSummary.adapter = SummaryAdapter(summaryItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
