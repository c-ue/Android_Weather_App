package cc.mil.cnt.cream_sauce_smoked_chicken_spaghetti

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ListView

class ADDActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val btn_ret: Button = findViewById(R.id.btn_addactivity_return)
        btn_ret.setOnClickListener({ OnClick() })
    }

    override fun onResume() {
        super.onResume()

        var itemlist: ArrayList<Item>? = null
        itemlist = DBHelper(resources.getString(R.string.ver_code)).DBReadItems(this, false)

        val listView: ListView = findViewById(R.id.listview)

        listView.adapter = ItemAdapter(this, itemlist)

        listView.setOnItemClickListener({ parent, view, position, id ->
            itemClicked(position, itemlist)
        })
    }

    private fun itemClicked(position: Int, itemlist: ArrayList<Item>) {
        val item: Item = itemlist.get(position)
        item.selected = true
        DBHelper(resources.getString(R.string.ver_code)).DBUpdateItem(this, item)
        val intent = Intent()
        intent.setClass(this@ADDActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun OnClick() {
        val intent = Intent()
        intent.setClass(this@ADDActivity, MainActivity::class.java)
        startActivity(intent)
    }
}
