package com.tyehooney.myvideos.domain.usecase.get_videos

import com.tyehooney.myvideos.common.Constants.UNEXPECTED_ERROR_MSG
import com.tyehooney.myvideos.common.Resource
import com.tyehooney.myvideos.domain.model.Video
import com.tyehooney.myvideos.domain.repository.MyVideosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val repository: MyVideosRepository
) {
    operator fun invoke(): Flow<Resource<List<Video>>> = flow {
        try {
            emit(Resource.Loading())
            val videos = repository.getMyVideos()
            emit(Resource.Success(videos))
        } catch (e: IOException) {
            emit(Resource.Error(UNEXPECTED_ERROR_MSG))
        }
    }
}