package com.example.if570_lab_uts_hosea_00000070462.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.model.Story

class StoryAdapter(val stories: MutableList<Story>, val listener: StoryInteractionListener) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    interface StoryInteractionListener {
        fun onLikeClicked(story: Story)
//        fun onSaveClicked(story: Story)
        fun onPinClicked(story: Story)
    }

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivStoryImage: ImageView = itemView.findViewById(R.id.ivStoryImage)
        private val tvStoryText: TextView = itemView.findViewById(R.id.tvStoryText)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
//        private val btnSave: ImageButton = itemView.findViewById(R.id.btnSave)
        private val btnPin: ImageButton = itemView.findViewById(R.id.btnPin)

        private var lastClickTime: Long = 0

        fun bind(story: Story) {
            tvStoryText.text = story.text
            tvLikeCount.text = story.likes.toString()

            if (story.imageUrl != null) {
                ivStoryImage.visibility = View.VISIBLE
                Glide.with(itemView.context).load(story.imageUrl).into(ivStoryImage)
            } else {
                ivStoryImage.visibility = View.GONE
            }

            updateLikeButton(story.isLiked)
//            btnSave.setImageResource(if (story.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark)
            btnPin.setImageResource(if (story.isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin)

            itemView.setOnClickListener {
                val clickTime = System.currentTimeMillis()
                if (clickTime - lastClickTime < 300) {  // Double-tap detected
                    listener.onLikeClicked(story)
                }
                lastClickTime = clickTime
            }

            btnLike.setOnClickListener { listener.onLikeClicked(story) }
//            btnSave.setOnClickListener { listener.onSaveClicked(story) }
            btnPin.setOnClickListener { listener.onPinClicked(story) }
        }

        private fun updateLikeButton(isLiked: Boolean) {
            btnLike.setImageResource(if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    fun updateStories(newStories: MutableList<Story>) {
        stories.clear()
        stories.addAll(newStories)
        notifyDataSetChanged()
    }

    fun updateStory(updatedStory: Story) {
        val position = stories.indexOfFirst { it.id == updatedStory.id }
        if (position != -1) {
            stories[position] = updatedStory
            notifyItemChanged(position)
        }
    }

}