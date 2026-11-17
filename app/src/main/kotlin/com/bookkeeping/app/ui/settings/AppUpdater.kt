package com.bookkeeping.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.bookkeeping.app.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** 应用内更新状态机：Idle → Checking → Available/UpToDate/Failed → Downloading → Ready（自动拉起系统安装器）。 */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpToDate(val version: String) : UpdateState
    data class Available(val version: String, val notes: String, val downloadUrl: String, val sha256: String?) : UpdateState
    data class Downloading(val progress: Float) : UpdateState
    data class Ready(val version: String) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/**
 * 应用内更新器：设置页「检查更新」的驱动层。
 * - 检查：GitHub Releases latest（免登录，60 次/小时/IP，低频操作足够）；
 *   远端版本取 tag_name（v1.0.3），与本机 versionName 逐段数字比较。
 * - 下载：APK 资产流式写入 cacheDir/update/，实时进度；release 说明里含 SHA-256 时校验；
 * - 安装：FileProvider content:// 拉起系统安装器（首次需在系统设置授权「安装未知应用」，见设置页引导）。
 * 单例自持协程作用域：切换页面不会中断下载；cancel() 可主动取消。
 */
object AppUpdater {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()
    private var downloadJob: Job? = null
    private var apkFile: File? = null

    fun check() {
        if (_state.value is UpdateState.Checking || _state.value is UpdateState.Downloading) return
        _state.value = UpdateState.Checking
        scope.launch {
            _state.value = try {
                val release = fetchLatest()
                val remote = release.tag.trimStart('v', 'V')
                if (isNewerVersion(remote, BuildConfig.VERSION_NAME)) {
                    UpdateState.Available(remote, release.body, release.apkUrl, release.sha256)
                } else {
                    UpdateState.UpToDate(BuildConfig.VERSION_NAME)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UpdateState.Failed("检查更新失败：${e.message ?: "网络异常，请稍后再试"}")
            }
        }
    }

    fun download(context: Context, version: String, downloadUrl: String, expectedSha256: String?) {
        downloadJob?.cancel()
        val appContext = context.applicationContext
        downloadJob = scope.launch {
            _state.value = UpdateState.Downloading(0f)
            try {
                val dir = File(appContext.cacheDir, "update").apply { mkdirs() }
                val file = File(dir, "update-${version}.apk")
                val conn = URL(downloadUrl).openConnection() as HttpURLConnection
                conn.connectTimeout = 15_000
                conn.readTimeout = 30_000
                conn.instanceFollowRedirects = true
                val digest = MessageDigest.getInstance("SHA-256")
                var actualSha: String? = null
                conn.inputStream.use { input ->
                    file.outputStream().use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var total = conn.contentLengthLong
                        var done = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            out.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                            done += read
                            if (total > 0) _state.value = UpdateState.Downloading((done.toFloat() / total).coerceIn(0f, 1f))
                        }
                    }
                }
                if (expectedSha256 != null) {
                    actualSha = digest.digest().joinToString("") { "%02x".format(it) }
                    require(actualSha.equals(expectedSha256, ignoreCase = true)) { "安装包校验失败，已放弃安装" }
                }
                apkFile = file
                _state.value = UpdateState.Ready(version)
                install(appContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UpdateState.Failed("下载失败：${e.message ?: "网络异常，请稍后再试"}")
            }
        }
    }

    /** 拉起系统安装器（content:// 经 FileProvider 授权；安装确认框由系统给出，无法静默）。 */
    fun install(context: Context) {
        val file = apkFile ?: return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        _state.value = UpdateState.Idle
    }

    /** 取消进行中的检查/下载并回到空闲。 */
    fun cancel() {
        downloadJob?.cancel()
        downloadJob = null
        _state.value = UpdateState.Idle
    }

    /** 返回 (tag 版本号, release 说明, APK 下载直链, release 说明中的 SHA-256 或 null)。 */
    private fun fetchLatest(): LatestRelease {
        val conn = URL("https://api.github.com/repos/Githao3/bookkeeping/releases/latest").openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        val code = conn.responseCode
        require(code in 200..299) { "GitHub 返回 $code" }
        val json = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val root = org.json.JSONObject(json)
        val tag = root.getString("tag_name")
        val body = root.optString("body", "")
        var apkUrl: String? = null
        val assets = root.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
                apkUrl = asset.getString("browser_download_url")
                break
            }
        }
        requireNotNull(apkUrl) { "Release 中没有 APK 附件" }
        val sha256 = SHA256_REGEX.find(body)?.groupValues?.get(1)
        return LatestRelease(tag, body, apkUrl, sha256)
    }

    private data class LatestRelease(val tag: String, val body: String, val apkUrl: String, val sha256: String?)
}

private val SHA256_REGEX = Regex("(?i)sha[-_]?256\\s*[:：`\\s]*([0-9a-f]{64})")

/** 逐段数字比较，1.10 > 1.9；长度不等时前缀相同则更长者新。 */
fun isNewerVersion(remote: String, local: String): Boolean {
    fun parts(v: String) = v.split('.').map { it.trim().toIntOrNull() ?: 0 }
    val r = parts(remote)
    val l = parts(local)
    for (i in 0 until maxOf(r.size, l.size)) {
        val rv = r.getOrElse(i) { 0 }
        val lv = l.getOrElse(i) { 0 }
        if (rv != lv) return rv > lv
    }
    return false
}
