<?php


declare(strict_types=1);

require_once __DIR__ . '/Db.php';

if (class_exists('CubeOtp')) {
    return;
}

/**
 * One-time-code login for the CubeVPN Android app (docs/api-contract.md
 * in the app repo). Stores codes in its own table so nothing about the
 * existing `user` table has to change; reuses FaoximaAuth's token column
 * for the session token once a code is verified.
 */
final class CubeOtp
{
    private const COOLDOWN_SECONDS = 60;
    private const CODE_TTL_SECONDS = 300;
    private const MAX_ATTEMPTS = 5;

    public static function ensureTable(): void
    {
        FaoximaDb::execute(
            "CREATE TABLE IF NOT EXISTS cube_otp (
                identifier VARCHAR(64) NOT NULL PRIMARY KEY,
                user_id BIGINT NOT NULL,
                code VARCHAR(10) NOT NULL,
                attempts INT NOT NULL DEFAULT 0,
                requested_at INT NOT NULL,
                expires_at INT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }

    /**
     * @return array{type:string,value:string} type is 'phone', 'telegram_id', or 'invalid'
     */
    public static function resolveIdentifier(string $raw): array
    {
        $digits = preg_replace('/\D+/', '', $raw) ?? '';

        if (preg_match('/^0?9\d{9}$/', $digits)) {
            $local = preg_replace('/^0/', '', $digits);
            return ['type' => 'phone', 'value' => '98' . $local];
        }
        if (preg_match('/^989\d{9}$/', $digits)) {
            return ['type' => 'phone', 'value' => $digits];
        }
        if (preg_match('/^\d{5,15}$/', $digits)) {
            return ['type' => 'telegram_id', 'value' => $digits];
        }
        return ['type' => 'invalid', 'value' => $digits];
    }

    public static function findUser(array $resolved): ?array
    {
        if ($resolved['type'] === 'phone') {
            $row = select('user', '*', 'number', $resolved['value'], 'select');
        } elseif ($resolved['type'] === 'telegram_id') {
            $row = select('user', '*', 'id', (int)$resolved['value'], 'select');
        } else {
            return null;
        }
        return is_array($row) && !empty($row) ? $row : null;
    }

    /**
     * @return array{ok:bool,error?:string,message?:string,cooldown_seconds?:int}
     */
    public static function issue(string $identifier, array $user): array
    {
        self::ensureTable();

        $now = time();
        $existing = FaoximaDb::fetchOne(
            "SELECT * FROM cube_otp WHERE identifier = :id",
            [':id' => $identifier]
        );
        if ($existing !== null && ((int)$existing['requested_at'] + self::COOLDOWN_SECONDS) > $now) {
            $remaining = ((int)$existing['requested_at'] + self::COOLDOWN_SECONDS) - $now;
            return [
                'ok' => false,
                'error' => 'rate_limited',
                'message' => 'لطفاً ' . $remaining . ' ثانیه دیگر دوباره تلاش کنید.',
            ];
        }

        $code = (string)random_int(10000, 99999);
        FaoximaDb::execute(
            "INSERT INTO cube_otp (identifier, user_id, code, attempts, requested_at, expires_at)
             VALUES (:id, :uid, :code, 0, :now, :exp)
             ON DUPLICATE KEY UPDATE user_id = :uid2, code = :code2, attempts = 0, requested_at = :now2, expires_at = :exp2",
            [
                ':id' => $identifier,
                ':uid' => (int)$user['id'],
                ':code' => $code,
                ':now' => $now,
                ':exp' => $now + self::CODE_TTL_SECONDS,
                ':uid2' => (int)$user['id'],
                ':code2' => $code,
                ':now2' => $now,
                ':exp2' => $now + self::CODE_TTL_SECONDS,
            ]
        );

        $text = "کد ورود شما به کیوب‌وی‌پی‌ان:\n\n{$code}\n\nاین کد تا ۵ دقیقه دیگر معتبر است. اگر درخواست این کد را نداده‌اید، نادیده بگیرید.";
        $result = sendmessage((int)$user['id'], $text, null, null);
        if (empty($result['ok'])) {
            return [
                'ok' => false,
                'error' => 'identifier_not_found',
                'message' => 'ابتدا ربات @cubevvpn_bot را در تلگرام استارت کنید، سپس دوباره تلاش کنید.',
            ];
        }

        return ['ok' => true, 'cooldown_seconds' => self::COOLDOWN_SECONDS];
    }

    /**
     * @return array{ok:bool,error?:string,message?:string,user_id?:int}
     */
    public static function verify(string $identifier, string $code): array
    {
        self::ensureTable();

        $row = FaoximaDb::fetchOne(
            "SELECT * FROM cube_otp WHERE identifier = :id",
            [':id' => $identifier]
        );
        if ($row === null || (int)$row['expires_at'] < time()) {
            return ['ok' => false, 'error' => 'expired_code', 'message' => 'کد منقضی شده است؛ دوباره درخواست دهید.'];
        }
        if ((int)$row['attempts'] >= self::MAX_ATTEMPTS) {
            return ['ok' => false, 'error' => 'too_many_attempts', 'message' => 'تعداد تلاش‌های مجاز به پایان رسید؛ دوباره درخواست دهید.'];
        }
        if (!hash_equals((string)$row['code'], trim($code))) {
            FaoximaDb::execute(
                "UPDATE cube_otp SET attempts = attempts + 1 WHERE identifier = :id",
                [':id' => $identifier]
            );
            return ['ok' => false, 'error' => 'invalid_code', 'message' => 'کد وارد شده نادرست است.'];
        }

        FaoximaDb::execute("DELETE FROM cube_otp WHERE identifier = :id", [':id' => $identifier]);
        return ['ok' => true, 'user_id' => (int)$row['user_id']];
    }
}
