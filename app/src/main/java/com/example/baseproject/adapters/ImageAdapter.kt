package com.example.baseproject.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.baseproject.databinding.ItemAdsBinding
import com.example.baseproject.databinding.ItemImageBinding
import com.example.baseproject.databinding.ItemImageFavoriteBinding
import com.example.baseproject.databinding.ItemImageRowBinding
import com.example.baseproject.models.ImageModel
import com.example.baseproject.utils.ads.AdsManager
import com.example.baseproject.utils.setOnUnDoubleClick
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative


class ImageAdapter(
    val context: Context,
    val isTrending: Boolean = false,
    val isCategory: Boolean = false,
    val isFavorite: Boolean = false,
    val isShowAds: Boolean = false,
    val onItemClick: (ImageModel) -> Unit,
    val onFavoriteClick: (ImageModel) -> Unit
) : ListAdapter<ImageModel, ImageAdapter.ImageVH>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ImageModel>() {
            override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
                return oldItem.img == newItem.img && oldItem.isFavorite == newItem.isFavorite && oldItem.id == newItem.id
            }
        }
    }

    inner class ImageVH(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageModel: ImageModel) {
            when (binding) {
                is ItemImageBinding -> {
                    binding.ivFavorite.isSelected = imageModel.isFavorite
                    Glide.with(context).load("file:///android_asset/${imageModel.img}")
                        .into(binding.ivImage)
                    binding.ivFavorite.isVisible = !isTrending
                    binding.ivFavorite.setOnClickListener {
                        onFavoriteClick(imageModel)
                    }

                    itemView.setOnUnDoubleClick {
                        onItemClick(imageModel)
                    }
                }

                is ItemImageRowBinding -> {
                    binding.ivFavorite.isSelected = imageModel.isFavorite
                    Glide.with(context).load("file:///android_asset/${imageModel.img}")
                        .into(binding.ivImage)
                    binding.ivFavorite.isVisible = !isTrending
                    binding.ivFavorite.setOnClickListener {
                        onFavoriteClick(imageModel)
                    }

                    itemView.setOnUnDoubleClick {
                        onItemClick(imageModel)
                    }
                }

                is ItemImageFavoriteBinding -> {
                    binding.ivFavorite.isSelected = imageModel.isFavorite
                    Glide.with(context).load("file:///android_asset/${imageModel.img}")
                        .into(binding.ivImage)
                    binding.ivFavorite.isVisible = !isTrending
                    binding.ivFavorite.setOnClickListener {
                        onFavoriteClick(imageModel)
                    }
                    itemView.setOnUnDoubleClick {
                        onItemClick(imageModel)
                    }
                }

//                is ItemAdsBinding -> {
//                    AdmobLib.loadAndShowNative(
//                        context as Activity,
//                        AdsManager.nativeOtherModel,
//                        binding.frNative,
//                        size = GoogleENative.UNIFIED_MEDIUM_LIKE_BUTTON,
//                        layout = R.layout.native_ads_item
//                    )
//                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isCategory && isShowAds && position == 2 -> 3
            isCategory -> 1
            isFavorite -> 2
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> ImageVH(ItemImageBinding.inflate(inflater, parent, false))
            1 -> ImageVH(ItemImageRowBinding.inflate(inflater, parent, false))
            3 -> ImageVH(ItemAdsBinding.inflate(inflater, parent, false))
            else -> ImageVH(ItemImageFavoriteBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ImageVH, position: Int) {
        val image = getItem(position)
        holder.bind(image)
    }
}