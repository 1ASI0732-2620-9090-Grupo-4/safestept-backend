param(
    [string]$BaseUrl = 'http://127.0.0.1:8092/api/v1'
)

$ErrorActionPreference = 'Stop'
$username = 'smoke.' + [guid]::NewGuid().ToString('N').Substring(0, 12)
$password = 'SmokeOnly!' + [guid]::NewGuid().ToString('N')
$credentials = @{ username = $username; password = $password } | ConvertTo-Json

$registration = Invoke-WebRequest "$BaseUrl/authentication/sign-up" -Method Post -ContentType 'application/json' -Body $credentials
$session = Invoke-RestMethod "$BaseUrl/authentication/sign-in" -Method Post -ContentType 'application/json' -Body $credentials
if (-not $session.token) { throw 'Authentication did not return an access token.' }
if ($session.roles -notcontains 'ROLE_USER' -or $session.roles -contains 'ROLE_ADMIN') {
    throw 'A new account did not receive the expected user-only role.'
}
$headers = @{ Authorization = "Bearer $($session.token)" }

$simulations = Invoke-RestMethod "$BaseUrl/simulations" -Headers $headers
$products = Invoke-RestMethod "$BaseUrl/commerce/products" -Headers $headers
if ($simulations.Count -eq 0) { throw 'No simulation is available for the smoke test.' }
$simulation = $simulations[0]
if (-not $simulation.id) { throw "Simulation response has no id (type: $($simulation.GetType().FullName))." }
$startedAt = (Get-Date).ToUniversalTime().AddSeconds(-30).ToString('o')
$completedAt = (Get-Date).ToUniversalTime().ToString('o')
$attemptBody = @{
    mode = 'practice'
    startedAt = $startedAt
    completedAt = $completedAt
    score = 100
    totalSteps = @($simulation.steps).Count
    correctSteps = @($simulation.steps).Count
    timeElapsed = 30
    errors = @()
} | ConvertTo-Json -Depth 5
$attempt = Invoke-WebRequest "$BaseUrl/simulations/$($simulation.id)/attempts" -Headers $headers -Method Post -ContentType 'application/json' -Body $attemptBody
$history = Invoke-RestMethod "$BaseUrl/simulations/attempts/me" -Headers $headers
$progress = Invoke-RestMethod "$BaseUrl/gamification/summary/me" -Headers $headers
$openApi = Invoke-WebRequest ($BaseUrl -replace '/api/v1$', '/v3/api-docs')
$adminDeleteStatus = $null
try {
    Invoke-WebRequest "$BaseUrl/simulations/$($simulation.id)" -Headers $headers -Method Delete | Out-Null
    throw 'A regular user was able to delete a simulation.'
} catch {
    if ($_.Exception.Response) {
        $adminDeleteStatus = [int]$_.Exception.Response.StatusCode
    } else {
        throw
    }
}
if ($adminDeleteStatus -ne 403) { throw "Expected HTTP 403 for admin deletion; got $adminDeleteStatus." }

[pscustomobject]@{
    RegistrationStatus = $registration.StatusCode
    TokenReturned = [bool]$session.token
    UserRole = 'ROLE_USER'
    SimulationCount = $simulations.Count
    ProductCount = $products.Count
    AttemptStatus = $attempt.StatusCode
    HistoryCount = $history.Count
    CompletedSimulations = $progress.completedSimulations
    OpenApiStatus = $openApi.StatusCode
    AdminDeleteStatus = $adminDeleteStatus
}
