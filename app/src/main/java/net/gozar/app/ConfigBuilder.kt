package net.gozar.app

import org.json.JSONArray
import org.json.JSONObject

object ConfigBuilder {

    private val DohHosts = listOf(
        "chrome.cloudflare-dns.com",
        "mozilla.cloudflare-dns.com",
        "cloudflare-dns.com",
        "dns.google",
        "dns64.dns.google",
        "dns.quad9.net",
        "doh.opendns.com",
        "dns.nextdns.io",
        "doh.cleanbrowsing.org",
        "dns.adguard-dns.com"
    )

    private val AdHosts = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "googletagservices.com",
        "adservice.google.com",
        "app-measurement.com",
        "amazon-adsystem.com",
        "adnxs.com",
        "adsrvr.org",
        "bidswitch.net",
        "rubiconproject.com",
        "pubmatic.com",
        "casalemedia.com",
        "openx.net",
        "smartadserver.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "sharethrough.com",
        "33across.com",
        "teads.tv",
        "media.net",
        "adform.net",
        "serving-sys.com",
        "flashtalking.com",
        "revcontent.com",
        "mgid.com",
        "zedo.com",
        "advertising.com",
        "yieldmo.com",
        "gumgum.com",
        "indexww.com",
        "moatads.com",
        "adsafeprotected.com",
        "doubleverify.com",
        "scorecardresearch.com",
        "quantserve.com",
        "demdex.net",
        "omtrdc.net",
        "everesttech.net",
        "2o7.net",
        "bluekai.com",
        "krxd.net",
        "agkn.com",
        "exelator.com",
        "rlcdn.com",
        "crwdcntrl.net",
        "mc.yandex.ru",
        "clarity.ms",
        "hotjar.com",
        "fullstory.com",
        "mouseflow.com",
        "inspectlet.com",
        "optimizely.com",
        "mixpanel.com",
        "amplitude.com",
        "segment.io",
        "segment.com",
        "branch.io",
        "appsflyer.com",
        "adjust.com",
        "kochava.com",
        "singular.net",
        "tenjin.io",
        "flurry.com",
        "braze.com",
        "clevertap.com",
        "leanplum.com",
        "applovin.com",
        "ironsrc.com",
        "supersonicads.com",
        "unityads.unity3d.com",
        "vungle.com",
        "chartboost.com",
        "adcolony.com",
        "inmobi.com",
        "mopub.com",
        "tapjoy.com",
        "startappservice.com",
        "smaato.com",
        "pubnative.net",
        "mobfox.com",
        "analytics.tiktok.com",
        "ads.tiktok.com",
        "ads.linkedin.com",
        "ads.yahoo.com",
        "3lift.com",
        "adition.com",
        "adzerk.net",
        "bidr.io",
        "contextweb.com",
        "districtm.io",
        "emxdgt.com",
        "lijit.com",
        "mathtag.com",
        "sitescout.com",
        "spotxchange.com",
        "stickyadstv.com",
        "tremorhub.com",
        "undertone.com",
        "zemanta.com",
        "sonobi.com",
        "improvedigital.com",
        "adsymptotic.com",
        "themoneytizer.com",
        "adthrive.com",
        "mediavine.com",
        "sovrn.com",
        "onetag.io",
        "adkernel.com",
        "adpushup.com",
        "yieldlab.net",
        "adtelligent.com",
        "loopme.com",
        "smartyads.com",
        "adhigh.net",
        "adriver.ru",
        "adspirit.de",
        "rtbhouse.com",
        "onaudience.com",
        "id5-sync.com",
        "adlooxtracking.com",
        "adsco.re",
        "bttrack.com",
        "tracking-protection.com",
        "trackjs.com",
        "krux.net",
        "parsely.com",
        "chartbeat.com",
        "chartbeat.net",
        "dpm.demdex.net",
        "sc-static.net",
        "adsmoloco.com",
        "liftoff.io",
        "fyber.com",
        "digitalturbine.com",
        "pangle.io",
        "pangolin-sdk-toutiao.com",
        "mintegral.com",
        "adsgreat.com",
        "zqtk.net",
        "trafficjunky.com",
        "exoclick.com",
        "juicyads.com",
        "popads.net",
        "propellerads.com",
        "adsterra.com",
        "hilltopads.net",
        "clickadu.com",
        "adcash.com",
        "trafficstars.com",
        "ad.gt",
        "adnium.com",
        "adsupply.com",
        "bidvertiser.com",
        "infolinks.com",
        "smartlook.com",
        "heap.io",
        "heapanalytics.com",
        "pendo.io",
        "mouseflow.net",
        "matomo.cloud",
        "plausible.io",
        "statcounter.com",
        "histats.com",
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "securepubads.g.doubleclick.net",
        "adservice.google.co.uk",
        "ad.doubleclick.net",
        "stats.g.doubleclick.net",
        "cdn.branch.io",
        "api2.branch.io",
        "metrics.apple.com",
        "ads.mopub.com",
        "yektanet.com",
        "yn-cdn.com",
        "sabavision.com",
        "clickyab.com",
        "adivery.com",
        "tapsell.ir",
        "tapsell.com",
        "tapsell.net",
        "adad.ir",
        "adro.co",
        "mediaad.org",
        "anetwork.ir",
        "netbina.com",
        "adnegah.net",
        "zarpop.com",
        "metrix.ir",
        "adtrace.io",
        "pushe.co",
        "najva.com",
        "chabok.io",
        "webgozar.com",
        "webgozar.ir",
        "ads.aparat.com",
        "biz.varzesh3.com",
        "biz-cdn.varzesh3.com",
        "adnxs-simple.com",
        "adnexus.net",
        "advertserve.com",
        "adtech.de",
        "adtechus.com",
        "atdmt.com",
        "bat.bing.com",
        "clicktale.net",
        "decibelinsights.com",
        "quantcount.com",
        "adroll.com",
        "adroll.net",
        "rfihub.com",
        "turn.com",
        "mediamath.com",
        "adsymptotic.net",
        "simpli.fi",
        "eyeota.net",
        "semasio.net",
        "weborama.fr",
        "liadm.com",
        "tapad.com",
        "drawbridge.com",
        "nexac.com",
        "owneriq.net",
        "addthis.com",
        "sharethis.com",
        "po.st",
        "disqusads.com",
        "adform.com",
        "adotmob.com",
        "adux.com",
        "smartclip.net",
        "spotx.tv",
        "spotxcdn.com",
        "vidoomy.com",
        "viralize.com",
        "connatix.com",
        "primis.tech",
        "playwire.com",
        "adsninja.ca",
        "ezoic.net",
        "ezojs.com",
        "adsense.com",
        "adperium.com",
        "trackingsoft.com",
        "trkn.us",
        "tremorvideo.com",
        "yieldoptimizer.com",
        "onesignal.com",
        "pushwoosh.com",
        "pushengage.com",
        "izooto.com",
        "truepush.com",
        "webpushr.com",
        "sendpulse.com",
        "vwo.com",
        "crazyegg.com",
        "luckyorange.com",
        "usabilla.com",
        "qualtrics.com",
        "surveymonkey.com",
        "typekit.net",
        "kissmetrics.com",
        "woopra.com",
        "gosquared.com",
        "keen.io",
        "countly.com"
    )

    fun build(
        config: ProxyConfig,
        fragment: Boolean = false,
        splitRouting: Boolean = false,
        sniffing: Boolean = false,
        sniffTypes: Set<String> = setOf("http", "tls", "quic"),
        fragmentPackets: String = "tlshello",
        fragmentLength: String = "10-20",
        fragmentInterval: String = "10-20",
        mux: Boolean = false,
        muxConcurrency: Int = 8,
        adBlock: Boolean = false,
        directOnly: Boolean = false,
        fakeDns: Boolean = false,
        encryptedDns: Boolean = false,
        torBase: ProxyConfig? = null,
        chainBase: ProxyConfig? = null,
        onionRouting: Boolean = false
    ): String {
        val onion = onionRouting && config.protocol != "tor"
        val fake = fakeDns || onion
        val dnsOn = fake || encryptedDns
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "warning"))

        if (fake) {
            root.put("fakedns", JSONArray().put(JSONObject()
                .put("ipPool", "198.18.0.0/15")
                .put("poolSize", 65535)))
        }
        if (dnsOn) {
            val servers = JSONArray()
            if (fake) servers.put("fakedns")
            if (encryptedDns) {
                servers.put("https://1.1.1.1/dns-query")
                servers.put("https://8.8.8.8/dns-query")
            }
            servers.put("1.1.1.1")
            servers.put("8.8.8.8")
            root.put("dns", JSONObject()
                .put("servers", servers)
                .put("queryStrategy", "UseIPv4"))
        }

        root.put("stats", JSONObject())
        root.put("policy", JSONObject().put("system", JSONObject()
            .put("statsOutboundUplink", true)
            .put("statsOutboundDownlink", true)))

        val tunIn = JSONObject().put("tag", "tun-in").put("port", 0).put("protocol", "tun")
            .put("settings", JSONObject().put("name", "xray0").put("MTU", 1500))
        if (splitRouting || sniffing || adBlock || fake) {
            val types = if (sniffing) expandSniffTypes(sniffTypes) else listOf("http", "tls", "quic")
            val destOverride = JSONArray()
            types.forEach { destOverride.put(it) }
            if (fake && !types.contains("fakedns")) destOverride.put("fakedns")
            if (adBlock) {
                listOf("http", "tls", "quic").forEach {
                    if (!types.contains(it)) destOverride.put(it)
                }
            }
            tunIn.put("sniffing", JSONObject()
                .put("enabled", true)
                .put("destOverride", destOverride)
                .put("routeOnly", !adBlock && splitRouting && !sniffing))
        }

        val socksIn = JSONObject().put("tag", "socks-in")
            .put("port", MixedPort.value).put("listen", "127.0.0.1").put("protocol", "socks")
            .put("settings", JSONObject().put("udp", true))
        if (splitRouting || sniffing || adBlock) {
            val socksTypes = JSONArray()
            listOf("http", "tls", "quic").forEach { socksTypes.put(it) }
            socksIn.put("sniffing", JSONObject()
                .put("enabled", true)
                .put("destOverride", socksTypes)
                .put("routeOnly", false))
        }

        val inbounds = JSONArray().put(tunIn).put(socksIn)
        if (config.protocol == "tor" || onion) {
            inbounds.put(JSONObject().put("tag", "tor-in")
                .put("port", TorController.BRIDGE_PORT).put("listen", "127.0.0.1")
                .put("protocol", "socks")
                .put("settings", JSONObject().put("udp", false)))
        }
        root.put("inbounds", inbounds)

        val chained = chainBase != null && chainBase.id != config.id
        val proxyOut = buildOutbound(config)
        if (chained) {
            val stream = proxyOut.optJSONObject("streamSettings") ?: JSONObject().also {
                proxyOut.put("streamSettings", it)
            }
            stream.put("sockopt", JSONObject().put("dialerProxy", "chain"))
        } else if (fragment) {
            proxyOut.optJSONObject("streamSettings")
                ?.put("sockopt", JSONObject().put("dialerProxy", "fragment"))
        }
        if (mux) {
            proxyOut.put("mux", JSONObject()
                .put("enabled", true)
                .put("concurrency", muxConcurrency.coerceIn(1, 128)))
        }

        val outbounds = JSONArray()
        if (directOnly) {
            outbounds.put(JSONObject().put("tag", "proxy").put("protocol", "freedom")
                .put("settings", JSONObject().put("domainStrategy", "UseIP")))
        } else {
            outbounds.put(proxyOut)
        }
        if (fragment) {
            outbounds.put(JSONObject()
                .put("tag", "fragment")
                .put("protocol", "freedom")
                .put("settings", JSONObject().put("fragment", JSONObject()
                    .put("packets", fragmentPackets.ifBlank { "tlshello" })
                    .put("length", fragmentLength.ifBlank { "10-20" })
                    .put("interval", fragmentInterval.ifBlank { "10-20" }))))
        }
        outbounds.put(JSONObject().put("tag", "direct").put("protocol", "freedom"))
        outbounds.put(JSONObject().put("tag", "block").put("protocol", "blackhole"))
        if (onion) {
            outbounds.put(JSONObject().put("tag", "tor-out").put("protocol", "socks")
                .put("settings", JSONObject().put("servers", JSONArray().put(
                    JSONObject().put("address", "127.0.0.1")
                        .put("port", TorController.SOCKS_PORT)))))
        }
        if (config.protocol == "tor" && torBase != null) {
            outbounds.put(buildOutbound(torBase).put("tag", "torbase"))
        }
        if (chained && chainBase != null) {
            outbounds.put(buildOutbound(chainBase).put("tag", "chain"))
        }
        if (dnsOn) {
            outbounds.put(JSONObject().put("tag", "dns-out").put("protocol", "dns"))
        }
        root.put("outbounds", outbounds)

        val rules = JSONArray()
        if (onion) {
            rules.put(JSONObject().put("type", "field")
                .put("domain", JSONArray().put("regexp:\\.onion$"))
                .put("outboundTag", "tor-out"))
        }
        if (config.protocol == "tor" && torBase != null) {
            rules.put(JSONObject().put("type", "field")
                .put("inboundTag", JSONArray().put("tor-in"))
                .put("outboundTag", "torbase"))
        }
        if (dnsOn) {
            rules.put(JSONObject().put("type", "field")
                .put("port", 53)
                .put("outboundTag", "dns-out"))
            val dohHosts = JSONArray()
            DohHosts.forEach { dohHosts.put("domain:" + it) }
            rules.put(JSONObject().put("type", "field")
                .put("domain", dohHosts)
                .put("outboundTag", "block"))
        }
        if (adBlock) {
            val adDomains = JSONArray()
                .put("geosite:category-ads-all")
                .put("geosite:ads")
            AdHosts.forEach { adDomains.put("domain:" + it) }
            rules.put(JSONObject().put("type", "field")
                .put("domain", adDomains)
                .put("outboundTag", "block"))
            rules.put(JSONObject().put("type", "field")
                .put("network", "udp")
                .put("port", "443")
                .put("outboundTag", "block"))
        }
        if (splitRouting) {
            rules.put(JSONObject().put("type", "field")
                .put("ip", JSONArray().put("geoip:private").put("geoip:ir"))
                .put("outboundTag", "direct"))
            rules.put(JSONObject().put("type", "field")
                .put("domain", JSONArray().put("geosite:category-ir"))
                .put("outboundTag", "direct"))
        }
        rules.put(JSONObject().put("type", "field")
            .put("inboundTag", JSONArray().put("tun-in").put("socks-in"))
            .put("outboundTag", "proxy"))
        root.put("routing", JSONObject().put("domainStrategy", "AsIs").put("rules", rules))

        return root.toString()
    }

    fun buildForTest(config: ProxyConfig, chainBase: ProxyConfig? = null): String {
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "none"))
        val proxyOut = buildOutbound(config)
        val outbounds = JSONArray().put(proxyOut)
        if (chainBase != null && chainBase.id != config.id) {
            val stream = proxyOut.optJSONObject("streamSettings") ?: JSONObject().also {
                proxyOut.put("streamSettings", it)
            }
            stream.put("sockopt", JSONObject().put("dialerProxy", "chain"))
            outbounds.put(buildOutbound(chainBase).put("tag", "chain"))
        }
        root.put("outbounds", outbounds)
        return root.toString()
    }

    fun buildForTestDirect(): String {
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "none"))
        root.put("outbounds", JSONArray().put(
            JSONObject().put("tag", "proxy").put("protocol", "freedom")
                .put("settings", JSONObject().put("domainStrategy", "UseIP"))
        ))
        return root.toString()
    }

    private fun expandSniffTypes(types: Set<String>): List<String> {
        val out = LinkedHashSet<String>()
        for (t in types) {
            if (t == "fakedns+others") out.addAll(listOf("fakedns", "http", "tls", "quic"))
            else out.add(t)
        }
        if (out.isEmpty()) { out.add("http"); out.add("tls") }
        return out.toList()
    }

    private fun buildOutbound(config: ProxyConfig): JSONObject {
        if (config.protocol == "wireguard") return buildWireguard(config)
        if (config.protocol == "aether") return buildAether()
        if (config.protocol == "hysteria2") return buildHysteria2(config)
        if (config.protocol == "tor") return buildTor()
        val settings = when (config.protocol) {
            "vless", "vmess" -> {
                val user = JSONObject().put("id", config.uuid)
                if (config.protocol == "vless") {
                    user.put("encryption", config.encryption)
                    if (config.flow.isNotEmpty()) user.put("flow", config.flow)
                } else {
                    user.put("alterId", config.alterId)
                    user.put("security", config.encryption.ifEmpty { "auto" })
                }
                val vnext = JSONObject().put("address", config.address).put("port", config.port)
                    .put("users", JSONArray().put(user))
                JSONObject().put("vnext", JSONArray().put(vnext))
            }
            "trojan" -> {
                val server = JSONObject().put("address", config.address)
                    .put("port", config.port).put("password", config.password)
                if (config.flow.isNotEmpty()) server.put("flow", config.flow)
                JSONObject().put("servers", JSONArray().put(server))
            }
            "shadowsocks" -> {
                val server = JSONObject().put("address", config.address).put("port", config.port)
                    .put("method", config.method).put("password", config.password)
                JSONObject().put("servers", JSONArray().put(server))
            }
            "http", "socks" -> {
                val server = JSONObject().put("address", config.address).put("port", config.port)
                if (config.uuid.isNotEmpty() || config.password.isNotEmpty()) {
                    server.put("users", JSONArray().put(
                        JSONObject().put("user", config.uuid).put("pass", config.password)
                    ))
                }
                JSONObject().put("servers", JSONArray().put(server))
            }
            else -> JSONObject()
        }
        return JSONObject().put("tag", "proxy").put("protocol", config.protocol)
            .put("settings", settings).put("streamSettings", buildStream(config))
    }


    private fun buildTor(): JSONObject {
        val server = JSONObject()
            .put("address", "127.0.0.1")
            .put("port", TorController.SOCKS_PORT)
        val settings = JSONObject().put("servers", JSONArray().put(server))
        return JSONObject().put("tag", "proxy").put("protocol", "socks").put("settings", settings)
    }

    private fun buildHysteria2(config: ProxyConfig): JSONObject {
        val settings = JSONObject()
            .put("version", 2)
            .put("address", config.address)
            .put("port", config.port)

        val tls = JSONObject()
            .put("serverName", config.sni.ifBlank { config.host.ifBlank { config.address } })
            .put("allowInsecure", config.allowInsecure)
            .put("alpn", csvArray(config.alpn.ifBlank { "h3" }))

        val hy = JSONObject()
            .put("version", 2)
            .put("auth", config.password)
            .put("udpIdleTimeout", 60)
        if (config.hyUpMbps > 0) hy.put("up", config.hyUpMbps.toString() + "mbps")
        if (config.hyDownMbps > 0) hy.put("down", config.hyDownMbps.toString() + "mbps")

        val stream = JSONObject()
            .put("network", "hysteria")
            .put("security", "tls")
            .put("tlsSettings", tls)
            .put("hysteriaSettings", hy)

        if (config.hyObfsPassword.isNotBlank()) {
            stream.put(
                "udpmasks",
                JSONArray().put(
                    JSONObject()
                        .put("type", config.hyObfs.ifBlank { "salamander" })
                        .put("settings", JSONObject().put("password", config.hyObfsPassword))
                )
            )
        }

        return JSONObject().put("tag", "proxy").put("protocol", "hysteria")
            .put("settings", settings).put("streamSettings", stream)
    }

    private fun buildAether(): JSONObject {
        val server = JSONObject()
            .put("address", "127.0.0.1")
            .put("port", AetherController.SOCKS_PORT)
        val settings = JSONObject().put("servers", JSONArray().put(server))
        return JSONObject().put("tag", "proxy").put("protocol", "socks").put("settings", settings)
    }

    private fun buildWireguard(config: ProxyConfig): JSONObject {
        val isWarpHost = config.address.equals("engage.cloudflareclient.com", ignoreCase = true)
        val epAddress = if (isWarpHost) Warp.WARP_ENDPOINT_HOST else config.address
        val epPort = if (isWarpHost) Warp.WARP_ENDPOINT_PORT else config.port

        val peer = JSONObject()
            .put("publicKey", config.publicKey)
            .put("endpoint", "$epAddress:$epPort")
            .put("allowedIPs", JSONArray().put("0.0.0.0/0").put("::/0"))

        val addrs = JSONArray()
        config.localAddress.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .forEach { addrs.put(it) }

        val settings = JSONObject()
            .put("secretKey", config.privateKey)
            .put("address", addrs)
            .put("peers", JSONArray().put(peer))
        if (config.mtu > 0) settings.put("mtu", config.mtu)

        val reserved = config.reserved.split(",").map { it.trim() }.mapNotNull { it.toIntOrNull() }
        if (reserved.size == 3) {
            settings.put("reserved", JSONArray().apply { reserved.forEach { put(it) } })
        }

        return JSONObject().put("tag", "proxy").put("protocol", "wireguard").put("settings", settings)
    }

    private fun normalizeNetwork(n: String): String = when (val v = n.trim().lowercase()) {
        "", "raw" -> "tcp"
        "mkcp" -> "kcp"
        "websocket" -> "ws"
        "h2", "http2" -> "http"
        "splithttp" -> "xhttp"
        else -> v
    }

    private fun csvArray(s: String): JSONArray {
        val arr = JSONArray()
        s.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { arr.put(it) }
        return arr
    }

    private fun buildStream(config: ProxyConfig): JSONObject {
        val net = normalizeNetwork(config.network)
        val stream = JSONObject().put("network", net)

        when (net) {
            "tcp" -> {
                if (config.headerType.equals("http", ignoreCase = true)) {
                    val request = JSONObject()
                        .put("version", "1.1")
                        .put("method", "GET")
                        .put("path", csvArray(config.path.ifEmpty { "/" }))
                    val headers = JSONObject()
                    if (config.host.isNotEmpty()) headers.put("Host", csvArray(config.host))
                    headers.put("User-Agent", JSONArray()
                        .put("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"))
                    headers.put("Accept-Encoding", JSONArray().put("gzip, deflate"))
                    headers.put("Connection", JSONArray().put("keep-alive"))
                    headers.put("Pragma", "no-cache")
                    request.put("headers", headers)
                    stream.put("tcpSettings", JSONObject().put("header",
                        JSONObject().put("type", "http").put("request", request)))
                }
            }
            "kcp" -> {
                val kcp = JSONObject().put("header",
                    JSONObject().put("type", config.headerType.ifEmpty { "none" }))
                if (config.path.isNotEmpty()) kcp.put("seed", config.path)
                stream.put("kcpSettings", kcp)
            }
            "ws" -> {
                val ws = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) ws.put("headers", JSONObject().put("Host", config.host))
                stream.put("wsSettings", ws)
            }
            "httpupgrade" -> {
                val hu = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) hu.put("host", config.host)
                stream.put("httpupgradeSettings", hu)
            }
            "xhttp" -> {
                val xh = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) xh.put("host", config.host)
                if (config.mode.isNotEmpty()) xh.put("mode", config.mode)
                stream.put("xhttpSettings", xh)
            }
            "grpc" -> {
                stream.put("grpcSettings", JSONObject()
                    .put("serviceName", config.serviceName)
                    .put("multiMode", config.mode == "multi"))
            }
            "http" -> {
                val h = JSONObject().put("path", config.path.ifEmpty { "/" })
                if (config.host.isNotEmpty()) h.put("host", csvArray(config.host))
                stream.put("httpSettings", h)
            }
        }

        when (config.security) {
            "reality" -> stream.put("security", "reality").put("realitySettings", JSONObject()
                .put("serverName", config.sni).put("publicKey", config.publicKey)
                .put("shortId", config.shortId).put("fingerprint", config.fingerprint).put("spiderX", "/"))
            "tls" -> {
                val tls = JSONObject()
                    .put("serverName", config.sni.ifEmpty {
                        config.host.substringBefore(",").trim().ifEmpty { config.address }
                    })
                    .put("fingerprint", config.fingerprint)
                if (config.alpn.isNotEmpty()) {
                    val arr = csvArray(config.alpn)
                    if (arr.length() > 0) tls.put("alpn", arr)
                }
                stream.put("security", "tls").put("tlsSettings", tls)
            }
        }
        return stream
    }
}