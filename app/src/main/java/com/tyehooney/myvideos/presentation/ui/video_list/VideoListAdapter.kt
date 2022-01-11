package com.tyehooney.myvideos.presentation.ui.video_list

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyehooney.myvideos.R
import com.tyehooney.myvideos.common.Constants.KEY_CREATED_AT
import com.tyehooney.myvideos.common.Constants.KEY_TITLE
import com.tyehooney.myvideos.common.Constants.KEY_VIDEO
import com.tyehooney.myvideos.common.util.binding
import com.tyehooney.myvideos.databinding.ItemVideoListBinding
import com.tyehooney.myvideos.domain.model.Video

class VideoListAdapter(
    private val onItemClick: (Bundle) -> (Unit)
) : ListAdapter<Video, VideoListViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoListViewHolder {
        return VideoListViewHolder(parent.binding(R.layout.item_video_list), onItemClick)
    }

    override fun onBindViewHolder(holder: VideoListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean {
                return oldItem.data == newItem.data
            }

            override fun areContentsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class VideoListViewHolder(
    private val binding: ItemVideoListBinding,
    private val itemClick: (Bundle) -> (Unit)
) : RecyclerView.ViewHolder(binding.root) {

    private var _item: Video? = null
    val item: Video? get() = _item

    init {
        itemView.setOnClickListener {
            if (_item == null) return@setOnClickListener

            itemClick(
                bundleOf(
                    KEY_TITLE to _item?.title,
                    KEY_CREATED_AT to _item?.createdAt,
                    KEY_VIDEO to _item?.data.toString()
                )
            )
        }
    }

    fun bind(video: Video) {
        _item = video
        itemView.tag = this

        with(binding) {
            viewModel = video
            currentTime = System.currentTimeMillis()
            arflVideoItem.setAspectRatio(16f / 9f)
            ivVideoItemThumbnail.setImageBitmap(video.img)
        }
    }
}