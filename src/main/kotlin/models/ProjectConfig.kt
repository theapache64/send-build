package models

import com.google.gson.annotations.SerializedName

data class ProjectConfig(
    @SerializedName("cc")
    val cc: List<String>,
    @SerializedName("name")
    val name: String, // Send Build
    @SerializedName("to")
    val to: List<String>, // theapache64@gmail.com
    @SerializedName("last_sent_date_time_in_millis")
    var lastSentDateTimeInMillis: Long
)