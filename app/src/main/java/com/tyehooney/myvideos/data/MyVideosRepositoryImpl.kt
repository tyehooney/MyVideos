package com.tyehooney.myvideos.data

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.tyehooney.myvideos.domain.model.Video
import com.tyehooney.myvideos.domain.repository.MyVideosRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MyVideosRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MyVideosRepository {

    @SuppressLint("Range")
    override suspend fun getMyVideos(): List<Video> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<Video>()

        val contentResolver = context.contentResolver
        val columns = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media._ID
        )
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC"
        )

        with(cursor) {
            if (this != null && moveToFirst()) {
                do {
                    val title = getString(getColumnIndex(MediaStore.Video.Media.TITLE))
                    val createdAt = getLong(getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))
                    val id = getLong(getColumnIndex(MediaStore.Video.Media._ID))
                    val data = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val thumbnailImg =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentResolver.loadThumbnail(data, Size(1600, 900), null)
                        } else {
                            MediaStore.Video.Thumbnails.getThumbnail(
                                contentResolver,
                                id,
                                MediaStore.Video.Thumbnails.MINI_KIND,
                                null
                            )
                        }

                    videoList.add(Video(title, createdAt, thumbnailImg, data))
                } while (moveToNext())
            }
        }

        return@withContext videoList
    }
}