package com.android.dialgaugeview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class DialGaugeView(
    context: Context,
    attributeSet: AttributeSet
) : View(context, attributeSet) {

    private val startAngle: Double
    private val sweepAngle: Double
    private val maxValue: Float
    private val notches: Int
    private val digitsMargin: Float
    private val textSize: Float

    private val values = hashMapOf<Double, String>()

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.DialGaugeView
        )
        startAngle = typedArray.getInt(R.styleable.DialGaugeView_dgv_start_angle, 0).toDouble()
        sweepAngle = typedArray.getInt(R.styleable.DialGaugeView_dgv_sweep_angle, 0).toDouble()
        maxValue = typedArray.getFloat(R.styleable.DialGaugeView_dgv_end_value, 0f)
        notches = typedArray.getInt(R.styleable.DialGaugeView_dgv_notches, DEFAULT_NOTCHES_COUNTER) - 1
        digitsMargin = typedArray.getDimension(
            R.styleable.DialGaugeView_dgv_digits_margin,
            DEFAULT_DIGITS_MARGIN.dpToPx()
        )
        textSize = typedArray.getDimension(
            R.styleable.DialGaugeView_dgv_text_size,
            DEFAULT_TEXT_SIZE.dpToPx()
        )
        typedArray.recycle()

        val radPerNotch = Math.toRadians(sweepAngle) / notches
        val startRad = Math.toRadians(startAngle)
        for (i in 0..notches) {
            var bigDecValue = BigDecimal.valueOf((maxValue / notches * i).toDouble())
                .setScale(3, RoundingMode.HALF_DOWN)
                .stripTrailingZeros()

            // Java BigDecimal bug
            // http://hg.openjdk.java.net/jdk8/jdk8/jdk/rev/2ee772cda1d6
            if (bigDecValue.toDouble() == 0.0) {
                bigDecValue = BigDecimal.ZERO
            }
            values[(radPerNotch * i) + startRad] = bigDecValue.toPlainString()
        }
    }

    private var center: Point = Point()

    private val paint = Paint()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.white)
        textSize = this@DialGaugeView.textSize
    }
    private val textBounds = Rect()


    private val scalePath = Path()
    private val scalePaint = Paint().apply {
        color = context.getColor(R.color.white)
        strokeWidth = SCALE_STROKE_WIDTH
        style = Paint.Style.STROKE
    }

    private val centralCirclePath = Path()
    private val centralCirclePaint = Paint().apply {
        color = context.getColor(R.color.dark_gray)
        style = Paint.Style.FILL
        setShadowLayer(SHADOW_RADIUS, 0f, 0f, context.getColor(R.color.red))
    }

    private var angle: Double = Math.toRadians(startAngle)

    fun setData(data: Double) {
        if (data > maxValue) return
        if (data < 0) return
        angle = (Math.toRadians(sweepAngle) / maxValue * data) + Math.toRadians(startAngle)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.x = w / 2
        center.y = h / 2
        scalePath.apply {
            reset()
            addArc(
                MARGIN.dpToPx(),
                MARGIN.dpToPx(),
                width - MARGIN.dpToPx(),
                width - MARGIN.dpToPx(),
                startAngle.toFloat(),
                sweepAngle.toFloat()
            )
            values.keys.forEach {
                val startPoint = getPointOnCircle(
                    it.toFloat(), (center.x - NOTCHES_OFFSET.dpToPx() - NOTCHES_LENGTH.dpToPx())
                )
                val endPoint = getPointOnCircle(it.toFloat(), (center.x - NOTCHES_OFFSET.dpToPx()))
                moveTo(center.x + startPoint[0], center.x + startPoint[1])
                lineTo(center.x + endPoint[0], center.x + endPoint[1])
            }
        }
        centralCirclePath.apply {
            addCircle(
                center.x.toFloat(),
                center.x.toFloat(),
                CENTRAL_CIRCLE_RADIUS.dpToPx(),
                Path.Direction.CCW
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(widthSize, heightSize)//min(DESIRED_WIDTH, widthSize)
            else -> DESIRED_WIDTH
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(widthSize, heightSize)
            else -> DESIRED_HEIGHT
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) throw IllegalArgumentException("canvas must be not null")
        canvas.drawColor(context.getColor(R.color.black))
        canvas.drawPath(scalePath, scalePaint)
        drawDigits(canvas)
        drawArrow(canvas)
    }

    private fun drawDigits(canvas: Canvas) {
        values.forEach {
            val startPoint = getPointOnCircle(it.key.toFloat(), center.x - digitsMargin)
            textBounds.apply {
                textPaint.getTextBounds(it.value, 0, it.value.length, this)
            }
            val textWCenter = textBounds.centerX()
            val textHCenter = textBounds.centerY()
            canvas.drawText(
                it.value,
                center.x + startPoint[0] - textWCenter,
                center.x + startPoint[1] - textHCenter,
                textPaint
            )
        }
    }

    private fun drawArrow(canvas: Canvas) {
        paint.apply {
            color = context.getColor(R.color.dark_gray)
            strokeWidth = ARROW_STROKE_WIDTH
            style = Paint.Style.STROKE
        }
        val endPointCoordinates = getPointOnCircle(angle.toFloat(), center.x)
        val startPoint1Coordinates =
            getPointOnCircle((angle + (Math.PI / 2)).toFloat(), ARROW_START_WIDTH.dpToPx())
        val startPoint2Coordinates =
            getPointOnCircle((angle - (Math.PI / 2)).toFloat(), ARROW_START_WIDTH.dpToPx())

        canvas.drawLines(
            floatArrayOf(
                center.x.toFloat() + startPoint1Coordinates[0],
                center.x.toFloat() + startPoint1Coordinates[1],
                center.x + endPointCoordinates[0],
                center.x + endPointCoordinates[1],
                center.x.toFloat() + startPoint2Coordinates[0],
                center.x.toFloat() + startPoint2Coordinates[1],
                center.x + endPointCoordinates[0],
                center.x + endPointCoordinates[1],
            ),
            paint
        )

        paint.apply {
            color = context.getColor(R.color.red)
            strokeWidth = INTERNAL_ARROW_STROKE_WIDTH
            style = Paint.Style.STROKE
        }

        val redLineEndCoordinates =
            getPointOnCircle(angle.toFloat(), (center.x * INTERNAL_ARROW_LENGTH_K).toFloat())

        canvas.drawLine(
            center.x.toFloat(),
            center.x.toFloat(),
            center.x + redLineEndCoordinates[0],
            center.x + redLineEndCoordinates[1],
            paint
        )

        canvas.drawPath(centralCirclePath, centralCirclePaint)
    }

    private fun getPointOnCircle(angle: Float, radius: Int): List<Float> = listOf(
        (cos(angle) * radius),
        (sin(angle) * radius)
    )

    private fun getPointOnCircle(angle: Float, radius: Float): List<Float> = listOf(
        (cos(angle) * radius),
        (sin(angle) * radius)
    )

    private fun Float.dpToPx(): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics
    )

    companion object {
        private const val MARGIN = 4f
        private const val CENTRAL_CIRCLE_RADIUS = 16f

        private const val ARROW_START_WIDTH = 4f
        private const val ARROW_STROKE_WIDTH = 20f
        private const val INTERNAL_ARROW_STROKE_WIDTH = 5f
        private const val INTERNAL_ARROW_LENGTH_K = 0.9

        private const val DEFAULT_DIGITS_MARGIN = 50f
        private const val DEFAULT_NOTCHES_COUNTER = 10

        private const val NOTCHES_OFFSET = 10f
        private const val NOTCHES_LENGTH = 16f
        private const val SCALE_STROKE_WIDTH = 10f
        private const val DEFAULT_TEXT_SIZE = 16f

        private const val SHADOW_RADIUS = 24f

        private const val DESIRED_HEIGHT = 350
        private const val DESIRED_WIDTH = 350
    }
}