package com.flowart.ar.drawing.sketch.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flowart.ar.drawing.sketch.fragments.FullScreenFragment
import com.flowart.ar.drawing.sketch.fragments.IntroFragment

class IntroViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val isShowNativeFull1: Boolean,
    private val isShowNativeFull2: Boolean,
) :
    FragmentStateAdapter(fragmentActivity) {

    private val ARG_OBJECT = "position"

    override fun createFragment(position: Int): Fragment {

        if (isShowNativeFull1 && isShowNativeFull2) {
            when (position) {
                0 -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, position)
                    fragment.arguments = args
                    return fragment
                }

                1 -> return FullScreenFragment()
                2 -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, 1)
                    fragment.arguments = args
                    return fragment
                }

                3 -> return FullScreenFragment()
                else -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, 2)
                    fragment.arguments = args
                    return fragment
                }
            }
        } else if (isShowNativeFull1) {
            when (position) {
                0 -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, position)
                    fragment.arguments = args
                    return fragment
                }

                1 -> return FullScreenFragment()
                2 -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, 1)
                    fragment.arguments = args
                    return fragment
                }

                else -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, 2)
                    fragment.arguments = args
                    return fragment
                }
            }
        } else if (isShowNativeFull2) {
            when (position) {
                0, 1 -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, position)
                    fragment.arguments = args
                    return fragment
                }

                2 -> return FullScreenFragment()
                else -> {
                    val fragment: Fragment = IntroFragment()
                    val args = Bundle()
                    args.putInt(ARG_OBJECT, 2)
                    fragment.arguments = args
                    return fragment
                }
            }
        } else {
            val fragment: Fragment = IntroFragment()
            val args = Bundle()
            args.putInt(ARG_OBJECT, position)
            fragment.arguments = args
            return fragment
        }

    }

    override fun getItemCount(): Int {
        return when {
            isShowNativeFull1 && isShowNativeFull2 -> 5
            !isShowNativeFull1 && isShowNativeFull2 -> 3
            else -> 4
        }
    }
}