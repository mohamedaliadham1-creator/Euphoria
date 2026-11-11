package com.example.euphoria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.euphoria.data.local.entity.PlaylistEntity
import com.example.euphoria.data.model.Album
import com.example.euphoria.data.model.Artist
import com.example.euphoria.data.model.Song
import com.example.euphoria.theme.EuphoriaThemeData
import com.example.euphoria.ui.components.GlassCard
import java.util.Locale

@Composable
fun HomeScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<PlaylistEntity>,
    favoriteSongIds: List<Long>,
    currentSong: Song?,
    isPlaying: Boolean,
    theme: EuphoriaThemeData,
    onSongClick: (Song, List<Song>) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMetadataEditor: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("Songs", "Albums", "Playlists", "Artists", "Folders")

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("home_screen")
    ) {
        // Top Header matching Screenshot 1 & 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(theme.accentColor, theme.secondaryAccentColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EUPHORIA",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Made by Adham Mohamed Ali",
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.testTag("btn_home_search")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = theme.textPrimary
                )
            }

            IconButton(
                onClick = onOpenThemes,
                modifier = Modifier.testTag("btn_home_theme")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Themes",
                    tint = theme.accentColor
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("btn_home_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = theme.textSecondary
                )
            }
        }

        // Category Pills (Songs, Albums, Playlists, Artists, Folders)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { index, title ->
                val isSelected = selectedCategoryIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryIndex = index },
                    label = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.accentColor,
                        selectedLabelColor = Color.White,
                        containerColor = theme.surfaceColor.copy(alpha = 0.6f),
                        labelColor = theme.textSecondary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Main content based on selected tab
        when (selectedCategoryIndex) {
            0 -> {
                // Songs Tab: Featured cards banner + Song List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        // Featured Horizontal Banner inspired by reference screenshot 2
                        FeaturedCardsRow(theme = theme, onCardClick = { title ->
                            // Play matching demo playlist
                            val matching = songs.filter { it.album.contains(title, ignoreCase = true) || it.title.contains(title, ignoreCase = true) }
                            if (matching.isNotEmpty()) {
                                onSongClick(matching.first(), songs)
                            }
                        })
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Songs (${songs.size})",
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    items(songs, key = { it.id }) { song ->
                        val isCurrent = currentSong?.id == song.id
                        val isFav = favoriteSongIds.contains(song.id)

                        SongListItem(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            isFavorite = isFav,
                            theme = theme,
                            onClick = { onSongClick(song, songs) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onAddToPlaylist = { onAddToPlaylist(song) },
                            onPlayNext = { onPlayNext(song) },
                            onEditMetadata = { onOpenMetadataEditor(song) }
                        )
                    }
                }
            }

            1 -> {
                // Albums Tab: 2-column Grid matching Screenshot 1 (Albums Grid)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "${albums.size} Albums",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    val chunked = albums.chunked(2)
                    items(chunked) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            pair.forEach { album ->
                                Box(modifier = Modifier.weight(1f)) {
                                    AlbumGridCard(
                                        album = album,
                                        theme = theme,
                                        onClick = { onAlbumClick(album) }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            2 -> {
                // Playlists Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "Playlists (${playlists.size})",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    items(playlists) { pl ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    // Filter songs in playlist and play
                                    val firstSong = songs.firstOrNull()
                                    if (firstSong != null) onSongClick(firstSong, songs)
                                },
                            backgroundColor = theme.surfaceColor,
                            cornerRadius = 16.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(theme.accentColor, theme.secondaryAccentColor)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pl.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pl.name,
                                        color = theme.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = pl.description.ifBlank { "Personal Playlist" },
                                        color = theme.textSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = theme.accentColor
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // Artists Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "Artists (${artists.size})",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    items(artists) { artist ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onArtistClick(artist) },
                            backgroundColor = theme.surfaceColor,
                            cornerRadius = 16.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(theme.accentColor, theme.cardColor)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = artist.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = artist.name,
                                        color = theme.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${artist.songCount} Songs • ${artist.albumCount} Albums",
                                        color = theme.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Folders Tab
                val folders = songs.groupBy { it.folderName }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "Music Folders (${folders.size})",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    items(folders.keys.toList()) { folderName ->
                        val count = folders[folderName]?.size ?: 0
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    val first = folders[folderName]?.firstOrNull()
                                    if (first != null) onSongClick(first, folders[folderName] ?: songs)
                                },
                            backgroundColor = theme.surfaceColor,
                            cornerRadius = 16.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📁", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = folderName, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "$count tracks", color = theme.textSecondary, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.accentColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedCardsRow(theme: EuphoriaThemeData, onCardClick: (String) -> Unit) {
    val items = listOf(
        Triple("Midnight Vibes", "108 songs", theme.accentColor),
        Triple("Chill & Relax", "91 songs", Color(0xFF00B4D8)),
        Triple("Energy Boost", "55 songs", Color(0xFFFF5A36)),
        Triple("Liquid Audio", "64 songs", Color(0xFF10B981))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items.forEach { (title, subtitle, color) ->
            GlassCard(
                modifier = Modifier
                    .width(170.dp)
                    .height(130.dp)
                    .clickable { onCardClick(title) },
                backgroundColor = theme.surfaceColor,
                cardAlpha = 0.85f,
                cornerRadius = 20.dp,
                borderColor = color.copy(alpha = 0.4f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(color.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumGridCard(
    album: Album,
    theme: EuphoriaThemeData,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = theme.cardColor,
        cardAlpha = 0.85f,
        cornerRadius = 18.dp,
        borderColor = Color.White.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(theme.cardColor, theme.accentColor.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (album.artworkUri != null) {
                    AsyncImage(
                        model = album.artworkUri,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = album.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = album.name,
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${album.songCount} songs • ${album.artist}",
                    color = theme.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    theme: EuphoriaThemeData,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onPlayNext: () -> Unit,
    onEditMetadata: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        listOf(theme.accentColor.copy(alpha = 0.5f), theme.surfaceColor)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = song.title.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            if (isCurrent && isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = theme.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isCurrent) theme.accentColor else theme.textPrimary,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.album}",
                color = theme.textSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatTime(song.durationMs),
            color = theme.textMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Song options",
                    tint = theme.textMuted
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
                    onClick = {
                        showMenu = false
                        onToggleFavorite()
                    },
                    leadingIcon = {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Play Next") },
                    onClick = {
                        showMenu = false
                        onPlayNext()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist") },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit Metadata & Cover") },
                    onClick = {
                        showMenu = false
                        onEditMetadata()
                    }
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
