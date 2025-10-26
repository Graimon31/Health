package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSummaryCardBinding

class SummaryAdapter(
    private val items: List<SummaryItem>
) : RecyclerView.Adapter<SummaryAdapter.VH>() {

    inner class VH(val bind: ItemSummaryCardBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSummaryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val it = items[pos]
        with(holder.bind) {
            icIcon.setImageResource(it.iconRes)
            tvTitle.text       = it.title
            tvValue.text       = it.value
            tvDetail.text      = it.detail
        }
    }

    override fun getItemCount() = items.size
}
