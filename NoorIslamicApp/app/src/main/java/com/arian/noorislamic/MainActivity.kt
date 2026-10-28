package com.arian.noorislamic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Lime = Color(0xFFA3E635)
private val Ink = Color(0xFF172018)
private val Canvas = Color(0xFFFAFCF8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { NoorApp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoorApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("Home") }
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var reciters by remember { mutableStateOf<List<Reciter>>(emptyList()) }
    var selectedReciter by remember { mutableStateOf("husary") }
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var prayerTimes by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableStateOf("") }
    var tasbih by remember { mutableIntStateOf(0) }
    var bookmarks by remember { mutableStateOf(setOf<Int>()) }
    var downloaded by remember { mutableStateOf(setOf<Int>()) }
    var showReciters by remember { mutableStateOf(false) }

    fun refreshData() { scope.launch { loading=true; try { surahs=Api.getSurahs(); reciters=Api.getReciters(); prayerTimes=Api.prayerTimes(); if(reciters.isNotEmpty() && reciters.none{it.id==selectedReciter}) selectedReciter=reciters.first().id; message="" } catch(e:Exception) { message="Network/API error: ${e.message ?: "Please try again"}" } finally { loading=false } } }
    LaunchedEffect(Unit) { refreshData() }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    MaterialTheme(colorScheme = lightColorScheme(primary=Lime, onPrimary=Ink, background=Canvas, surface=Color.White, onSurface=Ink)) {
        Scaffold(containerColor=Canvas, topBar={
            TopAppBar(title={ Column { Text("NOOR", fontWeight=FontWeight.Black, letterSpacing=3.sp, fontSize=20.sp); Text("YOUR DAILY ISLAMIC COMPANION", fontSize=9.sp, letterSpacing=1.5.sp, color=Ink.copy(.55f)) } }, actions={ IconButton(onClick={tab="Settings"}) { Icon(Icons.Default.Settings, contentDescription="Settings") } }, colors=TopAppBarDefaults.topAppBarColors(containerColor=Canvas))
        }, bottomBar={ NavigationBar(containerColor=Color.White) {
            listOf("Home" to Icons.Default.Home,"Quran" to Icons.Default.MenuBook,"Prayer" to Icons.Default.AccessTime,"Tools" to Icons.Default.Explore).forEach { (name,icon) -> NavigationBarItem(selected=tab==name,onClick={tab=name},icon={Icon(icon,contentDescription=name)},label={Text(name)},colors=NavigationBarItemDefaults.colors(selectedIndicatorColor=Lime)) }
        } }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (message.isNotBlank()) Card(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=4.dp), colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3E0))) { Row(Modifier.padding(12.dp), verticalAlignment=Alignment.CenterVertically) { Text(message, Modifier.weight(1f), fontSize=12.sp); TextButton(onClick={refreshData()}) { Text("Retry") } } }
                if (loading && surahs.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth(), color=Lime)
                when(tab) {
                    "Home" -> HomeScreen(prayerTimes, onOpenQuran={tab="Quran"}, onOpenPrayer={tab="Prayer"}, onOpenTools={tab="Tools"}, onOpenCalendar={tab="Calendar"}, onOpenDua={tab="Dua"}, onOpenSettings={tab="Settings"})
                    "Quran" -> QuranScreen(surahs, search, {search=it}, selectedSurah, { selectedSurah=it }, onPlay={s -> playSurah(context,s,selectedReciter); message="Playing ${s.transliteration} in background" }, onDownload={s -> scope.launch { try { downloadProgress="Downloading ${s.transliteration}…"; val folder=File(context.filesDir,"quran/surah_${s.number}"); for(a in 1..s.ayahCount){ val dest=File(folder,"ayah_$a.mp3"); if(!dest.exists()) Api.download(Api.audioUrl(selectedReciter,s.number,a),dest); downloadProgress="${s.transliteration}: $a/${s.ayahCount}" }; downloaded=downloaded+s.number; downloadProgress="${s.transliteration} saved for offline playback" } catch(e:Exception){ downloadProgress="Download failed: ${e.message}" } } }, downloaded, bookmarks, {n -> bookmarks=if(n in bookmarks) bookmarks-n else bookmarks+n}, showReciters, {showReciters=it}, reciters, selectedReciter, {selectedReciter=it}, downloadProgress, onPlayOffline={s -> playOffline(context,s,selectedReciter); message="Playing downloaded ${s.transliteration}"})
                    "Prayer" -> PrayerScreen(prayerTimes, onRefresh={refreshData()}, onAskLocation={ if(Build.VERSION.SDK_INT>=33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) })
                    "Tools" -> ToolsScreen(onDua={tab="Dua"},onTasbih={tab="Tasbih"},onQibla={tab="Qibla"},onCalendar={tab="Calendar"})
                    "Dua" -> DuaScreen()
                    "Tasbih" -> TasbihScreen(tasbih,{tasbih=it})
                    "Qibla" -> QiblaScreen()
                    "Calendar" -> CalendarScreen()
                    else -> SettingsScreen()
                }
            }
        }
    }
}

private fun playSurah(context: android.content.Context, s: Surah, reciter: String) {
    val urls=ArrayList<String>(); for(a in 1..s.ayahCount) urls.add(Api.audioUrl(reciter,s.number,a))
    val i=Intent(context,QuranPlaybackService::class.java).putStringArrayListExtra(QuranPlaybackService.EXTRA_URLS,urls).putExtra(QuranPlaybackService.EXTRA_TITLE,s.transliteration)
    ContextCompat.startForegroundService(context,i)
}
private fun playOffline(context: android.content.Context, s: Surah, reciter: String) {
    val folder=File(context.filesDir,"quran/surah_${s.number}"); val urls=ArrayList<String>()
    for(a in 1..s.ayahCount){ val f=File(folder,"ayah_$a.mp3"); urls.add(if(f.exists()) android.net.Uri.fromFile(f).toString() else Api.audioUrl(reciter,s.number,a)) }
    val i=Intent(context,QuranPlaybackService::class.java).putStringArrayListExtra(QuranPlaybackService.EXTRA_URLS,urls).putExtra(QuranPlaybackService.EXTRA_TITLE,s.transliteration)
    ContextCompat.startForegroundService(context,i)
}

@Composable private fun HomeScreen(times:Map<String,String>,onOpenQuran:()->Unit,onOpenPrayer:()->Unit,onOpenTools:()->Unit,onOpenCalendar:()->Unit,onOpenDua:()->Unit,onOpenSettings:()->Unit) {
    LazyColumn(contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Text("Assalamu Alaikum", fontSize=14.sp, color=Ink.copy(.65f)); Text("Find your calm.\nReconnect with faith.", fontSize=29.sp, lineHeight=34.sp, fontWeight=FontWeight.Bold, color=Ink) }
        item { Card(colors=CardDefaults.cardColors(containerColor=Ink), shape=RoundedCornerShape(28.dp), modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(22.dp)) { Text("ٱلسَّلَامُ عَلَيْكُمْ", fontSize=28.sp, color=Lime, modifier=Modifier.fillMaxWidth(), textAlign=TextAlign.End); Spacer(Modifier.height(8.dp)); Text("A little every day brings the heart closer.", color=Color.White, fontSize=15.sp); Spacer(Modifier.height(18.dp)); Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color=Color.White.copy(.7f), fontSize=12.sp); Text("Daily reflection", color=Color.White, fontWeight=FontWeight.SemiBold) }; Text("☾", color=Lime, fontSize=44.sp) } } } }
        item { SectionTitle("Prayer times", "TODAY"); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Fajr","Dhuhr","Asr","Maghrib","Isha").forEach { p -> Card(Modifier.weight(1f), shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(vertical=12.dp, horizontal=4.dp), horizontalAlignment=Alignment.CenterHorizontally) { Text(p.take(3), fontSize=10.sp, color=Ink.copy(.6f)); Text(times[p] ?: "--:--", fontSize=11.sp, fontWeight=FontWeight.Bold) } } } } }
        item { SectionTitle("Explore", "YOUR ESSENTIALS"); Column(verticalArrangement=Arrangement.spacedBy(10.dp)) { Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { FeatureTile("Al-Quran","Read & listen","۞",Modifier.weight(1f),onOpenQuran); FeatureTile("Prayer times","Stay on time","◷",Modifier.weight(1f),onOpenPrayer) }; Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { FeatureTile("Dua & Azkar","Daily remembrance","✧",Modifier.weight(1f),onOpenDua); FeatureTile("Islamic tools","Qibla & Tasbih","☼",Modifier.weight(1f),onOpenTools) }; Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { FeatureTile("Hijri calendar","Islamic dates","☾",Modifier.weight(1f),onOpenCalendar); FeatureTile("Settings","Personalize","⚙",Modifier.weight(1f),onOpenSettings) } } }
        item { Text("Quran audio is provided through a public API. Internet is required for streaming; download a Surah for offline use.", color=Ink.copy(.55f), fontSize=11.sp, lineHeight=16.sp) }
    }
}
@Composable private fun SectionTitle(title:String,caption:String) { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text(title,fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f)); Text(caption,fontSize=9.sp,letterSpacing=1.sp,color=Ink.copy(.5f)) } }
@Composable private fun FeatureTile(title:String,subtitle:String,icon:String,modifier:Modifier=Modifier,click:()->Unit) { Card(modifier.clickable{click()},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(16.dp).fillMaxWidth()) { Box(Modifier.size(38.dp).clip(RoundedCornerShape(14.dp)).background(Lime.copy(.38f)),contentAlignment=Alignment.Center){Text(icon,fontSize=22.sp,color=Ink)}; Spacer(Modifier.height(13.dp)); Text(title,fontWeight=FontWeight.Bold,fontSize=14.sp); Text(subtitle,fontSize=11.sp,color=Ink.copy(.55f)) } } }

@Composable private fun QuranScreen(surahs:List<Surah>,search:String,onSearch:(String)->Unit,selected:Surah?,onSelect:(Surah)->Unit,onPlay:(Surah)->Unit,onDownload:(Surah)->Unit,downloaded:Set<Int>,bookmarks:Set<Int>,onBookmark:(Int)->Unit,showReciters:Boolean,onShowReciters:(Boolean)->Unit,reciters:List<Reciter>,selectedReciter:String,onReciter:(String)->Unit,downloadProgress:String,onPlayOffline:(Surah)->Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        Text("Al-Quran",fontSize=26.sp,fontWeight=FontWeight.Bold); Text("Read, listen and keep your favorites",fontSize=12.sp,color=Ink.copy(.6f)); Spacer(Modifier.height(12.dp))
        OutlinedTextField(search,onSearch,Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Search Surah")},leadingIcon={Icon(Icons.Default.Search,null)},shape=RoundedCornerShape(18.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(vertical=8.dp)) { OutlinedButton(onClick={onShowReciters(!showReciters)}) { Icon(Icons.Default.GraphicEq,null); Spacer(Modifier.width(5.dp)); Text("Reciter") }; if(showReciters) { var expanded by remember { mutableStateOf(false) }; Box { TextButton(onClick={expanded=true}) { Text(reciters.firstOrNull{it.id==selectedReciter}?.name ?: selectedReciter) }; DropdownMenu(expanded,{expanded=false}) { reciters.forEach { DropdownMenuItem(text={Text(it.name)},onClick={onReciter(it.id);expanded=false;onShowReciters(false)}) } } } } }
        if(downloadProgress.isNotBlank()) Text(downloadProgress,fontSize=11.sp,color=Ink.copy(.65f),modifier=Modifier.padding(bottom=6.dp))
        if(selected!=null) { Card(colors=CardDefaults.cardColors(containerColor=Ink),shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().padding(bottom=10.dp)) { Column(Modifier.padding(16.dp)) { Text(selected.arabic,Modifier.fillMaxWidth(),textAlign=TextAlign.End,color=Lime,fontSize=26.sp); Text("${selected.transliteration} · ${selected.ayahCount} ayahs",color=Color.White,fontWeight=FontWeight.Bold); Text(selected.translation,color=Color.White.copy(.7f),fontSize=12.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { Button(onClick={onPlay(selected)},colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Ink)) { Icon(Icons.Default.PlayArrow,null); Text("Play online") }; OutlinedButton(onClick={onDownload(selected)}, colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) { Icon(Icons.Default.Download,null); Text(if(selected.number in downloaded) "Downloaded" else "Download") }; if(selected.number in downloaded) TextButton(onClick={onPlayOffline(selected)}) { Text("Offline",color=Lime) } } } } }
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)) { items(surahs.filter{search.isBlank() || it.transliteration.contains(search,true) || it.translation.contains(search,true) || it.number.toString()==search}) { s -> Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=if(selected?.number==s.number) Lime.copy(.22f) else Color.White),modifier=Modifier.fillMaxWidth().clickable{onSelect(s)}) { Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(38.dp).clip(CircleShape).background(Lime.copy(.35f)),contentAlignment=Alignment.Center){Text(s.number.toString(),fontWeight=FontWeight.Bold,fontSize=12.sp)}; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(s.transliteration,fontWeight=FontWeight.SemiBold); Text("${s.translation} • ${s.ayahCount} ayahs",fontSize=11.sp,color=Ink.copy(.55f)) }; Text(s.arabic,fontSize=20.sp); IconButton(onClick={onBookmark(s.number)}) { Icon(if(s.number in bookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,null,tint=if(s.number in bookmarks) Color(0xFF5B8E13) else Ink.copy(.55f)) } } } } }
    }
}

@Composable private fun PrayerScreen(times:Map<String,String>,onRefresh:()->Unit,onAskLocation:()->Unit) { LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { item { Text("Prayer times",fontSize=27.sp,fontWeight=FontWeight.Bold); Text("Showing Dhaka, Bangladesh by default. Confirm the calculation method with your local mosque.",fontSize=12.sp,color=Ink.copy(.6f)); Button(onClick=onRefresh,colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Ink)) { Icon(Icons.Default.Refresh,null); Text("Refresh times") } }; items(listOf("Fajr","Sunrise","Dhuhr","Asr","Maghrib","Isha")) { p -> Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically) { Text(p,Modifier.weight(1f),fontWeight=FontWeight.SemiBold); Text(times[p]?:"--:--",fontSize=19.sp,fontWeight=FontWeight.Bold) } } } } }
@Composable private fun ToolsScreen(onDua:()->Unit,onTasbih:()->Unit,onQibla:()->Unit,onCalendar:()->Unit) { Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { Text("Islamic tools",fontSize=26.sp,fontWeight=FontWeight.Bold); FeatureTile("Qibla Compass","Direction to Kaaba","⌖",Modifier.fillMaxWidth(),onQibla); FeatureTile("Digital Tasbih","Count your dhikr","◉",Modifier.fillMaxWidth(),onTasbih); FeatureTile("Dua & Azkar","Daily remembrance","✧",Modifier.fillMaxWidth(),onDua); FeatureTile("Hijri Calendar","Islamic dates","☾",Modifier.fillMaxWidth(),onCalendar) } }
@Composable private fun DuaScreen() { val duas=listOf("سُبْحَانَ اللَّهِ" to "SubhanAllah — Glory be to Allah","الْحَمْدُ لِلَّهِ" to "Alhamdulillah — All praise belongs to Allah","اللَّهُ أَكْبَرُ" to "Allahu Akbar — Allah is the Greatest","أَسْتَغْفِرُ اللَّهَ" to "Astaghfirullah — I seek Allah’s forgiveness","لَا إِلَٰهَ إِلَّا اللَّهُ" to "La ilaha illallah — There is no deity worthy of worship except Allah"); LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { item { Text("Dua & Azkar",fontSize=26.sp,fontWeight=FontWeight.Bold); Text("Common dhikr phrases. Verify full supplications and references from trusted Islamic sources.",fontSize=12.sp,color=Ink.copy(.6f)) }; items(duas) { (ar,en) -> Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp)) { Text(ar,Modifier.fillMaxWidth(),fontSize=27.sp,textAlign=TextAlign.End); Spacer(Modifier.height(8.dp)); Text(en,fontSize=13.sp) } } } } }
@Composable private fun TasbihScreen(count:Int,onCount:(Int)->Unit) { Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) { Text("Digital Tasbih",fontSize=27.sp,fontWeight=FontWeight.Bold); Text("Tap to count your dhikr",color=Ink.copy(.6f)); Spacer(Modifier.height(34.dp)); Box(Modifier.size(230.dp).clip(CircleShape).background(Lime.copy(.28f)).clickable{onCount(count+1)},contentAlignment=Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally) { Text(count.toString(),fontSize=58.sp,fontWeight=FontWeight.Bold); Text("TAP TO COUNT",fontSize=10.sp,letterSpacing=2.sp) } }; Spacer(Modifier.height(24.dp)); OutlinedButton(onClick={onCount(0)}) { Icon(Icons.Default.Refresh,null); Text("Reset counter") } } }
@Composable private fun QiblaScreen() { Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) { Text("Qibla Compass",fontSize=27.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(20.dp)); Box(Modifier.size(250.dp).clip(CircleShape).background(Color.White),contentAlignment=Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally) { Text("🕋",fontSize=54.sp); Text("QIBLA",fontSize=12.sp,letterSpacing=3.sp,fontWeight=FontWeight.Bold); Text("Compass sensor integration",fontSize=10.sp,color=Ink.copy(.55f)) } }; Spacer(Modifier.height(18.dp)); Text("This starter screen needs device location and orientation-sensor calibration before it can show a reliable live direction.",textAlign=TextAlign.Center,fontSize=12.sp,color=Ink.copy(.65f)) } }
@Composable private fun CalendarScreen() { val now=LocalDate.now(); Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { Text("Islamic calendar",fontSize=26.sp,fontWeight=FontWeight.Bold); Card(colors=CardDefaults.cardColors(containerColor=Ink),shape=RoundedCornerShape(24.dp)) { Column(Modifier.padding(24.dp)) { Text("TODAY",color=Lime,letterSpacing=2.sp,fontSize=11.sp); Text(now.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),fontSize=24.sp,color=Color.White,fontWeight=FontWeight.Bold); Text("Gregorian date",color=Color.White.copy(.65f),fontSize=12.sp) } }; Text("Hijri date and event dates should be loaded from a verified calendar API. Dates may differ according to local moon sighting.",fontSize=12.sp,color=Ink.copy(.65f)) } }
@Composable private fun SettingsScreen() { Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { Text("Settings",fontSize=27.sp,fontWeight=FontWeight.Bold); Text("Noor Islamic · Version 1.0.0",color=Ink.copy(.6f)); Text("• API: Al Furqan Quran API\n• Prayer times: AlAdhan API\n• Theme: White + Lime\n• Language support: Bengali, Arabic and English planned\n• Audio downloads are stored inside app-managed storage.",fontSize=14.sp,lineHeight=23.sp) } }
