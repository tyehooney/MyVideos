package com.tyehooney.myvideos.domain.repository

import com.tyehooney.myvideos.domain.model.Video

interface MyVideosRepository {
    suspend fun getMyVideos(): List<Video>
}