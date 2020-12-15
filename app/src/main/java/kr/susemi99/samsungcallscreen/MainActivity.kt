package kr.susemi99.samsungcallscreen

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import kr.susemi99.samsungcallscreen.databinding.MainActivityBinding
import kotlin.math.hypot

class MainActivity : AppCompatActivity() {
  private lateinit var binding: MainActivityBinding
  private var dragAreaAnimationCompleted = false
  private var downPosition = Point()
  private var isOutOfDragArea = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = MainActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.startButton.setOnClickListener { startAnimation() }
    binding.endButton.setOnClickListener { stopAnimation() }

    binding.acceptButton.setOnTouchListener(acceptButtonTouchListener)
    binding.rejectButton.setOnTouchListener(rejectButtonTouchListener)
  }

  private fun startAnimation() {
    binding.motionLayout.setTransition(R.id.defaultTransition)
    binding.motionLayout.setTransitionListener(null)
    binding.motionLayout.transitionToEnd()
    isOutOfDragArea = false
    dragAreaAnimationCompleted = false
  }

  private fun stopAnimation() {
    binding.motionLayout.progress = 0f
  }

  private fun onButtonTouch(event: MotionEvent, dragArea: ImageView, @IdRes transition: Int, onOutOfArea: () -> Unit) {
    if (isOutOfDragArea) {
      return
    }

    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        downPosition = Point(event.x.toInt(), event.y.toInt())
        transparentCenter(dragArea, 0f)
        dragAreaAnimationCompleted = false
        binding.motionLayout.setTransition(transition)
        binding.motionLayout.transitionToEnd()
        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
          override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
          override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
          override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            dragAreaAnimationCompleted = true
          }

          override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })
      }

      MotionEvent.ACTION_UP -> startAnimation()

      MotionEvent.ACTION_MOVE -> {
        if (!dragAreaAnimationCompleted) return
        if (isOutOfDragArea) return

        val radius = hypot(downPosition.x.toDouble() - event.x, downPosition.y.toDouble() - event.y)
        if ((dragArea.width / 2) <= radius) {
          isOutOfDragArea = true
          onOutOfArea()
        } else {
          transparentCenter(dragArea, radius.toFloat())
        }
      }
    }
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
    stopAnimation()
  }

  private fun rejectCall() {
    Toast.makeText(this, "reject call", Toast.LENGTH_SHORT).show()
    stopAnimation()
  }

  /**************************************************************************
   * listener
   **************************************************************************/
  @SuppressLint("ClickableViewAccessibility")
  private val acceptButtonTouchListener = View.OnTouchListener { _, event ->
    onButtonTouch(event, binding.acceptDragArea, R.id.acceptPressTransition) { acceptCall() }
    return@OnTouchListener true
  }

  @SuppressLint("ClickableViewAccessibility")
  private val rejectButtonTouchListener = View.OnTouchListener { _, event ->
    onButtonTouch(event, binding.rejectDragArea, R.id.rejectPressTransition) { rejectCall() }
    return@OnTouchListener true
  }
}