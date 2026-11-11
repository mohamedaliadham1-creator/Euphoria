package com.example.euphoria.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import com.example.euphoria.data.model.Song
import com.example.euphoria.player.RepeatMode
import com.example.euphoria.theme.EuphoriaThemeData
import com.example.euphoria.theme.PlayerThemeStyle
import com.example.euphoria.ui.components.GlassCard
import com.example.euphoria.ui.components.WaveformVisualizer
import java.util.Locale

@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPosMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    playbackSpeed: Float,
    volume: Float,
    isFavorite: Boolean,
    amplitudes: List<Float>,
    theme: EuphoriaThemeData,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetVolume: (Float) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenMetadataEditor: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showVolumeDialog by remember { mutableStateOf(false) }
    var showLyricsDialog by remember { mutableStateOf(false) }

    // Vinyl rotation animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rot")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    // Pulsing glowing ring
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_now_playing")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Playing Now",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("btn_now_playing_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = theme.textPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Playback Speed (${playbackSpeed}x)") },
                            onClick = {
                                showMenu = false
                                showSpeedDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Volume Control") },
                            onClick = {
                                showMenu = false
                                showVolumeDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Lyrics") },
                            onClick = {
                                showMenu = false
                                showLyricsDialog = true
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep Timer") },
                            onClick = {
                                showMenu = false
                                onOpenSleepTimer()
                            },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
                        )
                        if (song != null) {
                            DropdownMenuItem(
                                text = { Text("Edit Song Metadata") },
                                onClick = {
                                    showMenu = false
                                    onOpenMetadataEditor(song)
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Album Artwork with glowing ring inspired by screenshot 1 (right phone)
            val isCircular = theme.playerThemeStyle == PlayerThemeStyle.CIRCULAR_VINYL ||
                    theme.playerThemeStyle == PlayerThemeStyle.NEON_WAVE

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Neon glowing ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(if (isCircular) CircleShape else RoundedCornerShape(32.dp))
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    theme.accentColor.copy(alpha = glowPulse),
                                    theme.secondaryAccentColor.copy(alpha = glowPulse * 0.8f),
                                    Color.Transparent,
                                    theme.accentColor.copy(alpha = glowPulse)
                                )
                            ),
                            shape = if (isCircular) CircleShape else RoundedCornerShape(32.dp)
                        )
                )

                // Artwork image
                val artModifier = Modifier
                    .fillMaxSize(0.92f)
                    .clip(if (isCircular) CircleShape else RoundedCornerShape(26.dp))
                    .rotate(if (isCircular && isPlaying) rotationAngle else 0f)

                if (song?.albumArtUri != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = "Album Cover",
                        contentScale = ContentScale.Crop,
                        modifier = artModifier
                    )
                } else {
                    Box(
                        modifier = artModifier.background(
                            Brush.linearGradient(
                                listOf(
                                    theme.cardColor,
                                    theme.accentColor.copy(alpha = 0.7f),
                                    theme.secondaryAccentColor
                                )
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = song?.title?.take(1)?.uppercase() ?: "E",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 64.sp
                            )
                            Text(
                                text = "EUPHORIA",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 3.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Song Title & Artist
            Text(
                text = song?.title ?: "Select a song",
                color = theme.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${song?.artist ?: "EUPHORIA"} • ${song?.album ?: "Offline"}",
                color = theme.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Sub-actions pill row: Add to Playlist, Favorite, Queue
            GlassCard(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp)),
                backgroundColor = theme.surfaceColor,
                cardAlpha = 0.6f,
                cornerRadius = 30.dp,
                borderColor = Color.White.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (song != null) onAddToPlaylist(song) },
                        modifier = Modifier.testTag("btn_add_to_playlist")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to playlist",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("btn_toggle_favorite")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) theme.accentColor else theme.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    IconButton(
                        onClick = onOpenQueue,
                        modifier = Modifier.testTag("btn_open_queue")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Live Animated Glowing Waveform Visualizer
            WaveformVisualizer(
                amplitudes = amplitudes,
                isPlaying = isPlaying,
                primaryColor = theme.accentColor,
                secondaryColor = theme.secondaryAccentColor,
                style = theme.waveformStyle,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Progress Bar
            var isDraggingSlider by remember { mutableStateOf(false) }
            var sliderDragValue by remember { mutableFloatStateOf(0f) }

            val totalDur = durationMs.coerceAtLeast(1L)
            val currentProgress = if (isDraggingSlider) {
                sliderDragValue
            } else {
                (currentPosMs.toFloat() / totalDur.toFloat()).coerceIn(0f, 1f)
            }

            Slider(
                value = currentProgress,
                onValueChange = {
                    isDraggingSlider = true
                    sliderDragValue = it
                },
                onValueChangeFinished = {
                    val targetMs = (sliderDragValue * totalDur).toLong()
                    onSeek(targetMs)
                    isDraggingSlider = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = theme.accentColor,
                    activeTrackColor = theme.accentColor,
                    inactiveTrackColor = theme.accentColor.copy(alpha = 0.25f)
                )
            )

            // Timestamps row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val displayPos = if (isDraggingSlider) (sliderDragValue * totalDur).toLong() else currentPosMs
                Text(
                    text = formatTime(displayPos),
                    color = theme.textMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(durationMs),
                    color = theme.textMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Player Control Buttons Row (Shuffle, Previous, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("btn_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) theme.accentColor else theme.textMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.testTag("btn_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause (Large glowing circular button matching reference design)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(16.dp, CircleShape, spotColor = theme.accentColor)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(theme.accentColor, theme.secondaryAccentColor)
                            )
                        )
                        .clickable { onTogglePlayPause() }
                        .testTag("btn_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("btn_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier.testTag("btn_repeat")
                ) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) theme.accentColor else theme.textMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    speeds.forEach { sp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetSpeed(sp)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sp}x",
                                color = if (playbackSpeed == sp) theme.accentColor else theme.textPrimary,
                                fontWeight = if (playbackSpeed == sp) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (playbackSpeed == sp) {
                                Text("✓", color = theme.accentColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Volume Dialog
    if (showVolumeDialog) {
        var tempVol by remember { mutableFloatStateOf(volume) }
        AlertDialog(
            onDismissRequest = { showVolumeDialog = false },
            title = { Text("Player Volume", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(tempVol * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = tempVol,
                        onValueChange = {
                            tempVol = it
                            onSetVolume(it)
                        },
                        colors = SliderDefaults.colors(thumbColor = theme.accentColor, activeTrackColor = theme.accentColor)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVolumeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Lyrics Dialog
    if (showLyricsDialog) {
        AlertDialog(
            onDismissRequest = { showLyricsDialog = false },
            title = { Text(song?.title ?: "Lyrics", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val lyrics = listOf(
                        "Deep within the silicon veins",
                        "Where the ghost of rhythm remains",
                        "Pulse of neon, cold and bright",
                        "We are dancing in the static light",
                        "Echoes of a forgotten sun",
                        "Before the digital age had begun",
                        "Infinite loops and endless strands",
                        "Slipping through our hollow hands",
                        "Wait for the signal to return",
                        "Let the circuits finally burn",
                        "Euphoria echoes through the night",
                        "Everything will be alright"
                    )
                    lyrics.forEachIndexed { idx, line ->
                        Text(
                            text = line,
                            color = if (idx == 3) theme.accentColor else theme.textSecondary,
                            fontWeight = if (idx == 3) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (idx == 3) 16.sp else 14.sp,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLyricsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
