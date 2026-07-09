param(
    [string]$Repository = "battleship-command",
    [string]$Tag = "2.0.0"
)

$ErrorActionPreference = "Stop"

docker build `
    -f deploy/docker/Dockerfile `
    -t "${Repository}:${Tag}" `
    .

Write-Host "Built image ${Repository}:${Tag}"
