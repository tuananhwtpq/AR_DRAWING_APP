package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.flowart.ar.drawing.sketch.adapters.ImageAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityCategoryDetailBinding
import com.flowart.ar.drawing.sketch.fragments.DrawGuideDialog
import com.flowart.ar.drawing.sketch.models.ImageModel
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.visible
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryDetailActivity : BaseActivity<ActivityCategoryDetailBinding>(
    ActivityCategoryDetailBinding::inflate
) {

    private var originalList = listOf<ImageModel>()

    private var adsItemId = System.currentTimeMillis().toInt()

    private var adapter: ImageAdapter? = null
    private val type by lazy {
        intent.getIntExtra("type", 0)
    }

    private val categoryName by lazy {
        intent.getIntExtra("categoryName", 0)
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
                SharedPrefManager.putBoolean("wantShowRate", true)
                finish()

        }
    }

    override fun initData() {
        adapter = ImageAdapter(
            this,
            isCategory = true,
            isShowAds = false,
            onItemClick = { handleItemClick(it) },
            onFavoriteClick = { handleFavoriteClick(it) })

        if (type == 0) {
            ImageRepositories.INSTANCE.trendingImages.observe(this) {
                binding.lLoading.visible()
                CoroutineScope(Dispatchers.IO).launch {
                    originalList = it
                    val newList = listItemWithAds(originalList)
                    withContext(Dispatchers.Main) {
                        adapter?.submitList(newList) {
                            binding.lLoading.gone()
                        }
                    }
                }
            }
        } else {
            ImageRepositories.INSTANCE.getImageByCategory(type).observe(this) {
                binding.lLoading.visible()
                CoroutineScope(Dispatchers.IO).launch {
                    originalList = it
                    val newList = listItemWithAds(originalList)
                    withContext(Dispatchers.Main) {
                        adapter?.submitList(it) {
                            binding.lLoading.gone()
                        }
                    }
                }
            }
        }
    }

    override fun initView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.rcvPicture.layoutManager = gridLayoutManager
        binding.rcvPicture.adapter = adapter
        binding.rcvPicture.itemAnimator = null
        binding.tvCategoryName.text = getString(categoryName)
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        adsItemId = System.currentTimeMillis().toInt()
        lifecycleScope.launch {
            val newList = listItemWithAds(originalList)
            withContext(Dispatchers.Main) {
                adapter?.submitList(newList)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }

    override fun initActionView() {
        binding.ivBack.setOnUnDoubleClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivInfo.setOnClickListener {
            DrawGuideDialog().init().show(supportFragmentManager, "DrawGuideDialog")
        }

    }

    private fun listItemWithAds(list: List<ImageModel>): List<ImageModel> {
        return if (list.size < 2) {
            list
        } else {
            val newList = list.toMutableList().apply {
            }
            newList
        }
    }

    private fun handleItemClick(image: ImageModel) {
        val intent = Intent(this, PreviewImageActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image.img)
        intent.putExtra("imageId", image.id)
        intent.putExtra("isFromCategoryDetail", false)
        startActivity(intent)
    }

    private fun handleFavoriteClick(image: ImageModel) {
        ImageRepositories.INSTANCE.updateImageFavorite(image.isFavorite, image.id)
    }
}