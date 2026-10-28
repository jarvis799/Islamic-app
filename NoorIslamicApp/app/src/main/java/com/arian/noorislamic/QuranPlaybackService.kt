package com.arian.noorislamic

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class QuranPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            setHandleAudioBecomingNoisy(true)
        }
        mediaSession = MediaSession.Builder(this, player).setSessionActivity(
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        ).build()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        val items = intent?.getStringArrayListExtra(EXTRA_URLS)
        val player = mediaSession?.player
        if (!items.isNullOrEmpty() && player != null) {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Quran Recitation"
            player.setMediaItems(items.mapIndexed { index, url -> MediaItem.Builder().setUri(url).setMediaMetadata(MediaMetadata.Builder().setTitle("$title • Ayah ${index+1}").setArtist("Noor Islamic").build()).build() })
            player.prepare(); player.playWhenReady = true
        }
        return result
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
    override fun onDestroy() { mediaSession?.run { player.release(); release() }; mediaSession=null; super.onDestroy() }
    companion object { const val EXTRA_URLS="urls"; const val EXTRA_TITLE="title" }
}
