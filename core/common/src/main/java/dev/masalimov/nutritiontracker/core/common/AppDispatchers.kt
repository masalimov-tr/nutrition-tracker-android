package dev.masalimov.nutritiontracker.core.common

import kotlinx.coroutines.CoroutineDispatcher


class AppDispatchers(
    val ioDispatcher: CoroutineDispatcher,
    val mainDispatcher: CoroutineDispatcher,
    val defaultDispatcher: CoroutineDispatcher,
)