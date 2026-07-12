package net.gozar.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val PARTICLE_COUNT = 128
private const val FIELD_ALPHA = 0.35f

@Composable
fun ParticleField(modifier: Modifier = Modifier) {
    val conn by VpnState.state.collectAsState()
    val tint by animateColorAsState(
        targetValue = when (conn) {
            Connection.CONNECTED -> Color(0xFF4BF0A4)
            Connection.CONNECTING -> Color(0xFFFFA94D)
            else -> Color(0xFF5B83D6)
        },
        animationSpec = tween(500),
        label = "particleTint"
    )
    val density = LocalDensity.current.density

    val xs = remember { FloatArray(PARTICLE_COUNT) }
    val ys = remember { FloatArray(PARTICLE_COUNT) }
    val vxs = remember { FloatArray(PARTICLE_COUNT) }
    val vys = remember { FloatArray(PARTICLE_COUNT) }
    val phs = remember { FloatArray(PARTICLE_COUNT) }
    val rs = remember { FloatArray(PARTICLE_COUNT) }
    var fieldW by remember { mutableStateOf(0f) }
    var fieldH by remember { mutableStateOf(0f) }
    var seeded by remember { mutableStateOf(false) }
    var nowMs by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = 0L
        while (isActive) {
            withFrameNanos { t ->
                val dtMs = if (last == 0L) 0f else min(64f, (t - last) / 1_000_000f)
                last = t
                nowMs = t / 1_000_000
                if (seeded && dtMs > 0f) {
                    val s = dtMs / 1000f
                    val m = 12f * density
                    val w = fieldW
                    val h = fieldH
                    for (i in 0 until PARTICLE_COUNT) {
                        xs[i] += vxs[i] * s
                        ys[i] += vys[i] * s
                        if (xs[i] < -m) xs[i] = w + m
                        if (xs[i] > w + m) xs[i] = -m
                        if (ys[i] < -m) ys[i] = h + m
                        if (ys[i] > h + m) ys[i] = -m
                    }
                }
            }
        }
    }

    Canvas(
        modifier.onSizeChanged { sz ->
            val w = sz.width.toFloat()
            val h = sz.height.toFloat()
            if (w > 20f && h > 20f && (w != fieldW || h != fieldH)) {
                fieldW = w
                fieldH = h
                val rnd = Random(7)
                for (i in 0 until PARTICLE_COUNT) {
                    xs[i] = rnd.nextFloat() * w
                    ys[i] = rnd.nextFloat() * h
                    val ang = rnd.nextFloat() * (PI.toFloat() * 2f)
                    val spd = (6f + rnd.nextFloat() * 9f) * density
                    vxs[i] = cos(ang) * spd
                    vys[i] = sin(ang) * spd
                    phs[i] = rnd.nextFloat() * (PI.toFloat() * 2f)
                    rs[i] = (1.1f + rnd.nextFloat() * 1.3f) * density
                }
                seeded = true
            }
        }
    ) {
        if (!seeded) return@Canvas
        val now = nowMs
        val linkDist = 96.dp.toPx()
        val linkDist2 = linkDist * linkDist
        val sw = 1.dp.toPx()

        for (i in 0 until PARTICLE_COUNT) {
            for (j in i + 1 until PARTICLE_COUNT) {
                val dx = xs[i] - xs[j]
                val dy = ys[i] - ys[j]
                val d2 = dx * dx + dy * dy
                if (d2 > linkDist2) continue
                val d = sqrt(d2)
                val a = (1f - d / linkDist) * 0.30f * FIELD_ALPHA
                drawLine(
                    color = tint.copy(alpha = a),
                    start = Offset(xs[i], ys[i]),
                    end = Offset(xs[j], ys[j]),
                    strokeWidth = sw
                )
            }
        }

        for (i in 0 until PARTICLE_COUNT) {
            val tw = 0.5f + 0.5f * sin(now / 900f + phs[i])
            val mapped = 0.55f + 0.45f * tw
            val glowAlpha = (38f / 255f) * mapped * FIELD_ALPHA
            val coreAlpha = ((70f + 150f * mapped) / 255f) * FIELD_ALPHA
            val c = Offset(xs[i], ys[i])
            drawCircle(color = tint.copy(alpha = glowAlpha), radius = rs[i] * 3.2f, center = c)
            drawCircle(color = tint.copy(alpha = coreAlpha), radius = rs[i], center = c)
        }
    }
}