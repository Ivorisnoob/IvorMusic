package com.ivor.ivormusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ivor.ivormusic.ui.home.HomeScreen
import com.ivor.ivormusic.ui.home.HomeViewModel
import com.ivor.ivormusic.ui.player.PlayerViewModel
import com.ivor.ivormusic.ui.theme.IvorMusicTheme
import com.ivor.ivormusic.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val loadLocalSongs by themeViewModel.loadLocalSongs.collectAsState()
            
            IvorMusicTheme(darkTheme = isDarkMode) {
                MusicApp(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { themeViewModel.setDarkMode(it) },
                    loadLocalSongs = loadLocalSongs,
                    onLoadLocalSongsToggle = { themeViewModel.setLoadLocalSongs(it) }
                )
            }
        }
    }
}

@Composable
fun MusicApp(
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    loadLocalSongs: Boolean,
    onLoadLocalSongsToggle: (Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = remember { PlayerViewModel(context) }
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                },
                playerViewModel = playerViewModel,
                viewModel = homeViewModel,
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToDownloads = { navController.navigate("downloads") },
                loadLocalSongs = loadLocalSongs
            )
        }
        composable("settings") {
            com.ivor.ivormusic.ui.settings.SettingsScreen(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                loadLocalSongs = loadLocalSongs,
                onLoadLocalSongsToggle = onLoadLocalSongsToggle,
                onLogoutClick = { 
                    homeViewModel.logout()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("downloads") {
            val downloadedSongs by playerViewModel.downloadedSongs.collectAsState()
            val downloadProgress by playerViewModel.downloadProgress.collectAsState()
            
            com.ivor.ivormusic.ui.downloads.DownloadsScreen(
                downloadedSongs = downloadedSongs,
                activeDownloads = downloadProgress,
                onBack = { navController.popBackStack() },
                onPlaySong = { song -> 
                    playerViewModel.playSong(song)
                },
                onDeleteDownload = { songId -> 
                    playerViewModel.deleteDownload(songId)
                },
                onCancelDownload = { songId -> 
                    playerViewModel.cancelDownload(songId)
                },
                onRetryDownload = { song -> 
                    playerViewModel.toggleDownload(song)
                }
            )
        }
    }
}