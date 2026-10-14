package com.varshuai.a1swaara.extractor

import android.content.Context
import org.schabi.newpipe.extractor.downloader.Downloader

internal fun createSystemDownloader(
    fallback: NewPipeDownloader,
    context: Context?,
): Downloader = fallback
