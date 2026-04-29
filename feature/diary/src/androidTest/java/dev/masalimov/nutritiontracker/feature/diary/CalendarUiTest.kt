package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.core.ui.NutritionTrackerTheme
import dev.masalimov.nutritiontracker.domain.diary.model.DiaryDate
import dev.masalimov.nutritiontracker.feature.diary.components.Calendar
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val june15 = DiaryDate.of(LocalDate(2024, 6, 15))
    private val june16 = DiaryDate.of(LocalDate(2024, 6, 16))
    private val june17 = DiaryDate.of(LocalDate(2024, 6, 17))

    @Test
    fun showsLoadingPlaceholders_whenLoading() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                Calendar(calendarUiState = CalendarUiState.Loading)
            }
        }
        // Loading state renders placeholder items with "." and "xxx" text
        composeTestRule.onAllNodes(
            hasText(".")
        ).apply {
            fetchSemanticsNodes().isNotEmpty().let { assertThat(it).isTrue() }
        }
    }

    @Test
    fun showsDayNumbers_whenCalendarLoaded() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                Calendar(
                    calendarUiState = CalendarUiState.Calendar(
                        uiModels = listOf(
                            DateUiModel(june15, isSelected = true),
                            DateUiModel(june16, isSelected = false),
                            DateUiModel(june17, isSelected = false),
                        )
                    )
                )
            }
        }
        composeTestRule.onNodeWithText("15").assertIsDisplayed()
        composeTestRule.onNodeWithText("16").assertIsDisplayed()
        composeTestRule.onNodeWithText("17").assertIsDisplayed()
    }

    @Test
    fun showsMonthAbbreviation_whenCalendarLoaded() {
        composeTestRule.setContent {
            NutritionTrackerTheme {
                Calendar(
                    calendarUiState = CalendarUiState.Calendar(
                        uiModels = listOf(
                            DateUiModel(june15, isSelected = true),
                        )
                    )
                )
            }
        }
        // June abbreviation — locale-sensitive but "Jun" for default English locale
        composeTestRule.onNodeWithText("Jun").assertIsDisplayed()
    }

    @Test
    fun invokesCallback_whenDayClicked() {
        var clickedDate: DiaryDate? = null
        composeTestRule.setContent {
            NutritionTrackerTheme {
                Calendar(
                    calendarUiState = CalendarUiState.Calendar(
                        uiModels = listOf(
                            DateUiModel(june15, isSelected = false),
                            DateUiModel(june16, isSelected = false),
                        )
                    ),
                    onDayClick = { clickedDate = it }
                )
            }
        }
        composeTestRule.onNodeWithText("16").performClick()
        assertThat(clickedDate).isEqualTo(june16)
    }

    @Test
    fun doesNotInvokeOnDayClickCallback_whenDayClickedInLoadingState() {
        var callbackInvoked = false
        composeTestRule.setContent {
            NutritionTrackerTheme {
                Calendar(
                    calendarUiState = CalendarUiState.Loading,
                    onDayClick = { callbackInvoked = true }
                )
            }
        }
        // Loading placeholder items use an empty default onClick, so the outer callback is not wired
        composeTestRule.onAllNodes(
            hasText(".")
        ).apply {
            if (fetchSemanticsNodes().isNotEmpty()) {
                get(0).performClick()
            }
        }
        assertThat(callbackInvoked).isFalse()
    }
}
