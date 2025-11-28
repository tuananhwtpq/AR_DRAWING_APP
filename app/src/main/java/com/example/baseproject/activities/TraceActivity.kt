package com.example.baseproject.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityTraceBinding
import com.example.baseproject.models.LessonModel
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.SharedPrefManager
import com.example.baseproject.utils.ads.AdsManager
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.setOnUnDoubleClick
import com.example.baseproject.utils.visible
import com.snake.drawingview.brushtool.data.Brush
import com.snake.drawingview.state.ActionsStacks
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
       // binding.drawingView.width - convertToPx(16f).toInt()
    }

    private var startTime = System.currentTimeMillis()

    private var lesson: LessonModel? = null

    private var totalStep = 0
        set(value) {
            field = value
            //binding.tvStep.text = getString(R.string.step_num, currentStep, totalStep)
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
            //binding.tvStep.text = getString(R.string.step_num, currentStep, totalStep)
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

    private var backgroundColor = Color.WHITE
        set(value) {
            field = value
            binding.drawingView.changeBackgroundColor(value)
        }

    private var brushColor = Color.BLACK
        set(value) {
            field = value
            binding.drawingView.setBrushColor(value)
        }

//    private var brushColorAdapter: ColorPickerAdapter? = null
//    private var backgroundColorAdapter: ColorPickerAdapter? = null
//
//    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            ExitDialog().init(
//                onExit = {
//                    loadAndShowInterBack(binding.vShowInterAds) {
//                        startTime = System.currentTimeMillis()
//                        finish()
//                    }
//                },
//                onDismiss = {
//                    startTime = System.currentTimeMillis()
//                }
//            ).show(supportFragmentManager, "ExitDialog")
//            val spentTime = SharedPrefManager.getLong(Constants.KEY_SPENT_TIME, 0L)
//            SharedPrefManager.putLong(
//                Constants.KEY_SPENT_TIME,
//                spentTime + System.currentTimeMillis() - startTime
//            )
//        }
//    }

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

//        if (SharedPrefManager.getBoolean("first_trace", true)) {
//            SharedPrefManager.putBoolean("first_trace", false)
//            DrawGuideDialog().init().show(supportFragmentManager, "DrawGuideDialog")
//        }
    }

    override fun initView() {
        toolOption = 0
        binding.lStep.isVisible = isFromLesson
        //onBackPressedDispatcher.addCallback(onBackPressedCallback)
        binding.drawingView.getState().addOnStateChangedListener {
            binding.btnUndo.isEnabled = it.canCallUndo()
            binding.btnRedo.isEnabled = it.canCallRedo()
            binding.btnUndo.setImageResource(if (it.canCallUndo()) R.drawable.ic_undo_enable else R.drawable.ic_undo_disable)
            binding.btnRedo.setImageResource(if (it.canCallRedo()) R.drawable.ic_redo_disable else R.drawable.ic_redo_disable)
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
            binding.drawingView.undo()
        }

        binding.btnRedo.setOnClickListener {
            binding.drawingView.redo()
        }

        binding.btnSave.setOnUnDoubleClick {
            showInterDone { done() }
        }

        binding.btnPrevStep.setOnClickListener {
            currentStep -= 1
        }

        binding.btnNextStep.setOnClickListener {
            currentStep += 1
        }

//        binding.sbBrush.onProgressChange { progress ->
//            binding.drawingView.setBrushOrEraserSize((progress + 1) / 100f)
//        }
//
//        binding.sbEraser.onProgressChange { progress ->
//            binding.drawingView.setBrushOrEraserSize((progress + 1) / 100f)
//        }
//
//        binding.sbOpacity.onProgressChange { progress ->
//            templateBitmap?.let {
//                binding.drawingView.setDrawingFrame(
//                    BitmapUtils.applyAlphaToBitmap(it, progress),
//                    bitmap,
//                    actionStack
//                )
//            }
//        }
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

//        binding.btnBrush.setImageResource(if (toolOption == 0) R.drawable.ic_brush_white else R.drawable.ic_brush)
//        binding.btnErase.setImageResource(if (toolOption == 1) R.drawable.ic_eraser_white else R.drawable.ic_eraser)
//        binding.btnOpacity.setImageResource(if (toolOption == 2) R.drawable.ic_opacity_selected else R.drawable.ic_opacity_unselected)
//        binding.btnBackground.setImageResource(if (toolOption == 3) R.drawable.ic_palette_white else R.drawable.ic_palette)
    }

    private fun done() {
        lesson?.let {
            if (!it.isDone) {
                lifecycleScope.launch {
                    ImageRepositories.INSTANCE.markDone(lessonId)
                }
            }
        }
//        val intent = Intent(this, ResultActivity::class.java)
//        val bmp = BitmapUtils.setBackgroundForBitmap(bitmap, backgroundColor)
//        ResultActivity.bitmap = bmp
//        startActivity(intent)
    }

    private fun showColorPickerDialog(
        isBackGround: Boolean = false,
        onOk: (Int) -> Unit
    ) {
//        val dialog = ColorPickerDialog(
//            this,
//            if (isBackGround) backgroundColor else brushColor,
//            false,
//            object : ColorPickerDialog.OnAmbilWarnaListener {
//                override fun onCancel(dialog: ColorPickerDialog?) {
//
//                }
//
//                override fun onOk(dialog: ColorPickerDialog?, color: Int) {
//                    onOk(color)
//                }
//
//            })
//
//        dialog.show()
    }

    private fun setColorAdapter() {
//        brushColorAdapter =
//            ColorPickerAdapter(
//                Common.listBrushColor,
//                onSelectColor = { brushColor = it },
//                onPickColor = {
//                    showColorPickerDialog(false) { color ->
//                        brushColor = color
//                        brushColorAdapter?.updateColor(color)
//                    }
//                })
//        backgroundColorAdapter =
//            ColorPickerAdapter(Common.listBackgroundColor,
//                onSelectColor = { backgroundColor = it },
//                onPickColor = {
//                    showColorPickerDialog(true) { color ->
//                        backgroundColor = color
//                        backgroundColorAdapter?.updateColor(color)
//                    }
//                })
//
//        binding.rcvBrushColor.adapter = brushColorAdapter
//        binding.rcvBackgroundColor.adapter = backgroundColorAdapter

        binding.rcvBrushColor.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        binding.rcvBackgroundColor.layoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
    }

    private fun setDrawingView() {
        //todo: check bitmap
//        CoroutineScope(Dispatchers.IO).launch {
//            delay(1000)
//            if (isFromLesson) {
//                lesson = ImageRepositories.INSTANCE.getLessonById(lessonId)
//                lesson?.let {
//                    withContext(Dispatchers.Main) {
//                        listImage = it.listStep
//                        totalStep = it.listStep.size
//                        if (currentStep == 0) {
//                            currentStep = 1
//                        }
//                    }
//                    updateDrawView(listImage[currentStep - 1])
//                }
//            } else {
//                if (imageUri == null) {
//                    updateDrawView("$image")
//                } else {
//                    var bitmapFromUri = BitmapUtils.getBitmapFromUri(
//                        this@TraceActivity,
//                        Uri.parse(imageUri)
//                    )
//
//                    if (bitmapFromUri != null) {
//                        templateBitmap = BitmapUtils.resizeBitmap(
//                            bitmapFromUri,
//                            maxWidth,
//                            maxWidth
//                        )
//                        bitmapFromUri = templateBitmap?.let { BitmapUtils.applyAlphaToBitmap(it) }
//                    }
//                    binding.drawingView.setDrawingFrame(
//                        bitmapFromUri, bitmap, actionStack
//                    )
//                }
//            }
//
//            withContext(Dispatchers.Main) {
//                binding.drawingView.changeBackgroundColor(backgroundColor)
//                binding.lLoading.gone()
//            }
//        }
    }

    private fun updateDrawView(path: String) {
//        val bitmapFromAsset = BitmapUtils.getBitmapFromAsset(
//            this@TraceActivity,
//            path,
//        )
//        templateBitmap = bitmapFromAsset?.let { BitmapUtils.resizeBitmap(it, maxWidth, maxWidth) }
//        val currentBitmap =
//            templateBitmap?.let { BitmapUtils.applyAlphaToBitmap(it, binding.sbOpacity.progress) }
//        binding.drawingView.setDrawingFrame(
//            currentBitmap,
//            bitmap,
//            actionStack
//        )
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        showNativeColl()
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //todo: save instance
        outState.putInt("brushColor", brushColor)
        outState.putInt("backgroundColor", backgroundColor)
        outState.putInt("toolOption", toolOption)
        outState.putInt("currentStep", currentStep)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //todo: on restore instance
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

//        brushColorAdapter?.updateColor(brushColor)
//        backgroundColorAdapter?.updateColor(backgroundColor)
        if (toolOption == 0) {
            binding.drawingView.setBrushOrEraser(Brush.Pen)
            binding.drawingView.setBrushOrEraserSize((binding.sbBrush.progress + 1) / 100f)
        }

        if (toolOption == 1) {
            binding.drawingView.setBrushOrEraser(Brush.HardEraser)
            binding.drawingView.setBrushOrEraserSize((binding.sbEraser.progress + 1) / 100f)
        }
    }

    private fun showInterDone(navAction: () -> Unit) {
//        if (AdsManager.isShowInterDone()) {
//            loadAndShowInterWithNativeAfter(AdsManager.interOtherModel, binding.vShowInterAds) {
//                navAction()
//            }
//        } else {
//            navAction()
//        }
    }

    private fun showNativeColl() {
//        when (RemoteConfig.remoteNativeCollapsibleTrace) {
//            0L -> return
//            1L -> {
//                binding.viewLine.invisible()
//                binding.frNative.visible()
//                AdmobLib.loadAndShowNative(
//                    this,
//                    AdsManager.nativeOtherModel,
//                    binding.frBanner,
//                    size = GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
//                    layout = R.layout.native_ads_custom_small_like_banner,
//                    onAdsLoadFail = {
//                        binding.viewLine.gone()
//                    }
//                )
//            }
//
//            2L -> {
//                binding.frNative.visible()
//                binding.frBanner.visible()
//                binding.viewLine.invisible()
//                AdmobLib.loadAndShowNativeCollapsibleSingle(
//                    this,
//                    AdsManager.nativeOtherModel,
//                    viewGroupExpanded = binding.frNative,
//                    viewGroupCollapsed = binding.frBanner,
//                    layoutExpanded = R.layout.native_ads_custom_medium_bottom,
//                    layoutCollapsed = R.layout.native_ads_custom_small_like_banner,
//                    onAdsLoadFail = {
//                        binding.viewLine.gone()
//                    }
//                )
//            }
//        }
    }
}