package com.example.glassfold

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

class GlassBackgroundDrawable(
  private val backgroundAlpha: Int,
  private val cornerRadiusPx: Float
) : Drawable() {

  private val baseStrokeAlpha: Int
  private val baseHighlightAlpha: Int

  private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = Color.argb(backgroundAlpha, 20, 20, 20) // dark glass base
  }

  private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 2f
    color = Color.argb(90, 255, 255, 255)
  }

  private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 2f * Resources.getSystem().displayMetrics.density
    color = Color.argb(45, 255, 255, 255)
  }

  init {
    baseStrokeAlpha = strokePaint.alpha
    baseHighlightAlpha = highlightPaint.alpha
  }

  private val rect = RectF()

  override fun draw(canvas: Canvas) {
    rect.set(bounds)
    val inset = strokePaint.strokeWidth / 2f
    rect.inset(inset, inset)
    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)
    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint)

    canvas.drawLine(
      rect.left + cornerRadiusPx,
      rect.top + 2f,
      rect.right - cornerRadiusPx,
      rect.top + 2f,
      highlightPaint
    )
  }

  override fun setAlpha(alpha: Int) {
    fillPaint.alpha = (alpha * backgroundAlpha) / 255
    strokePaint.alpha = (alpha * baseStrokeAlpha) / 255
    highlightPaint.alpha = (alpha * baseHighlightAlpha) / 255
    invalidateSelf()
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    fillPaint.colorFilter = colorFilter
    strokePaint.colorFilter = colorFilter
    invalidateSelf()
  }

  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
