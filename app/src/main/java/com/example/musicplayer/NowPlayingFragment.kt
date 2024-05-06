package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.Model.setSongPosition
import com.example.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex], true)
        binding = FragmentNowPlayingBinding.inflate(layoutInflater)
        binding.root.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPlayPauseNP.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic()
            else playMusic()
        }

        binding.btnNextNP.setOnClickListener {
            setSongPosition(true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
                .into(binding.songImgNP)
            binding.TVNameSongNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            binding.tvArtist.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].artist
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
            playMusic()
        }

        binding.root.setOnClickListener {
            //requireContext() được sd để lấy Context của Fragment hiện tại.
            val i = Intent(requireContext(), PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("index", PlayerActivity.songPosition)
            bundle.putString("class", "NowPlaying")
            i.putExtras(bundle)
            val activityOptions = ActivityOptionsCompat.makeCustomAnimation(
                requireContext(), R.anim.slide_up, R.anim.fade_out
            )
            startActivity(i, activityOptions.toBundle())
        }
    }

    override fun onResume() {
        super.onResume()
        //Nếu không phải null, tức là dịch vụ âm nhạc (musicService) đã được khởi tạo(có bài hát đang phát)
        if (PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            binding.TVNameSongNP.isSelected = true //cuộn chữ
            binding.tvArtist.isSelected = true
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
                .into(binding.songImgNP)
            binding.TVNameSongNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            binding.tvArtist.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].artist
            //Nếu nhạc đang chạy  thì setIcon thành icon pause
            if (PlayerActivity.isPlaying) binding.btnPlayPauseNP.setIconResource(R.drawable.pause_ic)
            else binding.btnPlayPauseNP.setIconResource(R.drawable.play_ic)
        }
    }

    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.btnPlayPauseNP.setIconResource(R.drawable.pause_ic)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.btnPlayPauseNP.setIconResource(R.drawable.play_ic)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_ic)
        PlayerActivity.isPlaying = false
    }
}