package com.tyehooney.myvideos.domain.usecase.get_video

import android.net.Uri
import com.tyehooney.myvideos.common.Constants.UNEXPECTED_ERROR_MSG
import com.tyehooney.myvideos.common.Resource
import com.tyehooney.myvideos.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetVideoUseCase @Inject constructor() {

    operator fun invoke(
        title: String,
        createdAt: Long,
        strData: String
    ): Flow<Resource<Video>> = flow {
        try {
            emit(Resource.Loading())

            val video = Video(
                title = title,
                createdAt = createdAt,
                data = Uri.parse(strData)
            )
            emit(Resource.Success(video))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: UNEXPECTED_ERROR_MSG))
        }
    }
}