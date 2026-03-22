package com.flowart.ar.drawing.sketch.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flowart.ar.drawing.sketch.fragments.GalleryFragment
import com.flowart.ar.drawing.sketch.fragments.HomeFragment
import com.flowart.ar.drawing.sketch.fragments.LessonFragment
import com.flowart.ar.drawing.sketch.fragments.SettingFragment

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> LessonFragment()
            2 -> GalleryFragment()
            3 -> SettingFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int = 4
}