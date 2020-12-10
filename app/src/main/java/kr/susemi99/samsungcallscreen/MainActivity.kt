package kr.susemi99.samsungcallscreen

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.susemi99.samsungcallscreen.databinding.MainActivityBinding
import kotlin.math.hypot


class MainActivity : AppCompatActivity() {
  private lateinit var binding: MainActivityBinding
  private var isTouching = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = MainActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.startButton.setOnClickListener { startAnimation() }
    binding.endButton.setOnClickListener { stopAnimation() }

    binding.acceptButton.setOnTouchListener(acceptButtonTouchListener)
    binding.rejectButton.setOnTouchListener(rejectButtonTouchListener)

    binding.acceptDragArea.setOnTouchListener(acceptDragButtonTouchListener)
    binding.rejectDragArea.setOnTouchListener(rejectDragButtonTouchListener)
  }

  private fun startAnimation() {
    binding.motionLayout.transitionToEnd()
  }

  private fun stopAnimation() {
    binding.motionLayout.progress = 0f
  }

  private fun onButtonTouch(event: MotionEvent, dragArea: ImageView): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        dragArea.visibility = View.VISIBLE
        stopAnimation()
      }

      MotionEvent.ACTION_UP -> {
        dragArea.visibility = View.INVISIBLE
        startAnimation()
      }
    }

    return false
  }


  private fun onDragAreaTouch(view: View, event: MotionEvent, dragArea: ImageView, onOutOfArea: () -> Unit): Boolean {
    if (view.visibility != View.VISIBLE) {
      return true
    }

    val center = Point(view.width / 2, view.height / 2)
    if (hypot(center.x.toDouble() - event.x, center.y.toDouble() - event.y) > 150 && !isTouching) {
      return true
    }

    when (event.action) {
      MotionEvent.ACTION_DOWN -> isTouching = true
      MotionEvent.ACTION_UP -> {
        isTouching = false
        transparentCenter(dragArea, 0f)
        view.visibility = View.INVISIBLE
        startAnimation()
      }
      MotionEvent.ACTION_MOVE -> {
        val radius = hypot(center.x.toDouble() - event.x, center.y.toDouble() - event.y)
        if ((view.width / 2) <= radius) {
          view.visibility = View.INVISIBLE
          onOutOfArea()
          return true
        }

        transparentCenter(dragArea, radius.toFloat())
      }
    }

    return true
  }

  private fun transparentCenter(imageView: ImageView, radius: Float) {
    imageView.setImageResource(R.drawable.bg_btn_call_drag)

    if (radius == 0f) {
      return
    }

    val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)

    val paint = Paint().apply {
      isAntiAlias = true
      color = Color.TRANSPARENT
      xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    val drawable = imageView.drawable
    val halfWidth = imageView.width / 2
    val halfHeight = imageView.height / 2

    Canvas(bitmap).apply {
      drawable.setBounds(0, 0, width, height)
      drawable.draw(this)
      drawCircle(halfWidth.toFloat(), halfHeight.toFloat(), radius, paint)
    }

    imageView.setImageBitmap(bitmap)
  }

  private fun acceptCall() {
    Toast.makeText(this, "accept call", Toast.LENGTH_SHORT).show()
  }

  private fun rejectCall() {
    Toast.makeText(this, "reject call", Toast.LENGTH_SHORT).show()
  }

  /**************************************************************************
   * listener
   **************************************************************************/
  @SuppressLint("ClickableViewAccessibility")
  private val acceptButtonTouchListener = View.OnTouchListener { _, event ->
    return@OnTouchListener onButtonTouch(event, binding.acceptDragArea)
  }

  @SuppressLint("ClickableViewAccessibility")
  private val rejectButtonTouchListener = View.OnTouchListener { _, event ->
    return@OnTouchListener onButtonTouch(event, binding.rejectDragArea)
  }

  @SuppressLint("ClickableViewAccessibility")
  private val acceptDragButtonTouchListener = View.OnTouchListener { view, event ->
    return@OnTouchListener onDragAreaTouch(view, event, binding.acceptDragArea) { acceptCall() }
  }

  @SuppressLint("ClickableViewAccessibility")
  private val rejectDragButtonTouchListener = View.OnTouchListener { view, event ->
    return@OnTouchListener onDragAreaTouch(view, event, binding.rejectDragArea) { rejectCall() }
  }
}