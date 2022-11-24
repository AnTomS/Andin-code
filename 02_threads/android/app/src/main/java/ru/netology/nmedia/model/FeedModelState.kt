package ru.netology.nmedia.model


data class FeedModelState(
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val loading: Boolean = false,
)