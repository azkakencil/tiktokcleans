package com.example.data.remote

import com.example.domain.model.ActivityCategory
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.VideoItemStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TikTokDataExportParser {

    private val json = Json { ignoreUnknownKeys = true }

    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    fun parseOfficialExportJson(rawJson: String): Result<List<TikTokActivityItem>> = runCatching {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val results = mutableListOf<TikTokActivityItem>()

        // Support both TikTok GDPR export schemas ("Activity", "Your Activity", or "Likes and Favorites")
        val activityObj = (root["Activity"] as? JsonObject)
            ?: (root["Your Activity"] as? JsonObject)
            ?: (root["Likes and Favorites"] as? JsonObject)
            ?: root

        val likesArray = findArrayByPaths(
            root,
            activityObj,
            listOf(
                listOf("Like List", "ItemFavoriteList"),
                listOf("Likes and Favorites", "Like List", "ItemFavoriteList"),
                listOf("Like List", "LikesList"),
                listOf("Likes")
            )
        )
        likesArray?.let { extractEntries(it, ActivityCategory.LIKES, "TikTok Data Export", results) }

        val favoritesArray = findArrayByPaths(
            root,
            activityObj,
            listOf(
                listOf("Favorite Videos", "FavoriteVideoList"),
                listOf("Likes and Favorites", "Favorite Videos", "FavoriteVideoList"),
                listOf("Favorites")
            )
        )
        favoritesArray?.let { extractEntries(it, ActivityCategory.FAVORITES, "TikTok Data Export", results) }

        val repostsArray = findArrayByPaths(
            root,
            activityObj,
            listOf(
                listOf("Reposts", "RepostList"),
                listOf("Repost List", "RepostList"),
                listOf("Your Activity", "Reposts", "RepostList"),
                listOf("Reposts")
            )
        )
        repostsArray?.let { extractEntries(it, ActivityCategory.REPOSTS, "TikTok Data Export", results) }

        results.distinctBy { "${it.category.name}:${it.videoUrl}" }
    }

    fun parsePastedLinks(rawText: String, category: ActivityCategory): List<TikTokActivityItem> {
        val nowMs = System.currentTimeMillis()
        val nowLabel = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(nowMs)
        return rawText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && TikTokOEmbedService.isSupportedTikTokUrl(it) }
            .distinct()
            .map { url ->
                val creator = TikTokOEmbedService.extractUsernameFromUrl(url)
                TikTokActivityItem(
                    id = buildStableId(category, url),
                    videoUrl = url,
                    category = category,
                    creatorUsername = creator,
                    description = "",
                    activityDateLabel = nowLabel,
                    activityTimestampMs = nowMs,
                    thumbnailUrl = null,
                    status = VideoItemStatus.PENDING_ANALYSIS,
                    isSelected = false,
                    sourceLabel = "Direct URL Analysis"
                )
            }
            .toList()
    }

    private fun extractEntries(
        array: JsonArray,
        category: ActivityCategory,
        sourceLabel: String,
        target: MutableList<TikTokActivityItem>
    ) {
        for (element in array) {
            val obj = element as? JsonObject ?: continue
            val link = obj.stringField("Link", "link", "VideoLink")
            if (link.isBlank() || !TikTokOEmbedService.isSupportedTikTokUrl(link)) continue

            val rawDate = obj.stringField("Date", "date")
            val timestampMs = parseDateMs(rawDate)
            val creator = TikTokOEmbedService.extractUsernameFromUrl(link)

            target.add(
                TikTokActivityItem(
                    id = buildStableId(category, link),
                    videoUrl = link,
                    category = category,
                    creatorUsername = creator,
                    description = "",
                    activityDateLabel = rawDate.ifBlank { "Unknown date" },
                    activityTimestampMs = timestampMs,
                    thumbnailUrl = null,
                    status = VideoItemStatus.PENDING_ANALYSIS,
                    isSelected = false,
                    sourceLabel = sourceLabel
                )
            )
        }
    }

    private fun JsonObject.stringField(vararg keys: String): String {
        for (k in keys) {
            val value = this[k]?.jsonPrimitive?.contentOrNull?.trim()
            if (!value.isNullOrBlank()) return value
        }
        return ""
    }

    private fun findArrayByPaths(
        root: JsonObject,
        secondaryRoot: JsonObject,
        paths: List<List<String>>
    ): JsonArray? {
        for (candidateRoot in listOf(secondaryRoot, root)) {
            for (path in paths) {
                var current: JsonElement? = candidateRoot
                for (key in path) {
                    current = (current as? JsonObject)?.get(key)
                    if (current == null) break
                }
                if (current is JsonArray && current.isNotEmpty()) {
                    return current
                }
            }
        }
        return null
    }

    private fun parseDateMs(raw: String): Long {
        if (raw.isBlank()) return 0L
        for (fmt in dateFormats) {
            val parsed = runCatching { fmt.parse(raw)?.time }.getOrNull()
            if (parsed != null) return parsed
        }
        return 0L
    }

    companion object {
        fun buildStableId(category: ActivityCategory, videoUrl: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("${category.name}:$videoUrl".toByteArray(Charsets.UTF_8))
            return digest.take(16).joinToString("") { "%02x".format(it) }
        }
    }
}
