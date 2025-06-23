package com.example.bcsd_android_2025_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

data class MusicItem(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: String,
    val albumId: Long,
    val data: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter
    private val musicList = mutableListOf<MusicItem>()


    private val musicPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadMusicFiles()
            Toast.makeText(this, "allow", Toast.LENGTH_SHORT).show()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        checkAndRequestPermission()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_music)
        musicAdapter = MusicAdapter(musicList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = musicAdapter
        }
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, musicPermission) == PackageManager.PERMISSION_GRANTED) {
            loadMusicFiles()
        } else {
            showPermissionRequestDialog()
        }
    }

    private fun showPermissionRequestDialog() {
        AlertDialog.Builder(this)
            .setTitle("음악 및 오디오 접근 권한")
            .setMessage("음악 파일을 불러오기 위해 권한이 필요합니다.")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissionLauncher.launch(musicPermission)
            }
            .setNegativeButton("Don't allow") { _, _ ->
                showPermissionDeniedDialog()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 필요!")
            .setMessage("음악 파일을 표시하려면 권한이 필요합니다.\n\nAllow permission to show files")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun loadMusicFiles() {

        val file = File("/sdcard/Music/Lil_Tecca_Dark_Thoughts.mp3")
        if (file.exists()) {
            MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null, null)
        }

        if (file.exists()) {
            val uri = Uri.fromFile(file)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
        }

        musicList.clear()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "Unknown Title"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val duration = formatDuration(it.getLong(durationColumn))
                val albumId = it.getLong(albumIdColumn)
                val data = it.getString(dataColumn) ?: ""

                musicList.add(MusicItem(id, title, artist, duration, albumId, data))
            }
        }

        musicAdapter.notifyDataSetChanged()
    }

    private fun formatDuration(durationMs: Long): String {
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, musicPermission) == PackageManager.PERMISSION_GRANTED && musicList.isEmpty()) {
            loadMusicFiles()
        }
    }
}
