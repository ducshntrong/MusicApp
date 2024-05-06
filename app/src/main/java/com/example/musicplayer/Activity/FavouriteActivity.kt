package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adapter.FavouriteAdapter
import com.example.musicplayer.Model.Music
import com.example.musicplayer.Model.checkPlaylist
import com.example.musicplayer.NowPlayingFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
    private lateinit var favouriteAdapter: FavouriteAdapter

    companion object{
        //data MusicListFav dc lấy từ playerActivity khi click nút fav
        var MusicListFav: ArrayList<Music> = ArrayList()
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityFavouriteBinding
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MusicListFav = checkPlaylist(MusicListFav)
        initializeLayout()
        binding.btnBack.setOnClickListener { finish() }
        if(MusicListFav.isNotEmpty()) binding.instructionFV.visibility = View.GONE
        binding.btnShuffle.setOnClickListener{
            if (MusicListFav.isNotEmpty()){
                val i = Intent(this@FavouriteActivity, PlayerActivity::class.java)
                val bundle = Bundle()
                bundle.putInt("index", 0)
                bundle.putString("class", "FavouriteShuffle")
                i.putExtras(bundle)
                startActivity(i)
            }else{Toast.makeText(this, "Your playlist is currently empty!!", Toast.LENGTH_SHORT).show()}
        }

    }

    private fun initializeLayout(){
        binding.rvFavourite.setHasFixedSize(true)
        binding.rvFavourite.setItemViewCacheSize(13)
        binding.rvFavourite.layoutManager = LinearLayoutManager(this@FavouriteActivity)
        favouriteAdapter = FavouriteAdapter(this, MusicListFav)
        binding.rvFavourite.adapter = favouriteAdapter
        binding.tvTotalSong.text = "Total Songs: ${favouriteAdapter.itemCount}"
    }

    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        NowPlayingFragment.binding.btnPlayPauseNP.setImageResource(R.drawable.pause_circle_outline_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        NowPlayingFragment.binding.btnPlayPauseNP.setImageResource(R.drawable.play_circle_outline_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_ic)
        PlayerActivity.isPlaying = false
    }

    override fun onResume() {//Cập nhật lại view khi data thay đổi(bị xoá đi)
        super.onResume()
        favouriteAdapter.musicListFav(MusicListFav)
    }

}