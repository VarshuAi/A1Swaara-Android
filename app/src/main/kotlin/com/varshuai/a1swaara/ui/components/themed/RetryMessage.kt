package com.varshuai.a1swaara.ui.components.themed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.utils.center
import com.varshuai.a1swaara.utils.secondary
import app.a1swaara.core.ui.LocalAppearance

@Composable
fun RetryMessage(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (_, typography) = LocalAppearance.current

    BasicText(
        text = stringResource(R.string.error_message),
        style = typography.s.secondary.center,
        modifier = modifier
            .clickable(onClick = onRetry)
            .padding(horizontal = 16.dp, vertical = 32.dp),
    )
}
