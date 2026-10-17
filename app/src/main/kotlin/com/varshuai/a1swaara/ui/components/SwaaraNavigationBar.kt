package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R

data class SwaaraNavigationItem(
    val title: String,
    val iconId: Int,
    val index: Int
)

@Composable
fun SwaaraNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            SwaaraNavigationItem("Home", R.drawable.disc, 0),
            SwaaraNavigationItem("Search", R.drawable.search, 1),
            SwaaraNavigationItem("Library", R.drawable.library, 2),
            SwaaraNavigationItem("Downloads", R.drawable.download, 3),
            SwaaraNavigationItem("Settings", R.drawable.settings, 4)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SwaaraObsidian)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle Top Hairline Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedIndex == item.index
                    val interactionSource = remember { MutableInteractionSource() }

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) SwaaraPrimaryText else SwaaraSecondaryText,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconColor"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) SwaaraElevatedSurface else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pillBgColor"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onTabSelected(item.index)
                            }
                            .background(pillBgColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconId),
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = item.title,
                            color = iconColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
