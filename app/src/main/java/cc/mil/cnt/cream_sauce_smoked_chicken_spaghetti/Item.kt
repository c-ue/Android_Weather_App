package cc.mil.cnt.cream_sauce_smoked_chicken_spaghetti

class Item(
    var db_id: Int,
    var flag: Int,
    var location: String,
    var nick_name: String,
    var temp: Float,
    var humi: Float,
    var update_timestamp: Long,
    var selected: Boolean
)
