package models

import com.google.gson.annotations.SerializedName

data class ReleaseDetails(
    @SerializedName("apkData")
    val apkData: ApkData,
    @SerializedName("outputType")
    val outputType: OutputType,
    @SerializedName("path")
    val path: String, // app-release.apk
    @SerializedName("properties")
    val properties: Properties
)

data class ApkData(
    @SerializedName("baseName")
    val baseName: String, // release
    @SerializedName("dirName")
    val dirName: String,
    @SerializedName("enabled")
    val enabled: Boolean, // true
    @SerializedName("fullName")
    val fullName: String, // release
    @SerializedName("outputFile")
    val outputFile: String, // app-release.apk
    @SerializedName("splits")
    val splits: List<Any>,
    @SerializedName("type")
    val type: String, // MAIN
    @SerializedName("versionCode")
    val versionCode: Int, // 100011
    @SerializedName("versionName")
    val versionName: String // 1.0.0-alpha11
)

data class OutputType(
    @SerializedName("type")
    val type: String // APK
)

class Properties(
)