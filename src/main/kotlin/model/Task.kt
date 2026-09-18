package com.bagaspardanailham.bpiktorplayground.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int? = null,
    val title: String,
    val isCompleted: Boolean,
    val userId: Int? = null
)