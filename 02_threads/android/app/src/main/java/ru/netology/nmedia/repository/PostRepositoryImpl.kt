package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity


private const val RESPONSE_CODE_SUCCESS = 200

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data: LiveData<List<Post>> = postDao.getAll().map {
        it.map(PostEntity::toDto)
    }

    override suspend fun getAllAsync(): List<Post> {
        val posts = PostsApi.retrofitService.getAll()
        postDao.insert(posts.map(PostEntity::fromDto))
        return posts
    }

    override suspend fun likeByIdAsync(id: Long) {
    }

    override suspend fun dislikeByIdAsync(id: Long) {

    }

    override suspend fun shareById(id: Long) {

    }

    override suspend fun saveAsync(post: Post) {

    }

    override suspend fun removeByIdAsync(id: Long) {
    }


}
