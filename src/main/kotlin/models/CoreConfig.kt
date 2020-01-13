package models

import com.google.gson.annotations.SerializedName


data class CoreConfig(
    @SerializedName("name")
    val name: String, // Your Name
    @SerializedName("smtp_config")
    val smtpConfig: SmtpConfig
)

data class SmtpConfig(
    @SerializedName("host")
    val host: String, // HOST
    @SerializedName("password")
    val password: String, // PASSWORD
    @SerializedName("port")
    val port: String, // PORT
    @SerializedName("username")
    val username: String // USERNAME
)