package com.taruc.visory.admin

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.taruc.visory.R

import com.taruc.visory.admin.dummy.DummyContent.DummyItem
import kotlinx.android.synthetic.main.fragment_feedback.view.*

val honeydew = Color.rgb(240,255,240)
val seashell = Color.rgb(255, 245, 238)
val grey = Color.rgb(211, 211, 211)

class MyFeedbackRecyclerViewAdapter(
    private val values: List<DummyItem>
) : RecyclerView.Adapter<MyFeedbackRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
        holder.itemView.feedback_cardView.setCardBackgroundColor(honeydew)

        if(position == 2 || position in 4 until 6){
            holder.itemView.feedback_cardView.setCardBackgroundColor(seashell)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.feedback_name)
        val contentView: TextView = view.findViewById(R.id.feedback_date)

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}