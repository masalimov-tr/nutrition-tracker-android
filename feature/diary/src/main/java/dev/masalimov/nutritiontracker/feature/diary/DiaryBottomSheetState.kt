package dev.masalimov.nutritiontracker.feature.diary

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable


@OptIn(ExperimentalMaterial3Api::class)
@Stable
class DiaryBottomSheetState (
    private val showSheetState: MutableState<Boolean>,
    private val bottomSheetState: SheetState
) {
    val isExtended: Boolean
        get() = showSheetState.value

    val sheetState: SheetState
        get() = bottomSheetState

    fun open() {
        showSheetState.value = true
    }

    fun close() {
        showSheetState.value = false
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDiaryBottomSheetState(): DiaryBottomSheetState {
    val showSheetState = rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    return DiaryBottomSheetState(
        showSheetState,
        bottomSheetState,
    )
}