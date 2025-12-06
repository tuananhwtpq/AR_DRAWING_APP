package com.flowart.ar.drawing.sketch.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.flowart.ar.drawing.sketch.R
import com.flowart.ar.drawing.sketch.adapters.ColorPickerAdapter
import com.flowart.ar.drawing.sketch.bases.BaseActivity
import com.flowart.ar.drawing.sketch.databinding.ActivityTraceBinding
import com.flowart.ar.drawing.sketch.dialog.color_picker.ColorPickerDialog
import com.flowart.ar.drawing.sketch.fragments.DrawGuideDialog
import com.flowart.ar.drawing.sketch.fragments.ExitDialog
import com.flowart.ar.drawing.sketch.models.LessonModel
import com.flowart.ar.drawing.sketch.utils.BitmapUtils
import com.flowart.ar.drawing.sketch.utils.Common
import com.flowart.ar.drawing.sketch.utils.Constants
import com.flowart.ar.drawing.sketch.utils.SharedPrefManager
import com.flowart.ar.drawing.sketch.utils.ads.AdsManager
import com.flowart.ar.drawing.sketch.utils.ads.RemoteConfig
import com.flowart.ar.drawing.sketch.utils.convertToPx
import com.flowart.ar.drawing.sketch.utils.gone
import com.flowart.ar.drawing.sketch.utils.onProgressChange
import com.flowart.ar.drawing.sketch.utils.setOnUnDoubleClick
import com.flowart.ar.drawing.sketch.utils.visible
import com.snake.drawingview.brushtool.data.Brush
import com.snake.drawingview.state.ActionsStacks
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.ar.drawing.sketch.db.ImageRepositories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TraceActivity : BaseActivity<ActivityTraceBinding>(ActivityTraceBinding::inflate) {
    private val image by lazy {
        intent.getStringExtra(Constants.KEY_IMAGE_PATH)
    }

    private val lessonId by lazy {
        intent.getIntExtra(Constants.KEY_LESSON_ID, 0)
    }

    private val isFromLesson by lazy {
        intent.getBooleanExtra(Constants.IS_FROM_LESSON, false)
    }

    private val maxWidth by lazy {
        binding.drawingView.width - convertToPx(16f).toInt()
    }

    private var startTime = System.currentTimeMillis()

    private var lesson: LessonModel? = null

    private var totalStep = 0
        set(value) {
            field = value
            binding.tvStep.text = getString(R.string.step_num, currentStep, totalStep)
        }

    private var templateBitmap: Bitmap? = null
    private lateinit var bitmap: Bitmap

    private val actionStack = ActionsStacks()

    private var listImage = listOf<String>()
    private val imageUri by lazy {
        intent.getStringExtra(Constants.KEY_IMAGE_URI)
    }

    private var toolOption = 0
        set(value) {
            field = value
            setOption()
        }

    private var currentStep = 0
        set(value) {
            field = value
            binding.btnPrevStep.isEnabled = value > 1
            binding.btnPrevStep.alpha = if (value > 1) 1f else 0.6f
            binding.btnNextStep.isEnabled = value < totalStep
            binding.btnNextStep.alpha = if (value < totalStep) 1f else 0.6f
            binding.tvStep.text = getString(R.string.step_num, currentStep, totalStep)
            updateDrawView(listImage[value - 1])
        }

    private var isLocked: Boolean = false
        set(value) {
            field = value
            binding.lTitle.visibility = if (value) View.INVISIBLE else View.VISIBLE
            binding.lContainer.isVisible = !value
            binding.lBottom.isVisible = !value
            binding.lLockTitle.isVisible = value
            binding.vLock.isVisible = value
        }

    private var backgroundColor = Color.TRANSPARENT
        set(value) {
            field = value
            binding.drawingView.changeBackgroundColor(value)
        }

    private var brushColor = Color.BLACK
        set(value) {
            field = value
            binding.drawingView.setBrushColor(value)
        }

    private var brushColorAdapter: ColorPickerAdapter? = null
    private var backgroundColorAdapter: ColorPickerAdapter? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            ExitDialog().init(
                onExit = {
                    loadAndShowInterBackHome(binding.vShowInterAds) {
                        SharedPrefManager.putBoolean("wantShowRate", true)
                        startTime = System.currentTimeMillis()
                        finish()
                    }
                },
                onDismiss = {
                    startTime = System.currentTimeMillis()
                }
            ).show(supportFragmentManager, "ExitDialog")
            val spentTime = SharedPrefManager.getLong(Constants.KEY_SPENT_TIME, 0L)
            SharedPrefManager.putLong(
                Constants.KEY_SPENT_TIME,
                spentTime + System.currentTimeMillis() - startTime
            )
            //finish()
        }
    }

    private var isLoading = false
    private val handler = Handler(Looper.getMainLooper())


    private var isCountingCollapsibleHome = false

    private val runnableCollapsibleSketchTrace: kotlinx.coroutines.Runnable = object :
        kotlinx.coroutines.Runnable {
        override fun run() {
            if (AdsManager.isReloadingCollapsibleSketchTrace() && !isLoading) {
                loadAndShowNativeCollapsibleDrawing { AdsManager.updateCollapsibleSketchTrace() }
            }
            handler.postDelayed(this, 1000L)

        }
    }

    override fun initData() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        bitmap = Bitmap.createBitmap(
            width,
            width,
            Bitmap.Config.ARGB_8888
        )
        binding.lLoading.visible()
        setColorAdapter()

        setDrawingView()

        if (SharedPrefManager.getBoolean("first_trace", true)) {
            SharedPrefManager.putBoolean("first_trace", false)
            DrawGuideDialog().init().show(supportFragmentManager, "DrawGuideDialog")
        }
    }

    override fun initView() {
        toolOption = 0
        binding.lStep.isVisible = isFromLesson
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.drawingView.getState().addOnStateChangedListener {
            //binding.btnUndo.isEnabled = it.canCallUndo()
            binding.btnRedo.isEnabled = it.canCallRedo()
            binding.btnUndo.setImageResource(if (it.canCallUndo()) R.drawable.ic_undo_disable else R.drawable.ic_undo_enable)
            binding.btnRedo.setImageResource(if (it.canCallRedo()) R.drawable.ic_redo_enable else R.drawable.ic_redo_disable)
        }
    }


    override fun initActionView() {
        binding.ivBack.setOnUnDoubleClick {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBrush.setOnClickListener {
            toolOption = 0

            binding.drawingView.setBrushOrEraser(Brush.Pen)
            binding.drawingView.setBrushOrEraserSize((binding.sbBrush.progress + 1) / 100f)

        }

        binding.btnErase.setOnClickListener {
            toolOption = 1

            binding.drawingView.setBrushOrEraser(Brush.HardEraser)
            binding.drawingView.setBrushOrEraserSize((binding.sbEraser.progress + 1) / 100f)


        }

        binding.btnOpacity.setOnClickListener {
            toolOption = 2

        }

        binding.btnBackground.setOnClickListener {
            toolOption = 3

        }

        binding.btnLock.setOnClickListener {
            isLocked = true
        }

        binding.btnUnlock.setOnClickListener {
            isLocked = false
        }

        binding.btnDone.setOnClickListener {
            isLocked = false
        }

        binding.btnUndo.setOnClickListener {
            if (binding.drawingView.getState().canCallUndo()) {
                binding.drawingView.undo()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.undo_limit_reached_max_50_steps_older_steps_will_be_removed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.btnRedo.setOnClickListener {
            binding.drawingView.redo()
        }

        binding.btnSave.setOnUnDoubleClick {
            loadAndShowInterDone(binding.vShowInterAds) { done() }
        }

        binding.btnPrevStep.setOnClickListener {
            currentStep -= 1
        }

        binding.btnNextStep.setOnClickListener {
            currentStep += 1
        }

        binding.sbBrush.onProgressChange { progress ->
            binding.drawingView.setBrushOrEraserSize((progress + 1) / 100f)
        }

        binding.sbEraser.onProgressChange { progress ->
            binding.drawingView.setBrushOrEraserSize((progress + 1) / 100f)
        }

        binding.sbOpacity.onProgressChange { progress ->
            templateBitmap?.let {
                binding.drawingView.setDrawingFrame(
                    BitmapUtils.applyAlphaToBitmap(it, progress),
                    bitmap,
                    actionStack
                )
            }
        }
    }

    private fun setupBottomNavVisibility(option: Int) {
        binding.bottomNavBrush.visibility = if (option == 0) View.VISIBLE else View.INVISIBLE
        binding.bottomNavEraser.visibility = if (option == 1) View.VISIBLE else View.INVISIBLE
        binding.bottomNavOpacity.visibility = if (option == 2) View.VISIBLE else View.INVISIBLE
        binding.bottomNavColor.visibility = if (option == 3) View.VISIBLE else View.INVISIBLE
    }

    @SuppressLint("ResourceAsColor")
    private fun setupTextColor(option: Int) {
        binding.navOpacity.setTextColor(
            if (option == 2) getColor(R.color.mainTextColor) else getColor(
                R.color.unSelectedText
            )
        )
        binding.navBrush.setTextColor(
            if (option == 0) getColor(R.color.mainTextColor) else getColor(
                R.color.unSelectedText
            )
        )
        binding.navEraser.setTextColor(
            if (option == 1) getColor(R.color.mainTextColor) else getColor(
                R.color.unSelectedText
            )
        )
        binding.navColor.setTextColor(
            if (option == 3) getColor(R.color.mainTextColor) else getColor(
                R.color.unSelectedText
            )
        )
    }

    private fun setOption() {
        binding.btnBrush.isSelected = toolOption == 0
        binding.btnErase.isSelected = toolOption == 1
        binding.btnOpacity.isSelected = toolOption == 2
        binding.btnBackground.isSelected = toolOption == 3

        binding.lBrush.isVisible = toolOption == 0 && binding.lBrush.isVisible == false
        binding.lEraser.isVisible = toolOption == 1 && binding.lEraser.isVisible == false
        binding.lOpacity.isVisible = toolOption == 2 && binding.lOpacity.isVisible == false
        binding.rcvBackgroundColor.isVisible =
            toolOption == 3 && binding.rcvBackgroundColor.isVisible == false

        setupBottomNavVisibility(toolOption)
        setupTextColor(toolOption)
    }

    private fun done() {
        lesson?.let {
            if (!it.isDone) {
                lifecycleScope.launch {
                    ImageRepositories.INSTANCE.markDone(lessonId)
                }
            }
        }
        val intent = Intent(this, ResultActivity::class.java)
        val bmp = BitmapUtils.setBackgroundForBitmap(bitmap, backgroundColor)
        ResultActivity.bitmap = bmp
        startActivity(intent)
    }

    private fun showColorPickerDialog(
        isBackGround: Boolean = false,
        onOk: (Int) -> Unit
    ) {
        val dialog = ColorPickerDialog(
            this,
            if (isBackGround) backgroundColor else brushColor,
            false,
            object : ColorPickerDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: ColorPickerDialog?) {

                }

                override fun onOk(dialog: ColorPickerDialog?, color: Int) {
                    onOk(color)
                }

            })
        dialog.show()
    }

    private fun setColorAdapter() {
        brushColorAdapter =
            ColorPickerAdapter(
                Common.listBrushColor,
                onSelectColor = { brushColor = it },
                onPickColor = {
                    showColorPickerDialog(false) { color ->
                        brushColor = color
                        brushColorAdapter?.updateColor(color)
                    }
                })
        backgroundColorAdapter =
            ColorPickerAdapter(
                Common.listBackgroundColor,
                onSelectColor = { backgroundColor = it },
                onPickColor = {
                    showColorPickerDialog(true) { color ->
                        backgroundColor = color
                        backgroundColorAdapter?.updateColor(color)
                    }
                })

        binding.rcvBrushColor.adapter = brushColorAdapter
        binding.rcvBackgroundColor.adapter = backgroundColorAdapter

        binding.rcvBrushColor.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        binding.rcvBackgroundColor.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
    }

    private fun setDrawingView() {
        //todo: check bitmap
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            if (isFromLesson) {
                lesson = ImageRepositories.INSTANCE.getLessonById(lessonId)
                lesson?.let {
                    withContext(Dispatchers.Main) {
                        listImage = it.listStep
                        totalStep = it.listStep.size
                        if (currentStep == 0) {
                            currentStep = 1
                        }
                    }
                    updateDrawView(listImage[currentStep - 1])
                }
            } else {
                if (imageUri == null) {
                    updateDrawView("$image")
                } else {
                    var bitmapFromUri = BitmapUtils.getBitmapFromUri(
                        this@TraceActivity,
                        Uri.parse(imageUri)
                    )

                    if (bitmapFromUri != null) {
                        templateBitmap = BitmapUtils.resizeBitmap(
                            bitmapFromUri,
                            maxWidth,
                            maxWidth
                        )
                        bitmapFromUri = templateBitmap?.let { BitmapUtils.applyAlphaToBitmap(it) }
                    }
                    binding.drawingView.setDrawingFrame(
                        bitmapFromUri, bitmap, actionStack
                    )
                }
            }

            withContext(Dispatchers.Main) {
                binding.drawingView.changeBackgroundColor(backgroundColor)

                actionStack.clear()
                binding.drawingView.getState().setActionStacks(actionStack)

                binding.lLoading.gone()
            }
        }
    }

    private fun updateDrawView(path: String) {
        val bitmapFromAsset = BitmapUtils.getBitmapFromAsset(
            this@TraceActivity,
            path,
        )
        templateBitmap = bitmapFromAsset?.let { BitmapUtils.resizeBitmap(it, maxWidth, maxWidth) }
        val currentBitmap =
            templateBitmap?.let { BitmapUtils.applyAlphaToBitmap(it, binding.sbOpacity.progress) }
        binding.drawingView.setDrawingFrame(
            currentBitmap,
            bitmap,
            actionStack
        )

        if (currentStep != totalStep) {
            binding.btnSave.alpha = 0.5f
            binding.btnSave.isEnabled = false
        } else {
            binding.btnSave.alpha = 1f
            binding.btnSave.isEnabled = true
        }

    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        loadAndShowNativeCollapsibleDrawing { AdsManager.updateCollapsibleSketchTrace() }
    }

    override fun onStart() {
        super.onStart()
        handler.post(runnableCollapsibleSketchTrace)
        isCountingCollapsibleHome = true
    }

    override fun onPause() {
        val spentTime = SharedPrefManager.getLong(Constants.KEY_SPENT_TIME, 0L)
        SharedPrefManager.putLong(
            Constants.KEY_SPENT_TIME,
            spentTime + System.currentTimeMillis() - startTime
        )
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
        if (isCountingCollapsibleHome) {
            handler.removeCallbacks(runnableCollapsibleSketchTrace)
            isCountingCollapsibleHome = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("brushColor", brushColor)
        outState.putInt("backgroundColor", backgroundColor)
        outState.putInt("toolOption", toolOption)
        outState.putInt("currentStep", currentStep)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("brushColor")) {
            brushColor = savedInstanceState.getInt("brushColor")
        }

        if (savedInstanceState.containsKey("backgroundColor")) {
            backgroundColor = savedInstanceState.getInt("backgroundColor")
        }

        if (savedInstanceState.containsKey("toolOption")) {
            toolOption = savedInstanceState.getInt("toolOption")
        }

        if (savedInstanceState.containsKey("currentStep") && isFromLesson) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                lesson = ImageRepositories.INSTANCE.getLessonById(lessonId)
                lesson?.let {
                    withContext(Dispatchers.Main) {
                        listImage = it.listStep
                        totalStep = it.listStep.size
                        currentStep = savedInstanceState.getInt("currentStep")
                    }
                    updateDrawView(listImage[currentStep - 1])
                }
            }
        }

        brushColorAdapter?.updateColor(brushColor)
        backgroundColorAdapter?.updateColor(backgroundColor)
        if (toolOption == 0) {
            binding.drawingView.setBrushOrEraser(Brush.Pen)
            binding.drawingView.setBrushOrEraserSize((binding.sbBrush.progress + 1) / 100f)
        }

        if (toolOption == 1) {
            binding.drawingView.setBrushOrEraser(Brush.HardEraser)
            binding.drawingView.setBrushOrEraserSize((binding.sbEraser.progress + 1) / 100f)
        }
    }

    fun loadAndShowNativeCollapsibleDrawing(onShowOrFailed: () -> Unit) {
        if (isLoading) return
        when (RemoteConfig.remoteNativeCollapsibleDrawing) {
            1L -> {
                isLoading = true
                binding.frNativeSmall.visible()
                AdmobLib.loadAndShowNative(
                    activity = this,
                    admobNativeModel = AdsManager.NATIVE_COLLAPSIBLE_DRAWING,
                    viewGroup = binding.frNativeSmall,
                    size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                    layout = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.viewLine.visible()
                        onShowOrFailed()
                        isLoading = false
                    },
                    onAdsLoadFail = {
                        binding.viewLine.gone()
                        onShowOrFailed()
                        isLoading = false
                    }
                )
            }

            2L -> {
                isLoading = true
                binding.frNativeSmall.visible()
                binding.frNativeExpand.visible()
                AdmobLib.loadAndShowNativeCollapsibleSingle(
                    activity = this@TraceActivity,
                    admobNativeModel = AdsManager.NATIVE_COLLAPSIBLE_DRAWING,
                    viewGroupExpanded = binding.frNativeExpand,
                    viewGroupCollapsed = binding.frNativeSmall,
                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
                    onAdsLoaded = {
                        binding.viewLine.visible()
                        onShowOrFailed()
                        isLoading = false
                    },
                    onAdsLoadFail = {
                        binding.viewLine.gone()
                        onShowOrFailed()
                        isLoading = false
                    }
                )
            }
        }


    }
}