<?php
// CubeVPN app endpoint: GET, Authorization: Bearer <token>
// See docs/api-contract.md (GET /api/accountme.php) in the CubeVPN Android repo.
//
// Reuses the existing ServiceHandler (api/handlers/ServiceHandler.php) to
// build each service's payload, so it works the same way the miniapp's own
// service screen does — Marzban/Hiddify/x-ui/stock configs, whatever the
// account already has wired up.

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
    __cube_emit(500, ['ok' => false, 'error' => 'server_error', 'message' => 'accountme.php finished without emitting a response']);
});

try {
    require_once __DIR__ . '/lib/Bootstrap.php';
    require_once __DIR__ . '/handlers/ServiceHandler.php';

    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'GET') {
        __cube_emit(405, ['ok' => false, 'error' => 'method_not_allowed', 'message' => 'GET only']);
        exit;
    }

    $token = FaoximaAuth::extractBearerToken();
    if ($token === null || $token === '') {
        __cube_emit(401, ['ok' => false, 'error' => 'unauthorized', 'message' => 'Missing bearer token']);
        exit;
    }

    $user = FaoximaAuth::userFromToken($token);
    if ($user === null) {
        __cube_emit(401, ['ok' => false, 'error' => 'unauthorized', 'message' => 'Invalid or expired session']);
        exit;
    }

    $userId = (int)$user['id'];
    $displayName = (string)($user['username'] ?? '');
    if ($displayName === '' || $displayName === 'none') {
        $displayName = (string)($user['namecustom'] ?? '');
    }
    $identifier = (string)($user['number'] ?? '');
    if ($identifier === '' || $identifier === 'none') {
        $identifier = (string)$userId;
    }
    if ($displayName === '' || $displayName === 'none') {
        $displayName = $identifier;
    }

    $invoices = FaoximaDb::fetchAll(
        "SELECT * FROM invoice
          WHERE id_user = :uid
            AND name_product != 'سرویس تست'
            AND (Status = 'active' OR Status = 'end_of_time' OR Status = 'end_of_volume'
                 OR Status = 'sendedwarn' OR Status = 'send_on_hold')
          ORDER BY id_invoice DESC",
        [':uid' => $userId]
    );

    $svcHandler = new ServiceHandler($user, []);
    $services = [];
    foreach ($invoices as $invoice) {
        $payload = null;
        try {
            $payload = $svcHandler->buildPayloadFromInvoice($invoice);
        } catch (Throwable $e) {
            $payload = null; // panel unreachable right now — skip this one, don't fail the whole request
        }
        if ($payload === null) continue;

        $subUrl = (string)($payload['subscription_url'] ?? '');
        if ($subUrl === '') continue; // nothing the app can subscribe to for this invoice

        $services[] = [
            'id' => (string)($invoice['id_invoice'] ?? $invoice['username'] ?? ''),
            'name' => (string)($payload['product_name'] ?? $invoice['username'] ?? 'Service'),
            'subscription_url' => $subUrl,
            // Best-effort fallback; the subscription-userinfo header the app
            // reads from subscription_url itself is the source of truth.
            'expire' => 0,
            'total_bytes' => (int)round((float)($payload['total_traffic_gb'] ?? 0) * 1024 ** 3),
            'used_bytes' => (int)round((float)($payload['used_traffic_gb'] ?? 0) * 1024 ** 3),
        ];
    }

    __cube_emit(200, [
        'ok' => true,
        'user' => [
            'id' => (string)$userId,
            'identifier' => $identifier,
            'display_name' => $displayName,
        ],
        'services' => $services,
    ]);
} catch (Throwable $e) {
    __cube_emit(500, [
        'ok' => false,
        'error' => 'server_error',
        'message' => 'accountme.php exception: ' . $e->getMessage(),
    ]);
}
