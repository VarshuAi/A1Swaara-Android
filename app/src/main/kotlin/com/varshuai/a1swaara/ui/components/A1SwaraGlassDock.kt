package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import com.varshuai.a1swaara.R
import app.a1swaara.core.ui.LocalAppearance

data class A1SwaraNavItem(
    val title: String,
    val iconId: Int,
    val tabIndex: Int
)

@Composable
fun A1SwaraGlassDock(
    currentTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        A1SwaraNavItem("Home", R.drawable.sparkles, 0),
        A1SwaraNavItem("Search", R.drawable.search, 1),
        A1SwaraNavItem("Library", R.drawable.playlist, 2)
    )

    val typography = LocalAppearance.current.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFF1DB954))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xEE16161F))
                .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(32.dp))
                .height(60.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { navItem ->
                val isSelected = currentTabIndex == navItem.tabIndex
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF1DB954) else Color(0xFF8E8E9A),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "tab_color"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onTabSelected(navItem.tabIndex) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(navItem.iconId),
                        contentDescription = navItem.title,
                        colorFilter = ColorFilter.tint(contentColor),
                        modifier = Modifier.size(22.dp)
                    )

                    if (isSelected) {
                        BasicText(
                            text = navItem.title,
                            style = typography.xs.copy(
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
