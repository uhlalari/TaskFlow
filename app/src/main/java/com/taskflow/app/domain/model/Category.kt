package com.taskflow.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String,
    val sortOrder: Int = 0
)
