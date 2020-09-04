package com.nogotech.gameoflife.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
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
    private val map = HashMap<Pair<Float, Float>, Int>()

    private var deadColor = ResourcesCompat.getColor(resources, R.color.colorDead, null)
    private var liveColor = ResourcesCompat.getColor(resources, R.color.colorLive, null)

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private val cellRectLeft = resources.getDimension(R.dimen.cellLeft)
    private val cellRectTop = resources.getDimension(R.dimen.cellTop)
    private var cellRectRight = resources.getDimension(R.dimen.cellRight)
    private var cellRectBottom = resources.getDimension(R.dimen.cellBottom)

    private var rectInset = resources.getDimension(R.dimen.rectInset)
    private var cellBlockWidth = cellRectRight + rectInset
    private var cellBlockHeight = cellRectBottom + rectInset


    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.GameView) {
            liveColor = getColor(R.styleable.GameView_liveColor, liveColor)
            deadColor = getColor(R.styleable.GameView_deadColor, deadColor)
            cellRectRight = getDimension(R.styleable.GameView_cellWidth, cellRectRight)
            cellRectBottom = getDimension(R.styleable.GameView_cellHeight, cellRectBottom)
            rectInset = getDimension(R.styleable.GameView_insetSize, rectInset)
            cellBlockWidth = cellRectRight + rectInset
            cellBlockHeight = cellRectBottom + rectInset
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(deadColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    private fun drawCellRectangle(canvas: Canvas, color: Int) {
        canvas.clipRect(
            cellRectLeft, cellRectTop,
            cellRectRight, cellRectBottom
        )
        canvas.drawColor(color)
    }

    private fun drawBackCellRectangle(canvas: Canvas, dx: Float, dy: Float, color: Int) {
        canvas.save()
        canvas.translate(dx, dy)
        drawCellRectangle(canvas, color)
        canvas.restore()
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        currentX = motionTouchEventX
        currentY = motionTouchEventY

        val dx = currentX - (currentX % cellBlockWidth)
        val dy = currentY - (currentY % cellBlockHeight)

        drawBackCellRectangle(extraCanvas, dx, dy,
            if (extraBitmap[dx.toInt() + 1, dy.toInt() + 1] == liveColor) deadColor else liveColor)

        invalidate()

        return true
    }

    private fun touchMove() {
        var dx = abs(motionTouchEventX - currentX)
        var dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw in the extra bitmap to cache it.
            dx = currentX - (currentX % cellBlockWidth)
            dy = currentY - (currentY % cellBlockHeight)

            drawBackCellRectangle(extraCanvas, dx, dy,
                if (extraBitmap[dx.toInt() + 1, dy.toInt() +1] == liveColor) deadColor else liveColor)

            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> performClick()
            MotionEvent.ACTION_MOVE -> touchMove()
        }
        return true
    }

    private fun neighborLiveCellCount(i: Float, j: Float){
        for (k in -1..1) {
            for (l in -1..1) {
                val riCanvas = i + k * cellBlockWidth
                val ciCanvas = j + l * cellBlockHeight

                val riBitmap = riCanvas.toInt() + 1
                val ciBitmap = ciCanvas.toInt() + 1

                if (riBitmap >= 0 && riBitmap < extraBitmap.width &&
                    ciBitmap >= 0 && ciBitmap < extraBitmap.height
                ) {
                    val pair = Pair(riCanvas, ciCanvas)

                    if(!map.containsKey(pair)) map[pair] = 0
                    if(k != 0 || l !=0) map[pair] = map[pair]!! + 1
                }
            }
        }
    }

    fun nextState() {
        map.clear()

        var i = 0F
        while (i < extraBitmap.width) {
            var j = 0F
            while (j < extraBitmap.height) {
                if (extraBitmap[i.toInt() + 1, j.toInt() + 1] == liveColor)
                    neighborLiveCellCount(i, j)
                j += cellBlockHeight
            }
            i += cellBlockWidth
        }

        for(cell in map){
            val (ri ,rj) = cell.key
            val count = cell.value
            if(count == 3 ||(count == 2 && (extraBitmap[ri.toInt() + 1, rj.toInt() + 1] == liveColor)))
                drawBackCellRectangle(extraCanvas, ri, rj, liveColor)
            else drawBackCellRectangle(extraCanvas, ri, rj, deadColor)
        }

        invalidate()
    }
}