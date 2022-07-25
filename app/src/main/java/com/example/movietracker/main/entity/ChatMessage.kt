package com.example.movietracker.main.entity

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class ChatMessage(
    val id: String? = "",
    val senderId: String? = "",
    val receiverId: String? = "",
    val message: String? = "",
    val mediaUrl: String? = "",
    val date: String? = ""
) : Comparable<ChatMessage> {
    fun getZonedDate(): ZonedDateTime {
        return ZonedDateTime.parse(this.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    override fun compareTo(other: ChatMessage): Int {
        val thisDate = this.getZonedDate()
        val otherDate = other.getZonedDate()

        return when {
            thisDate.isAfter(otherDate) -> 1
            thisDate.isBefore(otherDate) -> -1
            else -> 0
        }
    }
}