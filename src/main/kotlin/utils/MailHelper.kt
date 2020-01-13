package utils

import java.io.File
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


object MailHelper {

    fun sendMail(
        username: String,
        password: String,
        smtpHost: String,
        port: String,
        to: List<String>,
        cc: List<String>,
        subject: String?,
        _msg: String,
        file: File,
        fileName: String,
        fromName: String?
    ) {


        require(to.isNotEmpty()) { "To address can't be empty" }


        val properties = Properties()
        properties["mail.smtp.host"] = smtpHost
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.port"] = port
        properties["mail.smtp.starttls.enable"] = true
        properties["mail.smtp.socketFactory.port"] = port
        properties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        val session: Session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                return javax.mail.PasswordAuthentication(username, password)
            }
        })

        val message = MimeMessage(session)
        message.setFrom(InternetAddress(username, fromName))


        val toAddresses = to.joinToString(separator = ",")
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddresses))

        if (cc.isNotEmpty()) {
            val ccAddresses = cc.joinToString(separator = ",")
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses))
        }

        message.subject = subject
        val html = _msg.replace("\n", "<br/>")


        val multipart: Multipart = MimeMultipart()
        val attachmentBodyPart = MimeBodyPart()
        attachmentBodyPart.dataHandler = DataHandler(FileDataSource(file))
        attachmentBodyPart.fileName = fileName
        multipart.addBodyPart(attachmentBodyPart)

        // HTML
        val htmlBodyPart = MimeBodyPart()
        htmlBodyPart.setContent(html, "text/html; charset=utf-8")
        multipart.addBodyPart(htmlBodyPart)
        message.setContent(multipart)

        Transport.send(message)
    }
}