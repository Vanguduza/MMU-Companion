# PowerShell script to set JAVA_HOME and update PATH for JDK 17
$JdkPath = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', $JdkPath, [System.EnvironmentVariableTarget]::Machine)
$oldPath = [System.Environment]::GetEnvironmentVariable('Path', [System.EnvironmentVariableTarget]::Machine)
if ($oldPath -notlike "$JdkPath\bin*") {
    $newPath = "$JdkPath\bin;" + $oldPath
    [System.Environment]::SetEnvironmentVariable('Path', $newPath, [System.EnvironmentVariableTarget]::Machine)
}
Write-Host "JAVA_HOME and PATH have been set for all users. Please restart your terminal or computer for changes to take effect."
