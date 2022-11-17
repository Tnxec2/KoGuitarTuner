package com.kontranik.koguitartuner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import kotlin.math.round

class TunerView : View {

    private var isPainting = false

    private val halfWidthRange = 50

    private val pointHeight = 4F
    private val pointWidth = 2F

    private var bounds = Rect()
    private var textY = 45F
    private var graphStartY = 47F

    private val frequencyGraph = mutableListOf<Float?>()

    private var tuneNote: Note = Note("E", 2)

    private var backColor = ContextCompat.getColor(context, R.color.black)
    private var lineColor = ContextCompat.getColor(context, R.color.lines)
    private var graphColor = ContextCompat.getColor(context, R.color.graph)

    private val backPaint = Paint().apply {
        color = backColor
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = lineColor
        style = Paint.Style.FILL
        strokeWidth = 1F
    }

    private val graphPaint = Paint().apply {
        color = graphColor
        style = Paint.Style.STROKE
        strokeWidth = 1F
    }

    private val textPaint = Paint().apply {
        color = graphColor
        style = Paint.Style.FILL
        isAntiAlias = true
        strokeWidth = 1F
        textSize = 16 * resources.displayMetrics.density
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.TunerView, defStyle, 0
        )

/*        frequencyGraph.add(50F)
        frequencyGraph.add(50F)
        frequencyGraph.add(50F)
        frequencyGraph.add(50F)
        frequencyGraph.add(55F)
        frequencyGraph.add(55F)
        frequencyGraph.add(55F)
        frequencyGraph.add(60F)
        frequencyGraph.add(60F)
        frequencyGraph.add(60F)
        frequencyGraph.add(65F)
        frequencyGraph.add(65F)
        frequencyGraph.add(65F)
        frequencyGraph.add(70F)
        frequencyGraph.add(70F)
        frequencyGraph.add(70F)
        frequencyGraph.add(75F)
        frequencyGraph.add(75F)
        frequencyGraph.add(75F)
        frequencyGraph.add(80F)
        frequencyGraph.add(80F)
        frequencyGraph.add(80F)
        frequencyGraph.add(80F)
        frequencyGraph.add(82F)
        frequencyGraph.add(82F)
        frequencyGraph.add(82F)
        frequencyGraph.add(85F)
        frequencyGraph.add(85F)
        frequencyGraph.add(85F)
        frequencyGraph.add(85F)
        frequencyGraph.add(90F)
        frequencyGraph.add(90F)
        frequencyGraph.add(90F)
        frequencyGraph.add(90F)
        frequencyGraph.add(95F)
        frequencyGraph.add(95F)
        frequencyGraph.add(95F)
        frequencyGraph.add(100F)
        frequencyGraph.add(100F)
        frequencyGraph.add(100F)
        frequencyGraph.add(100F)
        frequencyGraph.add(105F)
        frequencyGraph.add(105F)
        frequencyGraph.add(105F)
        frequencyGraph.add(105F)
        frequencyGraph.add(110F)
        frequencyGraph.add(110F)
        frequencyGraph.add(110F)
        frequencyGraph.add(115F)
        frequencyGraph.add(115F)
        frequencyGraph.add(115F)
        frequencyGraph.add(120F)
        frequencyGraph.add(120F)
        frequencyGraph.add(120F)
        frequencyGraph.add(125F)
        frequencyGraph.add(125F)
        frequencyGraph.add(125F)
        frequencyGraph.add(125F)*/

        a.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        isPainting = true

        val floatWidth = width.toFloat()
        val floatHeight = height.toFloat()
        val halfWidth = floatWidth / 2

        canvas.drawPaint(backPaint)

        canvas.drawLine( halfWidth, 0F, halfWidth, floatHeight, linePaint)

        var mText = String.format("%.0f", tuneNote.frequency)
        //var textWidth = textPaint.measureText(mText)

        textPaint.getTextBounds(mText, 0, mText.length, bounds)
        var textWidth = bounds.width().toFloat()
        textY = bounds.height().toFloat()
        graphStartY = textY + 2
        canvas.drawText( mText, halfWidth - textWidth/2, textY, textPaint)

        if (frequencyGraph.isNotEmpty()) {
            try {
                val lastFreq = frequencyGraph.last { it != null }
                if ( lastFreq != null) {
                    mText = String.format("%.1f", lastFreq)
                    if (lastFreq > tuneNote.frequency) {
                        textWidth = textPaint.measureText(mText)
                        canvas.drawText(mText, width - 10F - textWidth, textY, textPaint)
                    } else if (lastFreq < tuneNote.frequency) {
                        canvas.drawText(mText, 10F, textY, textPaint)
                    }
                }
            } catch (_: Exception) {

            }
        }

        if ( frequencyGraph.isNotEmpty()) {
            val scaleX = halfWidth / ( halfWidthRange / pointWidth )

            var pointX: Float
            var pointY = graphStartY
            var red = 255
            var green = 255


            val redWidthRangeLeft = floatWidth / 4
            val yellowRangeLeft = halfWidth - floatWidth / 8
            val yellowRangeRight = halfWidth + floatWidth / 8
            val redWidthRangeRight = width - floatWidth / 4
            val colorScaleRed = 255 / (floatWidth / 4)
            val colorScaleGreen = 255 / (floatWidth / 8)

            for (freq in frequencyGraph.asReversed()) {
                if (freq != null) {
                    pointX = when {
                        freq < tuneNote.frequency -> {
                            halfWidth - (tuneNote.frequency - freq) * scaleX
                        }
                        freq > tuneNote.frequency -> {
                            halfWidth + (freq - tuneNote.frequency) * scaleX
                        }
                        else -> {
                            halfWidth
                        }
                    }

                    pointX = MathUtils.clamp(pointX, pointWidth, width.toFloat() - pointWidth)
                    pointY = MathUtils.clamp(pointY, graphStartY, floatHeight - pointHeight)

                    if ( pointX <= redWidthRangeLeft) {
                        red = 255
                        green = round(pointX * colorScaleRed).toInt()
                    } else if ( pointX <= yellowRangeLeft ) {
                        green = 255
                        red = 255
                    } else if ( pointX <= halfWidth ) {
                        green = 255
                        red = round( (halfWidth - pointX) * colorScaleGreen ).toInt()
                    } else if ( pointX <= yellowRangeRight) {
                        green = 255
                        red = 255 - round( (yellowRangeRight - pointX) * colorScaleGreen ).toInt()
                    } else if ( pointX <= redWidthRangeRight ){
                        green = 255
                        red = 255
                    } else {
                        red = 255
                        green = round((redWidthRangeRight - pointX) * colorScaleRed).toInt()
                    }

                    graphPaint.color = Color.rgb(red, green, 0)

                    canvas.drawRect(pointX - pointWidth/2, pointY, pointX + pointWidth / 2, pointY + pointHeight, graphPaint)
                }
                pointY += pointHeight
            }

        }

        isPainting = false
    }

    fun setNote(checkedNote: Note) {
        tuneNote = checkedNote
        frequencyGraph.clear()
        invalidate()
    }

    fun addFrequency(freq: Float?) {
        if (freq == null || freq < AudioHelper.MIN_FREQUENCY || freq > AudioHelper.MAX_FREQUENCY) {
            frequencyGraph.add(null)
        } else {
            val roundedFreq = round(freq * 100f) / 100f
            frequencyGraph.add(roundedFreq)
        }
        if ( frequencyGraph.size > (height-graphStartY) / pointHeight) {
            frequencyGraph.removeFirst()
        }
        if (!isPainting) invalidate()
    }

}