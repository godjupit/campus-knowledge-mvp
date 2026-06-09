param(
    [string]$BaseUrl = "http://localhost:8088",
    [string]$Account = "search_test_user",
    [string]$Password = "123456",
    [int]$CachedRequests = 30,
    [switch]$SkipCacheClear
)

$ErrorActionPreference = "Stop"

function Invoke-JsonPost($Url, $Body) {
    Invoke-RestMethod `
        -Uri $Url `
        -Method Post `
        -ContentType "application/json" `
        -Body ($Body | ConvertTo-Json)
}

function Get-Token() {
    $loginBody = @{
        account = $Account
        password = $Password
    }

    try {
        $loginResponse = Invoke-JsonPost "$BaseUrl/api/auth/login" $loginBody
        return $loginResponse.data.token
    } catch {
        Write-Host "Login failed, trying to register test user..." -ForegroundColor Yellow
        $registerBody = @{
            username = $Account
            email = "$Account@example.com"
            password = $Password
        }
        Invoke-JsonPost "$BaseUrl/api/auth/register" $registerBody | Out-Null
        $loginResponse = Invoke-JsonPost "$BaseUrl/api/auth/login" $loginBody
        return $loginResponse.data.token
    }
}

function Clear-HotPostsCache() {
    if ($SkipCacheClear) {
        Write-Host "Skip cache clear." -ForegroundColor Yellow
        return
    }

    try {
        redis-cli del hot:posts:top10 | Out-Null
        Write-Host "Deleted Redis key: hot:posts:top10" -ForegroundColor Green
    } catch {
        Write-Host "Could not run redis-cli. Cache may not be cleared." -ForegroundColor Yellow
    }
}

function Measure-HotRequest($Headers) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    Invoke-RestMethod -Uri "$BaseUrl/api/posts/hot" -Headers $Headers | Out-Null
    $sw.Stop()
    return [math]::Round($sw.Elapsed.TotalMilliseconds, 2)
}

function Get-CacheTtl() {
    try {
        return (redis-cli ttl hot:posts:top10)
    } catch {
        return "unknown"
    }
}

$token = Get-Token
$headers = @{
    Authorization = "Bearer $token"
}

Clear-HotPostsCache

Write-Host ""
Write-Host "Testing hot posts API: $BaseUrl/api/posts/hot" -ForegroundColor Cyan
Write-Host "First request should miss Redis and query MySQL." -ForegroundColor Cyan

$missTime = Measure-HotRequest $headers
$ttlAfterMiss = Get-CacheTtl

Write-Host "Cache miss request: ${missTime}ms"
Write-Host "Redis TTL after first request: $ttlAfterMiss"

Write-Host ""
Write-Host "Testing $CachedRequests cached requests..." -ForegroundColor Cyan

$cachedTimes = @()
for ($i = 1; $i -le $CachedRequests; $i++) {
    $time = Measure-HotRequest $headers
    $cachedTimes += $time
}

$avg = [math]::Round(($cachedTimes | Measure-Object -Average).Average, 2)
$min = [math]::Round(($cachedTimes | Measure-Object -Minimum).Minimum, 2)
$max = [math]::Round(($cachedTimes | Measure-Object -Maximum).Maximum, 2)

Write-Host ""
Write-Host "Result" -ForegroundColor Green
Write-Host "Cache miss: ${missTime}ms"
Write-Host "Cache hit avg: ${avg}ms"
Write-Host "Cache hit min: ${min}ms"
Write-Host "Cache hit max: ${max}ms"
Write-Host "Redis TTL now: $(Get-CacheTtl)"

if ($avg -gt 0) {
    $ratio = [math]::Round($missTime / $avg, 2)
    Write-Host "Miss / hit avg ratio: ${ratio}x"
}
