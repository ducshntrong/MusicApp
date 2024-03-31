package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Adapter.PlaylistAdapter
import com.example.musicplayer.Model.MusicPlaylist
import com.example.musicplayer.Model.Playlist
import com.example.musicplayer.databinding.ActivityPlaylistBinding
import com.example.musicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    lateinit var playlistAdapter: PlaylistAdapter

    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeLayout()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener {customAlertDialog()}
        deletePlaylist()
    }

    private fun customAlertDialog() {
        val dialogBinding = AddPlaylistDialogBinding.inflate(LayoutInflater.from(this@PlaylistActivity))
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(dialogBinding.root).apply {
            setTitle("Playlist Details")
            setPositiveButton("ADD") { _: DialogInterface, i: Int ->
                val playlistName = dialogBinding.playlistName.text.toString()
                val createBy = dialogBinding.yourName.text.toString()
                if (playlistName != null && createBy != null){
                    if (playlistName.isNotEmpty() && createBy.isNotEmpty()){
                        addPlaylist(playlistName, createBy)
                    }
                }
            }
        }.show()
    }
    private fun initializeLayout(){
        binding.rvPlaylist.setHasFixedSize(true)
        binding.rvPlaylist.setItemViewCacheSize(10)
        binding.rvPlaylist.layoutManager = GridLayoutManager(this,
            2, GridLayoutManager.VERTICAL, false)
        playlistAdapter = PlaylistAdapter(this, musicPlaylist.ref)
        binding.rvPlaylist.adapter = playlistAdapter
    }

    private fun addPlaylist(playlistName: String, createBy: String) {
        var playListExits = false
        for (i in musicPlaylist.ref){
            if (playlistName == i.name){
                playListExits = true
                break
            }
        }
        if (playListExits) Toast.makeText(this, "Playlist Exits!!", Toast.LENGTH_SHORT).show()
        else{
            val tempPlaylist = Playlist()
            tempPlaylist.name = playlistName
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createBy = createBy
            val cal = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createOn = sdf.format(cal)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.setMusicPlaylist(musicPlaylist.ref)
        }
    }

    private fun deletePlaylist(){
        val itemTouchHelper = object :ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val playlist = playlistAdapter.getPlaylistByPosition(position)
                musicPlaylist.ref.remove(playlist)
                showDeleteSnackBar(playlist)
                playlistAdapter.setMusicPlaylist(musicPlaylist.ref)
            }
        }
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.rvPlaylist)
    }

    private fun showDeleteSnackBar(playlist: Playlist) {
        Snackbar.make(findViewById(android.R.id.content), "Playlist was delete", Snackbar.LENGTH_LONG).apply {
            setAction("Undo"){
                musicPlaylist.ref.add(playlist)
                playlistAdapter.setMusicPlaylist(musicPlaylist.ref)
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        val editor = getSharedPreferences("Favourite", Context.MODE_PRIVATE).edit()
        val jsonPl = GsonBuilder().create().toJson(musicPlaylist)
        editor.putString("musicPlaylist", jsonPl)
        editor.apply()
    }
}