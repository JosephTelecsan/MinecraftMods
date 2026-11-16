# Syncs the newest jar for each manhunt mod into .\releases
# and removes older jars of the same mod from that folder.
# Run from the repo root:  .\scripts\sync-releases.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Releases = Join-Path $Root "releases"
$ModsRoot = Join-Path $Root "mods"
New-Item -ItemType Directory -Force -Path $Releases | Out-Null

$Mods = @(
	@{
		Id = "manhunt-more-pearls"
		Pattern = "manhunt-more-pearls-*.jar"
		Search = @((Join-Path $ModsRoot "more-pearls\build\libs"))
	}
	@{
		Id = "manhunt-teamchat"
		Pattern = "manhunt-teamchat-*.jar"
		Search = @((Join-Path $ModsRoot "teamchat\build\libs"))
	}
	@{
		Id = "manhunt-pausegame"
		Pattern = "manhunt-pausegame-*.jar"
		Search = @((Join-Path $ModsRoot "pausegame\build\libs"))
	}
	@{
		Id = "manhunt-entity-counter"
		Pattern = "manhunt-entity-counter-*.jar"
		Search = @((Join-Path $ModsRoot "entity-counter\build\libs"))
	}
	@{
		Id = "spectatorplus-fabric"
		Pattern = "spectatorplus-fabric-*.jar"
		Search = @((Join-Path $ModsRoot "spectatorplus\fabric\build\libs"))
	}
)

function Get-LatestJar([string[]]$SearchDirs, [string]$Pattern) {
	$candidates = foreach ($dir in $SearchDirs) {
		if (-not (Test-Path $dir)) { continue }
		Get-ChildItem -Path $dir -Filter $Pattern -File -ErrorAction SilentlyContinue |
			Where-Object { $_.Name -notmatch "sources|javadoc|-dev" }
	}
	if (-not $candidates) { return $null }
	return $candidates |
		Sort-Object Name, LastWriteTime -Descending |
		Select-Object -First 1
}

Write-Host "Syncing latest jars -> $Releases"
foreach ($mod in $Mods) {
	$latest = Get-LatestJar $mod.Search $mod.Pattern
	if (-not $latest) {
		Write-Host ("  SKIP  {0} (no built jar found)" -f $mod.Id) -ForegroundColor Yellow
		continue
	}

	Get-ChildItem -Path $Releases -Filter ($mod.Id + "*.jar") -File -ErrorAction SilentlyContinue |
		Where-Object { $_.Name -ne $latest.Name } |
		ForEach-Object {
			Write-Host ("  RM    {0}" -f $_.Name) -ForegroundColor DarkGray
			Remove-Item $_.FullName -Force
		}

	$dest = Join-Path $Releases $latest.Name
	Copy-Item $latest.FullName $dest -Force
	Write-Host ("  OK    {0}" -f $latest.Name) -ForegroundColor Green
}

Write-Host ""
Write-Host "Current releases:"
Get-ChildItem $Releases -Filter "*.jar" | Sort-Object Name | ForEach-Object {
	Write-Host ("  - {0}" -f $_.Name)
}
