package org.sonso.hackautumn2025.service

interface EmailSender {
    fun sendPassCodeMessage(to: String, passCode: String)
}
