package com.example.musicplayer.Model

import android.animation.AnimatorInflater
import android.content.Context
import android.media.MediaMetadataRetriever
import android.widget.ImageButton
import com.example.musicplayer.Activity.FavouriteActivity
import com.example.musicplayer.Activity.PlayerActivity
import com.example.musicplayer.R
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String
)

class Playlist {//Playlist là một lớp đại diện cho một danh sách phát âm nhạc.
    lateinit var name: String
    lateinit var playlist: ArrayList<Music> //đại diện cho các bài hát trong danh sách phát.
    lateinit var createBy: String //người tạo
    lateinit var createOn: String //ngày tạo
}

class MusicPlaylist {//lớp đại diện cho một danh sách các đối tượng Playlist.
    //danh sách chứa các đối tượng Playlist.
    var ref: ArrayList<Playlist> = ArrayList()
}

fun formatDuration(duration: Long):String{
    //chuyển đổi giá trị duration từ đơn vị mili giây sang đơn vị phút bằng cách sử dụng lớp TimeUnit
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    //chuyển đổi giá trị duration từ đơn vị mili giây sang đơn vị giây và sau đó trừ đi số phút đã tính ở bước trước đó để lấy số giây còn lại
    val seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes*TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImgAri(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    //khi PlayerActivity.repeat là false, chúng ta chỉ cho phép di chuyển đến bài hát tiếp theo
    // hoặc trước đó khi lặp lại bài hát đang bị tắt. Khi lặp lại bài hát đang được kích hoạt,
    // chúng ta không thay đổi vị trí bài hát và vẫn giữ nguyên vị trí hiện tại.
    if (!PlayerActivity.repeat){
        if (increment){
            if (PlayerActivity.songPosition == PlayerActivity.musicListPA.size-1)//nếu idSong là cuối ds
                PlayerActivity.songPosition = 0 // gán idSong=0 trở lại đầu ds
            else ++PlayerActivity.songPosition //tăng idSong(bài tiếp theo)
        }else{
            if (PlayerActivity.songPosition == 0) //nếu idSong là đẩu ds
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size-1 // gán idSong vào cuối ds
            else --PlayerActivity.songPosition //giảm idSong(bài trước đó)
        }
    }
}
fun exitApp(){
    if (PlayerActivity.musicService != null){
        //từ bỏ quyền kiểm soát âm thanh của dịch vụ âm nhạc (MusicService) đối với hệ thống.
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        //Dừng việc chạy dưới nền của dịch vụ âm nhạc (MusicService) bằng cách gọi phương thức
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()////MediaPlayer dc dừng và tài nguyên được giải phóng.
        //giải phóng tài nguyên và ngừng hoạt động của dịch vụ âm nhạc.
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

//ktra xem một bài hát có trong danh sách yêu thích hay k
//Hàm này trả về vtri của bài hát trong danh sách yêu thích nếu nó tồn tại, ngược lại trả về -1.
fun favouriteCheck(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.MusicListFav.forEachIndexed { index, music ->
        if (id == music.id){
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

//thêm các bài hát hợp lệ vào filteredPlaylist.
//ds filteredPlaylist, chứa các bài hát đã được ktra và không chứa các bài hát không tồn tại.
fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music> {
    val filteredPlaylist = ArrayList<Music>()
    playlist.forEach { music ->
        val file = File(music.path)
        if (file.exists()) {
            filteredPlaylist.add(music)
        }
    }
    return filteredPlaylist
}

//tham số clickHandler kiểu () -> Unit cho hàm applyClickAnimation.
// Điều này cho phép chúng ta truyền một xử lý sự kiện riêng cho mỗi nút.
fun applyClickAnimation(context: Context, button: ImageButton, clickHandler: () -> Unit) {
//    val clickAnimation = ObjectAnimator.ofPropertyValuesHolder(
//        button,
//        PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1.0f),
//        PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1.0f)
//    )
    val mAnimator = AnimatorInflater.loadAnimator(context, R.animator.button_pressed)
    button.setOnClickListener {
        mAnimator.setTarget(it)
        mAnimator.start()
        clickHandler.invoke()
    }
}
