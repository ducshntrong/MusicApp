package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adapter.MusicAdapter
import com.example.musicplayer.Model.Music
import com.example.musicplayer.Model.MusicPlaylist
import com.example.musicplayer.Model.exitApp
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var filteredListMA: ArrayList<Music>
        var search: Boolean = false //biến check xem có search hay k

        var themeIndex: Int = 0
        val currentTheme = arrayOf(R.style.coolPink, R.style.coolBlue, R.style.coolPurple,
            R.style.coolGreen, R.style.coolBlack)
        val currentThemeNav = arrayOf(R.style.coolPinkNav, R.style.coolBlueNav, R.style.coolPurpleNav,
            R.style.coolGreenNav, R.style.coolBlackNav)

        var sortOrder: Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC"
            ,MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.SIZE + " DESC")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        setTheme(currentThemeNav[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)//cho phép toggle lắng nghe sự kiện mở và đóng ngăn kéo.
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (requestRuntimePermission()) {
            initializeLayout()
            getDataListFav()
        }
        suKienClick()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        search = false // khởi tạo ban đầu thì đặt false chưa thực hiện search
        //nhận trạng thái sắp xếp gửi từ settingActivity
        // Nếu không có giá trị nào được lưu trữ, mặc định là 0
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)

        MusicListMA = getAllAudio()
        binding.rvSong.setHasFixedSize(true)
        binding.rvSong.setItemViewCacheSize(13)
        binding.rvSong.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.rvSong.adapter = musicAdapter
        binding.tvTotalSong.text = "Total Songs: ${musicAdapter.itemCount}"
    }

    private fun suKienClick(){
        binding.btnShuffle.setOnClickListener {
            val i = Intent(this@MainActivity, PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("index", 0)
            bundle.putString("class", "MainActivity")
            i.putExtras(bundle)
            startActivity(i)
        }
        binding.btnFav.setOnClickListener {
            val i = Intent(this@MainActivity, FavouriteActivity::class.java)
            startActivity(i)
        }
        binding.btnPlaylist.setOnClickListener {
            val i = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(i)
        }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navFeed -> startActivity(Intent(this@MainActivity, FeedbackActivity::class.java))
                R.id.navSettings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.navAbout -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.navExit -> {
                    val dialog = AlertDialog.Builder(this)
                    dialog.apply {
                        setTitle("Exit")
                        setMessage("Do you want to close this app?")
                        setNegativeButton("No"){ dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                            exitApp()
                        }
                        setCancelable(true)
                    }
                    val customDialog = dialog.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }
            }
            true
        }
    }

    //kiểm tra và yêu cầu quyền truy cập trong thời gian chạy trên Android.
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) { //kiểm tra phiên bản SDK của thiết bị.
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
                return false
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Nếu phiên bản SDK lớn hơn hoặc bằng Build.VERSION_CODES.R (tương ứng với Android 12 trở lên)
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 13)
                return false
            }
        }
        return true
    }

    //được gọi khi người dùng đáp ứng yêu cầu cấp quyền
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 13){//Nếu requestCode là 13 (giống với mã yêu cầu trong requestRuntimePermission())
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){//Nếu quyền đã được cấp
                //phương thức kiểm tra xem quyền đã được cấp hay chưa thông qua grantResults.
                Toast.makeText(this, "Permission Granted",Toast.LENGTH_SHORT).show()
                initializeLayout()
            }
            else //Nếu quyền không được cấp, ActivityCompat.requestPermissions() được gọi lại để yêu cầu quyền một lần nữa.
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    //fun lấy danh sách tất cả các file âm thanh trên thiết bị Android.
    @SuppressLint("Recycle", "Range", "SuspiciousIndentation")
    private fun getAllAudio(): ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"//chỉ lấy những file âm thanh (không phải là nhạc chuông, tiếng kêu, v.v.).
        //Mảng các cột thông tin cần lấy cho mỗi file âm thanh
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
        //truy vấn cơ sở dữ liệu MediaStore bằng cách sử dụng contentResolver.query()
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
            null, sortingList[sortOrder], null)
        if (cursor != null){
            if (cursor.moveToFirst())//di chuyển con trỏ tới hàng đầu tiên
                do { //sau đó sử dụng vòng lặp do-while để lặp qua tất cả các hàng.
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val albumIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(uri, albumIdC).toString()//nối album ID với đường dẫn uri
                    val music = Music(idC, titleC, albumC, artistC, durationC, pathC, artUri)
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                }while (cursor.moveToNext())
                cursor.close()//đóng con trỏ
        }
        return tempList
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        //tìm mục menu có ID là searchView và lấy view của mục menu đó, sau đó ép kiểu về
        //androidx.appcompat.widget.SearchView cho phép tương tác với thanh tìm kiếm trong mã.
        val searchView = menu?.findItem(R.id.searchView)?.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListMusic(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun filterListMusic(query: String?) {
        if (query != null){
            filteredListMA = ArrayList<Music>()
            for (i in MusicListMA){
                if (i.title.contains(query, ignoreCase = true))
                    filteredListMA.add(i)
            }
            search = true //sau khi search thì set thành true
            musicAdapter.setListMusic(filteredListMA)
            binding.rvSong.adapter = musicAdapter
        }
    }

    override fun onDestroy() {//dc gọi khi ứng dụng bị huỷ
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
            //Nếu bài hát đang không được phát và dịch vụ nhạc tồn tại
            exitApp()
        }
    }

    override fun onResume() {
        super.onResume()
        //for sorting
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        //value sort hiện tại khác với sort truỳen tới điều này ngụ ý rằng trạng thái sắp xếp đã thay đổi
        if (sortOrder != sortValue){
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.setListMusic(MusicListMA)
        }
    }

    private fun storageFavourite(){
        val editor = getSharedPreferences("Favourite", Context.MODE_PRIVATE).edit()
        val jsonFav = GsonBuilder().create().toJson(FavouriteActivity.MusicListFav)
        val jsonPl = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("musicListFav", jsonFav)
        editor.putString("musicPlaylist", jsonPl)
        editor.apply()
    }

    private fun getDataListFav(){
        FavouriteActivity.MusicListFav = ArrayList()
        val editor = getSharedPreferences("Favourite", Context.MODE_PRIVATE)
        val jsonFav = editor.getString("musicListFav", null)
        val typeFav = object : TypeToken<ArrayList<Music>>(){}.type
        if (jsonFav != null){
            val dataFav: ArrayList<Music> = GsonBuilder().create().fromJson(jsonFav, typeFav)
            FavouriteActivity.MusicListFav.addAll(dataFav)
        }

        PlaylistActivity.musicPlaylist = MusicPlaylist()
        val jsonPl = editor.getString("musicPlaylist", null)
        if (jsonPl != null){
            val dataPl: MusicPlaylist = GsonBuilder().create().fromJson(jsonPl, MusicPlaylist::class.java)
            PlaylistActivity.musicPlaylist = dataPl
        }
    }

    override fun onPause() {
        super.onPause()
        storageFavourite()
    }
}