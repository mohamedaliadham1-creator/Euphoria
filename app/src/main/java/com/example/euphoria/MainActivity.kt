package com.example.euphoria

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.euphoria.data.local.entity.PlaylistEntity
import com.example.euphoria.data.model.Song
import com.example.euphoria.ui.components.GlassCard
import com.example.euphoria.ui.components.MiniPlayer
import com.example.euphoria.ui.screens.HomeScreen
import com.example.euphoria.ui.screens.MetadataEditorScreen
import com.example.euphoria.ui.screens.NowPlayingScreen
import com.example.euphoria.ui.screens.PlaylistDetailScreen
import com.example.euphoria.ui.screens.QueueScreen
import com.example.euphoria.ui.screens.SearchScreen
import com.example.euphoria.ui.screens.SettingsScreen
import com.example.euphoria.ui.screens.ThemeCustomizerScreen
import com.example.euphoria.ui.theme.EuphoriaTheme
import com.example.euphoria.ui.viewmodel.EuphoriaViewModel

sealed interface EuphoriaScreen {
    data object Home : EuphoriaScreen
    data object NowPlaying : EuphoriaScreen
    data object ThemeCustomizer : EuphoriaScreen
    data class MetadataEditor(val song: Song) : EuphoriaScreen
    data object Queue : EuphoriaScreen
    data object Search : EuphoriaScreen
    data object Settings : EuphoriaScreen
    data class PlaylistDetail(val playlist: PlaylistEntity) : EuphoriaScreen
}

class MainActivity : ComponentActivity() {

    private val viewModel: EuphoriaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by viewModel.currentTheme.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            EuphoriaTheme(themeData = theme) {
                EuphoriaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EuphoriaApp(viewModel: EuphoriaViewModel) {
    val context = LocalContext.current

    // Request permissions on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: false
        val storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        if (audioGranted || storageGranted) {
            viewModel.rescanLibrary()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Observed states
    val theme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val customThemes by viewModel.customThemes.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val currentQueueIndex by viewModel.currentQueueIndex.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val audioFocusMode by viewModel.audioFocusMode.collectAsStateWithLifecycle()
    val fadeSettings by viewModel.fadeSettings.collectAsStateWithLifecycle()
    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val waveformAmplitudes by viewModel.waveformAmplitudes.collectAsStateWithLifecycle()

    // Navigation screen state
    var currentScreen by remember { mutableStateOf<EuphoriaScreen>(EuphoriaScreen.Home) }
    var selectedBottomNavIndex by remember { mutableStateOf(0) }

    // Add to playlist modal state
    var songForPlaylistModal by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistInModal by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val progressPercent = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()) else 0f

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = theme.primaryBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Content Area
            when (val screen = currentScreen) {
                is EuphoriaScreen.Home -> {
                    HomeScreen(
                        songs = songs,
                        albums = albums,
                        artists = artists,
                        playlists = playlists,
                        favoriteSongIds = favoriteSongIds,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        theme = theme,
                        onSongClick = { song, list ->
                            viewModel.playSong(song, list)
                            currentScreen = EuphoriaScreen.NowPlaying
                        },
                        onOpenSearch = { currentScreen = EuphoriaScreen.Search },
                        onOpenThemes = { currentScreen = EuphoriaScreen.ThemeCustomizer },
                        onOpenSettings = { currentScreen = EuphoriaScreen.Settings },
                        onOpenMetadataEditor = { song -> currentScreen = EuphoriaScreen.MetadataEditor(song) },
                        onAddToPlaylist = { song -> songForPlaylistModal = song },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song.id) },
                        onPlayNext = { song -> viewModel.playNextInQueue(song) },
                        onAlbumClick = { album ->
                            val albumSongs = songs.filter { it.album == album.name }
                            val first = albumSongs.firstOrNull()
                            if (first != null) viewModel.playSong(first, albumSongs)
                        },
                        onArtistClick = { artist ->
                            val artistSongs = songs.filter { it.artist == artist.name }
                            val first = artistSongs.firstOrNull()
                            if (first != null) viewModel.playSong(first, artistSongs)
                        }
                    )
                }

                is EuphoriaScreen.NowPlaying -> {
                    NowPlayingScreen(
                        song = currentSong,
                        isPlaying = isPlaying,
                        currentPosMs = currentPositionMs,
                        durationMs = durationMs,
                        isShuffle = isShuffle,
                        repeatMode = repeatMode,
                        playbackSpeed = playbackSpeed,
                        volume = volume,
                        isFavorite = currentSong?.let { favoriteSongIds.contains(it.id) } ?: false,
                        amplitudes = waveformAmplitudes,
                        theme = theme,
                        onClose = { currentScreen = EuphoriaScreen.Home },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onPrevious = { viewModel.playPrevious() },
                        onSeek = { viewModel.seekTo(it) },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onCycleRepeat = { viewModel.cycleRepeatMode() },
                        onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it.id) } },
                        onSetSpeed = { viewModel.setPlaybackSpeed(it) },
                        onSetVolume = { viewModel.setVolume(it) },
                        onOpenQueue = { currentScreen = EuphoriaScreen.Queue },
                        onOpenSleepTimer = { currentScreen = EuphoriaScreen.Settings },
                        onOpenMetadataEditor = { song -> currentScreen = EuphoriaScreen.MetadataEditor(song) },
                        onAddToPlaylist = { song -> songForPlaylistModal = song }
                    )
                }

                is EuphoriaScreen.ThemeCustomizer -> {
                    ThemeCustomizerScreen(
                        currentTheme = theme,
                        themeMode = themeMode,
                        customThemes = customThemes,
                        onSelectTheme = { viewModel.applyTheme(it) },
                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                        onApplyWallpaperTheme = { uri, mode, br, bl, al, sat ->
                            viewModel.applyWallpaperTheme(uri, mode, br, bl, al, sat)
                        },
                        onSaveCustomTheme = { viewModel.saveCurrentThemeAsCustom(it) },
                        onDeleteCustomTheme = { viewModel.deleteCustomTheme(it) },
                        onUpdatePlayerStyle = { viewModel.updatePlayerThemeStyle(it) },
                        onUpdateWaveformStyle = { viewModel.updateWaveformStyle(it) },
                        onUpdateFont = { viewModel.updateFontFamily(it) },
                        onBack = { currentScreen = EuphoriaScreen.Home }
                    )
                }

                is EuphoriaScreen.MetadataEditor -> {
                    MetadataEditorScreen(
                        song = screen.song,
                        theme = theme,
                        onSave = { songId, t, a, al, g, y, tr, desc, art ->
                            viewModel.saveMetadataOverride(songId, t, a, al, g, y, tr, desc, art)
                        },
                        onRestoreOriginal = { viewModel.restoreOriginalMetadata(it) },
                        onBack = { currentScreen = EuphoriaScreen.Home }
                    )
                }

                is EuphoriaScreen.Queue -> {
                    QueueScreen(
                        queue = queue,
                        currentIndex = currentQueueIndex,
                        theme = theme,
                        onPlaySongAtIndex = { idx ->
                            if (idx in queue.indices) viewModel.playSong(queue[idx], queue)
                        },
                        onRemoveFromQueue = { viewModel.removeFromQueue(it) },
                        onMoveUp = { idx -> viewModel.reorderQueue(idx, idx - 1) },
                        onMoveDown = { idx -> viewModel.reorderQueue(idx, idx + 1) },
                        onClearQueue = { viewModel.clearQueue() },
                        onShuffleQueue = { viewModel.shuffleQueue() },
                        onSaveQueueAsPlaylist = { viewModel.saveQueueAsPlaylist(it) },
                        onBack = { currentScreen = EuphoriaScreen.Home }
                    )
                }

                is EuphoriaScreen.Search -> {
                    SearchScreen(
                        allSongs = songs,
                        theme = theme,
                        onSongClick = { song, list ->
                            viewModel.playSong(song, list)
                            currentScreen = EuphoriaScreen.NowPlaying
                        },
                        onBack = { currentScreen = EuphoriaScreen.Home }
                    )
                }

                is EuphoriaScreen.Settings -> {
                    SettingsScreen(
                        theme = theme,
                        themeMode = themeMode,
                        fadeSettings = fadeSettings,
                        audioFocusMode = audioFocusMode,
                        sleepTimerState = sleepTimerState,
                        totalSongs = songs.size,
                        totalAlbums = albums.size,
                        totalArtists = artists.size,
                        totalPlaylists = playlists.size,
                        onOpenThemeCustomizer = { currentScreen = EuphoriaScreen.ThemeCustomizer },
                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                        onUpdateFadeSettings = { viewModel.setFadeSettings(it) },
                        onUpdateAudioFocus = { viewModel.setAudioFocusMode(it) },
                        onStartSleepTimer = { min, fade, pause -> viewModel.startSleepTimer(min, fade, pause) },
                        onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                        onRescanLibrary = { viewModel.rescanLibrary() },
                        onBack = { currentScreen = EuphoriaScreen.Home }
                    )
                }

                is EuphoriaScreen.PlaylistDetail -> {
                    PlaylistDetailScreen(
                        playlist = screen.playlist,
                        playlistSongs = songs,
                        allSongs = songs,
                        theme = theme,
                        onPlaySong = { s, l -> viewModel.playSong(s, l) },
                        onAddSong = { pid, sid -> viewModel.addSongToPlaylist(pid, sid) },
                        onRemoveSong = { pid, sid -> viewModel.removeSongFromPlaylist(pid, sid) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onBack = { currentScreen = EuphoriaScreen.Home }
                    )
                }
            }

            // Bottom Floating Controls: MiniPlayer & Floating Bottom Navigation Bar (Visible on non-NowPlaying screens)
            if (currentScreen !is EuphoriaScreen.NowPlaying) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // MiniPlayer
                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        progressPercent = progressPercent,
                        theme = theme,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onOpenNowPlaying = { currentScreen = EuphoriaScreen.NowPlaying },
                        onOpenQueue = { currentScreen = EuphoriaScreen.Queue }
                    )

                    // Floating Glassmorphic Bottom Navigation Bar matching Screenshot 2
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(26.dp)),
                        backgroundColor = theme.surfaceColor,
                        cardAlpha = 0.95f,
                        cornerRadius = 26.dp,
                        borderColor = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Home
                            BottomNavPill(
                                icon = Icons.Default.Home,
                                label = "Home",
                                isSelected = currentScreen is EuphoriaScreen.Home,
                                theme = theme,
                                onClick = { currentScreen = EuphoriaScreen.Home }
                            )

                            // Queue
                            BottomNavPill(
                                icon = Icons.Default.QueueMusic,
                                label = "Queue",
                                isSelected = currentScreen is EuphoriaScreen.Queue,
                                theme = theme,
                                onClick = { currentScreen = EuphoriaScreen.Queue }
                            )

                            // Themes
                            BottomNavPill(
                                icon = Icons.Default.Palette,
                                label = "Themes",
                                isSelected = currentScreen is EuphoriaScreen.ThemeCustomizer,
                                theme = theme,
                                onClick = { currentScreen = EuphoriaScreen.ThemeCustomizer }
                            )

                            // Settings
                            BottomNavPill(
                                icon = Icons.Default.Settings,
                                label = "Settings",
                                isSelected = currentScreen is EuphoriaScreen.Settings,
                                theme = theme,
                                onClick = { currentScreen = EuphoriaScreen.Settings }
                            )
                        }
                    }
                }
            }
        }
    }

    // Global Add to Playlist Modal
    if (songForPlaylistModal != null) {
        val targetSong = songForPlaylistModal!!
        AlertDialog(
            onDismissRequest = {
                songForPlaylistModal = null
                showCreatePlaylistInModal = false
            },
            title = {
                Text(
                    text = "Add \"${targetSong.title}\" to Playlist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    if (showCreatePlaylistInModal) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            label = { Text("New Playlist Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    viewModel.createPlaylist(newPlaylistName)
                                    Toast.makeText(context, "Playlist created!", Toast.LENGTH_SHORT).show()
                                    showCreatePlaylistInModal = false
                                    newPlaylistName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create & Save")
                        }
                    } else {
                        Button(
                            onClick = { showCreatePlaylistInModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Playlist")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (playlists.isEmpty()) {
                            Text("No playlists yet. Create one above!", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            LazyColumn(modifier = Modifier.height(200.dp)) {
                                items(playlists) { pl ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.addSongToPlaylist(pl.id, targetSong.id)
                                                Toast.makeText(context, "Added to ${pl.name}", Toast.LENGTH_SHORT).show()
                                                songForPlaylistModal = null
                                            }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(pl.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Add, contentDescription = null, tint = theme.accentColor)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    songForPlaylistModal = null
                    showCreatePlaylistInModal = false
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BottomNavPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    theme: com.example.euphoria.theme.EuphoriaThemeData,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) theme.accentColor else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else theme.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
