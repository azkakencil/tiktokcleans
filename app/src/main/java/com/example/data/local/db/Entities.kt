package com.example.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_items",
    indices = [
        Index(value = ["category", "activityTimestampMs"]),
        Index(value = ["category", "status"]),
        Index(value = ["creatorUsername"]),
        Index(value = ["isSelected"])
    ]
)
data class ActivityItemEntity(
    @PrimaryKey val id: String,
    val videoUrl: String,
    val category: String,
    val creatorUsername: String,
    val description: String,
    val activityDateLabel: String,
    val activityTimestampMs: Long,
    val thumbnailUrl: String?,
    val status: String,
    val isSelected: Boolean,
    val sourceLabel: String,
    val errorReason: String?,
    val cachedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "cleaning_sessions")
data class CleaningSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtMs: Long,
    val category: String,
    val totalAnalyzed: Int,
    val totalSelected: Int,
    val totalSucceeded: Int,
    val totalManualAction: Int,
    val totalFailed: Int,
    val status: String
)

@Entity(
    tableName = "cleaning_session_items",
    foreignKeys = [
        ForeignKey(
            entity = CleaningSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class CleaningSessionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val videoUrl: String,
    val category: String,
    val creatorUsername: String,
    val description: String,
    val activityDateLabel: String,
    val thumbnailUrl: String?,
    val finalStatus: String,
    val note: String?
)
