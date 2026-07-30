<?php
// CubeVPN app endpoint: POST { "identifier": "...", "code": "12345" }
// See docs/api-contract.md (POST /api/verifycode.php) in the CubeVPN Android repo.

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
    __cube_emit(500, ['ok' => false, 'error' => 'server_error', 'message' => 'verifycode.php finished without emitting a response']);
});

try {
    require_once __DIR__ . '/lib/Bootstrap.php';
    require_once __DIR__ . '/lib/CubeOtp.php';

    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
        __cube_emit(405, ['ok' => false, 'error' => 'method_not_allowed', 'message' => 'POST only']);
        exit;
    }

    $raw = file_get_contents('php://input');
    $body = $raw ? json_decode($raw, true) : null;
    $identifier = is_array($body) ? trim((string)($body['identifier'] ?? '')) : '';
    $code = is_array($body) ? trim((string)($body['code'] ?? '')) : '';

    if ($identifier === '' || $code === '') {
        __cube_emit(400, ['ok' => false, 'error' => 'invalid_identifier', 'message' => 'identifier and code are required']);
        exit;
    }

    $resolved = CubeOtp::resolveIdentifier($identifier);
    if ($resolved['type'] === 'invalid') {
        __cube_emit(400, ['ok' => false, 'error' => 'invalid_identifier', 'message' => 'شماره یا شناسه واردشده نامعتبر است.']);
        exit;
    }

    $result = CubeOtp::verify($resolved['value'], $code);
    if (!$result['ok']) {
        __cube_emit(200, ['ok' => false, 'error' => $result['error'], 'message' => $result['message']]);
        exit;
    }

    $userId = $result['user_id'];
    $userRow = select('user', '*', 'id', $userId, 'select');
    if (!is_array($userRow) || empty($userRow)) {
        __cube_emit(500, ['ok' => false, 'error' => 'server_error', 'message' => 'User record not found after verification']);
        exit;
    }

    $token = FaoximaAuth::issueToken($userId);
    $displayName = (string)($userRow['username'] ?? '');
    if ($displayName === '') {
        $displayName = (string)($userRow['namecustom'] ?? '');
    }
    if ($displayName === '' || $displayName === 'none') {
        $displayName = $resolved['value'];
    }

    __cube_emit(200, [
        'ok' => true,
        'token' => $token,
        'user' => [
            'id' => (string)$userId,
            'identifier' => $resolved['value'],
            'display_name' => $displayName,
        ],
    ]);
} catch (Throwable $e) {
    __cube_emit(500, [
        'ok' => false,
        'error' => 'server_error',
        'message' => 'verifycode.php exception: ' . $e->getMessage(),
    ]);
}
