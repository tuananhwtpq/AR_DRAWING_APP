package com.example.baseproject.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.activities.PreviewImageActivity
import com.example.baseproject.adapters.ImageAdapter
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.models.ImageModel
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.setOnUnDoubleClick
import com.example.baseproject.utils.showToast
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import kotlin.math.min

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private var trendingAdapter: ImageAdapter? = null
    private var recentAdapter: ImageAdapter? = null
    private var imageUri: Uri? = null

    val requestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                launchCamera()
            } else {
                showToast("Camera Permission Denied")
            }
        }

    val takeImageLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                imageUri?.let {
                    val intent = Intent(requireContext(), PreviewImageActivity::class.java)
                    intent.putExtra(Constants.KEY_IMAGE_URI, it.toString())
                    startActivity(intent)
                }
            }
        }

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val intent = Intent(requireContext(), PreviewImageActivity::class.java)
            intent.putExtra(Constants.KEY_IMAGE_URI, uri.toString())
            startActivity(intent)
        } else {
            showToast("No image selected")
        }
    }


    override fun initData() {

    }

    override fun initView() {
        setupRCV()
    }

    override fun initActionView() {

        binding.layoutGallery.setOnUnDoubleClick {
            pickImageLauncher.launch("image/*")
        }

        binding.layoutCamera.setOnUnDoubleClick {

            checkCameraPermission()
        }

        binding.tvSeeAllAnime.setOnUnDoubleClick { goToCategory(1, R.string.anime) }
        binding.tvSeeAllCartoon.setOnUnDoubleClick { goToCategory(2, R.string.cartoon) }
        binding.tvSeeAllAnimal.setOnUnDoubleClick { goToCategory(3, R.string.animal) }
        binding.tvSeeAllChibi.setOnUnDoubleClick { goToCategory(4, R.string.chibi) }
        binding.tvSeeAllCute.setOnUnDoubleClick { goToCategory(5, R.string.flower) }
    }

    private fun checkCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestLauncher.launch(permissions)
    }

    private fun launchCamera() {
//        lifecycleScope.launch {
//            val files = withContext(Dispatchers.IO) {
//                try {
//                    //createImageFile()
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error creating image file: ${e.message} - ${e.printStackTrace()}")
//                    null
//                }
//            }
//
//            if (files != null) {
//                imageUri = FileProvider.getUriForFile(
//                    requireContext(),
//                    "${requireActivity().applicationContext.packageName}.fileprovider",
//                    files
//                )
//                imageUri?.let {
//                    takeImageLauncher.launch(it)
//                }
//            } else {
//                showToast("Unable to create image file")
//            }
//        }
    }

//        private fun createImageFile(): File {
//        val currentTime = System.currentTimeMillis()
//        val simpleFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
//        val formatedTime = simpleFormat.format(currentTime)
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile("JPEG_${formatedTime}_", ".jpg", storageDir)
//    }

    private fun setupRCV() {
        trendingAdapter = ImageAdapter(
            requireContext(),
            isTrending = true,
            onItemClick = { handleImageClick(it) },
            onFavoriteClick = {})

        recentAdapter = ImageAdapter(
            requireContext(),
            onItemClick = { handleImageClick(it) },
            onFavoriteClick = { handleFavoriteClick(it) })

        ImageRepositories.INSTANCE.getImageByCategory(0).observe(this) {
            trendingAdapter?.submitList(it) {
                //(activity as? MainActivity)?.showLoading(false)
            }
        }
        binding.rcvTrending.adapter = trendingAdapter
        binding.rcvRecent.adapter = recentAdapter

        ImageRepositories.INSTANCE.getRecentImage().observe(this) {
            recentAdapter?.submitList(it) {
                //(activity as? MainActivity)?.showLoading(false)
                binding.rcvRecent.isVisible = it.isNotEmpty()
                binding.txtRecent.isVisible = it.isNotEmpty()
                binding.rcvRecent.smoothScrollToPosition(0)
            }
        }
//
//        val gridLayoutManager =
//            GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
//        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return if (position == 0) 2 else 1
//            }
//        }
        binding.rcvTrending.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
        binding.rcvRecent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        setRecyclerView(1, binding.rcvAnime)
        setRecyclerView(2, binding.rcvCartoon)
        setRecyclerView(3, binding.rcvAnimal)
        setRecyclerView(4, binding.rcvChibi)
        setRecyclerView(5, binding.rcvCute)

    }

    private fun handleFavoriteClick(image: ImageModel) {
        ImageRepositories.INSTANCE.updateImageFavorite(image.isFavorite, image.id)
    }

    private fun setRecyclerView(
        category: Int,
        rcv: RecyclerView
    ) {
        val adapter = ImageAdapter(
            requireContext(),
            onItemClick = { handleImageClick(it) },
            onFavoriteClick = { handleFavoriteClick(it) })
        ImageRepositories.INSTANCE.getImageByCategory(category).observe(this) {
            // (activity as? MainActivity)?.showLoading(true)
            adapter.submitList(it.subList(0, min(it.size, 6))) {
                //(activity as? MainActivity)?.showLoading(false)
            }
        }
        rcv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rcv.adapter = adapter
    }

    private fun goToCategory(type: Int, categoryName: Int) {
//        (activity as? MainActivity)?.showInterAds {
//            val intent = Intent(requireContext(), CategoryDetailActivity::class.java)
//            intent.putExtra("type", type)
//            intent.putExtra("categoryName", categoryName)
//            startActivity(intent)
//        }

    }

    private fun handleImageClick(image: ImageModel) {
        //(activity as? MainActivity)?.showInterAds {
        val intent = Intent(requireContext(), PreviewImageActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image.img)
        intent.putExtra("imageId", image.id)
        //launcher.launch(intent)
        startActivity(intent)
        //}
    }

    companion object {
        private const val TAG = "HomeFragment"
    }

}