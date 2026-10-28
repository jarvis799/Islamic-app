package com.arian.noorislamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object Api {
    private val client = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
    private const val QURAN = "https://alfurqan.online"
    suspend fun getJson(url: String): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url).header("User-Agent", "NoorIslamicAndroid/1.0").build()
        client.newCall(req).execute().use { r -> if (!r.isSuccessful) error("HTTP ${r.code}"); JSONObject(r.body?.string() ?: "{}") }
    }
    suspend fun getSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        val json = getJson("$QURAN/api/v1/surahs")
        val arr = json.optJSONArray("surahs") ?: return@withContext emptyList()
        buildList { for (i in 0 until arr.length()) { val o=arr.getJSONObject(i); add(Surah(o.optInt("number",i+1), o.optString("name","سورة"), o.optString("transliteration","Surah ${i+1}"), o.optString("translation",""), o.optInt("ayahCount",7), o.optString("revelationType",""))) } }
    }
    suspend fun getReciters(): List<Reciter> = withContext(Dispatchers.IO) {
        val json=getJson("$QURAN/api/v1/reciters"); val arr=json.optJSONArray("reciters") ?: return@withContext emptyList()
        buildList { for(i in 0 until arr.length()){val o=arr.getJSONObject(i); add(Reciter(o.optString("id"),o.optString("name"),o.optString("arabicName")))} }
    }
    fun audioUrl(reciter: String, surah: Int, ayah: Int) = "$QURAN/api/v1/audio/$reciter/surah/$surah/ayah/$ayah"
    suspend fun download(url: String, destination: File) = withContext(Dispatchers.IO) {
        destination.parentFile?.mkdirs()
        val req=Request.Builder().url(url).build()
        client.newCall(req).execute().use { r -> if(!r.isSuccessful) error("Download failed: HTTP ${r.code}"); val body=r.body ?: error("Empty audio response"); body.byteStream().use { input -> FileOutputStream(destination).use { output -> input.copyTo(output) } } }
    }
    suspend fun prayerTimes(city: String = "Dhaka", country: String = "Bangladesh"): Map<String,String> = withContext(Dispatchers.IO) {
        val url="https://api.aladhan.com/v1/timingsByCity?city=${java.net.URLEncoder.encode(city,"UTF-8")}&country=${java.net.URLEncoder.encode(country,"UTF-8")}&method=1"
        val json=getJson(url); val data=json.optJSONObject("data")?.optJSONObject("timings") ?: error("Prayer times unavailable")
        listOf("Fajr","Sunrise","Dhuhr","Asr","Maghrib","Isha").associateWith { data.optString(it,"--:--").substringBefore(" ") }
    }
}

data class Surah(val number:Int,val arabic:String,val transliteration:String,val translation:String,val ayahCount:Int,val revelationType:String)
data class Reciter(val id:String,val name:String,val arabicName:String)
