package com.tyehooney.myvideos.presentation.main.video_player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyehooney.myvideos.common.Constants.UNEXPECTED_ERROR_MSG
import com.tyehooney.myvideos.common.Resource
import com.tyehooney.myvideos.domain.usecase.get_video.GetVideoUseCase
import com.tyehooney.myvideos.presentation.main.video_player.VideoPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val getVideoUseCase: GetVideoUseCase
) : ViewModel() {

    private val _videoPlayerResult = MutableStateFlow(VideoPlayerState())
    val videoPlayerResult: StateFlow<VideoPlayerState> = _videoPlayerResult

    fun setVideo(title: String, createdAt: Long, strData: String) {
        getVideoUseCase.invoke(title, createdAt, strData).onEach { result ->
            _videoPlayerResult.value = when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        VideoPlayerState(video = result.data)
                    } else {
                        VideoPlayerState(error = result.message ?: UNEXPECTED_ERROR_MSG)
                    }
                }
                is Resource.Error -> {
                    VideoPlayerState(error = result.message ?: UNEXPECTED_ERROR_MSG)
                }
                is Resource.Loading -> {
                    VideoPlayerState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}