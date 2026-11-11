package com.example.euphoria.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String = "",
    val genre: String = "Various",
    val durationMs: Long = 0L,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val trackNumber: Int = 1,
    val discNumber: Int = 1,
    val year: Int = 2024,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val folderName: String = "Music",
    val description: String = "",
    val isDemo: Boolean = false
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: Uri? = null,
    val year: Int = 2024
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val artworkUri: Uri? = null
)
