package net.gozar.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kotlin.math.sqrt
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun Modifier.sshPressBounce(
    scale: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope
): Modifier = this
    .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
    .pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            scope.launch {
                scale.animateTo(
                    0.96f,
                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
            waitForUpOrCancellation()
            scope.launch {
                scale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
        }
    }

@Composable
private fun sshT(): (String) -> String {
    val lang = LocalLang.current
    return { key -> Strings.get(lang, key) }
}

@Composable
fun SshScreen(
    store: SshStore,
    onSubScreenChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val t = sshT()
    val hosts by store.hosts.collectAsState()
    val statuses by SshManager.status.collectAsState()
    val scope = rememberCoroutineScope()

    var editing by remember { mutableStateOf<SshHost?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<SshHost?>(null) }

    var terminalOpen by remember { mutableStateOf(false) }
    var shellHost by remember { mutableStateOf<SshHost?>(null) }
    var sftpHost by remember { mutableStateOf<SshHost?>(null) }
    val editorOpen = creating || editing != null

    BackHandler(enabled = editorOpen) { editing = null; creating = false }

    val shellOpen = shellHost != null
    val sftpOpen = sftpHost != null
    LaunchedEffect(editorOpen, terminalOpen, shellOpen, sftpOpen) {
        onSubScreenChange(editorOpen || terminalOpen || shellOpen || sftpOpen)
    }

    confirmDelete?.let { target ->
        SshGlassDialog(
            onDismiss = { confirmDelete = null },
            title = t("ssh_delete_title"),
            confirmLabel = t("delete"),
            onConfirm = { store.remove(target.id); confirmDelete = null },
            dismissLabel = t("cancel"),
            destructive = true
        ) {
            Text(
                mixedText(t("ssh_delete_q").format(target.title)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    AnimatedContent(
        targetState = when {
            sftpOpen -> "sftp"
            shellOpen -> "shell"
            terminalOpen -> "terminal"
            editorOpen -> "editor"
            else -> "list"
        },
        transitionSpec = {
            (scaleIn(tween(220), initialScale = 0.92f) + fadeIn(tween(220))) togetherWith
                    (scaleOut(tween(180), targetScale = 0.92f) + fadeOut(tween(180)))
        },
        label = "sshTab",
        modifier = modifier.fillMaxSize()
    ) { key ->
        if (key == "sftp") {
            val h = sftpHost
            if (h == null) sftpHost = null
            else SftpScreen(host = h, onBack = { sftpHost = null })
        } else if (key == "shell") {
            val host = shellHost
            val remote = remember(host?.id) { host?.let { SshManager.shell(it.id) } }
            if (remote == null) {
                shellHost = null
            } else {
                TerminalScreen(shell = remote, onBack = { shellHost = null })
            }
        } else if (key == "terminal") {
            val context = LocalContext.current
            LaunchedEffect(Unit) { LocalShell.start(context) }
            TerminalScreen(shell = LocalShell, onBack = { terminalOpen = false })
        } else if (key == "editor") {
            SshHostEditor(
                existing = editing,
                onSave = { host ->
                    if (editing != null) store.update(host) else store.add(host)
                    editing = null; creating = false
                },
                onCancel = { editing = null; creating = false }
            )
        } else {
            SshHostList(
                hosts = hosts,
                statuses = statuses,
                onAdd = { creating = true },
                onOpenTerminal = { terminalOpen = true },
                onConnect = { h -> scope.launch { SshManager.connect(h) } },
                onDisconnect = { h -> SshManager.disconnect(h.id) },
                onEdit = { h -> editing = h },
                onDelete = { h -> confirmDelete = h },
                onOpenShell = { h -> shellHost = h },
                onOpenSftp = { h -> sftpHost = h }
            )
        }
    }
}

@Composable
private fun SshHostList(
    hosts: List<SshHost>,
    statuses: Map<String, SshStatus>,
    onAdd: () -> Unit,
    onOpenTerminal: () -> Unit,
    onConnect: (SshHost) -> Unit,
    onDisconnect: (SshHost) -> Unit,
    onEdit: (SshHost) -> Unit,
    onDelete: (SshHost) -> Unit,
    onOpenShell: (SshHost) -> Unit,
    onOpenSftp: (SshHost) -> Unit
) {
    val t = sshT()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SshEntryCard(
                icon = Icons.Filled.Add,
                title = t("ssh_add"),
                subtitle = t("ssh_add_sub"),
                onClick = onAdd
            )
        }
        item {
            SshEntryCard(
                icon = Icons.Filled.Terminal,
                title = t("term_local"),
                subtitle = t("term_local_sub"),
                onClick = onOpenTerminal
            )
        }

        if (hosts.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(t("ssh_empty"), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        t("ssh_empty_sub"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(hosts, key = { it.id }) { host ->
            SshHostCard(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(280, easing = FastOutSlowInEasing),
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    fadeOutSpec = tween(200, easing = FastOutSlowInEasing)
                ),
                host = host,
                status = statuses[host.id] ?: SshStatus.Idle,
                onConnect = { onConnect(host) },
                onDisconnect = { onDisconnect(host) },
                onEdit = { onEdit(host) },
                onDelete = { onDelete(host) },
                onOpenShell = { onOpenShell(host) },
                onOpenSftp = { onOpenSftp(host) }
            )
        }
    }
}

@Composable
private fun SshEntryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth()
            .sshPressBounce(scale, scope)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SshHostCard(
    modifier: Modifier = Modifier,
    host: SshHost,
    status: SshStatus,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenShell: () -> Unit,
    onOpenSftp: () -> Unit
) {
    val t = sshT()
    val up = status is SshStatus.Up
    val busy = status is SshStatus.Connecting
    val failed = status is SshStatus.Failed
    val routed = SshManager.willUseTunnel(host)

    val accent = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val stateColor = when {
        up -> Color(0xFF2E9E44)
        busy -> MaterialTheme.colorScheme.primary
        else -> if (dark) Color(0xFFBFBFBF) else Color(0xFF6B6B6B)
    }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth()
            .sshPressBounce(scale, scope)
            .clip(RoundedCornerShape(20.dp))
            .clickable { if (up) onOpenShell() else if (!busy) onConnect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = if (up) 0.75f else 0.55f
            )
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = if (up) 0.55f else 0.30f))
    ) {
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .animateContentSize(tween(260, easing = FastOutSlowInEasing))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = accent,
                            trackColor = accent.copy(alpha = 0.22f)
                        )
                    } else {
                        Icon(
                            Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        mixedText(host.title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        host.endpoint,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = monoFont(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AnimatedContent(
                    targetState = when {
                        busy -> t("ssh_connecting")
                        up -> t("ssh_connected")
                        else -> t("ssh_state_off")
                    },
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(140)) },
                    label = "sshStateText"
                ) { stateLabel ->
                    Text(
                        stateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = stateColor
                    )
                }
                Spacer(Modifier.width(6.dp))
                SshStateDot(active = up, color = stateColor)
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when {
                        failed -> Icons.Filled.Cancel
                        routed -> Icons.Filled.Lock
                        else -> Icons.Filled.Public
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = when {
                        failed -> MaterialTheme.colorScheme.error
                        routed -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.width(6.dp))
                AnimatedContent(
                    targetState = if (failed) t((status as SshStatus.Failed).messageKey)
                    else if (routed) t("ssh_via_tunnel") else t("ssh_via_direct"),
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(140)) },
                    label = "sshRoute",
                    modifier = Modifier.weight(1f)
                ) { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (failed) FontWeight.Medium else FontWeight.Normal,
                        color = if (failed) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AnimatedVisibility(
                    visible = !up,
                    enter = expandHorizontally(tween(260, easing = FastOutSlowInEasing)) +
                            fadeIn(tween(200, delayMillis = 60)),
                    exit = shrinkHorizontally(tween(260, easing = FastOutSlowInEasing)) +
                            fadeOut(tween(120))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(8.dp))
                        SshCardAction(
                            icon = Icons.Filled.Edit,
                            label = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onEdit
                        )
                        Spacer(Modifier.width(6.dp))
                        SshCardAction(
                            icon = Icons.Filled.Delete,
                            label = null,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = onDelete
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = up,
                enter = expandVertically(tween(260, easing = FastOutSlowInEasing)) +
                        fadeIn(tween(200, delayMillis = 60)),
                exit = shrinkVertically(tween(260, easing = FastOutSlowInEasing)) +
                        fadeOut(tween(120))
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SshCardAction(
                            icon = Icons.Filled.Terminal,
                            label = t("ssh_open_shell"),
                            tint = MaterialTheme.colorScheme.primary,
                            onClick = onOpenShell
                        )
                        Spacer(Modifier.width(8.dp))
                        SshCardAction(
                            icon = Icons.Filled.Folder,
                            label = t("sftp"),
                            tint = MaterialTheme.colorScheme.primary,
                            onClick = onOpenSftp
                        )
                        Spacer(Modifier.width(8.dp))
                        SshCardAction(
                            icon = Icons.Filled.LinkOff,
                            label = t("ssh_disconnect"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onDisconnect
                        )
                        Spacer(Modifier.width(8.dp))
                        SshCardAction(
                            icon = Icons.Filled.Edit,
                            label = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onEdit
                        )
                        Spacer(Modifier.width(6.dp))
                        SshCardAction(
                            icon = Icons.Filled.Delete,
                            label = null,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SshCardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String?,
    tint: Color,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .sshPressBounce(scale, scope)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.28f))
    ) {
        Row(
            Modifier.padding(
                horizontal = if (label == null) 10.dp else 12.dp,
                vertical = if (label == null) 6.dp else 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(if (label == null) 21.dp else 15.dp)
            )
            if (label != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
            }
        }
    }
}

@Composable
private fun SshHostEditor(
    existing: SshHost?,
    onSave: (SshHost) -> Unit,
    onCancel: () -> Unit
) {
    val t = sshT()
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var port by remember { mutableStateOf((existing?.port ?: 22).toString()) }
    var username by remember { mutableStateOf(existing?.username ?: "") }
    var password by remember { mutableStateOf(existing?.password ?: "") }
    var direct by remember { mutableStateOf(existing?.direct ?: false) }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SshGroup(title = t("ssh_host_details")) {
            OutlinedTextField(
                label, { label = it },
                label = { Text(t("ssh_label")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                address, { address = it; error = "" },
                label = { Text(t("ssh_address")) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = monoFont()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                port, { v -> port = v.filter { it.isDigit() }.take(5); error = "" },
                label = { Text(t("ssh_port")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = LocalTextStyle.current.copy(fontFamily = monoFont()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        SshGroup(title = t("ssh_credentials")) {
            OutlinedTextField(
                username, { username = it; error = "" },
                label = { Text(t("ssh_username")) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = monoFont()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                password, { password = it },
                label = { Text(t("ssh_password")) },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        SshGroup {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("ssh_direct"), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        t("ssh_direct_sub"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(checked = direct, onCheckedChange = { direct = it })
            }
        }

        AnimatedVisibility(
            visible = error.isNotEmpty(),
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
        ) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SshFillButton(
                text = t("cancel"),
                onClick = onCancel,
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            SshFillButton(
                text = t("save"),
                onClick = {
                    val p = port.toIntOrNull() ?: 0
                    when {
                        address.isBlank() -> error = t("err_address")
                        p !in 1..65535 -> error = t("err_port")
                        username.isBlank() -> error = t("ssh_err_no_user")
                        else -> onSave(
                            (existing ?: SshHost()).copy(
                                label = label.trim(),
                                address = address.trim(),
                                port = p,
                                username = username.trim(),
                                password = password,
                                direct = direct
                            )
                        )
                    }
                },
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SshGroup(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (title != null) {
                Text(
                    mixedText(title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
internal fun SshStateDot(active: Boolean, color: Color) {
    if (!active) {
        Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        }
        return
    }
    val transition = rememberInfiniteTransition(label = "sshDot")
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
        label = "sshRipple"
    )
    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(22.dp)
                .graphicsLayer {
                    val sc = 0.40f + ripple * 0.60f
                    scaleX = sc; scaleY = sc
                    alpha = (1f - ripple) * 0.6f
                }
                .background(Brush.radialGradient(listOf(color, Color.Transparent)), CircleShape)
        )
        Box(
            Modifier
                .size(15.dp)
                .background(
                    Brush.radialGradient(listOf(color.copy(alpha = 0.40f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
    }
}

@Composable
internal fun SshFillButton(
    text: String,
    onClick: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp
) {
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    var center by remember { mutableStateOf(Offset.Zero) }
    var sz by remember { mutableStateOf(IntSize.Zero) }
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sshFillScale"
    )
    val maxR = remember(center, sz) {
        val dx = maxOf(center.x, sz.width - center.x)
        val dy = maxOf(center.y, sz.height - center.y)
        sqrt(dx * dx + dy * dy)
    }
    val radius by animateFloatAsState(
        targetValue = if (pressed) maxR else 0f,
        animationSpec = tween(durationMillis = if (pressed) 550 else 300),
        label = "sshFillRadius"
    )
    val fillFrac = if (maxR > 0f) (radius / maxR).coerceIn(0f, 1f) else 0f
    val contentColor = lerp(accent, onAccent, fillFrac)

    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(accent.copy(alpha = 0.10f))
            .border(1.5.dp, accent.copy(alpha = 0.55f), shape)
            .onSizeChanged { sz = it }
            .drawBehind {
                if (radius > 0f) {
                    drawCircle(color = accent, radius = radius, center = center)
                }
            }
            .indication(interaction, null)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    center = down.position
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .heightIn(min = minHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun SshGlassDialog(
    onDismiss: () -> Unit,
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String? = null,
    destructive: Boolean = false,
    body: @Composable ColumnScope.() -> Unit
) {
    val accent = if (destructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
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
                    color = accent
                )
                body()
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (dismissLabel != null) {
                        SshFillButton(
                            text = dismissLabel,
                            onClick = onDismiss,
                            accent = MaterialTheme.colorScheme.primary,
                            minHeight = 42.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    SshFillButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        accent = accent,
                        minHeight = 42.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
