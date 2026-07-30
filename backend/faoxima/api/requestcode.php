<?php
// CubeVPN app endpoint: POST { "identifier": "<phone or Telegram numeric id>" }
// See docs/api-contract.md (POST /api/requestcode.php) in the CubeVPN Android repo.

declare(strict_types=1);

if (!defined('FAOXIMA_SKIP_BOTAPI_ROUTER')) {
    define('FAOXIMA_SKIP_BOTAPI_ROUTER', true);
}

ob_start();

@ini_set('display_errors', '0');
@ini_set('display_startup_errors', '0');
@ini_set('log_errors', '1');
error_reporting(E_ALL);

$GLOBALS['__cube_response_sent'] = false;

function __cube_emit(int $http, array $payload): void
{
    while (ob_get_level() > 0) {
        @ob_end_clean();
    }
    if (!headers_sent()) {
        http_response_code($http);
        header('Content-Type: application/json; charset=utf-8');
        header('Cache-Control: no-store, max-age=0');
    }
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    $GLOBALS['__cube_response_sent'] = true;
}

register_shutdown_function(static function () {
    if (!empty($GLOBALS['__cube_response_sent'])) {
        return;
    }
    $err = error_get_last();
    $fatal = [E_ERROR, E_PARSE, E_COMPILE_ERROR, E_CORE_ERROR, E_USER_ERROR];
    if (is_array($err) && in_array($err['type'], $fatal, true)) {
        __cube_emit(500, ['ok' => false, 'error' => 'server_error', 'message' => 'PHP fatal: ' . $err['message']]);
        return;
    }
    __cube_emit(500, ['ok' => false, 'error' => 'server_error', 'message' => 'requestcode.php finished without emitting a response']);
});

try {
    require_once __DIR__ . '/lib/Bootstrap.php';
    require_once __DIR__ . '/lib/CubeOtp.php';

    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
        __cube_emit(405, ['ok' => false, 'error' => 'method_not_allowed', 'message' => 'POST only']);
        exit;
    }

    global $APIKEY;
    if (!is_string($APIKEY) || $APIKEY === '') {
        __cube_emit(500, ['ok' => false, 'error' => 'server_error', 'message' => 'Bot APIKEY is not configured']);
        exit;
    }

    $raw = file_get_contents('php://input');
    $body = $raw ? json_decode($raw, true) : null;
    $identifier = is_array($body) ? trim((string)($body['identifier'] ?? '')) : '';

    if ($identifier === '') {
        __cube_emit(400, ['ok' => false, 'error' => 'invalid_identifier', 'message' => 'identifier is required']);
        exit;
    }

    $resolved = CubeOtp::resolveIdentifier($identifier);
    if ($resolved['type'] === 'invalid') {
        __cube_emit(400, ['ok' => false, 'error' => 'invalid_identifier', 'message' => 'شماره یا شناسه واردشده نامعتبر است.']);
        exit;
    }

    $user = CubeOtp::findUser($resolved);
    if ($user === null) {
        __cube_emit(404, [
            'ok' => false,
            'error' => 'identifier_not_found',
            'message' => 'ابتدا ربات @cubevvpn_bot را در تلگرام استارت کنید، سپس دوباره تلاش کنید.',
        ]);
        exit;
    }

    $result = CubeOtp::issue($resolved['value'], $user);
    if (!$result['ok']) {
        __cube_emit(200, ['ok' => false, 'error' => $result['error'], 'message' => $result['message']]);
        exit;
    }

    __cube_emit(200, ['ok' => true, 'cooldown_seconds' => $result['cooldown_seconds']]);
} catch (Throwable $e) {
    __cube_emit(500, [
        'ok' => false,
        'error' => 'server_error',
        'message' => 'requestcode.php exception: ' . $e->getMessage(),
    ]);
}
