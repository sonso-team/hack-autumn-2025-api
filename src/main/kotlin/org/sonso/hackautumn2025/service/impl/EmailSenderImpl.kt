package org.sonso.hackautumn2025.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.sonso.hackautumn2025.service.EmailSender
import java.nio.charset.StandardCharsets

@Service
internal class EmailSenderImpl(
    private val mailSender: JavaMailSenderImpl,
    private val mailProperties: MailProperties
) : EmailSender {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val cachedTemplate: String by lazy {
        ClassPathResource("templates/authcode-mail.html")
            .inputStream
            .readBytes()
            .toString(StandardCharsets.UTF_8)
    }

    @Async
    override fun sendPassCodeMessage(to: String, passCode: String) {
        log.info("Sending mail to verify is began")

        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(mailProperties.username)
        helper.setTo(to)
        helper.setSubject("$passCode - одноразовый код для входа")
        helper.setText(passwordMailTemplate(passCode), true) // true for HTML

        mailSender.send(message)
        log.info("Passcode mail $to has been sent")
    }

    private fun passwordMailTemplate(password: String): String {
        return cachedTemplate.replace("{{password}}", password)
    }
}
