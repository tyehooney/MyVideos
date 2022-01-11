package com.tyehooney.myvideos.presentation.main.video_player

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.tyehooney.myvideos.R
import com.tyehooney.myvideos.common.Constants.KEY_CREATED_AT
import com.tyehooney.myvideos.common.Constants.KEY_TITLE
import com.tyehooney.myvideos.common.Constants.KEY_VIDEO
import com.tyehooney.myvideos.common.util.videoDateFormat
import com.tyehooney.myvideos.databinding.FragmentVideoPlayerBinding
import com.tyehooney.myvideos.presentation.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideoPlayerFragment :
    BaseFragment<FragmentVideoPlayerBinding>(R.layout.fragment_video_player) {

    private val viewModel: VideoPlayerViewModel by viewModels()
    private var videoPlayer: ExoPlayer? = null
    private var _isExpanded = true
    val isExpanded get() = _isExpanded
    private var isPlaying = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.dateFormat = videoDateFormat
        initMotionLayout()
        initButtons()
        fetchVideo()
        observeStates()
    }

    private fun initMotionLayout() {
        binding.mlPlayerScreen.setTransitionListener(object : MotionLayout.TransitionListener {
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
                binding.spvVideo.useController = currentId == R.id.start
                _isExpanded = currentId == R.id.start
                binding.ivBtnPlayPause.isClickable = currentId == R.id.end
                binding.ivBtnClose.isClickable = currentId == R.id.end
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}
        })
    }

    private fun fetchVideo() {
        videoPlayer?.release()

        arguments?.let {
            viewModel.setVideo(
                it.getString(KEY_TITLE, ""),
                it.getLong(KEY_CREATED_AT),
                it.getString(KEY_VIDEO, "")
            )
        }

        binding.mlPlayerScreen.transitionToStart()
        binding.spvVideo.useController = true
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videoPlayerResult.collect { state ->
                    if (state.error.isNotEmpty()) showToast(state.error)
                    setVideo(state.video?.data)
                }
            }
        }
    }

    private fun setVideo(data: Uri?) {
        if (data == null) return

        setPlayer(data)
        binding.spvVideo.player = videoPlayer
    }

    private fun initButtons() {
        with(binding) {
            ivBtnPlayPause.setOnClickListener {
                if (isPlaying) videoPlayer?.pause()
                else {
                    if (videoPlayer?.playbackState == ExoPlayer.STATE_ENDED) {
                        videoPlayer?.seekTo(0)
                    }
                    videoPlayer?.play()
                }
            }

            ivBtnClose.setOnClickListener {
                parentFragmentManager.popBackStack(KEY_VIDEO, POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    private fun setPlayer(data: Uri) {
        context?.let {
            videoPlayer = ExoPlayer.Builder(it).build()
            val dataSourceFactory = DefaultDataSource.Factory(it)
            val mediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(data))
            videoPlayer?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    binding.ivBtnPlayPause.setImageDrawable(
                        if (isPlaying) ContextCompat.getDrawable(it, R.drawable.ic_pause)
                        else ContextCompat.getDrawable(it, R.drawable.ic_play)
                    )
                    this@VideoPlayerFragment.isPlaying = isPlaying
                }
            })
            videoPlayer?.setMediaSource(mediaSource)
            videoPlayer?.prepare()
            videoPlayer?.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        videoPlayer?.pause()
    }

    override fun onDestroyView() {
        videoPlayer?.release()
        super.onDestroyView()
    }

    override fun onDetach() {
        instance = null
        super.onDetach()
    }

    fun onBackPressed() {
        binding.mlPlayerScreen.transitionToEnd()
    }

    companion object {
        private var instance: VideoPlayerFragment? = null

        fun newInstance(bundle: Bundle): VideoPlayerFragment {
            instance?.let {
                return it.apply {
                    arguments = bundle
                    fetchVideo()
                }
            }

            instance = VideoPlayerFragment().apply {
                arguments = bundle
            }

            return instance!!
        }
    }
}