package com.collaborativeshoppinglist.core.logging

import android.util.Log

object AppLogger {
    private const val TAG = "ShoppingList"

    fun error(operation: String, throwable: Throwable) {
        Log.e(TAG, operation, throwable)
    }
}
