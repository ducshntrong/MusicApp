package com.example.musicplayer.Activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.*
import com.example.musicplayer.Model.*
import com.example.musicplayer.Service.MusicService
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.example.musicplayer.databinding.BottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        //var mediaPLayer: MediaPlayer? = null //đối tượng MediaPlayer để phát nhạc.
        var isPlaying:Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false //check lặp lại bài hát
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""

        var isFavourite: Boolean = false
        var fIndex: Int = -1//sd để lưu trữ vtri của bài hát trong danh sách yêu thích
    }

    lateinit var bundle: Bundle
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Xoay imgSong
        val rotationAnimator = ObjectAnimator.ofFloat(binding.imgSong, "rotation", 0f, 360f)
        rotationAnimator.duration = 10000
        rotationAnimator.repeatCount = ObjectAnimator.INFINITE
        rotationAnimator.interpolator = LinearInterpolator()
        rotationAnimator.start()

        if (intent.data?.scheme.contentEquals("content")){
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgAri(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
                .into(binding.imgSong)
            binding.tvSongName.text = musicListPA[songPosition].title
        }
        else initializeLayout()//khởi tạo Bố cục

        binding.btnPausePlay.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        applyClickAnimation(this, binding.btnNext){nextPreviousSong(true)}
        applyClickAnimation(this, binding.btnPreviou){nextPreviousSong(false)}

        binding.imgBtnBack.setOnClickListener { finish() }

        //sẽ đặt trình nghe khi nhấp chuột trên thanh seekbar để thay đổi tiến trình của seekbar khi người dùng tương thích với nó.
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //Phương thức này được gọi khi giá trị của SeekBar thay đổi
                //ktra xem sự thay đổi có được thực hiện bởi người dùng (fromUser là true) hay không
                //sd phương thức seekTo(progress) để di chuyển đến thời điểm đã kéo đến trên SeekBar.
                if (fromUser) musicService!!.mediaPlayer?.seekTo(progress)
            }

            //Phương thức này được gọi khi người dùng bắt đầu chạm vào SeekBar
            //k thực hiện bất kỳ hành động nào, do đó chỉ cần để nó trống và trả về Unit
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            //Phương thức này được gọi khi người dùng dừng chạm vào SeekBar
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        applyClickAnimation(this, binding.btnRepeatPA){
            if (!repeat){
                repeat = true
                binding.btnRepeatPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }else{
                repeat = false
                binding.btnRepeatPA.setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
        }

        applyClickAnimation(this, binding.btnEqualizerPA){
            try {
                //Intent được tạo để mở cài đặt bộ điều chỉnh âm thanh (Equalizer) cho phiên bản âm thanh đang phát
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                //Intent được gửi đi với một số thông tin bổ sung như session audio, package name và content type.
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            }catch (e: Exception){
                Toast.makeText(this, "Equalizer Feature not Supported!!", Toast.LENGTH_SHORT).show()}
        }

        applyClickAnimation(this, binding.btnTimerPA){
            //Biến timer sẽ là true nếu ít nhất 1 trong 3 biến min15, min30, min60 có gtri là true.
            val timer = min15 || min30 || min60
            //ktra nếu timer là false, tức là k có bộ đếm tgian nào được thiết lập, thì showBottomSheetDialog() được gọi
            if (!timer) showBottomSheetDialog()
            else {//Trong trường hợp đã thiết lập bộ đếm thời gian
                val dialog = AlertDialog.Builder(this)
                dialog.apply {
                    setTitle("Stop Timer")
                    setMessage("Do you want to stop timer?")
                    setNegativeButton("No"){ dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                        //chọn yes để huỷ bộ đếm tgian và đặt all về false
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this@PlayerActivity,
                            R.color.white
                        ))
                    }
                    setCancelable(true)
                }
                val customDialog = dialog.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }

        applyClickAnimation(this, binding.btnSharePA) {
            val shareIntent = Intent() //tạo đối dượng Intent
            shareIntent.action = Intent.ACTION_SEND //gán hành động mục đích của intent là chia sẻ.
            shareIntent.type = "audio/*" //chỉ định rằng dữ liệu sẽ được chia sẻ là âm thanh.
            //Intent.EXTRA_STREAM được sử dụng để chỉ định một URI của tệp âm thanh mà muốn chia sẻ.
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            //Intent.createChooser() được sử dụng để hiển thị một danh sách các ứng dụng có thể xử
            // lý intent và cho phép người dùng chọn ứng dụng để chia sẻ.
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
        }

        binding.imgBtnFav.setOnClickListener {
            fIndex = favouriteCheck(musicListPA[songPosition].id)
            if (isFavourite){
                isFavourite = false
                binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
                FavouriteActivity.MusicListFav.removeAt(fIndex)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Song was deleted",
                    Snackbar.LENGTH_SHORT).show()
            }else{
                isFavourite = true
                binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
                binding.imgBtnFav.setColorFilter(ContextCompat.getColor(this, R.color.white))
                FavouriteActivity.MusicListFav.add(musicListPA[songPosition])
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Song saved",
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return Music("Unknown", path.toString(), "Unknown", "Unknown", duration,
            path.toString(), "Unknown")
        }finally {
            cursor?.close()
        }
    }

    private fun initializeLayout(){//khởi tạo Bố cục
        bundle = intent.extras!!
        songPosition = bundle!!.getInt("index", 0)
        //danh sách nhạc và vị trí bài hát được truyền qua Intent và được sử dụng để phát nhạc từ danh sách đó.
        when(bundle.getString("class")){
            "FavouriteAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.MusicListFav)
                setLayout()
            }
            "NowPlaying" -> {
                setLayout()
                //set lại seekBar
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.btnPausePlay.setIconResource(R.drawable.pause_ic)
                else binding.btnPausePlay.setIconResource(R.drawable.play_ic)
            }
            "MusicAdapterSearch" -> {
                //starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.filteredListMA)
                setLayout()
            }
            "MusicAdapter" -> {
                //starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                //danh sách musicListPA được khởi tạo và sao chép từ MainActivity.MusicListMA.
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" -> {
                //starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()//xáo trộn ngẫu nhiên bài hát
                setLayout()
            }
            "FavouriteShuffle" -> {
                //starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.MusicListFav)
                musicListPA.shuffle()//xáo trộn ngẫu nhiên bài hát
                setLayout()
            }
            "PlaylistDetailsAdapter" -> {
                //starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist)
                setLayout()
            }
            "PlaylistDetailsShuffle" -> {
                //starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)//Tham số BIND_AUTO_CREATE chỉ định rằng dịch vụ sẽ được tạo nếu nó chưa tồn tại.
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun setLayout(){
        //fIndex được cập nhật bằng việc gọi favouriteCheck() với id của bài hát hiện tại
        //để ktra xem bài hát có trong danh sách yêu thích hay không.
        fIndex = favouriteCheck(musicListPA[songPosition].id)

        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
            .into(binding.imgSong)
        binding.tvSongName.text = musicListPA[songPosition].title
        binding.tvArtist.text = musicListPA[songPosition].artist
        binding.tvSeekBarEnd.text = formatDuration(musicListPA[songPosition].duration)
        if (repeat) binding.btnRepeatPA.setColorFilter(ContextCompat.getColor(this,
            R.color.cool_pink
        ))
        //nếu 1 trong 3 bộ đếm còn thiết lập(true) thì set màu icon là màu tím
        if (min15 || min30 || min60) binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this,
            R.color.cool_pink
        ))

        if (isFavourite) binding.imgBtnFav.setImageResource(R.drawable.favorite_ic)
        else binding.imgBtnFav.setImageResource(R.drawable.favourite_empty_ic)
    }

    private fun createMediaPlayer(){// fun tạo đối tượng MediaPlayer mới và chuẩn bị cho việc phát nhạc
        try {
            if (musicService!!.mediaPlayer == null) //ktr mediaPLayer  có null k
                musicService!!.mediaPlayer = MediaPlayer()//tạo một đối tượng MediaPlayer mới và gán cho mediaPLayer.
            musicService!!.mediaPlayer?.reset()//Đặt lại trạng thái của đối tượng mediaPLayer về trạng thái ban đầu.
            //Đặt nguồn dữ liệu (đường dẫn) cho mediaPLayer để phát nhạc.
            musicService!!.mediaPlayer?.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer?.prepare()//Chuẩn bị mediaPLayer để phát nhạc.
            musicService!!.mediaPlayer?.start()// Bắt đầu phát nhạc bằng cách gọi phương thức start()
            //nhạc đang dc phát và set icon pause
            isPlaying = true
            binding.btnPausePlay.setIconResource(R.drawable.pause_ic)
            musicService!!.showNotification(R.drawable.pause_ic)
            //set seekBar
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0 //đặt gtri hiện tại của SeekBar là 0, tức là thanh trượt sẽ ở vtri ban đầu.
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration //đặt giá trị tối đa của SeekBar
            //đăng ký PlayerActivity làm OnCompletionListener cho đối tượng mediaPlayer trong musicService
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            //gán nowPlayingId trùng vs id bài hát đang phát
            nowPlayingId = musicListPA[songPosition].id
        }catch (e: Exception){return}
    }

    private fun playMusic(){//Phát nhạc
        binding.btnPausePlay.setIconResource(R.drawable.pause_ic)
        musicService!!.showNotification(R.drawable.pause_ic)
        isPlaying = true
        musicService!!.mediaPlayer?.start()
    }

    private fun pauseMusic(){//dừng nhạc
        binding.btnPausePlay.setIconResource(R.drawable.play_ic)
        musicService!!.showNotification(R.drawable.play_ic)
        isPlaying = false
        musicService!!.mediaPlayer?.pause()
    }

    private fun nextPreviousSong(increment: Boolean){ //hàm để next hoặc pre nhạc
        setSongPosition(increment)
        setLayout()
        createMediaPlayer()
    }

    //Phương thức này được gọi khi kết nối với một dịch vụ (MusicService)
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        //ktr musicService == null được thực hiện để xác định xem có sự kết nối với dịch vụ âm nhạc
        // (MusicService) đã được thiết lập hay chưa.
        if(musicService == null){
            val binder = service as MusicService.MyBinder //ép kiểu IBinder thành MusicService.MyBinder
            musicService = binder.currentService() // trả về đối tượng MusicService được liên kết.
            //sd để thiết lập và yêu cầu quyền kiểm soát âm thanh từ hệ thống Android thông qua lớp AudioManager
            //lấy một thể hiện của lớp AudioManager từ hệ thống Android.
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            // yêu cầu quyền kiểm soát âm thanh.
            musicService!!.audioManager.requestAudioFocus(musicService,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    //Phương thức này được gọi khi kết nối với dịch vụ (MusicService) bị ngắt
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null //gán null cho biết k còn kết nối với dịch vụ.
    }

    //định nghĩa phương thức onCompletion() để xử lý sự kiện khi phát nhạc hoàn thành.
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)//true thì next nhạc
        createMediaPlayer()
        setLayout()
        NowPlayingFragment.binding.TVNameSongNP.isSelected = true
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash).centerCrop())
            .into(NowPlayingFragment.binding.songImgNP)
        NowPlayingFragment.binding.TVNameSongNP.text = musicListPA[songPosition].title
        NowPlayingFragment.binding.tvArtist.text = musicListPA[songPosition].artist
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //kiểm tra kết quả trả về từ Intent mở cài đặt Equalizer.
        if (requestCode == 13 || requestCode == RESULT_OK)
            return
        //nếu kqua trả về từ cài đặt Equalizer thành công (RESULT_OK) hoặc mã yêu cầu mở cài đặt
        // là 13, chúng ta không thực hiện bất kỳ hoạt động nào và thoát khỏi hàm.
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetDialogBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(bottomSheetBinding.root)
        dialog.show()

        //đóng app sau 1 tgian nhất định
        bottomSheetBinding.min15.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            // gán gtri true cho biến min15, để đánh dấu rằng bộ đếm thời gian 15 phút đã được thiết lập.
            min15 = true
            //1 luồng (thread) mới được tạo bằng cách sử dụng Thread và Thread.sleep()
            //để tạm dừng thực thi trong 15p
            Thread{Thread.sleep((15*60000).toLong())
            //ktra xem min15 vẫn đang là true(đảm bảo rằng bộ đếm tgian k bị hủy bỏ trong khoảng tgian chờ)
            //nếu hết 15p mà bộ đếm time chưa bị huỷ(vẫn =true) thì exitApp
            if (min15) exitApp() }.start()
            dialog.dismiss()
        }
        bottomSheetBinding.min30.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            min30 = true
            Thread{Thread.sleep((30*60000).toLong())
            if (min30) exitApp() }.start()
            dialog.dismiss()
        }
        bottomSheetBinding.min60.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            min60 = true
            Thread{Thread.sleep((60*60000).toLong())
                if (min60) exitApp() }.start()
            dialog.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
        val editor = getSharedPreferences("Favourite", Context.MODE_PRIVATE).edit()
        val jsonFav = GsonBuilder().create().toJson(FavouriteActivity.MusicListFav)
        editor.putString("musicListFav", jsonFav)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitApp()
    }
}