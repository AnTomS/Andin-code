package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.util.SingleLiveEvent


private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<FeedModel> =
        repository.data.map { FeedModel(it, it.isEmpty()) }.asLiveData(Dispatchers.Default)

    private val edited = MutableLiveData(empty)

    private val _dataState = MutableLiveData(FeedModelState())

    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNeverCount(it.posts.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default)
    }


    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val scope = MainScope()

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {

        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllAsync()
                //repository.getNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }

    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAllAsync()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun retrySave(post: Post?) {
        viewModelScope.launch {
            try {
                if (post != null) {
                    PostsApi.retrofitService.save(post)
                    loadPosts()
                }
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(error = true, retryType = RetryTypes.SAVE, retryPost = post)
            }
        }
    }


    fun save() {
        edited.value?.let {

            viewModelScope.launch {
                try {
                    _postCreated.value = Unit
                    repository.saveAsync(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeByIdAsync(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            repository.dislikeByIdAsync(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true, retryId = id)
        }
    }


    fun removeById(id: Long) {
        val posts = data.value?.posts.orEmpty()
            .filter { it.id != id }
        data.value?.copy(posts = posts, empty = posts.isEmpty())

        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun loadNewPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            //repository.getAllAsync()
            repository.getNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}




