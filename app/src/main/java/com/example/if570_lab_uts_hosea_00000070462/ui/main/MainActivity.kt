package com.example.if570_lab_uts_hosea_00000070462.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.model.Story
import com.example.if570_lab_uts_hosea_00000070462.data.repository.StoryRepository
import com.example.if570_lab_uts_hosea_00000070462.ui.adapter.StoryAdapter
import com.example.if570_lab_uts_hosea_00000070462.ui.story.PostStoryActivity

class MainActivity : AppCompatActivity(), StoryAdapter.StoryInteractionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private val storyRepository = StoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list
        storyAdapter = StoryAdapter(mutableListOf(), this)
        recyclerView.adapter = storyAdapter

        loadStories()

        val btnPostStory = findViewById<Button>(R.id.btnPostStory)
        btnPostStory.setOnClickListener {
            val intent = Intent(this, PostStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadStories() {
        storyRepository.getStories { stories ->
            // Update the adapter's data
            storyAdapter.updateStories(stories.toMutableList())
        }
    }

    override fun onLikeClicked(story: Story) {
        storyRepository.likeStory(story.id) { success ->
            if (success) {
                story.isLiked = !story.isLiked
                story.likes = if (story.isLiked) story.likes + 1 else story.likes - 1
                storyAdapter.updateStory(story)
            } else {
                Log.e("MainActivity", "Failed to like story")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPinClicked(story: Story) {
        storyRepository.pinStory(story.id) { success ->
            if (success) {
                story.isPinned = !story.isPinned
                // Re-sort the list
                storyAdapter.stories.sortWith(
                    compareByDescending<Story> { it.isPinned }
                        .thenByDescending { it.timestamp?.seconds ?: 0 }
                )
                storyAdapter.notifyDataSetChanged()
            } else {
                Log.e("MainActivity", "Failed to pin story")
            }
        }
    }


    override fun onSaveClicked(story: Story) {
        story.isSaved = !story.isSaved
        storyAdapter.updateStory(story)
    }

    override fun onResume() {
        super.onResume()
        loadStories()
    }
}
