package net.gozar.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val DONATION_CARD_NUMBER: String =
    BuildConfig.DONATION_CARD_NUMBER.ifBlank { "0000 0000 0000 0000" }
private val DONATION_CARD_HOLDER: String =
    BuildConfig.DONATION_CARD_HOLDER.ifBlank { "—" }

@Composable
fun DonationScreen(modifier: Modifier = Modifier) {
    val lang = LocalLang.current
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2200)
            copied = false
        }
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (lang == Lang.FA)
                "حمایت مالی شما عزیزان برای من به شدت ارزشمنده. در جهت فراگیر کردن این اپلیکیشن و پیاده سازی ایده های بزرگتر نیاز به سرور چه برای کانفیگ های رایگان اپ و چه برای هاست ایده های آینده وجود داره. در صورتی که دوست داشتین میتونید مبلغی رو به شماره کارت زیر بزنید تا توی توسعه جی روت شریک باشید. ممنون از همتون ❤\uFE0F"
            else
                "Your financial support means a great deal to me. To make this app more widely available and to bring bigger ideas to life, servers are needed, both for the app's free configs and for hosting future ideas. If you'd like, you can send any amount to the card number below to be part of GRoute's development. Thank you all ❤",
            style = MaterialTheme.typography.bodyMedium
        )

        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF13234A), Color(0xFF24407F), Color(0xFF3D6AD6))
                    )
                )
                .clickable {
                    clipboard.setText(AnnotatedString(DONATION_CARD_NUMBER.replace(" ", "")))
                    copied = true
                }
                .padding(22.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "GRoute",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            Icons.Filled.CreditCard,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Box(
                        Modifier.size(46.dp, 34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE8C670), Color(0xFFB98A2F))
                                )
                            )
                    )
                    Text(
                        DONATION_CARD_NUMBER,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        letterSpacing = 2.sp,
                        maxLines = 1
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                if (lang == Lang.FA) "به نام" else "CARD HOLDER",
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                DONATION_CARD_HOLDER,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (copied) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4BF0A4),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    if (lang == Lang.FA) " کپی شد" else " Copied",
                                    color = Color(0xFF4BF0A4),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            Text(
                                if (lang == Lang.FA) "برای کپی لمس کنید" else "Tap to copy",
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Text(
            if (lang == Lang.FA)
                "شماره کارت با لمس کارت در کلیپ‌بورد کپی می‌شود."
            else
                "Tapping the card copies the number to your clipboard.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}