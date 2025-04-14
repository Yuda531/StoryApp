package com.dicoding.aplikasistoryapp.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.aplikasistoryapp.R
import com.dicoding.aplikasistoryapp.data.remote.response.ListStoryItem
import com.dicoding.aplikasistoryapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var storyItem: ListStoryItem
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.detail_page_title)
            setDisplayHomeAsUpEnabled(true)
        }

        storyItem = intent.getParcelableExtra(EXTRA_STORY_ITEM) ?: throw IllegalArgumentException("Story item is missing")
        setData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setData() {
        Glide.with(this)
            .load(storyItem.photoUrl)
            .fitCenter()
            .into(binding.ivDetailPhoto)

        binding.apply {
            tvDetailName.text = storyItem.name
            tvDetailDescription.text = storyItem.description
        }
    }

    companion object {
        const val EXTRA_STORY_ITEM = "storyItem"
    }
}
