package com.example.musicplayer.Service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.Model.formatDuration
import com.example.musicplayer.Model.getImgAri
import com.example.musicplayer.NowPlayingFragment
import com.example.musicplayer.R

// Lớp này để phát các bài hát ngay cả sau khi đóng ứng dụng.
//Lớp Service được sử dụng để thực hiện các hoạt động dài hạn và không giao diện người dùng trực tiếp.
class MusicService: Service(), AudioManager.OnAudioFocusChangeListener {
    //xử lý cuộc gọi đến và các thay đổi âm thanh khác bằng lớp audiomanager.
    lateinit var audioManager: AudioManager

    private var myBinder = MyBinder()//sd để trả về một đối tượng Binder từ phương thức onBind().
    var mediaPlayer:MediaPlayer? = null //đối tượng MediaPlayer để phát nhạc.
    //sử dụng đối tượng MediaSessionCompat để quản lý các sự kiện phát nhạc, như phát, dừng, tua, tiến lên...
    private lateinit var mediaSession: MediaSessionCompat //(quản lý và điều khiển trạng thái phát nhạc)
    //biến runnable kiểu Runnable để thực hiện một tác vụ lặp đi lặp lại.
    private lateinit var runnable: Runnable

    //Phương thức này đc gọi khi 1 thành phần bên ngoài muốn liên kết với MusicService
    //trả về 1 đối tượng IBinder để cho phép giao tiếp giữa MusicService và thành phần bên ngoài.
    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My  Music")
        return myBinder
    }

    inner class MyBinder: Binder(){ //sd để cung cấp 1 đối tượng Binder để liên kết với MusicService.
        fun currentService(): MusicService {//phương thức để truy cập vào chính đối tượng MusicService từ bên ngoài.
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn: Int){
        //Nếu phiên bản Android hiện tại không phải là S+
        // PendingIntent.FLAG_IMMUTABLE chỉ khi phiên bản Android hiện tại là S+ (API level 31 trở lên).
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        //click vào Notification thì quay lại lại app
        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        //tạo các Intent và PendingIntent cho các hành động trong thông báo.
        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.PREVIOUS
        )
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.PLAY
        )
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.NEXT
        )
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.EXIT
        )
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        val imgArt = getImgAri(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val imgSong = if (imgArt != null){
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_splash)
        }
        //baseContext: là ngữ cảnh hoạt động của ứng dụng
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)//set click vào Notification
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_note_ic)
            .setLargeIcon(imgSong)
            //hiển thị các điều khiển nhạc trong thông báo và cho phép người dùng tương tác với trình phát nhạc từ thông báo.
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.skip_previous_ic, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.skip_next_ic, "Next", nextPendingIntent)
            .addAction(R.drawable.close_ic, "Exit", exitPendingIntent)
            .build()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            mediaSession.setMetadata(MediaMetadataCompat.Builder()
//                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
//                .build())
//            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
//                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playBackSpeed)
//                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                .build())
//        }
        //Đặt dịch vụ của ứng dụng trong chế độ "foreground" và hiển thị thông báo đã xây dựng với độ ưu tiên là 13.
        startForeground(13, notification)
    }

    fun createMediaPlayer(){// fun tạo đối tượng MediaPlayer mới và chuẩn bị cho việc phát nhạc
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) //ktr mediaPLayer  có null k
                PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()//tạo một đối tượng MediaPlayer mới và gán cho mediaPLayer.
            PlayerActivity.musicService!!.mediaPlayer?.reset()//Đặt lại trạng thái của đối tượng mediaPLayer về trạng thái ban đầu.
            //Đặt nguồn dữ liệu (đường dẫn) cho mediaPLayer để phát nhạc.
            PlayerActivity.musicService!!.mediaPlayer?.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer?.prepare()//Chuẩn bị mediaPLayer để phát nhạc.
            PlayerActivity.binding.btnPausePlay.setIconResource(R.drawable.pause_ic)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
            //set View seekBar
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration

            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        }catch (e: Exception){return}
    }

    fun seekBarSetup(){
        //thiết lập một Runnable để cập nhật liên tục các thành phần giao diện người dùng liên quan
        // đến SeekBar và thời gian phát nhạc trong lớp MusicService.
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            //cập nhật giá trị hiện tại của SeekBar (seekBarPA) với giá trị currentPosition
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            //sd Handler và postDelayed() để lặp lại tác vụ sau mỗi khoảng tgian 200ms.
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        //gọi postDelayed() lần đầu tiên với tgian 0ms để bắt đầu thực hiện tác vụ ngay lập tức.
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    //được gọi khi trạng thái của quyền kiểm soát âm thanh thay đổi
    //sd để điều khiển việc pause và play nhạc dựa trên trạng thái của quyền kiểm soát âm thanh.
    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            //Nếu focusChange <= 0, có nghĩa là app đã mất quyền kiểm soát âm thanh hoặc
            // đang có các app khác yêu cầu quyền kiểm soát âm thanh.
            //pause music
            PlayerActivity.binding.btnPausePlay.setIconResource(R.drawable.play_ic)
            NowPlayingFragment.binding.btnPlayPauseNP.setIconResource(R.drawable.play_ic)
            showNotification(R.drawable.play_ic)
            PlayerActivity.isPlaying = false
            mediaPlayer?.pause()
        }else{// nghĩa là ứng dụng đã nhận lại quyền kiểm soát âm thanh
            //play music
            PlayerActivity.binding.btnPausePlay.setIconResource(R.drawable.pause_ic)
            NowPlayingFragment.binding.btnPlayPauseNP.setIconResource(R.drawable.pause_ic)
            showNotification(R.drawable.pause_ic)
            PlayerActivity.isPlaying = true
            mediaPlayer?.start()
        }
    }
}