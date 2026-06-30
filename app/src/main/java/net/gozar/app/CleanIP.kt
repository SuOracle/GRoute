package net.gozar.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Finds "clean" Cloudflare CDN edge IPs — ones still reachable under filtering — by
 * sampling Cloudflare's published IPv4 ranges and TCP-probing port 443 on each. The
 * connect time is the latency; unreachable/blocked IPs time out and are dropped.
 */
object CleanIpScanner {

    // Cloudflare IPv4 CDN ranges (cloudflare.com/ips-v4) as (base, prefix).
    private val CIDRS = listOf(
        "173.245.48.0" to 20, "103.21.244.0" to 22, "103.22.200.0" to 22,
        "103.31.4.0" to 22, "141.101.64.0" to 18, "108.162.192.0" to 18,
        "190.93.240.0" to 20, "188.114.96.0" to 20, "197.234.240.0" to 22,
        "198.41.128.0" to 17, "162.158.0.0" to 15, "104.16.0.0" to 13,
        "104.24.0.0" to 14, "172.64.0.0" to 13, "131.0.72.0" to 22
    )

    private val ranges = CIDRS.map { (base, prefix) ->
        Pair(ipToLong(base), 1L shl (32 - prefix))   // (baseAsLong, size)
    }
    private val totalSize = ranges.sumOf { it.second }

    private fun ipToLong(ip: String): Long {
        val p = ip.split(".")
        return (p[0].toLong() shl 24) or (p[1].toLong() shl 16) or
                (p[2].toLong() shl 8) or p[3].toLong()
    }

    private fun longToIp(v: Long): String =
        "${(v shr 24) and 0xff}.${(v shr 16) and 0xff}.${(v shr 8) and 0xff}.${v and 0xff}"

    private fun randomIp(rnd: java.util.Random): String {
        var pick = (rnd.nextDouble() * totalSize).toLong()
        for ((base, size) in ranges) {
            if (pick < size) return longToIp(base + (rnd.nextDouble() * size).toLong())
            pick -= size
        }
        return longToIp(ranges[0].first)
    }

    // Distinct random IPs sampled across all ranges, weighted by range size.
    fun candidates(n: Int): List<String> {
        val rnd = java.util.Random()
        val set = LinkedHashSet<String>()
        var guard = 0
        while (set.size < n && guard++ < n * 30) set.add(randomIp(rnd))
        return set.toList()
    }
}

data class CleanHit(val ip: String, val ms: Int)

@Composable
fun CleanIpScreen() {
    val lang = LocalLang.current
    val t: (String) -> String = { Strings.get(lang, it) }
    val n: (String) -> String = { localizeDigits(it, lang) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var count by remember { mutableStateOf(250) }
    var scanning by remember { mutableStateOf(false) }
    var tested by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var copied by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }
    val hits = remember { mutableStateListOf<CleanHit>() }

    LaunchedEffect(copied) { if (copied.isNotEmpty()) { kotlinx.coroutines.delay(1800); copied = "" } }

    fun start() {
        hits.clear(); tested = 0; total = count; copied = ""; scanning = true
        job = scope.launch {
            val ips = CleanIpScanner.candidates(count)
            val sem = Semaphore(48)
            coroutineScope {
                ips.forEach { ip ->
                    launch {
                        sem.withPermit {
                            val r = Pinger.ping(ip, 443, 1500)
                            if (r is PingResult.Ok) {
                                hits.add(CleanHit(ip, r.ms))
                                hits.sortBy { it.ms }
                            }
                            tested++
                        }
                    }
                }
            }
            scanning = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(t("scan_intro"), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text(t("scan_count"), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(100, 250, 500, 1000).forEach { c ->
                val sel = c == count
                if (sel) {
                    Button(onClick = { count = c }, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)) { Text(n("$c")) }
                } else {
                    OutlinedButton(onClick = { if (!scanning) count = c }, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)) { Text(n("$c")) }
                }
            }
        }

        Button(
            onClick = { if (scanning) { job?.cancel(); scanning = false } else start() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                if (scanning) "${t("scanning")}  ${n("$tested")}/${n("$total")}" else t("scan_start"),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (!scanning && tested > 0) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (hits.isEmpty()) t("scan_none") else n(t("scan_done").format(hits.size)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hits.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (hits.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(hits.joinToString("\n") { it.ip }))
                            copied = t("copied_all")
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(t("copy_all")) }
                }
            }
        }

        if (copied.isNotEmpty()) {
            Text(copied, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(hits, key = { it.ip }) { h ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        clipboard.setText(AnnotatedString(h.ip))
                        copied = "${t("copied")} ${h.ip}"
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            n(h.ip),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${n("${h.ms}")} ${t("unit_ms")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(0.dp))
                        Icon(
                            Icons.Filled.ContentCopy, contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}