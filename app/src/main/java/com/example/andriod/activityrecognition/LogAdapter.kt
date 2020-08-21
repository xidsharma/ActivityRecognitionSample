package com.example.andriod.activityrecognition

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(private val items: List<String>) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TextView(parent.context).apply {
            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_small)
            setPadding(0, spacing, 0, spacing)
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(text: String) {
            textView.text = text
        }
    }
}