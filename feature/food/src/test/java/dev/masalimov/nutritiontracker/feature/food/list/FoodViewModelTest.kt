package dev.masalimov.nutritiontracker.feature.food.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.masalimov.nutritiontracker.domain.FoodSearchException
import dev.masalimov.nutritiontracker.domain.food.Food
import dev.masalimov.nutritiontracker.domain.food.FoodId
import dev.masalimov.nutritiontracker.domain.food.FoodRepository
import dev.masalimov.nutritiontracker.domain.food.usecase.GetFoodByQueryUseCase
import dev.masalimov.nutritiontracker.domain.food.usecase.SavedAndSearchResults
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var getFoodByQueryUseCase: GetFoodByQueryUseCase

    @MockK
    private lateinit var foodRepository: FoodRepository

    private lateinit var viewModel: FoodViewModel

    private val apple = Food(FoodId(1), "Apple", 52.0, 0.3, 0.2, 14.0)
    private val banana = Food(FoodId(2), "Banana", 89.0, 1.1, 0.3, 23.0)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = FoodViewModel(getFoodByQueryUseCase, foodRepository)
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial uiState is Initial before any subscription`() {
        assertThat(viewModel.uiState.value).isInstanceOf(FoodListUiState.Initial::class.java)
    }

    @Test
    fun `initial deletionState is Idle`() {
        assertThat(viewModel.deletionState.value).isInstanceOf(DeletionState.Idle::class.java)
    }

    // ── Query changes ─────────────────────────────────────────────────────────

    @Test
    fun `blank query emits Loading then Success with saved food`() = runTest {
        coEvery { getFoodByQueryUseCase.invoke("") } returns SavedAndSearchResults(
            savedFood = listOf(apple),
            searchFood = emptyList(),
        )

        viewModel.uiState.test {
            awaitItem() // Initial

            val loading = awaitItem()
            assertThat(loading).isInstanceOf(FoodListUiState.Loading::class.java)

            val success = awaitItem() as FoodListUiState.Success
            assertThat(success.savedFood).hasSize(1)
            assertThat(success.savedFood.first().name).isEqualTo("Apple")
            assertThat(success.searchedFood).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `non-blank query emits Loading then Success after debounce`() = runTest {
        coEvery { getFoodByQueryUseCase.invoke("ban") } returns SavedAndSearchResults(
            savedFood = emptyList(),
            searchFood = listOf(banana),
        )

        viewModel.uiState.test {
            awaitItem() // Initial from subscription
            advanceUntilIdle() // let blank query settle first

            viewModel.onQueryChanged("ban")
            // debounce is 300 ms — advance past it
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(300L)

            // consume whatever states appeared before we advanced time
            // (Loading for blank, Success for blank, Loading for "ban")
            var last: FoodListUiState = awaitItem()
            while (last !is FoodListUiState.Success || last.searchedFood.isEmpty()) {
                last = awaitItem()
            }
            assertThat(last.searchedFood.first().name).isEqualTo("Banana")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQueryChanged with query that matches no food emits empty Success`() = runTest {
        coEvery { getFoodByQueryUseCase.invoke("xyz") } returns SavedAndSearchResults(
            savedFood = emptyList(),
            searchFood = emptyList(),
        )

        viewModel.uiState.test {
            awaitItem() // Initial
            advanceUntilIdle()

            viewModel.onQueryChanged("xyz")
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(300L)

            var last: FoodListUiState = awaitItem()
            while (last !is FoodListUiState.Success) {
                last = awaitItem()
            }
            assertThat(last.savedFood).isEmpty()
            assertThat(last.searchedFood).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search error emits Error state`() = runTest {
        coEvery { getFoodByQueryUseCase.invoke("") } throws FoodSearchException()

        viewModel.uiState.test {
            awaitItem() // Initial

            var last: FoodListUiState = awaitItem()
            while (last !is FoodListUiState.Error) {
                last = awaitItem()
            }
            assertThat(last.errorMessage).isEqualTo("An error occurred while searching food")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Deletion ──────────────────────────────────────────────────────────────

    @Test
    fun `onItemDeleteClick transitions InProgress then Success`() = runTest {
        coEvery { foodRepository.deleteFood(1L) } returns Unit
        coEvery { getFoodByQueryUseCase.invoke(any()) } returns SavedAndSearchResults(emptyList(), emptyList())

        viewModel.deletionState.test {
            awaitItem() // Idle

            viewModel.onItemDeleteClick(1L)
            assertThat(awaitItem()).isInstanceOf(DeletionState.InProgress::class.java)
            assertThat(awaitItem()).isInstanceOf(DeletionState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onItemDeleteClick transitions InProgress then Error when delete fails`() = runTest {
        coEvery { foodRepository.deleteFood(1L) } throws RuntimeException("DB error")

        viewModel.deletionState.test {
            awaitItem() // Idle

            viewModel.onItemDeleteClick(1L)
            assertThat(awaitItem()).isInstanceOf(DeletionState.InProgress::class.java)
            assertThat(awaitItem()).isInstanceOf(DeletionState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeletionSnackbarDismissed resets deletionState to Idle`() = runTest {
        coEvery { foodRepository.deleteFood(1L) } returns Unit
        coEvery { getFoodByQueryUseCase.invoke(any()) } returns SavedAndSearchResults(emptyList(), emptyList())

        viewModel.onItemDeleteClick(1L)
        advanceUntilIdle()

        viewModel.onDeletionSnackbarDismissed()

        assertThat(viewModel.deletionState.value).isInstanceOf(DeletionState.Idle::class.java)
    }

    @Test
    fun `successful deletion calls foodRepository deleteFood with correct id`() = runTest {
        coEvery { foodRepository.deleteFood(42L) } returns Unit
        coEvery { getFoodByQueryUseCase.invoke(any()) } returns SavedAndSearchResults(emptyList(), emptyList())

        viewModel.onItemDeleteClick(42L)
        advanceUntilIdle()

        coVerify(exactly = 1) { foodRepository.deleteFood(42L) }
    }
}
