package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.system.exitProcess

class FeedFragment : Fragment() {

    internal val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

        })
        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)

            binding.emptyText.isVisible = state.empty
        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(
                    binding.root,
                    R.string.error_loading,
                    Snackbar.LENGTH_LONG
                )

                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .setAction(R.string.close_app) {
                        activity?.finish()
                        exitProcess(0)
                    }
                    .show()
            }
        }

        binding.newPostFab.visibility = View.GONE


        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.newPostFab.visibility = View.VISIBLE
            }
            println("Newer count: $it")
        }

        binding.retryButton.setOnClickListener {
            viewModel.refreshPosts()
        }

        binding.newPostFab.setOnClickListener {
            viewModel.loadNewPosts()
            binding.newPostFab.visibility = View.GONE
            binding.list.smoothScrollToPosition(0)
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
            binding.swipeRefresh.isRefreshing = false
        }

        return binding.root
    }
}