import com.google.gson.reflect.TypeToken
import models.CoreConfig
import models.ProjectConfig
import models.ReleaseDetails
import utils.DateUtils
import utils.DesktopApi
import utils.GsonUtils
import utils.MailHelper
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


private const val CORE_CONFIG_SAMPLE_FILE_NAME = "core_config.sample.json"
private const val CORE_CONFIG_FILE_NAME = "core_config.json"
private const val PROJECT_CONFIG_SAMPLE_FILE_NAME = "project_config.sample.json"
private const val FEATURE = "🌟"
private const val FIX = "🐛"
private const val FIX_OR_FEATURE = "$FEATURE\\|$FIX"

lateinit var currentDir: String
lateinit var projectConfig: ProjectConfig
lateinit var coreConfig: CoreConfig
lateinit var releaseDetails: List<ReleaseDetails>
lateinit var buildFile: File
lateinit var projectConfigFile: File

private const val IS_DEBUG = false

class Main {

}

fun getJarDir(): String {
    return File(
        Main::class.java.protectionDomain.codeSource.location
            .toURI()
    ).parent
}

fun main(args: Array<String>) {

    val coreConfigFile = File(getJarDir() + File.separator + CORE_CONFIG_FILE_NAME)

    // Getting core config
    if (coreConfigFile.exists()) {

        coreConfig =
            GsonUtils.gson.fromJson(coreConfigFile.readText(), CoreConfig::class.java)


        currentDir =
            if (IS_DEBUG) "/home/theapache64/Documents/projects/thinkpalm/toshokan" else System.getProperty("user.dir")

        projectConfigFile = File("$currentDir/project_config.json")

        if (projectConfigFile.exists()) {

            projectConfig =
                GsonUtils.gson.fromJson(projectConfigFile.readText(), ProjectConfig::class.java)


            val releaseDetailsFile = File("${currentDir}/app/release/output.json")
            if (releaseDetailsFile.exists()) {

                val typeToken = TypeToken.getParameterized(List::class.java, ReleaseDetails::class.java)
                releaseDetails =
                    GsonUtils.gson.fromJson(releaseDetailsFile.readText(), typeToken.type)

                buildFile = File("$currentDir/app/release/${releaseDetails.first().apkData.outputFile}")

                if (buildFile.exists()) {

                    val commits = getCommits()
                    if (commits.isNotEmpty()) {
                        val mailBody = getMailBody(commits)
                        val fileName = "${projectConfig.name}-${releaseDetails.first().apkData.versionName}.apk"
                        val cc =
                            if (projectConfig.cc.isNotEmpty()) " and CC to ${projectConfig.cc.joinToString(separator = "")}" else ""
                        println(
                            "Sending ${releaseDetails.first().path} as $fileName to ${projectConfig.to.joinToString(
                                separator = ","
                            )} $cc ..."
                        )
                        send(mailBody, fileName)
                        println("👍 Sent")
                    } else {
                        // no commits changed
                        logError("No new issue/release commit found. Rejecting build delivery")
                    }

                } else {
                    logError("${buildFile.name} not found")
                }


            } else {
                logError("Release details file not found")
            }


        } else {
            logError("Send build config not initiated.")
            val projectConfigSampleFile = File(getJarDir() + File.separator + PROJECT_CONFIG_SAMPLE_FILE_NAME)
            projectConfigSampleFile.copyTo(projectConfigFile)
            DesktopApi.open(projectConfigFile)
        }
    } else {
        println("Core config not initiated")
        val coreConfigSampleFile = File(getJarDir() + File.separator + CORE_CONFIG_SAMPLE_FILE_NAME)
        coreConfigSampleFile.copyTo(coreConfigFile)
        DesktopApi.open(coreConfigFile)
    }

}

fun send(mailBody: String, fileName: String) {
    val smtpConfig = coreConfig.smtpConfig

    MailHelper.sendMail(
        smtpConfig.username,
        smtpConfig.password,
        smtpConfig.host,
        smtpConfig.port,
        projectConfig.to,
        projectConfig.cc,
        "${projectConfig.name} New Build : v${releaseDetails.first().apkData.versionName}",
        mailBody,
        buildFile,
        fileName,
        coreConfig.name
    )

    // Change last send date
    projectConfig.lastSentDateTimeInMillis = System.currentTimeMillis()
    val newJson = GsonUtils.gson.toJson(projectConfig)
    projectConfigFile.writeText(newJson)
}

fun getMailBody(commits: List<String>): String {

    val mailBodyBuilder = StringBuilder(
        """
            Hi,

            Here's a new build of project ${projectConfig.name}.
            
            
        """.trimIndent()
    )

    if (commits.isNotEmpty()) {
        val features = mutableListOf<String>()
        val fixes = mutableListOf<String>()

        commits.forEach { commit ->
            if (commit.contains(FIX)) {
                fixes.add(commit)
            } else if (commit.contains(FEATURE)) {
                features.add(commit)
            }
        }

        println("🌟 ${features.size} feature(s)")
        println("🐛 ${fixes.size} fix(es)")

        mailBodyBuilder.append(
            """
                
            <b><u>Changelog</u></b>
            
        """.trimIndent()
        )

        if (features.isNotEmpty()) {
            mailBodyBuilder.append(
                """
                    
                <b><u>Features</u></b>
                
            """.trimIndent()
            )

            features.forEach { feature ->
                mailBodyBuilder.append(abbr(feature)).append("\n")
            }
        }


        if (fixes.isNotEmpty()) {
            mailBodyBuilder.append(
                """
                    
                <b><u>Bug Fixes</u></b>
                
            """.trimIndent()
            )

            fixes.forEach { fix ->
                val newFix = abbr(
                    fix.replace(
                        "#(\\d+)".toRegex(),
                        "<a href=\"http://bugzilla.thinkpalm.info/show_bug.cgi?id=$1\">#$1</a>"
                    )
                )

                mailBodyBuilder.append(newFix).append("\n")
            }
        }

    }

    mailBodyBuilder.append(
        """
            
            Regards
            ${coreConfig.name}
        """.trimIndent()
    )

    return mailBodyBuilder.toString()
}

fun abbr(replace: String): String {
    return replace.replace(
        "(.+) DATE!(.+)!\$".toRegex(),
        "<abbr style=\"text-decoration: none !important;\" title=\"$2\">$1</abbr>"
    )
}

@Throws(IOException::class)
fun getCommits(): List<String> {
    val gitFolder = File("$currentDir/.git")
    if (gitFolder.exists()) {
        val lastTime = projectConfig.lastSentDateTimeInMillis

        val from = if (lastTime != -1L) {
            DateUtils.toYYYMMDDWithTime(lastTime)
        } else {
            null
        }

        val commitCommand = getReportCommand(from, FIX_OR_FEATURE)
        return executeCommand(commitCommand)

    } else {
        throw IOException("${projectConfig.name} is not a git project")
    }
}


@Throws(IOException::class)
fun executeCommand(command: String): List<String> {

    val rt = Runtime.getRuntime()
    val commands = arrayOf(
        "/bin/sh", "-c",
        "cd $currentDir && $command"
    )
    val proc = rt.exec(commands)

    val stdInput = BufferedReader(InputStreamReader(proc.inputStream))

    val stdError = BufferedReader(InputStreamReader(proc.errorStream))

    // Read the output from the command
    // Read the output from the command
    var s: String?
    val result = mutableListOf<String>()
    while (stdInput.readLine().also { s = it } != null) {
        result.add(s!!)
    }

    // Read any errors from the attempted command
    // Read any errors from the attempted command
    val error = StringBuilder()
    while (stdError.readLine().also { s = it } != null) {
        error.append(error).append("\n")
    }

    if (error.isNotBlank()) {
        // has error
        throw IOException(error.toString())
    }

    return result
}

fun getReportCommand(sinceDate: String?, grepVal: String?): String {
    val since = if (sinceDate != null) "--since=\"$sinceDate\"" else ""
    val grep = if (grepVal != null) "--grep=\"$grepVal\" -i" else ""
    return "git log $grep $since --format=\"- %s DATE!%ar - %ad!\" --date=format:\"%d %b %I:%M:%S:%p\" | sort -V"
}

fun logError(msg: String) {
    println("ERROR : $msg")
}