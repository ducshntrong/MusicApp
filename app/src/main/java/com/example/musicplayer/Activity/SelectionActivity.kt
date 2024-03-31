package com.example.musicplayer.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adapter.MusicAdapter
import com.example.musicplayer.Model.Music
import com.example.musicplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectionBinding
    lateinit var musicAdapter: MusicAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeLayout()
        binding.btnBack.setOnClickListener { finish() }
        binding.searchSongSA.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null){
                    MainActivity.filteredListMA = ArrayList<Music>()
                    for (i in MainActivity.MusicListMA){
                        if (i.title.contains(newText, ignoreCase = true))
                            MainActivity.filteredListMA.add(i)
                    }
                    MainActivity.search = true //sau khi search thì set thành true
                    musicAdapter.setListMusic(MainActivity.filteredListMA)
                    binding.rvSelection.adapter = musicAdapter
                }
                return true
            }
        })
    }

    private fun initializeLayout(){
        binding.rvSelection.setHasFixedSize(true)
        binding.rvSelection.setItemViewCacheSize(10)
        binding.rvSelection.layoutManager = LinearLayoutManager(this@SelectionActivity)
        musicAdapter = MusicAdapter(this, MainActivity.MusicListMA, selectionActivity = true)
        binding.rvSelection.adapter = musicAdapter
    }
}