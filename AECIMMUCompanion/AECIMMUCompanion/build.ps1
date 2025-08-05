$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
Write-Host "JAVA_HOME set to: $env:JAVA_HOME"
Write-Host "Running Gradle build..."
& .\gradlew.bat clean --refresh-dependencies 