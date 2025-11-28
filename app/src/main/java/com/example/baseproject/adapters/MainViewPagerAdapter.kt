package com.example.baseproject.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.fragments.GalleryFragment
import com.example.baseproject.fragments.HomeFragment
import com.example.baseproject.fragments.LessonFragment
import com.example.baseproject.fragments.SettingFragment

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity
): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> LessonFragment()
            2 -> GalleryFragment()
            3 -> SettingFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int  = 4
}