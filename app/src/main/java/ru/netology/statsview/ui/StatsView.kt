package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes,
) {

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5)
    private var colors = emptyList<Int>()

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
            )
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)
    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth.toFloat()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth - 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var sumPercents = data.sum()
        var startAngle = -90F

//        for ((index, datum) in data.withIndex()) {
//            val angle = (datum / sumPercents) * 360F
//            paint.color = colors.getOrNull(index) ?: generateRandomColor()
//            canvas.drawArc(oval, startAngle , angle * progress, false, paint)
//            startAngle += angle
//        }

        data.forEachIndexed { index, datum ->
            val angle = (datum / sumPercents) * 360F
            paint.color = colors.getOrNull(index) ?: generateRandomColor()
            canvas.drawArc(oval, startAngle + progress * 360F, angle * progress, false, paint)
            startAngle += angle
        }.apply {
            if (progress == 1F) {
                paint.color = colors[0]
                canvas.drawPoint(center.x, center.y - radius, paint)
            }
        }
//        data.forEachIndexed { index, datum ->
//            val angle = (datum / sumPercents) * 360F
//            paint.color = colors.getOrElse(index) { generateRandomColor() }
//            canvas.drawArc(oval, startAngle * progress, angle * progress, false, paint)
//            startAngle += angle
//        }

        canvas.drawText(
            "%.2f%%".format(100F),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )

        if (progress == 1F) {
            paint.color = colors.getOrElse(0) { generateRandomColor() }
            canvas.drawArc(oval, startAngle, 1F, false, paint)
        }
    }


    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 1500L
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}