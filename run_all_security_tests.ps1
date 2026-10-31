$ErrorActionPreference = "Continue"

Write-Host "`n====================================================================" -ForegroundColor Cyan
Write-Host "       TALENTSYNC MICROSERVICES INFRASTRUCTURE & SECURITY TESTS" -ForegroundColor Cyan
Write-Host "====================================================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$eurekaUrl = "http://localhost:8761/eureka/apps"

$results = [System.Collections.Generic.List[PSCustomObject]]::new()

function Record-Test($category, $testName, $expected, $actual, $status) {
    $results.Add([PSCustomObject]@{
        Category = $category
        TestName = $testName
        Expected = $expected
        Actual = $actual
        Result = if ($status) { "PASS" } else { "FAIL" }
    })
    $passFail = if ($status) { "PASS" } else { "FAIL" }
    $color = if ($status) { "Green" } else { "Red" }
    Write-Host "[$category] $testName : " -NoNewline
    Write-Host $passFail -ForegroundColor $color
}

try {
    $eurekaApps = Invoke-RestMethod -Uri $eurekaUrl -Headers @{Accept='application/json'}
    $registered = $eurekaApps.applications.application.name
    $hasGateway = $registered -contains "API-GATEWAY"
    $hasAuth = $registered -contains "AUTH-SERVICE"
    $hasJob = $registered -contains "JOB-SERVICE"
    $hasApp = $registered -contains "APPLICATION-SERVICE"
    $hasRec = $registered -contains "RECRUITMENT-SERVICE"
    $allUp = $hasGateway -and $hasAuth -and $hasJob -and $hasApp -and $hasRec

    Record-Test "Discovery" "All 5 Microservices Registered with Eureka" "5 Registered" "$($registered.Count) Registered" $allUp
} catch {
    Record-Test "Discovery" "Eureka Discovery Live Check" "200 OK" $_.Exception.Message $false
}

$recruiterToken = ""
$candidateToken = ""

try {
    $rLogin = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"sakeeth","password":"sakeeth123"}'
    $recruiterToken = $rLogin.token
    Record-Test "Auth" "Recruiter Login & JWT Generation" "ROLE_RECRUITER" $rLogin.role ($rLogin.role -eq "ROLE_RECRUITER" -and !([string]::IsNullOrEmpty($rLogin.token)))
} catch {
    Record-Test "Auth" "Recruiter Login & JWT Generation" "200 OK" $_.Exception.Message $false
}

try {
    $cLogin = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"sai","password":"saianilkumar123"}'
    $candidateToken = $cLogin.token
    Record-Test "Auth" "Candidate Login & JWT Generation" "ROLE_CANDIDATE" $cLogin.role ($cLogin.role -eq "ROLE_CANDIDATE" -and !([string]::IsNullOrEmpty($cLogin.token)))
} catch {
    Record-Test "Auth" "Candidate Login & JWT Generation" "200 OK" $_.Exception.Message $false
}

try {
    $null = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Get
    Record-Test "Security" "Missing Authorization Header Blocked" "401" "200" $false
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Record-Test "Security" "Missing Authorization Header Blocked" "401 Unauthorized" "$code" ($code -eq 401)
}

try {
    $null = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Get -Headers @{Authorization="Bearer $($candidateToken)corrupted"}
    Record-Test "Security" "Tampered Token Blocked" "401" "200" $false
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Record-Test "Security" "Tampered Token Blocked" "401 Unauthorized" "$code" ($code -eq 401)
}

$jobId = $null
try {
    $jobPayload = '{"companyName":"Amazon AWS","role":"DevOps Specialist","location":"Hyderabad","description":"Docker, Kubernetes, CI/CD","status":"OPEN"}'
    $jobRes = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $recruiterToken"} -Body $jobPayload
    $jobId = $jobRes.jobId
    Record-Test "RBAC Positive" "Recruiter Allowed to Create Job" "Job Created" "ID: $jobId" ($jobId -gt 0)
} catch {
    Record-Test "RBAC Positive" "Recruiter Allowed to Create Job" "201 Created" $_.Exception.Message $false
}

try {
    $fakeJob = '{"companyName":"HackerCo","role":"Attacker","location":"Remote","description":"Forbidden"}'
    $null = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $candidateToken"} -Body $fakeJob
    Record-Test "RBAC Negative" "Candidate Blocked From Creating Job" "403" "200" $false
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Record-Test "RBAC Negative" "Candidate Blocked From Creating Job" "403 Forbidden" "$code" ($code -eq 403)
}

$appId = $null
try {
    $appPayload = @"
{"jobId":$jobId,"candidateId":1,"resumeLink":"https://sai.dev/resume.pdf"}
"@
    $appRes = Invoke-RestMethod -Uri "$baseUrl/applications" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $candidateToken"} -Body $appPayload
    $appId = $appRes.applicationId
    Record-Test "Inter-Service Feign" "Candidate Applies for Job (Feign validates job)" "APPLIED" $appRes.status ($appRes.status -eq "APPLIED")
} catch {
    Record-Test "Inter-Service Feign" "Candidate Applies for Job (Feign validates job)" "201 Created" $_.Exception.Message $false
}

try {
    $rAppPayload = @"
{"jobId":$jobId,"candidateId":2,"resumeLink":"https://recruiter.dev/resume.pdf"}
"@
    $null = Invoke-RestMethod -Uri "$baseUrl/applications" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $recruiterToken"} -Body $rAppPayload
    Record-Test "RBAC Negative" "Recruiter Blocked From Applying for Job" "403" "200" $false
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Record-Test "RBAC Negative" "Recruiter Blocked From Applying for Job" "403 Forbidden" "$code" ($code -eq 403)
}

try {
    $recPayload = @"
{"applicationId":$appId,"interviewStatus":"SCHEDULED","finalStatus":"PENDING"}
"@
    $recRes = Invoke-RestMethod -Uri "$baseUrl/recruitments" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $recruiterToken"} -Body $recPayload
    
    $appCheck = Invoke-RestMethod -Uri "$baseUrl/applications/$appId" -Method Get -Headers @{Authorization="Bearer $recruiterToken"}
    $isSynced = ($appCheck.status -eq "INTERVIEW")
    Record-Test "Inter-Service Feign" "Recruitment Service Updates Application via OpenFeign" "INTERVIEW" $appCheck.status $isSynced
} catch {
    Record-Test "Inter-Service Feign" "Recruitment Service Updates Application via OpenFeign" "Synced" $_.Exception.Message $false
}

try {
    $null = Invoke-RestMethod -Uri "$baseUrl/recruitments/1" -Method Get -Headers @{Authorization="Bearer $candidateToken"}
    Record-Test "RBAC Negative" "Candidate Blocked From Accessing Recruitments" "403" "200" $false
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Record-Test "RBAC Negative" "Candidate Blocked From Accessing Recruitments" "403 Forbidden" "$code" ($code -eq 403)
}

try {
    $null = Invoke-RestMethod -Uri "$baseUrl/student/all" -Method Get -Headers @{Authorization="Bearer $candidateToken"}
    Record-Test "RBAC Negative" "Candidate Blocked From Viewing Student Roster" "403" "200" $false
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Record-Test "RBAC Negative" "Candidate Blocked From Viewing Student Roster" "403 Forbidden" "$code" ($code -eq 403)
}

try {
    $studs = Invoke-RestMethod -Uri "$baseUrl/student/all" -Method Get -Headers @{Authorization="Bearer $recruiterToken"}
    Record-Test "Routing Positive" "Recruiter Allowed to View Student Roster via Gateway" "List" "$($studs.Count) candidates" ($studs.Count -gt 0)
} catch {
    Record-Test "Routing Positive" "Recruiter Allowed to View Student Roster via Gateway" "200 OK" $_.Exception.Message $false
}

Write-Host "`n====================================================================" -ForegroundColor Cyan
Write-Host "                        TEST RESULTS SUMMARY" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan

$results | Format-Table -AutoSize
$passedCount = ($results | Where-Object { $_.Result -eq "PASS" }).Count
$totalCount = $results.Count

$summaryColor = if ($passedCount -eq $totalCount) { "Green" } else { "Red" }
Write-Host "Total Tests: $totalCount | Passed: $passedCount | Failed: $($totalCount - $passedCount)" -ForegroundColor $summaryColor
Write-Host "====================================================================`n" -ForegroundColor Cyan
