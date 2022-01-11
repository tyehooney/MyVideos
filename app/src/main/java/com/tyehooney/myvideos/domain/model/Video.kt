package com.tyehooney.myvideos.domain.model

import android.graphics.Bitmap
import android.net.Uri

data class Video(
    val title: String,
    val createdAt: Long,
    val img: Bitmap? = null,
    val data: Uri
)
