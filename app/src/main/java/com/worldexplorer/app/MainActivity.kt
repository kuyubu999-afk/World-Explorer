package com.worldexplorer.app

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.Locale

class MainActivity : Activity() {
    private var textToSpeech: TextToSpeech? = null
    private var pendingText: String? = null
    private var pendingLanguage: String = "zh-CN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(SpeechBridge(), "AndroidSpeech")
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    private inner class SpeechBridge {
        @JavascriptInterface
        fun speak(text: String, language: String) {
            runOnUiThread {
                pendingText = text
                pendingLanguage = language
                if (textToSpeech == null) {
                    textToSpeech = TextToSpeech(this@MainActivity) { status ->
                        if (status == TextToSpeech.SUCCESS) speakPending()
                    }
                } else {
                    speakPending()
                }
            }
        }

        @JavascriptInterface
        fun stop() {
            runOnUiThread { textToSpeech?.stop() }
        }
    }

    private fun speakPending() {
        val engine = textToSpeech ?: return
        engine.language = if (pendingLanguage.startsWith("zh")) Locale.SIMPLIFIED_CHINESE else Locale.US
        val text = pendingText ?: return
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "world-explorer-guide")
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        val view = (findViewById<android.view.View>(android.R.id.content) as? android.view.ViewGroup)
            ?.getChildAt(0) as? WebView
        if (view?.canGoBack() == true) view.goBack() else super.onBackPressed()
    }
}
