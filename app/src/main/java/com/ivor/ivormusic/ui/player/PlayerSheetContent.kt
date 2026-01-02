package com.ivor.ivormusic.ui.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.ivor.ivormusic.data.Song
import com.ivor.ivormusic.data.SongSource
import com.ivor.ivormusic.ui.theme.IvorMusicTheme

/**
 * Player content designed to be shown inside a ModalBottomSheet.
 * Uses Material 3 Expressive components with bouncy animations.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSheetContent(
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleModeEnabled by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val currentQueue by viewModel.currentQueue.collectAsState()
    
    var isFavorite by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }

    val surfaceColor = Color.Black // Force black background
    val onSurfaceColor = Color.White
    val onSurfaceVariantColor = Color.White.copy(alpha = 0.7f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryContainerColor = Color(0xFF1A1A1A) // Darker container for black theme
    
    // Total black background as requested
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = onSurfaceColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = if (showQueue) "Up Next" else "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                
                IconButton(onClick = { showQueue = !showQueue }) {
                    Icon(
                        imageVector = if (showQueue) Icons.Rounded.MusicNote else Icons.Default.QueueMusic,
                        contentDescription = "Toggle Queue",
                        tint = if (showQueue) primaryColor else onSurfaceColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Crossfade(targetState = showQueue, label = "PlayerQueueTransition") { isQueueVisible ->
                if (isQueueVisible) {
                    QueueView(
                        queue = currentQueue,
                        currentSong = currentSong,
                        onSongClick = { song -> viewModel.playQueue(currentQueue, song) },
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        primaryColor = primaryColor
                    )
                } else {
                    PlayerView(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        progress = progress,
                        duration = duration,
                        shuffleModeEnabled = shuffleModeEnabled,
                        repeatMode = repeatMode,
                        isFavorite = isFavorite,
                        onFavoriteToggle = { isFavorite = it },
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        secondaryContainerColor = secondaryContainerColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerView(
    currentSong: Song?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    progress: Long,
    duration: Long,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    isFavorite: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    viewModel: PlayerViewModel,
    primaryColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    secondaryContainerColor: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Album Art with Large Expressive Corner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(48.dp),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (currentSong?.albumArtUri != null || currentSong?.thumbnailUrl != null) {
                    AsyncImage(
                        model = currentSong?.highResThumbnailUrl ?: currentSong?.albumArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(140.dp),
                        tint = onSurfaceVariantColor.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Song Info and Favorite
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (currentSong?.title.takeIf { !it.isNullOrBlank() && !it.startsWith("Unknown") } ?: "Untitled Song"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = onSurfaceColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = (currentSong?.artist.takeIf { !it.isNullOrBlank() && !it.startsWith("Unknown") } ?: "Unknown Artist"),
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = onFavoriteToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else onSurfaceColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Wavy Progress Bar with Enhanced Styling
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(contentAlignment = Alignment.Center) {
                val progressFraction = if (duration > 0) progress.toFloat() / duration.toFloat() else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "Progress"
                )
                
                val thickStrokeWidth = with(LocalDensity.current) { 6.dp.toPx() }
                val thickStroke = Stroke(width = thickStrokeWidth, cap = StrokeCap.Round)

                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    stroke = thickStroke,
                    trackStroke = thickStroke,
                    color = primaryColor,
                    trackColor = onSurfaceVariantColor.copy(alpha = 0.15f)
                )

                // Transparent Slider for interaction
                Slider(
                    value = progress.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(progress),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Playback Controls with Shape Morphing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = { viewModel.skipToPrevious() },
                modifier = Modifier.size(64.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = secondaryContainerColor
                )
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
            }

            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(92.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                // Play/Pause Button with shape morphing or Loading
                if (isBuffering && !isPlaying) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {  
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            FilledIconButton(
                onClick = { viewModel.skipToNext() },
                modifier = Modifier.size(64.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = secondaryContainerColor
                )
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Secondary Controls (Shuffle & Repeat)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconToggleButton(
                checked = shuffleModeEnabled,
                onCheckedChange = { viewModel.toggleShuffle() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(28.dp),
                    tint = if (shuffleModeEnabled) primaryColor else onSurfaceVariantColor
                )
            }
            
            IconToggleButton(
                checked = repeatMode != Player.REPEAT_MODE_OFF,
                onCheckedChange = { viewModel.toggleRepeat() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    modifier = Modifier.size(28.dp),
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) primaryColor else onSurfaceVariantColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun QueueView(
    queue: List<Song>,
    currentSong: Song?,
    onSongClick: (Song) -> Unit,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    primaryColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (queue.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Queue is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceVariantColor
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
                    val isCurrent = song.id == currentSong?.id
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCurrent) primaryColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onSongClick(song) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            AsyncImage(
                                model = song.thumbnailUrl ?: song.albumArtUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) primaryColor else onSurfaceColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrent) primaryColor.copy(alpha = 0.7f) else onSurfaceVariantColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Playing",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (index < queue.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = onSurfaceColor.copy(alpha = 0.05f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
