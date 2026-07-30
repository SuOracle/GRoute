# CubeVPN account API contract

The Android app talks to one backend base URL (`API_BASE_URL`, set in
`secrets.properties`) for login and service/config delivery.

**Reference implementation**: [`backend/faoxima/`](../backend/faoxima) has a
ready-to-drop-in implementation of all three endpoints below, built
directly on top of a Faoxima-based panel (reusing its
`select()`/`update()`/`sendmessage()` helpers, its `user` and `invoice`
tables, and its existing `ServiceHandler` for pulling live panel data) —
see [`backend/README.md`](../backend/README.md) for exactly where those
files go. If you're implementing this some other way, everything below
is the contract the app actually calls; implement it however you like on
top of your `cubevvpn_bot` bot and database.

All requests/responses are JSON. All responses include `"ok": boolean`.
On `ok: false`, include `"error"` (a short machine-readable code) and
`"message"` (human-readable, shown to the user — Persian or English is
fine, the app displays it verbatim).

## Auth

### `POST /api/requestcode.php`

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

### `POST /api/verifycode.php`

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

### `POST /api/logout.php` *(optional, best-effort — not implemented by the reference build)*

`Authorization: Bearer <token>`. Invalidate the token server-side if you
want. The app clears its local session regardless of the response, and
doesn't currently call this endpoint at all — add it only if you want
server-side revocation.

## Account

### `GET /api/accountme.php`

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

## Deployment (Faoxima reference implementation)

[`backend/faoxima/api/`](../backend/faoxima/api) has four PHP files that
implement this contract on top of a Faoxima panel install, written to sit
next to the panel's existing `phone.php` / `verify.php`:

```
backend/faoxima/api/lib/CubeOtp.php   — OTP generation/storage/verification
backend/faoxima/api/requestcode.php   — POST /api/requestcode.php
backend/faoxima/api/verifycode.php    — POST /api/verifycode.php
backend/faoxima/api/accountme.php     — GET  /api/accountme.php
```

Copy all four into your Faoxima install's `api/` directory (the same
folder that already has `phone.php`, `verify.php`, and `handlers/`) —
`CubeOtp.php` next to the existing `api/lib/*.php` files, the other three
directly under `api/`. Nothing else needs to change:

- They reuse the panel's existing `select()`/`update()` globals,
  `sendmessage()` (Telegram send), `FaoximaAuth` (bearer token issue/
  lookup — same `user.token` column phone/verify already use), and
  `ServiceHandler::buildPayloadFromInvoice()` (same code the miniapp's
  own service screen uses, so Marzban/Hiddify/x-ui/stock configs all
  work the same way).
- `CubeOtp.php` creates its own `cube_otp` table on first use
  (`CREATE TABLE IF NOT EXISTS`) — no changes to the `user` or `invoice`
  tables, no migration to run by hand.
- Identifier resolution accepts either an Iranian mobile number (any of
  `0912...`, `912...`, `98912...`, `+98912...`) matched against the
  `user.number` column, or a bare numeric Telegram ID matched against
  `user.id`. A user must already exist in the `user` table (i.e. have
  started `@cubevvpn_bot` at least once) — `requestcode.php` returns
  `identifier_not_found` otherwise.

Then set, in the Android project's `secrets.properties`:
```
API_BASE_URL=https://your-faoxima-domain.example
```
(no trailing slash, no `/api` suffix — the app appends `/api/....php`
itself).

Not included, deliberately: the OTP message text is in Persian and
minimal ("کد ورود شما به کیوب‌وی‌پی‌ان: …") — edit the string directly in
`CubeOtp::issue()` if you want different wording or branding.
