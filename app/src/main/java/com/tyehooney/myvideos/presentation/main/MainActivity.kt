package com.tyehooney.myvideos.presentation.main

import android.Manifest
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.tyehooney.myvideos.R
import com.tyehooney.myvideos.common.Constants.KEY_VIDEO
import com.tyehooney.myvideos.databinding.ActivityMainBinding
import com.tyehooney.myvideos.domain.model.Video
import com.tyehooney.myvideos.presentation.BaseActivity
import com.tyehooney.myvideos.presentation.ui.video_list.VideoListAdapter
import com.tyehooney.myvideos.presentation.main.video_player.VideoPlayerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
    private val viewModel: MainViewModel by viewModels()
    private val videoListAdapter by lazy {
        VideoListAdapter {
            binding.rvVideoList.stopPlayer()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_video_container, VideoPlayerFragment.newInstance(it))
                .addToBackStack(KEY_VIDEO)
                .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        checkPermission()
        observeStates()

        supportFragmentManager.addOnBackStackChangedListener {
            binding.rvVideoList.isPlayable = supportFragmentManager.fragments.isEmpty()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.isEmpty()) {
            super.onBackPressed()
            return
        }

        val fragment = supportFragmentManager.fragments[0] as VideoPlayerFragment
        if (fragment.isExpanded) {
            fragment.onBackPressed()
        } else {
            supportFragmentManager.clearBackStack(KEY_VIDEO)
            super.onBackPressed()
        }
    }

    private fun checkPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    viewModel.getMyVideos()
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                    finish()
                }
            })
            .setRationaleMessage(R.string.need_permission)
            .setDeniedMessage(R.string.permission_denied)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videoListResult.collect { state ->
                    if (state.error.isNotEmpty()) showToast(state.error)
                    setVideoListToRecyclerView(state.videos)
                }
            }
        }
    }

    private fun setVideoListToRecyclerView(videoItems: List<Video>) {
        with(binding.rvVideoList) {
            if (adapter == null) {
                adapter = videoListAdapter
            }

            videoListAdapter.submitList(videoItems)
        }
    }
}