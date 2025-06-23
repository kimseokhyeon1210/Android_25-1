package com.example.bcsd_android_2025_1

import android.content.ContentUris
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MusicAdapter(
    private val musicList: List<MusicItem>
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.iv_album_art)
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val artist: TextView = itemView.findViewById(R.id.tv_artist)
        val duration: TextView = itemView.findViewById(R.id.tv_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val musicItem = musicList[position]

        holder.title.text = musicItem.title
        holder.artist.text = musicItem.artist
        holder.duration.text = musicItem.duration

        val albumArtUri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            musicItem.albumId
        )

        Glide.with(holder.itemView.context)
            .load(albumArtUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .centerCrop()
            )
            .into(holder.albumArt)
    }

    override fun getItemCount(): Int = musicList.size
}
