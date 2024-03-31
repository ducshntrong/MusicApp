package com.example.musicplayer.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.Activity.PlaylistActivity
import com.example.musicplayer.Activity.PlaylistDetailsActivity
import com.example.musicplayer.Model.Music
import com.example.musicplayer.Model.formatDuration
import com.example.musicplayer.R
import com.example.musicplayer.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var musicList: ArrayList<Music>,
                   private var playlistDetails: Boolean = false, private val selectionActivity: Boolean = false):
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {
    class MusicViewHolder(binding: MusicViewBinding): RecyclerView.ViewHolder(binding.root) {
        val songName = binding.songName
        val songAlbum = binding.songAlbumMV
        val imgMusicView = binding.imgMusicView
        val duration = binding.songDuration
        val root = binding.root
    }

    fun setListMusic(musicList: ArrayList<Music>){
        this.musicList = musicList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(MusicViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.songName.text = musicList[position].title
        holder.songAlbum.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context).
            load(musicList[position].artUri).
            apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop()).
            into(holder.imgMusicView)
        when{
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlaylistDetailsAdapter", position)
                }
            }
            selectionActivity -> {
                holder.root.setOnClickListener {
                    if (addSong(musicList[position])){
                        when(MainActivity.themeIndex){
                            0 -> holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_pink))
                            1 -> holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_blue))
                            2 -> holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_purple))
                            3 -> holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_green))
                            4 -> holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.black2))
                        }
                    }else{
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
            }
            else ->{
                holder.root.setOnClickListener {
                    when{//ktr xem gtri là true hay false (có search hay k search)
                        MainActivity.search -> sendIntent("MusicAdapterSearch", position)
                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent("NowPlaying", PlayerActivity.songPosition)
                        else -> sendIntent("MusicAdapter", position)
                    }
                }
            }
        }

    }

    private fun sendIntent(ref: String, pos: Int){
        // tạo ra một Intent để chuyển sang PlayerActivity khi người dùng nhấp vào một phần tử trong danh sách
        val i = Intent(context, PlayerActivity::class.java)
        val bundle = Bundle()
        bundle.putInt("index", pos)
        bundle.putString("class", ref)
        i.putExtras(bundle)
        context.startActivity(i)
    }

    //thêm bài hát vào ds phát hiện tại
    private fun addSong(song: Music): Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            //vòng lặp forEachIndexed được sd để duyệt qua các bài hát trong ds phát hiện tại.
            if (song.id == music.id){
                //nếu song.id trùng khớp với music.id (điều này ngụ ý rằng bài hát đã tồn tại trong danh sách phát)
                PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        //Nếu không có bài hát nào trùng khớp, bài hát mới sẽ được thêm vào ds phát
        PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.add(song)
        return true
    }
}