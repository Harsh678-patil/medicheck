$ErrorActionPreference = "Stop"
$url = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$zipPath = ".\maven_dl.zip"
$tmpDir  = ".\maven_tmp"
$targetDir = ".\.maven"

Write-Host "[1/4] Downloading Maven 3.9.6..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing

Write-Host "[2/4] Extracting..."
if (Test-Path $tmpDir) { Remove-Item $tmpDir -Recurse -Force }
Expand-Archive -Path $zipPath -DestinationPath $tmpDir -Force

Write-Host "[3/4] Moving files to .maven..."
$inner = Get-ChildItem $tmpDir -Directory | Select-Object -First 1
Copy-Item "$($inner.FullName)\*" $targetDir -Recurse -Force

Write-Host "[4/4] Cleaning up..."
Remove-Item $zipPath -Force
Remove-Item $tmpDir -Recurse -Force

Write-Host ""
Write-Host "SUCCESS! Maven is ready at $targetDir\bin\mvn.cmd"
& "$targetDir\bin\mvn.cmd" --version
