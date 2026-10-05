package com.bigegg.tvrepairledger.options

import org.junit.Assert.assertEquals
import org.junit.Test

class CommonOptionStoreTest {
    @Test
    fun defaultRepairDeviceOptions_containsCommonDevices() {
        assertEquals(
            listOf("OLED电视", "投影仪", "显示器", "机顶盒", "液晶电视", "电视机"),
            CommonOptionStore.defaultRepairDeviceOptions
        )
    }

    @Test
    fun defaultAddressOptions_containsCommunitiesFromReport() {
        assertEquals(100, CommonOptionStore.defaultAddressOptions.size)
        assertEquals(true, CommonOptionStore.defaultAddressOptions.contains("明发锦绣银山"))
        assertEquals(true, CommonOptionStore.defaultAddressOptions.contains("昌建融创·观澜公馆"))
        assertEquals(true, CommonOptionStore.defaultAddressOptions.contains("贵地·九悦澜庭"))
    }

    @Test
    fun defaultBrandOptions_containsCommonTvBrands() {
        val brands = CommonOptionStore.defaultBrandOptions

        assertEquals(20, brands.size)
        assertEquals(true, brands.contains("小米"))
        assertEquals(true, brands.contains("海信"))
        assertEquals(true, brands.contains("TCL"))
        assertEquals(true, brands.contains("索尼"))
    }

    @Test
    fun normalizeOptions_trimsDropsBlanksAndDeduplicates() {
        val result = CommonOptionStore.normalizeOptions(
            listOf(" 黑屏 ", "", "灰屏", "黑屏", "  ")
        )

        assertEquals(listOf("灰屏", "黑屏"), result)
    }

    @Test
    fun rankAddressOptions_putsRecentMatchesFirst() {
        val result = CommonOptionStore.rankAddressOptions(
            options = listOf("阳光花园", "幸福小区", "万达公寓", "建设路88号"),
            recentOptions = listOf("万达公寓", "幸福小区")
        )

        assertEquals(listOf("万达公寓", "幸福小区", "建设路88号", "阳光花园"), result)
    }

    @Test
    fun mergeAddressOptions_addsDefaultsToExistingSavedAddressesAndHonorsDeletedDefaults() {
        val result = CommonOptionStore.mergeAddressOptions(
            savedOptions = listOf("自定义小区"),
            deletedDefaultOptions = listOf("明发锦绣银山")
        )

        assertEquals(true, result.contains("自定义小区"))
        assertEquals(true, result.contains("昌建融创·观澜公馆"))
        assertEquals(false, result.contains("明发锦绣银山"))
    }

    @Test
    fun visibleAddressOptions_showsAllWhenBlankAndFiltersWhenSearching() {
        val options = (1..30).map { "幸福小区$it" }

        val blankResult = CommonOptionStore.visibleAddressOptions(options, "")
        val searchResult = CommonOptionStore.visibleAddressOptions(options, "幸福小区2")

        assertEquals(30, blankResult.size)
        assertEquals(listOf("幸福小区2", "幸福小区20", "幸福小区21", "幸福小区22", "幸福小区23", "幸福小区24", "幸福小区25", "幸福小区26", "幸福小区27", "幸福小区28", "幸福小区29"), searchResult)
    }
}
