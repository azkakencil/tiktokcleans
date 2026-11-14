package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

data class OEmbedMetadata(
    val thumbnailUrl: String?,
    val creatorUsername: String?,
    val title: String?,
    val success: Boolean,
    val errorReason: String? = null
)

class TikTokOEmbedService(private val client: OkHttpClient) {

    // Respect TikTok API rate limits by capping parallel oEmbed fetches
    private val concurrencyLimit = Semaphore(permits = 3)

    suspend fun fetchVideoMetadata(videoUrl: String): OEmbedMetadata = withContext(Dispatchers.IO) {
        concurrencyLimit.withPermit {
            delay(120L) // gentle pacing to avoid HTTP 429 rate-limit throttling
            val cleanUrl = videoUrl.trim()
            if (!isSupportedTikTokUrl(cleanUrl)) {
                return@withContext OEmbedMetadata(
                    thumbnailUrl = null,
                    creatorUsername = null,
                    title = null,
                    success = false,
                    errorReason = "Invalid TikTok URL"
                )
            }

            runCatching {
                val encoded = URLEncoder.encode(cleanUrl, "UTF-8")
                val request = Request.Builder()
                    .url("https://www.tiktok.com/oembed?url=$encoded")
                    .get()
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@runCatching OEmbedMetadata(
                            thumbnailUrl = null,
                            creatorUsername = extractUsernameFromUrl(cleanUrl),
                            title = null,
                            success = false,
                            errorReason = "HTTP ${response.code}"
                        )
                    }
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val thumb = json.optString("thumbnail_url").takeIf { it.isNotBlank() }
                    val authorUniqueId = json.optString("author_unique_id").takeIf { it.isNotBlank() }
                    val authorName = json.optString("author_name").takeIf { it.isNotBlank() }
                    val creator = authorUniqueId ?: authorName ?: extractUsernameFromUrl(cleanUrl)
                    val title = json.optString("title").takeIf { it.isNotBlank() }

                    OEmbedMetadata(
                        thumbnailUrl = thumb,
                        creatorUsername = creator,
                        title = title,
                        success = thumb != null,
                        errorReason = if (thumb == null) "Thumbnail unavailable" else null
                    )
                }
            }.getOrElse { error ->
                OEmbedMetadata(
                    thumbnailUrl = null,
                    creatorUsername = extractUsernameFromUrl(cleanUrl),
                    title = null,
                    success = false,
                    errorReason = error.localizedMessage ?: "Network unavailable"
                )
            }
        }
    }

    companion object {
        fun isSupportedTikTokUrl(url: String): Boolean {
            val lower = url.lowercase()
            return (lower.startsWith("https://") || lower.startsWith("http://")) &&
                (lower.contains("tiktok.com/") || lower.contains("tiktokv.com/"))
        }

        fun extractUsernameFromUrl(url: String): String {
            val regex = Regex("""tiktok\.com/@([A-Za-z0-9_.]+)""")
            val match = regex.find(url)
            return match?.groupValues?.getOrNull(1) ?: "tiktok_creator"
        }
    }
}
