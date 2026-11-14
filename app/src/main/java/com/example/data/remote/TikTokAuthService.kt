package com.example.data.remote

import android.net.Uri
import com.example.BuildConfig
import com.example.domain.model.OAuthResult
import com.example.domain.model.TikTokUserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class TikTokOwnVideo(
    val id: String,
    val title: String,
    val description: String,
    val coverImageUrl: String?,
    val shareUrl: String,
    val createTimeSec: Long,
    val likeCount: Long
)

class TikTokAuthService(private val client: OkHttpClient) {

    companion object {
        const val REDIRECT_URI = "tiktokcleaner://oauth/callback"
        private const val AUTH_BASE_URL = "https://www.tiktok.com/v2/auth/authorize/"
        private const val TOKEN_URL = "https://open.tiktokapis.com/v2/oauth/token/"
        private const val REVOKE_URL = "https://open.tiktokapis.com/v2/oauth/revoke/"
        private const val USER_INFO_URL =
            "https://open.tiktokapis.com/v2/user/info/?fields=open_id,union_id,avatar_url,display_name,bio_description,profile_deep_link,is_verified,username,follower_count,following_count,likes_count,video_count"
        private const val VIDEO_LIST_URL =
            "https://open.tiktokapis.com/v2/video/list/?fields=id,title,video_description,cover_image_url,share_url,create_time,like_count"
        private const val SCOPES = "user.info.basic,user.info.profile,user.info.stats,video.list"
    }

    fun isClientKeyConfigured(): Boolean {
        val key = BuildConfig.TIKTOK_CLIENT_KEY
        return key.isNotBlank() && !key.startsWith("YOUR_")
    }

    fun createPkcePair(): Pair<String, String> {
        val bytes = ByteArray(48)
        SecureRandom().nextBytes(bytes)
        val verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        return verifier to challenge
    }

    fun generateState(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun buildAuthorizationUri(state: String, codeChallenge: String): Uri {
        return Uri.parse(AUTH_BASE_URL).buildUpon()
            .appendQueryParameter("client_key", BuildConfig.TIKTOK_CLIENT_KEY)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
    }

    suspend fun exchangeCodeForTokens(
        code: String,
        codeVerifier: String
    ): Result<Triple<String, String?, Long>> = withContext(Dispatchers.IO) {
        runCatching {
            val formBuilder = FormBody.Builder()
                .add("client_key", BuildConfig.TIKTOK_CLIENT_KEY)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", REDIRECT_URI)
                .add("code_verifier", codeVerifier)

            val secret = BuildConfig.TIKTOK_CLIENT_SECRET
            if (secret.isNotBlank() && !secret.startsWith("YOUR_")) {
                formBuilder.add("client_secret", secret)
            }

            val request = Request.Builder()
                .url(TOKEN_URL)
                .post(formBuilder.build())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("TikTok OAuth token exchange failed (${response.code}): $rawBody")
                }
                val json = JSONObject(rawBody)
                if (json.has("error") && json.optString("error").isNotBlank()) {
                    val desc = json.optString("error_description", json.optString("error"))
                    throw IllegalStateException(desc)
                }
                val accessToken = json.getString("access_token")
                val refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() }
                val expiresIn = json.optLong("expires_in", 86400L)
                Triple(accessToken, refreshToken, expiresIn)
            }
        }
    }

    suspend fun fetchUserInfo(accessToken: String): OAuthResult = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(USER_INFO_URL)
                .get()
                .header("Authorization", "Bearer $accessToken")
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@runCatching OAuthResult(
                        success = false,
                        errorMessage = "TikTok user info error (${response.code})"
                    )
                }
                val root = JSONObject(rawBody)
                val errorObj = root.optJSONObject("error")
                val errorCode = errorObj?.optString("code").orEmpty()
                if (errorCode.isNotBlank() && errorCode != "ok") {
                    return@runCatching OAuthResult(
                        success = false,
                        errorMessage = errorObj?.optString("message", errorCode) ?: errorCode
                    )
                }
                val userObj = root.optJSONObject("data")?.optJSONObject("user")
                    ?: return@runCatching OAuthResult(false, errorMessage = "Missing user payload")

                val profile = TikTokUserProfile(
                    openId = userObj.optString("open_id"),
                    unionId = userObj.optString("union_id"),
                    username = userObj.optString("username").ifBlank {
                        userObj.optString("display_name")
                    },
                    displayName = userObj.optString("display_name"),
                    avatarUrl = userObj.optString("avatar_url"),
                    bioDescription = userObj.optString("bio_description"),
                    profileDeepLink = userObj.optString("profile_deep_link"),
                    isVerified = userObj.optBoolean("is_verified", false),
                    followerCount = userObj.optLong("follower_count", 0L),
                    followingCount = userObj.optLong("following_count", 0L),
                    likesCount = userObj.optLong("likes_count", 0L),
                    videoCount = userObj.optLong("video_count", 0L),
                    connectedAtEpochMs = System.currentTimeMillis(),
                    isDataExportMode = false
                )
                OAuthResult(success = true, profile = profile)
            }
        }.getOrElse { err ->
            OAuthResult(success = false, errorMessage = err.localizedMessage ?: "Network error")
        }
    }

    suspend fun fetchOwnVideos(accessToken: String): Result<List<TikTokOwnVideo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = JSONObject().put("max_count", 20).toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(VIDEO_LIST_URL)
                    .post(payload)
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                client.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException("video.list HTTP ${response.code}")
                    }
                    val root = JSONObject(raw)
                    val videosArray = root.optJSONObject("data")?.optJSONArray("videos")
                        ?: return@runCatching emptyList()
                    buildList {
                        for (i in 0 until videosArray.length()) {
                            val obj = videosArray.getJSONObject(i)
                            add(
                                TikTokOwnVideo(
                                    id = obj.optString("id"),
                                    title = obj.optString("title"),
                                    description = obj.optString("video_description"),
                                    coverImageUrl = obj.optString("cover_image_url").takeIf { it.isNotBlank() },
                                    shareUrl = obj.optString("share_url"),
                                    createTimeSec = obj.optLong("create_time", 0L),
                                    likeCount = obj.optLong("like_count", 0L)
                                )
                            )
                        }
                    }
                }
            }
        }

    suspend fun revokeToken(accessToken: String): Boolean = withContext(Dispatchers.IO) {
        if (!isClientKeyConfigured()) return@withContext false
        runCatching {
            val form = FormBody.Builder()
                .add("client_key", BuildConfig.TIKTOK_CLIENT_KEY)
                .add("token", accessToken)
                .build()
            val request = Request.Builder()
                .url(REVOKE_URL)
                .post(form)
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }
}
