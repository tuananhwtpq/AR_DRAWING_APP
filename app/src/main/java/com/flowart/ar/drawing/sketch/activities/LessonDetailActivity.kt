package com.flowart.ar.drawing.sketch.activities

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.adapters.LessonAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityLessonDetailBinding
import com.flowart.ar.drawing.sketch.fragments.DrawGuideDialog
import com.flowart.ar.drawing.sketch.models.LessonModel
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.visible
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LessonDetailActivity :
    BaseActivity<ActivityLessonDetailBinding>(ActivityLessonDetailBinding::inflate) {
    private val level by lazy {
        intent.getIntExtra("level", 0)
    }

    private val isShowAds by lazy {
        //RemoteConfig.remoteNativeListItem != 0L
    }

    private var adapter: LessonAdapter? = null
    private var listLesson = listOf<LessonModel>()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
//            loadAndShowInterBack(binding.vShowInterAds) {
//                finish()
//            }
            finish()
        }
    }
    private var adsItemId = System.currentTimeMillis().toInt()

    override fun initData() {
        adapter = LessonAdapter(
            this,
            onClickItem = { handleClickLesson(it) },
            onClickFavorite = { handleClickFavorite(it) },
            isShowAds = false
        )

        ImageRepositories.INSTANCE.getLessonByLevel(level).observe(this) {
            binding.lLoading.visible()
            lifecycleScope.launch {
                listLesson = it
                val listWithAds = listWithAdsItem(listLesson)
                withContext(Dispatchers.Main) {
                    adapter?.submitList(listWithAds) {
                        binding.lLoading.gone()
                    }
                }
            }
        }
    }

    override fun initView() {
        when (level) {
            0 -> binding.tvLessonName.text = getString(R.string.beginner)
            1 -> binding.tvLessonName.text = getString(R.string.intermediate)
            2 -> binding.tvLessonName.text = getString(R.string.professional)
        }
        binding.rcvLesson.layoutManager = LinearLayoutManager(this)
        binding.rcvLesson.adapter = adapter
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    override fun initActionView() {
        binding.ivBack.setOnUnDoubleClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivInfo.setOnClickListener {
            DrawGuideDialog().init().show(supportFragmentManager, "DrawGuideDialog")
        }
    }

    private fun handleClickLesson(lesson: LessonModel) {
        val intent = Intent(this, PreviewImageActivity::class.java)
        intent.putExtra(Constants.IS_FROM_LESSON, true)
        intent.putExtra(Constants.KEY_IMAGE_PATH, lesson.listStep.last())
        intent.putExtra(Constants.KEY_LESSON_ID, lesson.id)
        startActivity(intent)
    }

    private fun handleClickFavorite(lesson: LessonModel) {
        ImageRepositories.INSTANCE.updateLessonFavorite(lesson.isFavorite, lesson.id)
    }

    override fun onResume() {
        super.onResume()
        showBannerAds()
        adsItemId = System.currentTimeMillis().toInt()
        adapter?.submitList(listWithAdsItem(listLesson))
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }

    private fun listWithAdsItem(list: List<LessonModel>): List<LessonModel> {
//        return if (list.isEmpty() || !isShowAds) {
//            list
//        } else {
//            val newList = list.toMutableList().apply {
//                add(1, LessonModel.ITEM_ADS.copy(id = adsItemId))
//            }
//            newList.toList()
//        }

        return if (list.isEmpty()) {
            list
        } else {
            val newList = list.toMutableList()
            newList.toList()
        }
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