package com.example.if570_lab_uts_hosea_00000070462.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.model.Story
import com.example.if570_lab_uts_hosea_00000070462.data.repository.StoryRepository
import com.example.if570_lab_uts_hosea_00000070462.ui.adapter.StoryAdapter
import com.example.if570_lab_uts_hosea_00000070462.ui.story.PostStoryActivity

class HomeFragment : Fragment(), StoryAdapter.StoryInteractionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private val storyRepository = StoryRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the adapter with an empty list
        storyAdapter = StoryAdapter(mutableListOf(), this)
        recyclerView.adapter = storyAdapter

        loadStories()



        return view
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
                Log.e("HomeFragment", "Failed to like story")
            }
        }
    }

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
                Log.e("HomeFragment", "Failed to pin story")
            }
        }
    }

//    override fun onSaveClicked(story: Story) {
//        story.isSaved = !story.isSaved
//        storyAdapter.updateStory(story)
//    }

    override fun onResume() {
        super.onResume()
        loadStories()
    }
}
