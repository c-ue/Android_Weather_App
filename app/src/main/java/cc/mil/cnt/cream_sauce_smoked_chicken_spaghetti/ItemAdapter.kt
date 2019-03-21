package cc.mil.cnt.cream_sauce_smoked_chicken_spaghetti

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class ItemAdapter(val c: Context, val items: ArrayList<Item>) :
    ArrayAdapter<Item>(c, 0, items) {

    override fun getView(
        position: Int, convertView: View?,
        parent: ViewGroup?
    ): View {

        val inflater = LayoutInflater.from(c)
        var itemlayout: LinearLayout? = null

        if (convertView == null) {
            itemlayout = inflater.inflate(R.layout.listitem, null)
                    as? LinearLayout
        } else {
            itemlayout = convertView as? LinearLayout
        }
        val item: Item = getItem(position)

        val title_txt: TextView = itemlayout?.findViewById(R.id.title_text)!!
        title_txt.text = item.nick_name
        val detail_txt: TextView = itemlayout.findViewById(R.id.detail_text)!!
        detail_txt.text = "地點：" + item.location + ", 溫度：" + item.temp + ", 濕度：" + item.humi
        val iv: ImageView = itemlayout.findViewById(R.id.icon_item)!!
        iv.setImageResource(item.flag)

        return itemlayout
    }
}