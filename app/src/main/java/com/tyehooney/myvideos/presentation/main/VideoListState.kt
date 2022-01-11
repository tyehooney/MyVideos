package com.tyehooney.myvideos.presentation.main

import com.tyehooney.myvideos.domain.model.Video

data class VideoListState(
    val isLoading: Boolean = false,
    val videos: List<Video> = emptyList(),
    val error: String = ""
)