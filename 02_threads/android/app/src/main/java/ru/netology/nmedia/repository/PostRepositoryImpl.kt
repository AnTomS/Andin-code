package ru.netology.nmedia.repository


import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post


private const val RESPONSE_CODE_SUCCESS = 200

class PostRepositoryImpl : PostRepository {



    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostsApi.retrofitService.getAll().enqueue(object : retrofit2.Callback<List<Post>> {
            override fun onResponse(
                call: Call<List<Post>>,
                response: Response<List<Post>>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }

                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }
        })
    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.likeById(id).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    callback.onError(
                        RuntimeException("Error '${response.code()}' with message: '${response.message()}'")
                    )
                    return
                }

                val body = response.body() ?: run {
                    callback.onError(
                        RuntimeException("Error '${response.code()}' with message: '${response.message()}'")
                    )
                    return
                }
                callback.onSuccess(body)
            }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }
            })
        }

    override fun dislikeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {

        PostsApi.retrofitService.dislikeById(id).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    callback.onError(
                        RuntimeException("Error '${response.code()}' with message: '${response.message()}'")
                    )
                    return
                }

                val body = response.body() ?: run {
                    callback.onError(
                        RuntimeException("Error '${response.code()}' with message: '${response.message()}'")
                    )
                    return
                }
                callback.onSuccess(body)
            }
            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }
        })
    }


    override fun shareById(id: Long) {
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.save(post).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                call: Call<Post>,
                response: Response<Post>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }

                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }
        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {

        PostsApi.retrofitService.removeById(id).enqueue(object : retrofit2.Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }

                if (response.code() == RESPONSE_CODE_SUCCESS) {
                    callback.onSuccess(Unit)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }
        })
    }

    }
