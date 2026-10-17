@file:Suppress("TooManyFunctions")

package com.varshuai.a1swaara.ui.screens.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.LocalPlayerAwareWindowInsets
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.ui.components.SwaaraCyan
import com.varshuai.a1swaara.ui.components.SwaaraGlassContainer
import com.varshuai.a1swaara.ui.components.SwaaraPink
import com.varshuai.a1swaara.ui.components.SwaaraPurple
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.NumberFieldDialog
import com.varshuai.a1swaara.ui.components.themed.Scaffold
import com.varshuai.a1swaara.ui.components.themed.Slider
import com.varshuai.a1swaara.ui.components.themed.Switch
import com.varshuai.a1swaara.ui.components.themed.ValueSelectorDialog
import com.varshuai.a1swaara.ui.screens.GlobalRoutes
import com.varshuai.a1swaara.ui.screens.Route
import com.varshuai.a1swaara.utils.color
import com.varshuai.a1swaara.utils.secondary
import com.varshuai.a1swaara.utils.semiBold
import app.a1swaara.compose.persist.PersistMapCleanup
import app.a1swaara.compose.routing.RouteHandler
import app.a1swaara.core.ui.LocalAppearance
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Route
@Composable
fun SettingsScreen() {
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabChanged) = rememberSaveable { mutableIntStateOf(0) }

    PersistMapCleanup("settings/")

    RouteHandler {
        GlobalRoutes()

        Content {
            Scaffold(
                key = "settings",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChange = onTabChanged,
                tabColumnContent = {
                    tab(0, R.string.appearance, R.drawable.color_palette, canHide = false)
                    tab(1, R.string.player, R.drawable.play, canHide = false)
                    tab(2, R.string.cache, R.drawable.server, canHide = false)
                    tab(3, R.string.database, R.drawable.server, canHide = false)
                    tab(4, R.string.sync, R.drawable.sync, canHide = false)
                    tab(5, R.string.other, R.drawable.shapes, canHide = false)
                    tab(6, R.string.about, R.drawable.information, canHide = false)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> AppearanceSettings()
                        1 -> PlayerSettings()
                        2 -> CacheSettings()
                        3 -> DatabaseSettings()
                        4 -> SyncSettings()
                        5 -> OtherSettings()
                        6 -> About()
                    }
                }
            }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> EnumValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    noinline onValueSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    noinline valueText: @Composable (T) -> String = { it.name },
    noinline trailingContent: (@Composable () -> Unit)? = null
) = ValueSelectorSettingsEntry(
    title = title,
    selectedValue = selectedValue,
    values = enumValues<T>().toList().toImmutableList(),
    onValueSelect = onValueSelect,
    modifier = modifier,
    isEnabled = isEnabled,
    valueText = valueText,
    trailingContent = trailingContent
)

@Composable
fun <T> ValueSelectorSettingsEntry(
    title: String,
    selectedValue: T,
    values: ImmutableList<T>,
    onValueSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    isEnabled: Boolean = true,
    usePadding: Boolean = true,
    valueText: @Composable (T) -> String = { it.toString() },
    trailingContent: (@Composable () -> Unit)? = null
) {
    var isShowingDialog by remember { mutableStateOf(false) }

    if (isShowingDialog) ValueSelectorDialog(
        onDismiss = { isShowingDialog = false },
        title = title,
        selectedValue = selectedValue,
        values = values,
        onValueSelect = onValueSelect,
        valueText = valueText
    )

    SettingsEntry(
        modifier = modifier,
        title = title,
        text = text ?: valueText(selectedValue),
        onClick = { isShowingDialog = true },
        isEnabled = isEnabled,
        trailingContent = trailingContent,
        usePadding = usePadding
    )
}

@Composable
fun SwitchSettingsEntry(
    title: String,
    text: String?,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    usePadding: Boolean = true
) = SettingsEntry(
    modifier = modifier,
    title = title,
    text = text,
    onClick = { onCheckedChange(!isChecked) },
    isEnabled = isEnabled,
    usePadding = usePadding
) {
    Switch(isChecked = isChecked)
}

@Composable
fun SliderSettingsEntry(
    title: String,
    text: String,
    state: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    onSlide: (Float) -> Unit = { },
    onSlideComplete: () -> Unit = { },
    toDisplay: @Composable (Float) -> String = { it.toString() },
    steps: Int = 0,
    isEnabled: Boolean = true,
    usePadding: Boolean = true
) = Column(modifier = modifier) {
    SettingsEntry(
        title = title,
        text = "$text (${toDisplay(state)})",
        onClick = {},
        isEnabled = isEnabled,
        usePadding = usePadding
    )

    Slider(
        state = state,
        setState = onSlide,
        onSlideComplete = onSlideComplete,
        range = range,
        steps = steps,
        modifier = Modifier
            .height(36.dp)
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    )
}

@Composable
inline fun IntSettingsEntry(
    title: String,
    text: String,
    currentValue: Int,
    crossinline setValue: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    defaultValue: Int = currentValue
) {
    var isShowingDialog by remember { mutableStateOf(false) }

    if (isShowingDialog) NumberFieldDialog(
        onDismiss = { isShowingDialog = false },
        initialValue = currentValue,
        defaultValue = defaultValue,
        convert = { it.toIntOrNull() },
        onAccept = { setValue(it) },
        range = range
    )

    SettingsEntry(
        modifier = modifier,
        title = title,
        text = text,
        onClick = { isShowingDialog = true },
        isEnabled = isEnabled
    )
}

@Composable
fun SettingsEntry(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    isEnabled: Boolean = true,
    usePadding: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) = Row(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .clickable(enabled = isEnabled, onClick = onClick)
        .alpha(if (isEnabled) 1f else 0.45f)
        .padding(horizontal = 16.dp, vertical = 12.dp)
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (text != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp
            )
        }
    }

    trailingContent?.invoke()
}

@Composable
fun SettingsDescription(
    text: String,
    modifier: Modifier = Modifier,
    important: Boolean = false
) {
    Text(
        text = text,
        color = if (important) SwaaraPink else Color.White.copy(alpha = 0.50f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsEntryGroupText(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        color = SwaaraCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = modifier
            .padding(start = 6.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsGroupSpacer(modifier: Modifier = Modifier) = Spacer(modifier = modifier.height(18.dp))

@Composable
fun SettingsCategoryScreen(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    scrollState: ScrollState? = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0xFF07070B))
            .fillMaxSize()
            .let { if (scrollState != null) it.verticalScroll(state = scrollState) else it }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 120.dp)
    ) {
        if (description != null) {
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
            SettingsGroupSpacer()
        }

        content()
    }
}

@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    important: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) = Column(modifier = modifier.fillMaxWidth()) {
    SettingsEntryGroupText(title = title)

    if (description != null) {
        SettingsDescription(
            text = description,
            important = important
        )
    }

    // Wrap Group Entries inside a Frosted Glass Container Card
    SwaaraGlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color(0xFF131322).copy(alpha = 0.85f),
        borderColor = Color.White.copy(alpha = 0.10f),
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }

    SettingsGroupSpacer()
}
