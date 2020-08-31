package com.scorealarm.squareprogress.views

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import com.scorealarm.squareprogress.R

class RoundSquareProgressView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private val TAG = RoundSquareProgressView::class.java.canonicalName
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val progressBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = TextPaint()

    private val textPadding = 8f
    private val viewBounds = Rect()

    private var progressBgColorStatic = Color.BLACK
    private var progressBgColorDynamic = Color.BLACK
    private var progressPercent = -1f
    private var text = ""
    private var r = 0f
    private var progressStartAngle = 0
    private var textGravity = Gravity.NO_GRAVITY

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RoundSquareProgressView,
            defStyleAttr,
            defStyleAttr
        )
            .apply {
                try {
                    setRadius(getDimension(R.styleable.RoundSquareProgressView_radius, 0f))
                    setBgColor(
                        getColor(
                            R.styleable.RoundSquareProgressView_bg_color,
                            Color.TRANSPARENT
                        )
                    )
                    if (!isInEditMode) {
                        setTextFont(
                            getString(R.styleable.RoundSquareProgressView_font_name)
                                ?: ""
                        )
                    }
                    setText(getString(R.styleable.RoundSquareProgressView_text) ?: "")
                    setTextColor(
                        getColor(
                            R.styleable.RoundSquareProgressView_text_color,
                            Color.WHITE
                        )
                    )
                    setTextGravity(
                        getInt(
                            R.styleable.RoundSquareProgressView_text_gravity,
                            Gravity.NO_GRAVITY
                        )
                    )
                    setProgressWidth(
                        getDimension(
                            R.styleable.RoundSquareProgressView_progress_width,
                            12f
                        )
                    )
                    setProgressPercent(
                        getFloat(
                            R.styleable.RoundSquareProgressView_progress_percent,
                            0f
                        )
                    )
                    setProgressColor(
                        getColor(
                            R.styleable.RoundSquareProgressView_progress_color,
                            Color.GRAY
                        )
                    )
                    setProgressBgToStaticColor(
                        getColor(
                            R.styleable.RoundSquareProgressView_progress_bg_color_static,
                            Color.LTGRAY
                        )
                    )
                    setProgressBgToDynamicColor(
                        getColor(
                            R.styleable.RoundSquareProgressView_progress_bg_color_dynamic,
                            Color.RED
                        )
                    )
                    setProgressStartAngle(
                        getInt(
                            R.styleable.RoundSquareProgressView_progress_start_angle,
                            0
                        )
                    )
                } finally {
                    recycle()
                }
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        var width = suggestedMinimumWidth + paddingStart - paddingEnd
        var height = suggestedMinimumHeight + paddingTop - paddingBottom
        when (modeWidth) {
            MeasureSpec.AT_MOST -> {
                width = Math.min(sizeWidth, width)
            }
            MeasureSpec.EXACTLY -> {
                width = sizeWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                width = Math.min(sizeWidth, width)
            }
        }
        when (modeHeight) {
            MeasureSpec.AT_MOST -> {
                height = Math.min(sizeHeight, height)
            }
            MeasureSpec.EXACTLY -> {
                height = sizeHeight
            }
            MeasureSpec.UNSPECIFIED -> {
                height = Math.min(sizeHeight, height)
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.getClipBounds(viewBounds)

        canvas?.drawColor(Color.TRANSPARENT) //clears canvas
        canvas?.drawBitmap(createProgressBitmap(), null, viewBounds, null)
        drawText(canvas)
    }

    fun setBgColor(color: Int) {
        bgPaint.color = color
        invalidate()
        requestLayout()
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
        requestLayout()
    }

    fun setProgressBgToStaticColor(color: Int) {
        progressBgColorStatic = color
        invalidate()
        requestLayout()
    }

    fun setProgressBgToDynamicColor(color: Int) {
        progressBgColorDynamic = color
        invalidate()
        requestLayout()
    }

    fun setProgressWidth(width: Float) {
        progressPaint.strokeWidth = width
        progressBgPaint.strokeWidth = width
        invalidate()
        requestLayout()
    }

    fun setRadius(radius: Float) {
        r = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, radius, resources.displayMetrics)
        invalidate()
        requestLayout()
    }

    fun setProgressPercent(percent: Float) {
        when (percent.toInt()) {
            -1, 0 -> {
                progressPercent = -1f
                if (progressBgPaint.color != progressBgColorStatic) {
                    animateProgressBgToStaticColor()
                } else {
                    invalidate()
                }
            }
            in 1..100 -> {
                if (progressBgPaint.color != progressBgColorDynamic) {
                    animateProgressBgToDynamicColor()
                }
                progressPercent = percent
                animateProgressChange(percent)
            }
            else -> {
                progressPercent = -1f
                if (progressBgPaint.color != progressBgColorStatic) {
                    animateProgressBgToStaticColor()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun setText(text: String) {
        this.text = text
        invalidate()
        requestLayout()
    }

    fun setTextFont(fontName: String?) {
        fontName?.apply {
            if (fontName.isBlank()) {
                textPaint.typeface = Typeface.DEFAULT
            } else {
                textPaint.typeface = Typeface.createFromAsset(context.assets, "fonts/$fontName")
            }
            invalidate()
            requestLayout()
        }
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
        requestLayout()
    }

    fun setTextGravity(gravity: Int) {
        textGravity = gravity
    }

    fun setProgressStartAngle(angle: Int) {
        progressStartAngle = angle
        invalidate()
    }

    private fun animateProgressBgToDynamicColor() {
        ValueAnimator.ofArgb(progressBgPaint.color, progressBgColorDynamic).apply {
            duration = 700L
            start()
        }.addUpdateListener {
            progressBgPaint.color = it.animatedValue as Int
            invalidate()
        }
    }

    private fun animateProgressBgToStaticColor() {
        ValueAnimator.ofArgb(progressBgPaint.color, progressBgColorStatic).apply {
            duration = 700L
            start()
        }.addUpdateListener {
            progressBgPaint.color = it.animatedValue as Int
            invalidate()
        }
    }

    private fun createProgressBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        val progressPath = createProgressPath()
        c.drawPath(progressPath, bgPaint)
        c.drawPath(progressPath, progressBgPaint)
        c.drawPath(createProgressPathSegment(progressPath), progressPaint)

        return bitmap
    }

    private fun createProgressPath(): Path {
        val path = Path()
        val halfProgressWidth = progressPaint.strokeWidth / 2f
        val rectF = RectF(viewBounds)
        rectF.inset(halfProgressWidth, halfProgressWidth)
        path.addRoundRect(rectF, r, r, Path.Direction.CW)

        return path
    }

    private fun createProgressPathSegment(path: Path): Path {
        val pathSegment = Path()
        val pathSegment2 = Path()
        if (progressPercent.toInt() in 1..100) {
            val pathMeasure = PathMeasure(path, false)
            val pathLength = pathMeasure.length
            val offset = pathLength / 360f * progressStartAngle
            val progressPercent2: Float
            pathMeasure.getSegment(
                offset,
                pathLength * progressPercent / 100f + offset,
                pathSegment,
                true
            )
            if (progressPercent * pathLength / 100f > pathLength - offset) {
                progressPercent2 = progressPercent - (pathLength - offset) / (pathLength / 100f)
                pathMeasure.getSegment(0f, pathLength * progressPercent2 / 100f, pathSegment2, true)
                pathSegment.addPath(pathSegment2)
            }
        }

        return pathSegment
    }

    private fun animateProgressChange(newProgressPercent: Float) {
        val v = PropertyValuesHolder.ofFloat("progressPercent", progressPercent, newProgressPercent)
        ValueAnimator().apply {
            duration = 500L
            setValues(v)
            start()
        }.addUpdateListener {
            progressPercent = it.getAnimatedValue("progressPercent") as Float
            invalidate()
        }
    }

    private fun drawText(canvas: Canvas?) {
        val tempRect = Rect()
        textPaint.textSize = 15f // help precision to stay the same when dividing numbers
        textPaint.getTextBounds(text, 0, text.length, tempRect)
        val offset = progressPaint.strokeWidth + TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            textPadding,
            resources.displayMetrics
        )
        if (!tempRect.isEmpty) {
            val v1 = (width - 2 * offset) / (tempRect.width().toFloat() + textPadding)
            val v2 = (height - 2 * offset) / tempRect.height().toFloat()
            textPaint.textSize *= Math.min(v1, v2)
        }
        var x: Float
        val y = (viewBounds.height() - (textPaint.descent() + textPaint.ascent())) / 2
        textPaint.getTextBounds(text, 0, text.length, tempRect)
        when (textGravity) {
            Gravity.LEFT -> {
                x = viewBounds.left + offset
            }
            Gravity.CENTER -> {
                x = viewBounds.centerX() - (tempRect.width() / 2f)
            }
            Gravity.RIGHT -> {
                x = viewBounds.right - offset - tempRect.width()
            }
            else -> {
                x = viewBounds.left + offset
            }
        }
        for (c in text) {
            canvas?.drawText(
                c.toString(),
                x,
                y,
                textPaint
            )
            x += textPaint.measureText(c.toString())
        }
    }
}