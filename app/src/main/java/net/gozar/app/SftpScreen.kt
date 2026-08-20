package net.gozar.app

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SftpScreen(
    host: SshHost,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLang.current
    val t: (String) -> String = { key -> Strings.get(lang, key) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var path by remember(host.id) { mutableStateOf("") }
    var entries by remember(host.id) { mutableStateOf<List<SftpEntry>>(emptyList()) }
    var loading by remember(host.id) { mutableStateOf(true) }
    var error by remember(host.id) { mutableStateOf("") }
    var pending by remember(host.id) { mutableStateOf<SftpEntry?>(null) }
    var newFolder by remember(host.id) { mutableStateOf(false) }
    var folderName by remember(host.id) { mutableStateOf("") }
    var busyLabel by remember(host.id) { mutableStateOf("") }

    suspend fun load(target: String) {
        loading = true
        error = ""
        var result = SftpBrowser.list(host.id, target)
        if (result.isFailure && !SshManager.isUp(host.id)) {
            busyLabel = t("ssh_connecting")
            SshManager.connect(host)
            busyLabel = ""
            result = SftpBrowser.list(host.id, target)
        }
        result
            .onSuccess { entries = it; path = target }
            .onFailure { error = it.message.orEmpty().ifBlank { t("sftp_failed") } }
        loading = false
    }

    LaunchedEffect(host.id) {
        val start = SftpBrowser.home(host.id)
        load(start)
    }

    BackHandler {
        if (path.trimEnd('/').isNotEmpty() && path != "/") {
            scope.launch { load(SftpBrowser.parent(path)) }
        } else onBack()
    }

    val downloadTarget = remember(host.id) { mutableStateOf<SftpEntry?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val entry = downloadTarget.value ?: return@rememberLauncherForActivityResult
        downloadTarget.value = null
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busyLabel = t("sftp_downloading")
            val out = runCatching { context.contentResolver.openOutputStream(uri) }.getOrNull()
            if (out == null) {
                error = t("sftp_failed")
            } else {
                SftpBrowser.download(host.id, SftpBrowser.join(path, entry.name), out)
                    .onFailure { error = it.message.orEmpty().ifBlank { t("sftp_failed") } }
            }
            busyLabel = ""
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busyLabel = t("sftp_uploading")
            val name = queryName(context, uri)
            val input = runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()
            if (input == null) {
                error = t("sftp_failed")
            } else {
                SftpBrowser.upload(host.id, input, SftpBrowser.join(path, name))
                    .onSuccess { load(path) }
                    .onFailure { error = it.message.orEmpty().ifBlank { t("sftp_failed") } }
            }
            busyLabel = ""
        }
    }

    pending?.let { target ->
        SftpConfirm(
            title = t("sftp_delete_title"),
            body = t("sftp_delete_q").format(target.name),
            confirm = t("delete"),
            dismiss = t("cancel"),
            onConfirm = {
                val victim = target
                pending = null
                scope.launch {
                    SftpBrowser.delete(host.id, SftpBrowser.join(path, victim.name), victim.isDir)
                        .onSuccess { load(path) }
                        .onFailure { error = it.message.orEmpty().ifBlank { t("sftp_failed") } }
                }
            },
            onDismiss = { pending = null }
        )
    }

    Column(
        modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                path.ifBlank { "/" },
                style = MaterialTheme.typography.bodySmall,
                fontFamily = monoFont(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { scope.launch { load(SftpBrowser.parent(path)) } },
                enabled = path.trimEnd('/').isNotEmpty() && path != "/",
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { scope.launch { load(path) } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = error.isNotEmpty() || busyLabel.isNotEmpty(),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140))
        ) {
            Text(
                busyLabel.ifBlank { error },
                style = MaterialTheme.typography.bodySmall,
                color = if (busyLabel.isNotBlank()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        AnimatedVisibility(visible = newFolder) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    folderName, { folderName = it },
                    label = { Text(t("sftp_folder_name")) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = monoFont()),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                SshFillButton(
                    text = t("save"),
                    onClick = {
                        val name = folderName.trim()
                        folderName = ""
                        newFolder = false
                        if (name.isNotEmpty()) scope.launch {
                            SftpBrowser.mkdir(host.id, SftpBrowser.join(path, name))
                                .onSuccess { load(path) }
                                .onFailure { error = it.message.orEmpty().ifBlank { t("sftp_failed") } }
                        }
                    },
                    accent = MaterialTheme.colorScheme.primary,
                    minHeight = 44.dp
                )
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        t("sftp_empty"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(entries, key = { it.name }) { entry ->
                        SftpRow(
                            entry = entry,
                            onOpen = {
                                if (entry.isDir) {
                                    scope.launch { load(SftpBrowser.join(path, entry.name)) }
                                }
                            },
                            onDownload = {
                                downloadTarget.value = entry
                                saveLauncher.launch(entry.name)
                            },
                            onDelete = { pending = entry }
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SshFillButton(
                text = t("sftp_upload"),
                onClick = { openLauncher.launch(arrayOf("*/*")) },
                accent = MaterialTheme.colorScheme.primary,
                minHeight = 46.dp,
                modifier = Modifier.weight(1f)
            )
            SshFillButton(
                text = t("sftp_new_folder"),
                onClick = { newFolder = !newFolder },
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                minHeight = 46.dp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun queryName(context: android.content.Context, uri: android.net.Uri): String {
    val fromCursor = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    }.getOrNull()
    return fromCursor?.takeIf { it.isNotBlank() }
        ?: uri.lastPathSegment?.substringAfterLast('/').orEmpty().ifBlank { "upload.bin" }
}

@Composable
private fun SftpRow(
    entry: SftpEntry,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth()
            .sshPressBounce(scale, scope)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (entry.isDir) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (entry.isDir) entry.permissions
                    else "${SftpBrowser.humanSize(entry.size)}   ${entry.permissions}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = monoFont(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!entry.isDir) {
                IconButton(onClick = onDownload, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SftpConfirm(
    title: String,
    body: String,
    confirm: String,
    dismiss: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(mixedText(body), style = MaterialTheme.typography.bodyMedium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SshFillButton(
                        text = dismiss,
                        onClick = onDismiss,
                        accent = MaterialTheme.colorScheme.primary,
                        minHeight = 42.dp,
                        modifier = Modifier.weight(1f)
                    )
                    SshFillButton(
                        text = confirm,
                        onClick = onConfirm,
                        accent = MaterialTheme.colorScheme.error,
                        minHeight = 42.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
