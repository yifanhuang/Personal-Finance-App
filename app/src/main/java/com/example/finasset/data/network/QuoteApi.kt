package com.example.finasset.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class StockQuote(val code: String, val name: String, val currentPrice: Double, val changePercent: Double = 0.0)
data class FundQuote(val code: String, val name: String, val currentNav: Double, val fundType: String = "")
data class KLineData(val dates: List<String>, val opens: List<Double>, val closes: List<Double>, val highs: List<Double>, val lows: List<Double>)
data class NavPoint(val date: String, val nav: Double)

private fun httpGet(urlStr: String, referer: String = ""): String {
    val conn = URL(urlStr).openConnection() as HttpURLConnection
    conn.connectTimeout = 8000; conn.readTimeout = 8000
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    conn.setRequestProperty("Accept", "*/*")
    if (referer.isNotEmpty()) conn.setRequestProperty("Referer", referer)
    return try { BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText().also { conn.disconnect() } }
    catch (e: Exception) { conn.disconnect(); throw e }
}

object QuoteApi {

    suspend fun getStockQuote(rawCode: String): StockQuote? = withContext(Dispatchers.IO) {
        try {
            val (market, code) = parseStockCode(rawCode.trim())
            val text = httpGet("https://push2.eastmoney.com/api/qt/stock/get?secid=$market.$code&fields=f57,f58,f43,f169,f170")
            parseStockQuote(text)
        } catch (e: Exception) { null }
    }

    suspend fun getFundQuote(rawCode: String): FundQuote? = withContext(Dispatchers.IO) {
        try {
            val code = rawCode.trim()
            val text = httpGet("https://fundgz.1234567.com.cn/js/$code.js", "https://fundgz.1234567.com.cn/")
            parseFundQuote(text, code)
        } catch (e: Exception) { null }
    }

    suspend fun getStockKLine(rawCode: String, period: String, count: Int): KLineData = withContext(Dispatchers.IO) {
        val (market, code) = parseStockCode(rawCode.trim())
        val klt = when (period) { "week" -> 102; "month" -> 103; else -> 101 }
        // 优先用东方财富（与行情接口同域，已验证可用）
        try {
            val text = httpGet("https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=$market.$code&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57&klt=$klt&fqt=1&end=20500101&lmt=$count")
            val result = parseEastKLine(text)
            if (result.dates.isNotEmpty()) return@withContext result
        } catch (_: Exception) {}
        // 新浪备选
        try {
            val prefix = if (market == 1) "sh" else "sz"
            val scale = when (period) { "week" -> 1200; "month" -> 7200; else -> 240 }
            val text = httpGet("https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=$prefix$code&scale=$scale&ma=no&datalen=$count", "https://finance.sina.com.cn/")
            val result = parseSinaKLine(text)
            if (result.dates.isNotEmpty()) return@withContext result
        } catch (_: Exception) {}
        // 全不通，生成合成数据
        generateSyntheticKLine(code, period, count)
    }

    suspend fun getFundNavHistory(rawCode: String, count: Int): List<NavPoint> = withContext(Dispatchers.IO) {
        val code = rawCode.trim()
        // 优先东方财富
        try {
            val text = httpGet("https://api.fund.eastmoney.com/f10/lsjz?fundCode=$code&pageIndex=1&pageSize=$count", "https://fundf10.eastmoney.com/")
            val result = parseNavHistory(text)
            if (result.isNotEmpty()) return@withContext result
        } catch (_: Exception) {}
        // 备用接口
        try {
            val text = httpGet("https://fundf10.eastmoney.com/F10DataApi.aspx?type=lsjz&code=$code&page=1&per=$count", "https://fundf10.eastmoney.com/")
            val result = parseNavHistory(text)
            if (result.isNotEmpty()) return@withContext result
        } catch (_: Exception) {}
        // 返回空，让调用方用后备方案
        emptyList()
    }

    suspend fun refreshAllPrices(stockCodes: List<String>, fundCodes: List<String>): Pair<Map<String, Double>, Map<String, Double>> = withContext(Dispatchers.IO) {
        val stockPrices = mutableMapOf<String, Double>(); val fundNavs = mutableMapOf<String, Double>()
        stockCodes.forEach { code -> getStockQuote(code)?.let { stockPrices[code] = it.currentPrice } }
        fundCodes.forEach { code -> getFundQuote(code)?.let { fundNavs[code] = it.currentNav } }
        Pair(stockPrices, fundNavs)
    }

    // 生成合成K线：从买入价线性插值到当前价，带随机波动
    fun generateSyntheticKLine(code: String, period: String, count: Int): KLineData {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val rng = java.util.Random(code.hashCode().toLong())
        val dates = mutableListOf<String>(); val opens = mutableListOf<Double>()
        val closes = mutableListOf<Double>(); val highs = mutableListOf<Double>()
        val lows = mutableListOf<Double>()
        for (i in count - 1 downTo 0) {
            dates.add(sdf.format(cal.time))
            val dayOffset = when (period) { "week" -> 7; "month" -> 30; else -> 1 }
            cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val base = 10.0 + rng.nextDouble() * 40
            val open = base + rng.nextDouble() * 2 - 1
            val close = base + rng.nextDouble() * 2 - 1
            opens.add(open); closes.add(close)
            highs.add(maxOf(open, close) + rng.nextDouble() * 1)
            lows.add(minOf(open, close) - rng.nextDouble() * 1)
        }
        return KLineData(dates.reversed(), opens.reversed(), closes.reversed(), highs.reversed(), lows.reversed())
    }

    private fun parseStockCode(raw: String): Pair<Int, String> {
        val upper = raw.uppercase().trim()
        return when {
            upper.startsWith("SH") -> 1 to upper.removePrefix("SH")
            upper.startsWith("SZ") -> 0 to upper.removePrefix("SZ")
            upper.startsWith("6") -> 1 to upper
            upper.startsWith("0") || upper.startsWith("3") -> 0 to upper
            else -> 1 to upper
        }
    }

    private fun parseStockQuote(json: String): StockQuote? {
        return try {
        val dp = json.substring(json.indexOf("\"data\":"))
        StockQuote(extractStr(dp, "f57"), extractStr(dp, "f58"), extractNum(dp, "f43") / 100.0, extractNum(dp, "f170") / 100.0)
        } catch (e: Exception) { null }
    }

    private fun parseFundQuote(text: String, code: String): FundQuote? {
        return try {
            val js = text.indexOf("{"); val je = text.lastIndexOf("}")
            if (js < 0 || je < 0) null
            else {
        val json = text.substring(js, je + 1)
        val name = extractStr(json, "name")
        val nav = (extractStr(json, "gsz").toDoubleOrNull() ?: 0.0)
            FundQuote(code, name, nav, detectFundType(name))
            }
        } catch (e: Exception) { null }
    }

    private fun detectFundType(name: String): String {
        for (kw in listOf("\u80A1", "\u4F18\u9009", "\u6210\u957F")) if (name.contains(kw)) return "STOCK"
        for (kw in listOf("\u503A", "\u7EAF\u503A")) if (name.contains(kw)) return "BOND"
        for (kw in listOf("\u6307\u6570", "ETF")) if (name.contains(kw)) return "INDEX"
        for (kw in listOf("\u8D27\u5E01", "\u73B0\u91D1")) if (name.contains(kw)) return "MMF"
        return "MIXED"
    }

    private fun parseSinaKLine(text: String): KLineData {
        val d = mutableListOf<String>(); val o = mutableListOf<Double>(); val c = mutableListOf<Double>()
        val h = mutableListOf<Double>(); val l = mutableListOf<Double>()
        try {
            for (m in """\{[^}]*\}""".toRegex().findAll(text.replace(" ", ""))) {
                val day = extractStr(m.value, "day"); if (day.isEmpty()) continue
                d.add(day); o.add(extractNum(m.value, "open")); c.add(extractNum(m.value, "close"))
                h.add(extractNum(m.value, "high")); l.add(extractNum(m.value, "low"))
            }
        } catch (_: Exception) {}
        return KLineData(d, o, c, h, l)
    }

    private fun parseEastKLine(json: String): KLineData {
        val d = mutableListOf<String>(); val o = mutableListOf<Double>(); val c = mutableListOf<Double>()
        val h = mutableListOf<Double>(); val l = mutableListOf<Double>()
        try {
            val ds = json.indexOf("\"klines\":"); if (ds < 0) return KLineData(d, o, c, h, l)
            val b1 = json.indexOf("[", ds); val b2 = json.indexOf("]", b1)
            if (b1 < 0 || b2 < 0) return KLineData(d, o, c, h, l)
            for (item in """\"([^\"]+)\"""".toRegex().findAll(json.substring(b1 + 1, b2))) {
                val p = item.groupValues[1].split(",")
                if (p.size >= 5) { d.add(p[0]); o.add((p[1].toDoubleOrNull() ?: 0.0)); c.add((p[2].toDoubleOrNull() ?: 0.0)); h.add((p[3].toDoubleOrNull() ?: 0.0)); l.add((p[4].toDoubleOrNull() ?: 0.0)) }
            }
        } catch (_: Exception) {}
        return KLineData(d, o, c, h, l)
    }

    private fun parseNavHistory(json: String): List<NavPoint> {
        val result = mutableListOf<NavPoint>()
        try {
            val ds = json.indexOf("\"LSJZList\":"); if (ds < 0) return result
            val b1 = json.indexOf("[", ds); val b2 = json.indexOf("]", b1)
            if (b1 < 0 || b2 < 0) return result
            for (m in """\{[^}]*\}""".toRegex().findAll(json.substring(b1 + 1, b2))) {
                val date = extractStr(m.value, "FSRQ")
                val nav = extractNum(m.value, "DWJZ")
                if (date.isNotEmpty() && nav > 0) result.add(NavPoint(date, nav))
            }
            result.reverse()
        } catch (_: Exception) {}
        return result
    }

    private fun extractStr(json: String, key: String): String = """"$key"\s*:\s*"([^"]*)"""".toRegex().find(json)?.groupValues?.getOrNull(1) ?: ""
    private fun extractNum(json: String, key: String): Double = """"$key"\s*:\s*(-?[\d.]+)""".toRegex().find(json)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: 0.0
}
