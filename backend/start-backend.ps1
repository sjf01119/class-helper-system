param(
    [int]$Port = 8080,
    [switch]$Clean
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$javaHome = Join-Path $projectRoot 'jdk-17.0.12+7'
$mavenCmd = Join-Path $projectRoot 'apache-maven-3.9.6\bin\mvn.cmd'
$pomFile = Join-Path $PSScriptRoot 'pom.xml'

if (-not (Test-Path $javaHome)) {
    throw "未找到内置 JDK: $javaHome"
}

if (-not (Test-Path $mavenCmd)) {
    throw "未找到内置 Maven: $mavenCmd"
}

$env:JAVA_HOME = $javaHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

if ($Clean) {
    & $mavenCmd -f $pomFile -DskipTests clean compile
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

& $mavenCmd "-Dspring-boot.run.arguments=--server.port=$Port" -DskipTests -f $pomFile spring-boot:run
exit $LASTEXITCODE
