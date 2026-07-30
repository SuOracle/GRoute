# CubeVPN

[فارسی](README-fa.md)

A lightweight Android VPN client built on [Xray-core](https://github.com/XTLS/Xray-core) for bypassing internet restrictions, with a custom black/red Jetpack Compose UI in English and Persian, and account sign-in through the [@cubevvpn_bot](https://t.me/cubevvpn_bot) Telegram bot.

## Features

- **Sign-in via Telegram** — enter your phone number or Telegram ID and verify with a one-time code sent by [@cubevvpn_bot](https://t.me/cubevvpn_bot); no password to remember.
- **Purchased services** — after sign-in the app pulls your active plan(s) automatically, each with its own data quota, expiry, and server list.
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
- **Bilingual** — full English and Persian UI with right-to-left support.
- **Themes** — light, dark, and pure-black AMOLED, all built around the CubeVPN red accent.

## How to use

**1. Sign in.** On first launch, enter the phone number or Telegram ID you use with [@cubevvpn_bot](https://t.me/cubevvpn_bot) and tap **Get code**. Open Telegram to read the one-time code the bot sends you, enter it, and you're in — your purchased service(s) and servers load automatically.

**2. Pick a server.** Tap the server card to open the server list, grouped by service. Tap **Test all** to ping every server, then choose **Fastest first** from the sort menu to move the quickest to the top.

**3. Connect.** Go back to the main screen and tap **Connect**. The first time, Android asks for VPN permission (and, on Android 13+, notification permission) — allow both. Once connected you'll see live upload/download speed, and the notification shows the speed with a **Disconnect** button you can use without opening the app.

**4. Tune it (Settings tab).**

- **Split routing** — keeps Iranian traffic direct and outside the tunnel. Useful for opening Iran-only websites.
- **Fragment (anti-DPI)** — helps slip past DPI, although modern DPI is increasingly able to recognize VPN traffic patterns.
- **Sniffing** — routes by the domain detected from traffic instead of the raw IP; turn it on if a server won't connect correctly, and choose what to sniff (HTTP / TLS / QUIC).
- **Per-app proxy** — choose exactly which apps use the VPN.
- **Auto-refresh subscriptions** — set how often your services' server lists update (off, hourly, or every few hours).
- **Language** — switch between English and Persian.
- **Theme** — use the toggle in the top-right to cycle light / dark / AMOLED.

**5. Test your connection.**

- While connected, use **Ping** on the main screen for latency through the tunnel.
- Open the **Internet quality test** from Settings for a full report — speed, ping, jitter, and latency, plus a quality rating for gaming, browsing, streaming, and video calling, measured through the tunnel or on your direct connection.

**6. Find a clean IP.** Open the **Cloudflare IP scanner** from Settings to find clean edge IPs that work on your network, ranked by latency — tap one to copy it for use with Cloudflare-fronted servers.

**7. Track usage.** The Settings tab shows your all-time total; tap it for hourly, daily, and custom-range charts.

**8. Troubleshoot.** Open **Xray logs** from Settings to see the engine's runtime output if a connection misbehaves, and **About** to check the bundled Xray-core version.

**9. Sign out.** Settings → Account → **Sign out**.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Xray-core (via a gomobile bridge) · minSdk 26 · targetSdk 36.

## Backend

The app is a pure client. Sign-in, OTP delivery, and the list of a user's
purchased services/configs are served by a separate backend that talks to
`@cubevvpn_bot` — see [`docs/api-contract.md`](docs/api-contract.md) for the
exact HTTP contract the app expects, and set `API_BASE_URL` in
`secrets.properties` to point the app at your deployment. A ready-to-use
implementation of that contract for Faoxima-based panels lives in
[`backend/`](backend).

## Privacy

CubeVPN's only account data is what's needed to sign you in (your phone
number or Telegram ID) and deliver your purchased service's configs — see
the full policy in the **About** screen. Server configurations and usage
statistics stay on your device and are never transmitted elsewhere.

## Support

Questions or issues? Reach us on Telegram at [@cubevvpn_bot](https://t.me/cubevvpn_bot).

## License

This project is a derivative of [GRoute](https://github.com/SuOracle/GRoute),
licensed under [GPL-3.0](LICENSE); that license still governs this codebase
and applies to anyone who receives a build of this app (GPL-3.0 requires
offering them the corresponding source on request — it does not require
publishing this repository publicly). Bundled Xray-core remains under its
own MPL-2.0 license.
