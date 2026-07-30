# CubeVPN account API contract

The Android app talks to one backend base URL (`API_BASE_URL`, set in
`secrets.properties`) for login and service/config delivery. Everything
below is what the app expects — implement it however you like on top of
the existing `cubevvpn_bot` bot and your panel's database.

All requests/responses are JSON. All responses include `"ok": boolean`.
On `ok: false`, include `"error"` (a short machine-readable code) and
`"message"` (human-readable, shown to the user — Persian or English is
fine, the app displays it verbatim).

## Auth

### `POST /v1/auth/request-code`

Sends a one-time code to the user via the `cubevvpn_bot` Telegram bot.

Request:
```json
{ "identifier": "+989123456789" }
```
`identifier` is either a phone number (any reasonable format) or a
numeric Telegram user ID, exactly what the user typed on the login
screen. The user must have already started a chat with `@cubevvpn_bot`
(otherwise Telegram's `sendMessage` will fail) — return `identifier_not_found`
in that case so the app can tell the user to open the bot first.

Response 200:
```json
{ "ok": true, "cooldown_seconds": 60 }
```
`cooldown_seconds` is how long the app should disable the "resend code"
button.

Error response (still HTTP 200 or 4xx, app just checks `ok`):
```json
{ "ok": false, "error": "rate_limited", "message": "لطفاً کمی صبر کنید." }
```
Known `error` codes the app treats specially: `invalid_identifier`,
`identifier_not_found`, `rate_limited`. Any other code just shows `message`.

### `POST /v1/auth/verify-code`

Request:
```json
{ "identifier": "+989123456789", "code": "12345" }
```

Response 200:
```json
{
  "ok": true,
  "token": "opaque-bearer-token",
  "user": { "id": "123", "identifier": "+989123456789", "display_name": "Reza" }
}
```
`token` is an opaque bearer token the app stores (encrypted, on-device)
and sends as `Authorization: Bearer <token>` on every later request.
It should not expire quickly — this is the user's persistent login.

Error response:
```json
{ "ok": false, "error": "invalid_code", "message": "کد وارد شده نادرست است." }
```
Known `error` codes: `invalid_code`, `expired_code`, `too_many_attempts`.

### `POST /v1/auth/logout` *(optional, best-effort)*

`Authorization: Bearer <token>`. Invalidate the token server-side if you
want. The app clears its local session regardless of the response.

## Account

### `GET /v1/account/me`

`Authorization: Bearer <token>`.

Response 200:
```json
{
  "ok": true,
  "user": { "id": "123", "identifier": "+989123456789", "display_name": "Reza" },
  "services": [
    {
      "id": "svc_1",
      "name": "Fast",
      "subscription_url": "https://panel.example.com/sub/abc123",
      "expire": 1785369600,
      "total_bytes": 1073741824,
      "used_bytes": 33280
    }
  ]
}
```
Each entry in `services` is one purchased plan. `subscription_url` **must
be a standard Xray/V2Ray subscription link** — the same format the app
already supports for the "paste a subscription link" feature:
- the response body is either plain text or base64, one `vless://` /
  `vmess://` / `trojan://` / `ss://` link per line
- optionally send the `subscription-userinfo` response header
  (`upload=...;download=...;total=...;expire=...`, all bytes/unix-seconds)
  so the app can show data-used / data-left / expiry on the service card
  without needing `total_bytes`/`used_bytes`/`expire` in this JSON at all
  — those three fields here are a fallback if you'd rather not add the
  header.

On login, and periodically after, the app calls this endpoint, then adds
`subscription_url` for each service the same way it already handles any
user-pasted subscription link (fetch → parse configs → show under that
service's server list, with the quota bar from the `subscription-userinfo`
header).

### 401 handling

Any endpoint returning HTTP 401 (or `{"ok": false, "error": "unauthorized"}`)
is treated by the app as "token expired" — it clears the stored session
and sends the user back to the login screen.
