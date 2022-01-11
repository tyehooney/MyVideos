package com.tyehooney.myvideos.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyehooney.myvideos.common.Constants.UNEXPECTED_ERROR_MSG
import com.tyehooney.myvideos.common.Resource
import com.tyehooney.myvideos.domain.usecase.get_videos.GetVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _videoListResult = MutableStateFlow(VideoListState())
    val videoListResult = _videoListResult.asStateFlow()

    private var permissionGranted = false

    fun getMyVideos() {

        if (permissionGranted) return

        getVideosUseCase().onEach { result ->
            _videoListResult.value = when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        VideoListState(videos = result.data)
                    } else {
                        VideoListState(error = result.message ?: UNEXPECTED_ERROR_MSG)
                    }
                }
                is Resource.Error -> {
                    VideoListState(error = result.message ?: UNEXPECTED_ERROR_MSG)
                }
                is Resource.Loading -> {
                    VideoListState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)

        permissionGranted = true
    }
}