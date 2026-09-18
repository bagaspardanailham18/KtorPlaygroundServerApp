package com.bagaspardanailham.bpiktorplayground.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val isCompleted: Boolean
)