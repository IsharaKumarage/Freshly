package com.freshly.app.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userType: UserType = UserType.CONSUMER,
    val profileImageUrl: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

enum class UserType {
    FARMER,
    CONSUMER,
    ADMIN
}
