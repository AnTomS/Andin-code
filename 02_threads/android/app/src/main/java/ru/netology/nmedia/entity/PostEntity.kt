package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val viewed: Boolean = false,
) {
    fun toDto() = Post(id, author, authorAvatar, content, published, likedByMe, likes, viewed)

    companion object {
        fun fromDto(dto: Post, viewed: Boolean = false) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                dto.viewed
            )

    }
}

fun List<PostEntity>.toDto() = map { it.toDto() }
fun List<Post>.toEntity() = map { PostEntity.fromDto(it) }

