package com.example.baseproject.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.baseproject.databinding.ItemAdsBinding
import com.example.baseproject.databinding.ItemLessonBinding
import com.example.baseproject.models.LessonModel
import com.example.baseproject.utils.setOnUnDoubleClick

class LessonAdapter(
    val context: Context,
    val onClickItem: (LessonModel) -> Unit,
    val onClickFavorite: (LessonModel) -> Unit,
    private val isShowAds: Boolean
) :
    ListAdapter<LessonModel, LessonAdapter.LessonVH>(
        diffUtil
    ) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<LessonModel>() {
            override fun areItemsTheSame(oldItem: LessonModel, newItem: LessonModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LessonModel, newItem: LessonModel): Boolean {
                return oldItem.id == newItem.id && oldItem.isFavorite == newItem.isFavorite && oldItem.difficulty == newItem.difficulty
            }
        }
    }

    inner class LessonVH(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lesson: LessonModel) {
            if (binding is ItemLessonBinding)
                bindLesson(lesson)
            else if (binding is ItemAdsBinding) {
                bindAds()
            }
        }

        private fun bindAds() {
//            val binding = binding as ItemAdsBinding
//            AdmobLib.loadAndShowNative(
//                context as Activity,
//                AdsManager.nativeOtherModel,
//                binding.frNative,
//                size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
//                R.layout.native_ads_item
//            )
        }

        private fun bindLesson(lesson: LessonModel) {
            val binding = binding as ItemLessonBinding
            binding.lessonProgress.rating = lesson.difficulty.toFloat()
            binding.ivFavorite.isSelected = lesson.isFavorite
            binding.tvStep.text = lesson.listStep.size.toString()
            Glide.with(context).load("file:///android_asset/" + lesson.listStep.last())
                .into(binding.ivImage)
            binding.ivFavorite.setOnClickListener {
                onClickFavorite(lesson)
            }
            itemView.setOnUnDoubleClick {
                onClickItem(lesson)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAds && position == 1) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonVH {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            LessonVH(ItemLessonBinding.inflate(inflater, parent, false))
        } else {
            LessonVH(ItemAdsBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: LessonVH, position: Int) {
        val lesson = getItem(position)
        holder.bind(lesson)
    }
}