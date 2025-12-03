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
//    private val isShowAds by lazy {
//        RemoteConfig.remoteNativeListItem != 0L
//    }

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
//            loadAndShowInterBack(binding.vShowInterAds) {
//                finish()
//            }
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
//                    adapter?.submitList(newList) {
//                        binding.lLoading.gone()
//                    }
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
//        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return if (position == 2 && isShowAds) 2 else 1
//            }
//        }
        binding.rcvPicture.layoutManager = gridLayoutManager
        binding.rcvPicture.adapter = adapter
        binding.rcvPicture.itemAnimator = null
        binding.tvCategoryName.text = getString(categoryName)
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        showBannerAds()
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
        return if (list.size < 2 /*|| !isShowAds*/) {
            list
        } else {
            val newList = list.toMutableList().apply {
//                add(
//                    2,
//                    ImageModel.EMPTY_ITEM.copy(id = adsItemId)
//                )
            }
            newList
        }
    }

    private fun handleItemClick(image: ImageModel) {
        val intent = Intent(this, PreviewImageActivity::class.java)
        intent.putExtra(Constants.KEY_IMAGE_PATH, image.img)
        intent.putExtra("imageId", image.id)
        startActivity(intent)
    }

    private fun handleFavoriteClick(image: ImageModel) {
        ImageRepositories.INSTANCE.updateImageFavorite(image.isFavorite, image.id)
    }

    private fun showBannerAds() {
//        if (RemoteConfig.remoteBannerListItem == 0L) return
//        binding.frBanner.visible()
//        binding.viewLine.visible()
//        if (RemoteConfig.remoteBannerListItem == 1L) {
//            AdmobLib.loadAndShowBanner(
//                this,
//                AdsManager.BANNER_OTHER,
//                binding.frBanner,
//                binding.viewLine
//            )
//            return
//        }
//
//        if (RemoteConfig.remoteBannerListItem == 2L) {
//            AdmobLib.loadAndShowBannerCollapsible(
//                this,
//                AdsManager.bannerCollapseListItemModel,
//                binding.frBanner,
//                binding.viewLine
//            )
//            return
//        }
    }
}