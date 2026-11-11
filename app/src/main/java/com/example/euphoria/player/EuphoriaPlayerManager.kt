package com.example.euphoria.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.CountDownTimer
import com.example.euphoria.data.model.Song
import com.example.euphoria.data.repository.MusicRepository
import com.example.euphoria.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class AudioFocusMode(val displayName: String) {
    EXCLUSIVE("Exclusive Playback"),
    ALLOW_OTHER("Allow Other Apps"),
    DUCK_OTHER("Duck Other Audio"),
    RESPECT_OTHER("Respect Other Audio")
}

data class FadeSettings(
    val fadeInEnabled: Boolean = true,
    val fadeOutEnabled: Boolean = true,
    val fadeDurationSec: Float = 1.5f,
    val crossfadeEnabled: Boolean = true
)

data class SleepTimerState(
    val isActive: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val fadeOutBeforeStop: Boolean = true,
    val pauseWhenPlaybackPaused: Boolean = false
)

class EuphoriaPlayerManager private constructor(private val context: Context) {

    private val playerScope = CoroutineScope(Dispatchers.Main)
    val repository = MusicRepository(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Playback state flows
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Settings flows
    private val _audioFocusMode = MutableStateFlow(AudioFocusMode.EXCLUSIVE)
    val audioFocusMode: StateFlow<AudioFocusMode> = _audioFocusMode.asStateFlow()

    private val _fadeSettings = MutableStateFlow(FadeSettings())
    val fadeSettings: StateFlow<FadeSettings> = _fadeSettings.asStateFlow()

    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    // Live dynamic waveform amplitudes (32 bars, 0.0 to 1.0)
    private val _waveformAmplitudes = MutableStateFlow<List<Float>>(List(32) { 0.2f })
    val waveformAmplitudes: StateFlow<List<Float>> = _waveformAmplitudes.asStateFlow()

    private var positionTrackerJob: Job? = null
    private var sleepCountDownTimer: CountDownTimer? = null
    private var fadeJob: Job? = null

    init {
        // Initial queue load from repository demo/scanned songs
        playerScope.launch {
            repository.songs.collect { songList ->
                if (_queue.value.isEmpty() && songList.isNotEmpty()) {
                    _queue.value = songList
                    if (_currentSong.value == null) {
                        _currentSong.value = songList.firstOrNull()
                    }
                }
            }
        }
        startPositionAndWaveformLoop()
    }

    private fun startPositionAndWaveformLoop() {
        positionTrackerJob?.cancel()
        positionTrackerJob = playerScope.launch {
            var phase = 0.0
            while (isActive) {
                val mp = mediaPlayer
                if (mp != null && _isPlaying.value) {
                    try {
                        val pos = mp.currentPosition.toLong()
                        val dur = mp.duration.toLong().coerceAtLeast(1L)
                        _currentPositionMs.value = pos
                        _durationMs.value = dur

                        // Handle near-end fadeout if enabled
                        val timeLeftMs = dur - pos
                        val fadeDurationMs = (_fadeSettings.value.fadeDurationSec * 1000).toLong()
                        if (_fadeSettings.value.fadeOutEnabled && timeLeftMs in 1..fadeDurationMs && fadeJob?.isActive != true) {
                            val ratio = (timeLeftMs.toFloat() / fadeDurationMs).coerceIn(0.05f, 1f)
                            mp.setVolume(ratio * _volume.value, ratio * _volume.value)
                        }

                        // Generate fluid reactive waveform bars
                        phase += 0.15
                        val amps = List(32) { i ->
                            val freqMod = sin(phase + i * 0.4).toFloat() * 0.35f
                            val waveMod = sin((pos / 350.0) + i * 0.25).toFloat() * 0.25f
                            (0.35f + freqMod + waveMod).coerceIn(0.12f, 0.98f)
                        }
                        _waveformAmplitudes.value = amps

                        // Update widgets progress
                        val percent = ((pos.toFloat() / dur.toFloat()) * 100).toInt().coerceIn(0, 100)
                        WidgetUpdateHelper.updateAllWidgets(context, _currentSong.value, true, percent)
                    } catch (e: Exception) {
                        // ignore transitory mediaplayer state errors
                    }
                } else {
                    // Idle subtle breathing waveform
                    phase += 0.04
                    val idleAmps = List(32) { i ->
                        (0.18f + sin(phase + i * 0.2).toFloat() * 0.08f).coerceIn(0.1f, 0.4f)
                    }
                    _waveformAmplitudes.value = idleAmps
                }
                delay(80)
            }
        }
    }

    fun playSong(song: Song, queueList: List<Song>? = null) {
        if (queueList != null) {
            _queue.value = queueList
            val idx = queueList.indexOfFirst { it.id == song.id }
            _currentQueueIndex.value = if (idx >= 0) idx else 0
        } else {
            val existingIdx = _queue.value.indexOfFirst { it.id == song.id }
            if (existingIdx >= 0) {
                _currentQueueIndex.value = existingIdx
            } else {
                _queue.value = _queue.value + song
                _currentQueueIndex.value = _queue.value.lastIndex
            }
        }
        _currentSong.value = song

        playerScope.launch {
            repository.recordSongPlay(song.id)
        }

        prepareAndPlay(song)
    }

    private fun prepareAndPlay(song: Song) {
        requestAudioFocus()
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, song.contentUri)
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong()
                    applyPlaybackSpeed()
                    mp.start()
                    _isPlaying.value = true
                    applyFadeIn()
                    EuphoriaPlaybackService.startService(context)
                    WidgetUpdateHelper.updateAllWidgets(context, song, true)
                }
                setOnCompletionListener {
                    onSongCompleted()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        val song = _currentSong.value
        if (song == null) {
            val first = _queue.value.firstOrNull()
            if (first != null) {
                playSong(first)
            }
            return
        }

        if (mediaPlayer == null) {
            prepareAndPlay(song)
            return
        }

        requestAudioFocus()
        mediaPlayer?.start()
        _isPlaying.value = true
        applyFadeIn()
        EuphoriaPlaybackService.startService(context)
        WidgetUpdateHelper.updateAllWidgets(context, song, true)
    }

    fun pause() {
        if (_fadeSettings.value.fadeOutEnabled) {
            applyFadeOutAndPause()
        } else {
            mediaPlayer?.pause()
            _isPlaying.value = false
            WidgetUpdateHelper.updateAllWidgets(context, _currentSong.value, false)
        }
    }

    private fun applyFadeIn() {
        if (!_fadeSettings.value.fadeInEnabled) {
            mediaPlayer?.setVolume(_volume.value, _volume.value)
            return
        }
        fadeJob?.cancel()
        fadeJob = playerScope.launch {
            val totalSteps = 15
            val stepDelay = ((_fadeSettings.value.fadeDurationSec * 1000) / totalSteps).toLong()
            for (i in 1..totalSteps) {
                val factor = (i.toFloat() / totalSteps) * _volume.value
                try {
                    mediaPlayer?.setVolume(factor, factor)
                } catch (e: Exception) {
                    break
                }
                delay(stepDelay)
            }
            try {
                mediaPlayer?.setVolume(_volume.value, _volume.value)
            } catch (e: Exception) {}
        }
    }

    private fun applyFadeOutAndPause() {
        fadeJob?.cancel()
        fadeJob = playerScope.launch {
            val totalSteps = 12
            val stepDelay = ((_fadeSettings.value.fadeDurationSec * 1000) / totalSteps).toLong()
            val startVol = _volume.value
            for (i in totalSteps downTo 1) {
                val factor = (i.toFloat() / totalSteps) * startVol
                try {
                    mediaPlayer?.setVolume(factor, factor)
                } catch (e: Exception) {
                    break
                }
                delay(stepDelay)
            }
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {}
            _isPlaying.value = false
            WidgetUpdateHelper.updateAllWidgets(context, _currentSong.value, false)
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        var nextIdx = _currentQueueIndex.value + 1
        if (_isShuffle.value) {
            nextIdx = (q.indices).random()
        }

        if (nextIdx >= q.size) {
            if (_repeatMode.value == RepeatMode.ALL) {
                nextIdx = 0
            } else {
                _isPlaying.value = false
                return
            }
        }

        _currentQueueIndex.value = nextIdx
        val nextSong = q[nextIdx]
        playSong(nextSong)
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        var prevIdx = _currentQueueIndex.value - 1
        if (prevIdx < 0) {
            prevIdx = q.lastIndex
        }

        _currentQueueIndex.value = prevIdx
        val prevSong = q[prevIdx]
        playSong(prevSong)
    }

    private fun onSongCompleted() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (_currentQueueIndex.value < _queue.value.lastIndex) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    WidgetUpdateHelper.updateAllWidgets(context, _currentSong.value, false)
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _currentPositionMs.value = positionMs
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackSpeed()
    }

    private fun applyPlaybackSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { mp ->
                    val params = mp.playbackParams
                    params.speed = _playbackSpeed.value
                    mp.playbackParams = params
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun setFadeSettings(settings: FadeSettings) {
        _fadeSettings.value = settings
    }

    fun setAudioFocusMode(mode: AudioFocusMode) {
        _audioFocusMode.value = mode
    }

    private fun requestAudioFocus() {
        val mode = _audioFocusMode.value
        if (mode == AudioFocusMode.ALLOW_OTHER) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusGain = when (mode) {
                AudioFocusMode.DUCK_OTHER -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                else -> AudioManager.AUDIOFOCUS_GAIN
            }
            val request = AudioFocusRequest.Builder(focusGain)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    handleAudioFocusChange(focusChange)
                }
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            audioManager.requestAudioFocus(
                { focusChange -> handleAudioFocusChange(focusChange) },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (_audioFocusMode.value == AudioFocusMode.RESPECT_OTHER || _audioFocusMode.value == AudioFocusMode.EXCLUSIVE) {
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (_audioFocusMode.value == AudioFocusMode.RESPECT_OTHER) {
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (_audioFocusMode.value == AudioFocusMode.DUCK_OTHER) {
                    mediaPlayer?.setVolume(0.2f * _volume.value, 0.2f * _volume.value)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(_volume.value, _volume.value)
                if (!_isPlaying.value && _audioFocusMode.value == AudioFocusMode.RESPECT_OTHER) {
                    play()
                }
            }
        }
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int, fadeOutBeforeStop: Boolean = true, pauseWhenPaused: Boolean = false) {
        cancelSleepTimer()
        val totalSec = minutes * 60
        _sleepTimerState.value = SleepTimerState(
            isActive = true,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            fadeOutBeforeStop = fadeOutBeforeStop,
            pauseWhenPlaybackPaused = pauseWhenPaused
        )

        sleepCountDownTimer = object : CountDownTimer(totalSec * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSec = (millisUntilFinished / 1000).toInt()
                _sleepTimerState.value = _sleepTimerState.value.copy(remainingSeconds = remainingSec)

                // If fadeOutBeforeStop is true, gently lower volume in the last 20 seconds
                if (fadeOutBeforeStop && remainingSec in 1..20) {
                    val fadeRatio = (remainingSec.toFloat() / 20f) * _volume.value
                    mediaPlayer?.setVolume(fadeRatio, fadeRatio)
                }
            }

            override fun onFinish() {
                _sleepTimerState.value = SleepTimerState(isActive = false)
                pause()
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepCountDownTimer?.cancel()
        sleepCountDownTimer = null
        _sleepTimerState.value = SleepTimerState(isActive = false)
        mediaPlayer?.setVolume(_volume.value, _volume.value)
    }

    // Queue Operations
    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
    }

    fun playNextInQueue(song: Song) {
        val current = _queue.value.toMutableList()
        val insertIdx = (_currentQueueIndex.value + 1).coerceAtMost(current.size)
        current.add(insertIdx, song)
        _queue.value = current
    }

    fun removeFromQueue(index: Int) {
        if (index in _queue.value.indices) {
            val mutable = _queue.value.toMutableList()
            mutable.removeAt(index)
            _queue.value = mutable
            if (index < _currentQueueIndex.value) {
                _currentQueueIndex.value = _currentQueueIndex.value - 1
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (fromIndex in _queue.value.indices && toIndex in _queue.value.indices) {
            val list = _queue.value.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _queue.value = list
        }
    }

    fun clearQueue() {
        val current = _currentSong.value
        _queue.value = if (current != null) listOf(current) else emptyList()
        _currentQueueIndex.value = 0
    }

    fun shuffleQueue() {
        val cur = _currentSong.value
        val others = _queue.value.filter { it.id != cur?.id }.shuffled()
        _queue.value = if (cur != null) listOf(cur) + others else others
        _currentQueueIndex.value = 0
    }

    fun toggleCurrentSongFavorite() {
        val song = _currentSong.value ?: return
        playerScope.launch {
            // Check if favorite
            val isFav = repository.isSongFavorite(song.id)
            // Toggle
            repository.toggleFavorite(song.id, false) // toggle in repo
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: EuphoriaPlayerManager? = null

        fun getInstance(context: Context): EuphoriaPlayerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = EuphoriaPlayerManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
