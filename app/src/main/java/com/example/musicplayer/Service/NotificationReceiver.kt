package com.example.musicplayer.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.Model.exitApp
import com.example.musicplayer.Model.favouriteCheck
import com.example.musicplayer.Model.formatDuration
import com.example.musicplayer.Model.setSongPosition
import com.example.musicplayer.NowPlayingFragment
import com.example.musicplayer.R

//Lớp NotificationReceiver là một BroadcastReceiver được sử dụng để xử lý các hành động từ thông báo
class NotificationReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){ //ktr hành động của Intent để xác định hành động từ thông báo.
            ApplicationClass.PREVIOUS -> nextPreviousSong(false, context!!)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> nextPreviousSong(true, context!!)
            ApplicationClass.EXIT -> {
                exitApp()
            }
        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer?.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
        PlayerActivity.binding.btnPausePlay.setIconResource(R.drawable.pause_ic)
        NowPlayingFragment.binding.btnPlayPauseNP.setIconResource(R.drawable.pause_ic)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer?.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_ic)
        PlayerActivity.binding.btnPausePlay.setIconResource(R.drawable.play_ic)
        NowPlayingFragment.binding.btnPlayPauseNP.setIconResource(R.drawable.play_ic)
    }

    private fun nextPreviousSong(increment: Boolean, context: Context){
        setSongPosition(increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        setLayoutPA(context)
        playMusic()
    }

    private fun setLayoutPA(context: Context) {
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
            .into(PlayerActivity.binding.imgSong)
        PlayerActivity.binding.tvSongName.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.binding.tvSeekBarEnd.
            text = formatDuration(PlayerActivity.musicListPA[PlayerActivity.songPosition].duration)

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
            .into(NowPlayingFragment.binding.songImgNP)
        NowPlayingFragment.binding.TVNameSongNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        NowPlayingFragment.binding.tvArtist.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].artist

        PlayerActivity.fIndex = favouriteCheck(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite) PlayerActivity.binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
        else PlayerActivity.binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
    }
}