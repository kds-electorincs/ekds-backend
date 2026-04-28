# KDS development environment setup script
# Run from project root: .\scripts\setup-db.ps1
# Or via the convenience wrapper: setup.bat

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  KDS Development Database Setup" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# --- Pre-flight checks ----------------------------------------------------

# Check psql is on PATH
$psql = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psql) {
    Write-Host "ERROR: psql not found on PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "Install PostgreSQL 17 from:" -ForegroundColor Yellow
    Write-Host "  https://www.enterprisedb.com/downloads/postgres-postgresql-downloads"
    Write-Host ""
    Write-Host "Then add to PATH (one-time, run as Administrator):"
    Write-Host '  [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\PostgreSQL\17\bin", "Machine")'
    Write-Host ""
    Write-Host "Close and reopen PowerShell, then re-run this script."
    exit 1
}
Write-Host "[OK] psql found: $($psql.Source)" -ForegroundColor Green

# Check Postgres service is reachable
try {
    $tcpTest = Test-NetConnection -ComputerName localhost -Port 5432 -InformationLevel Quiet -WarningAction SilentlyContinue
    if (-not $tcpTest) {
        throw "no listener"
    }
} catch {
    Write-Host "ERROR: Postgres is not running on localhost:5432." -ForegroundColor Red
    Write-Host ""
    Write-Host "Start the service from PowerShell (as Administrator):"
    Write-Host "  Start-Service postgresql-x64-17"
    Write-Host ""
    Write-Host "Or open Services.msc and start 'postgresql-x64-17'."
    exit 1
}
Write-Host "[OK] Postgres is listening on localhost:5432" -ForegroundColor Green

# --- Locate SQL file ------------------------------------------------------

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptDir "setup-db.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found at $sqlFile" -ForegroundColor Red
    exit 1
}

# --- Get postgres superuser password --------------------------------------

Write-Host ""
Write-Host "This script will create:" -ForegroundColor Yellow
Write-Host "  - Database: kds_dev"
Write-Host "  - User:     kds_user"
Write-Host "  - Password: kds_dev_password (development only)"
Write-Host ""
Write-Host "It needs the 'postgres' superuser password" -ForegroundColor Yellow
Write-Host "(set during PostgreSQL installation, NOT the kds_user password)."
Write-Host ""

$securePw = Read-Host "Postgres superuser password" -AsSecureString
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePw)
$pgPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)

if ([string]::IsNullOrEmpty($pgPassword)) {
    Write-Host "ERROR: Password cannot be empty." -ForegroundColor Red
    exit 1
}

# --- Run SQL setup --------------------------------------------------------

# psql reads PGPASSWORD from environment; safer than putting it on the command line
$env:PGPASSWORD = $pgPassword

try {
    Write-Host ""
    Write-Host "Step 1/2: Creating database, user, and database-level privileges..." -ForegroundColor Cyan
    & psql -U postgres -h localhost -d postgres -v ON_ERROR_STOP=1 -f $sqlFile
    if ($LASTEXITCODE -ne 0) {
        throw "psql exited with code $LASTEXITCODE during step 1."
    }

    Write-Host ""
    Write-Host "Step 2/2: Granting schema privileges inside kds_dev..." -ForegroundColor Cyan
    & psql -U postgres -h localhost -d kds_dev -v ON_ERROR_STOP=1 -c "GRANT ALL ON SCHEMA public TO kds_user;"
    if ($LASTEXITCODE -ne 0) {
        throw "psql exited with code $LASTEXITCODE during step 2."
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: Database setup failed." -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Common causes:" -ForegroundColor Yellow
    Write-Host "  - Wrong postgres superuser password"
    Write-Host "  - Postgres service stopped"
    Write-Host "  - Port 5432 occupied by another process"
    exit 1
} finally {
    # Always clear the password from the environment
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
    $pgPassword = $null
}

# --- Verify connection ----------------------------------------------------

Write-Host ""
Write-Host "Step 3/3: Verifying kds_user can connect..." -ForegroundColor Cyan

$env:PGPASSWORD = "kds_dev_password"
try {
    $verify = & psql -U kds_user -h localhost -d kds_dev -t -A -c "SELECT 'connected as ' || current_user || ' to ' || current_database();"
    if ($LASTEXITCODE -ne 0) {
        throw "Verification connection failed."
    }
    Write-Host "[OK] $verify" -ForegroundColor Green
} catch {
    Write-Host "ERROR: kds_user cannot connect after setup. Something went wrong." -ForegroundColor Red
    exit 1
} finally {
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

# --- Ensure .env exists ---------------------------------------------------

$projectRoot = Split-Path -Parent $scriptDir
$envFile = Join-Path $projectRoot ".env"

if (-not (Test-Path $envFile)) {
    Write-Host ""
    Write-Host "Creating .env file at project root..." -ForegroundColor Cyan
    $envContent = @"
KDS_DB_URL=jdbc:postgresql://localhost:5432/kds_dev
KDS_DB_USERNAME=kds_user
KDS_DB_PASSWORD=kds_dev_password
"@
    [System.IO.File]::WriteAllText($envFile, $envContent)
    Write-Host "[OK] Created .env" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[OK] .env already exists (not overwriting)" -ForegroundColor Green
}

# --- Done -----------------------------------------------------------------

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "  Setup complete!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "You can now run the application:" -ForegroundColor Cyan
Write-Host "  .\gradlew :kds-web:bootRun"
Write-Host ""