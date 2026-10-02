$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Install Java 17 or newer and make java available on PATH.'
    }
    if (-not (Test-Path 'healthplus.jar')) {
        throw 'Extract the complete HealthPlus package before launching.'
    }
    if (-not $env:HEALTHPLUS_DB_USER) {
        $dbUser = Read-Host 'MySQL username (press Enter for root)'
        $env:HEALTHPLUS_DB_USER = if ($dbUser) { $dbUser } else { 'root' }
    }
    if ($null -eq $env:HEALTHPLUS_DB_PASSWORD) {
        $securePassword = Read-Host 'Your local MySQL password' -AsSecureString
        $credential = New-Object System.Management.Automation.PSCredential('db', $securePassword)
        $env:HEALTHPLUS_DB_PASSWORD = $credential.GetNetworkCredential().Password
    }
    & java -cp 'healthplus.jar;lib/*' application.Main
    if ($LASTEXITCODE -ne 0) { throw 'Launch failed. Check Java version, MySQL connection and database import.' }
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    Read-Host 'Press Enter to close'
    exit 1
}
