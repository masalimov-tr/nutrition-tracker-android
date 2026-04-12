package dev.masalimov.nutritiontracker.core.common

import kotlinx.coroutines.CoroutineDispatcher


interface AppDispatchers {
    val ioDispatcher: CoroutineDispatcher
    val mainDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
}