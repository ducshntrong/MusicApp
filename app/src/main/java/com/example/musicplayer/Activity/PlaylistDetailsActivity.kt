package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Adapter.MusicAdapter
import com.example.musicplayer.Model.checkPlaylist
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityPlaylistDetailsBinding

class PlaylistDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistDetailsBinding
    lateinit var musicAdapter: MusicAdapter
    companion object{
        var currentPlaylistPos: Int = -1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeLayout()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnShuffle.setOnClickListener {
            val i = Intent(this@PlaylistDetailsActivity, PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("index", 0)
            bundle.putString("class", "PlaylistDetailsShuffle")
            i.putExtras(bundle)
            startActivity(i)
        }
        binding.addBtnPD.setOnClickListener {
            val i = Intent(this@PlaylistDetailsActivity, SelectionActivity::class.java)
            startActivity(i)
        }

        binding.removeAllPD.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.apply {
                setTitle("Remove All Songs")
                setMessage("Do you want to remove all songs from playlist?")
                setNegativeButton("No"){ dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                   PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                    musicAdapter.setListMusic(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
                }
                setCancelable(true)
            }
            val customDialog = dialog.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun initializeLayout(){
        currentPlaylistPos = intent.extras!!.getInt("index")
        try {
            PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist =
                checkPlaylist(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
        }catch (e:Exception){}
        binding.rvPlaylistDetail.setHasFixedSize(true)
        binding.rvPlaylistDetail.setItemViewCacheSize(10)
        binding.rvPlaylistDetail.layoutManager = LinearLayoutManager(this@PlaylistDetailsActivity)
        musicAdapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist, true)
        binding.rvPlaylistDetail.adapter = musicAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setLayout(){
        binding.tvPlaylistName.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.tvTotalSong.text = "Total 10 Songs"
        binding.tvCreateOn.text = "Create On: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createOn}"
        binding.tvCreateBy.text = "-- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createBy}"
        if (musicAdapter.itemCount > 0){
            Glide.with(this).
            load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri).
            apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop()).
            into(binding.playlistImgPD)
            binding.btnShuffle.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        setLayout()
        musicAdapter.notifyDataSetChanged()
//        val editor = getSharedPreferences("Favourite", Context.MODE_PRIVATE).edit()
//        val jsonPl = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
//        editor.putString("musicPlaylist", jsonPl)
//        editor.apply()
    }
}