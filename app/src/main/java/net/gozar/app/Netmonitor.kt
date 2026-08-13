package net.gozar.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL

object NetMonitor {

    data class Site(val name: String, val host: String)

    data class Category(val key: String, val icon: String, val sites: List<Site>)

    sealed class State {
        object Idle : State()
        object Testing : State()
        data class Reachable(val ms: Int) : State()
        object Sanctioned : State()
        object Unreachable : State()
    }

    val Essential = listOf(
        Site("Google", "www.google.com"),
        Site("YouTube", "www.youtube.com"),
        Site("Telegram", "core.telegram.org"),
        Site("WhatsApp", "web.whatsapp.com"),
        Site("Instagram", "www.instagram.com"),
        Site("GitHub", "github.com"),
        Site("Cloudflare", "www.cloudflare.com"),
        Site("OpenAI", "api.openai.com"),
        Site("Claude", "claude.ai"),
        Site("Google Play", "play.google.com")
    )

    private val Ai = listOf(
        Site("OpenAI", "api.openai.com"),
        Site("ChatGPT", "chatgpt.com"),
        Site("Claude", "claude.ai"),
        Site("Anthropic", "www.anthropic.com"),
        Site("Gemini", "gemini.google.com"),
        Site("Google AI Studio", "aistudio.google.com"),
        Site("Perplexity", "www.perplexity.ai"),
        Site("Copilot", "copilot.microsoft.com"),
        Site("DeepSeek", "www.deepseek.com"),
        Site("Mistral", "chat.mistral.ai"),
        Site("Grok", "grok.com"),
        Site("Hugging Face", "huggingface.co"),
        Site("Midjourney", "www.midjourney.com"),
        Site("Cursor", "www.cursor.com"),
        Site("OpenRouter", "openrouter.ai")
    )

    private val Gaming = listOf(
        Site("Steam", "store.steampowered.com"),
        Site("Steam Community", "steamcommunity.com"),
        Site("Activision", "www.activision.com"),
        Site("Epic Games", "store.epicgames.com"),
        Site("PlayStation", "www.playstation.com"),
        Site("Xbox", "www.xbox.com"),
        Site("Nintendo", "www.nintendo.com"),
        Site("Riot Games", "www.riotgames.com"),
        Site("Battle.net", "us.shop.battle.net"),
        Site("EA", "www.ea.com"),
        Site("Ubisoft", "store.ubisoft.com"),
        Site("Rockstar", "www.rockstargames.com"),
        Site("GOG", "www.gog.com"),
        Site("itch.io", "itch.io"),
        Site("Roblox", "www.roblox.com"),
        Site("Minecraft", "www.minecraft.net"),
        Site("Twitch", "www.twitch.tv"),
        Site("Supercell", "supercell.com")
    )

    private val Social = listOf(
        Site("Instagram", "www.instagram.com"),
        Site("Telegram", "core.telegram.org"),
        Site("WhatsApp", "web.whatsapp.com"),
        Site("X", "x.com"),
        Site("Facebook", "www.facebook.com"),
        Site("Reddit", "www.reddit.com"),
        Site("Discord", "discord.com"),
        Site("TikTok", "www.tiktok.com"),
        Site("LinkedIn", "www.linkedin.com"),
        Site("Snapchat", "www.snapchat.com"),
        Site("Pinterest", "www.pinterest.com"),
        Site("Threads", "www.threads.net"),
        Site("Signal", "signal.org"),
        Site("Tumblr", "www.tumblr.com"),
        Site("Mastodon", "mastodon.social"),
        Site("Spotify", "open.spotify.com"),
        Site("SoundCloud", "soundcloud.com"),
        Site("YouTube Music", "music.youtube.com"),
        Site("Netflix", "www.netflix.com"),
        Site("Twitch", "www.twitch.tv")
    )

    private val Trading = listOf(
        Site("TradingView", "www.tradingview.com"),
        Site("Binance", "www.binance.com"),
        Site("Coinbase", "www.coinbase.com"),
        Site("Kraken", "www.kraken.com"),
        Site("KuCoin", "www.kucoin.com"),
        Site("Bybit", "www.bybit.com"),
        Site("OKX", "www.okx.com"),
        Site("MEXC", "www.mexc.com"),
        Site("Bitget", "www.bitget.com"),
        Site("CoinMarketCap", "coinmarketcap.com"),
        Site("CoinGecko", "www.coingecko.com"),
        Site("Investing", "www.investing.com"),
        Site("Yahoo Finance", "finance.yahoo.com"),
        Site("Bloomberg", "www.bloomberg.com"),
        Site("MetaTrader", "www.metatrader5.com"),
        Site("Amazon", "www.amazon.com"),
        Site("eBay", "www.ebay.com"),
        Site("PayPal", "www.paypal.com")
    )

    private val News = listOf(
        Site("BBC", "www.bbc.com"),
        Site("BBC Persian", "www.bbc.com/persian"),
        Site("Iran International", "www.iranintl.com"),
        Site("Radio Farda", "www.radiofarda.com"),
        Site("DW", "www.dw.com"),
        Site("Manoto", "www.manototv.com"),
        Site("Reuters", "www.reuters.com"),
        Site("AP News", "apnews.com"),
        Site("Al Jazeera", "www.aljazeera.com"),
        Site("The Guardian", "www.theguardian.com"),
        Site("CNN", "edition.cnn.com"),
        Site("New York Times", "www.nytimes.com"),
        Site("Euronews", "www.euronews.com"),
        Site("France24", "www.france24.com"),
        Site("Independent Persian", "www.independentpersian.com")
    )

    private val Iranian = listOf(
        Site("Digikala", "www.digikala.com"),
        Site("Aparat", "www.aparat.com"),
        Site("Varzesh3", "www.varzesh3.com"),
        Site("Divar", "divar.ir"),
        Site("Snapp", "snapp.ir"),
        Site("Torob", "torob.com"),
        Site("Filimo", "www.filimo.com"),
        Site("Namava", "www.namava.ir"),
        Site("Balad", "balad.ir"),
        Site("Eitaa", "eitaa.com"),
        Site("Rubika", "rubika.ir"),
        Site("Bale", "bale.ai"),
        Site("Soroush", "splus.ir"),
        Site("Nobitex", "nobitex.ir"),
        Site("Wallex", "wallex.ir"),
        Site("Ramzinex", "ramzinex.com"),
        Site("Bank Melli", "bmi.ir"),
        Site("Bank Mellat", "bankmellat.ir"),
        Site("Bank Saderat", "www.bsi.ir"),
        Site("Bank Pasargad", "www.bpi.ir"),
        Site("Bank Saman", "www.sb24.ir"),
        Site("Blu Bank", "blubank.sb24.ir"),
        Site("Shaparak", "shaparak.ir"),
        Site("Zarinpal", "www.zarinpal.com"),
        Site("Sheypoor", "www.sheypoor.com"),
        Site("Alibaba", "www.alibaba.ir"),
        Site("Tapsi", "tapsi.ir"),
        Site("Cafe Bazaar", "cafebazaar.ir")
    )

    private fun sorted(list: List<Site>) = list.sortedBy { it.name.lowercase() }

    val Categories = listOf(
        Category("netcat_ai", "ai", sorted(Ai)),
        Category("netcat_social", "social", sorted(Social)),
        Category("netcat_gaming", "gaming", sorted(Gaming)),
        Category("netcat_trading", "trading", sorted(Trading)),
        Category("netcat_news", "news", sorted(News)),
        Category("netcat_iranian", "iranian", sorted(Iranian))
    )

    suspend fun probe(site: Site, viaTunnel: Boolean, timeoutMs: Int = 6000): State =
        withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            val proxy = if (viaTunnel && !IkeController.active) java.net.Proxy(
                java.net.Proxy.Type.SOCKS,
                InetSocketAddress("127.0.0.1", MixedPort.value)
            ) else java.net.Proxy.NO_PROXY

            val code = runCatching {
                val url = URL("https://" + site.host + "/")
                val conn = url.openConnection(proxy) as HttpURLConnection
                conn.requestMethod = "GET"
                conn.instanceFollowRedirects = false
                conn.connectTimeout = timeoutMs
                conn.readTimeout = timeoutMs
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) GRoute")
                conn.setRequestProperty("Accept", "*/*")
                val result = conn.responseCode
                runCatching { conn.inputStream.close() }
                runCatching { conn.errorStream?.close() }
                conn.disconnect()
                result
            }.getOrNull() ?: return@withContext State.Unreachable

            val ms = (System.currentTimeMillis() - start).toInt()
            if (code == 403 || code == 451) State.Sanctioned else State.Reachable(ms)
        }

    suspend fun probeAll(
        viaTunnel: Boolean,
        sites: List<Site>,
        concurrency: Int = 8,
        onResult: (Site, State) -> Unit
    ) {
        val sem = Semaphore(concurrency)
        withContext(Dispatchers.IO) {
            sites.forEach { site ->
                sem.withPermit { onResult(site, probe(site, viaTunnel)) }
            }
        }
    }
}