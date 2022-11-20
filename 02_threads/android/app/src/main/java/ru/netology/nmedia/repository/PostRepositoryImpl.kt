package ru.netology.nmedia.repository


import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnkError
import java.io.IOException


class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {

    override val data: Flow<List<Post>> = postDao.getAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override fun getNeverCount(firstId: Long): Flow<Int> = flow {
        try {
            while (true) {

                val response = PostsApi.retrofitService.getNewer(firstId)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(
                    body.toEntity()

                )
                emit(body.size)
                delay(10_000L)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }
        .flowOn(Dispatchers.Default)


    override suspend fun getAllAsync() {
        try {
            //postDao.getAll()
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity()
                .map { it.copy(viewed = true) }
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun getUnViewedPost() {
        try {
            postDao.viewedPosts()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }


    override suspend fun saveAsync(post: Post) {
        try {

            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun removeByIdAsync(id: Long) {
        try {
            postDao.removeById(id)
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun likeByIdAsync(id: Long) {
        try {
            postDao.likeById(id)
            val response = PostsApi.retrofitService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
                    .copy(viewed = false)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun dislikeByIdAsync(id: Long) {
        try {
            val response = PostsApi.retrofitService.dislikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
                    .copy(viewed = false)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun shareById(id: Long) {
        try {
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(
                    response.code(), response.message()
                )
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }
}
