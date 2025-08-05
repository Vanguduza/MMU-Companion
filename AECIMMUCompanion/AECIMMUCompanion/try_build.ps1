# PowerShell script to try different SSL configurations for Gradle build

Write-Host "Attempting Gradle build with SSL bypass..." -ForegroundColor Yellow

# Stop any running Gradle daemons
.\gradlew --stop

# Try build with SSL verification disabled
$env:GRADLE_OPTS = "-Dcom.sun.net.ssl.checkRevocation=false -Dtrust_all_cert=true -Djavax.net.ssl.trustStore=NONE -Djavax.net.ssl.trustStoreType=Windows-ROOT"

Write-Host "Building with SSL bypass configuration..." -ForegroundColor Green

# Attempt the build
.\gradlew clean assembleDebug --stacktrace --info --no-daemon

Write-Host "Build attempt completed." -ForegroundColor Yellow
