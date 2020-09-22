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
import kotlin.math.roundToInt

class RoundSquareProgressView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val TAG = RoundSquareProgressView::class.java.canonicalName
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        setStrokeJoin(Paint.Join.ROUND)
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
    private var textSize = 15f
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
                        setTextSize(getDimension(R.styleable.RoundSquareProgressView_text_size, 0f))
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
                                        Color.TRANSPARENT
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
        r = radius
        invalidate()
        requestLayout()
    }

    fun setProgressStartAngle(angle: Int) {
        progressStartAngle = angle
        invalidate()
        requestLayout()
    }

    fun setProgressPercent(percent: Float) {
        when (percent.toInt()) {
            -1, 0 -> {
                if (progressBgPaint.color != progressBgColorStatic) {
                    animateProgressBgToStaticColor()
                }
                if (isInEditMode) {
                    progressPercent = percent
                    invalidate()
                } else {
                    animateProgressChange(percent)
                }
            }
            in 1..100 -> {
                if (progressBgPaint.color != progressBgColorDynamic) {
                    animateProgressBgToDynamicColor()
                }
                if (isInEditMode) {
                    progressPercent = percent
                    invalidate()
                } else {
                    animateProgressChange(percent)
                }
            }
            else -> {
                progressPercent = -1f
                invalidate()
                if (progressBgPaint.color != progressBgColorStatic) {
                    animateProgressBgToStaticColor()
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
            textPaint.typeface = Typeface.createFromAsset(context.assets, "fonts/$fontName")
            invalidate()
            requestLayout()
        }
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
        requestLayout()
    }

    fun setTextSize(size: Float) {
        textSize = size
        invalidate()
        requestLayout()
    }

    fun setTextGravity(gravity: Int) {
        textGravity = gravity
        invalidate()
        requestLayout()
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
        val segment = createProgressPathSegment(progressPath)
        val bounds = RectF()
        segment.computeBounds(bounds, true)
        progressPaint.shader = LinearGradient(bounds.left, bounds.top, bounds.right, Math.min(bounds.width(), bounds.height()), context.getColor(R.color.progress), context.getColor(R.color.progressDark), Shader.TileMode.CLAMP)
        c.drawPath(segment, progressPaint)

        return bitmap
    }

    private fun createProgressPath(): Path {
        val radius = if (r > 0f && r < (Math.min(width, height) / 2f)) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r, resources.displayMetrics) else 0f
        val path = Path()
        path.addRoundRect(RectF(viewBounds).apply {
            inset(
                    progressPaint.strokeWidth / 2f,
                    progressPaint.strokeWidth / 2f
            )
        }, radius, radius, Path.Direction.CCW)
        path.close()
        return path
    }

    private fun createProgressPathSegment(path: Path): Path {
        val pathSegment = Path()
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length
        if (progressPercent.toInt() in 1..100) {
            val offset = pathLength / 360f * progressStartAngle + pathLength / 360f * 26f
            pathMeasure.getSegment(
                    offset,
                    pathLength / 100f * progressPercent + offset,
                    pathSegment,
                    true
            )
            if (progressPercent * pathLength / 100f > pathLength - offset) {
                val partLength = (pathLength / 100f).roundToInt()
                val partsCount = progressPercent - (pathLength - offset) / pathLength * 100
                val pathSegment2 = Path()
                pathMeasure.getSegment(0f, partsCount * partLength, pathSegment2, true)
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
        val offset = progressPaint.strokeWidth + TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                textPadding,
                resources.displayMetrics
        )
        textPaint.getTextBounds(text, 0, text.length, tempRect)
        if (isInEditMode) {
            if (textSize > 0f)
                textPaint.textSize = textSize
            else if (textSize <= 0f)
                textPaint.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        28f,
                        resources.displayMetrics
                )
        } else {
                val v1 = (viewBounds.width().toFloat() - 2f * offset) / tempRect.width().toFloat()
                val v2 = (viewBounds.height().toFloat() - 2f * offset) / tempRect.height().toFloat()
                textPaint.textSize *= v1.coerceAtMost(v2)
        }
        var x: Float
        val y = (viewBounds.height() - (textPaint.descent() + textPaint.ascent())) / 2f
        textPaint.getTextBounds(text, 0, text.length, tempRect)
        when (textGravity) {
            Gravity.LEFT -> {
                x = viewBounds.left + offset
            }
            Gravity.CENTER -> {
                x = viewBounds.width() / 2f - (tempRect.width() / 2f)
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
