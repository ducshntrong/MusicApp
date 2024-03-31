package com.example.musicplayer.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.Adapter.FavouriteAdapter
import com.example.musicplayer.Model.Music
import com.example.musicplayer.Model.checkPlaylist
import com.example.musicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
    lateinit var favouriteAdapter: FavouriteAdapter
    private lateinit var binding: ActivityFavouriteBinding

    companion object{
        var MusicListFav: ArrayList<Music> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MusicListFav = checkPlaylist(MusicListFav)
        initializeLayout()
        binding.btnBack.setOnClickListener { finish() }
        if(MusicListFav.size < 1) binding.btnShuffle.visibility = View.INVISIBLE
        if(MusicListFav.isNotEmpty()) binding.instructionFV.visibility = View.GONE
        binding.btnShuffle.setOnClickListener {
            val i = Intent(this@FavouriteActivity, PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("index", 0)
            bundle.putString("class", "FavouriteShuffle")
            i.putExtras(bundle)
            startActivity(i)
        }
    }

    private fun initializeLayout(){
        binding.rvFavourite.setHasFixedSize(true)
        binding.rvFavourite.setItemViewCacheSize(13)
        binding.rvFavourite.layoutManager = GridLayoutManager(this,
            2, GridLayoutManager.VERTICAL, false)
        favouriteAdapter = FavouriteAdapter(this, MusicListFav)
        binding.rvFavourite.adapter = favouriteAdapter
    }

    override fun onResume() {//Cập nhật lại view khi data thay đổi(bị xoá đi)
        super.onResume()
        favouriteAdapter.musicListFav(MusicListFav)
    }

}