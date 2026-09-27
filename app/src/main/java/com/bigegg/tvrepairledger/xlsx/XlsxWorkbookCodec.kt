package com.bigegg.tvrepairledger.xlsx

import org.w3c.dom.Element
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory

fun writeWorkbook(rows: List<List<String>>): ByteArray {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zip ->
        zip.putXml("[Content_Types].xml", contentTypesXml)
        zip.putXml("_rels/.rels", rootRelsXml)
        zip.putXml("xl/workbook.xml", workbookXml)
        zip.putXml("xl/_rels/workbook.xml.rels", workbookRelsXml)
        zip.putXml("xl/worksheets/sheet1.xml", sheetXml(rows))
    }
    return output.toByteArray()
}

fun readWorkbookRows(input: InputStream): List<List<String?>> {
    val entries = readZipEntries(input)
    val sharedStrings = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
    val sheetXml = entries["xl/worksheets/sheet1.xml"]
        ?: entries.entries.firstOrNull { it.key.startsWith("xl/worksheets/sheet") }?.value
        ?: return emptyList()
    return parseSheet(sheetXml, sharedStrings)
}

private fun readZipEntries(input: InputStream): Map<String, ByteArray> {
    val entries = mutableMapOf<String, ByteArray>()
    ZipInputStream(input).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                entries[entry.name] = zip.readBytes()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
    return entries
}

private fun parseSharedStrings(xml: ByteArray): List<String> {
    val document = newDocumentBuilder().parse(xml.inputStream())
    val nodes = document.getElementsByTagName("si")
    return (0 until nodes.length).map { index ->
        (nodes.item(index) as Element).textContent.orEmpty()
    }
}

private fun parseSheet(
    xml: ByteArray,
    sharedStrings: List<String>
): List<List<String?>> {
    val document = newDocumentBuilder().parse(xml.inputStream())
    val rowNodes = document.getElementsByTagName("row")
    return (0 until rowNodes.length).map { rowIndex ->
        val rowElement = rowNodes.item(rowIndex) as Element
        val cells = rowElement.getElementsByTagName("c")
        val values = mutableMapOf<Int, String?>()
        var maxColumn = -1

        for (cellIndex in 0 until cells.length) {
            val cell = cells.item(cellIndex) as Element
            val columnIndex = cell.getAttribute("r").takeIf { it.isNotBlank() }?.let(::columnIndexFromReference)
                ?: cellIndex
            maxColumn = maxOf(maxColumn, columnIndex)
            values[columnIndex] = readCellValue(cell, sharedStrings)
        }

        (0..maxColumn.coerceAtLeast(0)).map { column -> values[column].orEmpty() }
    }
}

private fun readCellValue(
    cell: Element,
    sharedStrings: List<String>
): String {
    val type = cell.getAttribute("t")
    return when (type) {
        "s" -> {
            val index = cell.firstText("v").toIntOrNull()
            if (index == null) "" else sharedStrings.getOrNull(index).orEmpty()
        }
        "inlineStr" -> cell.firstText("t")
        else -> cell.firstText("v")
    }
}

private fun Element.firstText(tagName: String): String {
    val nodes = getElementsByTagName(tagName)
    return if (nodes.length == 0) "" else nodes.item(0).textContent.orEmpty()
}

private fun columnIndexFromReference(reference: String): Int {
    val letters = reference.takeWhile { it.isLetter() }.uppercase()
    var index = 0
    letters.forEach { char ->
        index = index * 26 + (char - 'A' + 1)
    }
    return index - 1
}

private fun sheetXml(rows: List<List<String>>): String {
    val body = rows.mapIndexed { rowIndex, row ->
        val rowNumber = rowIndex + 1
        val cells = row.mapIndexed { columnIndex, value ->
            val reference = "${columnName(columnIndex)}$rowNumber"
            """<c r="$reference" t="inlineStr"><is><t>${value.xmlEscape()}</t></is></c>"""
        }.joinToString("")
        """<row r="$rowNumber">$cells</row>"""
    }.joinToString("")
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>$body</sheetData>
</worksheet>"""
}

private fun columnName(index: Int): String {
    var value = index + 1
    val name = StringBuilder()
    while (value > 0) {
        val remainder = (value - 1) % 26
        name.insert(0, 'A' + remainder)
        value = (value - 1) / 26
    }
    return name.toString()
}

private fun String.xmlEscape(): String = buildString {
    this@xmlEscape.forEach { char ->
        when (char) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&apos;")
            else -> append(char)
        }
    }
}

private fun ZipOutputStream.putXml(name: String, content: String) {
    putNextEntry(ZipEntry(name))
    write(content.toByteArray(Charsets.UTF_8))
    closeEntry()
}

private fun newDocumentBuilder() = DocumentBuilderFactory.newInstance().apply {
    isNamespaceAware = false
}.newDocumentBuilder()

private val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

private val rootRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

private val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="维修台账" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

private val workbookRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
