package com.example.musicplayer.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.Model.Music
import com.example.musicplayer.Model.formatDuration
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FavouriteViewBinding

class FavouriteAdapter(private val context: Context, private var musicFavList: ArrayList<Music>) :RecyclerView.Adapter<FavouriteAdapter.FavouriteHolder>() {
    inner class FavouriteHolder(binding: FavouriteViewBinding): RecyclerView.ViewHolder(binding.root) {
        val songNameFav = binding.songName
        val imgSongFav = binding.imgMusicView
        val artistFav = binding.songArtist
        val durationFav = binding.songDuration
        val root = binding.itemFavParent
    }

    @SuppressLint("NotifyDataSetChanged")
    fun musicListFav(musicFavList: ArrayList<Music>){
        this.musicFavList = musicFavList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteHolder {
        return FavouriteHolder(FavouriteViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return musicFavList.size
    }

    override fun onBindViewHolder(holder: FavouriteHolder, position: Int) {
        holder.songNameFav.text = musicFavList[position].title
        holder.songNameFav.isSelected = true
        holder.artistFav.text = musicFavList[position].artist
        holder.durationFav.text = formatDuration(musicFavList[position].duration)
        Glide.with(context)
            .load(musicFavList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash)).centerCrop()
            .into(holder.imgSongFav)

        holder.root.setOnClickListener {
            val i = Intent(context, PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("index", position)
            bundle.putString("class", "FavouriteAdapter")
            i.putExtras(bundle)
            val activityOptions = ActivityOptionsCompat.makeCustomAnimation(
                context, R.anim.slide_up, R.anim.fade_out
            )
            context.startActivity(i, activityOptions.toBundle())
        }
    }
}

