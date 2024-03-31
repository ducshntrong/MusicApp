package com.example.musicplayer.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Activity.PlaylistActivity
import com.example.musicplayer.Activity.PlaylistDetailsActivity
import com.example.musicplayer.Model.Playlist
import com.example.musicplayer.R
import com.example.musicplayer.databinding.PlaylistViewBinding

class PlaylistAdapter(private val context: Context, private var musicPlayList: ArrayList<Playlist>) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>() {
    inner class PlaylistHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {
        val imgPlaylist = binding.imgPlaylist
        val namePlaylist = binding.tvNamePlaylist
        val root = binding.root
    }

    fun setMusicPlaylist(musicPlayList: ArrayList<Playlist>){
        this.musicPlayList = musicPlayList
        notifyDataSetChanged()
    }

    fun getPlaylistByPosition(pos: Int): Playlist {
        return musicPlayList[pos]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        return PlaylistHolder(PlaylistViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return musicPlayList.size
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        holder.namePlaylist.text = musicPlayList[position].name
        holder.namePlaylist.isSelected = true
        if (PlaylistActivity.musicPlaylist.ref[position].playlist.size > 0){
            Glide.with(context).
            load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri).
            apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop()).
            into(holder.imgPlaylist)
        } else{
            Glide.with(context).
            load(R.drawable.music_player_icon_splash).
            into(holder.imgPlaylist)
        }

        holder.root.setOnClickListener {
            val i = Intent(context, PlaylistDetailsActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("index", position)
            i.putExtras(bundle)
            context.startActivity(i)
        }
    }
}