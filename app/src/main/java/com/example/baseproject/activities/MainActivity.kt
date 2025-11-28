package com.example.baseproject.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.baseproject.R
import com.example.baseproject.adapters.MainViewPagerAdapter
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityMainBinding
import com.example.baseproject.utils.setOnUnDoubleClick

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var mAdapter: MainViewPagerAdapter

    override fun initData() {

    }

    override fun initView() {
        initViewPager()
        setTabPosition(0)
    }

    override fun initActionView() {
        binding.navHome.setOnUnDoubleClick { setTabPosition(0) }
        binding.navLesson.setOnUnDoubleClick { setTabPosition(1) }
        binding.navGallery.setOnUnDoubleClick { setTabPosition(2) }
        binding.navSetting.setOnUnDoubleClick { setTabPosition(3) }
    }

    private fun initViewPager() {
        mAdapter = MainViewPagerAdapter(this)
        val onPageChangeCallBack = object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.ivInfo.isVisible = position == 0 || position == 1
                setTabPosition(position)
                when(position){
                    0 -> binding.tvTitle.text = getString(R.string.flowart_ar_drawing_sketch_art)
                    1 -> binding.tvTitle.text = getString(R.string.what_will_you_explore_today)
                    2 -> binding.tvTitle.text = getString(R.string.your_drawing_journey)
                    3 -> binding.tvTitle.text = getString(R.string.let_s_tune_tour_app)
                }
            }
        }

        binding.vpMain.apply {
            adapter = mAdapter
            registerOnPageChangeCallback(onPageChangeCallBack)
            isUserInputEnabled = false
            offscreenPageLimit = 1
        }
    }

    private fun setTabPosition(position: Int){
        binding.navHome.isSelected = position == 0
        binding.navLesson.isSelected = position == 1
        binding.navGallery.isSelected = position == 2
        binding.navSetting.isSelected = position == 3

        binding.vpMain.currentItem = position

        setSelectedText(binding.navHome)
        setSelectedText(binding.navLesson)
        setSelectedText(binding.navGallery)
        setSelectedText(binding.navSetting)

        binding.bottomNavHome.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        binding.bottomNavLesson.visibility = if (position == 1) View.VISIBLE else View.INVISIBLE
        binding.bottomNavGallery.visibility = if (position == 2) View.VISIBLE else View.INVISIBLE
        binding.bottomNavSetting.visibility = if (position == 3) View.VISIBLE else View.INVISIBLE
    }

    private fun bottomNavSelected(isSelected: Boolean){

    }

    private fun setSelectedText(textView: TextView){
        if (textView.isSelected){
            textView.setTextColor(resources.getColor(R.color.mainTextColor))
        } else{
            textView.setTextColor(resources.getColor(R.color.unSelectedText))
        }
    }

}