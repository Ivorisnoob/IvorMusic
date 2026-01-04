package com.ivor.ivormusic.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class SpotifyRepository(
    private val spotifySessionManager: SpotifySessionManager
) {
    private val client = OkHttpClient()

    suspend fun fetchPlaylists(): List<PlaylistDisplayItem> = withContext(Dispatchers.IO) {
        val accessToken = spotifySessionManager.getAccessToken() ?: throw IllegalStateException("Not logged in")
        
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/playlists")
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(responseBody)
            val items = json.optJSONArray("items") ?: return@withContext emptyList()
            
            val playlists = mutableListOf<PlaylistDisplayItem>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val images = item.optJSONArray("images")
                val imageUrl = if (images != null && images.length() > 0) {
                    images.getJSONObject(0).optString("url")
                } else null

                val id = item.getString("id")
                playlists.add(
                    PlaylistDisplayItem(
                        name = item.getString("name"),
                        url = id, // Passing ID as url, getter will return it as is since no 'list='
                        uploaderName = item.getJSONObject("owner").getString("display_name"),
                        itemCount = item.getJSONObject("tracks").getInt("total"),
                        thumbnailUrl = imageUrl
                    )
                )
            }
            playlists
        }
    }
}
