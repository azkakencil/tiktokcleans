package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DataStorage
import com.example.data.local.db.TikTokCleanerDatabase
import com.example.domain.model.ActivityCategory
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.VideoItemStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataStorageTest {

    private lateinit var database: TikTokCleanerDatabase
    private lateinit var dataStorage: DataStorage

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TikTokCleanerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataStorage = DataStorage(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun cachesLargeListsInBatchesAndPreservesResolvedMetadata() = runTest {
        val largeList = (1..1200).map { index ->
            val category = if (index % 2 == 0) ActivityCategory.LIKES else ActivityCategory.FAVORITES
            TikTokActivityItem(
                id = "id_$index",
                videoUrl = "https://www.tiktok.com/@creator$index/video/$index",
                category = category,
                creatorUsername = "creator$index",
                description = "Video $index",
                activityDateLabel = "2026-10-01",
                activityTimestampMs = index.toLong() * 1000L,
                thumbnailUrl = if (index == 1) "https://cdn.example.com/thumb1.jpg" else null,
                status = if (index == 1) VideoItemStatus.LOADED else VideoItemStatus.PENDING_ANALYSIS,
                isSelected = index == 1,
                sourceLabel = "TikTok Data Export"
            )
        }

        dataStorage.cacheActivities(largeList)
        assertEquals(1200, dataStorage.getCachedItemCount())

        // Re-importing raw export entries with null thumbnails should preserve cached metadata for id_1
        val reimportItem1 = largeList.first().copy(
            thumbnailUrl = null,
            status = VideoItemStatus.PENDING_ANALYSIS,
            isSelected = false
        )
        dataStorage.cacheActivities(listOf(reimportItem1))

        val pendingLikes = dataStorage.getItemsNeedingMetadataResolution(ActivityCategory.LIKES)
        val pendingFavorites = dataStorage.getItemsNeedingMetadataResolution(ActivityCategory.FAVORITES)
        assertEquals(600, pendingLikes.size)
        assertEquals(599, pendingFavorites.size) // id_1 is skipped because its thumbnail is cached

        val page = dataStorage.getActivityPage(
            category = ActivityCategory.FAVORITES,
            searchQuery = "creator1",
            sortNewest = true,
            limit = 20,
            offset = 0
        )
        assertEquals(20, page.size)
    }
}
