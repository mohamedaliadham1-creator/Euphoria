package com.example.euphoria.data.provider

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.euphoria.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object DemoMusicProvider {

    suspend fun getOrGenerateDemoSongs(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val audioDir = File(context.filesDir, "euphoria_demo_tracks").apply { mkdirs() }
        val tracksInfo = listOf(
            DemoTrackSpec(
                id = 9001L,
                title = "Midnight Vibes",
                artist = "Fire in Motion",
                album = "Neon Horizon",
                genre = "Synthwave",
                baseFreq = 220.0, // A3
                tempoBpm = 110,
                durationSeconds = 120,
                description = "Deep nocturnal synthwave with resonant basslines and shimmering arpeggios."
            ),
            DemoTrackSpec(
                id = 9002L,
                title = "Lost in Waves",
                artist = "Velo",
                album = "Cyber Echoes",
                genre = "Ambient Chill",
                baseFreq = 261.63, // C4
                tempoBpm = 95,
                durationSeconds = 110,
                description = "Lush atmospheric soundscapes inspired by neon rain and cyber dreamscapes."
            ),
            DemoTrackSpec(
                id = 9003L,
                title = "Shaitan Ka Saala",
                artist = "T-Series Remix",
                album = "Housefull Vibes",
                genre = "Electronic",
                baseFreq = 293.66, // D4
                tempoBpm = 128,
                durationSeconds = 135,
                description = "High-energy rhythmic dance beats inspired by Bollywood and electronic groove."
            ),
            DemoTrackSpec(
                id = 9004L,
                title = "Silent Dreams",
                artist = "Novi",
                album = "Astral Journey",
                genre = "Lo-Fi Beats",
                baseFreq = 196.0, // G3
                tempoBpm = 85,
                durationSeconds = 115,
                description = "Warm analog textures, soft keys, and hypnotic slow-tempo chill beats."
            ),
            DemoTrackSpec(
                id = 9005L,
                title = "Golden Hour",
                artist = "Luxe",
                album = "Velvet Sunset",
                genre = "R&B Chill",
                baseFreq = 329.63, // E4
                tempoBpm = 100,
                durationSeconds = 130,
                description = "Silky electric piano chords with smooth bass and melodic warmth."
            ),
            DemoTrackSpec(
                id = 9006L,
                title = "Neon Heartbeat",
                artist = "The Glitch Dreamers",
                album = "Liquid Audio",
                genre = "Glitch Hop",
                baseFreq = 246.94, // B3
                tempoBpm = 120,
                durationSeconds = 125,
                description = "Pulsing bass frequencies with crisp acoustic textures and liquid ambiance."
            )
        )

        val songs = mutableListOf<Song>()
        for (spec in tracksInfo) {
            val file = File(audioDir, "demo_${spec.id}.wav")
            if (!file.exists() || file.length() < 1000) {
                generateMelodicWav(file, spec.baseFreq, spec.tempoBpm, spec.durationSeconds)
            }
            val uri = Uri.fromFile(file)
            songs.add(
                Song(
                    id = spec.id,
                    title = spec.title,
                    artist = spec.artist,
                    album = spec.album,
                    albumArtist = spec.artist,
                    genre = spec.genre,
                    durationMs = spec.durationSeconds * 1000L,
                    contentUri = uri,
                    albumArtUri = null,
                    trackNumber = (spec.id - 9000).toInt(),
                    year = 2025,
                    description = spec.description,
                    isDemo = true
                )
            )
        }
        songs
    }

    private fun generateMelodicWav(file: File, baseFreq: Double, bpm: Int, durationSec: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationSec
        val bytesPerSample = 2 // 16-bit mono
        val dataSize = numSamples * bytesPerSample

        val chords = listOf(
            doubleArrayOf(baseFreq, baseFreq * 1.25, baseFreq * 1.5), // Major / Suspended
            doubleArrayOf(baseFreq * 0.89, baseFreq * 1.06, baseFreq * 1.33),
            doubleArrayOf(baseFreq * 0.75, baseFreq * 1.0, baseFreq * 1.25),
            doubleArrayOf(baseFreq * 0.84, baseFreq * 1.12, baseFreq * 1.4)
        )

        val beatsPerChord = 4
        val samplesPerBeat = (sampleRate * 60) / bpm

        FileOutputStream(file).use { fos ->
            // Write temporary 44-byte WAV header
            val header = ByteArray(44)
            fos.write(header)

            val buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN)
            var sampleIndex = 0

            while (sampleIndex < numSamples) {
                buffer.clear()
                val chunk = minOf(buffer.capacity() / bytesPerSample, numSamples - sampleIndex)

                for (i in 0 until chunk) {
                    val s = sampleIndex + i
                    val beat = s / samplesPerBeat
                    val chordIdx = (beat / beatsPerChord) % chords.size
                    val chord = chords[chordIdx]

                    val t = s.toDouble() / sampleRate
                    // Harmonic synthesis: fundamental + 3rd + 5th + sub-bass
                    val tone1 = sin(2.0 * Math.PI * chord[0] * t) * 0.4
                    val tone2 = sin(2.0 * Math.PI * chord[1] * t) * 0.25
                    val tone3 = sin(2.0 * Math.PI * chord[2] * t) * 0.2
                    val subBass = sin(2.0 * Math.PI * (chord[0] / 2.0) * t) * 0.35

                    // Rhythmic pulse envelope
                    val beatProgress = (s % samplesPerBeat).toDouble() / samplesPerBeat
                    val pulse = 0.7 + 0.3 * (1.0 - beatProgress)

                    // Fade in/out at extremes
                    val fadeIn = if (s < sampleRate * 2) s.toDouble() / (sampleRate * 2) else 1.0
                    val fadeOut = if (s > numSamples - sampleRate * 3) (numSamples - s).toDouble() / (sampleRate * 3) else 1.0

                    val sampleValue = ((tone1 + tone2 + tone3 + subBass) * pulse * fadeIn * fadeOut * 0.7).coerceIn(-1.0, 1.0)
                    val shortVal = (sampleValue * 32767.0).toInt().toShort()

                    buffer.putShort(shortVal)
                }

                fos.write(buffer.array(), 0, chunk * bytesPerSample)
                sampleIndex += chunk
            }
        }

        // Patch accurate WAV header
        RandomAccessFile(file, "rw").use { raf ->
            val totalFileSize = file.length()
            val byteRate = sampleRate * bytesPerSample

            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeInt(Integer.reverseBytes((totalFileSize - 8).toInt()))
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeInt(Integer.reverseBytes(16)) // Subchunk1Size (16 for PCM)
            raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat (1 = PCM)
            raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // NumChannels (1 = Mono)
            raf.writeInt(Integer.reverseBytes(sampleRate)) // SampleRate
            raf.writeInt(Integer.reverseBytes(byteRate)) // ByteRate
            raf.writeShort(java.lang.Short.reverseBytes(bytesPerSample.toShort()).toInt()) // BlockAlign
            raf.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt()) // BitsPerSample
            raf.writeBytes("data")
            raf.writeInt(Integer.reverseBytes(dataSize))
        }
    }
}

private data class DemoTrackSpec(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val baseFreq: Double,
    val tempoBpm: Int,
    val durationSeconds: Int,
    val description: String
)
