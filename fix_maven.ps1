Expand-Archive -Path 'maven.zip' -DestinationPath '.maven_tmp' -Force
$inner = Get-ChildItem '.maven_tmp' | Select-Object -First 1
Copy-Item "$($inner.FullName)\*" '.maven' -Recurse -Force
Remove-Item '.maven_tmp' -Recurse -Force
Write-Host 'Maven extracted successfully!'
Get-ChildItem '.maven\bin'
