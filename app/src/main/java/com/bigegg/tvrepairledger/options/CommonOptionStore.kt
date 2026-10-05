package com.bigegg.tvrepairledger.options

import android.content.Context

class CommonOptionStore(context: Context) {
    private val preferences = context.getSharedPreferences("common-options", Context.MODE_PRIVATE)

    fun loadFaultOptions(): List<String> =
        preferences.getStringSet(KEY_FAULTS, defaultFaultOptions.toSet()).orEmpty().sorted()

    fun loadRepairItemOptions(): List<String> =
        preferences.getStringSet(KEY_REPAIR_ITEMS, defaultRepairItemOptions.toSet()).orEmpty().sorted()

    fun loadRepairDeviceOptions(): List<String> =
        preferences.getStringSet(KEY_REPAIR_DEVICES, defaultRepairDeviceOptions.toSet()).orEmpty().sorted()

    fun loadBrandOptions(): List<String> =
        preferences.getStringSet(KEY_BRANDS, defaultBrandOptions.toSet()).orEmpty().sorted()

    fun loadAddressOptions(): List<String> =
        mergeAddressOptions(
            savedOptions = preferences.getStringSet(KEY_ADDRESSES, emptySet()).orEmpty().toList(),
            deletedDefaultOptions = preferences.getStringSet(KEY_DELETED_ADDRESSES, emptySet()).orEmpty().toList()
        )

    fun loadRecentAddressOptions(): List<String> =
        preferences.getString(KEY_RECENT_ADDRESSES, "")
            .orEmpty()
            .split(RECENT_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun saveFaultOptions(options: List<String>) {
        preferences.edit().putStringSet(KEY_FAULTS, normalizeOptions(options).toSet()).apply()
    }

    fun saveRepairItemOptions(options: List<String>) {
        preferences.edit().putStringSet(KEY_REPAIR_ITEMS, normalizeOptions(options).toSet()).apply()
    }

    fun saveRepairDeviceOptions(options: List<String>) {
        preferences.edit().putStringSet(KEY_REPAIR_DEVICES, normalizeOptions(options).toSet()).apply()
    }

    fun saveBrandOptions(options: List<String>) {
        preferences.edit().putStringSet(KEY_BRANDS, normalizeOptions(options).toSet()).apply()
    }

    fun saveAddressOptions(options: List<String>) {
        val normalizedOptions = normalizeOptions(options)
        val defaultSet = defaultAddressOptions.toSet()
        val customOptions = normalizedOptions.filterNot { it in defaultSet }
        val deletedDefaultOptions = defaultAddressOptions.filterNot { it in normalizedOptions }
        preferences.edit()
            .putStringSet(KEY_ADDRESSES, customOptions.toSet())
            .putStringSet(KEY_DELETED_ADDRESSES, deletedDefaultOptions.toSet())
            .apply()
    }

    fun recordAddressUse(option: String) {
        val normalized = option.trim()
        if (normalized.isEmpty()) return
        val recent = listOf(normalized) + loadRecentAddressOptions().filterNot { it == normalized }
        preferences.edit()
            .putString(KEY_RECENT_ADDRESSES, recent.take(MAX_RECENT_ADDRESSES).joinToString(RECENT_SEPARATOR))
            .apply()
    }

    companion object {
        private const val KEY_FAULTS = "fault-options"
        private const val KEY_REPAIR_ITEMS = "repair-item-options"
        private const val KEY_REPAIR_DEVICES = "repair-device-options"
        private const val KEY_BRANDS = "brand-options"
        private const val KEY_ADDRESSES = "address-options"
        private const val KEY_DELETED_ADDRESSES = "deleted-address-options"
        private const val KEY_RECENT_ADDRESSES = "recent-address-options"
        private const val RECENT_SEPARATOR = "\u001F"
        private const val MAX_RECENT_ADDRESSES = 30

        val defaultFaultOptions = listOf("黑屏", "灰屏", "无声音", "开机卡 LOGO", "遥控不灵", "自动重启")
        val defaultRepairItemOptions = listOf("更换背光灯条", "主板维修", "电源板维修", "音频板维修", "EMMC 数据修复", "更换红外接收头")
        val defaultRepairDeviceOptions = listOf("OLED电视", "投影仪", "显示器", "机顶盒", "液晶电视", "电视机")
        val defaultBrandOptions = listOf(
            "Redmi",
            "TCL",
            "LG",
            "Vidda",
            "东芝",
            "乐视",
            "创维",
            "华为",
            "夏普",
            "小米",
            "康佳",
            "松下",
            "海尔",
            "海信",
            "索尼",
            "荣耀",
            "飞利浦",
            "长虹",
            "雷鸟",
            "三星"
        )
        val defaultAddressOptions = listOf(
            "昌建融创·观澜公馆",
            "御景湾",
            "旭辉朗香郡",
            "银山鑫城",
            "中南·名人府",
            "平昌新城",
            "湘华银杏家园",
            "丽景花苑",
            "金港生活广场",
            "大港金樱苑",
            "龙泉新苑",
            "紫竹苑",
            "锦绣圌山花苑",
            "蓝城·桂语江南",
            "荔湾城",
            "宜乐苑",
            "瑞鑫嘉园",
            "国信上城",
            "逸翠园",
            "新乐苑",
            "新茂苑",
            "万科金域蓝湾",
            "港城尚府",
            "通都雅寓",
            "宝地名邸",
            "御都花园",
            "吉祥苑",
            "明发锦绣银山",
            "港中新村",
            "葛村新苑",
            "港口路99号",
            "大港紫荆花园",
            "逸翠园翠竹苑",
            "凯悦山庄",
            "大港金水湾",
            "金港花园",
            "宜居苑",
            "新怡苑",
            "天星苑",
            "融恒·紫晶香郡",
            "皓月苑",
            "港南花苑吉祥苑",
            "金桂坊",
            "海德公园",
            "证大易墅",
            "宜安苑",
            "怡人家园",
            "万科金域江湾",
            "泰和铭庭",
            "瑞鑫佳苑",
            "翡翠绿洲",
            "2077·中央公园",
            "北极锦绣圌山",
            "格林铭郡",
            "银山鑫城玉兰苑",
            "蓝城·桂语兰庭",
            "凤栖佳苑",
            "海德公园枫丹园",
            "海德公园翰文园",
            "九悦澜庭",
            "贵地·九悦澜庭",
            "名人府",
            "名人府(别墅)",
            "恒大御府",
            "中南锦悦",
            "中南·锦悦",
            "银山鑫城牡丹苑",
            "银山鑫城紫荆苑",
            "银山鑫城金香苑",
            "银山鑫城米兰苑",
            "蝴蝶商业广场",
            "路劲港欣城",
            "御景湾(新房)",
            "南湖庄园别墅",
            "益华广场",
            "四海家园",
            "海德公园檀香园",
            "海德公园(别墅)",
            "平昌宜居苑",
            "万科金域铭居(南区)",
            "万科金域铭居(北区)",
            "海德公园沁园",
            "明发盛世家园·锦绣公馆",
            "宜业苑",
            "中央公园",
            "中央公园(别墅)",
            "铭庭苑",
            "荔湾城·润都府",
            "路劲城",
            "首创悦府",
            "金山水城",
            "东方新卡纳",
            "东城绿洲",
            "银山鑫城玫瑰苑",
            "银山鑫城鈅珑湖",
            "仕德伟沁园银郡",
            "翡翠湾",
            "星港花苑",
            "一峰公馆",
            "亿都尚品"
        )

        fun normalizeOptions(options: List<String>): List<String> {
            return options
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }

        fun rankAddressOptions(options: List<String>, recentOptions: List<String>): List<String> {
            val normalizedOptions = normalizeOptions(options)
            val optionSet = normalizedOptions.toSet()
            val recentMatches = recentOptions
                .map { it.trim() }
                .filter { it in optionSet }
                .distinct()
            return recentMatches + normalizedOptions.filterNot { it in recentMatches }
        }

        fun mergeAddressOptions(savedOptions: List<String>, deletedDefaultOptions: List<String>): List<String> {
            val deletedSet = deletedDefaultOptions.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            return normalizeOptions(defaultAddressOptions + savedOptions)
                .filterNot { it in deletedSet }
        }

        fun visibleAddressOptions(options: List<String>, query: String): List<String> {
            val trimmedQuery = query.trim()
            return if (trimmedQuery.isEmpty()) {
                options
            } else {
                options.filter { it.contains(trimmedQuery, ignoreCase = true) }
            }
        }
    }
}
