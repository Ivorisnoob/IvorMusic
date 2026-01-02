package com.ivor.ivormusic.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.ivor.ivormusic.MainActivity
import com.ivor.ivormusic.data.YouTubeRepository
import com.ivor.ivormusic.data.SongSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.guava.future
import com.google.common.util.concurrent.ListenableFuture

class MusicService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var player: Player
    private lateinit var youtubeRepository: YouTubeRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeSessionAndPlayer()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = mediaLibrarySession

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    private fun initializeSessionAndPlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(androidx.media3.common.AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        youtubeRepository = YouTubeRepository(this)

        val sessionIntent = packageManager.getLaunchIntentForPackage(packageName).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, object : MediaLibrarySession.Callback {
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                return serviceScope.future {
                    val deferredItems = mediaItems.map { item ->
                        async(Dispatchers.IO) {
                            val videoId = item.mediaId
                            if (item.localConfiguration?.uri == null) {
                                try {
                                    // Timeout to avoid blocking indefinitely
                                    val streamUrl = kotlinx.coroutines.withTimeoutOrNull(8000) {
                                        youtubeRepository.getStreamUrl(videoId)
                                    }
                                    if (streamUrl != null) {
                                        item.buildUpon()
                                            .setUri(android.net.Uri.parse(streamUrl))
                                            .build()
                                    } else item
                                } catch (e: Exception) {
                                    item
                                }
                            } else item
                        }
                    }
                    deferredItems.awaitAll().toMutableList()
                }
            }

        })
            .setSessionActivity(sessionIntent)
            .build()
    }
}
