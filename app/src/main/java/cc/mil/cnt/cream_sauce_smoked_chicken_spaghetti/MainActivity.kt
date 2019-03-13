package cc.mil.cnt.cream_sauce_smoked_chicken_spaghetti

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView


class MainActivity : AppCompatActivity() {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

        val CurrentItemList: String = "CurrentItemList"
        val AddItem: String = "ADD_ITEM_ACTIVITY"
        val DelItem: String = "DEL_ITEM_ACTIVITY"
        val DATABASE_VERSION_TABLE: String = "ver"
        val DATABASE_VERSION_ID: Int = 0
        val DATABASE_VERSION_CODE: String = R.string.ver_code.toString()
        val DATABASE_VERSION_CREATE: String = "create table " + DATABASE_VERSION_TABLE + "(db_id, ver);"
        val DATABASE_WEATHER_TABLE: String = "weather"
        val DATABASE_WEATHER_CREATE: String = "create table " + DATABASE_WEATHER_TABLE + "(db_id, location, nick_name, temp, humi, update_timestamp, selected);"
    }

    var db: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemlist = ArrayList<Item>()

        val btn_add: Button = findViewById(R.id.btn_add)
        btn_add.setOnClickListener({ OnClick(itemlist, AddItem) })

        val btn_del: Button = findViewById(R.id.btn_del)
        btn_del.setOnClickListener({ OnClick(itemlist, DelItem) })

        // Example of a call to a native method
//        sample_text.text = stringFromJNI()

        val openhelper = DBOpenHelper(this)
        db = openhelper.writableDatabase

        itemlist.add(Item(0,R.drawable.red,"日本旅遊", "日本", 150f, 15f, 0,true))
        itemlist.add(Item(1,R.drawable.yellow,"小新生活照", "小新", 100f, 10f, 100000,true))
        itemlist.add(Item(5,R.drawable.blue,"旅遊", "旅遊", 50f, 5f, 10000000,true))

        val listView: ListView = findViewById(R.id.listview)

        listView.adapter = ItemAdapter(this, itemlist)

    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
    }

    private fun OnClick(itemlist: ArrayList<Item>, ACTIVITY: String){
        val intent = Intent()
        if(ACTIVITY.equals(AddItem)){
            intent.setClass(this@MainActivity, ADDActivity::class.java)
        }else if (ACTIVITY.equals(DelItem)){
            intent.setClass(this@MainActivity, DELActivity::class.java)
        }
        val itemlistID = ArrayList<Int>()
        for (i in 0 until itemlist.size) {
            itemlistID.add(itemlist.get(i).db_id)
        }
        intent.putExtra(CurrentItemList, itemlistID)
        startActivity(intent)
    }

//    private fun DBReadHelper(context: Context){
//        val openhelper = DBOpenHelper(context)
//        db = openhelper.writableDatabase
//
//        var cv = ContentValues()
//        cv.put("number", 1)
//        cv.put("title", "abc")
//        cv.put("body", "Hello abc!")
//        db?.insert(DATABASE_TABLE, null, cv)
//        db?.execSQL("insert into " + DATABASE_TABLE +
//                " values(2," + "'xyz'" + "," + "'Hello xyz!'" + ");")
//
//        var c = db?.rawQuery("select * from " +
//                DATABASE_TABLE, null)
//
//        for (i in 0..(c!!.columnCount-1)) {
//            Log.d("LINCYU", "ColumnNames (" +
//                    c.getColumnIndex(c.columnNames[i]) +
//                    "): " + c.columnNames[i])
//        }
//
//        c.moveToFirst()
//        for (i in 0..(c.count-1)) {
//            Log.d("LINCYU", "Title" + i + ": " +
//                    c.getString(c.getColumnIndex(c.columnNames[1])))
//            c.moveToNext()
//        }
//
//        cv = ContentValues()
//        cv.put("title", "def")
//        db?.update(DATABASE_TABLE, cv,
//            "title=" + "'xyz'" ,
//            null)
//
//        c = db?.rawQuery("select * from " + DATABASE_TABLE,
//            null)
//        c!!.moveToFirst()
//        for (i in 0..(c.count-1)) {
//            Log.d("LINCYU", "Title(Update1)" + i + ": " +
//                    c.getString(c.getColumnIndex(c.columnNames[1])))
//            c.moveToNext()
//        }
//
//        db?.execSQL("update " + DATABASE_TABLE +
//                " set body=" + "'Hello def!'" +
//                " where title=" + "'def'" + ";")
//        c = db?.rawQuery("select * from " + DATABASE_TABLE,
//            null)
//        c!!.moveToFirst()
//        for (i in 0..(c.count-1)) {
//            Log.d("LINCYU", "Body(Update2)" + i + ": " +
//                    c.getString(c.getColumnIndex(c.columnNames[2])))
//            c.moveToNext()
//        }
//        c.close()
//
//        db?.delete(DATABASE_TABLE, "title=" + "'abc'",
//            null)
//    }


    inner class DBOpenHelper(context: Context): SQLiteOpenHelper(
        context, "weather.mdb",null, 1) {

        override fun onCreate(db: SQLiteDatabase?) {
            val cursor = db?.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DATABASE_VERSION_TABLE + "'", null)
            if(cursor!!.count.equals(0)){
                buildVerDB(db)
                buildWeatherDB(db)
            }else if(cursor!!.count.equals(1)){
                var cursor_ver_code = db?.rawQuery("select ver from " + DATABASE_VERSION_TABLE + " where db_id = '" + DATABASE_VERSION_ID + "'", null)
                var ver_code = cursor_ver_code.getString(cursor_ver_code.getColumnIndex("ver"))
                cursor_ver_code.close()
                if(ver_code != DATABASE_VERSION_CODE){
                    buildVerDB(db)
                    buildWeatherDB(db)
                }
            }
            cursor!!.close()
        }

        override fun onUpgrade(db: SQLiteDatabase?, oV: Int, nV: Int) {
        }

        private fun buildVerDB(db: SQLiteDatabase){
            db?.execSQL("drop table if exists " + DATABASE_VERSION_TABLE )
            db?.execSQL(DATABASE_VERSION_CREATE)
            val cv = ContentValues()
            cv.put("db_id", DATABASE_VERSION_ID)
            cv.put("ver", DATABASE_VERSION_CODE)
            db?.insert(DATABASE_VERSION_TABLE, null, cv)
        }

        private fun buildWeatherDB(db: SQLiteDatabase){
//            create table script which gen by python3
//            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values({:d}, '{:s}', '{:s}', 0, 0, 0, 0);")
            db?.execSQL("drop table if exists " + DATABASE_WEATHER_TABLE )
            db?.execSQL(DATABASE_WEATHER_CREATE)
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(1, '台北-中正區', '台北-中正區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(2, '台北-大同區', '台北-大同區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(3, '台北-中山區', '台北-中山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(4, '台北-松山區', '台北-松山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(5, '台北-大安區', '台北-大安區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(6, '台北-萬華區', '台北-萬華區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(7, '台北-信義區', '台北-信義區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(8, '台北-士林區', '台北-士林區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(9, '台北-北投區', '台北-北投區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(10, '台北-內湖區', '台北-內湖區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(11, '台北-南港區', '台北-南港區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(12, '台北-文山區', '台北-文山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(13, '基隆-仁愛區', '基隆-仁愛區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(14, '基隆-信義區', '基隆-信義區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(15, '基隆-中正區', '基隆-中正區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(16, '基隆-中山區', '基隆-中山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(17, '基隆-安樂區', '基隆-安樂區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(18, '基隆-暖暖區', '基隆-暖暖區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(19, '基隆-七堵區', '基隆-七堵區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(20, '新北-萬里區', '新北-萬里區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(21, '新北-金山區', '新北-金山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(26, '新北-板橋區', '新北-板橋區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(27, '新北-汐止區', '新北-汐止區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(28, '新北-深坑區', '新北-深坑區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(29, '新北-石碇區', '新北-石碇區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(30, '新北-瑞芳區', '新北-瑞芳區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(31, '新北-平溪區', '新北-平溪區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(32, '新北-雙溪區', '新北-雙溪區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(33, '新北-貢寮區', '新北-貢寮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(34, '新北-新店區', '新北-新店區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(35, '新北-坪林區', '新北-坪林區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(36, '新北-烏來區', '新北-烏來區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(37, '新北-永和區', '新北-永和區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(38, '新北-中和區', '新北-中和區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(39, '新北-土城區', '新北-土城區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(40, '新北-三峽區', '新北-三峽區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(41, '新北-樹林區', '新北-樹林區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(42, '新北-鶯歌區', '新北-鶯歌區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(43, '新北-三重區', '新北-三重區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(44, '新北-新莊區', '新北-新莊區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(45, '新北-泰山區', '新北-泰山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(46, '新北-林口區', '新北-林口區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(47, '新北-蘆洲區', '新北-蘆洲區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(48, '新北-五股區', '新北-五股區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(49, '新北-八里區', '新北-八里區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(50, '新北-淡水區', '新北-淡水區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(51, '新北-三芝區', '新北-三芝區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(52, '新北-石門區', '新北-石門區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(22, '連江-南竿鄉', '連江-南竿鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(23, '連江-北竿鄉', '連江-北竿鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(24, '連江-莒光鄉', '連江-莒光鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(25, '連江-東引鄉', '連江-東引鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(53, '宜蘭-宜蘭市', '宜蘭-宜蘭市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(54, '宜蘭-頭城鎮', '宜蘭-頭城鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(55, '宜蘭-礁溪鄉', '宜蘭-礁溪鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(56, '宜蘭-壯圍鄉', '宜蘭-壯圍鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(57, '宜蘭-員山鄉', '宜蘭-員山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(58, '宜蘭-羅東鎮', '宜蘭-羅東鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(59, '宜蘭-三星鄉', '宜蘭-三星鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(60, '宜蘭-大同鄉', '宜蘭-大同鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(61, '宜蘭-五結鄉', '宜蘭-五結鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(62, '宜蘭-冬山鄉', '宜蘭-冬山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(63, '宜蘭-蘇澳鎮', '宜蘭-蘇澳鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(64, '宜蘭-南澳鄉', '宜蘭-南澳鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(65, '新竹-東區', '新竹-東區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(66, '新竹-香山區', '新竹-香山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(67, '新竹-北區', '新竹-北區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(68, '新竹-竹北市', '新竹-竹北市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(69, '新竹-湖口鄉', '新竹-湖口鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(70, '新竹-新豐鄉', '新竹-新豐鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(71, '新竹-新埔鎮', '新竹-新埔鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(72, '新竹-關西鎮', '新竹-關西鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(73, '新竹-芎林鄉', '新竹-芎林鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(74, '新竹-寶山鄉', '新竹-寶山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(75, '新竹-竹東鎮', '新竹-竹東鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(76, '新竹-五峰鄉', '新竹-五峰鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(77, '新竹-橫山鄉', '新竹-橫山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(78, '新竹-尖石鄉', '新竹-尖石鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(79, '新竹-北埔鄉', '新竹-北埔鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(80, '新竹-峨眉鄉', '新竹-峨眉鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(81, '桃園-中壢區', '桃園-中壢區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(82, '桃園-平鎮區', '桃園-平鎮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(83, '桃園-龍潭區', '桃園-龍潭區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(84, '桃園-楊梅區', '桃園-楊梅區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(85, '桃園-新屋區', '桃園-新屋區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(86, '桃園-觀音區', '桃園-觀音區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(87, '桃園-桃園區', '桃園-桃園區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(88, '桃園-龜山區', '桃園-龜山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(89, '桃園-八德區', '桃園-八德區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(90, '桃園-大溪區', '桃園-大溪區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(91, '桃園-復興區', '桃園-復興區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(92, '桃園-大園區', '桃園-大園區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(93, '桃園-蘆竹區', '桃園-蘆竹區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(94, '苗栗-竹南鎮', '苗栗-竹南鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(95, '苗栗-頭份鎮', '苗栗-頭份鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(96, '苗栗-三灣鄉', '苗栗-三灣鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(97, '苗栗-南庄鄉', '苗栗-南庄鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(98, '苗栗-獅潭鄉', '苗栗-獅潭鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(99, '苗栗-後龍鎮', '苗栗-後龍鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(100, '苗栗-通霄鎮', '苗栗-通霄鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(101, '苗栗-苑裡鎮', '苗栗-苑裡鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(102, '苗栗-苗栗市', '苗栗-苗栗市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(103, '苗栗-造橋鄉', '苗栗-造橋鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(104, '苗栗-頭屋鄉', '苗栗-頭屋鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(105, '苗栗-公館鄉', '苗栗-公館鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(106, '苗栗-大湖鄉', '苗栗-大湖鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(107, '苗栗-泰安鄉', '苗栗-泰安鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(108, '苗栗-銅鑼鄉', '苗栗-銅鑼鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(109, '苗栗-三義鄉', '苗栗-三義鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(110, '苗栗-西湖鄉', '苗栗-西湖鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(111, '苗栗-卓蘭鎮', '苗栗-卓蘭鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(112, '台中-中區', '台中-中區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(113, '台中-東區', '台中-東區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(114, '台中-南區', '台中-南區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(115, '台中-西區', '台中-西區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(116, '台中-北區', '台中-北區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(117, '台中-北屯區', '台中-北屯區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(118, '台中-西屯區', '台中-西屯區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(119, '台中-南屯區', '台中-南屯區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(120, '台中-太平區', '台中-太平區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(121, '台中-大里區', '台中-大里區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(122, '台中-霧峰區', '台中-霧峰區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(123, '台中-烏日區', '台中-烏日區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(124, '台中-豐原區', '台中-豐原區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(125, '台中-后里區', '台中-后里區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(126, '台中-石岡區', '台中-石岡區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(127, '台中-東勢區', '台中-東勢區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(128, '台中-和平區', '台中-和平區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(129, '台中-新社區', '台中-新社區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(130, '台中-潭子區', '台中-潭子區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(131, '台中-大雅區', '台中-大雅區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(132, '台中-神岡區', '台中-神岡區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(133, '台中-大肚區', '台中-大肚區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(134, '台中-沙鹿區', '台中-沙鹿區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(135, '台中-龍井區', '台中-龍井區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(136, '台中-梧棲區', '台中-梧棲區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(137, '台中-清水區', '台中-清水區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(138, '台中-大甲區', '台中-大甲區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(139, '台中-外埔區', '台中-外埔區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(140, '台中-大安區', '台中-大安區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(141, '彰化-彰化市', '彰化-彰化市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(142, '彰化-芬園鄉', '彰化-芬園鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(143, '彰化-花壇鄉', '彰化-花壇鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(144, '彰化-秀水鄉', '彰化-秀水鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(145, '彰化-鹿港鎮', '彰化-鹿港鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(146, '彰化-福興鄉', '彰化-福興鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(147, '彰化-線西鄉', '彰化-線西鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(148, '彰化-和美鎮', '彰化-和美鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(149, '彰化-伸港鄉', '彰化-伸港鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(150, '彰化-員林鎮', '彰化-員林鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(151, '彰化-社頭鄉', '彰化-社頭鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(152, '彰化-永靖鄉', '彰化-永靖鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(153, '彰化-埔心鄉', '彰化-埔心鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(154, '彰化-溪湖鎮', '彰化-溪湖鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(155, '彰化-大村鄉', '彰化-大村鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(156, '彰化-埔鹽鄉', '彰化-埔鹽鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(157, '彰化-田中鎮', '彰化-田中鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(158, '彰化-北斗鎮', '彰化-北斗鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(159, '彰化-田尾鄉', '彰化-田尾鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(160, '彰化-埤頭鄉', '彰化-埤頭鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(161, '彰化-溪州鄉', '彰化-溪州鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(162, '彰化-竹塘鄉', '彰化-竹塘鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(163, '彰化-二林鎮', '彰化-二林鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(164, '彰化-大城鄉', '彰化-大城鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(165, '彰化-芳苑鄉', '彰化-芳苑鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(166, '彰化-二水鄉', '彰化-二水鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(167, '南投-南投市', '南投-南投市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(168, '南投-中寮鄉', '南投-中寮鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(169, '南投-草屯鎮', '南投-草屯鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(170, '南投-國姓鄉', '南投-國姓鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(171, '南投-埔里鎮', '南投-埔里鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(172, '南投-仁愛鄉', '南投-仁愛鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(173, '南投-名間鄉', '南投-名間鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(174, '南投-集集鎮', '南投-集集鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(175, '南投-水里鄉', '南投-水里鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(176, '南投-魚池鄉', '南投-魚池鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(177, '南投-信義鄉', '南投-信義鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(178, '南投-竹山鎮', '南投-竹山鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(179, '南投-鹿谷鄉', '南投-鹿谷鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(180, '嘉義-西區', '嘉義-西區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(181, '嘉義-東區', '嘉義-東區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(182, '嘉義-番路鄉', '嘉義-番路鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(183, '嘉義-梅山鄉', '嘉義-梅山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(184, '嘉義-竹崎鄉', '嘉義-竹崎鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(185, '嘉義-阿里山鄉', '嘉義-阿里山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(186, '嘉義-中埔鄉', '嘉義-中埔鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(187, '嘉義-大埔鄉', '嘉義-大埔鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(188, '嘉義-水上鄉', '嘉義-水上鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(189, '嘉義-鹿草鄉', '嘉義-鹿草鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(190, '嘉義-太保市', '嘉義-太保市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(191, '嘉義-朴子市', '嘉義-朴子市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(192, '嘉義-東石鄉', '嘉義-東石鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(193, '嘉義-六腳鄉', '嘉義-六腳鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(194, '嘉義-新港鄉', '嘉義-新港鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(195, '嘉義-民雄鄉', '嘉義-民雄鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(196, '嘉義-大林鎮', '嘉義-大林鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(197, '嘉義-溪口鄉', '嘉義-溪口鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(198, '嘉義-義竹鄉', '嘉義-義竹鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(199, '嘉義-布袋鎮', '嘉義-布袋鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(200, '雲林-斗南鎮', '雲林-斗南鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(201, '雲林-大埤鄉', '雲林-大埤鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(202, '雲林-虎尾鎮', '雲林-虎尾鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(203, '雲林-土庫鎮', '雲林-土庫鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(204, '雲林-褒忠鄉', '雲林-褒忠鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(205, '雲林-東勢鄉', '雲林-東勢鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(206, '雲林-台西鄉', '雲林-台西鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(207, '雲林-崙背鄉', '雲林-崙背鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(208, '雲林-麥寮鄉', '雲林-麥寮鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(209, '雲林-斗六市', '雲林-斗六市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(210, '雲林-林內鄉', '雲林-林內鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(211, '雲林-古坑鄉', '雲林-古坑鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(212, '雲林-莿桐鄉', '雲林-莿桐鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(213, '雲林-西螺鎮', '雲林-西螺鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(214, '雲林-二崙鄉', '雲林-二崙鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(215, '雲林-北港鎮', '雲林-北港鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(216, '雲林-水林鄉', '雲林-水林鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(217, '雲林-口湖鄉', '雲林-口湖鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(218, '雲林-四湖鄉', '雲林-四湖鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(219, '雲林-元長鄉', '雲林-元長鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(220, '台南-中西區', '台南-中西區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(221, '台南-東區', '台南-東區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(222, '台南-南區', '台南-南區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(223, '台南-北區', '台南-北區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(224, '台南-安平區', '台南-安平區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(225, '台南-安南區', '台南-安南區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(226, '台南-永康區', '台南-永康區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(227, '台南-歸仁區', '台南-歸仁區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(228, '台南-新化區', '台南-新化區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(229, '台南-左鎮區', '台南-左鎮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(230, '台南-玉井區', '台南-玉井區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(231, '台南-楠西區', '台南-楠西區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(232, '台南-南化區', '台南-南化區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(233, '台南-仁德區', '台南-仁德區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(234, '台南-關廟區', '台南-關廟區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(235, '台南-龍崎區', '台南-龍崎區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(236, '台南-官田區', '台南-官田區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(237, '台南-麻豆區', '台南-麻豆區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(238, '台南-佳里區', '台南-佳里區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(239, '台南-西港區', '台南-西港區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(240, '台南-七股區', '台南-七股區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(241, '台南-將軍區', '台南-將軍區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(242, '台南-學甲區', '台南-學甲區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(243, '台南-北門區', '台南-北門區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(244, '台南-新營區', '台南-新營區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(245, '台南-後壁區', '台南-後壁區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(246, '台南-白河區', '台南-白河區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(247, '台南-東山區', '台南-東山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(248, '台南-六甲區', '台南-六甲區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(249, '台南-下營區', '台南-下營區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(250, '台南-柳營區', '台南-柳營區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(251, '台南-鹽水區', '台南-鹽水區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(252, '台南-善化區', '台南-善化區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(253, '台南-大內區', '台南-大內區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(254, '台南-山上區', '台南-山上區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(255, '台南-新市區', '台南-新市區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(256, '台南-安定區', '台南-安定區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(257, '高雄-新興區', '高雄-新興區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(258, '高雄-前金區', '高雄-前金區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(259, '高雄-苓雅區', '高雄-苓雅區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(260, '高雄-鹽埕區', '高雄-鹽埕區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(261, '高雄-鼓山區', '高雄-鼓山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(262, '高雄-旗津區', '高雄-旗津區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(263, '高雄-前鎮區', '高雄-前鎮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(264, '高雄-三民區', '高雄-三民區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(265, '高雄-楠梓區', '高雄-楠梓區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(266, '高雄-小港區', '高雄-小港區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(267, '高雄-左營區', '高雄-左營區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(268, '高雄-仁武區', '高雄-仁武區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(269, '高雄-大社區', '高雄-大社區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(270, '高雄-岡山區', '高雄-岡山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(271, '高雄-路竹區', '高雄-路竹區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(272, '高雄-阿蓮區', '高雄-阿蓮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(273, '高雄-田寮區', '高雄-田寮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(274, '高雄-燕巢區', '高雄-燕巢區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(275, '高雄-橋頭區', '高雄-橋頭區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(276, '高雄-梓官區', '高雄-梓官區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(277, '高雄-彌陀區', '高雄-彌陀區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(278, '高雄-永安區', '高雄-永安區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(279, '高雄-湖內區', '高雄-湖內區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(280, '高雄-鳳山區', '高雄-鳳山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(281, '高雄-大寮區', '高雄-大寮區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(282, '高雄-林園區', '高雄-林園區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(283, '高雄-鳥松區', '高雄-鳥松區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(284, '高雄-大樹區', '高雄-大樹區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(285, '高雄-旗山區', '高雄-旗山區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(286, '高雄-美濃區', '高雄-美濃區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(287, '高雄-六龜區', '高雄-六龜區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(288, '高雄-內門區', '高雄-內門區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(289, '高雄-杉林區', '高雄-杉林區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(290, '高雄-甲仙區', '高雄-甲仙區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(291, '高雄-桃源區', '高雄-桃源區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(292, '高雄-那瑪夏區', '高雄-那瑪夏區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(293, '高雄-茂林區', '高雄-茂林區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(294, '高雄-茄萣區', '高雄-茄萣區', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(295, '澎湖-馬公市', '澎湖-馬公市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(296, '澎湖-西嶼鄉', '澎湖-西嶼鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(297, '澎湖-望安鄉', '澎湖-望安鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(298, '澎湖-七美鄉', '澎湖-七美鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(299, '澎湖-白沙鄉', '澎湖-白沙鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(300, '澎湖-湖西鄉', '澎湖-湖西鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(301, '金門-金沙鎮', '金門-金沙鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(302, '金門-金湖鎮', '金門-金湖鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(303, '金門-金寧鄉', '金門-金寧鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(304, '金門-金城鎮', '金門-金城鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(305, '金門-烈嶼鄉', '金門-烈嶼鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(306, '金門-烏坵鄉', '金門-烏坵鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(307, '屏東-屏東市', '屏東-屏東市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(308, '屏東-三地門鄉', '屏東-三地門鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(309, '屏東-霧台鄉', '屏東-霧台鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(310, '屏東-瑪家鄉', '屏東-瑪家鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(311, '屏東-九如鄉', '屏東-九如鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(312, '屏東-里港鄉', '屏東-里港鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(313, '屏東-高樹鄉', '屏東-高樹鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(314, '屏東-鹽埔鄉', '屏東-鹽埔鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(315, '屏東-長治鄉', '屏東-長治鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(316, '屏東-麟洛鄉', '屏東-麟洛鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(317, '屏東-竹田鄉', '屏東-竹田鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(318, '屏東-內埔鄉', '屏東-內埔鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(319, '屏東-萬丹鄉', '屏東-萬丹鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(320, '屏東-潮州鎮', '屏東-潮州鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(321, '屏東-泰武鄉', '屏東-泰武鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(322, '屏東-來義鄉', '屏東-來義鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(323, '屏東-萬巒鄉', '屏東-萬巒鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(324, '屏東-崁頂鄉', '屏東-崁頂鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(325, '屏東-新埤鄉', '屏東-新埤鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(326, '屏東-南州鄉', '屏東-南州鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(327, '屏東-林邊鄉', '屏東-林邊鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(328, '屏東-東港鎮', '屏東-東港鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(329, '屏東-琉球鄉', '屏東-琉球鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(330, '屏東-佳冬鄉', '屏東-佳冬鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(331, '屏東-新園鄉', '屏東-新園鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(332, '屏東-枋寮鄉', '屏東-枋寮鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(333, '屏東-枋山鄉', '屏東-枋山鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(334, '屏東-春日鄉', '屏東-春日鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(335, '屏東-獅子鄉', '屏東-獅子鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(336, '屏東-車城鄉', '屏東-車城鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(337, '屏東-牡丹鄉', '屏東-牡丹鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(338, '屏東-恆春鎮', '屏東-恆春鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(339, '屏東-滿州鄉', '屏東-滿州鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(340, '台東-台東市', '台東-台東市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(341, '台東-綠島鄉', '台東-綠島鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(342, '台東-蘭嶼鄉', '台東-蘭嶼鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(343, '台東-延平鄉', '台東-延平鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(344, '台東-卑南鄉', '台東-卑南鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(345, '台東-鹿野鄉', '台東-鹿野鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(346, '台東-關山鎮', '台東-關山鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(347, '台東-海端鄉', '台東-海端鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(348, '台東-池上鄉', '台東-池上鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(349, '台東-東河鄉', '台東-東河鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(350, '台東-成功鎮', '台東-成功鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(351, '台東-長濱鄉', '台東-長濱鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(352, '台東-太麻里鄉', '台東-太麻里鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(353, '台東-金峰鄉', '台東-金峰鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(354, '台東-大武鄉', '台東-大武鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(355, '台東-達仁鄉', '台東-達仁鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(356, '花蓮-花蓮市', '花蓮-花蓮市', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(357, '花蓮-新城鄉', '花蓮-新城鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(358, '花蓮-秀林鄉', '花蓮-秀林鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(359, '花蓮-吉安鄉', '花蓮-吉安鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(360, '花蓮-壽豐鄉', '花蓮-壽豐鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(361, '花蓮-鳳林鎮', '花蓮-鳳林鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(362, '花蓮-光復鄉', '花蓮-光復鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(363, '花蓮-豐濱鄉', '花蓮-豐濱鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(364, '花蓮-瑞穗鄉', '花蓮-瑞穗鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(365, '花蓮-萬榮鄉', '花蓮-萬榮鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(366, '花蓮-玉里鎮', '花蓮-玉里鎮', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(367, '花蓮-卓溪鄉', '花蓮-卓溪鄉', 0, 0, 0, 0);")
            db?.execSQL("insert into " + DATABASE_WEATHER_TABLE + " values(368, '花蓮-富里鄉', '花蓮-富里鄉', 0, 0, 0, 0);")
        }

    }
}