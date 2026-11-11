package com.example.euphoria.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.euphoria.player.AudioFocusMode
import com.example.euphoria.player.FadeSettings
import com.example.euphoria.player.SleepTimerState
import com.example.euphoria.theme.EuphoriaThemeData
import com.example.euphoria.theme.ThemeMode
import com.example.euphoria.ui.components.GlassCard
import java.util.Locale

@Composable
fun SettingsScreen(
    theme: EuphoriaThemeData,
    themeMode: ThemeMode,
    fadeSettings: FadeSettings,
    audioFocusMode: AudioFocusMode,
    sleepTimerState: SleepTimerState,
    totalSongs: Int,
    totalAlbums: Int,
    totalArtists: Int,
    totalPlaylists: Int,
    onOpenThemeCustomizer: () -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onUpdateFadeSettings: (FadeSettings) -> Unit,
    onUpdateAudioFocus: (AudioFocusMode) -> Unit,
    onStartSleepTimer: (minutes: Int, fadeOut: Boolean, pauseWhenPaused: Boolean) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onRescanLibrary: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }

                Text(
                    text = "Settings",
                    color = theme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 1: Appearance & Personalization
                SettingsSectionHeader("Appearance & Customization", Icons.Default.Palette, theme)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenThemeCustomizer() },
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Theme Engine & Personalization", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("15 Signature Themes, My Wallpaper Engine, Visual Editor", color = theme.textSecondary, fontSize = 12.sp)
                        }
                        Text("Open ›", color = theme.accentColor, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 2: Audio Playback Fade & Effects
                SettingsSectionHeader("Audio Fade Effects", Icons.Default.GraphicEq, theme)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fade In on Start / Resume", color = theme.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Gradually ramps volume for seamless playback", color = theme.textSecondary, fontSize = 12.sp)
                            }
                            Switch(
                                checked = fadeSettings.fadeInEnabled,
                                onCheckedChange = { onUpdateFadeSettings(fadeSettings.copy(fadeInEnabled = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fade Out on Pause / Track End", color = theme.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Prevents harsh stopping cuts", color = theme.textSecondary, fontSize = 12.sp)
                            }
                            Switch(
                                checked = fadeSettings.fadeOutEnabled,
                                onCheckedChange = { onUpdateFadeSettings(fadeSettings.copy(fadeOutEnabled = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Fade Duration: ${String.format(Locale.getDefault(), "%.1f", fadeSettings.fadeDurationSec)}s", color = theme.textSecondary, fontSize = 12.sp)
                        Slider(
                            value = fadeSettings.fadeDurationSec,
                            onValueChange = { onUpdateFadeSettings(fadeSettings.copy(fadeDurationSec = it)) },
                            valueRange = 0.5f..5.0f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = theme.accentColor, activeTrackColor = theme.accentColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Audio Playback Focus
                SettingsSectionHeader("Audio Playback Focus", Icons.Default.Speed, theme)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AudioFocusMode.entries.forEach { mode ->
                            val isSel = audioFocusMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdateAudioFocus(mode) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSel) theme.accentColor else theme.textPrimary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSel) {
                                    Text("✓", color = theme.accentColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 4: Sleep Timer
                SettingsSectionHeader("Sleep Timer", Icons.Default.Timer, theme)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (sleepTimerState.isActive) {
                            val remMin = sleepTimerState.remainingSeconds / 60
                            val remSec = sleepTimerState.remainingSeconds % 60
                            Text(
                                text = "Timer Active: %d:%02d remaining".format(remMin, remSec),
                                color = theme.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onCancelSleepTimer,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                            ) {
                                Text("Cancel Sleep Timer")
                            }
                        } else {
                            Text("Playback will automatically stop after chosen time.", color = theme.textSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showSleepTimerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                            ) {
                                Text("Set Sleep Timer")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 5: Android Home-Screen Widgets
                SettingsSectionHeader("Android Home-Screen Widgets", Icons.Default.Widgets, theme)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("5 Genuine Native Android Widgets Installed:", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Widget 1: Compact Player (Artwork, title, prev, play/pause, next)", color = theme.textSecondary, fontSize = 12.sp)
                        Text("• Widget 2: Premium Player (Large artwork, title, progress bar, fav)", color = theme.textSecondary, fontSize = 12.sp)
                        Text("• Widget 3: Minimal Widget (Horizontal mini bar)", color = theme.textSecondary, fontSize = 12.sp)
                        Text("• Widget 4: Artwork Widget (Artwork hero frame with overlay)", color = theme.textSecondary, fontSize = 12.sp)
                        Text("• Widget 5: Extended Player (Full playback controls, progress, shuffle, repeat)", color = theme.textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("To add to your home screen: Long-press your home screen launcher, select 'Widgets', choose EUPHORIA, and pick your preferred style!", color = theme.accentColor, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 6: Library & Statistics
                SettingsSectionHeader("Library & Statistics", Icons.Default.LibraryMusic, theme)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("$totalSongs Tracks • $totalAlbums Albums • $totalArtists Artists • $totalPlaylists Playlists", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onRescanLibrary()
                                Toast.makeText(context, "Scanning device media...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rescan Device Music")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 7: About EUPHORIA
                SettingsSectionHeader("About EUPHORIA", Icons.Default.Info, theme)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = theme.surfaceColor,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Application: EUPHORIA Music Player", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Developer: Made by Adham Mohamed Ali", color = theme.accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Version: 1.0 (Native Android Build)", color = theme.textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Built with Jetpack Compose, Material 3, AndroidX MediaSession, Room Database, and native Android AppWidgets. Designed for premium high-fidelity offline audio playback and infinite personalization.",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    if (showSleepTimerDialog) {
        val presets = listOf(5, 10, 15, 20, 30, 45, 60, 90)
        var fadeOut by remember { mutableStateOf(true) }
        var pauseWhenPaused by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Select Sleep Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = fadeOut, onCheckedChange = { fadeOut = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gradually lower volume before stopping", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    presets.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { min ->
                                Button(
                                    onClick = {
                                        onStartSleepTimer(min, fadeOut, pauseWhenPaused)
                                        showSleepTimerDialog = false
                                        Toast.makeText(context, "Sleep timer set for $min minutes", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
                                ) {
                                    Text("${min}m", fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, theme: EuphoriaThemeData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
