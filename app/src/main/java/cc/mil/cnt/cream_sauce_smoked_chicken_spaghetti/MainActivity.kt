package cc.mil.cnt.cream_sauce_smoked_chicken_spaghetti

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
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
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_add: Button = findViewById(R.id.btn_add)
        btn_add.setOnClickListener({ OnClick(AddItem) })
//        btn_add.setOnClickListener({ OnClick(itemlist, AddItem) })

        val btn_del: Button = findViewById(R.id.btn_del)
        btn_del.setOnClickListener({ OnClick(DelItem) })
//        btn_del.setOnClickListener({ OnClick(itemlist, DelItem) })

        val ListRefresh: SwipeRefreshLayout = findViewById(R.id.ListRefresh);
        ListRefresh.setOnRefreshListener({ OnSwipe() })

//         Example of a call to a native method
//        sample_text.text = stringFromJNI()
//        itemlist.add(Item(0,R.drawable.red,"日本旅遊", "日本", 150f, 15f, 0,true))
//        itemlist.add(Item(1,R.drawable.yellow,"小新生活照", "小新", 100f, 10f, 100000,true))
//        itemlist.add(Item(5,R.drawable.blue,"旅遊", "旅遊", 50f, 5f, 10000000,true))

    }

//    TODO
    override fun onResume() {
        super.onResume()
        CheckUpdateTime()
        UpdateSelectedWeather()
        UpdateFlag()
        ShowListView()
    }

    private fun OnClick(ACTIVITY: String) {
//        private fun OnClick(itemlist: ArrayList<Item>, ACTIVITY: String){
        val intent = Intent()
        if(ACTIVITY.equals(AddItem)){
            intent.setClass(this@MainActivity, ADDActivity::class.java)
        }else if (ACTIVITY.equals(DelItem)){
            intent.setClass(this@MainActivity, DELActivity::class.java)
        }
//        val itemlistID = ArrayList<Int>()
//        for (i in 0 until itemlist.size) {
//            itemlistID.add(itemlist.get(i).db_id)
//        }
//        intent.putExtra(CurrentItemList, itemlistID)
        startActivity(intent)
    }

    //    TODO
    private fun OnSwipe(itemlist: ArrayList<Item>){
        CheckUpdateTime()
        UpdateSelectedWeather()
        UpdateFlag()
        ShowListView()
    }

    //    TODO
    private fun CheckUpdateTime(){

    }

    //    TODO
    private fun UpdateSelectedWeather(db: SQLiteDatabase) {

    }

    //    TODO
    private fun ShowListView(){
        var itemlist: ArrayList<Item>? = null
        itemlist = DBHelper(resources.getString(R.string.ver_code)).DBReadItems(this, true)
        UpdateFlag(itemlist)

        val listView: ListView = findViewById(R.id.listview)

        listView.adapter = ItemAdapter(this, itemlist)
    }

    private fun UpdateFlag(itemlist: ArrayList<Item>) {
        for (i in 0..(itemlist.count() - 1)) {
            if (itemlist.get(i).temp + (0.1 * itemlist.get(i).humi) < 35) {
                itemlist.set(
                    i,
                    Item(
                        itemlist.get(i).db_id,
                        R.drawable.blue,
                        itemlist.get(i).location,
                        itemlist.get(i).nick_name,
                        itemlist.get(i).temp,
                        itemlist.get(i).humi,
                        itemlist.get(i).update_timestamp,
                        itemlist.get(i).selected
                    )
                )
            } else if (itemlist.get(i).temp + (0.1 * itemlist.get(i).humi) >= 35 && itemlist.get(i).temp + (0.1 * itemlist.get(
                    i
                ).humi) < 40
            ) {
                itemlist.set(
                    i,
                    Item(
                        itemlist.get(i).db_id,
                        R.drawable.yellow,
                        itemlist.get(i).location,
                        itemlist.get(i).nick_name,
                        itemlist.get(i).temp,
                        itemlist.get(i).humi,
                        itemlist.get(i).update_timestamp,
                        itemlist.get(i).selected
                    )
                )
            } else {
                itemlist.set(
                    i,
                    Item(
                        itemlist.get(i).db_id,
                        R.drawable.red,
                        itemlist.get(i).location,
                        itemlist.get(i).nick_name,
                        itemlist.get(i).temp,
                        itemlist.get(i).humi,
                        itemlist.get(i).update_timestamp,
                        itemlist.get(i).selected
                    )
                )
            }
        }
    }
}