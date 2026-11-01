package com.example.data.remote

import android.util.Log

enum class OperationType {
    GET, LIST, CREATE, UPDATE, DELETE
}

fun handleFirestoreError(e: Exception, operation: OperationType, path: String) {
    Log.e(
        "FirestoreError",
        "Operation: ${operation.name}, Path: $path, Error: ${e.message}",
        e
    )
}
