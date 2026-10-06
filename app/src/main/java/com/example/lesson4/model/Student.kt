package com.example.lesson4.model

import java.io.Serializable

data class Student(
    val id: Long = 0,
    val studentCode: String,
    val fullName: String,
    val email: String,
    val avatarUri: String = ""
) : Serializable
