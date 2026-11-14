package com.example

import com.example.data.remote.TikTokDataExportParser
import com.example.domain.model.ActivityCategory
import com.example.domain.model.VideoItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TikTokDataExportParserTest {

    private val parser = TikTokDataExportParser()

    @Test
    fun parsesOfficialTikTokJsonExportCategories() {
        val sampleJson = """
            {
              "Activity": {
                "Like List": {
                  "ItemFavoriteList": [
                    { "Date": "2026-10-01 12:30:00", "Link": "https://www.tiktok.com/@creator1/video/111111" }
                  ]
                },
                "Favorite Videos": {
                  "FavoriteVideoList": [
                    { "Date": "2026-10-02 15:45:00", "Link": "https://www.tiktok.com/@creator2/video/222222" }
                  ]
                },
                "Reposts": {
                  "RepostList": [
                    { "Date": "2026-10-03 09:00:00", "Link": "https://www.tiktok.com/@creator3/video/333333" }
                  ]
                }
              }
            }
        """.trimIndent()

        val result = parser.parseOfficialExportJson(sampleJson)
        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(3, items.size)
        assertEquals(ActivityCategory.LIKES, items[0].category)
        assertEquals("creator1", items[0].creatorUsername)
        assertEquals(ActivityCategory.FAVORITES, items[1].category)
        assertEquals(ActivityCategory.REPOSTS, items[2].category)
        assertEquals(VideoItemStatus.PENDING_ANALYSIS, items[0].status)
    }

    @Test
    fun filtersNonTikTokUrlsWhenParsingLinks() {
        val input = """
            https://www.tiktok.com/@alpha/video/999
            https://example.com/not-tiktok
            https://vt.tiktok.com/ZSNabcdef/
        """.trimIndent()

        val items = parser.parsePastedLinks(input, ActivityCategory.FAVORITES)
        assertEquals(2, items.size)
        assertEquals("alpha", items[0].creatorUsername)
        assertEquals(ActivityCategory.FAVORITES, items[0].category)
    }
}
