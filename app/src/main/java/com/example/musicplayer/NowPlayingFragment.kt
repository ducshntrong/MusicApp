package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Activity.FavouriteActivity
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.Model.applyClickAnimation
import com.example.musicplayer.Model.favouriteCheck
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

        applyClickAnimation(requireActivity(), binding.btnPlayPauseNP) {
            if (PlayerActivity.isPlaying) pauseMusic()
            else playMusic()
        }

        applyClickAnimation(requireActivity(), binding.btnNextNP) {
            setSongPosition(true)
            PlayerActivity.fIndex = favouriteCheck(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
            if (PlayerActivity.fIndex == -1){ //nếu bài hát k nằm trong fav thì set icon empty
                binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
            }else{
                binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
            }
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

        applyClickAnimation(requireActivity(), binding.imgBtnFav){
            PlayerActivity.fIndex = favouriteCheck(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
            if (PlayerActivity.isFavourite){
                PlayerActivity.isFavourite = false
                PlayerActivity.binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
                binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
                FavouriteActivity.MusicListFav.removeAt(PlayerActivity.fIndex)
            }else{
                PlayerActivity.isFavourite = true
                PlayerActivity.binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
                binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
                PlayerActivity.binding.imgBtnFav.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.white))
                FavouriteActivity.MusicListFav.add(PlayerActivity.musicListPA[PlayerActivity.songPosition])
            }
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
            PlayerActivity.fIndex = favouriteCheck(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
            if (PlayerActivity.fIndex == -1){
                binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
            }else{
                binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
            }
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
            if (PlayerActivity.isPlaying) binding.btnPlayPauseNP.setImageResource(R.drawable.pause_circle_outline_24)
            else binding.btnPlayPauseNP.setImageResource(R.drawable.play_circle_outline_24)
        }
    }

    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.btnPlayPauseNP.setImageResource(R.drawable.pause_circle_outline_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.btnPlayPauseNP.setImageResource(R.drawable.play_circle_outline_24)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_ic)
        PlayerActivity.isPlaying = false
    }
}