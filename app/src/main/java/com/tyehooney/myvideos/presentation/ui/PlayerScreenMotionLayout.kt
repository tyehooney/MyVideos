package com.tyehooney.myvideos.presentation.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import com.tyehooney.myvideos.R

class PlayerScreenMotionLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : MotionLayout(context, attributeSet, defStyle) {

    private var motionTouchStarted = false
    private val containerView: View? by lazy { findViewById(R.id.cl_video) }
    private val touchRect = Rect()
    private val gestureDetector = GestureDetector(
        context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                event: MotionEvent,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                containerView?.getHitRect(touchRect)
                return touchRect.contains(event.x.toInt(), event.y.toInt())
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                transitionToStart()
                return false
            }
        }
    )

    init {
        setTransitionListener(object : TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                motionTouchStarted = false
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}

        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                motionTouchStarted = false
                return super.onTouchEvent(event)
            }
        }

        if (!motionTouchStarted) {
            containerView?.getHitRect(touchRect)
            motionTouchStarted = touchRect.contains(event.x.toInt(), event.y.toInt())
        }

        return super.onTouchEvent(event) && motionTouchStarted
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}