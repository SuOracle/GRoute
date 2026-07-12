package net.gozar.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.background
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NetworkCheck
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import gozarcore.Gozarcore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URL
import java.time.LocalDate
import kotlin.math.round
import kotlin.math.sqrt

private val BrandBlue = Color(0xFF3D6AD6)
private val SplashBackground = Color(0xFF13234A)

private val GnetLightColors = lightColorScheme(
    primary = Color(0xFF2C5EE8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDAE6FF),
    onPrimaryContainer = Color(0xFF0A1E4D),
    secondary = Color(0xFF4F6796),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6ECF7),
    onSecondaryContainer = Color(0xFF1B2536),
    background = Color(0xFFF3F6FC),
    onBackground = Color(0xFF151922),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151922),
    surfaceVariant = Color(0xFFE8EDF6),
    onSurfaceVariant = Color(0xFF55617A),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFFC4CCDA)
)

private val GnetDarkColors = darkColorScheme(
    primary = Color(0xFF6CA0FF),
    onPrimary = Color(0xFF071226),
    primaryContainer = Color(0xFF263A60),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF93A7C9),
    onSecondary = Color(0xFF0E1626),
    secondaryContainer = Color(0xFF1C2740),
    onSecondaryContainer = Color(0xFFD9E2F2),
    background = Color(0xFF0E1422),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF161D2E),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF232C40),
    onSurfaceVariant = Color(0xFFA2B0C8),
    error = Color(0xFFFF7A7A),
    onError = Color(0xFF2A0A0A),
    outline = Color(0xFF38445C)
)

private val GnetAmoledColors = darkColorScheme(
    primary = Color(0xFF6CA0FF),
    onPrimary = Color(0xFF071226),
    primaryContainer = Color(0xFF1B2944),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF93A7C9),
    onSecondary = Color(0xFF0E1626),
    secondaryContainer = Color(0xFF11161F),
    onSecondaryContainer = Color(0xFFD9E2F2),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF12161F),
    onSurfaceVariant = Color(0xFFA2B0C8),
    error = Color(0xFFFF7A7A),
    onError = Color(0xFF2A0A0A),
    outline = Color(0xFF2A3344)
)

internal val LocalLang = compositionLocalOf { Lang.EN }

@Composable
private fun stringsFn(): (String) -> String {
    val lang = LocalLang.current
    return { Strings.get(lang, it) }
}


@Composable
private fun WelcomeScreen(onDone: () -> Unit) {
    val t = stringsFn()
    val welcomeFont = if (LocalLang.current == Lang.FA) VazirFont else LexendFont
    var showLogo by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showLogo = true
        delay(400)
        showTagline = true
        delay(1600)
        onDone()
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0.7f,
        animationSpec = tween(600),
        label = "logoScale"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(700),
        label = "taglineAlpha"
    )
    val taglineShift by animateFloatAsState(
        targetValue = if (showTagline) 0f else 30f,
        animationSpec = tween(700),
        label = "taglineShift"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        alpha = logoAlpha
                        scaleX = logoScale
                        scaleY = logoScale
                    }
            )

            Text(
                text = t("welcome_tagline"),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = welcomeFont,
                color = Color(0xFFEDEFF3),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(start = 8.dp, end = 8.dp)
                    .graphicsLayer {
                        alpha = taglineAlpha
                        translationY = taglineShift
                    }
            )
        }

        Text(
            text = t("welcome_dev"),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = welcomeFont,
            color = Color(0xFFAAB4C4),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}
internal val LocalHazeState = compositionLocalOf<HazeState?> { null }

object ImportBus {
    private val _pending = kotlinx.coroutines.flow.MutableStateFlow<ByteArray?>(null)
    val pending: kotlinx.coroutines.flow.StateFlow<ByteArray?> = _pending
    fun offer(bytes: ByteArray) { _pending.value = bytes }
    fun clear() { _pending.value = null }
}

class MainActivity : ComponentActivity() {

    private lateinit var store: ConfigStore
    private var afterPermission: (() -> Unit)? = null

    private val vpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) afterPermission?.invoke()
            else VpnState.setDisconnected()
        }

    private var pendingConnect: (() -> Unit)? = null

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            pendingConnect?.invoke()
            pendingConnect = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ConfigStore.get(applicationContext)
        UsageStore.init(applicationContext)
        VpnBridge.register(applicationContext)
        handleImportIntent(intent)
        lifecycleScope.launch {
            VpnState.state.collect { s ->
                if (s == Connection.DISCONNECTED) {
                    delay(500)
                    if (VpnState.state.value == Connection.DISCONNECTED) warm()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Default) {
            Gozarcore.setLogger(object : gozarcore.Logger {
                override fun log(line: String?) {
                    android.util.Log.i("XrayCore", line ?: "")
                }
            })
            withContext(Dispatchers.Main) { warm() }
        }
        lifecycleScope.launch {
            store.configs.collect { list ->
                val anyLocked = list.any { it.locked }
                if (anyLocked) {
                    window.setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SECURE,
                        android.view.WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
        setContent {
            val themeMode by store.themeMode.collectAsState()
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.AMOLED -> true
                else -> isSystemInDarkTheme()
            }
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !dark
            val lang by store.lang.collectAsState()
            val direction = if (lang == Lang.FA) LayoutDirection.Rtl else LayoutDirection.Ltr

            MaterialTheme(
                colorScheme = if (!dark) GnetLightColors
                else if (themeMode == ThemeMode.AMOLED) GnetAmoledColors
                else GnetDarkColors,
                typography = if (lang == Lang.FA) VazirTypography else LexendTypography
            ) {
                CompositionLocalProvider(
                    LocalLang provides lang,
                    LocalLayoutDirection provides direction
                ) {
                    var showWelcome by remember { mutableStateOf(true) }
                    var startMain by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(1100)
                        startMain = true
                    }
                    Box {
                        if (startMain) {
                            GozarApp(store = store, onConnect = ::connectTo, onDisconnect = ::disconnect, onSwitch = ::switchTo)
                        }
                        AnimatedVisibility(
                            visible = showWelcome,
                            exit = fadeOut(tween(400))
                        ) {
                            WelcomeScreen(onDone = { showWelcome = false })
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleImportIntent(intent)
    }

    private fun handleImportIntent(intent: Intent?) {
        intent ?: return
        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND ->
                if (android.os.Build.VERSION.SDK_INT >= 33)
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
                else @Suppress("DEPRECATION") (intent.getParcelableExtra(Intent.EXTRA_STREAM) as? android.net.Uri)
            else -> null
        } ?: return
        lifecycleScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
            }
            if (bytes != null && bytes.isNotEmpty()) ImportBus.offer(bytes)
        }
    }

    private fun connectTo(config: ProxyConfig) {
        val s = VpnState.state.value
        if (s == Connection.CONNECTING || s == Connection.CONNECTED) return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pendingConnect = { proceedConnect(config) }
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            proceedConnect(config)
        }
    }

    private fun proceedConnect(config: ProxyConfig) {
        if (VpnState.state.value == Connection.CONNECTED) return
        val json = ConfigBuilder.build(config, store.fragment.value, store.splitRouting.value, store.sniffing.value, store.sniffTypes.value)
        VpnState.setConnecting(config.id)
        val intent = VpnService.prepare(this)
        if (intent != null) { afterPermission = { startTunnel(json, config.name) }; vpnPermission.launch(intent) }
        else startTunnel(json, config.name)
    }

    private fun startTunnel(configJson: String, name: String) {
        startService(
            Intent(this, GozarVpnService::class.java)
                .putExtra(GozarVpnService.EXTRA_CONFIG, configJson)
                .putExtra(GozarVpnService.EXTRA_NAME, name)
                .putExtra(GozarVpnService.EXTRA_STOP_LABEL, Strings.get(store.lang.value, "disconnect"))
        )
    }

    private fun disconnect() {
        startService(Intent(this, GozarVpnService::class.java).setAction(GozarVpnService.ACTION_STOP))
    }

    private fun switchTo(config: ProxyConfig) {
        val s = VpnState.state.value
        if (s != Connection.CONNECTED && s != Connection.CONNECTING) { connectTo(config); return }
        lifecycleScope.launch {
            startService(Intent(this@MainActivity, GozarVpnService::class.java).setAction(GozarVpnService.ACTION_STOP))
            withTimeoutOrNull(6000) {
                VpnState.state.first { it == Connection.DISCONNECTED || it == Connection.ERROR }
            }
            delay(400)
            connectTo(config)
        }
    }

    private fun warm() {
        val s = VpnState.state.value
        if (s == Connection.CONNECTING || s == Connection.CONNECTED) return
        runCatching {
            startService(Intent(this, GozarVpnService::class.java).setAction(GozarVpnService.ACTION_WARM))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GozarApp(
    store: ConfigStore,
    onConnect: (ProxyConfig) -> Unit,
    onDisconnect: () -> Unit,
    onSwitch: (ProxyConfig) -> Unit
) {
    val t = stringsFn()
    val scope = rememberCoroutineScope()
    val themeMode by store.themeMode.collectAsState()
    val effectiveDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
        else -> isSystemInDarkTheme()
    }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val settingsScroll = rememberScrollState()

    var showPicker by remember { mutableStateOf(false) }
    var showManual by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<ProxyConfig?>(null) }
    val updateCtx = LocalContext.current
    val updateUri = LocalUriHandler.current
    var updateAvailable by remember { mutableStateOf<UpdateChecker.Result.Available?>(null) }
    LaunchedEffect(Unit) {
        if (System.currentTimeMillis() - store.lastUpdateCheck() >= 24L * 60 * 60 * 1000L) {
            val ver = runCatching {
                updateCtx.packageManager.getPackageInfo(updateCtx.packageName, 0).versionName
            }.getOrNull() ?: ""
            val r = UpdateChecker.check(ver)
            store.markUpdateChecked()
            if (r is UpdateChecker.Result.Available) updateAvailable = r
        }
    }
    updateAvailable?.let { upd ->
        AlertDialog(
            onDismissRequest = { updateAvailable = null },
            title = { Text(t("update_available").format(upd.version)) },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { updateUri.openUri(upd.url) }
                    updateAvailable = null
                }) { Text(t("update_now")) }
            },
            dismissButton = {
                TextButton(onClick = { updateAvailable = null }) { Text(t("later")) }
            }
        )
    }
    var usageDetail by remember { mutableStateOf(false) }
    var perAppDetail by remember { mutableStateOf(false) }
    var logsDetail by remember { mutableStateOf(false) }
    var stabilityDetail by remember { mutableStateOf(false) }
    var aboutDetail by remember { mutableStateOf(false) }
    var cleanIpDetail by remember { mutableStateOf(false) }
    var donationDetail by remember { mutableStateOf(false) }
    var exportConfigs by remember { mutableStateOf<List<ProxyConfig>?>(null) }
    val sortBySpeed by store.sortBySpeed.collectAsState()
    var sortExpanded by remember { mutableStateOf(false) }
    val selectedId by store.selectedId.collectAsState()
    val pings = remember { mutableStateMapOf<String, PingResult>() }

    LaunchedEffect(Unit) {
        store.awaitReady()
        store.seedDefaultSubscriptionIfNeeded()
        store.migrateDefaultSubUrlIfNeeded()
        store.defaultSubPendingFirstFetch()?.let { sub ->
            runCatching {
                val result = SubscriptionFetcher.fetchFull(sub.url)
                if (result.configs.isNotEmpty()) {
                    val info = result.userInfo
                    store.upsertSubscription(
                        sub.copy(
                            used = info?.used ?: 0,
                            total = info?.total ?: 0,
                            expire = info?.expire ?: 0,
                            lastUpdated = System.currentTimeMillis()
                        ),
                        result.configs
                    )
                }
            }
        }
        while (true) {
            SubscriptionRefresher.refreshStale(store)
            delay(30 * 60 * 1000L)
        }
    }

    val importContext = LocalContext.current
    val pendingImport by ImportBus.pending.collectAsState()
    var importNeedsPassword by remember { mutableStateOf(false) }
    var importPassword by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf("") }
    var importBusy by remember { mutableStateOf(false) }

    LaunchedEffect(pendingImport) {
        val bytes = pendingImport ?: return@LaunchedEffect
        importPassword = ""
        importError = ""
        importNeedsPassword = runCatching { ConfigFile.isPasswordProtected(bytes) }.getOrDefault(false)
        if (!importNeedsPassword) {
            val configs = withContext(Dispatchers.Default) {
                runCatching { ConfigFile.decode(importContext, bytes, null) }.getOrNull()
            }
            if (configs != null) {
                val n = store.addImported(configs)
                android.widget.Toast.makeText(importContext, t("import_success").format(n), android.widget.Toast.LENGTH_SHORT).show()
                ImportBus.clear()
            } else {
                importNeedsPassword = true
            }
        }
    }

    if (pendingImport != null && importNeedsPassword) {
        AlertDialog(
            onDismissRequest = { if (!importBusy) { ImportBus.clear() } },
            title = { Text(t("import_title")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("import_needs_password"), style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        importPassword, { importPassword = it; importError = "" },
                        label = { Text(t("import_password")) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (importError.isNotEmpty())
                        Text(importError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !importBusy && importPassword.isNotEmpty(),
                    onClick = {
                        val bytes = pendingImport ?: return@TextButton
                        importBusy = true
                        scope.launch {
                            val configs = withContext(Dispatchers.Default) {
                                runCatching { ConfigFile.decode(importContext, bytes, importPassword) }
                            }
                            importBusy = false
                            configs.onSuccess { list ->
                                val n = store.addImported(list)
                                android.widget.Toast.makeText(importContext, t("import_success").format(n), android.widget.Toast.LENGTH_SHORT).show()
                                ImportBus.clear()
                                importNeedsPassword = false
                            }.onFailure { e ->
                                importError = when (e) {
                                    is ConfigFile.WrongPassword -> t("import_wrong_password")
                                    is ConfigFile.ForeignApp -> t("import_foreign_app")
                                    else -> t("import_bad_file")
                                }
                            }
                        }
                    }
                ) { Text(t("import_button")) }
            },
            dismissButton = {
                TextButton(onClick = { if (!importBusy) ImportBus.clear() }) { Text(t("cancel")) }
            }
        )
    }

    val page = pagerState.currentPage
    val onSettingsTab = page == 1
    val subScreenOpen = (page == 0 && (showPicker || showManual || exportConfigs != null)) || (onSettingsTab && (usageDetail || perAppDetail || logsDetail || stabilityDetail || aboutDetail || cleanIpDetail || donationDetail))

    val screenKey = when {
        page == 0 && exportConfigs != null -> "export"
        page == 0 && showManual -> "manual"
        page == 0 && showPicker -> "picker"
        page == 0 -> "connection"
        onSettingsTab && usageDetail -> "usage"
        onSettingsTab && perAppDetail -> "perapp"
        onSettingsTab && logsDetail -> "logs"
        onSettingsTab && stabilityDetail -> "stability"
        onSettingsTab && aboutDetail -> "about"
        onSettingsTab && cleanIpDetail -> "cleanip"
        onSettingsTab && donationDetail -> "donation"
        else -> "settings"
    }

    fun pop() {
        when {
            exportConfigs != null -> exportConfigs = null
            showManual -> { showManual = false; editingConfig = null }
            showPicker -> showPicker = false
            usageDetail -> usageDetail = false
            perAppDetail -> perAppDetail = false
            logsDetail -> logsDetail = false
            stabilityDetail -> stabilityDetail = false
            aboutDetail -> aboutDetail = false
            cleanIpDetail -> cleanIpDetail = false
            donationDetail -> donationDetail = false
            onSettingsTab -> scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    val canGoBack = subScreenOpen || onSettingsTab
    var backProgress by remember { mutableStateOf(0f) }

    PredictiveBackHandler(enabled = canGoBack) { progress ->
        try {
            progress.collect { event -> backProgress = event.progress }
            backProgress = 0f
            pop()
        } catch (e: CancellationException) {
            backProgress = 0f
        }
    }

    val contentScale = 1f - backProgress * 0.08f
    val contentAlpha = 1f - backProgress * 0.25f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (screenKey == "connection") {
                        val logoRes = if (effectiveDark) R.drawable.logo else R.drawable.logo_black
                        Box {
                            Image(
                                painter = painterResource(logoRes),
                                contentDescription = null,
                                modifier = Modifier.height(34.dp)
                            )
                            Image(
                                painter = painterResource(logoRes),
                                contentDescription = t("app_title"),
                                modifier = Modifier.height(34.dp)
                            )
                        }
                    } else {
                        Text(
                            when (screenKey) {
                                "manual" -> if (editingConfig != null) t("edit_config_title") else t("add_config_title")
                                "export" -> t("export_title")
                                "picker" -> t("choose_server")
                                "usage" -> t("data_usage")
                                "perapp" -> t("per_app")
                                "logs" -> t("xray_logs")
                                "stability" -> t("stab_title")
                                "about" -> t("about")
                                "cleanip" -> t("scan_title")
                                "donation" -> if (LocalLang.current == Lang.FA) "حمایت از ما" else "Support Us"
                                else -> t("settings")
                            }
                        )
                    }
                },
                navigationIcon = {
                    when (screenKey) {
                        "manual" -> BounceIconButton(onClick = { showManual = false; editingConfig = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "export" -> BounceIconButton(onClick = { exportConfigs = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "picker" -> BounceIconButton(onClick = { showPicker = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "usage" -> BounceIconButton(onClick = { usageDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "perapp" -> BounceIconButton(onClick = { perAppDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "logs" -> BounceIconButton(onClick = { logsDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "stability" -> BounceIconButton(onClick = { stabilityDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "about" -> BounceIconButton(onClick = { aboutDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "cleanip" -> BounceIconButton(onClick = { cleanIpDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "donation" -> BounceIconButton(onClick = { donationDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                },
                actions = {
                    if (screenKey == "picker") {
                        BounceIconButton(onClick = { sortExpanded = true }) {
                            Icon(Icons.Filled.SwapVert, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text((if (!sortBySpeed) "✓ " else "") + t("default_order")) },
                                onClick = { store.setSortBySpeed(false); sortExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text((if (sortBySpeed) "✓ " else "") + t("fastest_first")) },
                                onClick = { store.setSortBySpeed(true); sortExpanded = false }
                            )
                        }
                    }
                    BounceIconButton(onClick = {
                        store.setThemeMode(when (themeMode) {
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.AMOLED
                            ThemeMode.AMOLED -> ThemeMode.LIGHT
                            else -> if (effectiveDark) ThemeMode.LIGHT else ThemeMode.DARK
                        })
                    }) {
                        Icon(
                            when (themeMode) {
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                ThemeMode.AMOLED -> Icons.Filled.Contrast
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                                else -> if (effectiveDark) Icons.Filled.DarkMode else Icons.Filled.LightMode
                            },
                            contentDescription = "Toggle theme"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = page == 0,
                    onClick = {
                        showPicker = false; showManual = false; editingConfig = null
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    icon = { Icon(Icons.Filled.Bolt, contentDescription = null) },
                    label = { Text(t("connection")) }
                )
                NavigationBarItem(
                    selected = page == 1,
                    onClick = {
                        usageDetail = false
                        perAppDetail = false
                        logsDetail = false
                        stabilityDetail = false
                        aboutDetail = false
                        cleanIpDetail = false
                        donationDetail = false
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(t("settings")) }
                )
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !subScreenOpen,
            modifier = Modifier
                .padding(padding)
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                    alpha = contentAlpha
                }
        ) { p ->
            if (p == 0) {
                val connKey = when {
                    exportConfigs != null -> "export"
                    showManual -> "manual"
                    showPicker -> "picker"
                    else -> "connection"
                }
                AnimatedContent(
                    targetState = connKey,
                    transitionSpec = {
                        (scaleIn(tween(220), initialScale = 0.92f) + fadeIn(tween(220))) togetherWith
                                (scaleOut(tween(180), targetScale = 0.92f) + fadeOut(tween(180)))
                    },
                    label = "connTab"
                ) { key ->
                    when (key) {
                        "export" -> ExportConfigScreen(
                            configs = exportConfigs ?: emptyList(),
                            onCancel = { exportConfigs = null }
                        )
                        "manual" -> ManualConfigScreen(
                            existing = editingConfig,
                            onSave = { cfg ->
                                if (editingConfig != null) store.update(cfg) else store.add(cfg)
                                showManual = false; editingConfig = null
                            },
                            onCancel = { showManual = false; editingConfig = null }
                        )
                        "picker" -> ConfigPickerScreen(
                            store = store,
                            selectedId = selectedId,
                            sortBySpeed = sortBySpeed,
                            pings = pings,
                            onSelect = { id ->
                                store.setSelectedId(id)
                                showPicker = false
                                val st = VpnState.state.value
                                if ((st == Connection.CONNECTED || st == Connection.CONNECTING) && id != VpnState.activeId.value) {
                                    store.configs.value.find { c -> c.id == id }?.let(onSwitch)
                                }
                            },
                            onEdit = { editingConfig = it; showManual = true },
                            onAddManually = { showManual = true },
                            onShareFile = { exportConfigs = it }
                        )
                        else -> ConnectionScreen(
                            store = store,
                            selectedId = selectedId,
                            onOpenPicker = { showPicker = true },
                            onConnect = onConnect,
                            onDisconnect = onDisconnect
                        )
                    }
                }
            } else {
                val setKey = when {
                    usageDetail -> "usage"
                    perAppDetail -> "perapp"
                    logsDetail -> "logs"
                    stabilityDetail -> "stability"
                    aboutDetail -> "about"
                    cleanIpDetail -> "cleanip"
                    donationDetail -> "donation"
                    else -> "settings"
                }
                AnimatedContent(
                    targetState = setKey,
                    transitionSpec = {
                        if (targetState == "usage" || targetState == "perapp" || targetState == "logs" || targetState == "stability" || targetState == "about" || targetState == "cleanip" || targetState == "donation") {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) togetherWith
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250))
                        } else {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) togetherWith
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250))
                        }
                    },
                    label = "setTab"
                ) { key ->
                    when (key) {
                        "usage" -> DataUsageScreen()
                        "perapp" -> AppProxyScreen(store = store)
                        "logs" -> LogsScreen(store = store)
                        "stability" -> StabilityTestScreen(store = store)
                        "about" -> AboutScreen()
                        "cleanip" -> CleanIpScreen()
                        "donation" -> DonationScreen()
                        else -> SettingsScreen(
                            store = store,
                            scrollState = settingsScroll,
                            onOpenUsage = { usageDetail = true },
                            onOpenPerApp = { perAppDetail = true },
                            onOpenLogs = { logsDetail = true },
                            onOpenStability = { stabilityDetail = true },
                            onOpenAbout = { aboutDetail = true },
                            onOpenCleanIp = { cleanIpDetail = true },
                            onOpenDonation = { donationDetail = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionScreen(
    store: ConfigStore,
    selectedId: String?,
    onOpenPicker: () -> Unit,
    onConnect: (ProxyConfig) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val n: (String) -> String = { localizeDigits(it, lang) }
    val configs by store.configs.collectAsState()
    val conn by VpnState.state.collectAsState()
    val error by VpnState.error.collectAsState()
    val scope = rememberCoroutineScope()

    var totalUp by remember { mutableStateOf(0L) }
    var totalDown by remember { mutableStateOf(0L) }
    var upSpeed by remember { mutableStateOf(0L) }
    var downSpeed by remember { mutableStateOf(0L) }
    var delayResult by remember { mutableStateOf<String?>(null) }
    var delayRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        VpnBridge.counters.collect { c ->
            totalUp = c.totalUp; totalDown = c.totalDown
            upSpeed = c.upSpeed; downSpeed = c.downSpeed
        }
    }
    LaunchedEffect(conn) {
        if (conn != Connection.CONNECTED) delayResult = null
    }

    val selectedConfig = configs.find { it.id == selectedId }
    val connected = conn == Connection.CONNECTED || conn == Connection.CONNECTING

    val hazeState = remember { HazeState() }
    Box(modifier.fillMaxSize()) {
        ParticleField(Modifier.fillMaxSize().hazeSource(hazeState))
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenPicker() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            if (selectedConfig != null) {
                                Text(t("selected_server"), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedConfig.name, style = MaterialTheme.typography.titleMedium)
                                if (selectedConfig.locked) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Lock, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(t("locked_config"), style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Text(n("${selectedConfig.address}:${selectedConfig.port}"), style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                Text(t("tap_choose"), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }

                var btnPressed by remember { mutableStateOf(false) }
                val glowActive = !connected && selectedConfig != null && !btnPressed
                val glowAlpha by animateFloatAsState(
                    targetValue = if (glowActive) 1f else 0f,
                    animationSpec = tween(300),
                    label = "glowAlpha"
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                btnPressed = true
                                waitForUpOrCancellation()
                                btnPressed = false
                            }
                        }
                ) {
                    BounceButton(
                        onClick = {
                            if (connected) onDisconnect()
                            else selectedConfig?.let { onConnect(it) }
                        },
                        enabled = connected || selectedConfig != null,
                        modifier = Modifier.matchParentSize()
                    ) {
                        Text(
                            when {
                                conn == Connection.CONNECTING -> t("connecting_cancel")
                                connected -> t("disconnect")
                                else -> t("connect")
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (selectedConfig != null && glowAlpha > 0.001f) {
                        ConnectGlow(
                            color = MaterialTheme.colorScheme.primary,
                            alpha = glowAlpha,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }

                AnimatedVisibility(
                    visible = conn == Connection.CONNECTED,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatBox(
                            speed = downSpeed,
                            total = totalDown,
                            icon = Icons.Filled.ArrowDownward,
                            color = Color(0xFF35E0FF),
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            speed = upSpeed,
                            total = totalUp,
                            icon = Icons.Filled.ArrowUpward,
                            color = Color(0xFFFF4DD8),
                            modifier = Modifier.weight(1f)
                        )
                        BounceOutlinedButton(
                            onClick = {
                                delayRunning = true; delayResult = null
                                scope.launch {
                                    val ms = SpeedTest.delay()
                                    delayResult = if (ms != null) "${localizeDigits("$ms", lang)} ${t("unit_ms")}" else t("delay_failed")
                                    delayRunning = false
                                }
                            },
                            enabled = !delayRunning,
                            minHeight = 44.dp,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            when {
                                delayRunning -> Text("…", style = MaterialTheme.typography.labelLarge)
                                delayResult != null -> Text(delayResult!!, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                else -> Icon(Icons.Filled.NetworkCheck, contentDescription = t("real_delay"), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                EarthSection(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigPickerScreen(
    store: ConfigStore,
    selectedId: String?,
    sortBySpeed: Boolean,
    pings: SnapshotStateMap<String, PingResult>,
    onSelect: (String) -> Unit,
    onEdit: (ProxyConfig) -> Unit,
    onAddManually: () -> Unit,
    onShareFile: (List<ProxyConfig>) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val n: (String) -> String = { localizeDigits(it, lang) }
    val configs by store.configs.collectAsState()
    val subscriptions by store.subscriptions.collectAsState()
    val activeId by VpnState.activeId.collectAsState()
    val clipboard = LocalClipboardManager.current
    val pickerContext = LocalContext.current
    val pickerScope = rememberCoroutineScope()
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pickerScope.launch {
                val bytes: ByteArray? = withContext(Dispatchers.IO) {
                    runCatching {
                        pickerContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                }
                if (bytes != null && bytes.isNotEmpty()) ImportBus.offer(bytes)
            }
        }
    }

    var link by remember { mutableStateOf("") }
    var subStatus by remember { mutableStateOf("") }
    var addBusy by remember { mutableStateOf(false) }
    var addDone by remember { mutableStateOf("") }
    var testAllState by remember { mutableStateOf(0) }
    var addMenu by remember { mutableStateOf(false) }
    val expandedSubs by store.expandedSubs.collectAsState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val selected = remember { mutableStateMapOf<String, Boolean>() }
    var selectionMode by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val painting = remember { booleanArrayOf(false) }
    val paintSelect = remember { booleanArrayOf(true) }
    val anchorIdx = remember { intArrayOf(-1) }
    val lastIdx = remember { intArrayOf(-1) }
    val orderedSnapshot = remember { mutableListOf<String>() }
    val base = remember { hashSetOf<String>() }
    var viewportH by remember { mutableStateOf(0) }
    var dragging by remember { mutableStateOf(false) }
    var dragY by remember { mutableStateOf<Float?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val allIds = remember(configs) { configs.map { it.id }.toSet() }

    fun sortMaybe(list: List<ProxyConfig>): List<ProxyConfig> =
        if (sortBySpeed) list.sortedBy { pingRank(pings[it.id]) } else list
    val pingSortKey = if (sortBySpeed) {
        remember(configs, pings.toList()) {
            configs.joinToString(",") { "${it.id}:${pingRank(pings[it.id])}" }
        }
    } else 0
    val grouped = remember(configs, subscriptions, sortBySpeed, pingSortKey) {
        subscriptions.map { sub -> sub to sortMaybe(configs.filter { it.subId == sub.id }) }
    }
    val loose = remember(configs, sortBySpeed, pingSortKey) {
        sortMaybe(configs.filter { it.subId.isEmpty() })
    }
    fun displayedOrder(): List<String> = buildList {
        grouped.forEach { (sub, cfgs) -> if (sub.id in expandedSubs) cfgs.forEach { add(it.id) } }
        loose.forEach { add(it.id) }
    }

    fun idAt(y: Float): String? {
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            y >= it.offset && y < it.offset + it.size
        } ?: return null
        val key = item.key as? String ?: return null
        return if (key in allIds) key else null
    }
    fun toggle(id: String) {
        if (selected.remove(id) == null) selected[id] = true
        selectionMode = selected.isNotEmpty()
    }
    fun applyRange(currentIdx: Int) {
        if (currentIdx < 0 || anchorIdx[0] < 0) return
        val lo = minOf(anchorIdx[0], currentIdx)
        val hi = maxOf(anchorIdx[0], currentIdx)
        orderedSnapshot.forEachIndexed { i, id ->
            val want = if (i in lo..hi) paintSelect[0] else (id in base)
            val have = selected.containsKey(id)
            if (want && !have) selected[id] = true
            else if (!want && have) selected.remove(id)
        }
    }
    fun beginPaint(id: String) {
        orderedSnapshot.clear(); orderedSnapshot.addAll(displayedOrder())
        base.clear(); base.addAll(selected.keys)
        anchorIdx[0] = orderedSnapshot.indexOf(id)
        lastIdx[0] = anchorIdx[0]
        paintSelect[0] = !(id in base)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        applyRange(anchorIdx[0])
        painting[0] = true
        dragging = true
    }
    fun paintAt(id: String?) {
        if (id == null) return
        val idx = orderedSnapshot.indexOf(id)
        if (idx < 0 || idx == lastIdx[0]) return
        lastIdx[0] = idx
        applyRange(idx)
    }
    fun endPaint() {
        painting[0] = false
        dragging = false
        dragY = null
        anchorIdx[0] = -1
        lastIdx[0] = -1
        selectionMode = selected.isNotEmpty()
    }
    fun clearSel() { selected.clear(); selectionMode = false }

    BackHandler(enabled = selectionMode) { clearSel() }

    LaunchedEffect(dragging) {
        while (dragging) {
            val y = dragY
            if (y != null && viewportH > 0) {
                val delta = when {
                    y < 72f -> -14f
                    y > viewportH - 72f -> 14f
                    else -> 0f
                }
                if (delta != 0f) {
                    listState.scrollBy(delta)
                    paintAt(idAt(y))
                }
            }
            delay(16)
        }
    }

    LaunchedEffect(subStatus) {
        if (subStatus.isNotEmpty()) { delay(3000); subStatus = "" }
    }
    LaunchedEffect(addDone) { if (addDone.isNotEmpty()) { delay(3000); addDone = "" } }
    LaunchedEffect(testAllState) { if (testAllState == 2) { delay(2500); testAllState = 0 } }

    fun doAdd() {
        val text = link.trim()
        when {
            text.isEmpty() -> {}
            (text.startsWith("http://") || text.startsWith("https://")) && !text.contains('\n') -> {
                addBusy = true; addDone = ""
                scope.launch {
                    try {
                        val result = SubscriptionFetcher.fetchFull(text)
                        if (result.configs.isEmpty()) {
                            addDone = t("no_configs")
                        } else {
                            val name = runCatching { URL(text).host }.getOrDefault("Subscription")
                            val info = result.userInfo
                            store.upsertSubscription(
                                Subscription(
                                    name = name, url = text,
                                    used = info?.used ?: 0,
                                    total = info?.total ?: 0,
                                    expire = info?.expire ?: 0,
                                    lastUpdated = System.currentTimeMillis()
                                ),
                                result.configs
                            )
                            addDone = n(t("added_sub").format(result.configs.size))
                            link = ""
                        }
                    } catch (e: Exception) {
                        addDone = t("fetch_failed")
                    } finally {
                        addBusy = false
                    }
                }
            }
            else -> {
                val lines = text.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }
                val parsed = lines.mapNotNull { ConfigParser.parse(it) }
                if (parsed.isEmpty()) {
                    addDone = t("parse_none")
                } else {
                    parsed.forEach { store.add(it) }
                    addDone = n(t("added_configs").format(parsed.size))
                    link = ""
                }
            }
        }
    }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = selectionMode,
            enter = expandVertically(tween(220)) + fadeIn(tween(220)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
        ) {
            SelectionActionBar(
                count = selected.size,
                onClose = { clearSel() },
                onCopy = {
                    val text = configs.filter { selected.containsKey(it.id) }
                        .joinToString("\n") { ConfigShare.toLink(it) }
                    clipboard.setText(AnnotatedString(text))
                    android.widget.Toast.makeText(context, t("copied"), android.widget.Toast.LENGTH_SHORT).show()
                },
                onShareApp = {
                    val text = configs.filter { selected.containsKey(it.id) }
                        .joinToString("\n") { ConfigShare.toLink(it) }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, t("share")))
                },
                onShareFile = {
                    onShareFile(configs.filter { selected.containsKey(it.id) })
                    clearSel()
                },
                onDelete = { confirmDelete = true }
            )
        }
        if (!selectionMode) {
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text(t("paste_links")) },
                minLines = 1,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BounceButton(
                    onClick = { if (!addBusy) doAdd() },
                    enabled = !addBusy,
                    modifier = Modifier.weight(1f)
                ) { Text(when {
                    addBusy -> t("adding")
                    addDone.isNotEmpty() -> addDone
                    else -> t("add")
                }, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }

                Box {
                    BounceOutlinedButton(onClick = { addMenu = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add options")
                    }
                    DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                        CompactMenuItem(Icons.Filled.ContentPaste, t("paste_clipboard")) {
                            addMenu = false
                            val clip = clipboard.getText()?.text
                            if (clip.isNullOrBlank()) subStatus = t("clipboard_empty")
                            else { link = clip; subStatus = t("pasted") }
                        }
                        CompactMenuItem(Icons.Filled.Add, t("add_manually")) {
                            addMenu = false; onAddManually()
                        }
                        CompactMenuItem(Icons.Filled.UploadFile, t("import_button")) {
                            addMenu = false
                            filePicker.launch(arrayOf("*/*"))
                        }
                        CompactMenuItem(Icons.Filled.Bolt, t("add_warp")) {
                            addMenu = false
                            if (!addBusy) {
                                addBusy = true; addDone = ""
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) { Warp.register() }
                                    addDone = when (result) {
                                        is Warp.Result.Success -> { result.configs.forEach { store.add(it) }; t("warp_added") }
                                        is Warp.Result.Failure -> t("warp_failed")
                                    }
                                    addBusy = false
                                }
                            }
                        }
                    }
                }
            }

            BounceOutlinedButton(
                onClick = {
                    val snapshot = configs
                    if (testAllState != 1 && snapshot.isNotEmpty()) {
                        snapshot.forEach { pings[it.id] = PingResult.Testing }
                        testAllState = 1
                        scope.launch {
                            val sem = Semaphore(4)
                            val jobs = snapshot.map { cfg ->
                                launch {
                                    sem.withPermit {
                                        val ms = withContext(Dispatchers.IO) {
                                            Gozarcore.measureDelay(ConfigBuilder.buildForTest(cfg))
                                        }
                                        pings[cfg.id] = if (ms >= 0) PingResult.Ok(ms.toInt()) else PingResult.Failed
                                    }
                                }
                            }
                            jobs.joinAll()
                            testAllState = 2
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(when (testAllState) {
                1 -> t("testing")
                2 -> t("test_completed")
                else -> t("test_all")
            }) }

            if (subStatus.isNotEmpty())
                Text(subStatus, style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
                .onSizeChanged { viewportH = it.height }
                .pointerInput(allIds) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var painted = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val c = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!c.pressed) break
                            if (painting[0]) {
                                painted = true
                                c.consume()
                                dragY = c.position.y
                                paintAt(idAt(c.position.y))
                            }
                        }
                        if (painting[0] || painted) endPaint()
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            grouped.forEach { (sub, subConfigs) ->
                item(key = "sub-${sub.id}") {
                    SubscriptionHeader(
                        sub = sub,
                        isOpen = sub.id in expandedSubs,
                        onToggle = { store.toggleSubExpanded(sub.id) },
                        onRefresh = {
                            subStatus = t("fetching_sub")
                            scope.launch {
                                try {
                                    val result = SubscriptionFetcher.fetchFull(sub.url)
                                    val info = result.userInfo
                                    store.upsertSubscription(
                                        sub.copy(
                                            used = info?.used ?: sub.used,
                                            total = info?.total ?: sub.total,
                                            expire = info?.expire ?: sub.expire,
                                            lastUpdated = System.currentTimeMillis()
                                        ),
                                        result.configs
                                    )
                                    subStatus = n("${sub.name}: ${result.configs.size}")
                                } catch (e: Exception) {
                                    subStatus = "${t("fetch_failed")}: ${e.message ?: ""}"
                                }
                            }
                        },
                        onRename = { newName -> store.renameSubscription(sub.id, newName) },
                        onRemove = { store.deleteSubscription(sub.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                if (sub.id in expandedSubs) {
                    items(subConfigs, key = { it.id }) { cfg ->
                        ConfigRow(
                            config = cfg,
                            isSelected = cfg.id == selectedId,
                            isActive = cfg.id == activeId,
                            ping = pings[cfg.id],
                            selectionMode = selectionMode,
                            isChecked = { selected.containsKey(cfg.id) },
                            onClick = { if (selectionMode) toggle(cfg.id) else onSelect(cfg.id) },
                            onLongPress = { beginPaint(cfg.id) },
                            onEdit = { onEdit(cfg) },
                            onDelete = { store.delete(cfg.id); pings.remove(cfg.id) },
                            onShareFile = { onShareFile(listOf(cfg)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            if (loose.isNotEmpty()) {
                item(key = "loose-header") {
                    Text(
                        n("${t("manual_configs")} (${loose.size})"),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp).animateItem()
                    )
                }
                items(loose, key = { it.id }) { cfg ->
                    ConfigRow(
                        config = cfg,
                        isSelected = cfg.id == selectedId,
                        isActive = cfg.id == activeId,
                        ping = pings[cfg.id],
                        selectionMode = selectionMode,
                        isChecked = { selected.containsKey(cfg.id) },
                        onClick = { if (selectionMode) toggle(cfg.id) else onSelect(cfg.id) },
                        onLongPress = { beginPaint(cfg.id) },
                        onEdit = { onEdit(cfg) },
                        onDelete = { store.delete(cfg.id); pings.remove(cfg.id) },
                        onShareFile = { onShareFile(listOf(cfg)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(t("delete")) },
            text = { Text(t("delete_selected_q")) },
            confirmButton = {
                TextButton(onClick = {
                    configs.filter { selected.containsKey(it.id) }
                        .forEach { store.delete(it.id); pings.remove(it.id) }
                    clearSel()
                    confirmDelete = false
                }) { Text(t("delete")) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(t("cancel")) }
            }
        )
    }
}

@Composable
private fun ExportConfigScreen(
    configs: List<ProxyConfig>,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val multi = configs.size > 1

    val defaultName = if (multi) "GRoute-configs" else (configs.firstOrNull()?.name?.ifBlank { "config" } ?: "config")
    var fileName by remember { mutableStateOf(defaultName) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Lock, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    t("export_encrypted_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (multi) {
            Text(
                t("export_count").format(configs.size),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                fileName,
                { fileName = it },
                label = { Text(t("export_file_name")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Text(
                        ".grt",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 14.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                password,
                { password = it },
                label = { Text(t("export_password")) },
                placeholder = { Text(t("export_password_hint"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showPassword = !showPassword }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = showPassword, onCheckedChange = { showPassword = it })
                Text(t("show"), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.InsertDriveFile, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).padding(top = 2.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                t("export_locked_note"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        if (error.isNotEmpty())
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(2.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BounceOutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(t("cancel")) }
            BounceButton(
                onClick = {
                    if (busy) return@BounceButton
                    busy = true
                    error = ""
                    scope.launch {
                        val result = runCatching {
                            withContext(Dispatchers.Default) {
                                val bytes = ConfigFile.encode(context, configs, password.ifBlank { null })
                                ConfigFile.writeToCache(context, fileName, bytes)
                            }
                        }
                        busy = false
                        result.onSuccess { file ->
                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", file
                            )
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = ConfigFile.MIME
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(send, t("export_continue")))
                            onCancel()
                        }.onFailure {
                            error = t("import_bad_file")
                        }
                    }
                },
                enabled = !busy && fileName.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    t("export_continue"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ManualConfigScreen(
    existing: ProxyConfig? = null,
    onSave: (ProxyConfig) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    var name by remember { mutableStateOf(existing?.name ?: "") }

    if (existing?.locked == true) {
        Column(
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(t("locked_config"), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                t("locked_note"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                name, { name = it },
                label = { Text(t("name_optional")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BounceOutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(t("cancel")) }
                BounceButton(
                    onClick = { onSave(existing.copy(name = name.ifBlank { existing.name })) },
                    modifier = Modifier.weight(1f)
                ) { Text(t("save")) }
            }
        }
        return
    }

    var protocol by remember { mutableStateOf(existing?.protocol ?: "vless") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var port by remember { mutableStateOf(existing?.port?.takeIf { it > 0 }?.toString() ?: "") }
    var uuid by remember { mutableStateOf(existing?.uuid ?: "") }
    var password by remember { mutableStateOf(existing?.password ?: "") }
    var method by remember { mutableStateOf(existing?.method?.ifEmpty { "aes-256-gcm" } ?: "aes-256-gcm") }
    var flow by remember { mutableStateOf(existing?.flow ?: "") }
    var network by remember { mutableStateOf(existing?.network ?: "tcp") }
    var security by remember { mutableStateOf(existing?.security ?: "none") }
    var sni by remember { mutableStateOf(existing?.sni ?: "") }
    var publicKey by remember { mutableStateOf(existing?.publicKey ?: "") }
    var shortId by remember { mutableStateOf(existing?.shortId ?: "") }
    var path by remember { mutableStateOf(existing?.path ?: "") }
    var host by remember { mutableStateOf(existing?.host ?: "") }
    var serviceName by remember { mutableStateOf(existing?.serviceName ?: "") }
    var mode by remember { mutableStateOf(existing?.mode ?: "") }
    var alpn by remember { mutableStateOf(existing?.alpn ?: "") }
    var fingerprint by remember { mutableStateOf(existing?.fingerprint ?: "chrome") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(name, { name = it }, label = { Text(t("name_optional")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        LabeledDropdown(t("protocol"), listOf("vless", "vmess", "trojan", "shadowsocks"), protocol) { protocol = it }
        OutlinedTextField(address, { address = it }, label = { Text(t("address")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            port, { port = it.filter { c -> c.isDigit() } },
            label = { Text(t("port")) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (protocol == "vless" || protocol == "vmess")
            OutlinedTextField(uuid, { uuid = it }, label = { Text(t("uuid")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        if (protocol == "trojan" || protocol == "shadowsocks")
            OutlinedTextField(password, { password = it }, label = { Text(t("password")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        if (protocol == "shadowsocks")
            LabeledDropdown(t("enc_method"),
                listOf("aes-256-gcm", "aes-128-gcm", "chacha20-ietf-poly1305", "2022-blake3-aes-256-gcm"), method) { method = it }
        if (protocol == "vless")
            OutlinedTextField(flow, { flow = it }, label = { Text(t("flow_optional")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())

        if (protocol != "shadowsocks") {
            LabeledDropdown(t("network"), listOf("tcp", "ws", "grpc", "http", "httpupgrade", "xhttp"), network) { network = it }
            LabeledDropdown(t("security"), listOf("none", "tls", "reality"), security) { security = it }
            if (security == "tls" || security == "reality") {
                OutlinedTextField(sni, { sni = it }, label = { Text(t("sni")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                LabeledDropdown(t("fingerprint"), listOf("chrome", "firefox", "safari", "ios", "android", "edge", "random"), fingerprint.ifEmpty { "chrome" }) { fingerprint = it }
            }
            if (security == "tls")
                OutlinedTextField(alpn, { alpn = it }, label = { Text(t("alpn")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            if (security == "reality") {
                OutlinedTextField(publicKey, { publicKey = it }, label = { Text(t("public_key")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(shortId, { shortId = it }, label = { Text(t("short_id")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            }
            if (network == "ws" || network == "httpupgrade" || network == "http" || network == "xhttp") {
                OutlinedTextField(path, { path = it }, label = { Text(t("ws_path")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(host, { host = it }, label = { Text(t("ws_host")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            }
            if (network == "xhttp")
                LabeledDropdown(t("mode"), listOf("auto", "packet-up", "stream-up", "stream-one"), mode.ifEmpty { "auto" }) { mode = it }
            if (network == "grpc") {
                OutlinedTextField(serviceName, { serviceName = it }, label = { Text(t("service_name")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                LabeledDropdown(t("mode"), listOf("gun", "multi"), mode.ifEmpty { "gun" }) { mode = it }
            }
        }

        if (error.isNotEmpty())
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BounceOutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(t("cancel")) }
            BounceButton(
                onClick = {
                    val p = port.toIntOrNull()
                    when {
                        address.isBlank() -> error = t("err_address")
                        p == null || p !in 1..65535 -> error = t("err_port")
                        (protocol == "vless" || protocol == "vmess") && uuid.isBlank() -> error = t("err_uuid")
                        (protocol == "trojan" || protocol == "shadowsocks") && password.isBlank() -> error = t("err_password")
                        else -> onSave(
                            (existing ?: ProxyConfig(name = "", protocol = "", address = "", port = 0)).copy(
                                name = name.ifBlank { "$protocol $address" },
                                protocol = protocol,
                                address = address.trim(),
                                port = p,
                                uuid = uuid.trim(),
                                password = password.trim(),
                                method = method.trim(),
                                encryption = if (protocol == "vmess") "auto" else "none",
                                flow = flow.trim(),
                                network = if (protocol == "shadowsocks") "tcp" else network,
                                security = if (protocol == "shadowsocks") "none" else security,
                                sni = sni.trim(),
                                publicKey = publicKey.trim(),
                                shortId = shortId.trim(),
                                path = path.trim(),
                                host = host.trim(),
                                serviceName = serviceName.trim(),
                                mode = mode.trim(),
                                alpn = alpn.trim(),
                                fingerprint = fingerprint.trim()
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text(t("save")) }
        }
    }
}

@Composable
private fun CompactMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Modifier.appearOnce(): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(320), label = "appearA")
    val ty by animateFloatAsState(if (shown) 0f else 26f, tween(320), label = "appearY")
    return this.graphicsLayer { alpha = a; translationY = ty }
}

@Composable
private fun LabeledDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        BounceOutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); open = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    store: ConfigStore,
    scrollState: ScrollState,
    onOpenUsage: () -> Unit,
    onOpenPerApp: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenStability: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenCleanIp: () -> Unit,
    onOpenDonation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val fragment by store.fragment.collectAsState()
    val splitRouting by store.splitRouting.collectAsState()
    val sniffing by store.sniffing.collectAsState()
    val sniffTypes by store.sniffTypes.collectAsState()
    val usage by UsageStore.usage.collectAsState()
    val allTime = remember(usage) { UsageStore.totalAll(usage) }
    val curLang by store.lang.collectAsState()
    var langOpen by remember { mutableStateOf(false) }
    val autoRefreshHours by store.autoRefreshHours.collectAsState()
    var autoRefreshOpen by remember { mutableStateOf(false) }
    val autoSelect by store.autoSelect.collectAsState()

    fun refreshLabel(h: Int): String =
        if (h <= 0) t("auto_refresh_off")
        else if (h == 1) t("every_hour").format(localizeDigits("$h", lang))
        else t("every_hours").format(localizeDigits("$h", lang))

    Column(
        modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(t("data_usage"), style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenUsage() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(t("all_time_total"), style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatBytes(allTime[0] + allTime[1], lang),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(t("tap_ranges"), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenStability() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("stab_title"), style = MaterialTheme.typography.bodyLarge)
                    Text(t("stab_sub"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Text(t("routing"), style = MaterialTheme.typography.titleMedium)
        SettingRow(
            title = if (lang == Lang.FA) "انتخاب خودکار سریع‌ترین سرور" else "Auto-select fastest server",
            subtitle = if (lang == Lang.FA) "هر ۶۰ ثانیه همه‌ی سرورها تست و سریع‌ترین انتخاب می‌شود" else "Pings all servers every 60s and switches to the lowest",
            checked = autoSelect,
            onCheckedChange = { store.setAutoSelect(it) }
        )
        SettingRow(
            title = t("split_title"),
            subtitle = t("split_sub"),
            checked = splitRouting,
            onCheckedChange = { store.setSplitRouting(it) }
        )
        SettingRow(
            title = t("fragment_title"),
            subtitle = t("fragment_sub"),
            checked = fragment,
            onCheckedChange = { store.setFragment(it) }
        )
        SettingRow(
            title = t("sniffing_title"),
            subtitle = t("sniffing_sub"),
            checked = sniffing,
            onCheckedChange = { store.setSniffing(it) }
        )
        AnimatedVisibility(visible = sniffing) {
            Column {
                Text(
                    t("sniffing_type"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                SniffTypeSelector(
                    selected = sniffTypes,
                    onToggle = { store.toggleSniffType(it) }
                )
            }
        }

        val perAppMode by store.perAppMode.collectAsState()
        val perAppList by store.perAppList.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenPerApp() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("per_app"), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        perAppSummary(perAppMode, perAppList.size, lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Text(
            t("takes_effect"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(t("auto_refresh"), style = MaterialTheme.typography.titleMedium)
        Box {
            OutlinedButton(
                onClick = { autoRefreshOpen = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(refreshLabel(autoRefreshHours), modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ExpandMore, contentDescription = null)
            }
            DropdownMenu(expanded = autoRefreshOpen, onDismissRequest = { autoRefreshOpen = false }) {
                listOf(0, 1, 6, 12, 24).forEach { h ->
                    DropdownMenuItem(
                        text = { Text(refreshLabel(h)) },
                        onClick = { store.setAutoRefreshHours(h); autoRefreshOpen = false }
                    )
                }
            }
        }

        Text(t("tools"), style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenCleanIp() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("scan_warp"), style = MaterialTheme.typography.bodyLarge)
                    Text(t("scan_sub"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Text(t("language"), style = MaterialTheme.typography.titleMedium)
        Box {
            OutlinedButton(
                onClick = { langOpen = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (curLang == Lang.FA) "فارسی" else "English", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ExpandMore, contentDescription = null)
            }
            DropdownMenu(expanded = langOpen, onDismissRequest = { langOpen = false }) {
                DropdownMenuItem(text = { Text("English") }, onClick = { store.setLang(Lang.EN); langOpen = false })
                DropdownMenuItem(text = { Text("فارسی") }, onClick = { store.setLang(Lang.FA); langOpen = false })
            }
        }

        Text(t("developer"), style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenAbout() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("about"), style = MaterialTheme.typography.bodyLarge)
                    Text(t("about_sub"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenDonation() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (lang == Lang.FA) "حمایت از ما" else "Support Us", style = MaterialTheme.typography.bodyLarge)
                    Text(if (lang == Lang.FA) "با حمایت مالی، به توسعه جی روت و اینترنت آزاد کمک کنید" else "Donate to develope GRoute and Free internet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.Favorite, contentDescription = null,
                    tint = Color(0xFF6D9BEE), modifier = Modifier.padding(end = 12.dp))
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenLogs() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("xray_logs"), style = MaterialTheme.typography.bodyLarge)
                    Text(t("xray_logs_sub"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }
    }
}

private val TelegramIcon: ImageVector =
    ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).run {
        addPath(
            pathData = PathParser().parsePathString(
                "M9.78,18.65L10.06,14.42L17.74,7.5C18.08,7.19 17.67,7.04 17.22,7.31L7.74,13.3L3.64,12C2.76,11.75 2.75,11.14 3.84,10.7L19.81,4.54C20.54,4.21 21.24,4.72 20.96,5.84L18.24,18.65C18.05,19.55 17.5,19.77 16.74,19.35L12.6,16.3L10.61,18.23C10.38,18.46 10.19,18.65 9.78,18.65Z"
            ).toNodes(),
            fill = SolidColor(Color.Black)
        )
        build()
    }

@Composable
private fun AboutScreen(modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "—"
    }
    val xrayVersion = remember { xrayCoreVersion() }
    var privacyOpen by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var updateUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val logoRes = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) R.drawable.logo else R.drawable.logo_black
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(logoRes),
            contentDescription = null,
            modifier = Modifier.padding(top = 8.dp).size(128.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
                AboutInfoRow(t("app_version"), localizeDigits(appVersion, lang))
                AboutInfoRow(t("xray_version"), localizeDigits(xrayVersion, lang))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { runCatching { uriHandler.openUri("https://github.com/SuOracle/GRoute") } },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("source_code"), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "github.com/SuOracle/GRoute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = !checking) {
                    val url = updateUrl
                    if (url != null) {
                        runCatching { uriHandler.openUri(url) }
                    } else {
                        checking = true
                        updateStatus = t("checking_updates")
                        scope.launch {
                            when (val r = UpdateChecker.check(appVersion)) {
                                is UpdateChecker.Result.Available -> {
                                    updateStatus = t("update_available").format(r.version)
                                    updateUrl = r.url
                                }
                                UpdateChecker.Result.UpToDate -> updateStatus = t("up_to_date")
                                UpdateChecker.Result.Failed -> updateStatus = t("update_failed")
                            }
                            checking = false
                        }
                    }
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("check_updates"), style = MaterialTheme.typography.bodyLarge)
                    updateStatus?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { privacyOpen = !privacyOpen },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        t("privacy_policy"),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.ExpandMore, contentDescription = null,
                        modifier = Modifier.graphicsLayer { rotationZ = if (privacyOpen) 180f else 0f }
                    )
                }
                AnimatedVisibility(visible = privacyOpen) {
                    Text(
                        if (lang == Lang.FA) PRIVACY_FA else PRIVACY_EN,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { runCatching { uriHandler.openUri("https://t.me/OracleVPNsupport") } }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                TelegramIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                t("telegram_support"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun xrayCoreVersion(): String = runCatching {
    Class.forName("gozarcore.Gozarcore")
        .getMethod("xrayVersion")
        .invoke(null) as String
}.getOrNull()?.takeIf { it.isNotBlank() } ?: "—"

private val PRIVACY_EN = """
GRoute is built to protect your privacy.

What we collect: Nothing. GRoute has no accounts, no analytics, no advertising and no tracking. The developer runs no servers that receive your browsing activity.

On your device: Your server configurations are stored encrypted in the app's private storage. Data-usage statistics (how much traffic passed through the tunnel) stay only on your device and are never transmitted anywhere. Clearing the app's data removes them.

Network requests: To show your current IP address and approximate location, the app contacts third-party services such as ipwho.is and ipify.org. These services necessarily see the IP address of your connection. No other identifying information is sent.

Your servers: The proxy/VPN servers you add are provided by you or your subscription provider. GRoute has no control over, and no visibility into, those servers' logging practices — choose providers you trust.

Permissions: The VPN permission is used solely to route traffic through the tunnel you select. It is never used to inspect, modify or record your traffic.

Changes: This policy may be updated as the app evolves; material changes will be noted in new releases.

Contact: Questions? Reach the developer on Telegram at @OracleVPNsupport.
""".trimIndent()

private val PRIVACY_FA = """
جی‌روت برای حفاظت از حریم خصوصی شما ساخته شده است.

چه چیزی جمع‌آوری می‌کنیم: هیچ‌چیز. جی‌روت حساب کاربری، تحلیل آماری، تبلیغات و ردیابی ندارد. توسعه‌دهنده هیچ سروری که فعالیت مرور شما را دریافت کند اجرا نمی‌کند.

روی دستگاه شما: کانفیگ‌های سرور شما به‌صورت رمزگذاری‌شده در حافظهٔ خصوصی برنامه ذخیره می‌شوند. آمار مصرف داده (میزان ترافیک عبوری از تونل) فقط روی دستگاه شما می‌ماند و به هیچ‌جا ارسال نمی‌شود. پاک‌کردن دادهٔ برنامه آن را حذف می‌کند.

درخواست‌های شبکه: برای نمایش نشانی IP و موقعیت تقریبی شما، برنامه با سرویس‌های شخص ثالث مانند ipwho.is و ipify.org تماس می‌گیرد. این سرویس‌ها ناگزیر نشانی IP اتصال شما را می‌بینند. هیچ اطلاعات شناسایی دیگری ارسال نمی‌شود.

سرورهای شما: سرورهای پراکسی/وی‌پی‌ان که اضافه می‌کنید توسط شما یا ارائه‌دهندهٔ اشتراکتان فراهم می‌شوند. جی‌روت هیچ کنترل یا دیدی نسبت به سیاست ثبت لاگ آن سرورها ندارد؛ ارائه‌دهنده‌ای را انتخاب کنید که به آن اعتماد دارید.

دسترسی‌ها: دسترسی وی‌پی‌ان تنها برای هدایت ترافیک از طریق تونلی که انتخاب می‌کنید استفاده می‌شود و هرگز برای بازرسی، تغییر یا ثبت ترافیک شما به‌کار نمی‌رود.

تغییرات: این سیاست ممکن است با تکامل برنامه به‌روزرسانی شود؛ تغییرات مهم در نسخه‌های جدید اعلام می‌شوند.

تماس: سؤالی دارید؟ از طریق تلگرام با @OracleVPNsupport در ارتباط باشید.
""".trimIndent()

@Composable
private fun LogsScreen(store: ConfigStore, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val scope = rememberCoroutineScope()
    var logs by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val configs by store.configs.collectAsState()

    fun load() {
        loading = true
        scope.launch {
            val secrets = configs.filter { it.locked }
            val out = withContext(Dispatchers.IO) { redactSecrets(readLogcat(), secrets) }
            logs = out
            loading = false
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BounceButton(onClick = { load() }, modifier = Modifier.weight(1f)) { Text(t("refresh")) }
            BounceOutlinedButton(
                onClick = {
                    runCatching { Runtime.getRuntime().exec(arrayOf("logcat", "-c")) }
                    logs = ""
                },
                modifier = Modifier.weight(1f)
            ) { Text(t("clear")) }
        }
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (logs.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (loading) t("testing") else t("no_logs"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SelectionContainer {
                    Text(
                        logs,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)
                    )
                }
            }
        }
    }
}

private fun redactSecrets(text: String, secrets: List<ProxyConfig>): String {
    if (secrets.isEmpty() || text.isEmpty()) return text
    var out = text
    val tokens = LinkedHashSet<String>()
    secrets.forEach { c ->
        if (c.address.isNotBlank()) {
            tokens.add("${c.address}:${c.port}")
            tokens.add(c.address)
        }
        listOf(c.uuid, c.password, c.publicKey, c.shortId, c.privateKey, c.sni, c.host, c.serviceName)
            .filter { it.length >= 4 }
            .forEach { tokens.add(it) }
    }
    tokens.sortedByDescending { it.length }.forEach { token ->
        out = out.replace(token, "[hidden]", ignoreCase = true)
    }
    return out
}

private fun readLogcat(): String = try {
    val proc = Runtime.getRuntime().exec(arrayOf(
        "logcat", "-d", "-v", "time",
        "XrayCore:V", "GoLog:V", "GozarVpnService:V", "*:S"
    ))
    val lines = proc.inputStream.bufferedReader().readLines()
        .filterNot { it.startsWith("---------") }
    if (lines.isEmpty()) "" else lines.takeLast(400).joinToString("\n")
} catch (e: Exception) {
    e.message ?: "Unable to read logs"
}

@Composable
private fun StabilityTestScreen(store: ConfigStore, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()

    val configs by store.configs.collectAsState()
    val selectedId by store.selectedId.collectAsState()
    val target = configs.find { it.id == selectedId } ?: configs.firstOrNull()

    var directStatus by remember { mutableStateOf<DirectStatus?>(null) }
    LaunchedEffect(Unit) {
        directStatus = DirectStatus.CHECKING
        val hosts = listOf("8.8.8.8" to 443, "1.1.1.1" to 443)
        val lat = mutableListOf<Int>()
        repeat(5) { i ->
            when (val r = Pinger.ping(hosts[i % hosts.size].first, hosts[i % hosts.size].second, 2000)) {
                is PingResult.Ok -> lat.add(r.ms)
                else -> {}
            }
            delay(120)
        }
        directStatus = when {
            lat.isEmpty() -> DirectStatus.OFFLINE
            lat.size == 5 && lat.all { it < 250 } -> DirectStatus.STABLE
            else -> DirectStatus.UNSTABLE
        }
    }

    var phase by remember { mutableStateOf(StabilityTest.Phase.DONE) }
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(store.lastTestJson()?.let { StabilityTest.fromJson(it) }) }
    var lastTestTime by remember { mutableStateOf(store.lastTestTime()) }
    var failed by remember { mutableStateOf(false) }
    var dlLive by remember { mutableStateOf(result?.downloadMbps ?: 0.0) }
    var ulLive by remember { mutableStateOf(result?.uploadMbps ?: 0.0) }
    var livePing by remember { mutableStateOf(0.0) }
    var testJob by remember { mutableStateOf<Job?>(null) }

    fun start() {
        val cfg = target ?: run { failed = true; result = null; return }
        running = true; failed = false; result = null
        dlLive = 0.0; ulLive = 0.0; livePing = 0.0
        phase = StabilityTest.Phase.PING
        val testJson = ConfigBuilder.buildForTest(cfg)
        testJob = scope.launch {
            val r = StabilityTest.run(testJson) { ph, v ->
                phase = ph
                when (ph) {
                    StabilityTest.Phase.PING -> if (v > 0) livePing = v
                    StabilityTest.Phase.DOWNLOAD -> if (v > 0) dlLive = if (dlLive <= 0) v else dlLive * 0.6 + v * 0.4
                    StabilityTest.Phase.UPLOAD -> if (v > 0) ulLive = if (ulLive <= 0) v else ulLive * 0.6 + v * 0.4
                    else -> {}
                }
            }
            if (r != null) {
                dlLive = r.downloadMbps; ulLive = r.uploadMbps
                val now = System.currentTimeMillis()
                store.saveLastTest(StabilityTest.toJson(r), now)
                lastTestTime = now
            }
            result = r; failed = r == null; running = false
            phase = StabilityTest.Phase.DONE
            testJob = null
        }
    }

    fun cancel() {
        testJob?.cancel(); testJob = null
        running = false; failed = false
        phase = StabilityTest.Phase.DONE
        dlLive = result?.downloadMbps ?: 0.0
        ulLive = result?.uploadMbps ?: 0.0
        livePing = 0.0
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        AnimatedVisibility(
            visible = directStatus != null,
            enter = fadeIn(tween(300)) + expandVertically(tween(300))
        ) {
            directStatus?.let { DirectStatusBanner(it) }
        }
        if (running) {
            val phaseText = when (phase) {
                StabilityTest.Phase.PING ->
                    t("stab_ping") + "\u2026  " + localizeDigits("${livePing.toInt()}", lang) + " " + t("unit_ms")
                StabilityTest.Phase.DOWNLOAD -> t("download") + "\u2026"
                StabilityTest.Phase.UPLOAD -> t("upload") + "\u2026"
                else -> ""
            }
            Crossfade(targetState = phaseText, animationSpec = tween(300), label = "phaseText") { s ->
                Text(s, style = MaterialTheme.typography.titleMedium)
            }
            AnimatedVisibility(
                visible = phase == StabilityTest.Phase.PING,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(250)) + shrinkVertically(tween(250))
            ) {
                PingLine(color = Color(0xFF35E0FF))
            }
        } else if (result != null && lastTestTime > 0L) {
            Text(
                t("stab_last_test") + " " + formatTestTime(lastTestTime, lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SpeedBar(
                    label = t("download"),
                    mbps = dlLive,
                    active = running && phase == StabilityTest.Phase.DOWNLOAD,
                    accent = listOf(Color(0xFFC23BFF), Color(0xFFF07AD6))
                )
                SpeedBar(
                    label = t("upload"),
                    mbps = ulLive,
                    active = running && phase == StabilityTest.Phase.UPLOAD,
                    accent = listOf(Color(0xFF2AE6FF), Color(0xFF74FFF7))
                )
            }
        }

        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn(tween(400)) + expandVertically(tween(400)) +
                    scaleIn(tween(400), initialScale = 0.92f)
        ) {
            result?.let { r ->
                val ms: (Double) -> String = { localizeDigits("${it.toInt()}", lang) + " " + t("unit_ms") }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricItem(Icons.Filled.Schedule, t("stab_idle_latency"), ms(r.idleLatency), Modifier.weight(1f))
                            MetricItem(Icons.Filled.GraphicEq, t("stab_jitter"), ms(r.jitter), Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricItem(Icons.Filled.ArrowDownward, t("stab_dl_latency"), ms(r.downloadLatency), Modifier.weight(1f))
                            MetricItem(Icons.Filled.ArrowUpward, t("stab_ul_latency"), ms(r.uploadLatency), Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        BounceButton(
            onClick = { if (running) cancel() else start() },
            enabled = target != null,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(if (running) t("cancel") else t("stab_start"), style = MaterialTheme.typography.titleMedium) }

        Text(
            if (target != null) t("stab_testing_server") + " " + target.name else t("stab_no_server"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (failed && target != null) {
            Text(t("stab_failed"), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error)
        }

        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn(tween(450, delayMillis = 80)) + expandVertically(tween(450)) +
                    scaleIn(tween(450, delayMillis = 80), initialScale = 0.92f)
        ) {
            result?.let { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    RevealOnScroll { shown ->
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            RevealText(t("stab_quality"), MaterialTheme.typography.titleMedium, shown, 0)
                            QualityRow(Icons.Filled.SportsEsports, t("stab_gaming"), gamingStars(r), shown, 1)
                            QualityRow(Icons.Filled.Language, t("stab_browsing"), browsingStars(r), shown, 2)
                            QualityRow(Icons.Filled.Movie, t("stab_streaming"), streamingStars(r), shown, 3)
                            QualityRow(Icons.Filled.Videocam, t("stab_calling"), callingStars(r), shown, 4)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTestTime(millis: Long, lang: Lang): String {
    val sdf = java.text.SimpleDateFormat("yyyy/MM/dd  HH:mm", java.util.Locale.US)
    return localizeDigits(sdf.format(java.util.Date(millis)), lang)
}

private enum class DirectStatus { CHECKING, STABLE, UNSTABLE, OFFLINE }

@Composable
private fun DirectStatusBanner(status: DirectStatus, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val green = Color(0xFF2E9E5B)
    val amber = Color(0xFFE0A100)
    val red = Color(0xFFE0413C)

    val infinite = rememberInfiniteTransition(label = "directPulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "directPulseAlpha"
    )

    val fg = when (status) {
        DirectStatus.CHECKING -> MaterialTheme.colorScheme.onSurfaceVariant
        DirectStatus.STABLE -> green
        DirectStatus.UNSTABLE -> amber
        DirectStatus.OFFLINE -> red
    }
    val pulsing = status == DirectStatus.UNSTABLE || status == DirectStatus.OFFLINE

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fg.copy(alpha = 0.13f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
                .graphicsLayer { alpha = if (pulsing) pulse else 1f },
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (status) {
                DirectStatus.CHECKING ->
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = fg, strokeWidth = 2.dp)
                DirectStatus.STABLE ->
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = green,
                        modifier = Modifier.size(22.dp))
                DirectStatus.UNSTABLE ->
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = amber,
                        modifier = Modifier.size(22.dp))
                DirectStatus.OFFLINE ->
                    Icon(Icons.Filled.WifiOff, contentDescription = null, tint = red,
                        modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                when (status) {
                    DirectStatus.CHECKING -> t("stab_direct_checking")
                    DirectStatus.STABLE -> t("stab_direct_stable")
                    DirectStatus.UNSTABLE -> t("stab_direct_unstable")
                    DirectStatus.OFFLINE -> t("stab_direct_offline")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
    }
}

@Composable
private fun SpeedBar(
    label: String,
    mbps: Double,
    active: Boolean,
    accent: List<Color>
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val track = if (isDark) Color(0xFF111A2F) else MaterialTheme.colorScheme.surfaceVariant

    val targetFrac = sqrt((mbps / 100.0).coerceIn(0.0, 1.0)).toFloat()
    val frac by animateFloatAsState(targetFrac, tween(600), label = "speedBar")

    val barStart = accent.first()
    val barEnd = accent.last()
    var trackPx by remember { mutableStateOf(1) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val sweep by shimmer.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "sweep"
    )

    val accentBrush = Brush.horizontalGradient(
        if (isDark) accent else accent.map { lerp(it, Color.Black, 0.34f) }
    )
    val chip = if (isDark) Color(0xFF1B2440) else MaterialTheme.colorScheme.surfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(chip)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall.copy(brush = accentBrush))
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(chip)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    localizeDigits("%.2f".format(mbps), lang) + " " + t("unit_mbps"),
                    style = MaterialTheme.typography.titleLarge.copy(brush = accentBrush)
                )
            }
        }
        Box(
            Modifier.fillMaxWidth().height(22.dp)
                .onSizeChanged { trackPx = it.width }
                .clip(RoundedCornerShape(50)).background(track)
        ) {
            val fillFrac = frac.coerceIn(0f, 1f)
            val tp = trackPx.toFloat().coerceAtLeast(1f)
            val brush = if (isRtl)
                Brush.horizontalGradient(
                    colors = listOf(barEnd, barStart),
                    startX = fillFrac * tp - tp,
                    endX = fillFrac * tp
                )
            else
                Brush.horizontalGradient(
                    colors = listOf(barStart, barEnd),
                    startX = 0f,
                    endX = tp
                )
            Box(
                Modifier.fillMaxWidth(fillFrac).fillMaxHeight()
                    .clip(RoundedCornerShape(50)).background(brush)
            ) {
                if (active) {
                    val fw = (fillFrac * tp).coerceAtLeast(1f)
                    val band = fw * 0.4f
                    val pos = sweep * (fw + band) - band
                    Box(
                        Modifier.matchParentSize().background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startX = pos,
                                endX = pos + band
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun QualityRow(icon: ImageVector, label: String, rating: Float, shown: Boolean, order: Int) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(shown) {
        if (shown) { delay(order * 90L); appear.animateTo(1f, tween(450)) }
    }
    val p = appear.value
    Row(
        Modifier.fillMaxWidth().graphicsLayer { alpha = p; translationX = (1f - p) * 24f },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        StarRow(rating, progress = p)
    }
}

@Composable
private fun StarRow(rating: Float, progress: Float = 1f) {
    val gold = Color(0xFFFFB300)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row {
            for (i in 1..5) {
                val icon = when {
                    rating >= i -> Icons.Filled.Star
                    rating >= i - 0.5f -> Icons.Filled.StarHalf
                    else -> Icons.Filled.StarBorder
                }
                Icon(icon, contentDescription = null, tint = gold,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ConnectGlow(color: Color, modifier: Modifier = Modifier, alpha: Float = 1f) {
    val tr = rememberInfiniteTransition(label = "connectBeam")
    val progress by tr.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "beam"
    )
    Spacer(
        modifier
            .graphicsLayer { this.alpha = alpha }
            .drawWithCache {
                val radius = 16.dp.toPx()
                val inset = 1.dp.toPx()
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            Rect(inset, inset, size.width - inset, size.height - inset),
                            CornerRadius(radius, radius)
                        )
                    )
                }
                val pm = PathMeasure().apply { setPath(path, true) }
                val len = pm.length
                onDrawBehind {
                    if (len <= 0f) return@onDrawBehind
                    val head = ((progress % 1f) + 1f) % 1f * len
                    val tailLen = len * 0.16f
                    val blobs = 16
                    val step = tailLen / blobs
                    fun at(dist: Float) = pm.getPosition(((dist % len) + len) % len)
                    fun glow(c: Offset, r: Float, peak: Float) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.0f to color.copy(alpha = peak),
                                    0.40f to color.copy(alpha = peak * 0.45f),
                                    0.75f to color.copy(alpha = peak * 0.12f),
                                    1.0f to color.copy(alpha = 0f)
                                ),
                                center = c, radius = r
                            ),
                            radius = r, center = c
                        )
                    }
                    for (k in blobs downTo 1) {
                        val frac = 1f - (k - 1f) / blobs
                        val a = frac * frac
                        if (a <= 0.01f) continue
                        glow(at(head - k * step), 5.dp.toPx() + 7.dp.toPx() * frac, 0.6f * a)
                    }
                    val hp = at(head)
                    glow(hp, 12.dp.toPx(), 0.85f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.White,
                                0.45f to Color.White.copy(alpha = 0.5f),
                                1.0f to Color.White.copy(alpha = 0f)
                            ),
                            center = hp, radius = 4.5.dp.toPx()
                        ),
                        radius = 4.5.dp.toPx(), center = hp
                    )
                }
            }
    )
}

@Composable
private fun PingLine(color: Color, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "ping")
    val t by tr.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "pingT"
    )
    val pos = if (t < 0.5f) t * 2f else (1f - t) * 2f
    val dir = if (t < 0.5f) 1f else -1f
    Canvas(modifier.fillMaxWidth().height(34.dp)) {
        val midY = size.height / 2f
        val pad = 12f
        val usableW = (size.width - pad * 2).coerceAtLeast(1f)
        val dotX = pad + pos * usableW
        drawLine(
            color = color.copy(alpha = 0.15f),
            start = Offset(pad, midY), end = Offset(size.width - pad, midY),
            strokeWidth = 3f, cap = StrokeCap.Round
        )
        val trailLen = usableW * 0.34f
        val tailX = (dotX - dir * trailLen).coerceIn(pad, size.width - pad)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, color.copy(alpha = 0.9f)),
                startX = tailX, endX = dotX
            ),
            start = Offset(tailX, midY), end = Offset(dotX, midY),
            strokeWidth = 6f, cap = StrokeCap.Round
        )
        drawCircle(color.copy(alpha = 0.16f), radius = 13f, center = Offset(dotX, midY))
        drawCircle(color.copy(alpha = 0.32f), radius = 8.5f, center = Offset(dotX, midY))
        drawCircle(Color.White, radius = 3.5f, center = Offset(dotX, midY))
    }
}

@Composable
private fun RevealOnScroll(content: @Composable (shown: Boolean) -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val screenH = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    Box(
        Modifier.onGloballyPositioned { c ->
            if (!shown) {
                val b = c.boundsInWindow()
                if (b.height > 0f && b.top < screenH * 0.9f && b.bottom > 0f) shown = true
            }
        }
    ) {
        content(shown)
    }
}

@Composable
private fun RevealText(text: String, style: TextStyle, shown: Boolean, order: Int) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(shown) {
        if (shown) { delay(order * 90L); appear.animateTo(1f, tween(450)) }
    }
    val p = appear.value
    Text(text, style = style, modifier = Modifier.graphicsLayer { alpha = p; translationX = (1f - p) * 24f })
}

private fun starsLB(v: Double, a: Double, b: Double, c: Double, d: Double, e: Double): Float =
    when { v <= a -> 5f; v <= b -> 4f; v <= c -> 3f; v <= d -> 2f; v <= e -> 1f; else -> 0.5f }
private fun starsHB(v: Double, a: Double, b: Double, c: Double, d: Double, e: Double): Float =
    when { v >= a -> 5f; v >= b -> 4f; v >= c -> 3f; v >= d -> 2f; v >= e -> 1f; else -> 0.5f }
private fun pingStars(ms: Double) = if (ms <= 0.0) 0.5f else starsLB(ms, 60.0, 100.0, 160.0, 250.0, 400.0)
private fun jitterStars(ms: Double) = starsLB(ms, 10.0, 25.0, 45.0, 80.0, 130.0)
private fun dlStars(m: Double) = starsHB(m, 40.0, 20.0, 10.0, 4.0, 1.5)
private fun ulStars(m: Double) = starsHB(m, 15.0, 8.0, 4.0, 2.0, 0.7)
private fun roundHalf(x: Float) = (round(x * 2f) / 2f).coerceIn(0.5f, 5f)
private fun avgPing(r: StabilityTest.Result) = r.idleLatency
private fun avgJit(r: StabilityTest.Result) = r.jitter
private fun activePing(r: StabilityTest.Result) =
    maxOf(r.idleLatency, r.downloadLatency, r.uploadLatency)
private fun jitStarsOf(r: StabilityTest.Result) = if (avgPing(r) <= 0.0) 0.5f else jitterStars(avgJit(r))
private fun gamingStars(r: StabilityTest.Result) = roundHalf(
    0.45f * pingStars(activePing(r)) + 0.30f * jitStarsOf(r) +
            0.15f * dlStars(r.downloadMbps) + 0.10f * ulStars(r.uploadMbps))
private fun browsingStars(r: StabilityTest.Result) = roundHalf(
    0.40f * pingStars(avgPing(r)) + 0.20f * jitStarsOf(r) +
            0.30f * dlStars(r.downloadMbps) + 0.10f * ulStars(r.uploadMbps))
private fun streamingStars(r: StabilityTest.Result) = roundHalf(
    0.15f * pingStars(avgPing(r)) + 0.10f * jitStarsOf(r) +
            0.65f * dlStars(r.downloadMbps) + 0.10f * ulStars(r.uploadMbps))
private fun callingStars(r: StabilityTest.Result) = roundHalf(
    0.30f * pingStars(activePing(r)) + 0.25f * jitStarsOf(r) +
            0.20f * dlStars(r.downloadMbps) + 0.25f * ulStars(r.uploadMbps))

private enum class RangeMode(val key: String) {
    TODAY("today"), WEEK("range_7d"), MONTH("range_30d"), CUSTOM("custom_range")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataUsageScreen(modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val daily by UsageStore.usage.collectAsState()
    val hourly by UsageStore.hourly.collectAsState()
    val context = LocalContext.current
    var mode by remember { mutableStateOf(RangeMode.TODAY) }
    var menuOpen by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf(LocalDate.now().minusDays(6)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }

    val bars = remember(daily, hourly, mode, fromDate, toDate) {
        when (mode) {
            RangeMode.TODAY -> UsageStore.hourlyToday(hourly)
            RangeMode.WEEK -> UsageStore.dailyBars(daily, 7)
            RangeMode.MONTH -> UsageStore.dailyBars(daily, 30)
            RangeMode.CUSTOM -> {
                val lo = if (fromDate.isAfter(toDate)) toDate else fromDate
                val hi = if (fromDate.isAfter(toDate)) fromDate else toDate
                val span = java.time.temporal.ChronoUnit.DAYS.between(lo, hi)
                if (span <= 2) UsageStore.hourlyBarsRange(hourly, lo, hi)
                else UsageStore.dailyBarsRange(daily, lo, hi)
            }
        }
    }
    val total = remember(bars) { UsageStore.sum(bars) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = it }) {
            OutlinedTextField(
                value = t(mode.key),
                onValueChange = {},
                readOnly = true,
                label = { Text(t("range")) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                RangeMode.values().forEach { m ->
                    DropdownMenuItem(
                        text = { Text(t(m.key)) },
                        onClick = { mode = m; menuOpen = false }
                    )
                }
            }
        }

        if (mode == RangeMode.CUSTOM) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BounceOutlinedButton(
                    onClick = { showDatePicker(context, fromDate) { fromDate = it } },
                    modifier = Modifier.weight(1f)
                ) { Text(localizeDigits("${t("from")}: $fromDate", lang)) }
                BounceOutlinedButton(
                    onClick = { showDatePicker(context, toDate) { toDate = it } },
                    modifier = Modifier.weight(1f)
                ) { Text(localizeDigits("${t("to")}: $toDate", lang)) }
            }
            Text(
                t("custom_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${t("download")}   ${formatBytes(total[1], lang)}", style = MaterialTheme.typography.bodyLarge)
                Text("${t("upload")}   ${formatBytes(total[0], lang)}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${t("total")}   ${formatBytes(total[0] + total[1], lang)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (bars.isEmpty()) {
            Text(t("no_data_range"), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            var chartVisible by remember(mode, fromDate, toDate) { mutableStateOf(false) }
            LaunchedEffect(mode, fromDate, toDate) { chartVisible = true }
            AnimatedVisibility(
                visible = chartVisible,
                enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.92f)
            ) {
                UsageBarChart(bars)
            }
        }

        Text(
            t("tunnel_only"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun showDatePicker(context: Context, initial: LocalDate, onPicked: (LocalDate) -> Unit) {
    android.app.DatePickerDialog(
        context,
        { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
        initial.year, initial.monthValue - 1, initial.dayOfMonth
    ).show()
}

@Composable
private fun UsageBarChart(bars: List<UsageStore.Bar>) {
    val t = stringsFn()
    val lang = LocalLang.current
    val maxVal = (bars.maxOfOrNull { it.total } ?: 0L).coerceAtLeast(1L)
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val labelEvery = (bars.size / 6).coerceAtLeast(1)
    var focused by remember { mutableStateOf<Int?>(null) }

    val animKey = remember(bars) { bars.hashCode() }
    var appeared by remember(animKey) { mutableStateOf(false) }
    LaunchedEffect(animKey) { appeared = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val f = focused
            AnimatedVisibility(
                visible = f != null && f in bars.indices,
                enter = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(150))
            ) {
                val bar = bars[f ?: 0]
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(localizeDigits(bar.label, lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${t("download")} ${formatBytes(bar.down, lang)}   ${t("upload")} ${formatBytes(bar.up, lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${t("total")} ${formatBytes(bar.total, lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            if (f == null) {
                Text(t("peak_per_bar").format(formatBytes(maxVal, lang)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            var rowWidth by remember { mutableStateOf(1) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .onSizeChanged { rowWidth = it.width }
                    .pointerInput(bars.size) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown()
                                fun idxAt(x: Float): Int =
                                    ((x / rowWidth) * bars.size).toInt().coerceIn(0, bars.lastIndex)
                                focused = idxAt(down.position.x)
                                do {
                                    val event = awaitPointerEvent()
                                    val pos = event.changes.first().position
                                    focused = idxAt(pos.x)
                                } while (event.changes.any { it.pressed })
                                focused = null
                            }
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEachIndexed { i, bar ->
                    val frac = (bar.total.toFloat() / maxVal).coerceIn(0f, 1f)
                    val isFocused = focused == i
                    val targetFrac = if (bar.total > 0 && appeared) frac.coerceAtLeast(0.03f) else 0f
                    val animatedFrac by animateFloatAsState(
                        targetValue = targetFrac,
                        animationSpec = tween(durationMillis = 600),
                        label = "bar"
                    )
                    val focusColor = MaterialTheme.colorScheme.primaryContainer
                    val barColor by animateColorAsState(
                        targetValue = if (isFocused) focusColor else primary,
                        animationSpec = tween(180),
                        label = "barColor"
                    )
                    val barScale by animateFloatAsState(
                        targetValue = if (isFocused) 1.12f else 1f,
                        animationSpec = tween(180),
                        label = "barScale"
                    )
                    Box(
                        Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            Modifier.fillMaxWidth().fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(track.copy(alpha = 0.4f))
                        )
                        if (animatedFrac > 0f) {
                            Box(
                                Modifier.fillMaxWidth().fillMaxHeight(animatedFrac)
                                    .graphicsLayer {
                                        scaleX = barScale; scaleY = 1f
                                        transformOrigin = TransformOrigin(0.5f, 1f)
                                    }
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                bars.forEachIndexed { i, bar ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (i % labelEvery == 0) {
                            Text(
                                localizeDigits(bar.short, lang),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SniffTypeSelector(selected: Set<String>, onToggle: (String) -> Unit) {
    val types = listOf("http", "tls", "quic", "fakedns", "fakedns+others")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { type ->
            val on = type in selected
            val bg by animateColorAsState(
                targetValue = if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200), label = "chipBg"
            )
            val fg by animateColorAsState(
                targetValue = if (on) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200), label = "chipFg"
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onToggle(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(type, color = fg, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun Modifier.pressBounce(
    scale: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope
): Modifier = this
    .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
    .pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            scope.launch {
                scale.animateTo(0.9f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
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
private fun FillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderWidth: Dp = 1.5.dp,
    minHeight: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val disabled = primary.copy(alpha = 0.35f)
    val shape = RoundedCornerShape(16.dp)
    val hazeState = LocalHazeState.current
    val surfaceColor = MaterialTheme.colorScheme.surface

    val interaction = remember { MutableInteractionSource() }
    var center by remember { mutableStateOf(Offset.Zero) }
    var sz by remember { mutableStateOf(IntSize.Zero) }
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "fillScale"
    )
    val maxR = remember(center, sz) {
        val dx = maxOf(center.x, sz.width - center.x)
        val dy = maxOf(center.y, sz.height - center.y)
        sqrt(dx * dx + dy * dy)
    }
    val radius by animateFloatAsState(
        targetValue = if (pressed) maxR else 0f,
        animationSpec = tween(durationMillis = if (pressed) 550 else 300),
        label = "fillRadius"
    )
    val fillFrac = if (maxR > 0f) (radius / maxR).coerceIn(0f, 1f) else 0f
    val contentColor = lerp(if (enabled) primary else disabled, onPrimary, fillFrac)

    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .then(
                if (hazeState != null) Modifier.hazeEffect(hazeState) {
                    blurRadius = 10.dp
                    backgroundColor = surfaceColor
                    tints = listOf(HazeTint(surfaceColor.copy(alpha = 0.30f)))
                    noiseFactor = 0f
                } else Modifier
            )
            .drawBehind {
                if (radius > 0.5f) drawCircle(color = primary, radius = radius, center = center)
            }
            .border(BorderStroke(borderWidth, if (enabled) primary else disabled), shape)
            .defaultMinSize(minWidth = 56.dp, minHeight = minHeight)
            .onSizeChanged { sz = it }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    center = down.position
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
private fun BounceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) = FillButton(onClick, modifier, enabled, borderWidth = 2.dp, content = content)

@Composable
private fun BounceOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) = FillButton(onClick, modifier, enabled, borderWidth = 1.5.dp,
    minHeight = minHeight, contentPadding = contentPadding, content = content)

@Composable
private fun BounceTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.pressBounce(scale, scope),
        content = content
    )
}

@Composable
private fun BounceIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.pressBounce(scale, scope),
        content = content
    )
}

private fun pingRank(p: PingResult?): Int = when (p) {
    is PingResult.Ok -> p.ms
    PingResult.Testing -> 1_000_000
    null -> 2_000_000
    PingResult.Failed -> 3_000_000
}

private fun statusText(conn: Connection, error: String?, lang: Lang): String = when (conn) {
    Connection.DISCONNECTED -> Strings.get(lang, "status_disconnected")
    Connection.CONNECTING -> Strings.get(lang, "status_connecting")
    Connection.CONNECTED -> Strings.get(lang, "status_connected")
    Connection.ERROR -> localizeDigits("${Strings.get(lang, "status_error")}: ${error ?: ""}", lang)
}

private fun formatBytes(bytes: Long, lang: Lang): String {
    val unit: String
    val num: String
    when {
        bytes < 1024 -> { num = "$bytes"; unit = Strings.get(lang, "unit_b") }
        bytes < 1024 * 1024 -> { num = "%.1f".format(bytes / 1024.0); unit = Strings.get(lang, "unit_kb") }
        bytes < 1024L * 1024 * 1024 -> { num = "%.1f".format(bytes / (1024.0 * 1024)); unit = Strings.get(lang, "unit_mb") }
        else -> { num = "%.2f".format(bytes / (1024.0 * 1024 * 1024)); unit = Strings.get(lang, "unit_gb") }
    }
    return "\u202A${localizeDigits(num, lang)}\u202C $unit"
}

@Composable
private fun SpeedText(bytes: Long) {
    val t = stringsFn()
    val lang = LocalLang.current
    val parts = formatBytesParts(bytes, lang)
    Text(
        buildAnnotatedString {
            append("\u202A${parts.first}\u202C ")
            withStyle(SpanStyle(fontSize = 12.sp)) {
                append(parts.second + t("unit_per_sec"))
            }
        },
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1
    )
}

@Composable
private fun StatBox(
    speed: Long,
    total: Long,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val parts = formatBytesParts(speed, lang)
    val hazeState = LocalHazeState.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = if (isDark) color else lerp(color, Color.Black, 0.42f)
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (hazeState != null) Modifier.hazeEffect(hazeState) {
                    blurRadius = 10.dp
                    backgroundColor = surfaceColor
                    tints = listOf(HazeTint(surfaceColor.copy(alpha = 0.25f)))
                    noiseFactor = 0f
                } else Modifier
            )
            .background(accent.copy(alpha = if (isDark) 0.12f else 0.10f))
            .border(BorderStroke(1.dp, accent.copy(alpha = if (isDark) 0.75f else 0.55f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "\u202A${parts.first}\u202C ${parts.second}${t("unit_per_sec")}",
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                maxLines = 1
            )
        }
        Text(
            formatBytes(total, lang),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

private fun formatBytesParts(bytes: Long, lang: Lang): Pair<String, String> {
    val unit: String
    val num: String
    when {
        bytes < 1024 -> { num = "$bytes"; unit = Strings.get(lang, "unit_b") }
        bytes < 1024 * 1024 -> { num = "%.1f".format(bytes / 1024.0); unit = Strings.get(lang, "unit_kb") }
        bytes < 1024L * 1024 * 1024 -> { num = "%.1f".format(bytes / (1024.0 * 1024)); unit = Strings.get(lang, "unit_mb") }
        else -> { num = "%.2f".format(bytes / (1024.0 * 1024 * 1024)); unit = Strings.get(lang, "unit_gb") }
    }
    return localizeDigits(num, lang) to unit
}

@Composable
private fun SubscriptionHeader(
    sub: Subscription,
    isOpen: Boolean,
    onToggle: () -> Unit,
    onRefresh: () -> Unit,
    onRename: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var renaming by remember { mutableStateOf(false) }
    var shareMenu by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf(sub.name) }

    if (renaming) {
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text(t("edit_sub_name")) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val nm = draftName.trim()
                    if (nm.isNotEmpty()) onRename(nm)
                    renaming = false
                }) { Text(t("save")) }
            },
            dismissButton = {
                TextButton(onClick = { renaming = false }) { Text(t("cancel")) }
            }
        )
    }

    Card(
        modifier = modifier.appearOnce().fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isOpen) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(sub.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, modifier = Modifier.weight(1f))
                Box {
                    Icon(Icons.Filled.Share, contentDescription = t("share"), tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { shareMenu = true }.padding(6.dp).size(20.dp))
                    DropdownMenu(expanded = shareMenu, onDismissRequest = { shareMenu = false }) {
                        CompactMenuItem(Icons.Filled.ContentCopy, t("share_clipboard")) {
                            shareMenu = false
                            clipboard.setText(AnnotatedString(sub.url))
                            android.widget.Toast.makeText(context, t("copied"), android.widget.Toast.LENGTH_SHORT).show()
                        }
                        CompactMenuItem(Icons.Filled.Share, t("share_app")) {
                            shareMenu = false
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, sub.url)
                            }
                            context.startActivity(Intent.createChooser(send, sub.name))
                        }
                    }
                }
                Icon(Icons.Filled.Edit, contentDescription = t("edit_sub_name"), tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { draftName = sub.name; renaming = true }.padding(6.dp).size(20.dp))
                Icon(Icons.Filled.Refresh, contentDescription = t("refresh"), tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onRefresh() }.padding(6.dp).size(20.dp))
                Icon(Icons.Filled.Delete, contentDescription = t("remove"), tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onRemove() }.padding(6.dp).size(20.dp))
            }
            if (sub.total > 0) {
                Spacer(Modifier.height(6.dp))
                UsageBar(used = sub.used, total = sub.total)
            }
            val usage = usageText(sub, lang)
            if (usage != null) {
                Spacer(Modifier.height(4.dp))
                Text(usage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun usageText(sub: Subscription, lang: Lang): String? {
    if (sub.total <= 0 && sub.expire <= 0) return null
    val parts = mutableListOf<String>()
    if (sub.total > 0) {
        val remaining = (sub.total - sub.used).coerceAtLeast(0)
        parts.add("${formatBytes(remaining, lang)} ${Strings.get(lang, "of")} ${formatBytes(sub.total, lang)} ${Strings.get(lang, "left")}")
    }
    if (sub.expire > 0) {
        val daysLeft = (sub.expire * 1000 - System.currentTimeMillis()) / 86_400_000L
        if (daysLeft >= 0) parts.add("${Strings.get(lang, "expires_in")} ${localizeDigits("$daysLeft", lang)}${Strings.get(lang, "unit_days")}")
    }
    return parts.joinToString("  •  ")
}

@Composable
private fun UsageBar(used: Long, total: Long) {
    val remaining = (total - used).coerceAtLeast(0L)
    val frac = if (total > 0) (remaining.toFloat() / total).coerceIn(0f, 1f) else 0f
    val barColor = when {
        frac <= 0.10f -> Color(0xFFE53935)
        frac <= 0.30f -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (frac > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(frac)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConfigRow(
    config: ProxyConfig,
    isSelected: Boolean,
    isActive: Boolean,
    ping: PingResult?,
    selectionMode: Boolean,
    isChecked: () -> Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShareFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var shareMenu by remember { mutableStateOf(false) }
    val checked by remember { derivedStateOf { isChecked() } }

    val highlight by animateColorAsState(
        targetValue = if (checked || isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(220),
        label = "rowHighlight"
    )
    Card(
        modifier = modifier.appearOnce().fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().background(highlight)
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (checked) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            } else {
                LivePingDot(ping)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (config.locked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.weight(1f)) { MarqueeName(config.name) }
                    }
                } else {
                    MarqueeName(config.name)
                }
                Text(
                    when {
                        config.locked && isActive -> "${t("locked_config")}  •  ${t("status_connected")}"
                        config.locked -> t("locked_config")
                        isActive -> "${localizeDigits("${config.address}:${config.port}", lang)}  •  ${t("status_connected")}"
                        else -> localizeDigits("${config.address}:${config.port}", lang)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(6.dp))
            PingChip(ping)
            if (!checked && !selectionMode) {
                Spacer(Modifier.width(2.dp))
                Box {
                    Icon(Icons.Filled.Share, contentDescription = t("share"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(CircleShape).clickable { shareMenu = true }.padding(8.dp).size(21.dp))
                    DropdownMenu(expanded = shareMenu, onDismissRequest = { shareMenu = false }) {
                        if (!config.locked) {
                            CompactMenuItem(Icons.Filled.ContentCopy, t("share_clipboard")) {
                                shareMenu = false
                                clipboard.setText(AnnotatedString(ConfigShare.toLink(config)))
                                android.widget.Toast.makeText(context, t("copied"), android.widget.Toast.LENGTH_SHORT).show()
                            }
                            CompactMenuItem(Icons.Filled.Share, t("share_app")) {
                                shareMenu = false
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, ConfigShare.toLink(config))
                                }
                                context.startActivity(Intent.createChooser(send, config.name))
                            }
                        }
                        CompactMenuItem(Icons.Filled.InsertDriveFile, t("share_file")) {
                            shareMenu = false
                            onShareFile()
                        }
                    }
                }
                Icon(Icons.Filled.Edit, contentDescription = t("edit"),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(CircleShape).clickable { onEdit() }.padding(8.dp).size(21.dp))
                Icon(Icons.Filled.Delete, contentDescription = t("delete"),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clip(CircleShape).clickable { onDelete() }.padding(8.dp).size(21.dp))
            }
        }
    }
}

@Composable
private fun SelectionActionBar(
    count: Int,
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onShareApp: () -> Unit,
    onShareFile: () -> Unit,
    onDelete: () -> Unit
) {
    val t = stringsFn()
    val lang = LocalLang.current
    var shareMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Close, contentDescription = t("cancel"),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clip(CircleShape).clickable { onClose() }.padding(8.dp).size(22.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "${localizeDigits("$count", lang)} ${t("selected")}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Box {
                Icon(Icons.Filled.Share, contentDescription = t("share"),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clip(CircleShape).clickable { shareMenu = true }.padding(8.dp).size(22.dp))
                DropdownMenu(expanded = shareMenu, onDismissRequest = { shareMenu = false }) {
                    CompactMenuItem(Icons.Filled.ContentCopy, t("share_clipboard")) { shareMenu = false; onCopy() }
                    CompactMenuItem(Icons.Filled.Share, t("share_app")) { shareMenu = false; onShareApp() }
                    CompactMenuItem(Icons.Filled.InsertDriveFile, t("share_file")) { shareMenu = false; onShareFile() }
                }
            }
            Icon(Icons.Filled.Delete, contentDescription = t("delete"),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.clip(CircleShape).clickable { onDelete() }.padding(8.dp).size(22.dp))
        }
    }
}

@Composable
private fun MarqueeName(text: String) {
    var containerW by remember { mutableStateOf(0) }
    var textW by remember { mutableStateOf(0) }
    val scroll = remember { Animatable(0f) }
    val density = LocalDensity.current
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val speed = with(density) { 30.dp.toPx() }
    val overflow = (textW - containerW).coerceAtLeast(0)

    LaunchedEffect(overflow, text, ltr) {
        if (overflow <= 0) { scroll.snapTo(0f); return@LaunchedEffect }
        val target = if (ltr) -overflow.toFloat() else overflow.toFloat()
        val dur = ((overflow / speed) * 1000f).toInt().coerceIn(700, 7000)
        while (true) {
            scroll.snapTo(0f)
            delay(1500)
            scroll.animateTo(target, tween(dur, easing = LinearEasing))
            delay(2000)
            scroll.animateTo(0f, tween(dur, easing = LinearEasing))
            delay(1500)
        }
    }

    Box(
        Modifier.fillMaxWidth().clipToBounds().onSizeChanged { containerW = it.width }
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 14.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            modifier = Modifier
                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                .onSizeChanged { textW = it.width }
                .graphicsLayer { translationX = scroll.value }
        )
    }
}

@Composable
private fun LivePingDot(ping: PingResult?) {
    val color = pingColor(ping)
    val transition = rememberInfiniteTransition(label = "pingDot")
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
        label = "ripple"
    )
    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(24.dp)
                .graphicsLayer {
                    val sc = 0.40f + ripple * 0.60f
                    scaleX = sc; scaleY = sc
                    alpha = (1f - ripple) * 0.6f
                }
                .background(Brush.radialGradient(listOf(color, Color.Transparent)), CircleShape)
        )
        Box(
            Modifier
                .size(16.dp)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.40f), Color.Transparent)), CircleShape)
        )
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
    }
}

@Composable
private fun PingChip(ping: PingResult?) {
    if (ping == null) return
    val t = stringsFn()
    val lang = LocalLang.current
    val color = pingColor(ping)
    val text = when (ping) {
        is PingResult.Ok -> "${localizeDigits("${ping.ms}", lang)} ${t("unit_ms")}"
        PingResult.Testing -> "…"
        else -> t("delay_failed")
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
private fun pingColor(ping: PingResult?): Color = when (ping) {
    is PingResult.Ok -> when {
        ping.ms <= 250 -> Color(0xFF2E9E44)
        ping.ms <= 600 -> Color(0xFFF59E0B)
        else -> Color(0xFFE53935)
    }
    PingResult.Failed -> if (isSystemInDarkTheme()) Color(0xFF6B7280) else Color(0xFF4B5563)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun PingBadge(ping: PingResult?) {
    val t = stringsFn()
    val lang = LocalLang.current
    when (ping) {
        is PingResult.Ok -> Text("${localizeDigits("${ping.ms}", lang)} ${t("unit_ms")}", style = MaterialTheme.typography.bodySmall, color = pingColor(ping))
        PingResult.Testing -> Text("…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PingResult.Failed -> Text(t("delay_failed"), style = MaterialTheme.typography.bodySmall, color = pingColor(ping))
        null -> {}
    }
}

private data class AppEntry(
    val pkg: String,
    val label: String,
    val icon: ImageBitmap
)

private fun perAppSummary(mode: PerAppMode, count: Int, lang: Lang): String = when (mode) {
    PerAppMode.OFF -> Strings.get(lang, "per_app_off")
    PerAppMode.ALLOWLIST -> localizeDigits("${Strings.get(lang, "per_app_allow")} · $count", lang)
    PerAppMode.BLOCKLIST -> localizeDigits("${Strings.get(lang, "per_app_block")} · $count", lang)
}

@Composable
private fun AppProxyScreen(
    store: ConfigStore,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val context = LocalContext.current
    val mode by store.perAppMode.collectAsState()
    val selected by store.perAppList.collectAsState()

    var apps by remember { mutableStateOf<List<AppEntry>?>(null) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .asSequence()
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .filter { it.packageName != context.packageName }
                .map { ai ->
                    AppEntry(
                        pkg = ai.packageName,
                        label = runCatching { pm.getApplicationLabel(ai).toString() }
                            .getOrDefault(ai.packageName),
                        icon = runCatching {
                            pm.getApplicationIcon(ai).toBitmap(96, 96).asImageBitmap()
                        }.getOrElse {
                            android.graphics.Bitmap
                                .createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
                                .asImageBitmap()
                        }
                    )
                }
                .sortedBy { it.label.lowercase() }
                .toList()
        }
    }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            t("per_app_mode"),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ModeRow(t("per_app_off"), mode == PerAppMode.OFF) { store.setPerAppMode(PerAppMode.OFF) }
        ModeRow(t("per_app_allow"), mode == PerAppMode.ALLOWLIST) { store.setPerAppMode(PerAppMode.ALLOWLIST) }
        ModeRow(t("per_app_block"), mode == PerAppMode.BLOCKLIST) { store.setPerAppMode(PerAppMode.BLOCKLIST) }

        if (mode == PerAppMode.OFF) {
            Text(
                t("per_app_off_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(t("search_apps")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            val list = apps
            if (list == null) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            t("loading_apps"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val filtered = remember(list, query) {
                    if (query.isBlank()) list
                    else list.filter { it.label.contains(query, true) || it.pkg.contains(query, true) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.pkg }) { app ->
                        val checked = app.pkg in selected
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { store.togglePerApp(app.pkg) }
                                .animateItem(),
                            shape = RoundedCornerShape(16.dp),
                            colors = if (checked)
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else CardDefaults.cardColors()
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.label, style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(app.pkg, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { store.togglePerApp(app.pkg) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = if (selected)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}