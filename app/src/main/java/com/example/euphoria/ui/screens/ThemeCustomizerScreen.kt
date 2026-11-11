package com.example.euphoria.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.euphoria.data.local.entity.UserCustomThemeEntity
import com.example.euphoria.theme.BuiltInThemes
import com.example.euphoria.theme.EuphoriaFontFamily
import com.example.euphoria.theme.EuphoriaThemeData
import com.example.euphoria.theme.PlayerThemeStyle
import com.example.euphoria.theme.ThemeMode
import com.example.euphoria.theme.WallpaperOverlayMode
import com.example.euphoria.theme.WaveformStyle
import com.example.euphoria.ui.components.GlassCard
import com.example.euphoria.ui.components.WaveformVisualizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizerScreen(
    currentTheme: EuphoriaThemeData,
    themeMode: ThemeMode,
    customThemes: List<UserCustomThemeEntity>,
    onSelectTheme: (EuphoriaThemeData) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onApplyWallpaperTheme: (Uri, WallpaperOverlayMode, Float, Float, Float, Float) -> Unit,
    onSaveCustomTheme: (String) -> Unit,
    onDeleteCustomTheme: (Long) -> Unit,
    onUpdatePlayerStyle: (PlayerThemeStyle) -> Unit,
    onUpdateWaveformStyle: (WaveformStyle) -> Unit,
    onUpdateFont: (EuphoriaFontFamily) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Themes & Colors, 1: Wallpaper Theme, 2: Theme Editor, 3: Layout & Fonts
    var showSaveDialog by remember { mutableStateOf(false) }
    var newThemeName by remember { mutableStateOf("") }

    // Wallpaper customizer states
    var selectedWallpaperUri by remember { mutableStateOf<Uri?>(null) }
    var wallpaperOverlayMode by remember { mutableStateOf(WallpaperOverlayMode.GRADIENT) }
    var wallpaperBrightness by remember { mutableFloatStateOf(0.5f) }
    var wallpaperBlur by remember { mutableFloatStateOf(8f) }
    var wallpaperAlpha by remember { mutableFloatStateOf(0.75f) }
    var wallpaperSaturation by remember { mutableFloatStateOf(1.0f) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedWallpaperUri = uri
            onApplyWallpaperTheme(
                uri,
                wallpaperOverlayMode,
                wallpaperBrightness,
                wallpaperBlur,
                wallpaperAlpha,
                wallpaperSaturation
            )
            Toast.makeText(context, "Wallpaper & smart palette applied!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("theme_customizer_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_back_theme")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = currentTheme.textPrimary
                    )
                }

                Text(
                    text = "Theme & Personalization",
                    color = currentTheme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.testTag("btn_save_theme")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Theme",
                        tint = currentTheme.accentColor
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = currentTheme.accentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = currentTheme.accentColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Presets", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Wallpaper", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Editor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Style & Fonts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: 15 Built-in themes + Color quick circles + Theme modes
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Appearance Mode row (Dark, Light, AMOLED, System)
                        Text(
                            text = "Appearance Mode",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val isSelected = themeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectThemeMode(mode) },
                                    label = { Text(mode.name.replace("_", " ")) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = currentTheme.accentColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Color Palette Quick-Pill circles (matching Screenshot 1 middle phone)
                        Text(
                            text = "Color Palette",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BuiltInThemes.all.forEach { themeItem ->
                                val isSelected = currentTheme.id == themeItem.id
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.accentColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onSelectTheme(themeItem) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 15 Distinct Themes Cards Grid
                        Text(
                            text = "15 Built-in Signature Themes",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        BuiltInThemes.all.chunked(2).forEach { rowThemes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowThemes.forEach { themeItem ->
                                    val isSelected = currentTheme.id == themeItem.id
                                    GlassCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectTheme(themeItem) },
                                        backgroundColor = themeItem.cardColor,
                                        cardAlpha = 0.85f,
                                        cornerRadius = 16.dp,
                                        borderColor = if (isSelected) themeItem.accentColor else Color.White.copy(alpha = 0.1f),
                                        borderWidth = if (isSelected) 2.dp else 1.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(themeItem.accentColor)
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = themeItem.accentColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = themeItem.name,
                                                color = themeItem.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (themeItem.isAmoled) "AMOLED Black" else if (themeItem.isDark) "Dark Mode" else "Light Mode",
                                                color = themeItem.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Saved custom themes if any
                        if (customThemes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Your Saved Custom Themes",
                                color = currentTheme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            customThemes.forEach { saved ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassCard(
                                        modifier = Modifier.weight(1f),
                                        backgroundColor = currentTheme.surfaceColor,
                                        cornerRadius = 14.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = saved.name,
                                                color = currentTheme.textPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { onDeleteCustomTheme(saved.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                1 -> {
                    // TAB 1: My Wallpaper Theme (Import ANY image, extract palette, adjust blur/brightness/alpha/saturation)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "My Wallpaper Theme",
                            color = currentTheme.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Import any picture from your gallery to generate a custom complementary theme and dynamic background.",
                            color = currentTheme.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_pick_wallpaper"),
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image from Gallery")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Preview box
                        if (currentTheme.wallpaperUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, currentTheme.accentColor, RoundedCornerShape(20.dp))
                            ) {
                                AsyncImage(
                                    model = currentTheme.wallpaperUri,
                                    contentDescription = "Wallpaper Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                            )
                                        )
                                )
                                Text(
                                    text = "Active Wallpaper Theme",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Overlay Mode selector
                            Text(
                                text = "Overlay Mode",
                                color = currentTheme.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WallpaperOverlayMode.entries.forEach { mode ->
                                    val isSel = wallpaperOverlayMode == mode
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            wallpaperOverlayMode = mode
                                            val u = Uri.parse(currentTheme.wallpaperUri)
                                            onApplyWallpaperTheme(
                                                u, mode, wallpaperBrightness, wallpaperBlur, wallpaperAlpha, wallpaperSaturation
                                            )
                                        },
                                        label = { Text(mode.displayName) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = currentTheme.accentColor,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sliders: Brightness, Blur, Transparency
                            Text("Brightness: ${(wallpaperBrightness * 100).toInt()}%", color = currentTheme.textSecondary, fontSize = 12.sp)
                            Slider(
                                value = wallpaperBrightness,
                                onValueChange = {
                                    wallpaperBrightness = it
                                    val u = Uri.parse(currentTheme.wallpaperUri)
                                    onApplyWallpaperTheme(u, wallpaperOverlayMode, it, wallpaperBlur, wallpaperAlpha, wallpaperSaturation)
                                },
                                colors = SliderDefaults.colors(thumbColor = currentTheme.accentColor, activeTrackColor = currentTheme.accentColor)
                            )

                            Text("Blur Intensity: ${wallpaperBlur.toInt()} dp", color = currentTheme.textSecondary, fontSize = 12.sp)
                            Slider(
                                value = wallpaperBlur,
                                onValueChange = {
                                    wallpaperBlur = it
                                    val u = Uri.parse(currentTheme.wallpaperUri)
                                    onApplyWallpaperTheme(u, wallpaperOverlayMode, wallpaperBrightness, it, wallpaperAlpha, wallpaperSaturation)
                                },
                                valueRange = 0f..25f,
                                colors = SliderDefaults.colors(thumbColor = currentTheme.accentColor, activeTrackColor = currentTheme.accentColor)
                            )

                            Text("Opacity / Transparency: ${(wallpaperAlpha * 100).toInt()}%", color = currentTheme.textSecondary, fontSize = 12.sp)
                            Slider(
                                value = wallpaperAlpha,
                                onValueChange = {
                                    wallpaperAlpha = it
                                    val u = Uri.parse(currentTheme.wallpaperUri)
                                    onApplyWallpaperTheme(u, wallpaperOverlayMode, wallpaperBrightness, wallpaperBlur, it, wallpaperSaturation)
                                },
                                colors = SliderDefaults.colors(thumbColor = currentTheme.accentColor, activeTrackColor = currentTheme.accentColor)
                            )
                        } else {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                backgroundColor = currentTheme.surfaceColor,
                                cornerRadius = 18.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = currentTheme.accentColor,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No custom wallpaper loaded",
                                        color = currentTheme.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pick an artwork or photo from your gallery to create a personalized theme.",
                                        color = currentTheme.textSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                2 -> {
                    // TAB 2: Visual Theme Editor
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Advanced Visual Theme Editor",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Fine-tune UI colors, card transparency, and corner radiuses in real time.",
                            color = currentTheme.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live mini preview
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = currentTheme.cardColor,
                            cardAlpha = currentTheme.cardAlpha,
                            cornerRadius = currentTheme.cornerRadiusDp.dp,
                            borderColor = currentTheme.accentColor.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Live Theme Preview", color = currentTheme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Subtitle text in secondary color", color = currentTheme.textSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                WaveformVisualizer(
                                    amplitudes = List(24) { 0.5f },
                                    isPlaying = true,
                                    primaryColor = currentTheme.accentColor,
                                    secondaryColor = currentTheme.secondaryAccentColor,
                                    style = currentTheme.waveformStyle,
                                    modifier = Modifier.height(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Accent Action Button")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Accent Color picker chips
                        Text("Accent Color", color = currentTheme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        val accentChoices = listOf(
                            Color(0xFFC743FF), Color(0xFFFF2A55), Color(0xFF00F0FF), Color(0xFF00B4D8),
                            Color(0xFF10B981), Color(0xFFFF5A36), Color(0xFF70D6FF), Color(0xFFB388FF),
                            Color(0xFF2DD4BF), Color(0xFFD4A373), Color(0xFFE50914), Color(0xFF6366F1)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            accentChoices.forEach { col ->
                                val isSel = currentTheme.accentColor == col
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (isSel) 3.dp else 1.dp,
                                            color = if (isSel) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onSelectTheme(currentTheme.copy(accentColor = col, waveformColor = col))
                                        }
                                )
                            }
                        }

                        // Card Opacity slider
                        Text("Card Transparency: ${(currentTheme.cardAlpha * 100).toInt()}%", color = currentTheme.textSecondary, fontSize = 13.sp)
                        Slider(
                            value = currentTheme.cardAlpha,
                            onValueChange = {
                                onSelectTheme(currentTheme.copy(cardAlpha = it))
                            },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = currentTheme.accentColor, activeTrackColor = currentTheme.accentColor)
                        )

                        // Corner Radius slider
                        Text("Corner Radius: ${currentTheme.cornerRadiusDp.toInt()} dp", color = currentTheme.textSecondary, fontSize = 13.sp)
                        Slider(
                            value = currentTheme.cornerRadiusDp,
                            onValueChange = {
                                onSelectTheme(currentTheme.copy(cornerRadiusDp = it))
                            },
                            valueRange = 8f..32f,
                            colors = SliderDefaults.colors(thumbColor = currentTheme.accentColor, activeTrackColor = currentTheme.accentColor)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Current Theme As Custom")
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                3 -> {
                    // TAB 3: Style & Fonts (10 Player layouts, 5 Waveform styles, Typography)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Now Playing Layouts (10 Designs)",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PlayerThemeStyle.entries.forEach { style ->
                            val isSel = currentTheme.playerThemeStyle == style
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onUpdatePlayerStyle(style) },
                                backgroundColor = currentTheme.surfaceColor,
                                cornerRadius = 14.dp,
                                borderColor = if (isSel) currentTheme.accentColor else Color.White.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = style.displayName,
                                        color = if (isSel) currentTheme.accentColor else currentTheme.textPrimary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = currentTheme.accentColor)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Waveform Visualizer Styles",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        WaveformStyle.entries.forEach { style ->
                            val isSel = currentTheme.waveformStyle == style
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onUpdateWaveformStyle(style) },
                                backgroundColor = currentTheme.surfaceColor,
                                cornerRadius = 14.dp,
                                borderColor = if (isSel) currentTheme.accentColor else Color.White.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = style.displayName,
                                        color = if (isSel) currentTheme.accentColor else currentTheme.textPrimary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = currentTheme.accentColor)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Typography / Fonts",
                            color = currentTheme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        EuphoriaFontFamily.entries.forEach { font ->
                            val isSel = currentTheme.fontStyle == font
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onUpdateFont(font) },
                                backgroundColor = currentTheme.surfaceColor,
                                cornerRadius = 14.dp,
                                borderColor = if (isSel) currentTheme.accentColor else Color.White.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = font.displayName,
                                        color = if (isSel) currentTheme.accentColor else currentTheme.textPrimary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = currentTheme.accentColor)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a name for your personalized theme:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newThemeName,
                        onValueChange = { newThemeName = it },
                        placeholder = { Text("e.g. My Cosmic Vibe") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newThemeName.ifBlank { "Custom Theme ${System.currentTimeMillis() % 1000}" }
                        onSaveCustomTheme(name)
                        showSaveDialog = false
                        newThemeName = ""
                        Toast.makeText(context, "Theme saved!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
