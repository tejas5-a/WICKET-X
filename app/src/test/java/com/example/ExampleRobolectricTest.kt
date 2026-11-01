package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WicketX", appName)
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp")
    fun `responsive layout and bottom navigation fit on small 320dp screen`() {
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_live").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_score").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tournaments").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_profile").assertIsDisplayed()

        // Navigate across tabs on 320dp width
        composeTestRule.onNodeWithTag("nav_live").performClick()
        composeTestRule.onNodeWithTag("tab_matches_0").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tournaments").performClick()
        composeTestRule.onNodeWithTag("fab_create_tournament").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_user_card").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w360dp-h740dp")
    fun `responsive layout works on medium 360dp screen`() {
        composeTestRule.onNodeWithTag("hero_banner_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hero_start_match_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_score").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w480dp-h960dp")
    fun `responsive layout works on large 480dp screen`() {
        composeTestRule.onNodeWithTag("hero_banner_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_score").performClick()
        composeTestRule.onNodeWithTag("scorer_dashboard_screen").assertIsDisplayed()
    }
}
