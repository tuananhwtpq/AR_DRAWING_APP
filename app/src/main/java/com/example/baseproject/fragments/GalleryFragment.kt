package com.example.baseproject.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baseproject.R
import com.example.baseproject.activities.PreviewImageActivity
import com.example.baseproject.adapters.ImageAdapter
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentGalleryBinding
import com.example.baseproject.models.ImageModel
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.SharedPrefManager
import com.example.baseproject.utils.formatTime
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate) {

    private var adapter: ImageAdapter? = null

    override fun initData() {

        adapter = ImageAdapter(
            requireContext(),
            isFavorite = true,
            onItemClick = { handleItemClick(it) },
            onFavoriteClick = { handleFavoriteClick(it) })

        ImageRepositories.INSTANCE.getAllFavoriteImage().observe(this) { listImage ->
            binding.layoutEmptyState.visibility =
                if (listImage.isEmpty()) View.VISIBLE else View.GONE
            adapter?.submitList(listImage)
        }

    }

    override fun initView() {
        binding.rcvMyFavorite.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rcvMyFavorite.adapter = adapter
    }

    override fun initActionView() {

    }

    override fun onResume() {
        super.onResume()
        //showNativeAds()
        binding.tvTotalTimespent.text =
            SharedPrefManager.getLong(Constants.KEY_SPENT_TIME, 0L).formatTime(true)
        binding.tvTotalDraws.text = SharedPrefManager.getInt(Constants.KEY_DRAW_NUMBER, 0).toString()
    }

    private fun handleFavoriteClick(image: ImageModel) {
        ImageRepositories.INSTANCE.updateImageFavorite(image.isFavorite, image.id)
    }

    private fun handleItemClick(image: ImageModel) {
//        (activity as? MainActivity)?.showInterAds {
//            val intent = Intent(requireContext(), PreviewImageActivity::class.java)
//            intent.putExtra("imageId", image.id)
//            intent.putExtra(Constants.KEY_IMAGE_PATH, image.img)
//            startActivity(intent)
//        }
        val intent = Intent(requireContext(), PreviewImageActivity::class.java)
        intent.putExtra("imageId", image.id)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image.img)
        startActivity(intent)
    }
}