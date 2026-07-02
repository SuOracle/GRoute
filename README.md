<p align="center">
  <img src="docs/images/GRouteTransparentBlack.png" width="220" alt="GRoute">
</p>

# GRoute (جی‌روت)

[فارسی](README-fa.md)

A lightweight Android VPN client built on [Xray-core](https://github.com/XTLS/Xray-core) for bypassing internet restrictions, with a clean Jetpack Compose UI in English and Persian.

*GRoute — a tool to pass through restrictions.*

## Features

- **Multiple protocols** — VLESS, VMess, Trojan, and Shadowsocks.
- **Modern transports** — REALITY, TLS, WebSocket, gRPC, HTTPUpgrade, XHTTP, and plain TCP.
- **Cloudflare WARP** — register and add a WARP configuration in one tap.
- **Subscriptions** — add a subscription link to import all of its servers at once, with configurable auto-refresh (off, hourly, or every few hours) and remaining-data / expiry display.
- **Per-app proxy** — route only selected apps through the VPN, or route everything except selected apps.
- **Split routing** — Iranian sites connect directly, outside the tunnel.
- **TLS fragmentation (anti-DPI)** — splits the TLS handshake to slip past SNI filtering.
- **Sniffing (destination override)** — connect using the domain detected from traffic instead of the raw IP, which helps some servers connect correctly. Choose what to sniff (HTTP, TLS, QUIC).
- **Cloudflare clean-IP scanner** — samples Cloudflare's IP ranges, keeps the ones reachable from your network ranked by latency, and lets you copy one to use as a clean IP for Cloudflare-fronted servers.
- **Internet quality test** — measures speed, ping, jitter, and idle / download / upload latency, then rates your connection for gaming, web browsing, streaming, and video calling — through the tunnel or on your direct connection.
- **Per-server testing** — per-server ping with a one-tap *Test all*, plus a real-delay test while connected.
- **Share & import** — copy or share any server or subscription as a link, and multi-select servers to copy, share, or delete in bulk.
- **Xray logs** — view the engine's runtime logs inside the app.
- **Data-usage history** — live session speed and total, plus hourly, daily, and custom date ranges.
- **About** — shows the bundled Xray-core version, developer, and privacy policy.
- **Bilingual** — full English and Persian UI with right-to-left support.
- **Themes** — light, dark, and pure-black AMOLED.

<details>
<summary><b>App screenshots</b></summary>

<p align="center">
  <img src="docs/images/Artboard%203.png" width="720" alt="GRoute features">
</p>

</details>

## Download

Grab the latest APK from the [Releases](https://github.com/SuOracle/GNet/releases) page and install it on your device. Because GRoute is distributed outside the Play Store, you'll need to allow installation from your browser or file manager the first time. Google Play Protect may show a caution prompt on sideloaded apps; this is normal for direct APK installs and fades as install volume grows on a stable signing key.

To update later, install the newer APK over the existing one — as long as it's signed with the same key, your servers and settings are preserved. GRoute can also check for new releases from the **About** screen.

## How to use

**1. Add servers.** On the main screen, tap the server card to open the server list. You can add servers three ways:

- Paste one or more config links (`vless://`, `vmess://`, `trojan://`, `ss://`) — one per line — and tap **Add**.
- Paste a **subscription link** (starting with `http`/`https`) to import every server it contains.
- Use the **+** menu to paste from the clipboard, add a server manually field by field, or register a **WARP** configuration.

**2. Pick a server.** Tap any server to select it. Tap **Test all** to ping every server, then choose **Fastest first** from the sort menu to move the quickest to the top. The share icon on any server or subscription lets you copy or share it as a link.

**3. Connect.** Go back to the main screen and tap **Connect**. The first time, Android asks for VPN permission (and, on Android 13+, notification permission) — allow both. Once connected you'll see live upload/download speed, and the notification shows the speed with a **Disconnect** button you can use without opening the app.

**4. Tune it (Settings tab).**

- **Split routing** — keeps Iranian traffic direct and outside the tunnel. Useful for opening Iran-only websites.
- **Fragment (anti-DPI)** — helps slip past DPI, although modern DPI is increasingly able to recognize VPN traffic patterns.
- **Sniffing** — routes by the domain detected from traffic instead of the raw IP; turn it on if a server won't connect correctly, and choose what to sniff (HTTP / TLS / QUIC).
- **Per-app proxy** — choose exactly which apps use the VPN.
- **Auto-refresh subscriptions** — set how often subscriptions update (off, hourly, or every few hours).
- **Language** — switch between English and Persian.
- **Theme** — use the toggle in the top-right to cycle light / dark / AMOLED.

**5. Test your connection.**

- While connected, use **Ping** on the main screen for latency through the tunnel.
- Open the **Internet quality test** from Settings for a full report — speed, ping, jitter, and latency, plus a quality rating for gaming, browsing, streaming, and video calling, measured through the tunnel or on your direct connection.

**6. Find a clean IP.** Open the **Cloudflare IP scanner** from Settings to find clean edge IPs that work on your network, ranked by latency — tap one to copy it for use with Cloudflare-fronted servers.

**7. Track usage.** The Settings tab shows your all-time total; tap it for hourly, daily, and custom-range charts.

**8. Troubleshoot.** Open **Xray logs** from Settings to see the engine's runtime output if a connection misbehaves, and **About** to check the bundled Xray-core version.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Xray-core (via a gomobile bridge) · minSdk 26 · targetSdk 36.

## Privacy

GRoute has no accounts, analytics, advertising, or tracking. Server configurations and usage statistics stay on your device and are never transmitted. See the full policy in the **About** screen.

## Support

Questions or issues? Reach the developer on Telegram at [@OracleVPNsupport](https://t.me/OracleVPNsupport).

## License

[GPL-3.0](LICENSE). Bundled Xray-core remains under its own MPL-2.0 license.

Developed by Oracle VPN.
