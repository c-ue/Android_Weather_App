package cc.mil.cnt.cream_sauce_smoked_chicken_spaghetti

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ListView
import org.json.JSONObject
import java.net.URL


class MainActivity : AppCompatActivity() {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    companion object {

        val AddItem: String = "ADD_ITEM_ACTIVITY"
        val DelItem: String = "DEL_ITEM_ACTIVITY"
        val update_delay: Int = 10
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_add: Button = findViewById(R.id.btn_add)
        btn_add.setOnClickListener({ OnClick(AddItem) })

        val btn_del: Button = findViewById(R.id.btn_del)
        btn_del.setOnClickListener({ OnClick(DelItem) })

        val ListRefresh: SwipeRefreshLayout = findViewById(R.id.ListRefresh)
        ListRefresh.setOnRefreshListener({ OnSwipe(ListRefresh) })

    }

    override fun onResume() {
        super.onResume()
        var thread: Thread = Thread({
            if (isConnected()) {
                UpdateSelectedWeather()
            }
        })
        thread.start()
        thread.join()
        ShowListView()
    }

    private fun OnClick(ACTIVITY: String) {
        val intent = Intent()
        if(ACTIVITY.equals(AddItem)){
            intent.setClass(this@MainActivity, ADDActivity::class.java)
        }else if (ACTIVITY.equals(DelItem)){
            intent.setClass(this@MainActivity, DELActivity::class.java)
        }
        startActivity(intent)
    }

    private fun OnSwipe(ListRefresh: SwipeRefreshLayout) {
        var thread: Thread = Thread({
            if (isConnected()) {
                UpdateSelectedWeather()
            }
        })
        thread.start()
        thread.join()
        ShowListView()
        ListRefresh.isRefreshing = false
    }

    private fun UpdateWeather(db_id: Int): ArrayList<Float> {
        var json: String =
            URL("http://122.116.78.229/https2http.php?api=weathers/" + db_id.toString() + ".json").readText()
        var json_loader: JSONObject = JSONObject(json)
        var ret: ArrayList<Float> = ArrayList<Float>()
        ret.add(json_loader.getString("temperature").toFloat())
        ret.add(json_loader.getString("humidity").toFloat())
        return ret
    }

    private fun UpdateSelectedWeather() {
        var itemlist: ArrayList<Item>? = null
        itemlist = DBHelper().DBReadItems(this, true)
        for (i in 0..(itemlist.count() - 1)) {
            var now: Long = (System.currentTimeMillis() / 1000)
            if (now - itemlist.get(i).update_timestamp > update_delay) {
                var data: ArrayList<Float> = UpdateWeather(itemlist.get(i).db_id)
                var item: Item = Item(
                    itemlist.get(i).db_id,
                    R.drawable.blue,
                    itemlist.get(i).location,
                    itemlist.get(i).nick_name,
                    data.get(0),
                    data.get(1),
                    now,
                    itemlist.get(i).selected
                )
                DBHelper().DBUpdateItem(this, item)
            }
        }


    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun ShowListView(){
        var itemlist: ArrayList<Item>? = null
        itemlist = DBHelper().DBReadItems(this, true)
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