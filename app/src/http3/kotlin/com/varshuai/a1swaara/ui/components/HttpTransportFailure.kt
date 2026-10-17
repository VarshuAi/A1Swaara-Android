package com.varshuai.a1swaara.ui.components

import com.varshuai.a1swaara.utils.isHttp3TransportFailure

internal fun Throwable.isHttpTransportFailure(): Boolean = isHttp3TransportFailure()
