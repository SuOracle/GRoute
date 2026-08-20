package net.gozar.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

private val TermFontSize = 12.sp
private val TermLineHeight = 16.sp
private val TermBlue = Color(0xFF6D9BEE)
private val IpPattern = Regex(
    "\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?\\b" +
        "|(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}" +
        "|[0-9a-fA-F]{0,4}(?::[0-9a-fA-F]{1,4})*::(?:[0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{0,4}"
)

private fun highlightIps(text: String, base: Color): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in IpPattern.findAll(text)) {
        if (m.range.first > last) {
            withStyle(SpanStyle(color = base)) { append(text.substring(last, m.range.first)) }
        }
        withStyle(SpanStyle(color = TermBlue)) { append(m.value) }
        last = m.range.last + 1
    }
    if (last < text.length) {
        withStyle(SpanStyle(color = base)) { append(text.substring(last)) }
    }
}

@Composable
fun TerminalScreen(
    shell: ShellSession,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lines by shell.lines.collectAsState()
    val busy by shell.busy.collectAsState()
    val running by shell.running.collectAsState()
    val history by shell.history.collectAsState()
    val partial by shell.partial.collectAsState()

    var input by remember { mutableStateOf(TextFieldValue("")) }
    var historyIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()
    val focus = remember { FocusRequester() }

    BackHandler { onBack() }

    LaunchedEffect(lines.size, partial) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    fun submit() {
        if (busy) return
        val cmd = input.text
        input = TextFieldValue("")
        historyIndex = -1
        shell.send(cmd)
    }

    fun moveCursor(delta: Int) {
        val pos = (input.selection.start + delta).coerceIn(0, input.text.length)
        input = input.copy(selection = TextRange(pos))
    }

    fun stepHistory(delta: Int) {
        if (history.isEmpty()) return
        val next = when {
            historyIndex < 0 && delta < 0 -> history.lastIndex
            historyIndex < 0 -> return
            else -> historyIndex + delta
        }
        if (next < 0 || next > history.lastIndex) {
            historyIndex = -1
            input = TextFieldValue("")
            return
        }
        historyIndex = next
        val text = history[next]
        input = TextFieldValue(text, TextRange(text.length))
    }

    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Column(
        Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.Top
      ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { runCatching { focus.requestFocus() } },
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(lines) { line ->
                Row(Modifier.fillMaxWidth()) {
                    if (line.kind == ShellLineKind.INPUT && !shell.echoesInput) {
                        Text(
                            "$",
                            fontFamily = monoFont(),
                            fontSize = TermFontSize,
                            lineHeight = TermLineHeight,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(7.dp))
                    }
                    val lineColor = when (line.kind) {
                        ShellLineKind.ERROR -> MaterialTheme.colorScheme.error
                        ShellLineKind.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Text(
                        highlightIps(line.text, lineColor),
                        fontFamily = monoFont(),
                        fontSize = TermFontSize,
                        lineHeight = TermLineHeight,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (partial.isNotEmpty()) {
                Text(
                    highlightIps(partial, MaterialTheme.colorScheme.onSurface),
                    fontFamily = monoFont(),
                    fontSize = TermFontSize,
                    lineHeight = TermLineHeight,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(7.dp))
            }
            AnimatedVisibility(visible = busy, enter = fadeIn(tween(160)), exit = fadeOut(tween(120))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                }
            }
            if (!busy && !shell.echoesInput) {
                Text(
                    "$",
                    fontFamily = monoFont(),
                    fontSize = TermFontSize,
                    lineHeight = TermLineHeight,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(7.dp))
            }
            BasicTextField(
                value = input,
                onValueChange = { input = it; historyIndex = -1 },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = monoFont(),
                    fontSize = TermFontSize,
                    lineHeight = TermLineHeight,
                    color = if (busy) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(
                    if (busy) Color.Transparent else MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { submit() }),
                modifier = Modifier.weight(1f).focusRequester(focus)
            )
        }
      }

        Spacer(Modifier.size(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TermKey(
                icon = Icons.Filled.Stop,
                accent = MaterialTheme.colorScheme.error,
                enabled = running
            ) {
                if (input.text.isNotEmpty()) input = TextFieldValue("") else shell.interrupt()
            }
            TermKey(
                icon = Icons.Filled.PlayArrow,
                accent = Color(0xFF2E9E44),
                enabled = !running
            ) { shell.reconnect() }
            TermKey(icon = Icons.Filled.Keyboard) {
                runCatching { focus.requestFocus() }
                keyboard?.show()
            }
            Spacer(Modifier.weight(1f))
            TermKey(Icons.Filled.KeyboardArrowLeft, repeatable = true) { moveCursor(-1) }
            TermKey(Icons.Filled.KeyboardArrowUp, repeatable = true) { stepHistory(-1) }
            TermKey(Icons.Filled.KeyboardArrowDown, repeatable = true) { stepHistory(1) }
            TermKey(Icons.Filled.KeyboardArrowRight, repeatable = true) { moveCursor(1) }
        }
    }
}

@Composable
private fun TermKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color = Color.Unspecified,
    repeatable: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val base = if (accent == Color.Unspecified) MaterialTheme.colorScheme.primary else accent
    val tint = if (enabled) base else base.copy(alpha = 0.28f)
    val action by rememberUpdatedState(onClick)
    var pressed by remember { mutableStateOf(false) }

    if (repeatable) {
        LaunchedEffect(pressed) {
            if (!pressed || !enabled) return@LaunchedEffect
            action()
            delay(380)
            while (pressed) {
                action()
                delay(55)
            }
        }
    }

    val keyBase = Modifier
        .size(44.dp, 38.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(tint.copy(alpha = if (pressed) 0.22f else 0.10f))
        .border(1.dp, tint.copy(alpha = 0.30f), RoundedCornerShape(12.dp))

    val gesture = if (!enabled) keyBase else if (repeatable) {
        keyBase.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
    } else {
        keyBase.clickable { action() }
    }

    Box(gesture, contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    }
}
