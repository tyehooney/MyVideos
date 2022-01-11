package com.tyehooney.myvideos.presentation.main.video_player

import com.tyehooney.myvideos.domain.model.Video

data class VideoPlayerState(
    val isLoading: Boolean = false,
    val video: Video? = null,
    val error: String = ""
)
