package com.aeci.mmucompanion.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PasswordResetRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("reset_token")
    val resetToken: String,
    @SerializedName("device_id")
    val deviceId: String
)

data class PasswordResetResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("email_sent")
    val emailSent: Boolean = false
)

data class PasswordResetVerifyRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("device_id")
    val deviceId: String
)

data class PasswordResetVerifyResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("valid")
    val valid: Boolean,
    @SerializedName("message")
    val message: String
)

data class PasswordResetCompleteRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("new_password")
    val newPassword: String,
    @SerializedName("device_id")
    val deviceId: String
)

data class PasswordResetCompleteResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)
