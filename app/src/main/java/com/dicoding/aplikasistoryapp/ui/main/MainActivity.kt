package com.dicoding.aplikasistoryapp.ui.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.aplikasistoryapp.R
import com.dicoding.aplikasistoryapp.databinding.ActivityMainBinding
import com.dicoding.aplikasistoryapp.utils.ViewModelFactory
import com.dicoding.aplikasistoryapp.ui.welcome.WelcomeActivity
import com.dicoding.aplikasistoryapp.ui.adapter.LoadingStateAdapter
import com.dicoding.aplikasistoryapp.ui.adapter.StoryAdapter
import com.dicoding.aplikasistoryapp.ui.detail.DetailActivity
import com.dicoding.aplikasistoryapp.ui.detail.DetailActivity.Companion.EXTRA_STORY_ITEM
import com.dicoding.aplikasistoryapp.ui.maps.MapsActivity
import com.dicoding.aplikasistoryapp.ui.upload.UploadStoryActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(true)

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                lifecycleScope.launch {
                    viewModel.getStoriesPaging(user.token).observe(this@MainActivity) {
                        storyAdapter.submitData(lifecycle, it)
                    }
                }
            }
        }

        binding.rvStoryList.layoutManager = LinearLayoutManager(this)

        storyAdapter = StoryAdapter { storyItem ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(EXTRA_STORY_ITEM, storyItem)
            startActivity(intent)
        }
        binding.rvStoryList.adapter = storyAdapter

        val loadingStateAdapter = LoadingStateAdapter { storyAdapter.retry() }
        binding.rvStoryList.adapter = storyAdapter.withLoadStateFooter(
            footer = loadingStateAdapter
        )

        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

        setupView()
        playAnimation()
        setOptionMenu()
        setAddButton()


    }

    override fun onResume() {
        super.onResume()
        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                lifecycleScope.launch {
                    viewModel.getStoriesPaging(user.token).observe(this@MainActivity) {
                        storyAdapter.submitData(lifecycle, it)
                    }
                }
            }
        }
    }

    private fun setAddButton() {
        binding.buttonAdd.setOnClickListener {
            val intent = Intent(this, UploadStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setOptionMenu() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_maps -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                R.id.menu_setting -> {
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }

                R.id.action_logout -> {
                    AlertDialog.Builder(this).apply {
                        setTitle(getString(R.string.logout_dialog_title))
                        setMessage(getString(R.string.logout_dialog_message))
                        setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModel.logout()
                        }
                        setNegativeButton(getString(R.string.no)) { _, _ ->

                        }
                        create()
                        show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.rvStoryList.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun playAnimation() {

        val addStory = ObjectAnimator.ofFloat(binding.buttonAdd, View.ALPHA, 1f).setDuration(500)
        val listStory = ObjectAnimator.ofFloat(binding.rvStoryList, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playTogether(addStory, listStory)
            startDelay = 500
        }.start()
    }
}