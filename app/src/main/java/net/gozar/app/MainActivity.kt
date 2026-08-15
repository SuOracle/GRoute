package net.gozar.app

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.net.Uri
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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NetworkCheck
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import android.Manifest
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.FilterQuality
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.util.concurrent.Executors
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import kotlinx.coroutines.isActive
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
import kotlin.math.roundToInt

private val BrandBlue = Color(0xFF3D6AD6)
private val SplashBackground = Color(0xFF13234A)

private val GnetLightColors = lightColorScheme(
    primary = Color(0xFF2557D6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF08214F),
    inversePrimary = Color(0xFFA9C4FF),
    secondary = Color(0xFF4A6288),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFDDF2),
    onSecondaryContainer = Color(0xFF17212F),
    tertiary = Color(0xFF0A7C99),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC9EDF7),
    onTertiaryContainer = Color(0xFF04333F),
    background = Color(0xFFEBF0F8),
    onBackground = Color(0xFF131720),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF131720),
    surfaceVariant = Color(0xFFE1E8F4),
    onSurfaceVariant = Color(0xFF566276),
    surfaceTint = Color(0xFF2557D6),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD7DFEC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFDFEFF),
    surfaceContainerHigh = Color(0xFFF7FAFE),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFF272E3C),
    inverseOnSurface = Color(0xFFEBF0F8),
    error = Color(0xFFC02B26),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFFB6C1D2),
    outlineVariant = Color(0xFFD7DFEC),
    scrim = Color(0xFF000000)
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
    surfaceBright = Color(0xFF2A3348),
    surfaceDim = Color(0xFF0B101B),
    surfaceContainerLowest = Color(0xFF0A0F1A),
    surfaceContainerLow = Color(0xFF131A29),
    surfaceContainer = Color(0xFF161D2E),
    surfaceContainerHigh = Color(0xFF1D2537),
    surfaceContainerHighest = Color(0xFF232C40),
    tertiary = Color(0xFF35E0FF),
    onTertiary = Color(0xFF042430),
    tertiaryContainer = Color(0xFF10394A),
    onTertiaryContainer = Color(0xFFC5F1FD),
    inversePrimary = Color(0xFF2557D6),
    surfaceTint = Color(0xFF6CA0FF),
    inverseSurface = Color(0xFFE6EAF2),
    inverseOnSurface = Color(0xFF161D2E),
    error = Color(0xFFFF7A7A),
    onError = Color(0xFF2A0A0A),
    errorContainer = Color(0xFF5A1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF38445C),
    outlineVariant = Color(0xFF283244),
    scrim = Color(0xFF000000)
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
    surfaceBright = Color(0xFF1A1F2A),
    surfaceDim = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF07090D),
    surfaceContainer = Color(0xFF0B0E14),
    surfaceContainerHigh = Color(0xFF0F131B),
    surfaceContainerHighest = Color(0xFF12161F),
    tertiary = Color(0xFF35E0FF),
    onTertiary = Color(0xFF042430),
    tertiaryContainer = Color(0xFF0A2733),
    onTertiaryContainer = Color(0xFFC5F1FD),
    inversePrimary = Color(0xFF2557D6),
    surfaceTint = Color(0xFF6CA0FF),
    inverseSurface = Color(0xFFE6EAF2),
    inverseOnSurface = Color(0xFF0B0E14),
    error = Color(0xFFFF7A7A),
    onError = Color(0xFF2A0A0A),
    errorContainer = Color(0xFF3A1010),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF2A3344),
    outlineVariant = Color(0xFF1B2130),
    scrim = Color(0xFF000000)
)

private val AppCyan: Color
    @Composable get() =
        if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF35E0FF)
        else Color(0xFF0A7C99)

private val AppAqua: Color
    @Composable get() =
        if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF2AE6FF)
        else Color(0xFF067E9B)

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

            val taglineParts = remember(t("welcome_tagline")) {
                t("welcome_tagline").split(Regex("[,\u060C]"))
                    .map { it.trim() }.filter { it.isNotEmpty() }
            }
            val taglineColor = Color(0xFFEDEFF3)

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 10.dp)
                    .graphicsLayer {
                        alpha = taglineAlpha
                        translationY = taglineShift
                    }
            ) {
                val taglineStyle = MaterialTheme.typography.titleMedium

                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = taglineColor,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = taglineParts.getOrElse(0) { "" },
                    style = taglineStyle,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = welcomeFont,
                    color = taglineColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = " | ",
                    style = taglineStyle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = welcomeFont,
                    color = taglineColor.copy(alpha = 0.45f)
                )
                Text(
                    text = taglineParts.getOrElse(1) { "" },
                    style = taglineStyle,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = welcomeFont,
                    color = taglineColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(7.dp))
                Icon(
                    painter = painterResource(R.drawable.dove),
                    contentDescription = null,
                    tint = taglineColor,
                    modifier = Modifier.size(17.dp)
                )
            }
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

object WindscribeBrand {

    const val SUB_NAME = "Windscribe"

    fun isWindscribe(sub: Subscription): Boolean {
        val n = sub.name.trim()
        return n.equals(SUB_NAME, ignoreCase = true) ||
                n.startsWith("$SUB_NAME -", ignoreCase = true) ||
                n.startsWith("$SUB_NAME-", ignoreCase = true)
    }

    fun displayName(sub: Subscription, lang: Lang): String =
        if (lang == Lang.FA && isWindscribe(sub)) Strings.get(lang, "ws_title") else sub.name

    internal val LightStops = listOf(
        Color(0xFFC3D9F2),
        Color(0xFFBFE2F5),
        Color(0xFFC6EDF8)
    )

    internal val DarkStops = listOf(
        Color(0xFF1B2E4A),
        Color(0xFF1B3D5C),
        Color(0xFF1C4E6B)
    )

    internal val AmoledStops = listOf(
        Color(0xFF0B1521),
        Color(0xFF0C1F2E),
        Color(0xFF0D2839)
    )

    internal val LightRow = Color(0xFFA9D2F4)
    internal val DarkRow = Color(0xFF0C2138)
    internal val AmoledRow = Color(0xFF0C2A48)

}

@Composable
private fun windscribeDark(): Boolean =
    MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
private fun windscribeAmoled(): Boolean =
    MaterialTheme.colorScheme.surface == Color(0xFF000000)

@Composable
private fun windscribeCardBrush(): Brush = Brush.linearGradient(
    when {
        windscribeAmoled() -> WindscribeBrand.AmoledStops
        windscribeDark() -> WindscribeBrand.DarkStops
        else -> WindscribeBrand.LightStops
    }
)

@Composable
private fun windscribeRowColor(): Color = when {
    windscribeAmoled() -> WindscribeBrand.AmoledRow
    windscribeDark() -> WindscribeBrand.DarkRow
    else -> WindscribeBrand.LightRow
}


object ImportBus {
    private val _pending = kotlinx.coroutines.flow.MutableStateFlow<ByteArray?>(null)
    val pending: kotlinx.coroutines.flow.StateFlow<ByteArray?> = _pending
    fun offer(bytes: ByteArray) { _pending.value = bytes }
    fun clear() { _pending.value = null }

    private val _scanned = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val scanned: kotlinx.coroutines.flow.StateFlow<String?> = _scanned
    fun offerScan(text: String) { _scanned.value = text }
    fun clearScan() { _scanned.value = null }
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
        IkeController.bind(this)
        watchTunnel()
        lifecycleScope.launch {
            VpnState.state.collect { s ->
                if (s == Connection.DISCONNECTED && !IkeController.active) {
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

    private fun watchTunnel() {
        lifecycleScope.launch {
            VpnState.state.collect { state ->
                if (state == Connection.CONNECTED && !IkeController.active) {
                    TunnelHealth.check()
                    RadarRunner.start(true)
                    val id = VpnState.activeId.value
                    store.configs.value.find { it.id == id }?.let {
                        DebugRunner.start(it, store)
                    }
                } else if (state == Connection.DISCONNECTED) {
                    TunnelHealth.reset()
                    RadarRunner.start(false)
                } else {
                    TunnelHealth.reset()
                }
            }
        }
    }

    private fun t2(key: String): String = Strings.get(store.lang.value, key)

    private fun proceedConnect(config: ProxyConfig) {
        if (VpnState.state.value == Connection.CONNECTED) return
        if (config.protocol == "ikev2") {
            IkeController.claim(config)
            if (VpnState.state.value != Connection.DISCONNECTED) startService(
                Intent(this, GozarVpnService::class.java).setAction(GozarVpnService.ACTION_STOP)
            )
            val startIke = {
                if (!IkeController.connect(this, config)) {
                    Toast.makeText(this, t2("ikev2_bad_config"), Toast.LENGTH_LONG).show()
                    VpnState.setDisconnected()
                }
            }
            val consent = runCatching { VpnService.prepare(this) }.getOrNull()
            if (consent != null) {
                afterPermission = startIke
                vpnPermission.launch(consent)
            } else startIke()
            return
        }
        if (IkeController.active) IkeController.disconnect(this)
        val json = ConfigBuilder.build(config, store.fragment.value, store.splitRouting.value, store.sniffing.value, store.sniffTypes.value, mux = store.mux.value, muxConcurrency = store.muxConcurrency.value, adBlock = store.adBlock.value, fakeDns = store.fakeDns.value,
            encryptedDns = store.encryptedDns.value,
            torBase = if (config.protocol == "tor" && config.torBaseId.isNotEmpty())
                store.configs.value.find { it.id == config.torBaseId } else null,
            chainBase = if (config.chainId.isNotEmpty())
                store.configs.value.find { it.id == config.chainId } else null,
            onionRouting = store.onionRouting.value)
        VpnState.setConnecting(config.id)
        val aether = if (config.protocol == "aether") AetherController.spec(config) else ""
        val intent = VpnService.prepare(this)
        val tor = when {
            config.protocol == "tor" ->
                config.torCountry + "|" + (if (config.torThroughVpn) "1" else "0")
            store.onionRouting.value -> "|1"
            else -> null
        }
        if (intent != null) { afterPermission = { startTunnel(json, config.name, aether, tor) }; vpnPermission.launch(intent) }
        else startTunnel(json, config.name, aether, tor)
    }

    private fun startTunnel(configJson: String, name: String, aether: String, tor: String?) {
        startService(
            Intent(this, GozarVpnService::class.java)
                .putExtra(GozarVpnService.EXTRA_CONFIG, configJson)
                .putExtra(GozarVpnService.EXTRA_NAME, name)
                .putExtra(GozarVpnService.EXTRA_AETHER, aether)
                .putExtra(GozarVpnService.EXTRA_TOR, tor)
                .putExtra(GozarVpnService.EXTRA_STOP_LABEL, Strings.get(store.lang.value, "disconnect"))
        )
    }

    private fun startBlockOnly() {
        if (!store.adBlock.value || !store.blockWhenOff.value) return
        if (VpnService.prepare(this) != null) return
        val json = ConfigBuilder.build(
            ProxyConfig(name = "adblock", protocol = "freedom", address = "127.0.0.1", port = 1),
            splitRouting = store.splitRouting.value,
            sniffing = store.sniffing.value,
            sniffTypes = store.sniffTypes.value,
            adBlock = true,
            directOnly = true,
            fakeDns = store.fakeDns.value,
            encryptedDns = store.encryptedDns.value
        )
        startTunnel(json, Strings.get(store.lang.value, "adblock_notif"), "", null)
    }

    private fun disconnect() {
        if (IkeController.active) {
            IkeController.disconnect(this)
            VpnState.setDisconnected()
            return
        }
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
        if (IkeController.active) return
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
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })
    val settingsScroll = rememberScrollState()

    var showPicker by remember { mutableStateOf(false) }
    var showManual by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var showTorNodes by remember { mutableStateOf(false) }
    var showWindscribe by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
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
        GlassDialog(
            onDismiss = { updateAvailable = null },
            title = t("update_available").format(upd.version),
            confirmLabel = t("update_now"),
            dismissLabel = t("later"),
            onConfirm = {
                runCatching { updateUri.openUri(upd.url) }
                updateAvailable = null
            }
        ) {}
    }
    var usageDetail by remember { mutableStateOf(false) }
    var perAppDetail by remember { mutableStateOf(false) }
    var logsDetail by remember { mutableStateOf(false) }
    var stabilityDetail by remember { mutableStateOf(false) }
    var aboutDetail by remember { mutableStateOf(false) }
    var themeDetail by remember { mutableStateOf(false) }
    var cleanIpDetail by remember { mutableStateOf(false) }
    var netMonDetail by remember { mutableStateOf(false) }
    var netCatDetail by remember { mutableStateOf(false) }
    var netCatIndex by remember { mutableStateOf(-1) }
    var checkHostDetail by remember { mutableStateOf(false) }
    var toolsDetail by remember { mutableStateOf(false) }
    var connDetail by remember { mutableStateOf(false) }
    var prefsDetail by remember { mutableStateOf(false) }
    var exportConfigs by remember { mutableStateOf<List<ProxyConfig>?>(null) }
    val sortMode by store.sortMode.collectAsState()
    val selectedId by store.selectedId.collectAsState()
    val pings = remember { mutableStateMapOf<String, PingResult>() }

    LaunchedEffect(Unit) {
        store.awaitReady()
        store.seedDefaultAetherIfNeeded()
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
        val plain = withContext(Dispatchers.Default) {
            runCatching { ConfigParser.parseBundle(String(bytes, Charsets.UTF_8)) }
                .getOrDefault(emptyList())
        }
        if (plain.isNotEmpty()) {
            val added = store.addImported(plain)
            android.widget.Toast.makeText(
                importContext, t("import_success").format(added),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            ImportBus.clear()
            return@LaunchedEffect
        }
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
        GlassDialog(
            onDismiss = { if (!importBusy) ImportBus.clear() },
            title = t("import_title"),
            confirmLabel = t("import_button"),
            dismissLabel = t("cancel"),
            onConfirm = {
                val bytes = pendingImport
                if (bytes != null && !importBusy && importPassword.isNotEmpty()) {
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
            }
        ) {
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
    }

    val page = pagerState.currentPage
    val onSettingsTab = page == 3
    val subScreenOpen = (page == 1 && (showPicker || showManual || showProjects || showTorNodes || showWindscribe || showScanner || exportConfigs != null)) || (onSettingsTab && (usageDetail || perAppDetail || logsDetail || stabilityDetail || aboutDetail || cleanIpDetail || themeDetail || toolsDetail || connDetail || prefsDetail || netMonDetail || netCatDetail || netCatIndex >= 0 || checkHostDetail))

    val screenKey = when {
        page == 0 -> "shop"
        page == 1 && exportConfigs != null -> "export"
        page == 1 && showManual -> "manual"
        page == 1 && showTorNodes -> "tornodes"
        page == 1 && showScanner -> "scanqr"
        page == 1 && showWindscribe -> "windscribe"
        page == 1 && showProjects -> "projects"
        page == 1 && showPicker -> "picker"
        page == 1 -> "connection"
        page == 2 -> "debugger"
        onSettingsTab && usageDetail -> "usage"
        onSettingsTab && perAppDetail -> "perapp"
        onSettingsTab && logsDetail -> "logs"
        onSettingsTab && stabilityDetail -> "stability"
        onSettingsTab && aboutDetail -> "about"
        onSettingsTab && themeDetail -> "theme"
        onSettingsTab && cleanIpDetail -> "cleanip"
        onSettingsTab && checkHostDetail -> "checkhost"
        onSettingsTab && netCatIndex >= 0 -> "netcatone"
        onSettingsTab && netCatDetail -> "netcat"
        onSettingsTab && netMonDetail -> "netmon"
        onSettingsTab && toolsDetail -> "tools"
        onSettingsTab && connDetail -> "connection_settings"
        onSettingsTab && prefsDetail -> "preferences"
        else -> "settings"
    }

    fun pop() {
        when {
            exportConfigs != null -> exportConfigs = null
            showManual -> { showManual = false; editingConfig = null }
            showWindscribe -> showWindscribe = false
            showScanner -> showScanner = false
            showTorNodes -> showTorNodes = false
            showProjects -> showProjects = false
            showPicker -> showPicker = false
            usageDetail -> usageDetail = false
            perAppDetail -> perAppDetail = false
            logsDetail -> logsDetail = false
            stabilityDetail -> stabilityDetail = false
            aboutDetail -> aboutDetail = false
            themeDetail -> themeDetail = false
            cleanIpDetail -> cleanIpDetail = false
            checkHostDetail -> checkHostDetail = false
            netCatIndex >= 0 -> netCatIndex = -1
            netCatDetail -> netCatDetail = false
            netMonDetail -> netMonDetail = false
            toolsDetail -> toolsDetail = false
            connDetail -> connDetail = false
            prefsDetail -> prefsDetail = false
            page != 1 -> scope.launch { pagerState.animateScrollToPage(1) }
        }
    }

    val canGoBack = subScreenOpen || page != 1
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

    val gradBg = MaterialTheme.colorScheme.background
    val gradDark = gradBg.luminance() < 0.5f
    val gradient = remember(gradBg, gradDark) {
        if (gradDark) Brush.verticalGradient(
            0f to lerp(gradBg, Color(0xFF6D9BEE), 0.12f),
            0.45f to lerp(gradBg, Color(0xFF6D9BEE), 0.05f),
            1f to gradBg
        ) else SolidColor(gradBg)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.background(gradient),
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
                            mixedText(when (screenKey) {
                                "manual" -> if (editingConfig != null) t("edit_config_title") else t("add_config_title")
                                "export" -> t("export_title")
                                "picker" -> t("choose_server")
                                "projects" -> t("free_projects")
                                "tornodes" -> t("tor_nodes")
                                "windscribe" -> t("ws_title")
                                "scanqr" -> t("scan_qr")
                                "usage" -> t("data_usage")
                                "perapp" -> t("per_app")
                                "logs" -> t("xray_logs")
                                "stability" -> t("stab_title")
                                "about" -> t("about")
                                "theme" -> t("theme_settings")
                                "cleanip" -> t("scan_title")
                                "netmon" -> t("netmon_title")
                                "netcat" -> t("netcat_title")
                                "checkhost" -> t("chk_title")
                                "netcatone" -> t(NetMonitor.Categories.getOrNull(netCatIndex)?.key ?: "netcat_title")
                                "tools" -> t("tools")
                                "connection_settings" -> t("connection_settings")
                                "preferences" -> t("preferences")
                                "shop" -> t("shop")
                                "debugger" -> t("debugger_title")
                                else -> t("settings")
                            })
                        )
                    }
                },
                navigationIcon = {
                    when (screenKey) {
                        "manual" -> BounceIconButton(onClick = { showManual = false; editingConfig = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "export" -> BounceIconButton(onClick = { exportConfigs = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "picker" -> BounceIconButton(onClick = { showPicker = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "projects" -> BounceIconButton(onClick = { showProjects = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "tornodes" -> BounceIconButton(onClick = { showTorNodes = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "windscribe" -> BounceIconButton(onClick = { showWindscribe = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "scanqr" -> BounceIconButton(onClick = { showScanner = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "usage" -> BounceIconButton(onClick = { usageDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "perapp" -> BounceIconButton(onClick = { perAppDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "logs" -> BounceIconButton(onClick = { logsDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "stability" -> BounceIconButton(onClick = { stabilityDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "about" -> BounceIconButton(onClick = { aboutDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "theme" -> BounceIconButton(onClick = { themeDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "cleanip" -> BounceIconButton(onClick = { cleanIpDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "netmon" -> BounceIconButton(onClick = { netMonDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "netcat" -> BounceIconButton(onClick = { netCatDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "checkhost" -> BounceIconButton(onClick = { checkHostDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "netcatone" -> BounceIconButton(onClick = { netCatIndex = -1 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "tools" -> BounceIconButton(onClick = { toolsDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "connection_settings" -> BounceIconButton(onClick = { connDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        "preferences" -> BounceIconButton(onClick = { prefsDetail = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                },
                actions = {
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
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    icon = { Icon(Icons.Filled.ShoppingBag, contentDescription = null) },
                    label = { Text(t("shop")) }
                )
                NavigationBarItem(
                    selected = page == 1,
                    onClick = {
                        showPicker = false; showManual = false; showProjects = false; showTorNodes = false; showWindscribe = false; editingConfig = null
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                    label = { Text(t("home")) }
                )
                NavigationBarItem(
                    selected = page == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    icon = { Icon(Icons.Filled.BugReport, contentDescription = null) },
                    label = { Text(t("debugger")) }
                )
                NavigationBarItem(
                    selected = page == 3,
                    onClick = {
                        usageDetail = false
                        perAppDetail = false
                        logsDetail = false
                        stabilityDetail = false
                        aboutDetail = false
                        themeDetail = false
                        cleanIpDetail = false
                        netMonDetail = false
                        netCatDetail = false
                        netCatIndex = -1
                        checkHostDetail = false
                        toolsDetail = false
                        connDetail = false
                        prefsDetail = false
                        scope.launch { pagerState.animateScrollToPage(3) }
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
                ShopScreen()
            } else if (p == 1) {
                val connKey = when {
                    exportConfigs != null -> "export"
                    showManual -> "manual"
                    showScanner -> "scanqr"
                    showWindscribe -> "windscribe"
                    showTorNodes -> "tornodes"
                    showProjects -> "projects"
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
                            sortMode = sortMode,
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
                            onFreeProjects = { showProjects = true },
                            onWindscribe = { showWindscribe = true },
                            onScanQr = { showScanner = true },
                            onShareFile = { exportConfigs = it }
                        )
                        "projects" -> FreeProjectsScreen(
                            store = store,
                            onOpenTor = { showTorNodes = true }
                        )
                        "windscribe" -> WindscribeScreen(store = store)
                        "scanqr" -> QrScannerScreen(
                            onResult = { text ->
                                showScanner = false
                                ImportBus.offerScan(text)
                            }
                        )
                        "tornodes" -> TorNodesScreen(store = store)
                        else -> ConnectionScreen(
                            store = store,
                            selectedId = selectedId,
                            onOpenPicker = { showPicker = true },
                            onConnect = onConnect,
                            onDisconnect = onDisconnect
                        )
                    }
                }
            } else if (p == 2) {
                ConfigDebuggerScreen(
                    store = store,
                    onSwitch = onSwitch,
                    active = pagerState.settledPage == 2 && !pagerState.isScrollInProgress
                )
            } else {
                val setKey = when {
                    usageDetail -> "usage"
                    perAppDetail -> "perapp"
                    logsDetail -> "logs"
                    stabilityDetail -> "stability"
                    aboutDetail -> "about"
                    themeDetail -> "theme"
                    cleanIpDetail -> "cleanip"
                    checkHostDetail -> "checkhost"
                    netCatIndex >= 0 -> "netcatone"
                    netCatDetail -> "netcat"
                    netMonDetail -> "netmon"
                    toolsDetail -> "tools"
                    connDetail -> "connection_settings"
                    prefsDetail -> "preferences"
                    else -> "settings"
                }
                AnimatedContent(
                    targetState = setKey,
                    transitionSpec = {
                        if (settingsDepth(targetState) > settingsDepth(initialState)) {
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
                        "theme" -> ThemeSettingsScreen(store = store)
                        "cleanip" -> CleanIpScreen()
                        "netmon" -> NetMonitorScreen(onOpenCategories = { netCatDetail = true })
                        "netcat" -> NetCategoriesScreen(onOpen = { netCatIndex = it })
                        "checkhost" -> CheckHostScreen()
                        "netcatone" -> NetCategoryScreen(index = netCatIndex)
                        "tools" -> ToolsScreen(
                            store = store,
                            onOpenCheckHost = { checkHostDetail = true },
                            onOpenStability = { stabilityDetail = true },
                            onOpenCleanIp = { cleanIpDetail = true }
                        )
                        "connection_settings" -> ConnectionSettingsScreen(
                            store = store,
                            onOpenPerApp = { perAppDetail = true },
                            onOpenLogs = { logsDetail = true }
                        )
                        "preferences" -> PreferencesScreen(
                            store = store,
                            onOpenTheme = { themeDetail = true }
                        )
                        else -> SettingsScreen(
                            store = store,
                            scrollState = settingsScroll,
                            onOpenUsage = { usageDetail = true },
                            onOpenTools = { toolsDetail = true },
                            onOpenConnection = { connDetail = true },
                            onOpenPreferences = { prefsDetail = true },
                            onOpenAbout = { aboutDetail = true },
                            onOpenNetMon = { netMonDetail = true }
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
    val activeCfgId by VpnState.activeId.collectAsState()

    val mixedPortValue by store.mixedPort.collectAsState()
    LaunchedEffect(mixedPortValue) { MixedPort.value = mixedPortValue }

    LaunchedEffect(activeCfgId, configs) {
        UsageStore.currentConfigKey = configs.find { it.id == activeCfgId }?.name
    }

    LaunchedEffect(conn) {
        val off = conn != Connection.CONNECTED && conn != Connection.CONNECTING
        if (android.net.TrafficStats.getTotalRxBytes() == android.net.TrafficStats.UNSUPPORTED.toLong())
            return@LaunchedEffect
        UsageStore.syncDirect(
            android.net.TrafficStats.getTotalRxBytes(),
            android.net.TrafficStats.getTotalTxBytes(),
            off
        )
        if (!off) return@LaunchedEffect
        while (isActive) {
            delay(5000)
            UsageStore.syncDirect(
                android.net.TrafficStats.getTotalRxBytes(),
                android.net.TrafficStats.getTotalTxBytes(),
                true
            )
        }
    }
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
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenPicker() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                t("selected_server"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                repeat(3) {
                                    Box(
                                        Modifier.size(8.dp).background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                            CircleShape
                                        )
                                    )
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                if (selectedConfig != null) {
                                    val cursorT by rememberInfiniteTransition(label = "cursor").animateFloat(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(tween(1060, easing = LinearEasing)),
                                        label = "cursorBlink"
                                    )
                                    val fullName = selectedConfig.name
                                    var typed by remember(fullName) { mutableStateOf(0) }
                                    LaunchedEffect(fullName) {
                                        typed = 0
                                        if (fullName.isNotEmpty()) {
                                            val per = (1100L / fullName.length).coerceAtLeast(30L)
                                            while (typed < fullName.length) {
                                                delay(per)
                                                typed++
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            flagRuns(fullName.take(typed), monoLatinFont()),
                                            inlineContent = flagInlineContent(
                                                fullName.take(typed),
                                                MaterialTheme.typography.titleMedium.fontSize
                                            ),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            Modifier.padding(start = 3.dp)
                                                .size(width = 8.dp, height = 17.dp)
                                                .graphicsLayer { alpha = if (cursorT < 0.5f) 1f else 0f }
                                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                                        )
                                    }
                                    Spacer(Modifier.height(5.dp))
                                    if (selectedConfig.locked) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.Lock, contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                monoText(t("locked_config")),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Text(
                                            monoText(n("${selectedConfig.address}:${selectedConfig.port}")),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    val idleBlink by rememberInfiniteTransition(label = "idleCursor").animateFloat(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(tween(1060, easing = LinearEasing)),
                                        label = "idleCursorBlink"
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            monoText(t("tap_choose")),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            Modifier.padding(start = 3.dp)
                                                .size(width = 8.dp, height = 15.dp)
                                                .graphicsLayer { alpha = if (idleBlink < 0.5f) 1f else 0f }
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
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
                    val netOffline = rememberInternetOffline()
                    val alive by TunnelHealth.alive.collectAsState()
                    val deadTunnel = conn == Connection.CONNECTED && alive == false
                    val stateTint by animateColorAsState(
                        when {
                            netOffline || deadTunnel -> Color(0xFFE0413C)
                            conn == Connection.CONNECTING -> Color(0xFFFFA94D)
                            connected -> AppGreen
                            else -> MaterialTheme.colorScheme.primary
                        },
                        tween(450),
                        label = "connTint"
                    )
                    val enabled = connected || selectedConfig != null
                    val press by animateFloatAsState(
                        if (btnPressed && enabled) 0.97f else 1f,
                        tween(140, easing = FastOutSlowInEasing),
                        label = "connPress"
                    )
                    Box(
                        Modifier.matchParentSize()
                            .graphicsLayer { scaleX = press; scaleY = press }
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        stateTint.copy(alpha = 0.18f),
                                        stateTint.copy(alpha = 0.30f),
                                        stateTint.copy(alpha = 0.18f)
                                    )
                                )
                            )
                            .border(1.6.dp, stateTint.copy(alpha = 0.70f), RoundedCornerShape(20.dp))
                            .clickable(enabled = enabled) {
                                if (connected) onDisconnect()
                                else selectedConfig?.let { onConnect(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ConnectSweep(
                            color = stateTint,
                            active = conn == Connection.CONNECTING,
                            modifier = Modifier.matchParentSize()
                        )
                        AnimatedContent(
                            targetState = conn,
                            transitionSpec = {
                                (slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it / 2 } +
                                        fadeIn(tween(340))) togetherWith
                                        (slideOutVertically(tween(340, easing = FastOutSlowInEasing)) { -it / 2 } +
                                                fadeOut(tween(200)))
                            },
                            label = "connLabel",
                            modifier = Modifier.fillMaxSize()
                        ) { state ->
                            Row(
                                Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when {
                                        state == Connection.CONNECTING -> Icons.Filled.Autorenew
                                        state == Connection.CONNECTED -> Icons.Filled.PowerSettingsNew
                                        else -> Icons.Filled.Bolt
                                    },
                                    contentDescription = null,
                                    tint = stateTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    when {
                                        state == Connection.CONNECTING -> t("connecting_cancel")
                                        state == Connection.CONNECTED -> t("disconnect")
                                        else -> t("connect")
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = stateTint,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
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

                val globeStyle by store.globeStyle.collectAsState()
                if (globeStyle == "dots") {
                    DotGlobeSection(Modifier.weight(1f).fillMaxWidth())
                } else {
                    EarthSection(Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigPickerScreen(
    store: ConfigStore,
    selectedId: String?,
    sortMode: String,
    pings: SnapshotStateMap<String, PingResult>,
    onSelect: (String) -> Unit,
    onEdit: (ProxyConfig) -> Unit,
    onAddManually: () -> Unit,
    onFreeProjects: () -> Unit,
    onWindscribe: () -> Unit,
    onScanQr: () -> Unit,
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

    var subStatus by remember { mutableStateOf("") }
    var addBusy by remember { mutableStateOf(false) }
    var addDone by remember { mutableStateOf("") }
    var testAllState by remember { mutableStateOf(0) }
    var addMenu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    var purgeMenu by remember { mutableStateOf(false) }
    var confirmPurgeManual by remember { mutableStateOf(false) }
    var confirmPurgeDupes by remember { mutableStateOf(false) }
    var confirmPurgeAll by remember { mutableStateOf(false) }
    var confirmPurgeDead by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var pingingSubs by remember { mutableStateOf(emptySet<String>()) }
    var query by remember { mutableStateOf("") }
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
    var chainFor by remember { mutableStateOf<ProxyConfig?>(null) }
    var openActionsId by remember { mutableStateOf<String?>(null) }

    val allIds = remember(configs) { configs.map { it.id }.toSet() }

    fun sortMaybe(list: List<ProxyConfig>): List<ProxyConfig> = when (sortMode) {
        ConfigStore.SORT_FASTEST -> list.sortedBy { pingRank(pings[it.id]) }
        ConfigStore.SORT_ALPHA -> list.sortedBy { it.name.lowercase() }
        else -> list
    }
    val pingSortKey = if (sortMode == ConfigStore.SORT_FASTEST) {
        remember(configs, pings.toList()) {
            configs.joinToString(",") { "${it.id}:${pingRank(pings[it.id])}" }
        }
    } else 0
    val q = query.trim()
    val grouped = remember(configs, subscriptions, sortMode, pingSortKey, q) {
        subscriptions.map { sub ->
            val all = sortMaybe(configs.filter { it.subId == sub.id })
            sub to when {
                q.isEmpty() || sub.name.contains(q, true) -> all
                else -> all.filter { it.name.contains(q, true) }
            }
        }.filter { (sub, list) -> q.isEmpty() || list.isNotEmpty() || sub.name.contains(q, true) }
            .sortedByDescending { (sub, _) -> WindscribeBrand.isWindscribe(sub) }
    }
    val loose = remember(configs, sortMode, pingSortKey, q) {
        sortMaybe(configs.filter { it.subId.isEmpty() && (q.isEmpty() || it.name.contains(q, true)) })
    }
    fun displayedOrder(): List<String> = buildList {
        grouped.forEach { (sub, cfgs) -> if (sub.id in expandedSubs || q.isNotEmpty()) cfgs.forEach { add(it.id) } }
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

    fun doAdd(raw: String) {
        val text = raw.trim()
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
                        }
                    } catch (e: SubscriptionError) {
                        addDone = when (e.kind) {
                            SubscriptionError.Kind.HTTP ->
                                n(t("sub_err_http").format(e.code))
                            SubscriptionError.Kind.EMPTY -> t("sub_err_empty")
                            SubscriptionError.Kind.CLASH -> t("sub_err_clash")
                            SubscriptionError.Kind.NOT_CONFIG -> t("sub_err_notconfig")
                        }
                    } catch (e: Exception) {
                        addDone = t("fetch_failed")
                    } finally {
                        addBusy = false
                    }
                }
            }
            else -> {
                val parsed = ConfigParser.parseBundle(text)
                if (parsed.isEmpty()) {
                    addDone = t("parse_none")
                } else {
                    parsed.forEach { store.add(it) }
                    addDone = n(t("added_configs").format(parsed.size))
                }
            }
        }
    }

    val scanned by ImportBus.scanned.collectAsState()
    LaunchedEffect(scanned) {
        scanned?.let { text ->
            ImportBus.clearScan()
            if (ConfigParser.parseBundle(text).isEmpty() &&
                !text.startsWith("http://") && !text.startsWith("https://")
            ) {
                addDone = t("qr_invalid")
            } else {
                doAdd(text)
            }
        }
    }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AddServerPanel(
            expanded = addMenu,
            busy = addBusy,
            onToggle = { addMenu = !addMenu },
            onPaste = {
                addMenu = false
                val clip = clipboard.getText()?.text
                if (clip.isNullOrBlank()) subStatus = t("clipboard_empty")
                else if (!addBusy) doAdd(clip)
            },
            onManual = { addMenu = false; onAddManually() },
            onImport = { addMenu = false; filePicker.launch(arrayOf("*/*")) },
            onProjects = { addMenu = false; onFreeProjects() },
            onWindscribe = { addMenu = false; onWindscribe() },
            onScanQr = { addMenu = false; onScanQr() }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        pings[cfg.id] = if (cfg.protocol.trim().lowercase() == "ikev2") {
                                            Pinger.pingIke(cfg.address)
                                        } else {
                                            val ms = withContext(Dispatchers.IO) {
                                                Gozarcore.measureDelay(ConfigBuilder.buildForTest(cfg))
                                            }
                                            if (ms >= 0) PingResult.Ok(ms.toInt())
                                            else PingResult.Failed
                                        }
                                    }
                                }
                            }
                            jobs.joinAll()
                            testAllState = 2
                        }
                    }
                },
                minHeight = 42.dp,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Icon(painterResource(R.drawable.signal), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    when (testAllState) {
                        1 -> t("testing")
                        2 -> t("test_completed")
                        else -> t("test_all")
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                BounceOutlinedButton(
                    onClick = { purgeMenu = true },
                    minHeight = 42.dp,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteSweep,
                        contentDescription = t("delete_all"),
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = purgeMenu,
                    onDismissRequest = { purgeMenu = false },
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    DropdownMenuItem(
                        text = { Text(t("delete_manual_configs"), style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Filled.EditOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = {
                            purgeMenu = false
                            if (configs.none { it.subId.isBlank() }) addDone = t("no_manual")
                            else confirmPurgeManual = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(t("delete_timed_out"), style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Filled.TimerOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = {
                            purgeMenu = false
                            if (configs.none { pings[it.id] == PingResult.Failed }) {
                                addDone = t("no_timed_out")
                            } else confirmPurgeDead = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(t("delete_duplicates"), style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = {
                            purgeMenu = false
                            if (store.duplicateIds().isEmpty()) addDone = t("no_duplicates")
                            else confirmPurgeDupes = true
                        }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                t("delete_everything"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = { purgeMenu = false; confirmPurgeAll = true }
                    )
                }
            }

            Box {
                BounceOutlinedButton(
                    onClick = { sortMenu = true },
                    minHeight = 42.dp,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = t("sort"), modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = sortMenu,
                    onDismissRequest = { sortMenu = false },
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    listOf(
                        ConfigStore.SORT_ALPHA to t("sort_alpha"),
                        ConfigStore.SORT_FASTEST to t("sort_fastest"),
                        ConfigStore.SORT_ADDED to t("sort_added")
                    ).forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = {
                                if (sortMode == mode)
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(40.dp),
                            onClick = { store.setSortMode(mode); sortMenu = false }
                        )
                    }
                }
            }

            BounceOutlinedButton(
                onClick = {
                    searchOpen = !searchOpen
                    if (!searchOpen) query = ""
                },
                minHeight = 42.dp,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    if (searchOpen) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = t("search_servers"),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = searchOpen,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text(t("search_servers")) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        val statusLine = when {
            addBusy -> t("adding")
            addDone.isNotEmpty() -> addDone
            else -> subStatus
        }
        val badLines = remember(lang) {
            setOf(
                t("fetch_failed"), t("parse_none"), t("no_configs"), t("clipboard_empty"),
                t("qr_invalid"), t("no_timed_out"), t("import_bad_file"),
                t("import_wrong_password"), t("import_foreign_app"), t("ws_fetch_failed")
            )
        }
        val isBad = statusLine.isNotEmpty() &&
                badLines.any { it.isNotEmpty() && statusLine.startsWith(it) }
        val statusAccent =
            if (isBad) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        AnimatedVisibility(
            visible = statusLine.isNotEmpty(),
            enter = fadeIn(tween(220)) + expandVertically(tween(260, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(260, easing = FastOutSlowInEasing)) { -it / 3 },
            exit = fadeOut(tween(160)) + shrinkVertically(tween(220, easing = FastOutSlowInEasing)) +
                    slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { -it / 3 }
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    Modifier.clip(RoundedCornerShape(14.dp))
                        .background(statusAccent.copy(alpha = 0.10f))
                        .border(1.dp, statusAccent.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (addBusy) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = statusAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            if (isBad) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = statusAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        mixedText(statusLine),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val wsRowColor = windscribeRowColor()

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
                val wsRow = if (WindscribeBrand.isWindscribe(sub)) wsRowColor else null
                item(key = "sub-${sub.id}") {
                    SubscriptionHeader(
                        sub = sub,
                        isOpen = sub.id in expandedSubs || q.isNotEmpty(),
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
                        timedOutCount = subConfigs.count { pings[it.id] == PingResult.Failed },
                        onRemoveTimedOut = {
                            val dead = subConfigs.filter { pings[it.id] == PingResult.Failed }
                                .map { it.id }.toSet()
                            store.deleteConfigsByIds(dead)
                            dead.forEach { pings.remove(it); selected.remove(it) }
                            addDone = n(t("deleted_n").format(dead.size))
                        },
                        pinging = sub.id in pingingSubs,
                        onPing = {
                            if (sub.id !in pingingSubs && subConfigs.isNotEmpty()) {
                                pingingSubs = pingingSubs + sub.id
                                subConfigs.forEach { pings[it.id] = PingResult.Testing }
                                scope.launch {
                                    val sem = Semaphore(4)
                                    subConfigs.map { cfg ->
                                        launch {
                                            sem.withPermit {
                                                pings[cfg.id] = if (cfg.protocol.trim().lowercase() == "ikev2") {
                                                    Pinger.pingIke(cfg.address)
                                                } else {
                                                    val ms = withContext(Dispatchers.IO) {
                                                        Gozarcore.measureDelay(
                                                            ConfigBuilder.buildForTest(cfg)
                                                        )
                                                    }
                                                    if (ms >= 0) PingResult.Ok(ms.toInt())
                                                    else PingResult.Failed
                                                }
                                            }
                                        }
                                    }.joinAll()
                                    pingingSubs = pingingSubs - sub.id
                                }
                            }
                        },
                        modifier = Modifier.animateItem(fadeInSpec = tween(300), placementSpec = tween(300), fadeOutSpec = tween(200))
                    )
                }
                if (sub.id in expandedSubs || q.isNotEmpty()) {
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
                            onChain = { chainFor = cfg },
                            actionsOpen = openActionsId == cfg.id,
                            onToggleActions = {
                                openActionsId = if (openActionsId == cfg.id) null else cfg.id
                            },
                            modifier = Modifier.animateItem(fadeInSpec = tween(300), placementSpec = tween(300), fadeOutSpec = tween(200)),
                            containerColor = wsRow
                        )
                    }
                }
            }

            if (loose.isNotEmpty()) {
                item(key = "loose-header") {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp)
                            .animateItem(
                                fadeInSpec = tween(300),
                                placementSpec = tween(300),
                                fadeOutSpec = tween(200)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            Modifier.clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                n("${t("manual_configs")} (${loose.size})"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
                        onChain = { chainFor = cfg },
                        actionsOpen = openActionsId == cfg.id,
                        onToggleActions = {
                            openActionsId = if (openActionsId == cfg.id) null else cfg.id
                        },
                        modifier = Modifier.animateItem(fadeInSpec = tween(300), placementSpec = tween(300), fadeOutSpec = tween(200))
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selectionMode,
            enter = fadeIn(tween(220)) + expandVertically(tween(220)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
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
    }

    chainFor?.let { target ->
        ChainPickerDialog(
            store = store,
            config = configs.find { it.id == target.id } ?: target,
            onDismiss = { chainFor = null }
        )
    }

    if (confirmPurgeManual) {
        val manual = remember(configs) {
            configs.filter { it.subId.isBlank() }.map { it.id }.toSet()
        }
        GlassDialog(
            onDismiss = { confirmPurgeManual = false },
            title = t("delete_manual_configs"),
            confirmLabel = t("delete"),
            dismissLabel = t("cancel"),
            destructive = true,
            onConfirm = {
                store.deleteConfigsByIds(manual)
                manual.forEach { pings.remove(it); selected.remove(it) }
                addDone = n(t("deleted_n").format(manual.size))
                confirmPurgeManual = false
            }
        ) {
            Text(
                n(t("delete_manual_q").format(manual.size)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (confirmPurgeDupes) {
        val dupes = remember(configs) { store.duplicateIds() }
        GlassDialog(
            onDismiss = { confirmPurgeDupes = false },
            title = t("delete_duplicates"),
            confirmLabel = t("delete"),
            dismissLabel = t("cancel"),
            destructive = true,
            onConfirm = {
                store.deleteConfigsByIds(dupes)
                dupes.forEach { pings.remove(it); selected.remove(it) }
                addDone = n(t("deleted_n").format(dupes.size))
                confirmPurgeDupes = false
            }
        ) {
            Text(
                n(t("delete_duplicates_q").format(dupes.size)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (confirmPurgeAll) {
        GlassDialog(
            onDismiss = { confirmPurgeAll = false },
            title = t("delete_everything"),
            confirmLabel = t("delete"),
            destructive = true,
            onConfirm = {
                val removed = configs.size
                store.deleteAllConfigs()
                selected.clear()
                selectionMode = false
                addDone = n(t("deleted_n").format(removed))
                confirmPurgeAll = false
            }
        ) {
            Text(
                n(t("delete_everything_q").format(configs.size)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (confirmPurgeDead) {
        val dead = remember(configs, pings.toMap()) {
            configs.filter { pings[it.id] == PingResult.Failed }.map { it.id }.toSet()
        }
        GlassDialog(
            onDismiss = { confirmPurgeDead = false },
            title = t("delete_timed_out"),
            confirmLabel = t("delete"),
            destructive = true,
            onConfirm = {
                store.deleteConfigsByIds(dead)
                dead.forEach { pings.remove(it); selected.remove(it) }
                addDone = n(t("deleted_n").format(dead.size))
                confirmPurgeDead = false
            }
        ) {
            Text(
                n(t("delete_timeout_q").format(dead.size)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (confirmDelete) {
        GlassDialog(
            onDismiss = { confirmDelete = false },
            title = t("delete"),
            confirmLabel = t("delete"),
            dismissLabel = t("cancel"),
            destructive = true,
            onConfirm = {
                configs.filter { selected.containsKey(it.id) }
                    .forEach { store.delete(it.id); pings.remove(it.id) }
                clearSel()
                confirmDelete = false
            }
        ) {
            Text(t("delete_selected_q"), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ExportConfigScreen(
    configs: List<ProxyConfig>,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val multi = configs.size > 1

    val defaultName = if (multi) "GRoute-configs" else (configs.firstOrNull()?.name?.ifBlank { "config" } ?: "config")
    var fileName by remember { mutableStateOf(defaultName) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var lockDetails by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoBox(t("export_encrypted_note"))

        if (multi) {
            InfoBox(localizeDigits(t("export_count").format(configs.size), lang))
        }

        SettingsGroup {
            OutlinedTextField(
                fileName,
                { fileName = it },
                label = { Text(t("export_file_name")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontFamily = monoLatinFont()),
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
                textStyle = LocalTextStyle.current.copy(fontFamily = monoLatinFont()),
                trailingIcon = {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = t("show"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(CircleShape)
                            .clickable { showPassword = !showPassword }
                            .padding(8.dp)
                            .size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            SettingRow(
                title = t("export_lock_details"),
                subtitle = if (lockDetails) t("export_locked_note") else t("export_unlocked_note"),
                checked = lockDetails,
                onCheckedChange = { lockDetails = it }
            )
        }

        AnimatedVisibility(
            visible = error.isNotEmpty(),
            enter = fadeIn(tween(200)) + expandVertically(tween(240, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200, easing = FastOutSlowInEasing))
        ) {
            InfoBox(error, accent = MaterialTheme.colorScheme.error, centered = true)
        }

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
                                val bytes = ConfigFile.encode(
                                    context, configs, password.ifBlank { null }, lockDetails
                                )
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
                if (busy) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.InsertDriveFile,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(t("export_continue"), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
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
    var hyObfsPassword by remember { mutableStateOf(existing?.hyObfsPassword ?: "") }
    var hyUp by remember { mutableStateOf(if ((existing?.hyUpMbps ?: 0) > 0) "${existing?.hyUpMbps}" else "") }
    var hyDown by remember { mutableStateOf(if ((existing?.hyDownMbps ?: 0) > 0) "${existing?.hyDownMbps}" else "") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(name, { name = it }, label = { Text(t("name_optional")) }, singleLine = true, textStyle = LocalTextStyle.current.copy(fontFamily = scriptFont(name)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        LabeledDropdown(t("protocol"), listOf("vless", "vmess", "trojan", "shadowsocks", "hysteria2", "wireguard", "ikev2", "socks", "http"), protocol) { protocol = it }
        OutlinedTextField(address, { address = it }, label = { Text(t("address")) }, singleLine = true, textStyle = LocalTextStyle.current.copy(fontFamily = monoFont()), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        if (protocol != "ikev2") OutlinedTextField(
            port, { port = it.filter { c -> c.isDigit() } },
            label = { Text(t("port")) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (protocol == "vless" || protocol == "vmess")
            OutlinedTextField(uuid, { uuid = it }, label = { Text(t("uuid")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        if (protocol == "ikev2") {
            OutlinedTextField(uuid, { uuid = it }, label = { Text(t("ikev2_user")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text(t("password")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sni, { sni = it }, label = { Text(t("ikev2_remote_id")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Text(
                t("ikev2_note"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (protocol == "trojan" || protocol == "shadowsocks" || protocol == "hysteria2")
            OutlinedTextField(password, { password = it }, label = { Text(t("password")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        if (protocol == "hysteria2") {
            OutlinedTextField(hyObfsPassword, { hyObfsPassword = it }, label = { Text(t("hy_obfs")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    hyUp, { hyUp = it.filter { c -> c.isDigit() } },
                    label = { Text(t("hy_up")) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    hyDown, { hyDown = it.filter { c -> c.isDigit() } },
                    label = { Text(t("hy_down")) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)
                )
            }
        }
        if (protocol == "shadowsocks")
            LabeledDropdown(t("enc_method"),
                listOf("aes-256-gcm", "aes-128-gcm", "chacha20-ietf-poly1305", "2022-blake3-aes-256-gcm"), method) { method = it }
        if (protocol == "vless")
            OutlinedTextField(flow, { flow = it }, label = { Text(t("flow_optional")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())

        if (protocol == "hysteria2") {
            OutlinedTextField(sni, { sni = it }, label = { Text(t("sni")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(alpn, { alpn = it }, label = { Text(t("alpn")) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
        }

        if (protocol !in setOf("shadowsocks", "hysteria2", "wireguard", "ikev2")) {
            LabeledDropdown(t("network"), listOf("tcp", "ws", "grpc", "http", "httpupgrade", "xhttp"), network) { network = it }
            LabeledDropdown(t("security"), listOf("none", "tls", "reality"), security) { security = it }
            if (security == "tls" || security == "reality") {
                OutlinedTextField(sni, { sni = it }, label = { Text(t("sni")) }, singleLine = true, textStyle = LocalTextStyle.current.copy(fontFamily = monoFont()), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
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
                    val p = if (protocol == "ikev2") 500 else port.toIntOrNull()
                    when {
                        address.isBlank() -> error = t("err_address")
                        p == null || p !in 1..65535 -> error = t("err_port")
                        (protocol == "vless" || protocol == "vmess") && uuid.isBlank() -> error = t("err_uuid")
                        protocol == "ikev2" && uuid.isBlank() -> error = t("err_uuid")
                        (protocol == "trojan" || protocol == "shadowsocks" || protocol == "hysteria2" ||
                                protocol == "ikev2") && password.isBlank() -> error = t("err_password")
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
                                network = when (protocol) {
                                    "shadowsocks" -> "tcp"
                                    "hysteria2" -> "hysteria"
                                    "ikev2" -> "ikev2"
                                    else -> network
                                },
                                security = when (protocol) {
                                    "shadowsocks" -> "none"
                                    "hysteria2" -> "tls"
                                    "ikev2" -> "none"
                                    else -> security
                                },
                                sni = sni.trim(),
                                publicKey = publicKey.trim(),
                                shortId = shortId.trim(),
                                path = path.trim(),
                                host = host.trim(),
                                serviceName = serviceName.trim(),
                                mode = mode.trim(),
                                alpn = alpn.trim(),
                                fingerprint = fingerprint.trim(),
                                hyObfs = if (hyObfsPassword.isBlank()) "" else "salamander",
                                hyObfsPassword = hyObfsPassword.trim(),
                                hyUpMbps = hyUp.toIntOrNull() ?: 0,
                                hyDownMbps = hyDown.toIntOrNull() ?: 0
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
private fun AddServerPanel(
    expanded: Boolean,
    busy: Boolean,
    onToggle: () -> Unit,
    onPaste: () -> Unit,
    onManual: () -> Unit,
    onImport: () -> Unit,
    onProjects: () -> Unit,
    onWindscribe: () -> Unit,
    onScanQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val rot by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "addRot"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    monoText(t("add_server")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )
                BounceOutlinedButton(
                    onClick = onToggle,
                    enabled = !busy,
                    minHeight = 44.dp,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = t("add_server"),
                        modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = rot }
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Column(
                    Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AddTile(Icons.Filled.ContentPaste, t("paste_clipboard"), onPaste, Modifier.weight(1f))
                        AddTile(Icons.Filled.Add, t("add_manually"), onManual, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AddTile(Icons.Filled.UploadFile, t("import_from_file"), onImport, Modifier.weight(1f))
                        AddTile(
                            Icons.Filled.QrCodeScanner, t("scan_qr"), onScanQr, Modifier.weight(1f)
                        )
                    }
                    AddTile(
                        Icons.Filled.Shield, t("ws_title"), onWindscribe, Modifier.fillMaxWidth()
                    )
                    AddTile(
                        Icons.Filled.CardGiftcard, t("free_projects"), onProjects, Modifier.fillMaxWidth(),
                        accent = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    var textW by remember(label) { mutableStateOf<Dp?>(null) }

    BounceOutlinedButton(
        onClick = onClick,
        minHeight = 60.dp,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        accent = accent,
        modifier = modifier.height(60.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { r ->
                var widest = 0f
                for (i in 0 until r.lineCount) {
                    val lw = r.getLineRight(i) - r.getLineLeft(i)
                    if (lw > widest) widest = lw
                }
                val want = with(density) { widest.toDp() } + 1.dp
                val have = textW
                if (have == null || want.value > have.value + 0.5f) textW = want
            },
            modifier = textW?.let { Modifier.width(it) } ?: Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun FreeProjectsScreen(
    store: ConfigStore,
    onOpenTor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var aetherMode by remember { mutableStateOf("masque") }
    var aetherH2 by remember { mutableStateOf(false) }

    LaunchedEffect(status) { if (status.isNotEmpty()) { delay(4000); status = "" } }

    Column(
        modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(t("legacy_warp"), style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    accentText(
                        t("proj_warp_desc"),
                        "use Aether instead there",
                        "\u062f\u0631 \u0627\u06cc\u0631\u0627\u0646 \u0627\u0632 Aether \u0627\u0633\u062a\u0641\u0627\u062f\u0647 \u06a9\u0646\u06cc\u062f"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BounceButton(
                    onClick = {
                        if (busy) return@BounceButton
                        busy = true; status = ""
                        scope.launch {
                            val result = withContext(Dispatchers.IO) { Warp.register() }
                            status = when (result) {
                                is Warp.Result.Success -> {
                                    result.configs.forEach { store.add(it) }
                                    t("warp_added")
                                }
                                is Warp.Result.Failure -> t("warp_failed")
                            }
                            busy = false
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (busy) t("adding") else t("add_warp"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }
        }

        SettingsGroup(t("proj_aether") + " (by @CluvexStudio)") {
            Text(
                accentText(
                    t("proj_aether_desc"),
                    "MASQUE, WireGuard and nested WireGuard tunnels",
                    "\u062a\u0648\u0646\u0644 MASQUE\u060c \u0648\u0627\u06cc\u0631\u06af\u0627\u0631\u062f \u0648 \u0648\u0627\u06cc\u0631\u06af\u0627\u0631\u062f \u062a\u0648\u062f\u0631\u062a\u0648"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("masque" to "MASQUE", "wg" to "WireGuard", "gool" to "gool").forEach { (key, label) ->
                    val on = aetherMode == key
                    BounceOutlinedButton(
                        onClick = { aetherMode = key },
                        minHeight = 40.dp,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        accent = if (on) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(mixedText(label), maxLines = 1, softWrap = false,
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            SettingRow(
                title = t("proj_aether_h2"),
                subtitle = t("proj_aether_h2_sub"),
                checked = aetherH2,
                onCheckedChange = { aetherH2 = it }
            )
            BounceButton(
                onClick = {
                    if (!AetherController.available(context)) {
                        status = t("proj_aether_missing")
                        return@BounceButton
                    }
                    val cfg = ProxyConfig(
                        name = "Aether (${aetherMode.uppercase()})",
                        protocol = "aether",
                        address = "127.0.0.1",
                        port = AetherController.SOCKS_PORT,
                        aetherMode = aetherMode,
                        aetherScan = "balanced",
                        aetherHttp2 = aetherH2,
                        source = ConfigSource.COMMUNITY
                    )
                    store.add(cfg)
                    status = t("proj_aether_added")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("add"), maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
            }
        }

        SettingsHubCard(
            iconRes = R.drawable.tor,
            title = "Tor",
            subtitle = t("proj_tor_desc"),
            onClick = onOpenTor,
            accents = listOf(
                "Slow but very resilient",
                "\u06a9\u0646\u062f \u0627\u0645\u0627 \u0628\u0633\u06cc\u0627\u0631 \u0645\u0642\u0627\u0648\u0645"
            )
        )

        AnimatedVisibility(
            visible = status.isNotEmpty(),
            enter = fadeIn(tween(220)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(160)) + shrinkVertically(tween(220, easing = FastOutSlowInEasing))
        ) {
            InfoBox(status, centered = true)
        }

        Text(
            t("proj_more_soon"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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
        Text(mixedText(label), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Modifier.appearOnce(delayMillis: Int = 0, offsetY: Float = 26f): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val spec = tween<Float>(360, delayMillis = delayMillis, easing = FastOutSlowInEasing)
    val a by animateFloatAsState(if (shown) 1f else 0f, spec, label = "appearA")
    val ty by animateFloatAsState(if (shown) 0f else offsetY, spec, label = "appearY")
    val sc by animateFloatAsState(if (shown) 1f else 0.96f, spec, label = "appearS")
    return this.graphicsLayer {
        alpha = a
        translationY = ty
        scaleX = sc
        scaleY = sc
    }
}

@Composable
private fun LabeledDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box {
            OutlinedButton(
                onClick = { open = true },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selected,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = scriptFont(selected),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(
                expanded = open,
                onDismissRequest = { open = false },
                offset = DpOffset(0.dp, 8.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                opt,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = scriptFont(opt)
                            )
                        },
                        trailingIcon = {
                            if (opt == selected) Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = { onSelect(opt); open = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun settingsDepth(key: String): Int = when (key) {
    "settings" -> 0
    "stability", "cleanip", "perapp", "theme", "netcat" -> 2
    "checkhost" -> 3
    "netcatone" -> 3
    else -> 1
}

@Composable
private fun InfoBox(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    centered: Boolean = false
) {
    Box(
        modifier.fillMaxWidth(),
        contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            mixedText(text),
            style = MaterialTheme.typography.bodySmall,
            color = accent,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.08f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun GlassDialog(
    onDismiss: () -> Unit,
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String? = null,
    destructive: Boolean = false,
    accentOverride: Color? = null,
    body: @Composable ColumnScope.() -> Unit
) {
    val accent = accentOverride
        ?: if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
                        BounceOutlinedButton(
                            onClick = onDismiss,
                            minHeight = 42.dp,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(dismissLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                        }
                    }
                    BounceOutlinedButton(
                        onClick = onConfirm,
                        minHeight = 42.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(confirmLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopScreen(modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val plans = listOf(
        Triple("۱ ماهه · 30 GB", "۱۲۰,۰۰۰", "1 month · 30 GB"),
        Triple("۲ ماهه · 80 GB", "۲۲۰,۰۰۰", "2 months · 80 GB"),
        Triple("۳ ماهه · 150 GB", "۳۲۰,۰۰۰", "3 months · 150 GB"),
        Triple("۶ ماهه · 400 GB", "۵۹۰,۰۰۰", "6 months · 400 GB"),
        Triple("۱ ساله · نامحدود", "۹۹۰,۰۰۰", "1 year · Unlimited"),
        Triple("۲ ساله · نامحدود", "۱,۷۹۰,۰۰۰", "2 years · Unlimited"),
        Triple("اشتراک خانواده", "۱,۲۹۰,۰۰۰", "Family plan"),
        Triple("اشتراک نمایندگی", "۲,۹۹۰,۰۰۰", "Reseller plan")
    )

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().blur(14.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            plans.forEach { (fa, price, en) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (lang == Lang.FA) fa else en,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (lang == Lang.FA) "تحویل آنی" else "Instant delivery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            price + if (lang == Lang.FA) " تومان" else " T",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Box(
            Modifier.fillMaxSize().background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.35f)
            )
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    t("shop_soon"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    t("shop_soon_sub"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WindscribeScreen(store: ConfigStore, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var nodes by remember { mutableStateOf<List<WindscribeNode>>(emptyList()) }
    val picked = remember { mutableStateMapOf<String, Boolean>() }
    var status by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (nodes.isEmpty()) {
            loading = true
            nodes = WindscribeFetcher.fetch()
            loading = false
            if (nodes.isEmpty()) status = t("ws_fetch_failed")
        }
    }

    val shown = remember(nodes, query) {
        if (query.isBlank()) nodes
        else nodes.filter {
            it.label.contains(query, true) || it.hostname.contains(query, true) ||
                    it.country.contains(query, true)
        }
    }
    val grouped = remember(shown) {
        shown.groupBy { it.country.ifBlank { "?" } }.toList().sortedBy { it.first }
    }
    val open = remember { mutableStateMapOf<String, Boolean>() }
    var locationsOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    val count = picked.count { it.value }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "intro") {
                Text(
                    mixedText(t("ws_intro")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item(key = "creds") {
                TorCountryGroup(t("ws_credentials")) {
                    OutlinedTextField(
                        user, { user = it },
                        label = { Text(t("ikev2_user")) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        pass, { pass = it },
                        label = { Text(t("password")) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        accentText(
                            t("ws_creds_hint"),
                            "windscribe.com/myaccount",
                            "Config Generators",
                            "IKEv2"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (loading) item(key = "loading") {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(t("ws_loading"), style = MaterialTheme.typography.bodySmall)
                }
            }

            item(key = "locations") {
                androidx.compose.animation.AnimatedVisibility(
                    visible = nodes.isNotEmpty(),
                    enter = fadeIn(tween(320)) +
                            expandVertically(tween(400, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it / 6 },
                    exit = fadeOut(tween(180)) + shrinkVertically(tween(260))
                ) {
                    WindscribeLocationsHeader(
                        title = t("ws_locations"),
                        total = nodes.size,
                        chosen = count,
                        expanded = locationsOpen,
                        searchOpen = searchOpen,
                        allSelected = shown.isNotEmpty() &&
                                shown.all { picked[it.hostname] == true },
                        onSelectAll = {
                            val target = !(shown.isNotEmpty() &&
                                    shown.all { picked[it.hostname] == true })
                            shown.forEach { node ->
                                if (target) picked[node.hostname] = true
                                else picked.remove(node.hostname)
                            }
                        },
                        onToggle = { locationsOpen = !locationsOpen },
                        onToggleSearch = {
                            searchOpen = !searchOpen
                            if (!searchOpen) query = ""
                        },
                        query = query,
                        onQuery = { query = it },
                        searchLabel = t("search_servers")
                    )
                }
            }

            if (locationsOpen && nodes.isNotEmpty()) {
                items(grouped, key = { "c-" + it.first }) { (country, servers) ->
                    val open2 = open[country] == true || query.isNotBlank()
                    val chosen2 = servers.count { picked[it.hostname] == true }
                    WindscribeCountryCard(
                        country = country,
                        total = servers.size,
                        chosen = chosen2,
                        expanded = open2,
                        onToggle = { open[country] = !open2 },
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = tween(220),
                                placementSpec = tween(220, easing = FastOutSlowInEasing),
                                fadeOutSpec = tween(140)
                            )
                            .padding(horizontal = 12.dp)
                    ) {
                        servers.forEach { node ->
                            WindscribeServerRow(
                                label = node.label,
                                selected = picked[node.hostname] == true,
                                accent = FlagColors.of(country)
                                    ?: MaterialTheme.colorScheme.primary,
                                onClick = {
                                    picked[node.hostname] = picked[node.hostname] != true
                                }
                            )
                        }
                    }
                }
            }

            if (nodes.isEmpty() && !loading) item(key = "retry") {
                BounceOutlinedButton(
                    onClick = {
                        scope.launch {
                            loading = true
                            status = ""
                            nodes = WindscribeFetcher.fetch()
                            loading = false
                            if (nodes.isEmpty()) status = t("ws_fetch_failed")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(t("netmon_recheck"), maxLines = 1)
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (status.isNotEmpty()) Text(
                mixedText(status),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            BounceButton(
                onClick = {
                    val chosen = nodes.filter { picked[it.hostname] == true }
                    if (user.isBlank() || pass.isBlank()) {
                        status = t("ws_need_creds")
                        return@BounceButton
                    }
                    if (chosen.isEmpty()) {
                        status = t("ws_need_host")
                        return@BounceButton
                    }
                    store.addToLocalSub(
                        WindscribeBrand.SUB_NAME,
                        chosen.map { node ->
                            ProxyConfig(
                                name = node.label,
                                protocol = "ikev2",
                                address = node.ip.ifBlank { node.hostname },
                                port = 500,
                                uuid = user.trim(),
                                password = pass.trim(),
                                sni = node.hostname,
                                network = "ikev2",
                                security = "none",
                                source = ConfigSource.COMMUNITY
                            )
                        }
                    )
                    picked.clear()
                    status = t("ws_added").format(chosen.size)
                },
                enabled = count > 0 && user.isNotBlank() && pass.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (count > 0) t("ws_add_n").format(localizeDigits("$count", lang))
                    else t("add"),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WindscribeLocationsHeader(
    title: String,
    total: Int,
    chosen: Int,
    expanded: Boolean,
    searchOpen: Boolean,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onToggle: () -> Unit,
    onToggleSearch: () -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    searchLabel: String
) {
    val lang = LocalLang.current
    val rot by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "wsOuterChevron"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() }
                    .padding(
                        start = 16.dp,
                        end = 8.dp,
                        top = 14.dp,
                        bottom = if (expanded && searchOpen) 2.dp else 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    mixedText(title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (chosen > 0)
                        localizeDigits("$chosen", lang) + " / " + localizeDigits("$total", lang)
                    else localizeDigits("$total", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (chosen > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(180)) + expandHorizontally(tween(200)),
                    exit = fadeOut(tween(120)) + shrinkHorizontally(tween(160))
                ) {
                    Icon(
                        if (allSelected) Icons.Filled.Deselect else Icons.Filled.SelectAll,
                        contentDescription = null,
                        tint = if (allSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                            .clip(CircleShape)
                            .clickable { onSelectAll() }
                            .padding(6.dp)
                            .size(19.dp)
                    )
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(180)) + expandHorizontally(tween(200)),
                    exit = fadeOut(tween(120)) + shrinkHorizontally(tween(160))
                ) {
                    Icon(
                        if (searchOpen) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = null,
                        tint = if (searchOpen) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onToggleSearch() }
                            .padding(6.dp)
                            .size(19.dp)
                    )
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp, end = 6.dp)
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rot }
                )
            }

            AnimatedVisibility(
                visible = expanded && searchOpen,
                enter = fadeIn(tween(180)) +
                        expandVertically(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(120)) +
                        shrinkVertically(tween(180, easing = FastOutSlowInEasing))
            ) {
                OutlinedTextField(
                    query, onQuery,
                    label = { Text(searchLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun WindscribeServerRow(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val onAccent = Color.White
    val shape = RoundedCornerShape(14.dp)
    var center by remember { mutableStateOf(Offset.Zero) }
    var sz by remember { mutableStateOf(IntSize.Zero) }

    val maxR = remember(center, sz) {
        val dx = maxOf(center.x, sz.width - center.x)
        val dy = maxOf(center.y, sz.height - center.y)
        sqrt(dx * dx + dy * dy)
    }
    val radius by animateFloatAsState(
        targetValue = if (selected) maxR else 0f,
        animationSpec = tween(if (selected) 480 else 300, easing = FastOutSlowInEasing),
        label = "wsRowFill"
    )
    val frac = if (maxR > 0f) (radius / maxR).coerceIn(0f, 1f) else 0f
    val textColor = lerp(MaterialTheme.colorScheme.onSurface, onAccent, frac)

    Box(
        Modifier.fillMaxWidth()
            .clip(shape)
            .onGloballyPositioned {
                sz = it.size
                if (center == Offset.Zero) center = Offset(it.size.width / 2f, it.size.height / 2f)
            }
            .drawBehind {
                if (radius > 0.5f) drawCircle(color = accent, radius = radius, center = center)
            }
            .border(
                1.dp,
                lerp(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    accent.copy(alpha = 0.65f),
                    frac
                ),
                shape
            )
            .pointerInput(Unit) {
                detectTapGestures(onPress = { center = it }, onTap = { onClick() })
            }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                mixedText(label),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = textColor.copy(alpha = frac),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WindscribeCountryCard(
    country: String,
    total: Int,
    chosen: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val lang = LocalLang.current
    val rot by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "wsChevron"
    )
    val accent = FlagColors.of(country) ?: MaterialTheme.colorScheme.primary
    val border by animateColorAsState(
        targetValue = if (chosen > 0) accent.copy(alpha = 0.75f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "wsBorder"
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, border)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountryFlag(country, height = 16.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    mixedText(countryName(country).ifBlank { country.uppercase() }),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (chosen > 0)
                        localizeDigits("$chosen", lang) + " / " + localizeDigits("$total", lang)
                    else localizeDigits("$total", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (chosen > 0) accent
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = rot }
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) +
                        expandVertically(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(120)) +
                        shrinkVertically(tween(180, easing = FastOutSlowInEasing))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun TorNodesScreen(store: ConfigStore, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val context = LocalContext.current
    val ready = remember { TorController.available(context) }
    var picked by remember { mutableStateOf(setOf<String>()) }
    val configs by store.configs.collectAsState()
    val selectedId by store.selectedId.collectAsState()
    val base = configs.find { it.id == selectedId && it.protocol != "tor" }
        ?: configs.firstOrNull { it.protocol != "tor" && it.protocol != "aether" }
    val baseId = base?.id ?: ""
    var throughVpn by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(status) { if (status.isNotEmpty()) { delay(3000); status = "" } }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            t("tor_nodes_intro"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!ready) {
            InfoBox(
                t("proj_tor_missing"),
                accent = MaterialTheme.colorScheme.error,
                centered = true
            )
        }

        SettingsGroup {
            SettingRow(
                title = t("tor_through_vpn"),
                subtitle = t("tor_through_vpn_sub"),
                checked = throughVpn,
                onCheckedChange = { throughVpn = it }
            )
            if (throughVpn) {
                Text(
                    if (base != null) t("tor_base_is").format(base.name) else t("tor_base_none"),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (base != null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error
                )
            }
        }

        TorCountryGroup(t("tor_exit_country")) {
            TorController.Countries.filter { it.first.isNotEmpty() }.forEach { (code, label) ->
                val on = code in picked
                val boxTint by animateColorAsState(
                    targetValue = if (on) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "torCountryTint"
                )
                val boxFill by animateFloatAsState(
                    targetValue = if (on) 1f else 0f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "torCountryFill"
                )
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(boxTint.copy(alpha = 0.05f + 0.09f * boxFill))
                        .border(
                            1.dp,
                            boxTint.copy(alpha = 0.16f + 0.36f * boxFill),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            picked = if (on) picked - code else picked + code
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CountryFlag(code, height = 14.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    SmoothCheckbox(checked = on)
                }
            }
        }

        BounceButton(
            onClick = {
                if (!ready) { status = t("proj_tor_missing"); return@BounceButton }
                val list = if (picked.isEmpty()) listOf("") else picked.toList()
                list.forEach { code ->
                    val label = TorController.Countries.firstOrNull { it.first == code }?.second
                    store.add(
                        ProxyConfig(
                            name = if (code.isEmpty()) "Tor" else "Tor - " + (label ?: code),
                            protocol = "tor",
                            address = "127.0.0.1",
                            port = TorController.SOCKS_PORT,
                            torCountry = code,
                            torThroughVpn = throughVpn,
                            torBaseId = if (throughVpn) baseId else "",
                            source = ConfigSource.COMMUNITY
                        )
                    )
                }
                status = t("proj_tor_added")
            },
            enabled = ready && (!throughVpn || base != null),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (picked.isEmpty()) t("tor_add_auto")
                else t("tor_add_n").format(localizeDigits("${picked.size}", LocalLang.current)),
                maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false
            )
        }

        AnimatedVisibility(
            visible = status.isNotEmpty(),
            enter = fadeIn(tween(220)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(160)) + shrinkVertically(tween(220, easing = FastOutSlowInEasing))
        ) {
            InfoBox(status, centered = true)
        }
    }
}

@Composable
private fun SmoothCheckbox(checked: Boolean, modifier: Modifier = Modifier) {
    val tint = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.onSurfaceVariant
    val mark = MaterialTheme.colorScheme.onPrimary
    val p by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "checkboxFill"
    )
    Canvas(modifier.size(22.dp)) {
        val stroke = 1.6.dp.toPx()
        val radius = 7.dp.toPx()
        drawRoundRect(
            color = lerp(idle.copy(alpha = 0.50f), tint, p),
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = stroke)
        )
        if (p > 0.004f) {
            val grow = size.minDimension * p
            drawRoundRect(
                color = tint.copy(alpha = p),
                topLeft = Offset((size.width - grow) / 2f, (size.height - grow) / 2f),
                size = Size(grow, grow),
                cornerRadius = CornerRadius(radius * p, radius * p)
            )
            val tick = Path().apply {
                moveTo(size.width * 0.27f, size.height * 0.52f)
                lineTo(size.width * 0.44f, size.height * 0.70f)
                lineTo(size.width * 0.75f, size.height * 0.32f)
            }
            val pm = PathMeasure().apply { setPath(tick, false) }
            val seg = Path()
            pm.getSegment(0f, pm.length * p, seg, true)
            drawPath(
                seg,
                color = mark.copy(alpha = p),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun CheckHostScreen(modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var host by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("ping") }
    var running by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var nodes by remember { mutableStateOf(listOf<CheckHost.Node>()) }
    var info by remember { mutableStateOf<CheckHost.IpInfo?>(null) }
    val results = remember { mutableStateMapOf<String, CheckHost.NodeResult>() }

    fun run() {
        val target = host.trim()
        if (running || target.isEmpty()) return
        focus.clearFocus()
        running = true
        error = ""
        nodes = emptyList()
        info = null
        results.clear()
        scope.launch {
            launch { info = CheckHost.ipInfo(target) }
            val ok = CheckHost.run(
                host = target,
                kind = kind,
                onNodes = { list ->
                    nodes = list
                    list.forEach { results[it.id] = CheckHost.NodeResult.Pending }
                },
                onResults = { map -> map.forEach { (k, v) -> results[k] = v } }
            )
            if (!ok) error = t("chk_failed")
            running = false
        }
    }

    Column(
        modifier.fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focus.clearFocus() }
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            singleLine = true,
            label = { Text(t("chk_host")) },
            placeholder = { Text(mixedText("example.com")) },
            textStyle = LocalTextStyle.current.copy(
                fontFamily = if (LocalLang.current == Lang.FA) VazirFont else LexendFont
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { run() }, onDone = { focus.clearFocus() }),
            trailingIcon = {
                if (host.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.clickable { host = ""; focus.clearFocus() }
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ping" to t("chk_ping"), "http" to t("chk_http"), "tcp" to t("chk_tcp")).forEach { (k, label) ->
                val on = kind == k
                BounceOutlinedButton(
                    onClick = { kind = k },
                    minHeight = 40.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    accent = if (on) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(mixedText(label), maxLines = 1, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        BounceButton(
            onClick = { run() },
            enabled = !running && host.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (running) t("chk_running") else t("chk_start"),
                maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false
            )
        }

        if (error.isNotEmpty()) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        info?.let { i ->
            SettingsGroup(t("chk_info")) {
                listOf(
                    t("chk_ip") to i.ip,
                    t("chk_asn") to i.asn,
                    t("chk_org") to i.org,
                    t("chk_country") to i.country,
                    t("chk_region") to i.region,
                    t("chk_city") to i.city,
                    t("chk_tz") to i.timezone
                ).filter { it.second.isNotBlank() && it.second != " ()" && it.second != ", " }
                    .forEach { (k, v) ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                k,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(96.dp)
                            )
                            if (k == t("chk_country") && i.countryCode.length == 2) {
                                CountryFlag(i.countryCode, height = 12.dp)
                                Spacer(Modifier.width(7.dp))
                            }
                            Text(
                                monoText(v),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
            }
        }

        if (nodes.isNotEmpty()) {
            SettingsGroup {
                nodes.forEach { node ->
                    val res = results[node.id] ?: CheckHost.NodeResult.Pending
                    val tint = when (res) {
                        is CheckHost.NodeResult.Ok -> AppGreen
                        is CheckHost.NodeResult.Failed -> Color(0xFFE0413C)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadarDot(tint, res is CheckHost.NodeResult.Pending)
                        Spacer(Modifier.width(8.dp))
                        Row(
                            Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (node.countryCode.length == 2) {
                                CountryFlag(node.countryCode, height = 13.dp)
                                Spacer(Modifier.width(7.dp))
                            }
                            Text(
                                mixedText(node.city.ifEmpty { node.country.ifEmpty { node.id } }),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (node.city.isNotEmpty() && node.country.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    mixedText(node.country),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Row(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(tint.copy(alpha = 0.14f))
                                .border(1.dp, tint.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                .animateContentSize(tween(320, easing = FastOutSlowInEasing)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                when (res) {
                                    is CheckHost.NodeResult.Ok -> localizeDigits(
                                        String.format(
                                            java.util.Locale.US,
                                            if (res.avgMs < 10) "%.1f" else "%.0f",
                                            res.avgMs
                                        ), lang
                                    ) + " " + t("unit_ms")
                                    is CheckHost.NodeResult.Failed -> t("netmon_down")
                                    else -> t("testing")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = tint,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetRadarRow(site: NetMonitor.Site, st: NetMonitor.State) {
    val t = stringsFn()
    val lang = LocalLang.current
    val target = when (st) {
        is NetMonitor.State.Reachable -> AppGreen
        is NetMonitor.State.Sanctioned -> Color(0xFFFFA94D)
        is NetMonitor.State.Unreachable -> Color(0xFFE0413C)
        is NetMonitor.State.Testing -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val tint by animateColorAsState(target, tween(400), label = "radarTint")
    val label = when (st) {
        is NetMonitor.State.Reachable -> t("netmon_up")
        is NetMonitor.State.Sanctioned -> t("netmon_sanctioned")
        is NetMonitor.State.Unreachable -> t("netmon_down")
        is NetMonitor.State.Testing -> t("testing")
        else -> "\u2014"
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadarDot(tint, st is NetMonitor.State.Testing)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(mixedText(site.name), style = MaterialTheme.typography.bodyMedium)
            Text(
                scriptRuns(site.host, MonoFont),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier.clip(RoundedCornerShape(9.dp))
                .background(tint.copy(alpha = 0.12f))
                .border(1.dp, tint.copy(alpha = 0.38f), RoundedCornerShape(9.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .animateContentSize(tween(320, easing = FastOutSlowInEasing)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = st is NetMonitor.State.Reachable,
                enter = fadeIn(tween(280)) + expandHorizontally(tween(320)),
                exit = fadeOut(tween(160)) + shrinkHorizontally(tween(240))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (st is NetMonitor.State.Reachable)
                            localizeDigits("${st.ms}", lang) + " " + t("unit_ms") else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = tint.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                }
            }
            Crossfade(targetState = label, animationSpec = tween(300), label = "radarLabel") { l ->
                Text(
                    l,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RadarDot(tint: Color, pulsing: Boolean) {
    val transition = rememberInfiniteTransition(label = "radarDot")
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
        label = "radarRipple"
    )
    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        if (pulsing) {
            Box(
                Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        val sc = 0.40f + ripple * 0.60f
                        scaleX = sc; scaleY = sc
                        alpha = (1f - ripple) * 0.6f
                    }
                    .background(Brush.radialGradient(listOf(tint, Color.Transparent)), CircleShape)
            )
        }
        Box(
            Modifier
                .size(16.dp)
                .background(
                    Brush.radialGradient(listOf(tint.copy(alpha = 0.40f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(Modifier.size(9.dp).clip(CircleShape).background(tint))
    }
}

@Composable
private fun NetMonitorScreen(onOpenCategories: () -> Unit, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val states by RadarRunner.states.collectAsState()
    val running by RadarRunner.running.collectAsState()
    val conn by VpnState.state.collectAsState()
    val viaTunnel = conn == Connection.CONNECTED

    fun run() {
        if (!running) RadarRunner.start(viaTunnel)
    }

    LaunchedEffect(Unit) {
        if (states.isEmpty() && !running) RadarRunner.start(viaTunnel)
    }

    val reachable = states.values.count { it is NetMonitor.State.Reachable }
    val done = states.values.count { it !is NetMonitor.State.Testing }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            InfoBox(
                if (viaTunnel) t("netmon_via_tunnel") else t("netmon_via_direct"),
                accent = if (viaTunnel) AppGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            val spin = rememberInfiniteTransition(label = "radarSpin")
            val angle by spin.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = LinearEasing)
                ),
                label = "radarSpinAngle"
            )
            BounceIconButton(onClick = { run() }, enabled = !running) {
                Icon(
                    Icons.Filled.Autorenew,
                    contentDescription = t("netmon_recheck"),
                    tint = if (running) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp)
                        .graphicsLayer { rotationZ = if (running) angle else 0f }
                )
            }
        }

        SettingsGroup {
            NetMonitor.Essential.forEach { site ->
                NetRadarRow(site, states[site.host] ?: NetMonitor.State.Idle)
            }
        }

        Text(
            t("netmon_summary").format(
                localizeDigits("$reachable", lang),
                localizeDigits("${NetMonitor.Essential.size}", lang)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        SettingsHubCard(
            icon = Icons.Filled.Apps,
            title = t("netcat_title"),
            subtitle = t("netcat_sub"),
            onClick = onOpenCategories
        )
    }
}

@Composable
private fun NetCategoriesScreen(onOpen: (Int) -> Unit, modifier: Modifier = Modifier) {
    val t = stringsFn()
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NetMonitor.Categories.forEachIndexed { i, cat ->
            SettingsHubCard(
                icon = when (cat.icon) {
                    "ai" -> Icons.Filled.SmartToy
                    "social" -> Icons.Filled.Groups
                    "gaming" -> Icons.Filled.SportsEsports
                    "trading" -> Icons.Filled.TrendingUp
                    "news" -> Icons.AutoMirrored.Filled.Article
                    else -> null
                },
                iconRes = if (cat.icon == "iranian") R.drawable.iran else null,
                title = t(cat.key),
                subtitle = t("netcat_count").format(
                    localizeDigits("${cat.sites.size}", LocalLang.current)
                ),
                onClick = { onOpen(i) }
            )
        }
    }
}

@Composable
private fun NetCategoryScreen(index: Int, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val scope = rememberCoroutineScope()
    val cat = NetMonitor.Categories.getOrNull(index) ?: return
    val states = remember(index) { mutableStateMapOf<String, NetMonitor.State>() }
    val conn by VpnState.state.collectAsState()
    val viaTunnel = conn == Connection.CONNECTED

    LaunchedEffect(index) {
        cat.sites.forEach { states[it.host] = NetMonitor.State.Testing }
        NetMonitor.probeAll(viaTunnel, cat.sites) { site, state -> states[site.host] = state }
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (viaTunnel) t("netmon_via_tunnel") else t("netmon_via_direct"),
            style = MaterialTheme.typography.bodySmall,
            color = if (viaTunnel) AppGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
        SettingsGroup {
            cat.sites.forEach { site ->
                NetRadarRow(site, states[site.host] ?: NetMonitor.State.Idle)
            }
        }
    }
}

@Composable
private fun SettingsHubCard(
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color? = null,
    iconRes: Int? = null,
    accents: List<String> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, (tint ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.30f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                    .background((tint ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                val iconTint = tint ?: MaterialTheme.colorScheme.primary
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint,
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(mixedText(title), style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (accents.isEmpty()) mixedText(subtitle)
                    else accentText(subtitle, *accents.toTypedArray()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    store: ConfigStore,
    scrollState: ScrollState,
    onOpenUsage: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenConnection: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenNetMon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val usage by UsageStore.usage.collectAsState()
    val allTime = remember(usage) { UsageStore.totalAll(usage) }

    Column(
        modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsHubCard(
            icon = Icons.Filled.DataUsage,
            title = t("data_usage"),
            subtitle = formatBytes(allTime[0] + allTime[1], lang),
            onClick = onOpenUsage
        )
        SettingsHubCard(
            icon = Icons.Filled.TravelExplore,
            title = t("netmon_title"),
            subtitle = t("netmon_sub"),
            onClick = onOpenNetMon
        )
        SettingsHubCard(
            icon = Icons.Filled.Build,
            title = t("tools"),
            subtitle = t("tools_sub"),
            onClick = onOpenTools
        )
        SettingsHubCard(
            icon = Icons.Filled.Router,
            title = t("connection_settings"),
            subtitle = t("connection_settings_sub"),
            onClick = onOpenConnection
        )
        SettingsHubCard(
            icon = Icons.Filled.Tune,
            title = t("preferences"),
            subtitle = t("preferences_sub"),
            onClick = onOpenPreferences
        )
        SettingsHubCard(
            icon = Icons.Filled.Info,
            title = t("about"),
            subtitle = t("about_sub"),
            onClick = onOpenAbout
        )

        BackupRow(store)
    }
}

@Composable
private fun BackupRow(store: ConfigStore) {
    val t = stringsFn()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(status) {
        if (status.isNotEmpty()) { delay(3500); status = "" }
    }

    val saver = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ConfigFile.MIME)
    ) { uri ->
        if (uri != null) {
            busy = true
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        val data = ConfigFile.encodeBackup(
                            context,
                            store.configs.value,
                            store.subscriptions.value,
                            store.settingsSnapshot(),
                            null
                        )
                        context.contentResolver.openOutputStream(uri)?.use { it.write(data) }
                        true
                    }.getOrDefault(false)
                }
                status = if (ok) t("backup_done") else t("backup_failed")
                busy = false
            }
        }
    }

    val opener = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bytes = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                }
                when {
                    bytes == null || bytes.isEmpty() -> status = t("import_bad_file")
                    runCatching { ConfigFile.isBackup(context, bytes, null) }
                        .getOrDefault(false) -> pending = bytes
                    else -> status = t("backup_not_backup")
                }
            }
        }
    }

    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BounceOutlinedButton(
                onClick = {
                    if (!busy) {
                        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US)
                            .format(java.util.Date())
                        saver.launch("groute-backup-$stamp.${ConfigFile.EXTENSION}")
                    }
                },
                enabled = !busy,
                minHeight = 34.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Icon(
                    Icons.Filled.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    t("backup_export"),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false
                )
            }
            BounceOutlinedButton(
                onClick = { opener.launch(arrayOf("*/*")) },
                minHeight = 34.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Icon(
                    Icons.Filled.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    t("backup_import"),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        AnimatedVisibility(visible = status.isNotEmpty()) {
            Text(
                mixedText(status),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            )
        }
    }

    pending?.let { bytes ->
        GlassDialog(
            onDismiss = { pending = null },
            title = t("backup_import"),
            confirmLabel = t("import_button"),
            dismissLabel = t("cancel"),
            accentOverride = AppGreen,
            onConfirm = {
                pending = null
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching { ConfigFile.decodeBackup(context, bytes, null) }.getOrNull()
                    }
                    if (result == null) {
                        status = t("import_bad_file")
                    } else {
                        store.restoreBackup(result.configs, result.subs, result.settings)
                        status = localizeDigits(
                            t("backup_restored").format(result.configs.size, result.subs.size),
                            store.lang.value
                        )
                    }
                }
            }
        ) {
            Text(mixedText(t("backup_restore_q")), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ToolsScreen(
    store: ConfigStore,
    onOpenStability: () -> Unit,
    onOpenCleanIp: () -> Unit,
    onOpenCheckHost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val autoSelect by store.autoSelect.collectAsState()
    val onionRouting by store.onionRouting.collectAsState()
    val adBlock by store.adBlock.collectAsState()
    val blockWhenOff by store.blockWhenOff.collectAsState()
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsHubCard(
            icon = Icons.Filled.NetworkCheck,
            title = t("stab_title"),
            subtitle = t("stab_sub"),
            onClick = onOpenStability
        )
        SettingsHubCard(
            icon = Icons.Filled.Dns,
            title = t("chk_title"),
            subtitle = t("chk_sub"),
            onClick = onOpenCheckHost
        )
        SettingsHubCard(
            iconRes = R.drawable.cloudflare,
            title = t("scan_warp"),
            subtitle = t("scan_sub"),
            onClick = onOpenCleanIp
        )
        SettingsGroup {
            SettingRow(
                title = t("adblock_title"),
                subtitle = t("adblock_sub"),
                checked = adBlock,
                onCheckedChange = { store.setAdBlock(it) }
            )
            AnimatedVisibility(visible = adBlock) {
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    SettingRow(
                        title = t("adblock_always_title"),
                        subtitle = t("adblock_always_sub"),
                        checked = blockWhenOff,
                        onCheckedChange = { store.setBlockWhenOff(it) }
                    )
                }
            }
            SettingRow(
                title = t("onion_title"),
                subtitle = t("onion_sub"),
                checked = onionRouting,
                onCheckedChange = { store.setOnionRouting(it) }
            )
            SettingRow(
                title = t("smart_connect"),
                subtitle = t("smart_connect_sub"),
                checked = autoSelect,
                onCheckedChange = { store.setAutoSelect(it) }
            )
        }
    }
}

@Composable
private fun TorCountryGroup(
    title: String,
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
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            content()
        }
    }
}

@Composable
private fun ConfigDebuggerScreen(
    store: ConfigStore,
    onSwitch: (ProxyConfig) -> Unit,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val configs by store.configs.collectAsState()
    val selectedId by store.selectedId.collectAsState()
    val config = configs.find { it.id == selectedId }

    if (config == null) {
        Column(
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsGroup {
                Text(
                    t("dbg_no_config"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val staticChecks = remember(config) { ConfigDebug.inspect(config) }

    var tick by remember { mutableStateOf(0) }
    var connecting by remember(config.id) { mutableStateOf(false) }
    val allResults by DebugRunner.results.collectAsState()
    val runningIds by DebugRunner.running.collectAsState()
    val result = allResults[config.id]
    val testing = runningIds.contains(config.id)

    LaunchedEffect(config.id, tick, active) {
        if (!active) return@LaunchedEffect
        if (tick == 0) return@LaunchedEffect
        if (VpnState.activeId.value != config.id ||
            VpnState.state.value != Connection.CONNECTED
        ) {
            connecting = true
            onSwitch(config)
            withTimeoutOrNull(30000) {
                VpnState.state.first {
                    (it == Connection.CONNECTED && VpnState.activeId.value == config.id) ||
                            it == Connection.ERROR
                }
            }
            connecting = false
        }
        DebugRunner.start(config, store)
    }

    val info = result?.info
    var shownInfo by remember(config.id) { mutableStateOf<ProbeInfo?>(null) }
    if (info != null) shownInfo = info

    val checks = staticChecks + (result?.findings ?: emptyList())
    val problems = checks.filter { it.level != DebugLevel.OK }
    val broken = checks.count { it.level == DebugLevel.BAD }
    val warned = checks.count { it.level == DebugLevel.WARN }
    val state = result?.state ?: DebugState.TIMEOUT
    val pingMs = result?.pingMs ?: -1
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val stateColor = when {
        testing || result == null -> MaterialTheme.colorScheme.primary
        state == DebugState.HEALTHY -> AppGreen
        state == DebugState.TIMEOUT -> if (dark) Color(0xFFFFC24D) else Color(0xFF9A6B00)
        state == DebugState.BLOCKED -> if (dark) Color(0xFFFF8A3D) else Color(0xFFD2620F)
        state == DebugState.OFFLINE -> if (dark) Color(0xFF8A93A5) else Color(0xFF6B7484)
        else -> MaterialTheme.colorScheme.error
    }
    val stateLabel = when {
        connecting -> t("dbg_connecting")
        testing || result == null -> t("dbg_testing")
        state == DebugState.HEALTHY -> t("dbg_healthy")
        state == DebugState.TIMEOUT -> t("dbg_timeout")
        state == DebugState.BLOCKED -> t("dbg_blocked")
        state == DebugState.OFFLINE -> t("dbg_offline")
        else -> t("dbg_broken")
    }
    val tint by animateColorAsState(stateColor, tween(400), label = "dbgState")

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsGroup {
            Row(
                Modifier.animateContentSize(tween(320, easing = FastOutSlowInEasing)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadarDot(tint, testing)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    MarqueeName(config.name, MaterialTheme.typography.titleSmall)
                    Text(
                        monoText(
                            localizeDigits(
                                "${config.protocol} \u00b7 ${config.address}:${config.port}", lang
                            )
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BounceIconButton(onClick = { if (!testing) tick++ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = t("dbg_recheck"))
                }
            }
        }

        SettingsGroup(t("dbg_state")) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AnimatedContent(
                    targetState = stateLabel,
                    transitionSpec = {
                        (slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 2 } +
                                fadeIn(tween(320))) togetherWith
                                (slideOutVertically(tween(320, easing = FastOutSlowInEasing)) { -it / 2 } +
                                        fadeOut(tween(180)))
                    },
                    label = "dbgStateLabel",
                    modifier = Modifier.weight(1f)
                ) { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )
                }
                Row(
                    Modifier.clip(RoundedCornerShape(9.dp))
                        .background(tint.copy(alpha = 0.12f))
                        .border(1.dp, tint.copy(alpha = 0.38f), RoundedCornerShape(9.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        monoText(
                            t("dbg_ping") + "  " + localizeDigits("$pingMs", lang) +
                                    if (pingMs >= 0) " " + t("unit_ms") else ""
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        maxLines = 1
                    )
                }
            }
            Text(
                if (broken == 0 && warned == 0) t("dbg_all_ok")
                else t("dbg_issues").format(
                    localizeDigits("$broken", lang),
                    localizeDigits("$warned", lang)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = info != null,
            enter = fadeIn(tween(340)) +
                    slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it / 4 } +
                    expandVertically(tween(340, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(220)) + shrinkVertically(tween(280, easing = FastOutSlowInEasing))
        ) {
            shownInfo?.let { info ->
                SettingsGroup(t("dbg_info")) {
                    DebugInfoRow(t("dbg_part_transport"), info.method)
                    if (info.entryIp.isNotBlank() && !info.entryIp.equals(info.exitIp, true)) DebugInfoRow(
                        t("dbg_part_entry"),
                        listOf(info.entryIp, countryName(info.entryCountry), info.entryIsp)
                            .filter { it.isNotBlank() }
                            .joinToString(" \u00b7 ")
                    )
                    DebugInfoRow(
                        t("dbg_part_ip"),
                        if (info.exitIp.isBlank()) t("dbg_exit_offline")
                        else listOf(info.exitIp, countryName(info.exitCountry), info.exitIsp)
                            .filter { it.isNotBlank() }
                            .joinToString(" \u00b7 ")
                    )
                    DebugInfoRow(
                        t("dbg_part_iptype"),
                        info.kind.ifBlank { t("dbg_rep_unavailable") },
                        if (info.flagged) MaterialTheme.colorScheme.error else null
                    )
                    DebugInfoRow(
                        t("dbg_part_risk"),
                        if (info.reputation < 0) t("dbg_rep_unavailable")
                        else localizeDigits("${info.reputation} / 100", lang) + " \u00b7 " + info.repBand,
                        when {
                            info.reputation < 0 -> null
                            info.reputation >= 60 -> AppGreen
                            info.reputation >= 40 -> Color(0xFFFF8A3D)
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    if (info.flags.isNotBlank()) DebugInfoRow(
                        t("dbg_part_flags"), info.flags, MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        SettingsGroup(t("dbg_checks")) {
            AnimatedVisibility(
                visible = problems.isEmpty(),
                enter = fadeIn(tween(280)) + expandVertically(tween(280, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(160)) + shrinkVertically(tween(220, easing = FastOutSlowInEasing))
            ) {
                Text(
                    t("dbg_all_ok"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            problems.forEach { check ->
                val color = when (check.level) {
                    DebugLevel.OK -> AppGreen
                    DebugLevel.WARN -> Color(0xFFFFA94D)
                    DebugLevel.BAD -> MaterialTheme.colorScheme.error
                }
                val icon = when (check.level) {
                    DebugLevel.OK -> Icons.Filled.CheckCircle
                    DebugLevel.WARN -> Icons.Filled.Warning
                    DebugLevel.BAD -> Icons.Filled.Cancel
                }
                Row(
                    Modifier.fillMaxWidth()
                        .animateContentSize(tween(300, easing = FastOutSlowInEasing)),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.padding(top = 2.dp).size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            t(check.partKey),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            if (check.level == DebugLevel.OK) check.value.ifBlank { "\u2014" }
                            else t(check.noteKey),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (check.level == DebugLevel.OK)
                                MaterialTheme.colorScheme.onSurfaceVariant else color
                        )
                        if (check.level != DebugLevel.OK && check.value.isNotBlank()) Text(
                            monoText(check.value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugInfoRow(label: String, value: String, valueColor: Color? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(118.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Text(
            monoText(value),
            style = MaterialTheme.typography.labelSmall,
            color = valueColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsGroup(
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
private fun ConnectionSettingsScreen(
    store: ConfigStore,
    onOpenPerApp: () -> Unit,
    onOpenLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val fragment by store.fragment.collectAsState()
    val splitRouting by store.splitRouting.collectAsState()
    val sniffing by store.sniffing.collectAsState()
    val sniffTypes by store.sniffTypes.collectAsState()
    val killSwitch by store.killSwitch.collectAsState()
    val mux by store.mux.collectAsState()
    val muxConcurrency by store.muxConcurrency.collectAsState()
    val perAppMode by store.perAppMode.collectAsState()
    val perAppList by store.perAppList.collectAsState()
    val mixedPort by store.mixedPort.collectAsState()
    val fakeDns by store.fakeDns.collectAsState()
    val encryptedDns by store.encryptedDns.collectAsState()
    val settingsContext = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsGroup(t("routing")) {
            SettingRow(
                title = t("fakedns_title"),
                subtitle = t("fakedns_sub"),
                checked = fakeDns,
                onCheckedChange = { store.setFakeDns(it) }
            )
            SettingRow(
                title = t("encdns_title"),
                subtitle = t("encdns_sub"),
                checked = encryptedDns,
                onCheckedChange = { store.setEncryptedDns(it) }
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

            SettingRow(
                title = t("mux_title"),
                subtitle = t("mux_sub"),
                checked = mux,
                onCheckedChange = { store.setMux(it) }
            )
            AnimatedVisibility(visible = mux) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("mux_concurrency"), style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    IconButton(onClick = { store.setMuxConcurrency(muxConcurrency - 1) }) {
                        Icon(Icons.Filled.Remove, contentDescription = "-")
                    }
                    Text("$muxConcurrency", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                    IconButton(onClick = { store.setMuxConcurrency(muxConcurrency + 1) }) {
                        Icon(Icons.Filled.Add, contentDescription = "+")
                    }
                }
            }

            SettingRow(
                title = t("kill_switch_title"),
                subtitle = t("kill_switch_sub"),
                checked = killSwitch,
                onCheckedChange = { store.setKillSwitch(it) }
            )
            AnimatedVisibility(visible = killSwitch) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            runCatching {
                                settingsContext.startActivity(
                                    Intent("android.net.vpn.SETTINGS")
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure {
                                runCatching {
                                    settingsContext.startActivity(
                                        Intent(android.provider.Settings.ACTION_VPN_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t("always_on_title"), style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(t("always_on_sub"), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            }

        }

        SettingsGroup(t("advanced")) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("mixed_port"), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        t("mixed_port_sub"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                val focus = LocalFocusManager.current
                var portText by remember(mixedPort) { mutableStateOf(mixedPort.toString()) }
                BasicTextField(
                    value = portText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(5)
                        portText = digits
                        digits.toIntOrNull()?.let { if (it in 1024..65535) store.setMixedPort(it) }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = monoFont(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                    modifier = Modifier.width(78.dp).height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                    }
                )
            }
        }

        SettingsHubCard(
            icon = Icons.Filled.Apps,
            title = t("per_app"),
            subtitle = perAppSummary(perAppMode, perAppList.size, lang),
            onClick = onOpenPerApp
        )

        SettingsHubCard(
            icon = Icons.AutoMirrored.Filled.Article,
            title = t("xray_logs"),
            subtitle = t("xray_logs_sub"),
            onClick = onOpenLogs
        )

        Text(
            t("takes_effect"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PreferencesScreen(
    store: ConfigStore,
    onOpenTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val curLang by store.lang.collectAsState()
    var langOpen by remember { mutableStateOf(false) }
    val autoRefreshHours by store.autoRefreshHours.collectAsState()
    var autoRefreshOpen by remember { mutableStateOf(false) }

    fun refreshLabel(h: Int): String =
        if (h <= 0) t("auto_refresh_off")
        else if (h == 1) t("every_hour").format(localizeDigits("$h", lang))
        else t("every_hours").format(localizeDigits("$h", lang))

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsHubCard(
            icon = Icons.Filled.Palette,
            title = t("theme_settings"),
            subtitle = t("theme_settings_sub"),
            onClick = onOpenTheme
        )

        SettingsGroup {
            Text(t("language"), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Box {
                OutlinedButton(
                    onClick = { langOpen = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (curLang == Lang.FA) "فارسی" else "English",
                        fontFamily = if (curLang == Lang.FA) VazirFont else LexendFont,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = langOpen,
                    onDismissRequest = { langOpen = false },
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "English",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = LexendFont
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = { store.setLang(Lang.EN); langOpen = false }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "فارسی",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = VazirFont
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(40.dp),
                        onClick = { store.setLang(Lang.FA); langOpen = false }
                    )
                }
            }

            Text(t("auto_refresh"), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Box {
                OutlinedButton(
                    onClick = { autoRefreshOpen = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(refreshLabel(autoRefreshHours), modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = autoRefreshOpen,
                    onDismissRequest = { autoRefreshOpen = false },
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    listOf(0, 1, 6, 12, 24).forEach { h ->
                        DropdownMenuItem(
                            text = { Text(refreshLabel(h), style = MaterialTheme.typography.bodyMedium) },
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(40.dp),
                            onClick = { store.setAutoRefreshHours(h); autoRefreshOpen = false }
                        )
                    }
                }
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
private fun ThemeSettingsScreen(store: ConfigStore, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val themeMode by store.themeMode.collectAsState()
    val globeStyle by store.globeStyle.collectAsState()

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(t("theme_mode"), style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeModeRow(
                icon = Icons.Filled.LightMode,
                label = t("theme_light"),
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { store.setThemeMode(ThemeMode.LIGHT) }
            )
            ThemeModeRow(
                icon = Icons.Filled.DarkMode,
                label = t("theme_dark"),
                selected = themeMode == ThemeMode.DARK,
                onClick = { store.setThemeMode(ThemeMode.DARK) }
            )
            ThemeModeRow(
                icon = Icons.Filled.Contrast,
                label = t("theme_amoled"),
                selected = themeMode == ThemeMode.AMOLED,
                onClick = { store.setThemeMode(ThemeMode.AMOLED) }
            )
            ThemeModeRow(
                icon = Icons.Filled.Contrast,
                label = t("theme_system"),
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { store.setThemeMode(ThemeMode.SYSTEM) }
            )
        }

        Text(t("globe_style_title"), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlobeStyleOption(
                label = t("globe_style_filled"),
                selected = globeStyle == "filled",
                onClick = { store.setGlobeStyle("filled") },
                modifier = Modifier.weight(1f)
            )
            GlobeStyleOption(
                label = t("globe_style_dots"),
                selected = globeStyle == "dots",
                onClick = { store.setGlobeStyle("dots") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeModeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(mixedText(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
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
        }.getOrNull() ?: "\u2014"
    }
    val xrayVersion = remember { xrayCoreVersion() }
    var privacyOpen by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var updateUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val logoRes = if (isDark) R.drawable.logo else R.drawable.logo_black
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 2.dp)) {
            PulseHalo(color = primary, size = 190.dp)
            Image(
                painter = painterResource(logoRes),
                contentDescription = null,
                modifier = Modifier.size(152.dp)
            )
        }

        Text(
            mixedText(t("about_tagline")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Row(
            Modifier.padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AboutChip(t("app_version"), appVersion)
            AboutChip(t("xray_version"), xrayVersion)
        }

        AboutCard(
            icon = Icons.Filled.Hub,
            title = t("source_code"),
            value = "github.com/SuOracle/GRoute",
            onClick = { runCatching { uriHandler.openUri("https://github.com/SuOracle/GRoute") } }
        )

        AboutCard(
            icon = Icons.Filled.Refresh,
            title = t("check_updates"),
            value = updateStatus,
            busy = checking,
            onClick = {
                if (checking) return@AboutCard
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
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { privacyOpen = !privacyOpen },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AboutIconTile(Icons.Filled.Lock)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        mixedText(t("privacy_policy")),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    val turn by animateFloatAsState(
                        targetValue = if (privacyOpen) 180f else 0f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "privacyChevron"
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer { rotationZ = turn }
                    )
                }
                AnimatedVisibility(
                    visible = privacyOpen,
                    enter = fadeIn(tween(260)) + expandVertically(tween(300, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(160)) + shrinkVertically(tween(240, easing = FastOutSlowInEasing))
                ) {
                    Text(
                        mixedText(if (lang == Lang.FA) PRIVACY_FA else PRIVACY_EN),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
        }

        AboutCard(
            iconVector = TelegramIcon,
            title = t("telegram_support"),
            value = "@OracleVPN1",
            onClick = { runCatching { uriHandler.openUri("https://t.me/OracleVPN1") } }
        )

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun AboutIconTile(icon: ImageVector) {
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
}

@Composable
private fun AboutChip(label: String, value: String) {
    Row(
        Modifier.clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            mixedText(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(Modifier.width(7.dp))
        Text(
            mixedText(localizeDigits(value, LocalLang.current)),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = LexendFont,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
    }
}

@Composable
private fun AboutCard(
    title: String,
    value: String?,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconVector: ImageVector? = null,
    busy: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
            AboutIconTile(icon ?: iconVector ?: Icons.Filled.Info)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(mixedText(title), style = MaterialTheme.typography.bodyLarge)
                Crossfade(targetState = value, animationSpec = tween(300), label = "aboutValue") { v ->
                    if (!v.isNullOrBlank()) {
                        Text(
                            mixedText(localizeDigits(v, LocalLang.current)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

Contact: Questions? Reach the developer on Telegram at @OracleVPN1.
""".trimIndent()

private val PRIVACY_FA = """
جی‌روت برای حفاظت از حریم خصوصی شما ساخته شده است.

چه چیزی جمع‌آوری می‌کنیم: هیچ‌چیز. جی‌روت حساب کاربری، تحلیل آماری، تبلیغات و ردیابی ندارد. توسعه‌دهنده هیچ سروری که فعالیت مرور شما را دریافت کند اجرا نمی‌کند.

روی دستگاه شما: کانفیگ‌های سرور شما به‌صورت رمزگذاری‌شده در حافظهٔ خصوصی برنامه ذخیره می‌شوند. آمار مصرف داده (میزان ترافیک عبوری از تونل) فقط روی دستگاه شما می‌ماند و به هیچ‌جا ارسال نمی‌شود. پاک‌کردن دادهٔ برنامه آن را حذف می‌کند.

درخواست‌های شبکه: برای نمایش نشانی IP و موقعیت تقریبی شما، برنامه با سرویس‌های شخص ثالث مانند ipwho.is و ipify.org تماس می‌گیرد. این سرویس‌ها ناگزیر نشانی IP اتصال شما را می‌بینند. هیچ اطلاعات شناسایی دیگری ارسال نمی‌شود.

سرورهای شما: سرورهای پراکسی/وی‌پی‌ان که اضافه می‌کنید توسط شما یا ارائه‌دهندهٔ اشتراکتان فراهم می‌شوند. جی‌روت هیچ کنترل یا دیدی نسبت به سیاست ثبت لاگ آن سرورها ندارد؛ ارائه‌دهنده‌ای را انتخاب کنید که به آن اعتماد دارید.

دسترسی‌ها: دسترسی وی‌پی‌ان تنها برای هدایت ترافیک از طریق تونلی که انتخاب می‌کنید استفاده می‌شود و هرگز برای بازرسی، تغییر یا ثبت ترافیک شما به‌کار نمی‌رود.

تغییرات: این سیاست ممکن است با تکامل برنامه به‌روزرسانی شود؛ تغییرات مهم در نسخه‌های جدید اعلام می‌شوند.

تماس: سؤالی دارید؟ از طریق تلگرام با @OracleVPN1 در ارتباط باشید.
""".trimIndent()

@Composable
private fun LogsScreen(store: ConfigStore, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val lang = LocalLang.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var logs by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf("") }
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
    LaunchedEffect(toast) { if (toast.isNotEmpty()) { delay(1800); toast = "" } }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            t("xray_logs"),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1
                        )
                        Crossfade(
                            targetState = if (toast.isNotEmpty()) toast else
                                localizeDigits("${logs.count { it == '\n' }.let { if (logs.isBlank()) 0 else it + 1 }}", lang) +
                                        " " + t("no_logs").takeIf { logs.isBlank() }.orEmpty(),
                            animationSpec = tween(260),
                            label = "logMeta"
                        ) { line ->
                            Text(
                                line.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                    LogAction(Icons.Filled.Refresh, t("refresh")) { load() }
                    LogAction(Icons.Filled.ContentCopy, t("copy")) {
                        if (logs.isNotBlank()) {
                            clipboard.setText(AnnotatedString(logs))
                            toast = t("copied")
                        }
                    }
                    LogAction(Icons.Filled.Delete, t("clear")) {
                        runCatching { Runtime.getRuntime().exec(arrayOf("logcat", "-c")) }
                        logs = ""
                    }
                }
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
                )
                if (logs.isBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (loading) t("testing") else t("no_logs"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SelectionContainer {
                        Text(
                            logs,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = MonoFont,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .padding(start = 6.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
            .clickable {
                scope.launch {
                    scale.animateTo(0.88f, tween(90))
                    scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 420f))
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
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
        "XrayCore:V", "GoLog:V", "GozarVpnService:V",
        "Aether:V", "Tor:V", "GRouteIke:V",
        "charon:V", "CharonVpnService:V",
        "GRouteAuto:V", "GRouteQr:V", "GRouteHaptic:V",
        "*:S"
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
    val conn by VpnState.state.collectAsState()
    val activeId by VpnState.activeId.collectAsState()
    val target = if (conn == Connection.CONNECTED)
        configs.find { it.id == activeId } ?: configs.find { it.id == selectedId }
    else null

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
        val cfg = target
        running = true; failed = false; result = null
        dlLive = 0.0; ulLive = 0.0; livePing = 0.0
        phase = StabilityTest.Phase.PING
        val testJson =
            if (cfg != null && cfg.protocol.trim().lowercase() != "ikev2")
                ConfigBuilder.buildForTest(cfg)
            else ConfigBuilder.buildForTestDirect()
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
            visible = running,
            enter = fadeIn(tween(340, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(340, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(340, easing = FastOutSlowInEasing)) { -it / 2 },
            exit = fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                    shrinkVertically(tween(280, easing = FastOutSlowInEasing))
        ) {
            val phaseIcon = when (phase) {
                StabilityTest.Phase.PING -> Icons.Filled.Schedule
                StabilityTest.Phase.DOWNLOAD -> Icons.Filled.ArrowDownward
                else -> Icons.Filled.ArrowUpward
            }
            val phaseLabel = when (phase) {
                StabilityTest.Phase.PING -> t("stab_ping")
                StabilityTest.Phase.DOWNLOAD -> t("download")
                StabilityTest.Phase.UPLOAD -> t("upload")
                else -> ""
            }
            val phaseValue = when (phase) {
                StabilityTest.Phase.PING ->
                    localizeDigits("${livePing.toInt()}", lang) + " " + t("unit_ms")
                StabilityTest.Phase.DOWNLOAD ->
                    localizeDigits(String.format(java.util.Locale.US, "%.1f", dlLive), lang) + " " + t("unit_mbps")
                StabilityTest.Phase.UPLOAD ->
                    localizeDigits(String.format(java.util.Locale.US, "%.1f", ulLive), lang) + " " + t("unit_mbps")
                else -> ""
            }
            val phaseTint = when (phase) {
                StabilityTest.Phase.PING -> AppCyan
                StabilityTest.Phase.DOWNLOAD -> Color(0xFFC23BFF)
                else -> AppAqua
            }
            Crossfade(targetState = phase, animationSpec = tween(300), label = "phaseText") { ph ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(phaseTint.copy(alpha = 0.10f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ph == StabilityTest.Phase.PING) {
                        PingLine(color = phaseTint, size = 26.dp)
                    } else {
                        Icon(phaseIcon, contentDescription = null, tint = phaseTint,
                            modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        phaseLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        phaseValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = phaseTint
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !running && result != null && lastTestTime > 0L,
            enter = fadeIn(tween(340, delayMillis = 120, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(340, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(180))
        ) {
            Text(
                t("stab_last_test") + " " + formatTestTime(lastTestTime, lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().appearOnce(60),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedTile(
                    icon = Icons.Filled.ArrowDownward,
                    label = t("download"),
                    mbps = dlLive,
                    active = running && phase == StabilityTest.Phase.DOWNLOAD,
                    tint = Color(0xFFC23BFF),
                    modifier = Modifier.weight(1f)
                )
                SpeedTile(
                    icon = Icons.Filled.ArrowUpward,
                    label = t("upload"),
                    mbps = ulLive,
                    active = running && phase == StabilityTest.Phase.UPLOAD,
                    tint = AppAqua,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(420, easing = FastOutSlowInEasing)) +
                    scaleIn(tween(420, easing = FastOutSlowInEasing), initialScale = 0.92f),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
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

        QualityStartButton(
            running = running,
            onClick = { if (running) cancel() else start() },
            modifier = Modifier.fillMaxWidth().appearOnce(140)
        )

        InfoBox(
            if (target != null) t("stab_testing_server") + " " + target.name
            else t("stab_direct"),
            centered = true,
            modifier = Modifier.appearOnce(200)
        )

        if (failed) {
            Text(t("stab_failed"), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error)
        }

        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn(tween(450, delayMillis = 120, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(450, easing = FastOutSlowInEasing)) +
                    scaleIn(tween(450, delayMillis = 120, easing = FastOutSlowInEasing), initialScale = 0.92f),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            result?.let { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    RevealOnScroll { shown ->
                        Column(
                            Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            RevealText(t("stab_quality"), MaterialTheme.typography.labelLarge, shown, 0)
                            val score = overallScore(r)
                            val tint = qualityColor(score)
                            val sweep by animateFloatAsState(
                                targetValue = if (shown) (score / 100.0).toFloat() else 0f,
                                animationSpec = tween(900, easing = FastOutSlowInEasing),
                                label = "qualityArc"
                            )
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(Modifier.size(148.dp)) {
                                    val stroke = 14.dp.toPx()
                                    val inset = stroke / 2f
                                    drawArc(
                                        color = tint.copy(alpha = 0.16f),
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        topLeft = Offset(inset, inset),
                                        size = Size(size.width - stroke, size.height - stroke),
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = tint,
                                        startAngle = 135f,
                                        sweepAngle = 270f * sweep,
                                        useCenter = false,
                                        topLeft = Offset(inset, inset),
                                        size = Size(size.width - stroke, size.height - stroke),
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        localizeDigits("${(sweep * 100).toInt()}", lang),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = tint
                                    )
                                    Text(
                                        t(qualityLabelKey(score)),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun overallScore(r: StabilityTest.Result): Double {
    val latency = (100.0 - (r.idleLatency - 40.0) * 0.45).coerceIn(0.0, 100.0)
    val jitter = (100.0 - r.jitter * 3.0).coerceIn(0.0, 100.0)
    val loaded = (100.0 - (maxOf(r.downloadLatency, r.uploadLatency) - r.idleLatency) * 0.5)
        .coerceIn(0.0, 100.0)
    val down = (r.downloadMbps / 25.0 * 100.0).coerceIn(0.0, 100.0)
    val up = (r.uploadMbps / 10.0 * 100.0).coerceIn(0.0, 100.0)
    return (latency * 0.25 + jitter * 0.2 + loaded * 0.2 + down * 0.25 + up * 0.1)
        .coerceIn(0.0, 100.0)
}

private fun qualityLabelKey(score: Double): String = when {
    score >= 80 -> "stab_q_excellent"
    score >= 60 -> "stab_q_good"
    score >= 40 -> "stab_q_fair"
    else -> "stab_q_poor"
}

@Composable
private fun qualityColor(score: Double): Color = when {
    score >= 80 -> AppGreen
    score >= 60 -> AppCyan
    score >= 40 -> Color(0xFFFFA94D)
    else -> Color(0xFFE0413C)
}

private fun formatTestTime(millis: Long, lang: Lang): String {
    val sdf = java.text.SimpleDateFormat("yyyy/MM/dd  HH:mm", java.util.Locale.US)
    return localizeDigits(sdf.format(java.util.Date(millis)), lang)
}

@Composable
internal fun rememberInternetOffline(): Boolean {
    var offline by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (isActive) {
            val hosts = listOf("8.8.8.8" to 443, "1.1.1.1" to 443)
            var reached = false
            for (h in hosts) {
                if (Pinger.ping(h.first, h.second, 2000) is PingResult.Ok) {
                    reached = true
                    break
                }
                delay(120)
            }
            offline = !reached
            delay(if (offline) 5000 else 15000)
        }
    }
    return offline
}

@Composable
private fun SpeedTile(
    icon: ImageVector,
    label: String,
    mbps: Double,
    active: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val frac by animateFloatAsState(
        sqrt((mbps / 100.0).coerceIn(0.0, 1.0)).toFloat(),
        tween(600, easing = FastOutSlowInEasing),
        label = "speedTile"
    )
    val glow by rememberInfiniteTransition(label = "speedGlow").animateFloat(
        initialValue = 0.35f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speedGlowA"
    )
    val border = if (active) tint.copy(alpha = glow) else tint.copy(alpha = 0.22f)

    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.09f))
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                localizeDigits(String.format(java.util.Locale.US, "%.1f", mbps), lang),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (active) tint else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.width(4.dp))
            Text(
                t("unit_mbps"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Box(
            Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp))
                .background(tint.copy(alpha = 0.15f))
        ) {
            Box(
                Modifier.fillMaxWidth(frac).fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(tint)
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
            Text(mixedText(label), style = MaterialTheme.typography.bodySmall,
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
private fun QualityStartButton(running: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = stringsFn()
    val tint by animateColorAsState(
        targetValue = if (running) Color(0xFFFFA94D) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(420),
        label = "qualityBtnTint"
    )
    var pressed by remember { mutableStateOf(false) }
    val press by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "qualityBtnPress"
    )
    Box(
        modifier
            .height(58.dp)
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        tint.copy(alpha = 0.16f),
                        tint.copy(alpha = 0.30f),
                        tint.copy(alpha = 0.16f)
                    )
                )
            )
            .border(1.6.dp, tint.copy(alpha = 0.70f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        ConnectSweep(color = tint, active = running, modifier = Modifier.matchParentSize())
        AnimatedContent(
            targetState = running,
            transitionSpec = {
                (fadeIn(tween(280)) + scaleIn(tween(280), initialScale = 0.9f)) togetherWith
                        (fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.9f))
            },
            label = "qualityBtnLabel"
        ) { busy ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (busy) Icons.Filled.Close else Icons.Filled.Speed,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (busy) t("cancel") else t("stab_start"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ConnectSweep(color: Color, active: Boolean, modifier: Modifier = Modifier) {
    val phase = rememberInfiniteTransition(label = "connSweep").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "connSweepV"
    )
    val fade by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "connSweepFade"
    )
    if (fade <= 0.004f) return
    Spacer(
        modifier.drawWithCache {
            val bandW = (size.width * 0.42f).coerceAtLeast(1f)
            val brush = Brush.horizontalGradient(
                0.00f to color.copy(alpha = 0f),
                0.30f to color.copy(alpha = 0.14f),
                0.50f to color.copy(alpha = 0.42f),
                0.70f to color.copy(alpha = 0.14f),
                1.00f to color.copy(alpha = 0f),
                startX = 0f,
                endX = bandW
            )
            val travel = size.width + bandW
            val band = Size(bandW, size.height)
            onDrawBehind {
                val x = phase.value * travel - bandW
                translate(left = x) {
                    drawRect(brush = brush, topLeft = Offset.Zero, size = band, alpha = fade)
                }
            }
        }
    )
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
private fun PulseHalo(color: Color, size: Dp, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "halo")
    val breath by tr.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloBreath"
    )
    val strength by tr.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloStrength"
    )

    Canvas(modifier.size(size)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val glowR = (this.size.minDimension / 2f) * breath

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.22f * strength),
                    color.copy(alpha = 0.07f * strength),
                    color.copy(alpha = 0f)
                ),
                center = c,
                radius = glowR
            ),
            radius = glowR,
            center = c
        )
    }
}

@Composable
private fun PingLine(color: Color, size: Dp = 96.dp, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "ping")
    val t by tr.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "pingT"
    )
    val core by tr.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pingCore"
    )

    Canvas(modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val maxR = this.size.minDimension / 2f - 2.dp.toPx()

        for (i in 0 until 3) {
            val p = (t + i / 3f) % 1f
            val r = maxR * p
            val fade = (1f - p).coerceIn(0f, 1f)
            if (r > 1f) {
                drawCircle(
                    color = color.copy(alpha = 0.45f * fade * fade),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        val coreR = maxR * 0.22f * core
        drawCircle(color.copy(alpha = 0.18f), radius = coreR * 2.4f, center = Offset(cx, cy))
        drawCircle(color.copy(alpha = 0.40f), radius = coreR * 1.5f, center = Offset(cx, cy))
        drawCircle(color, radius = coreR, center = Offset(cx, cy))
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
    val dailyCfg by UsageStore.dailyCfg.collectAsState()
    val hourlyCfg by UsageStore.hourlyCfg.collectAsState()
    val context = LocalContext.current
    var mode by remember { mutableStateOf(RangeMode.TODAY) }
    var menuOpen by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf(LocalDate.now().minusDays(6)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var fromHour by remember { mutableStateOf(0) }
    var toHour by remember { mutableStateOf(23) }

    val bars = remember(daily, hourly, mode, fromDate, toDate, fromHour, toHour) {
        when (mode) {
            RangeMode.TODAY -> UsageStore.hourlyToday(hourly)
            RangeMode.WEEK -> UsageStore.dailyBars(daily, 7)
            RangeMode.MONTH -> UsageStore.dailyBars(daily, 30)
            RangeMode.CUSTOM -> {
                val lo = if (fromDate.isAfter(toDate)) toDate else fromDate
                val hi = if (fromDate.isAfter(toDate)) fromDate else toDate
                val span = java.time.temporal.ChronoUnit.DAYS.between(lo, hi)
                if (span <= 2) {
                    val loH = minOf(fromHour, toHour)
                    val hiH = maxOf(fromHour, toHour)
                    UsageStore.hourlyBarsRange(hourly, lo, hi).filter { bar ->
                        val h = bar.short.toIntOrNull()
                        h == null || h in loH..hiH
                    }
                } else UsageStore.dailyBarsRange(daily, lo, hi)
            }
        }
    }
    val total = remember(bars) { UsageStore.sum(bars) }
    val hourlyMode = mode == RangeMode.TODAY ||
            (mode == RangeMode.CUSTOM &&
                    java.time.temporal.ChronoUnit.DAYS.between(
                        if (fromDate.isAfter(toDate)) toDate else fromDate,
                        if (fromDate.isAfter(toDate)) fromDate else toDate
                    ) <= 2)
    val directOf: (UsageStore.Bar) -> Long = { bar ->
        val src = if (hourlyMode) hourlyCfg else dailyCfg
        src[bar.key]?.get(UsageStore.DIRECT_KEY)?.let { it[0] + it[1] } ?: 0L
    }
    val rangeDirect = remember(bars, dailyCfg, hourlyCfg, hourlyMode) { bars.sumOf(directOf) }
    val rangeVpn = (total[0] + total[1] - rangeDirect).coerceAtLeast(0L)

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                t("range"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(t(mode.key), modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    RangeMode.values().forEach { m ->
                        DropdownMenuItem(
                            text = { Text(t(m.key), style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = {
                                if (mode == m) Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(40.dp),
                            onClick = { mode = m; menuOpen = false }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = mode == RangeMode.CUSTOM,
            enter = fadeIn(tween(280)) +
                    slideInVertically(tween(320, easing = FastOutSlowInEasing)) { -it / 3 } +
                    expandVertically(tween(320, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(260, easing = FastOutSlowInEasing))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            RoundedCornerShape(14.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RangeCell(
                        label = t("from"),
                        date = fromDate,
                        hour = fromHour,
                        lang = lang,
                        onDate = { showDatePicker(context, fromDate) { fromDate = it } },
                        onHour = { fromHour = it },
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier.width(1.dp).height(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                    )
                    RangeCell(
                        label = t("to"),
                        date = toDate,
                        hour = toHour,
                        lang = lang,
                        onDate = { showDatePicker(context, toDate) { toDate = it } },
                        onHour = { toHour = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    t("custom_hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TransferTile(
                        icon = Icons.Filled.ArrowDownward,
                        label = t("download"),
                        bytes = total[1],
                        tint = Color(0xFF35E0FF),
                        lang = lang,
                        modifier = Modifier.weight(1f)
                    )
                    TransferTile(
                        icon = Icons.Filled.ArrowUpward,
                        label = t("upload"),
                        bytes = total[0],
                        tint = Color(0xFFB86BFF),
                        lang = lang,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            t("total"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            formatBytes(total[0] + total[1], lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = AppGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                t("via_vpn"),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            formatBytes(rangeVpn, lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppGreen,
                            maxLines = 1
                        )
                    }
                }
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

        val ranged = remember(dailyCfg, hourlyCfg, bars, hourlyMode) {
            UsageStore.configTotalsRange(dailyCfg, hourlyCfg, bars, hourlyMode)
        }
        val direct = ranged.firstOrNull { it.first == UsageStore.DIRECT_KEY }?.second
            ?: longArrayOf(0L, 0L)
        val perConfig = ranged.filter { it.first != UsageStore.DIRECT_KEY }
        val grand = direct[0] + direct[1] + perConfig.sumOf { it.second[0] + it.second[1] }

        if (grand > 0L) {
            SettingsGroup(t("usage_by_config")) {
                UsageShareRow(
                    name = t("usage_direct"),
                    bytes = direct[0] + direct[1],
                    grand = grand,
                    tint = DirectBarColor,
                    lang = lang
                )
                perConfig.take(8).forEachIndexed { i, (name, v) ->
                    UsageShareRow(
                        name = name,
                        bytes = v[0] + v[1],
                        grand = grand,
                        tint = ServerPalette[i % ServerPalette.size],
                        lang = lang
                    )
                }
            }
        }
    }
}

private val DirectBarColor = Color(0xFF8A94A6)

private val ServerPalette = listOf(
    Color(0xFFFFA94D), Color(0xFFFF6BC1), Color(0xFF6D9BEE),
    Color(0xFFFFD24D), Color(0xFFFF7A6B), Color(0xFF9BE85B)
)

@Composable
private fun TransferTile(
    icon: ImageVector,
    label: String,
    bytes: Long,
    tint: Color,
    lang: Lang,
    modifier: Modifier = Modifier
) {
    val parts = formatBytesParts(bytes, lang)
    Column(
        modifier.clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                parts.first,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.width(4.dp))
            Text(
                parts.second,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun RangeCell(
    label: String,
    date: java.time.LocalDate,
    hour: Int,
    lang: Lang,
    onDate: () -> Unit,
    onHour: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    Column(
        modifier.padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            mixedText(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                localizeDigits("${date.year}/${date.monthValue}/${date.dayOfMonth}", lang),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = if (lang == Lang.FA) VazirFont else LexendFont,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onDate() }
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            )
            Spacer(Modifier.width(6.dp))
            Box {
                Text(
                    localizeDigits(String.format(java.util.Locale.US, "%02d:00", hour), lang),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = if (lang == Lang.FA) VazirFont else LexendFont,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { open = true }
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                )
                DropdownMenu(
                    expanded = open,
                    onDismissRequest = { open = false },
                    offset = DpOffset(0.dp, 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    (0..23).forEach { h ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    localizeDigits(String.format(java.util.Locale.US, "%02d:00", h), lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = if (lang == Lang.FA) VazirFont else LexendFont
                                )
                            },
                            trailingIcon = {
                                if (h == hour) Icon(
                                    Icons.Filled.Check, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(38.dp),
                            onClick = { onHour(h); open = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageShareRow(
    name: String,
    bytes: Long,
    grand: Long,
    tint: Color,
    lang: Lang
) {
    val frac = if (grand > 0L) (bytes.toFloat() / grand.toFloat()).coerceIn(0f, 1f) else 0f
    val width by animateFloatAsState(frac, tween(500), label = "usageShare")
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                flagRuns(name, LexendFont),
                inlineContent = flagInlineContent(name, MaterialTheme.typography.bodyMedium.fontSize),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                formatBytes(bytes, lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f))
        ) {
            Box(
                Modifier.fillMaxWidth(width).fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(tint)
            )
        }
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
                    }
                }
            }
            if (f == null) {
                Text(t("peak_per_bar").format(formatBytes(maxVal, lang)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            var rowWidth by remember { mutableStateOf(1) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(mixedText(title), style = MaterialTheme.typography.bodyLarge)
            Text(mixedText(subtitle), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun GlobeStyleOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    else Color.Transparent
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, border), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
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
    accent: Color = MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit
) {
    val primary = accent
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
    accent: Color = MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit
) = FillButton(onClick, modifier, enabled, borderWidth = 1.5.dp,
    minHeight = minHeight, contentPadding = contentPadding, accent = accent, content = content)

@Composable
private fun BounceTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) = FillButton(
    onClick, modifier, enabled,
    borderWidth = 1.5.dp,
    minHeight = 40.dp,
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    content = content
)

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
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = if (isDark) color else lerp(color, Color.Black, 0.42f)
    val hazeState = LocalHazeState.current
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (hazeState != null) Modifier.hazeEffect(hazeState) {
                    blurRadius = 10.dp
                    backgroundColor = surfaceColor
                    tints = listOf(HazeTint(surfaceColor.copy(alpha = 0.30f)))
                    noiseFactor = 0f
                } else Modifier.background(surfaceColor.copy(alpha = if (isDark) 0.55f else 0.75f))
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
    onRemoveTimedOut: () -> Unit,
    timedOutCount: Int,
    onPing: () -> Unit,
    pinging: Boolean,
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
        GlassDialog(
            onDismiss = { renaming = false },
            title = t("edit_sub_name"),
            confirmLabel = t("save"),
            dismissLabel = t("cancel"),
            onConfirm = {
                val nm = draftName.trim()
                if (nm.isNotEmpty()) onRename(nm)
                renaming = false
            }
        ) {
            OutlinedTextField(
                value = draftName,
                onValueChange = { draftName = it },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    val ws = WindscribeBrand.isWindscribe(sub)
    val brandBrush = if (ws) windscribeCardBrush() else null

    Card(
        modifier = modifier.appearOnce().fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ws) Color.Transparent
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            Modifier.fillMaxWidth()
                .then(if (brandBrush != null) Modifier.background(brandBrush) else Modifier)
                .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val chevron by animateFloatAsState(
                    targetValue = if (!isOpen) 0f else if (ws) 180f else 90f,
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    label = "subChevron"
                )
                if (ws) {
                    Image(
                        painter = painterResource(R.drawable.windscribe),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(end = 7.dp).size(24.dp)
                            .graphicsLayer { rotationZ = chevron }
                    )
                } else {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 7.dp).graphicsLayer { rotationZ = chevron }
                    )
                }
                Box(Modifier.weight(1f)) {
                    MarqueeName(
                        WindscribeBrand.displayName(sub, lang),
                        MaterialTheme.typography.titleSmall
                    )
                }
                Box {
                    Icon(Icons.Filled.Share, contentDescription = t("share"), tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { shareMenu = true }.padding(7.dp).size(21.dp))
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
                if (pinging) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(7.dp).size(21.dp)
                    )
                } else {
                    Icon(Icons.Filled.Speed, contentDescription = t("test_all"), tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onPing() }.padding(7.dp).size(21.dp))
                }
                Icon(Icons.Filled.Edit, contentDescription = t("edit_sub_name"), tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { draftName = sub.name; renaming = true }.padding(7.dp).size(21.dp))
                Icon(Icons.Filled.Refresh, contentDescription = t("refresh"), tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onRefresh() }.padding(7.dp).size(21.dp))
                Box {
                    var subPurgeMenu by remember { mutableStateOf(false) }
                    Icon(Icons.Filled.Delete, contentDescription = t("remove"), tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .clickable { subPurgeMenu = true }.padding(7.dp).size(21.dp))
                    DropdownMenu(
                        expanded = subPurgeMenu,
                        onDismissRequest = { subPurgeMenu = false },
                        offset = DpOffset(0.dp, 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        DropdownMenuItem(
                            text = { Text(t("delete_all_configs"), style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(40.dp),
                            onClick = { subPurgeMenu = false; onRemove() }
                        )
                        DropdownMenuItem(
                            text = { Text(t("delete_timed_out"), style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(Icons.Filled.TimerOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            enabled = timedOutCount > 0,
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(40.dp),
                            onClick = { subPurgeMenu = false; onRemoveTimedOut() }
                        )
                    }
                }
            }
            if (sub.total > 0) {
                Spacer(Modifier.height(6.dp))
                UsageBar(used = sub.used, total = sub.total)
            }
            val quota = quotaChips(sub, lang)
            if (quota.isNotEmpty()) {
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quota.forEach { (label, level) ->
                        QuotaChip(label, level)
                    }
                }
            }
        }
    }
}

private fun quotaChips(sub: Subscription, lang: Lang): List<Pair<String, Int>> {
    if (sub.total <= 0 && sub.expire <= 0) return emptyList()
    val parts = mutableListOf<Pair<String, Int>>()
    if (sub.total > 0) {
        val remaining = (sub.total - sub.used).coerceAtLeast(0)
        val frac = remaining.toFloat() / sub.total
        val level = when {
            frac <= 0.10f -> 2
            frac <= 0.30f -> 1
            else -> 0
        }
        parts.add(
            "${formatBytes(remaining, lang)} ${Strings.get(lang, "of")} " +
                    "${formatBytes(sub.total, lang)} ${Strings.get(lang, "left")}" to level
        )
    }
    if (sub.expire > 0) {
        val daysLeft = (sub.expire * 1000 - System.currentTimeMillis()) / 86_400_000L
        if (daysLeft >= 0) {
            val level = when {
                daysLeft <= 1L -> 2
                daysLeft <= 3L -> 1
                else -> 0
            }
            parts.add(
                "${Strings.get(lang, "expires_in")} " +
                        "${localizeDigits("$daysLeft", lang)}${Strings.get(lang, "unit_days")}" to level
            )
        }
    }
    return parts
}

@Composable
private fun QuotaChip(label: String, level: Int) {
    val accent = when (level) {
        2 -> Color(0xFFE53935)
        1 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }
    Text(
        mixedText(label),
        style = MaterialTheme.typography.labelSmall,
        color = accent,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
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

@Composable
private fun ChainPickerDialog(
    store: ConfigStore,
    config: ProxyConfig,
    onDismiss: () -> Unit
) {
    val t = stringsFn()
    val configs by store.configs.collectAsState()
    val options = configs.filter { it.id != config.id && it.protocol != "tor" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
        ) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        t("chain_through"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    t("chain_hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TorCountryGroup(t("chain_carrier")) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item(key = "chain-none") {
                            ChainOptionRow(
                                label = t("chain_none"),
                                selected = config.chainId.isEmpty(),
                                onClick = {
                                    store.update(config.copy(chainId = ""))
                                    DebugRunner.clear(config.id)
                                    onDismiss()
                                }
                            )
                        }
                        items(options, key = { it.id }) { option ->
                            ChainOptionRow(
                                label = option.name,
                                selected = config.chainId == option.id,
                                onClick = {
                                    store.update(config.copy(chainId = option.id))
                                    DebugRunner.clear(config.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                BounceTextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(t("cancel"))
                }
            }
        }
    }
}

@Composable
private fun ChainOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "chainRowTint"
    )
    val fill by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "chainRowFill"
    )
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.05f + 0.09f * fill))
            .border(1.dp, tint.copy(alpha = 0.16f + 0.36f * fill), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            flagRuns(label, if (LocalLang.current == Lang.FA) VazirFont else LexendFont),
            inlineContent = flagInlineContent(
                label,
                MaterialTheme.typography.bodyMedium.fontSize
            ),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        SmoothCheckbox(checked = selected)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QrDialog(link: String, title: String, onDismiss: () -> Unit) {
    val t = stringsFn()
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary
    val qrBg = Color(0xFF0E1422)
    val qrFg = lerp(Color.White, accent, 0.06f)
    val bmp = remember(link, qrBg, qrFg) {
        ConfigShare.qrBitmap(link, darkColor = qrFg.toArgb(), lightColor = qrBg.toArgb())
    }

    val pulseTr = rememberInfiniteTransition(label = "qrPulse")
    val strokeAlpha by pulseTr.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "qrStroke"
    )

    fun shareImage() {
        val image = bmp ?: return
        runCatching {
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(dir, "groute-qr.png")
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val uri = FileProvider.getUriForFile(
                context, context.packageName + ".fileprovider", file
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, title))
        }
    }

    GlassDialog(
        onDismiss = onDismiss,
        title = t("qr_title"),
        confirmLabel = if (bmp == null) t("cancel") else t("share"),
        dismissLabel = if (bmp == null) null else t("cancel"),
        onConfirm = { if (bmp == null) onDismiss() else shareImage() }
    ) {
        if (bmp == null) {
            Text(
                mixedText(t("qr_too_long")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                mixedText(title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(240.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(qrBg)
                        .border(
                            1.dp,
                            accent.copy(alpha = strokeAlpha),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(12.dp)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        filterQuality = FilterQuality.None,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(
                mixedText(t("qr_hint")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QrScannerScreen(onResult: (String) -> Unit) {
    val t = stringsFn()
    val context = LocalContext.current
    val lifecycleOwner = remember(context) {
        generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<LifecycleOwner>()
            .firstOrNull()
    }

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    LaunchedEffect(Unit) {
        if (!granted) permLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(Modifier.fillMaxSize()) {
        if (!granted) {
            Column(
                Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    mixedText(t("camera_needed")),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                BounceOutlinedButton(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text(t("camera_grant"))
                }
            }
            return@Column
        }

        var handled by remember { mutableStateOf(false) }

        Box(
            Modifier.weight(1f).fillMaxWidth().padding(16.dp)
                .clip(RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val view = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    android.util.Log.d("GRouteQr", "factory: creating PreviewView")
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        runCatching {
                            val provider = providerFuture.get()
                            android.util.Log.d("GRouteQr", "provider ready, owner=$lifecycleOwner")
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(view.surfaceProvider)
                            }
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            val reader = MultiFormatReader().apply {
                                setHints(
                                    mapOf(
                                        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                                        DecodeHintType.TRY_HARDER to true
                                    )
                                )
                            }
                            analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                                if (!handled) {
                                    decodeQr(proxy, reader)?.let { text ->
                                        handled = true
                                        view.post { onResult(text) }
                                    }
                                }
                                proxy.close()
                            }
                            if (lifecycleOwner == null) {
                                android.util.Log.e("GRouteQr", "no LifecycleOwner found - cannot bind")
                            } else {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analysis
                                )
                                android.util.Log.d("GRouteQr", "camera bound")
                            }
                        }.onFailure {
                            android.util.Log.e("GRouteQr", "camera setup failed", it)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    view
                }
            )
            Box(
                Modifier.size(214.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
            )
        }

        Text(
            mixedText(t("scan_qr_hint")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)
        )
    }
}

private fun decodeQr(proxy: ImageProxy, reader: MultiFormatReader): String? {
    val buffer = proxy.planes[0].buffer
    val data = ByteArray(buffer.remaining()).also { buffer.get(it) }
    val source = PlanarYUVLuminanceSource(
        data, proxy.planes[0].rowStride, proxy.height,
        0, 0, proxy.width, proxy.height, false
    )
    val attempt = { src: LuminanceSource ->
        runCatching { reader.decodeWithState(BinaryBitmap(HybridBinarizer(src))).text }
            .getOrNull()
    }
    reader.reset()
    return attempt(source) ?: run {
        reader.reset()
        attempt(source.invert())
    }
}

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
    onChain: () -> Unit,
    actionsOpen: Boolean,
    onToggleActions: () -> Unit,
    modifier: Modifier = Modifier,
    appear: Boolean = true,
    containerColor: Color? = null
) {
    val t = stringsFn()
    val lang = LocalLang.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var shareMenu by remember { mutableStateOf(false) }
    var qrFor by remember { mutableStateOf<String?>(null) }
    val checked by remember { derivedStateOf { isChecked() } }

    qrFor?.let { link ->
        QrDialog(link = link, title = config.name, onDismiss = { qrFor = null })
    }

    val highlight by animateColorAsState(
        targetValue = when {
            checked || isSelected -> MaterialTheme.colorScheme.primaryContainer
            containerColor != null -> containerColor
            else -> Color.Transparent
        },
        animationSpec = tween(220),
        label = "rowHighlight"
    )

    val swipeRed = Color(0xFFE0413C)
    var rowWidth by remember { mutableStateOf(1) }
    var dragX by remember { mutableStateOf(0f) }
    val dragEnabled = !selectionMode && !checked

    val swiping = dragX < -6f
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(swiping) {
        if (swiping) {
            runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
            buzz(context)
        }
    }
    LaunchedEffect(dragEnabled) {
        if (!dragEnabled) dragX = 0f
    }

    val rowTint by animateColorAsState(
        targetValue = if (swiping) swipeRed else highlight,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "rowTint"
    )

    Card(
        modifier = (if (appear) modifier.appearOnce() else modifier)
            .fillMaxWidth()
            .onSizeChanged { rowWidth = it.width }
            .offset { IntOffset(dragX.roundToInt(), 0) }
            .clip(RoundedCornerShape(14.dp))
            .draggable(
                orientation = Orientation.Horizontal,
                enabled = dragEnabled,
                state = rememberDraggableState { delta ->
                    dragX = (dragX + delta).coerceIn(-rowWidth.toFloat(), 0f)
                },
                onDragStopped = {
                    if (-dragX >= rowWidth * (1f / 3f)) {
                        runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                        buzz(context)
                        animate(
                            initialValue = dragX,
                            targetValue = -rowWidth.toFloat(),
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        ) { value, _ -> dragX = value }
                        onDelete()
                    } else {
                        animate(
                            initialValue = dragX,
                            targetValue = 0f,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) { value, _ -> dragX = value }
                    }
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        shape = RoundedCornerShape(14.dp),
        colors = if (containerColor != null)
            CardDefaults.cardColors(containerColor = containerColor)
        else CardDefaults.cardColors()
    ) {
        Row(
            Modifier.fillMaxWidth().background(rowTint)
                .padding(start = 14.dp, end = 9.dp, top = 10.dp, bottom = 10.dp),
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
                        Box(Modifier.weight(1f)) { MarqueeName(config.name, color = MaterialTheme.colorScheme.onSurface) }
                    }
                } else {
                    MarqueeName(config.name, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    if (config.locked) AnnotatedString(t("locked_config"))
                    else scriptRuns("${config.address}:${config.port}", LexendFont),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(3.dp))
            PingChip(ping)
            AnimatedVisibility(
                visible = actionsOpen && !checked && !selectionMode,
                enter = fadeIn(tween(220)) + expandHorizontally(
                    tween(300, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.End
                ),
                exit = fadeOut(tween(150)) + shrinkHorizontally(
                    tween(260, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.End
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Icon(Icons.Filled.Share, contentDescription = t("share"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(CircleShape).clickable { shareMenu = true }.padding(4.dp).size(21.dp))
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
                                CompactMenuItem(Icons.Filled.QrCode2, t("qr_share")) {
                                    shareMenu = false
                                    qrFor = ConfigShare.toLink(config)
                                }
                            }
                            CompactMenuItem(Icons.Filled.InsertDriveFile, t("share_file")) {
                                shareMenu = false
                                onShareFile()
                            }
                        }
                    }
                    Icon(
                        Icons.Filled.Layers,
                        contentDescription = t("chain_through"),
                        tint = if (config.chainId.isNotEmpty()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clip(CircleShape).clickable { onChain() }
                            .padding(4.dp).size(21.dp)
                    )
                    Icon(Icons.Filled.Edit, contentDescription = t("edit"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(CircleShape).clickable { onEdit() }.padding(4.dp).size(21.dp))
                    Icon(Icons.Filled.Delete, contentDescription = t("delete"),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clip(CircleShape).clickable { onDelete() }.padding(4.dp).size(21.dp))
                }
            }
            if (!checked && !selectionMode) {
                Box(Modifier.size(29.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = swiping,
                        enter = fadeIn(tween(150)) + scaleIn(tween(180), initialScale = 0.65f),
                        exit = fadeOut(tween(150)) + scaleOut(tween(180), targetScale = 0.65f)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = t("delete"),
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !swiping,
                        enter = fadeIn(tween(150)) + scaleIn(tween(180), initialScale = 0.65f),
                        exit = fadeOut(tween(150)) + scaleOut(tween(180), targetScale = 0.65f)
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = null,
                            tint = if (actionsOpen) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clip(CircleShape)
                                .clickable { onToggleActions() }
                                .padding(4.dp).size(21.dp)
                        )
                    }
                }
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
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Close, contentDescription = t("cancel"),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clip(CircleShape).clickable { onClose() }.padding(9.dp).size(26.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "${localizeDigits("$count", lang)} ${t("selected")}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Box {
                Icon(Icons.Filled.Share, contentDescription = t("share"),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clip(CircleShape).clickable { shareMenu = true }.padding(9.dp).size(26.dp))
                DropdownMenu(expanded = shareMenu, onDismissRequest = { shareMenu = false }) {
                    CompactMenuItem(Icons.Filled.ContentCopy, t("share_clipboard")) { shareMenu = false; onCopy() }
                    CompactMenuItem(Icons.Filled.Share, t("share_app")) { shareMenu = false; onShareApp() }
                    CompactMenuItem(Icons.Filled.InsertDriveFile, t("share_file")) { shareMenu = false; onShareFile() }
                }
            }
            Icon(Icons.Filled.Delete, contentDescription = t("delete"),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.clip(CircleShape).clickable { onDelete() }.padding(9.dp).size(26.dp))
        }
    }
}

private val PersianRange = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\uFB50-\\uFDFF\\uFE70-\\uFEFF]")

internal fun scriptFont(text: String): FontFamily =
    if (PersianRange.containsMatchIn(text)) VazirFont else LexendFont

private fun isPersianChar(c: Char) =
    c in '\u0600'..'\u06FF' || c in '\u0750'..'\u077F' ||
            c in '\uFB50'..'\uFDFF' || c in '\uFE70'..'\uFEFF'

private fun buzz(context: Context) {
    android.util.Log.d("GRouteHaptic", "buzz() called")
    runCatching {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as android.os.VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
        if (!vibrator.hasVibrator()) {
            android.util.Log.w("GRouteHaptic", "device reports no vibrator")
            return
        }
        val amplitude = if (vibrator.hasAmplitudeControl()) 255
        else android.os.VibrationEffect.DEFAULT_AMPLITUDE
        val effect = android.os.VibrationEffect.createOneShot(50, amplitude)
        vibrator.cancel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(
                effect,
                android.os.VibrationAttributes.createForUsage(
                    android.os.VibrationAttributes.USAGE_HARDWARE_FEEDBACK
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(
                effect,
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
    }
}

@Composable
internal fun monoFont(): FontFamily =
    if (LocalLang.current == Lang.FA) VazirFont else MonoFont

@Composable
internal fun monoLatinFont(): FontFamily =
    if (LocalLang.current == Lang.FA) LexendFont else MonoFont

@Composable
internal fun monoText(text: String): AnnotatedString =
    scriptRuns(text, monoLatinFont())

@Composable
internal fun accentText(text: String, vararg terms: String): AnnotatedString {
    val base = mixedText(text)
    val accent = MaterialTheme.colorScheme.primary
    return buildAnnotatedString {
        append(base)
        for (term in terms) {
            if (term.isEmpty()) continue
            var i = text.indexOf(term)
            while (i >= 0) {
                addStyle(
                    SpanStyle(color = accent, fontWeight = FontWeight.SemiBold),
                    i,
                    i + term.length
                )
                i = text.indexOf(term, i + term.length)
            }
        }
    }
}

@Composable
internal fun mixedText(text: String): AnnotatedString =
    if (LocalLang.current == Lang.FA) scriptRuns(text, LexendFont) else AnnotatedString(text)

private fun scriptOf(c: Char): Boolean? = when {
    c in '\u06F0'..'\u06F9' -> true
    c in '\u0660'..'\u0669' -> true
    c.isLetter() -> isPersianChar(c)
    else -> null
}

internal fun scriptRuns(text: String, latin: FontFamily): AnnotatedString = buildAnnotatedString {
    if (text.isEmpty()) return@buildAnnotatedString
    var persian = text.firstNotNullOfOrNull { scriptOf(it) } ?: false
    var start = 0
    for (i in text.indices) {
        val p = scriptOf(text[i]) ?: continue
        if (p != persian) {
            withStyle(SpanStyle(fontFamily = if (persian) VazirFont else latin)) {
                append(text.substring(start, i))
            }
            start = i
            persian = p
        }
    }
    withStyle(SpanStyle(fontFamily = if (persian) VazirFont else latin)) {
        append(text.substring(start))
    }
}

@Composable
private fun MarqueeName(text: String, style: TextStyle? = null, color: Color = Color.Unspecified) {
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
            flagRuns(text, LexendFont),
            inlineContent = flagInlineContent(text, style?.fontSize ?: 14.sp),
            style = style ?: MaterialTheme.typography.titleSmall,
            color = color,
            fontSize = if (style == null) 14.sp else TextUnit.Unspecified,
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
    val target = pingColor(ping)
    val color by animateColorAsState(target, tween(400), label = "pingChipTint")
    val text = when (ping) {
        is PingResult.Ok -> "${localizeDigits("${ping.ms}", lang)} ${t("unit_ms")}"
        PingResult.Testing -> t("testing")
        else -> t("delay_failed")
    }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(
        if (shown) 1f else 0f,
        tween(320, easing = FastOutSlowInEasing),
        label = "pingChipAppear"
    )
    Box(
        Modifier.graphicsLayer {
            alpha = appear
            val sc = 0.85f + 0.15f * appear
            scaleX = sc
            scaleY = sc
        }
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .animateContentSize(tween(320, easing = FastOutSlowInEasing))
    ) {
        Crossfade(targetState = text, animationSpec = tween(300), label = "pingChipText") { s ->
            Text(
                s,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = LexendFont,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun pingColor(ping: PingResult?): Color = when (ping) {
    is PingResult.Ok -> when {
        ping.ms <= 250 -> Color(0xFF2E9E44)
        ping.ms <= 600 -> Color(0xFFF59E0B)
        else -> Color(0xFFE53935)
    }
    PingResult.Failed -> if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
        Color(0xFFBFBFBF) else Color(0xFF4A4A4A)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun PingBadge(ping: PingResult?) {
    val t = stringsFn()
    val lang = LocalLang.current
    when (ping) {
        is PingResult.Ok -> Text("${localizeDigits("${ping.ms}", lang)} ${t("unit_ms")}", style = MaterialTheme.typography.bodySmall, fontFamily = LexendFont, color = pingColor(ping))
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
    val focus = LocalFocusManager.current
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
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                PerAppMode.OFF to t("per_app_off"),
                PerAppMode.ALLOWLIST to t("per_app_allow"),
                PerAppMode.BLOCKLIST to t("per_app_block")
            ).forEach { (value, label) ->
                ModeSegment(
                    label = label,
                    active = mode == value,
                    onClick = { store.setPerAppMode(value) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (mode == PerAppMode.OFF) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(58.dp).clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        t("per_app_off_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(t("search_apps")) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.clickable { query = ""; focus.clearFocus() }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        localizeDigits("${filtered.size}", lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = LexendFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        localizeDigits("${selected.size}", lang) + " " + t("selected"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.pkg }) { app ->
                        val checked = app.pkg in selected
                        val tint by animateColorAsState(
                            targetValue = if (checked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            label = "appRowTint"
                        )
                        val fill by animateFloatAsState(
                            targetValue = if (checked) 1f else 0f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            label = "appRowFill"
                        )
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(tint.copy(alpha = 0.05f + 0.09f * fill))
                                .border(
                                    1.dp,
                                    tint.copy(alpha = 0.16f + 0.34f * fill),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { store.togglePerApp(app.pkg) }
                                .animateItem()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    app.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = scriptFont(app.label),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    app.pkg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            SmoothCheckbox(checked = checked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "modeSegFill"
    )
    val primary = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(primary.copy(alpha = 0.20f * fill))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = lerp(idle, primary, fill),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}