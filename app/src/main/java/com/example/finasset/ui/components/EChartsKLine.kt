package com.example.finasset.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.finasset.data.network.KLineData
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EChartsKLine(
    klineData: KLineData,
    isCandlestick: Boolean = true,
    redUpGreenDown: Boolean = true,
    period: String = "day",
    modifier: Modifier = Modifier
) {
    val json = remember(klineData, isCandlestick, redUpGreenDown, period) {
        buildChartJson(klineData, isCandlestick, redUpGreenDown, period)
    }
    val jsonJs = remember(json) { JSONObject.quote(json.toString()) }
    val latestJsonJs by rememberUpdatedState(jsonJs)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                WebView.setWebContentsDebuggingEnabled(true)
                setBackgroundColor(0x00000000)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        val msg = consoleMessage.message()
                        val src = consoleMessage.sourceId()
                        val line = consoleMessage.lineNumber()
                        val level = consoleMessage.messageLevel()
                        when (level) {
                            ConsoleMessage.MessageLevel.ERROR -> Log.e(TAG, "console: $msg ($src:$line)")
                            ConsoleMessage.MessageLevel.WARNING -> Log.w(TAG, "console: $msg ($src:$line)")
                            else -> Log.i(TAG, "console: $msg ($src:$line)")
                        }
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        Log.i(TAG, "onPageStarted url=$url")
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        Log.e(TAG, "onReceivedError url=${request?.url} code=${error?.errorCode} desc=${error?.description}")
                    }

                    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                        Log.e(TAG, "onReceivedHttpError url=${request?.url} status=${errorResponse?.statusCode}")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.i(TAG, "onPageFinished url=$url")
                        pollForReady(view, latestJsonJs)
                    }
                }
                loadUrl("file:///android_asset/kline.html")
            }
        },
        update = { view ->
            Log.i(TAG, "update isCandle=$isCandlestick dataLen=${klineData.dates.size}")
            view.evaluateJavascript(
                "if(window._echartsReady) updateChart($latestJsonJs); else window._pendingData=JSON.parse($latestJsonJs);",
                null
            )
            view.evaluateJavascript("if(window.forceResize) forceResize();", null)
        }
    )
}

private fun pollForReady(view: WebView?, jsonJs: String, attempt: Int = 0) {
    if (view == null || attempt > 20) return
    view.evaluateJavascript("window._echartsReady", { result ->
        if (result == "true") {
            Log.i(TAG, "echarts ready, push data")
            view.evaluateJavascript("updateChart($jsonJs)", null)
            view.evaluateJavascript("window._lastError || ''", { err ->
                if (err != null && err != "\"\"") {
                    Log.e(TAG, "js _lastError=$err")
                }
            })
        } else {
            Log.i(TAG, "echarts not ready yet: attempt=$attempt result=$result")
            Handler(Looper.getMainLooper()).postDelayed({
                pollForReady(view, jsonJs, attempt + 1)
            }, 300)
        }
    })
}

private const val TAG = "EChartsKLine"

private fun buildChartJson(data: KLineData, isCandlestick: Boolean, redUp: Boolean, period: String): JSONObject {
    val n = minOf(
        data.dates.size,
        data.closes.size,
        if (isCandlestick) data.opens.size else data.closes.size,
        if (isCandlestick) data.highs.size else data.closes.size,
        if (isCandlestick) data.lows.size else data.closes.size
    )
    val obj = JSONObject()
    obj.put("isCandlestick", isCandlestick)
    obj.put("redUp", redUp)
    obj.put("period", period)
    obj.put("dates", JSONArray(data.dates.take(n)))
    obj.put("opens", JSONArray((if (data.opens.isEmpty()) data.closes else data.opens).take(n)))
    obj.put("closes", JSONArray(data.closes.take(n)))
    obj.put("highs", JSONArray((if (data.highs.isEmpty()) data.closes else data.highs).take(n)))
    obj.put("lows", JSONArray((if (data.lows.isEmpty()) data.closes else data.lows).take(n)))
    obj.put("volumes", JSONArray(data.volumes.take(n)))
    return obj
}
