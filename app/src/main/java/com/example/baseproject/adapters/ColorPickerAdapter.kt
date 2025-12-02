package com.example.baseproject.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.databinding.ItemColorBinding
import com.example.baseproject.models.ColorModel

class ColorPickerAdapter(
    private val list: List<ColorModel>,
    private val onSelectColor: (Int) -> Unit,
    private val onPickColor: () -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorVH>() {

    inner class ColorVH(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root)

    private var currentColor = Color.parseColor(list[0].colorCode)
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorVH {
        val inflater = LayoutInflater.from(parent.context)
        return ColorVH(ItemColorBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ColorVH, position: Int) {
        val color = list[position]

        holder.itemView.isSelected = position == 0
        if (position == 0) {
            holder.binding.vColor.setBackgroundColor(currentColor)
        } else {
            if (color.isColor) {
                holder.binding.vColor.setBackgroundColor(Color.parseColor(color.colorCode))
            } else {
                holder.binding.vColor.setBackgroundResource(color.idSourceBg)
            }
        }

        holder.itemView.setOnClickListener {
            if (position == 2) {
                onPickColor()
            } else {
                currentColor = Color.parseColor(color.colorCode)
                onSelectColor(currentColor)
                notifyItemChanged(0)
            }
        }
    }

    fun updateColor(color: Int) {
        currentColor = color
        onSelectColor(currentColor)
        notifyItemChanged(0)
    }
}