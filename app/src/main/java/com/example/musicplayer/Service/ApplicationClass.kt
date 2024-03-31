package com.example.musicplayer.Service

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

//tạo ApplicationClass cho kênh thông báo trong ứng dụng trình phát nhạc của mình.
//sử dụng để cấu hình và khởi tạo các thành phần chung cho toàn bộ ứng dụng.
class ApplicationClass: Application() {
    companion object{
        const val CHANNEL_ID = "channel1"//xác định kênh thông báo trong Android.
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }
    override fun onCreate() {
        super.onCreate()
        //kiểm tra phiên bản Android của thiết bị và nếu phiên bản là Android Oreo(API level 26) hoặc cao hơn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //một đối tượng NotificationChannel mới
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Now Playing Song", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is a important channel for showing song!!"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //tạo kênh thông báo.
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}