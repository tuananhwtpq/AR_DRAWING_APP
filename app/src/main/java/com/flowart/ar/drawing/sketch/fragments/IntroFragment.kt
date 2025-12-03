package com.flowart.ar.drawing.sketch.fragments

import com.bumptech.glide.Glide
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.activities.IntroActivity
import com.flowart.ar.drawing.sketch.bases.BaseFragment
import com.flowart.ar.drawing.sketch.databinding.FragmentIntroBinding

class IntroFragment : BaseFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate) {

    private val ARG_OBJECT = "position"

    override fun initData() {}

    override fun initView() {
        if (arguments != null) {
            fragmentPosition(requireArguments().getInt(ARG_OBJECT))
        }
    }

    override fun initActionView() {
        binding.btnNext2.setOnClickListener {
            (activity as? IntroActivity)?.nextPage()
        }

        binding.btnNext1.setOnClickListener {
            (activity as? IntroActivity)?.nextPage()
        }
    }

    private fun fragmentPosition(position: Int) {
        when (position) {
            0 -> {
                Glide.with(this).load(R.drawable.bg_intro1).into(binding.ivIntro)
                binding.tvBig.text = getString(R.string.create_your_art_easily)
                binding.tvSmall.text =
                    getString(R.string.draw_easily_with_ar_that_guides_each_stroke_smoothly_and_helps_you_create_artwork_faster)
                binding.btnNext2.text = getString(R.string.next)
                binding.btnNext1.text = getString(R.string.next)
                binding.dotIndicator1.setImageResource(R.drawable.dot_1)
                binding.dotIndicator2.setImageResource(R.drawable.dot_1)
            }

            1 -> {
                Glide.with(this).load(R.drawable.bg_intro_2).into(binding.ivIntro)
                binding.tvBig.text = getString(R.string.endless_ar_templates)
                binding.tvSmall.text =
                    getString(R.string.explore_countless_ar_templates_that_make_practicing_your_drawing_feel_natural_and_fun)
                binding.btnNext2.text = getString(R.string.next)
                binding.btnNext1.text = getString(R.string.next)
                binding.dotIndicator1.setImageResource(R.drawable.dot_2)
                binding.dotIndicator2.setImageResource(R.drawable.dot_2)
            }

            else -> {
                Glide.with(this).load(R.drawable.bg_intro_3).into(binding.ivIntro)
                binding.tvBig.text = getString(R.string.your_collection)
                binding.tvSmall.text =
                    getString(R.string.save_and_share_all_your_favorite_sketches_effortlessly_with_just_a_few_simple_taps)
                binding.btnNext2.text = getString(R.string.start)
                binding.btnNext1.text = getString(R.string.start)
                binding.dotIndicator1.setImageResource(R.drawable.dot_3)
                binding.dotIndicator2.setImageResource(R.drawable.dot_3)
            }
        }
    }

}