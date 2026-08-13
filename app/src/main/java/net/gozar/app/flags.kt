package net.gozar.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val RI_FIRST = 0x1F1E6
private const val RI_LAST = 0x1F1FF
private const val FLAG_TAG = "flag:"

private val flagCache = HashMap<String, Int>()

internal fun flagEmoji(cc: String): String {
    if (cc.length != 2) return ""
    val a = RI_FIRST + (cc[0].lowercaseChar() - 'a')
    val b = RI_FIRST + (cc[1].lowercaseChar() - 'a')
    return String(Character.toChars(a)) + String(Character.toChars(b))
}

internal fun splitFlags(text: String): List<Pair<Boolean, String>> {
    val out = ArrayList<Pair<Boolean, String>>()
    val plain = StringBuilder()
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        val n = Character.charCount(cp)
        if (cp in RI_FIRST..RI_LAST && i + n < text.length) {
            val cp2 = text.codePointAt(i + n)
            if (cp2 in RI_FIRST..RI_LAST) {
                if (plain.isNotEmpty()) { out.add(false to plain.toString()); plain.setLength(0) }
                val a = 'a' + (cp - RI_FIRST)
                val b = 'a' + (cp2 - RI_FIRST)
                out.add(true to ("" + a + b))
                i += n + Character.charCount(cp2)
                continue
            }
        }
        plain.append(text, i, i + n)
        i += n
    }
    if (plain.isNotEmpty()) out.add(false to plain.toString())
    return out
}

internal fun hasFlagEmoji(text: String): Boolean = splitFlags(text).any { it.first }

internal fun flagRuns(text: String, latin: FontFamily): AnnotatedString {
    val parts = splitFlags(text)
    if (parts.none { it.first }) return scriptRuns(text, latin)
    return buildAnnotatedString {
        parts.forEach { (isFlag, value) ->
            if (isFlag) appendInlineContent(FLAG_TAG + value, "\u2691")
            else append(scriptRuns(value, latin))
        }
    }
}

@Composable
internal fun flagInlineContent(text: String, fontSize: TextUnit): Map<String, InlineTextContent> {
    val codes = remember(text) { splitFlags(text).filter { it.first }.map { it.second }.distinct() }
    if (codes.isEmpty()) return emptyMap()
    val size = if (fontSize == TextUnit.Unspecified) 14.sp else fontSize
    return remember(codes, size) {
        codes.associate { cc ->
            (FLAG_TAG + cc) to InlineTextContent(
                Placeholder(size * 1.34f, size, PlaceholderVerticalAlign.Center)
            ) { FlagFace(cc, Modifier.fillMaxSize()) }
        }
    }
}

@Composable
private fun flagResource(cc: String): Int {
    val context = LocalContext.current
    return remember(cc) {
        flagCache.getOrPut(cc) {
            runCatching {
                context.resources.getIdentifier("flag_" + cc, "drawable", context.packageName)
            }.getOrDefault(0)
        }
    }
}

@Composable
private fun FlagFace(cc: String, modifier: Modifier, emojiSize: TextUnit = TextUnit.Unspecified) {
    val resId = flagResource(cc)
    if (resId != 0) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(RoundedCornerShape(2.dp))
                .border(
                    0.7.dp,
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
                    RoundedCornerShape(2.dp)
                )
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(flagEmoji(cc), fontSize = emojiSize, maxLines = 1)
        }
    }
}

@Composable
internal fun flagBackdrop(countryCode: String, alpha: Float = 0.85f): Modifier {
    val cc = countryCode.trim().lowercase()
    val resId = if (cc.length == 2) flagResource(cc) else 0
    if (resId == 0) return Modifier
    val painter = painterResource(resId)
    return Modifier.drawWithContent {
        val iw = painter.intrinsicSize.width
        val ih = painter.intrinsicSize.height
        if (iw > 0f && ih > 0f && size.width > 0f && size.height > 0f) {
            val cover = maxOf(size.width / iw, size.height / ih)
            val w = iw * cover
            val h = ih * cover
            clipRect {
                translate(left = (size.width - w) / 2f, top = (size.height - h) / 2f) {
                    with(painter) { draw(Size(w, h), alpha = alpha) }
                }
            }
        }
        drawContent()
    }
}

@Composable
internal fun CountryFlag(
    countryCode: String,
    height: Dp = 13.dp,
    modifier: Modifier = Modifier
) {
    val cc = countryCode.trim().lowercase()
    if (cc.length != 2) return
    FlagFace(
        cc,
        modifier.width(height * 4f / 3f).height(height),
        emojiSize = height.value.sp
    )
}