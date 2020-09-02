package com.nogotech.gameoflife.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.get
import com.nogotech.gameoflife.R
import kotlin.math.abs

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val deadColor = ResourcesCompat.getColor(resources, R.color.colorDead, null)
    private val liveColor = ResourcesCompat.getColor(resources, R.color.colorLive, null)

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = liveColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        //strokeJoin = Paint.Join.ROUND // default: MITER
        //strokeCap = Paint.Cap.ROUND // default: BUTT
        //strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private val cellRectRight = resources.getDimension(R.dimen.cellRight)
    private val cellRectBottom = resources.getDimension(R.dimen.cellBottom)
    private val cellRectTop = resources.getDimension(R.dimen.cellTop)
    private val cellRectLeft = resources.getDimension(R.dimen.cellLeft)

    private val rectStart = resources.getDimension(R.dimen.rectStart)
    private val cellWidth = resources.getDimension(R.dimen.cellAndInset)
    private val rectInset = resources.getDimension(R.dimen.rectInset)
    private val smallRectOffset = resources.getDimension(R.dimen.smallRectOffset)


//    private val columnOne = rectInset
//    private val columnTwo = columnOne + rectInset + clipRectRight
//    private val rowOne = rectInset
//    private val rowTwo = rowOne + rectInset + clipRectBottom
//    private val rowThree = rowTwo + rectInset + clipRectBottom
//    private val rowFour = rowThree + rectInset + clipRectBottom
//    private val textRow = rowFour + (1.5f * clipRectBottom)

//    init{
//        isClickable = true
//    }
//
//    override fun performClick(): Boolean {
//        if(super.performClick()) return true
//        //TODO
//        invalidate()
//        return true
//    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(deadColor)
        Log.d("width", width.toString())
        Log.d("height", height.toString())
        Log.d("Bitmap", extraBitmap[0, 0].toString())
        Log.d("cellAndInset", cellWidth.toString())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    private fun drawCellRectangle(canvas: Canvas, cellValue:Int) {
        canvas.clipRect(
            cellRectLeft,cellRectTop,
            cellRectRight,cellRectBottom
        )
        if(cellValue == -1)
        canvas.drawColor(Color.BLACK)
        else canvas.drawColor(Color.WHITE)
    }

    private fun drawBackCellRectangle(canvas: Canvas, cellValue:Int) {
        canvas.drawColor(Color.BLACK)
        canvas.save()
        canvas.translate(rectStart, rectStart)
        drawCellRectangle(canvas, cellValue)
        canvas.restore()
    }

    private fun touchStart() {
        //path.reset()
        //path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY

        var w = (currentX - (currentX % cellWidth)+1).toInt()
        var h = (currentY- (currentY % cellWidth)+1).toInt()

        Log.d("Bitmap11", extraBitmap[w, h].toString())

        //extraCanvas.drawColor(Color.BLACK)
        extraCanvas.save()
        extraCanvas.translate(currentX - (currentX % cellWidth), currentY- (currentY % cellWidth))
        drawCellRectangle(extraCanvas, extraBitmap[w, h])
        extraCanvas.restore()

        Log.d("width", (currentX - (currentX % cellWidth)).toString())
        Log.d("height", (currentY- (currentY % cellWidth)).toString())
        w = (currentX - (currentX % cellWidth)+1).toInt()
        h = (currentY- (currentY % cellWidth)+1).toInt()

        invalidate()
        Log.d("Bitmap22", extraBitmap[w, h].toString())
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw in the extra bitmap to cache it.
            extraCanvas.save()
            extraCanvas.translate(currentX - (currentX % cellWidth), currentY- (currentY % cellWidth))

            var w = (currentX - (currentX % cellWidth)+1).toInt()
            var h = (currentY- (currentY % cellWidth)+1).toInt()

            drawCellRectangle(extraCanvas, extraBitmap[w, h])
            extraCanvas.restore()
        }
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        //path.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun neighborLiveCellCount(i: Int, j: Int){
        var count = 0
        for(k in -1..1){
            for(l in -1..1){
                if(k == 0 && l ==0) continue
                val ri = i + k * cellWidth
                val ci = j + l * cellWidth
                if(ri >= 0 && ri < extraBitmap.height &&
                    ci>= 0 && ci < extraBitmap.width){
                    count += extraBitmap[ri.toInt(), ci.toInt()]
                }
            }
        }

    }
}