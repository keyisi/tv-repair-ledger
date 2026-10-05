package com.bigegg.tvrepairledger.backup

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.json.JSONArray
import org.json.JSONObject

fun encodeBackup(records: List<RepairRecord>): String {
    val root = JSONObject()
    root.put("version", 1)
    root.put(
        "records",
        JSONArray(
            records.map { record ->
                JSONObject()
                    .put("id", record.id)
                    .put("dateEpochDay", record.dateEpochDay)
                    .put("address", record.address)
                    .put("phone", record.phone)
                    .put("faultSymptom", record.faultSymptom)
                    .put("repairItem", record.repairItem)
                    .put("chargedAmountCents", record.chargedAmountCents)
                    .put("partsCostCents", record.partsCostCents)
                    .put("notes", record.notes)
                    .put("warrantyPeriod", record.warrantyPeriod)
                    .put("createdAtMillis", record.createdAtMillis)
                    .put("updatedAtMillis", record.updatedAtMillis)
                    .put("customerName", record.customerName)
                    .put("warrantyDays", record.warrantyDays)
                    .put("repairDevice", record.repairDevice)
                    .put("brand", record.brand)
            }
        )
    )
    return root.toString()
}

fun decodeBackup(json: String): List<RepairRecord> {
    val root = JSONObject(json)
    require(root.getInt("version") == 1) { "Unsupported backup version" }
    val records = root.getJSONArray("records")

    return (0 until records.length()).map { index ->
        val item = records.getJSONObject(index)
        RepairRecord(
            id = item.getLong("id"),
            dateEpochDay = item.getLong("dateEpochDay"),
            address = item.getString("address"),
            phone = item.getString("phone"),
            faultSymptom = item.getString("faultSymptom"),
            repairItem = item.getString("repairItem"),
            chargedAmountCents = item.getLong("chargedAmountCents"),
            partsCostCents = if (item.isNull("partsCostCents")) null else item.getLong("partsCostCents"),
            notes = item.getString("notes"),
            warrantyPeriod = item.getString("warrantyPeriod"),
            createdAtMillis = item.getLong("createdAtMillis"),
            updatedAtMillis = item.getLong("updatedAtMillis"),
            customerName = item.optString("customerName", ""),
            warrantyDays = if (item.has("warrantyDays")) item.getInt("warrantyDays") else 90,
            repairDevice = item.optString("repairDevice", ""),
            brand = item.optString("brand", "")
        )
    }
}
