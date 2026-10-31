$ErrorActionPreference = "Continue"

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$mvn = "C:\Users\saian\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d\bin\mvn.cmd"
$baseDir = "D:\SOA_Project\Backend"

Write-Host "`n====================================================================" -ForegroundColor Cyan
Write-Host "       TALENTSYNC: MASTER TEST SUITE EXECUTION & VERIFICATION" -ForegroundColor Cyan
Write-Host "====================================================================`n" -ForegroundColor Cyan

$mavenResults = [System.Collections.Generic.List[PSCustomObject]]::new()

$services = @("api-gateway", "auth-service", "job-service", "application-service", "recruitment-service")

foreach ($svc in $services) {
    Write-Host "Running Maven tests for $svc..." -ForegroundColor Yellow -NoNewline
    $testOutput = & $mvn -f "$baseDir\$svc\pom.xml" test -Dtest=*Test
    $exitCode = $LASTEXITCODE

    $match = $testOutput | Select-String -Pattern "Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)" | Select-Object -Last 1
    $testsRun = 0
    $failures = 0
    $errors = 0
    if ($match -match "Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)") {
        $testsRun = [int]$matches[1]
        $failures = [int]$matches[2]
        $errors = [int]$matches[3]
    }

    $passed = ($exitCode -eq 0 -and $failures -eq 0 -and $errors -eq 0)
    $passFail = if ($passed) { "PASS" } else { "FAIL" }
    $color = if ($passed) { "Green" } else { "Red" }
    Write-Host " $passFail (Tests: $testsRun, Failures: $failures, Errors: $errors)" -ForegroundColor $color

    $mavenResults.Add([PSCustomObject]@{
        Microservice = $svc
        TestsRun = $testsRun
        Failures = $failures
        Errors = $errors
        Status = $passFail
    })
}

Write-Host "`n--------------------------------------------------------------------" -ForegroundColor Cyan
Write-Host " Running Live Gateway, Discovery & RBAC Security Verification Tests" -ForegroundColor Cyan
Write-Host "--------------------------------------------------------------------`n" -ForegroundColor Cyan

& powershell -ExecutionPolicy Bypass -File "$baseDir\run_all_security_tests.ps1"

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "                    MAVEN UNIT & INTEGRATION SUMMARY" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
$mavenResults | Format-Table -AutoSize
Write-Host "====================================================================`n" -ForegroundColor Cyan
