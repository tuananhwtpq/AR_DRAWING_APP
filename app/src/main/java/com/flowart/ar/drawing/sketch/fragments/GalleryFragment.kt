package com.flowart.ar.drawing.sketch.fragments

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.flowart.ar.drawing.sketch.activities.MainActivity
import com.flowart.ar.drawing.sketch.activities.PreviewImageActivity
import com.flowart.ar.drawing.sketch.adapters.ImageAdapter
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentGalleryBinding
import com.flowart.ar.drawing.sketch.models.ImageModel
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.formatTime
import com.ssquad.ar.drawing.sketch.db.ImageRepositories

class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate) {

    private var adapter: ImageAdapter? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isLoadingAds = false
    private var runnable = object : Runnable {
        override fun run() {
            if (!isLoadingAds && AdsManager.isReloadingNativeHome()) {
                isLoadingAds = true
                (requireActivity() as MainActivity).loadAndShowNativeHome(binding.frNative) {
                    AdsManager.updateNativeHome()
                    isLoadingAds = false
                }
            }
            handler.postDelayed(this, 5000L)
        }
    }

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
        binding.tvTotalTime.isSelected = true
        binding.tvYourDrawing.isSelected = true
    }

    override fun onResume() {
        super.onResume()

        AdsManager.lastNativeHomeShow = 0L
        handler.removeCallbacksAndMessages(null)
        handler.post(runnable)

        binding.tvTotalTimespent.text =
            SharedPrefManager.getLong(Constants.KEY_SPENT_TIME, 0L).formatTime(true)
        binding.tvTotalDraws.text =
            SharedPrefManager.getInt(Constants.KEY_DRAW_NUMBER, 0).toString()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun handleFavoriteClick(image: ImageModel) {
        ImageRepositories.INSTANCE.updateImageFavorite(image.isFavorite, image.id)
    }

    private fun handleItemClick(image: ImageModel) {
        (requireActivity() as MainActivity).loadAndShowInterHome {
            val intent = Intent(requireContext(), PreviewImageActivity::class.java)
            intent.putExtra("imageId", image.id)
            intent.putExtra(Constants.KEY_IMAGE_PATH, image.img)
            startActivity(intent)
        }
    }
}