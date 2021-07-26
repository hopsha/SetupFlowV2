package com.hopsha.setupflowv2.flow

interface Flow<T, R> {

    suspend fun run(input: T): R
}