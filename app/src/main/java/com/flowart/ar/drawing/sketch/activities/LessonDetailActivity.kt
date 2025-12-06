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
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LessonDetailActivity :
    BaseActivity<ActivityLessonDetailBinding>(ActivityLessonDetailBinding::inflate) {
    private val level by lazy {
        intent.getIntExtra("level", 0)
    }
    private var adapter: LessonAdapter? = null
    private var listLesson = listOf<LessonModel>()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            loadAndShowInterBackHome(binding.vShowInterAds) {
                SharedPrefManager.putBoolean("wantShowRate", true)
                finish()
            }
        }
    }
    private var adsItemId = System.currentTimeMillis().toInt()

    override fun initData() {
        adapter = LessonAdapter(
            this,
            onClickItem = { handleClickLesson(it) },
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
        adsItemId = System.currentTimeMillis().toInt()
        adapter?.submitList(listWithAdsItem(listLesson))

        //loadAndShowNativeOther(binding.frBanner)
        loadAndShowNativeOther()
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }

    private fun listWithAdsItem(list: List<LessonModel>): List<LessonModel> {
        return if (list.isEmpty()) {
            list
        } else {
            val newList = list.toMutableList()
            newList.toList()
        }
    }

    fun loadAndShowNativeOther() {
        when (RemoteConfig.remoteNativeOther) {
            1L -> {
                binding.frNativeSmall.visible()
                binding.frNativeExpand.visible()
                AdmobLib.loadAndShowNativeCollapsibleSingle(
                    activity = this,
                    admobNativeModel = AdsManager.NATIVE_OTHER,
                    viewGroupExpanded = binding.frNativeExpand,
                    viewGroupCollapsed = binding.frNativeSmall,
                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.whiteLine.visible()
                    },
                    onAdsLoadFail = {
                        binding.whiteLine.gone()
                    }
                )
            }
        }
    }
}