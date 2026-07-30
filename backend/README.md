# Backend (reference implementation)

`faoxima/` is a reference implementation of [`docs/api-contract.md`](../docs/api-contract.md)
— the three endpoints the CubeVPN Android app needs for Telegram-OTP login
and pulling a user's purchased services — built on top of a
[Faoxima](https://github.com/Mmd-Amir/Faoxima)-based panel.

## Install

Copy the contents of `faoxima/api/` into your Faoxima install's own `api/`
directory (the same folder that already has `phone.php`, `verify.php`, and
`handlers/`):

```
your-faoxima-install/api/lib/CubeOtp.php     <- backend/faoxima/api/lib/CubeOtp.php
your-faoxima-install/api/requestcode.php     <- backend/faoxima/api/requestcode.php
your-faoxima-install/api/verifycode.php      <- backend/faoxima/api/verifycode.php
your-faoxima-install/api/accountme.php       <- backend/faoxima/api/accountme.php
```

Nothing else on the panel needs to change — no schema migration to run by
hand (the OTP table is created automatically on first request), no config
file edits. Full details, including how identifiers are resolved and which
existing panel functions get reused, are in
[`docs/api-contract.md`](../docs/api-contract.md#deployment-faoxima-reference-implementation).

Then point the Android app at your domain — in the app repo's
`secrets.properties`:
```
API_BASE_URL=https://your-faoxima-domain.example
```

## If you're not on Faoxima

Ignore this folder and implement [`docs/api-contract.md`](../docs/api-contract.md)
directly — it's backend-agnostic; these three files are just one way to
satisfy it.
