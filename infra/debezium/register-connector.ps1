param(
    [string]$ConnectUrl = "http://localhost:28083",
    [string]$ConnectorName = "restaurant-outbox"
)

$ErrorActionPreference = "Stop"
$configPath = Join-Path $PSScriptRoot "restaurant-outbox.json"
$config = Get-Content -LiteralPath $configPath -Raw
$baseUrl = $ConnectUrl.TrimEnd('/')
$name = [Uri]::EscapeDataString($ConnectorName)

# PUT creates a missing connector or updates an existing connector configuration.
# Run only after restaurant-service has created public.outbox_events via Flyway.
Invoke-RestMethod -Method Put `
    -Uri "$baseUrl/connectors/$name/config" `
    -ContentType "application/json" `
    -Body $config | ConvertTo-Json -Depth 10

Write-Host "Connector configuration submitted. Check task status:"
Write-Host "$baseUrl/connectors/$name/status"
