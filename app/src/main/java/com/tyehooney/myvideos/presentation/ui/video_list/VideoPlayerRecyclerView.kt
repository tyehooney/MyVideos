package com.tyehooney.myvideos.presentation.ui.video_list

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource

class VideoPlayerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private val videoPlayer = ExoPlayer.Builder(context).build().apply {
        volume = 0f
    }
    private val playerView = PlayerView(context).apply {
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        useController = false
        player = videoPlayer
    }
    private var screenHeight = 0
    private var videoDefaultHeight = 0
    private var playingPos = -1
    private var isPlayerViewAdded = false
    var isPlayable = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initHeights()
        setOnScrollListener()
        setVideoPlayerListener()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePlayer()
    }

    private fun initHeights() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val windowInsets = windowMetrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            val insetsHeight = insets.top + insets.bottom
            val insetWidth = insets.left + insets.right

            val bounds = windowMetrics.bounds
            screenHeight = bounds.height() - insetsHeight
            videoDefaultHeight = bounds.width() - insetWidth
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val point = Point()
            @Suppress("DEPRECATION")
            display.getSize(point)
            screenHeight = point.y
            videoDefaultHeight = point.x
        }
    }

    private fun setOnScrollListener() {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE && isPlayable) {
                    playVideo(!recyclerView.canScrollVertically(1))
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = layoutManager as LinearLayoutManager
                if (playingPos !in manager.findFirstVisibleItemPosition()..manager.findLastVisibleItemPosition()) {
                    removePlayerView(playerView)
                }
            }
        })
    }

    private fun setVideoPlayerListener() {
        videoPlayer.addListener(object: Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                when (playbackState) {
                    STATE_ENDED -> { videoPlayer.seekTo(0) }
                    STATE_READY -> {
                        if (!isPlayerViewAdded && playingPos > -1) {
                            addPlayerView(playingPos)
                        }
                    }
                    STATE_BUFFERING -> {}
                    STATE_IDLE -> {}
                }
            }
        })
    }

    private fun playVideo(isEndOfList: Boolean) {
        val adapter = adapter as VideoListAdapter
        val layoutManager = layoutManager as LinearLayoutManager
        val target: Int

        if (isEndOfList) {
            target = adapter.itemCount - 1
        } else {
            val start = layoutManager.findFirstVisibleItemPosition()
            var end = layoutManager.findLastVisibleItemPosition()

            if (end - start > 1) {
                end = start + 1
            }

            if (start < 0 || end < 0) return

            target = if (start != end) {
                val startItemHeight = getVisibleItemHeight(start)
                val endItemHeight = getVisibleItemHeight(end)

                if (startItemHeight > endItemHeight) start else end
            } else {
                start
            }
        }

        if (target == playingPos) return
        playingPos = target
        playerView.visibility = INVISIBLE
        removePlayerView(playerView)

        val currentPos = target - layoutManager.findFirstVisibleItemPosition()
        val currentItem = addPlayerView(currentPos) ?: return
        val currentItemViewModel = (currentItem.tag as VideoListViewHolder).item ?: return

        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(currentItemViewModel.data))
        with(videoPlayer) {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    private fun getVisibleItemHeight(pos: Int): Int {
        val at = pos - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val item = getChildAt(at) ?: return 0

        val location = intArrayOf(0, 0)
        item.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoDefaultHeight
        } else {
            screenHeight - location[1]
        }
    }

    private fun addPlayerView(pos: Int): View? {
        val itemView = getChildAt(pos) ?: return null

        (itemView as ViewGroup).children.find { it is AspectRatioFrameLayout }
            ?.let { frameLayout ->
                (frameLayout as ViewGroup).addView(playerView)
                isPlayerViewAdded = true
                playerView.requestFocus()
                playerView.visibility = VISIBLE
            }

        return itemView
    }

    private fun removePlayerView(view: PlayerView) {
        val parent = view.parent as ViewGroup? ?: return
        parent.removeView(view)
        isPlayerViewAdded = false
    }

    private fun resetPlayerView() {
        if (!isPlayerViewAdded) return
        removePlayerView(playerView)
        playingPos = -1
    }

    private fun releasePlayer() {
        videoPlayer.release()
    }

    fun stopPlayer() {
        videoPlayer.stop()
    }
}