package com.jarimanis.jarimanis.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeVideoPlayer(videoId: String, lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isFullscreen by remember { mutableStateOf(false) }

    // Jika pengguna menekan tombol "Back" (Kembali) di HP saat mode Fullscreen,
    // maka keluar dari Fullscreen, bukan malah keluar dari aplikasi.
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    AndroidView(
        factory = { ctx ->
            val youTubePlayerView = YouTubePlayerView(ctx)
            lifecycleOwner.lifecycle.addObserver(youTubePlayerView)

            val playerOptions = IFramePlayerOptions.Builder(ctx)
                .controls(1)
                .fullscreen(1)
                .build()

            youTubePlayerView.enableAutomaticInitialization = false

            // --- INI KUNCI ANTI-CRASH: Mendaftarkan FullscreenListener ---
            youTubePlayerView.addFullscreenListener(object : FullscreenListener {
                override fun onEnterFullscreen(fullscreenView: android.view.View, exitFullscreen: () -> Unit) {
                    isFullscreen = true
                    // Menyembunyikan YouTubePlayer biasa
                    youTubePlayerView.visibility = android.view.View.GONE

                    // Membuat jendela layar penuh (memakan seluruh layar HP)
                    val decorView = activity?.window?.decorView as? FrameLayout
                    decorView?.addView(
                        fullscreenView,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )

                    // Putar HP secara otomatis ke mode Landscape
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                    // Sembunyikan status bar agar benar-benar penuh (Immersive Mode)
                    activity?.window?.let { window ->
                        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
                        decorView?.let { view ->
                            androidx.core.view.WindowInsetsControllerCompat(window, view).apply {
                                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            }
                        }
                    }
                }

                override fun onExitFullscreen() {
                    isFullscreen = false
                    // Munculkan YouTubePlayer biasa lagi
                    youTubePlayerView.visibility = android.view.View.VISIBLE

                    // Lepaskan jendela layar penuh
                    val decorView = activity?.window?.decorView as? FrameLayout
                    decorView?.removeViewAt(decorView.childCount - 1)

                    // Kembalikan HP ke mode berdiri (Portrait) atau sesuai sensor
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                    // Kembalikan status bar (Keluar dari Immersive Mode)
                    activity?.window?.let { window ->
                        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
                        decorView?.let { view ->
                            androidx.core.view.WindowInsetsControllerCompat(window, view).show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                        }
                    }
                }
            })

            youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.cueVideo(videoId, 0f)
                }
            }, playerOptions)

            youTubePlayerView
        },
        modifier = Modifier.fillMaxWidth()
    )
}