<div align="center">

<img src="docs/images/GRouteTransparentBlack.png" alt="GRoute" width="360">

# GRoute (جی‌روت)

**A lightweight Android VPN client built on [Xray-core](https://github.com/XTLS/Xray-core) for getting past internet restrictions — with a clean Jetpack Compose UI in English and Persian.**

[فارسی](README-fa.md) · [Releases](https://github.com/SuOracle/GRoute/releases) · [Telegram](https://t.me/OracleVPNsupport)

[![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3ddc84)
![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7f52ff)

*GRoute — Be Secured, Be Free*

</div>

---

## Contents

- [Features](#features)
- [Protocols and transports](#protocols-and-transports)
- [Download](#download)
- [How to use](#how-to-use)
- [Diagnostics](#diagnostics)
- [Building from source](#building-from-source)
- [Tech stack](#tech-stack)
- [Privacy](#privacy)
- [Support](#support)
- [License](#license)

---

## Features

### Getting connected

- **Four ways to add servers** — paste config links, import a subscription, enter details manually, or pull from a bundled free provider.
- **Subscriptions** — import every server from a link at once, with configurable auto-refresh and remaining-data / expiry display.
- **Cloudflare WARP** — register a free WARP account and add it as a WireGuard config in one tap.
- **Aether** — MASQUE, WireGuard, and nested WireGuard tunnels with automatic endpoint discovery. Works in places where classic WARP is blocked.
- **Tor** — route through the Tor network with a choice of exit country, either directly or through your own server first.
- **Windscribe importer** — sign in with your IKEv2 credentials and import free server locations in bulk.
- **Config chaining** — dial one config through another, tunnel inside tunnel.

### Getting through

- **TLS fragmentation (anti-DPI)** — splits the TLS handshake to slip past SNI filtering, with tunable packet, length, and interval settings.
- **Split routing** — Iranian sites connect directly, outside the tunnel.
- **Sniffing** — connect using the domain detected from traffic instead of the raw IP. Choose what to sniff (HTTP, TLS, QUIC, FakeDNS).
- **Mux** — multiplex several connections over one stream, with adjustable concurrency.
- **Encrypted DNS and FakeDNS** — resolve over DoH, and give each domain its own address so per-host rules always apply.
- **Onion routing** — send `.onion` addresses through Tor while everything else uses your server.
- **Cloudflare clean-IP scanner** — samples Cloudflare's ranges, keeps what's reachable from your network, and ranks by latency.

### Control and privacy

- **Per-app proxy** — route only selected apps through the VPN, or everything except them.
- **Kill switch** — block all traffic if the tunnel drops, so nothing leaks.
- **Ad and tracker blocking** — drop known ad domains inside the tunnel, optionally even while disconnected.
- **Encrypted storage** — server configs are encrypted at rest with AES-256-GCM via the Android Keystore.
- **Encrypted export** — share configs as password-protected files that only open inside GRoute.
- **Quick Settings tile** — connect and disconnect without opening the app.

### Interface

- **Bilingual** — full English and Persian UI with right-to-left support and Persian numerals.
- **Themes** — light, dark, and pure-black AMOLED.
- **Interactive globe** — filled or dotted, showing your live exit location.
- **Data-usage history** — live session speed and totals, plus hourly, daily, and custom date ranges.

---

## Protocols and transports

| | Supported |
|---|---|
| **Protocols** | VLESS · VMess · Trojan · Shadowsocks · Hysteria2 · WireGuard · IKEv2 · SOCKS · HTTP |
| **Transports** | TCP · WebSocket · gRPC · HTTPUpgrade · XHTTP · mKCP · HTTP/2 |
| **Security** | REALITY · TLS · uTLS fingerprinting (Chrome, Firefox, Safari, iOS, Android, Edge, random) |
| **Link formats** | `vless://` `vmess://` `trojan://` `ss://` `hysteria2://` `hy2://` `wireguard://` `wg://` `socks5://` `http://` `ikev2://` |

IKEv2 runs on a separate strongSwan engine, so per-app proxy, split routing, and the debugger don't apply to it.

---

## Download

Grab the latest APK from the [Releases](https://github.com/SuOracle/GRoute/releases) page. Builds are split per ABI — pick **arm64-v8a** for any phone from roughly 2017 onward, or **armeabi-v7a** for older 32-bit devices. Each release also ships a `SHA256SUMS.txt` so you can verify what you downloaded:

```bash
sha256sum -c SHA256SUMS.txt --ignore-missing
```

Because GRoute is distributed outside the Play Store, you'll need to allow installation from your browser or file manager the first time. Play Protect may show a caution prompt on sideloaded apps; this is normal for direct APK installs and fades as install volume grows on a stable signing key.

To update, install the newer APK over the existing one — as long as it's signed with the same key, your servers and settings are preserved. GRoute can also check for new releases from the **About** screen.

---

## How to use

**1. Add servers.** Tap the server card on the main screen to open the server list, then use the **+** menu:

- **Paste from clipboard** — one or many config links, one per line. Subscription links (`http`/`https`) import every server they contain.
- **Add manually** — enter the fields yourself.
- **Import from file** — open a `.grt` config file, including password-protected ones.
- **Free projects** — register Cloudflare WARP, add Aether, or pick Tor exit countries.
- **Windscribe** — sign in with IKEv2 credentials and select locations.

**2. Pick a server.** Tap any server to select it. **Test all** pings everything at once; sort by **Fastest first** to bring the quickest to the top. Long-press to multi-select for bulk copy, share, or delete. Swipe a row to delete it.

**3. Connect.** Tap **Connect** on the main screen. The first time, Android asks for VPN permission (and notification permission on Android 13+) — allow both. Once connected you'll see live upload and download speed, and the notification carries a **Disconnect** button.

**4. Tune it.** Under **Settings → Connection settings**:

| Setting | When to reach for it |
|---|---|
| Split routing | You want Iranian sites to load directly |
| Fragment | A server won't connect at all — likely SNI filtering |
| Sniffing | A server connects but sites misbehave |
| Mux | Restrictive network; leave off with REALITY/Vision |
| Kill switch | You can't afford a leak if the tunnel drops |
| Per-app proxy | Only some apps should use the VPN |

**5. Enable Smart connect** to test every server each minute and switch to the fastest automatically.

---

## Diagnostics

GRoute ships a fuller diagnostic set than most clients, which matters when a server fails and you need to know *why*:

- **Config debugger** — validates every field, then probes the connection in layers and tells you exactly where it broke: DNS resolution, TCP handshake, TLS handshake, SNI blocking, certificate rejection, port blocking, protocol handshake, or an exhausted subscription.
- **Internet quality test** — download and upload speed, idle / loaded latency, and jitter, rated for gaming, browsing, streaming, and video calling — through the tunnel or direct.
- **Check host** — reachability from check-host.net nodes worldwide, plus ASN, organisation, and geolocation for any address.
- **Network radar** — checks which sites are up, blocked, or geo-restricted on your current network, grouped by category.
- **IP intelligence** — flags whether your exit IP is recognised as a datacentre, VPN, proxy, Tor, or abuse range.
- **Engine logs** — Xray, Tor, and Aether runtime output, with credentials automatically redacted.

---

## Building from source

**Requirements:** JDK 17, Android SDK 36, NDK `27.3.13750724`, Go 1.26 (only if rebuilding the core).

```bash
git clone --recurse-submodules https://github.com/SuOracle/GRoute.git
cd GRoute
./gradlew assembleRelease
```

The Xray bridge ships as a prebuilt `app/libs/gozarcore.aar`. To rebuild it yourself:

```bash
go install golang.org/x/mobile/cmd/gomobile@latest
gomobile init
gomobile bind -target=android/arm64,android/arm -androidapi 26 -o app/libs/gozarcore.aar .
```

Signing is driven by environment variables (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`); without them the release build stays unsigned. Releases are cut by pushing a `v*` tag, which runs [`.github/workflows/release.yml`](.github/workflows/release.yml).

---

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Xray-core via gomobile · strongSwan for IKEv2 · minSdk 26 · targetSdk 36

---

## Privacy

No accounts, no analytics, no advertising, no tracking. The developer runs no server that sees your traffic.

Server configurations are stored encrypted in the app's private storage. Usage statistics never leave your device. The app contacts third-party services (`ipwho.is`, `ipify.org`) only to display your current IP and approximate location — those services necessarily see your connecting IP, and nothing else is sent.

The proxy and VPN servers you add are provided by you or your subscription provider. GRoute has no visibility into their logging practices — choose providers you trust. Full policy in the **About** screen.

---

## Support

Questions or issues? Open an [issue](https://github.com/SuOracle/GRoute/issues) or reach the developer on Telegram at [@OracleVPNsupport](https://t.me/OracleVPNsupport).

---

## License

[GPL-3.0](LICENSE). Bundled Xray-core remains under its own MPL-2.0 license; strongSwan under GPL-2.0.

<div align="center">

Developed by **Oracle VPN**

</div>
