param(
    [Parameter(Mandatory = $true)]
    [string]$AwsAccountId,

    [string]$AwsRegion = "us-east-1",
    [string]$Repository = "battleship-command",
    [string]$Tag = "2.0.0"
)

$ErrorActionPreference = "Stop"

$Registry = "${AwsAccountId}.dkr.ecr.${AwsRegion}.amazonaws.com"
$RemoteImage = "${Registry}/${Repository}:${Tag}"

aws ecr describe-repositories `
    --repository-names $Repository `
    --region $AwsRegion *> $null

if ($LASTEXITCODE -ne 0) {
    aws ecr create-repository `
        --repository-name $Repository `
        --region $AwsRegion
}

aws ecr get-login-password --region $AwsRegion |
    docker login --username AWS --password-stdin $Registry

docker tag "${Repository}:${Tag}" $RemoteImage
docker push $RemoteImage

aws ecr describe-images `
    --repository-name $Repository `
    --region $AwsRegion `
    --image-ids imageTag=$Tag

Write-Host "Pushed $RemoteImage"
