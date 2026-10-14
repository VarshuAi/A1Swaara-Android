@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.varshuai.a1swaara.service

import android.content.Context
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import java.util.concurrent.TimeUnit

private val playbackOkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(BrotliInterceptor)
        .build()
}

internal fun playbackHttpDataSourceFactory(context: Context): HttpDataSource.Factory =
    OkHttpDataSource.Factory(playbackOkHttpClient)


