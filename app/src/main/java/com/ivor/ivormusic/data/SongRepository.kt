package com.ivor.ivormusic.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SongRepository(private val context: Context) {

    /**
     * Get all songs from the device, optionally filtering out excluded folders.
     * @param excludedFolders Set of folder paths to exclude from results
     */
    suspend fun getSongs(excludedFolders: Set<String> = emptySet()): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA // File path for folder filtering
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val filePath = cursor.getString(dataColumn) ?: ""

                // Check if this song's folder is excluded
                val parentFolder = File(filePath).parent ?: ""
                if (excludedFolders.any { excluded -> 
                    parentFolder == excluded || parentFolder.startsWith("$excluded/") 
                }) {
                    continue
                }

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                songs.add(Song.fromLocal(id, title, artist, album, duration, contentUri, albumArtUri, filePath))
            }
        }
        songs
    }
    
    /**
     * Get all unique folders that contain music files.
     * Used for the folder exclusion UI.
     */
    suspend fun getAvailableFolders(): List<FolderInfo> = withContext(Dispatchers.IO) {
        val folders = mutableMapOf<String, Int>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumn) ?: continue
                val parentFolder = File(filePath).parent ?: continue
                folders[parentFolder] = (folders[parentFolder] ?: 0) + 1
            }
        }
        
        folders.map { (path, count) -> 
            FolderInfo(
                path = path,
                displayName = File(path).name,
                songCount = count
            )
        }.sortedBy { it.displayName.lowercase() }
    }
}

/**
 * Information about a folder containing music files.
 */
data class FolderInfo(
    val path: String,
    val displayName: String,
    val songCount: Int
)

