package com.varshuai.a1swaara.ui.screens.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R
import coil3.compose.AsyncImage

private val SwaaraObsidian = Color(0xFF08080C)
private val SwaaraDeepSurface = Color(0xFF111119)
private val SwaaraElevatedSurface = Color(0xFF191924)
private val SwaaraMagenta = Color(0xFFFF2DAA)
private val SwaaraViolet = Color(0xFF8B35FF)
private val SwaaraCyan = Color(0xFF20CFFF)
private val SwaaraPrimaryText = Color(0xFFF1F1F5)
private val SwaaraSecondaryText = Color(0xFF8E8EA0)

data class SwaaraLangItem(
    val id: String,
    val name: String,
    val nativeScript: String,
    val avatarUrl: String
)

@Composable
fun SwaaraOnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    val context = LocalContext.current

    val languages = remember {
        listOf(
            SwaaraLangItem("kannada", "Kannada", "ಕನ್ನಡ", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/a/a2/Vijay_Prakash.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("hindi", "Hindi", "हिंदी", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/d/d2/Arijit_Singh_at_Mirchi_Music_Awards_2014.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("english", "English", "English", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/b/b1/Taylor_Swift_at_the_2023_MTV_Video_Music_Awards_4.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("tamil", "Tamil", "தமிழ்", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/d/d9/Anirudh_Ravichander_at_Audio_Launch.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("telugu", "Telugu", "తెలుగు", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/9/91/Sid_Sriram_at_an_event.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("punjabi", "Punjabi", "ਪੰਜਾਬੀ", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/c/c5/Diljit_Dosanjh_at_IIFA_2017.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("malayalam", "Malayalam", "മലയാളം", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/9/94/K._J._Yesudas.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("marathi", "Marathi", "मराठी", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/a/ad/Ajay-Atul.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("bengali", "Bengali", "বাংলা", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/b/be/Shreya_Ghoshal_at_Filmfare_Awards_South.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("gujarati", "Gujarati", "ગુજરાતી", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/1/1a/Alka_Yagnik_in_2017.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("bhojpuri", "Bhojpuri", "भोजपुरी", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/4/4b/Pawan_Singh.jpg&w=400&h=400&fit=cover&output=webp"),
            SwaaraLangItem("haryanvi", "Haryanvi", "हरियाणवी", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/9/92/Sonu_Nigam.jpg&w=400&h=400&fit=cover&output=webp")
        )
    }

    val selectedLanguages = remember { mutableStateListOf("kannada") }
    var userNameInput by remember { mutableStateOf("") }

    val completeWelcome: () -> Unit = {
        val finalName = if (userNameInput.isNotBlank()) userNameInput.trim() else "Music Lover"
        val prefs = context.getSharedPreferences("swaara_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_name", finalName)
            .putStringSet("selected_languages", selectedLanguages.toSet())
            .putString("active_language", selectedLanguages.firstOrNull() ?: "kannada")
            .putBoolean("onboarding_completed", true)
            .commit()
        onOnboardingFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SwaaraObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Hero Brand Showcase
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.8f),
                        spotColor = SwaaraMagenta.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(SwaaraElevatedSurface)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(SwaaraMagenta, SwaaraViolet, SwaaraCyan)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.a1_swaara_logo),
                    contentDescription = "A1 Swaara",
                    modifier = Modifier.size(42.dp)
                )
            }

            Column {
                Text(
                    text = "Welcome to A1 Swaara",
                    color = SwaaraPrimaryText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "High-Fidelity Sovereign Audio Experience",
                    color = SwaaraCyan.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frosted Glass Name Input Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SwaaraDeepSurface.copy(alpha = 0.92f))
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SwaaraElevatedSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = null,
                        tint = SwaaraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = userNameInput,
                    onValueChange = { userNameInput = it },
                    textStyle = TextStyle(
                        color = SwaaraPrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(SwaaraCyan),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (userNameInput.isEmpty()) {
                            Text(
                                text = "Enter your display name (e.g. Varshan)",
                                color = SwaaraSecondaryText.copy(alpha = 0.6f),
                                fontSize = 13.5.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose Your Sonic Languages",
                color = SwaaraPrimaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${selectedLanguages.size} selected",
                color = SwaaraMagenta,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Languages Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(languages, key = { it.id }) { lang ->
                val isSelected = selectedLanguages.contains(lang.id)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSelected) SwaaraElevatedSurface
                            else SwaaraDeepSurface
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet))
                            else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                if (selectedLanguages.size > 1) {
                                    selectedLanguages.remove(lang.id)
                                }
                            } else {
                                selectedLanguages.add(lang.id)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Artist Portrait with fallback
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E2E))
                                .border(
                                    1.dp,
                                    if (isSelected) SwaaraCyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = lang.avatarUrl,
                                contentDescription = lang.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lang.name,
                                color = SwaaraPrimaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = lang.nativeScript,
                                color = if (isSelected) SwaaraCyan else SwaaraSecondaryText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Selection Indicator
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.sparkles),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Get Started Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(27.dp),
                    ambientColor = Color.Black.copy(alpha = 0.7f),
                    spotColor = SwaaraMagenta.copy(alpha = 0.55f)
                )
                .clip(RoundedCornerShape(27.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            SwaaraMagenta,
                            SwaaraViolet,
                            SwaaraCyan.copy(alpha = 0.8f)
                        )
                    )
                )
                .clickable { completeWelcome() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Start Listening to A1 Swaara",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}
