#!/usr/bin/env pwsh

$filePath = "app\src\main\java\com\aeci\mmucompanion\data\repository\TimeTrackingRepositoryImpl.kt"

Write-Host "Fixing TimeTrackingRepositoryImpl.kt - replacing TimeEntry with TaskTimeEntry..."

# Read the file content
$content = Get-Content $filePath -Raw

# Replace all TimeEntry with TaskTimeEntry in method signatures and return types
$content = $content -replace ': TimeEntry\b', ': TaskTimeEntry'
$content = $content -replace 'List<TimeEntry>', 'List<TaskTimeEntry>'
$content = $content -replace 'Flow<List<TimeEntry>>', 'Flow<List<TaskTimeEntry>>'
$content = $content -replace 'Flow<TimeEntry\?>', 'Flow<TaskTimeEntry?>'
$content = $content -replace '\.toTimeEntry\(\)', '.toTaskTimeEntry()'
$content = $content -replace 'timeEntry: TimeEntry', 'timeEntry: TaskTimeEntry'
$content = $content -replace 'timeEntries: List<TimeEntry>', 'timeEntries: List<TaskTimeEntry>'

# Fix the extension function name
$content = $content -replace 'private fun TaskTimeEntryEntity\.toTimeEntry\(\): TimeEntry\?', 'private fun TaskTimeEntryEntity.toTaskTimeEntry(): TaskTimeEntry?'

# Write back to file
$content | Out-File $filePath -Encoding UTF8 -NoNewline

Write-Host "TimeTrackingRepositoryImpl.kt has been updated successfully!"
