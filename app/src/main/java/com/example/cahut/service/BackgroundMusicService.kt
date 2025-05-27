package com.example.cahut.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.cahut.R

class BackgroundMusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 0.5f
    private var isMuted: Boolean = false

    override fun onCreate() {
        super.onCreate()
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.bg_music)
            mediaPlayer?.isLooping = true
            setVolume(currentVolume)
        } catch (e: Exception) {
            Log.e("BackgroundMusicService", "Error creating MediaPlayer: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "SET_VOLUME" -> {
                val volume = intent.getFloatExtra("volume", 0.5f)
                setVolume(volume)
            }
            "TOGGLE_MUTE" -> {
                toggleMute()
            }
        }
        mediaPlayer?.start()
        return START_STICKY
    }

    private fun setVolume(volume: Float) {
        currentVolume = volume
        if (!isMuted) {
            mediaPlayer?.setVolume(volume, volume)
        }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) {
            mediaPlayer?.setVolume(0f, 0f)
        } else {
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        }
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
} 