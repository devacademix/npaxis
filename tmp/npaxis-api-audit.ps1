$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api/v1'
$dbl = 'http://localhost:8080/api/v1/api/v1'
$results = New-Object System.Collections.ArrayList
$now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$tempDir = Join-Path $PSScriptRoot 'audit-out'
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

function Add-Result {
  param(
    [string]$Module,
    [string]$Method,
    [string]$Endpoint,
    [bool]$Ok,
    [int]$StatusCode,
    [string]$Reason,
    [string]$Severity,
    $Sample
  )

  $item = [PSCustomObject]@{
    module = $Module
    method = $Method
    endpoint = $Endpoint
    ok = $Ok
    statusCode = $StatusCode
    reason = $Reason
    severity = $Severity
    sample = $Sample
  }
  [void]$results.Add($item)
}

function Parse-Json {
  param([string]$Content)
  if ([string]::IsNullOrWhiteSpace($Content)) { return $null }
  try { return $Content | ConvertFrom-Json -Depth 20 } catch { return $null }
}

function Get-AuthHeaders {
  param([string]$Token)
  if ([string]::IsNullOrWhiteSpace($Token)) { return @{} }
  return @{ Authorization = ('Bearer ' + $Token) }
}

function Invoke-TestJson {
  param(
    [string]$Module,
    [string]$Method,
    [string]$Endpoint,
    [string]$Url,
    [hashtable]$Headers,
    $Body,
    $Session
  )

  try {
    $params = @{
      Uri = $Url
      Method = $Method
      ErrorAction = 'Stop'
    }
    if ($Headers -and $Headers.Count -gt 0) { $params.Headers = $Headers }
    if ($Session) { $params.WebSession = $Session }
    if ($null -ne $Body) {
      $params.ContentType = 'application/json'
      $params.Body = ($Body | ConvertTo-Json -Depth 20)
    }

    $response = Invoke-RestMethod @params
    Add-Result -Module $Module -Method $Method -Endpoint $Endpoint -Ok $true -StatusCode 200 -Reason 'OK' -Severity 'INFO' -Sample $response
    return $response
  } catch {
    $statusCode = 0
    $content = ''
    if ($_.Exception.Response) {
      try { $statusCode = [int]$_.Exception.Response.StatusCode.value__ } catch {}
      try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Close()
      } catch {}
    }
    $json = Parse-Json -Content $content
    $reason = if ($json -and $json.message) { [string]$json.message } elseif (-not [string]::IsNullOrWhiteSpace($content)) { $content.Substring(0, [Math]::Min(220, $content.Length)) } else { $_.Exception.Message }
    Add-Result -Module $Module -Method $Method -Endpoint $Endpoint -Ok $false -StatusCode $statusCode -Reason $reason -Severity 'HIGH' -Sample $json
    return $null
  }
}

function Invoke-TestFile {
  param(
    [string]$Module,
    [string]$Method,
    [string]$Endpoint,
    [string]$Url,
    [hashtable]$Headers,
    [string[]]$FormParts,
    [string]$OutFile
  )

  $headerArgs = @()
  if ($Headers) {
    foreach ($key in $Headers.Keys) {
      $headerArgs += '-H'
      $headerArgs += ('{0}: {1}' -f $key, $Headers[$key])
    }
  }

  $cmd = @('-sS', '-o', $OutFile, '-w', '%{http_code}', '-X', $Method)
  $cmd += $headerArgs
  if ($FormParts) {
    foreach ($part in $FormParts) {
      $cmd += '-F'
      $cmd += $part
    }
  }
  $cmd += $Url

  try {
    $statusText = & curl.exe @cmd
    $statusCode = 0
    [void][int]::TryParse([string]$statusText, [ref]$statusCode)
    $ok = $statusCode -ge 200 -and $statusCode -lt 300
    $sample = if (Test-Path $OutFile) { Get-Item $OutFile | Select-Object Name, Length } else { $null }
    $reason = 'curl request failed'
    $severity = 'HIGH'
    if ($ok) {
      $reason = 'OK'
      $severity = 'INFO'
    }
    Add-Result -Module $Module -Method $Method -Endpoint $Endpoint -Ok $ok -StatusCode $statusCode -Reason $reason -Severity $severity -Sample $sample
    return $ok
  } catch {
    Add-Result -Module $Module -Method $Method -Endpoint $Endpoint -Ok $false -StatusCode 0 -Reason $_.Exception.Message -Severity 'HIGH' -Sample $null
    return $false
  }
}

function Login {
  param([string]$Email, [string]$Password, [string]$Label)
  $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
  $json = Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint ('/auth/login [' + $Label + ']') -Url ($base + '/auth/login') -Headers @{} -Body @{ email = $Email; password = $Password } -Session $session
  if ($null -eq $json) { throw ('Unable to login as ' + $Label) }
  $data = if ($json.data) { $json.data } else { $json }
  return [PSCustomObject]@{
    session = $session
    token = [string]$data.accessToken
    userId = [int]$data.userId
    role = [string]$data.role
    email = $Email
  }
}

function Find-UserIdByEmail {
  param([string]$Email)
  $sql = "select user_id from users where email = '$Email';"
  $output = & 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -U postgres -d npaxis -t -A -c $sql
  $value = ($output | Select-Object -First 1).Trim()
  if ([string]::IsNullOrWhiteSpace($value)) { return 0 }
  return [int]$value
}

$studentPhoto = Join-Path $tempDir 'student.jpg'
$preceptorPdf = Join-Path $tempDir 'license.pdf'
[IO.File]::WriteAllBytes($studentPhoto, [byte[]](255,216,255,217))
[IO.File]::WriteAllBytes($preceptorPdf, [byte[]][char[]]'%PDF-1.4 audit file')

$admin = Login -Email 'admin@npaxis.com' -Password 'admin' -Label 'admin'
$preceptor = Login -Email 'preceptor-api-check-1777474582@example.com' -Password 'password123' -Label 'preceptor'
$student = Login -Email 'vishugkp222@gmail.com' -Password 'admin' -Label 'student'

$adminHeaders = Get-AuthHeaders -Token $admin.token
$preceptorHeaders = Get-AuthHeaders -Token $preceptor.token
$studentHeaders = Get-AuthHeaders -Token $student.token

$tempStudentEmail = 'audit-student-' + $now + '@example.com'
$tempPreceptorEmail = 'audit-preceptor-' + $now + '@example.com'
$tempAdminEmail = 'audit-admin-' + $now + '@example.com'
$tempRoleName = 'ROLE_AUDIT_' + $now
$tempCredentialName = 'Audit Credential ' + $now
$tempSpecialtyName = 'Audit Specialty ' + $now

Invoke-TestJson -Module 'System' -Method 'GET' -Endpoint '/' -Url ($base + '/') -Headers @{} -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/refresh-token' -Url ($base + '/auth/refresh-token') -Headers @{} -Body $null -Session $admin.session | Out-Null
Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/forgot-password' -Url ($base + '/auth/forgot-password') -Headers @{} -Body @{ email = $student.email } -Session $null | Out-Null
Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/reset-password' -Url ($base + '/auth/reset-password') -Headers @{} -Body @{ email = $student.email; password = 'admin' } -Session $null | Out-Null
Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/verify-otp' -Url ($base + '/auth/verify-otp') -Headers @{} -Body @{ email = $student.email; otp = '123456' } -Session $null | Out-Null
Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/initialize' -Url ($base + '/auth/initialize') -Headers @{} -Body $null -Session $null | Out-Null

Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/register [student]' -Url ($base + '/auth/register') -Headers @{} -Body @{
  displayName = 'Audit Student ' + $now
  email = $tempStudentEmail
  password = 'password123'
  roleId = 1
  university = 'Audit University'
  program = 'BSN'
  graduationYear = '2027'
  phone = '+1 5551001001'
} -Session $null | Out-Null

Invoke-TestJson -Module 'Auth' -Method 'POST' -Endpoint '/auth/register [preceptor]' -Url ($base + '/auth/register') -Headers @{} -Body @{
  displayName = 'Audit Preceptor ' + $now
  email = $tempPreceptorEmail
  password = 'password123'
  roleId = 2
  credentials = @('RN')
  specialties = @('Emergency')
  location = 'Audit City'
  setting = 'Hospital'
  availableDays = @('MONDAY','TUESDAY')
  honorarium = '100'
  requirements = 'None'
  phone = '+1 5551001002'
  licenseNumber = 'AUDIT-' + $now
  licenseState = 'NY'
} -Session $null | Out-Null

$tempStudentId = Find-UserIdByEmail -Email $tempStudentEmail
$tempPreceptorId = Find-UserIdByEmail -Email $tempPreceptorEmail

Invoke-TestJson -Module 'Role' -Method 'GET' -Endpoint '/roles/active/all' -Url ($base + '/roles/active/all') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Role' -Method 'GET' -Endpoint '/roles/active/role-1' -Url ($base + '/roles/active/role-1') -Headers $adminHeaders -Body $null -Session $null | Out-Null
$createdRole = Invoke-TestJson -Module 'Role' -Method 'POST' -Endpoint '/roles' -Url ($base + '/roles') -Headers $adminHeaders -Body @{ roleName = $tempRoleName; description = 'Audit role' } -Session $null
$createdRoleData = if ($createdRole -and $createdRole.data) { $createdRole.data } else { $createdRole }
$createdRoleId = if ($createdRoleData -and $createdRoleData.roleId) { [int]$createdRoleData.roleId } else { 0 }
if ($createdRoleId -gt 0) {
  Invoke-TestJson -Module 'Role' -Method 'PUT' -Endpoint '/roles/role-{id}' -Url ($base + '/roles/role-' + $createdRoleId) -Headers $adminHeaders -Body @{ description = 'Audit role updated' } -Session $null | Out-Null
}

Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users/user/me' -Url ($base + '/users/user/me') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'User' -Method 'PUT' -Endpoint '/users/user-{student}' -Url ($base + '/users/user-' + $student.userId) -Headers $studentHeaders -Body @{ fullName = 'Student Vishal'; username = $student.email; password = 'admin'; email = $student.email; roles = @(1) } -Session $null | Out-Null
Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users' -Url ($base + '/users') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users/all' -Url ($base + '/users/all') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users/active/all' -Url ($base + '/users/active/all') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users/active/user-5' -Url ($base + '/users/active/user-5') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestFile -Module 'User' -Method 'PUT' -Endpoint '/users/user-{student}/upload-profile-picture' -Url ($base + '/users/user-' + $student.userId + '/upload-profile-picture') -Headers $studentHeaders -FormParts @('file=@' + $studentPhoto + ';type=image/jpeg') -OutFile (Join-Path $tempDir 'upload-profile-picture.out') | Out-Null
if ($tempStudentId -gt 0) {
  Invoke-TestJson -Module 'User' -Method 'DELETE' -Endpoint '/users/soft-delete/user-{id}' -Url ($base + '/users/soft-delete/user-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
  Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users/deleted/all' -Url ($base + '/users/deleted/all') -Headers $adminHeaders -Body $null -Session $null | Out-Null
  Invoke-TestJson -Module 'User' -Method 'GET' -Endpoint '/users/deleted/user-{id}' -Url ($base + '/users/deleted/user-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
  Invoke-TestJson -Module 'User' -Method 'PUT' -Endpoint '/users/restore/user-{id}' -Url ($base + '/users/restore/user-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
}
Invoke-TestFile -Module 'User' -Method 'GET' -Endpoint '/users/user-{student}/profile-picture' -Url ($base + '/users/user-' + $student.userId + '/profile-picture') -Headers $studentHeaders -FormParts @() -OutFile (Join-Path $tempDir 'student-profile-download.bin') | Out-Null

Invoke-TestJson -Module 'Student' -Method 'GET' -Endpoint '/students' -Url ($base + '/students') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Student' -Method 'GET' -Endpoint '/students/active/all' -Url ($base + '/students/active/all') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Student' -Method 'GET' -Endpoint '/students/active/student-5' -Url ($base + '/students/active/student-5') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Student' -Method 'PUT' -Endpoint '/students/student-5' -Url ($base + '/students/student-5') -Headers $studentHeaders -Body @{ displayName = 'Audit Student Existing'; university = 'Audit University'; program = 'BSN'; graduationYear = '2028'; phone = '+1 5551000000' } -Session $null | Out-Null
Invoke-TestJson -Module 'Student' -Method 'POST' -Endpoint '/students/student-5/save-preceptor/3' -Url ($base + '/students/student-5/save-preceptor/3') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Student' -Method 'GET' -Endpoint '/students/student-5/saved' -Url ($base + '/students/student-5/saved') -Headers $studentHeaders -Body $null -Session $null | Out-Null
if ($tempStudentId -gt 0) {
  Invoke-TestJson -Module 'Student' -Method 'DELETE' -Endpoint '/students/soft-delete/student-{id}' -Url ($base + '/students/soft-delete/student-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
  Invoke-TestJson -Module 'Student' -Method 'PUT' -Endpoint '/students/restore/student-{id}' -Url ($base + '/students/restore/student-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
}

Invoke-TestJson -Module 'Preceptor' -Method 'GET' -Endpoint '/preceptors/search' -Url ($base + '/preceptors/search?page=0&size=5') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Preceptor' -Method 'GET' -Endpoint '/preceptors/active/preceptor-3' -Url ($base + '/preceptors/active/preceptor-3') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Preceptor' -Method 'PUT' -Endpoint '/preceptors/preceptor-3' -Url ($base + '/preceptors/preceptor-3') -Headers $preceptorHeaders -Body @{ name = 'Preceptor API Check'; credentials = @('RN'); specialties = @('Emergency'); location = 'Test City'; setting = 'Clinic'; availableDays = @('MONDAY'); honorarium = '125'; requirements = 'Bring resume'; email = $preceptor.email; phone = '+1 5551000002'; licenseNumber = 'AUDIT-3'; licenseState = 'NY'; licenseFileUrl = $null } -Session $null | Out-Null
Invoke-TestFile -Module 'Preceptor' -Method 'POST' -Endpoint '/preceptors/preceptor-3/submit-license' -Url ($base + '/preceptors/preceptor-3/submit-license') -Headers $preceptorHeaders -FormParts @('licenseFile=@' + $preceptorPdf + ';type=application/pdf', 'file=@' + $preceptorPdf + ';type=application/pdf', 'licenseNumber=AUDIT-3', 'licenseState=NY') -OutFile (Join-Path $tempDir 'submit-license.out') | Out-Null
Invoke-TestJson -Module 'Preceptor' -Method 'GET' -Endpoint '/preceptors/active/preceptor-3/reveal-contact' -Url ($base + '/preceptors/active/preceptor-3/reveal-contact') -Headers $studentHeaders -Body $null -Session $null | Out-Null
Invoke-TestFile -Module 'Preceptor' -Method 'GET' -Endpoint '/preceptors/preceptor-3/license' -Url ($base + '/preceptors/preceptor-3/license') -Headers $preceptorHeaders -FormParts @() -OutFile (Join-Path $tempDir 'preceptor-license.bin') | Out-Null
Invoke-TestFile -Module 'Preceptor' -Method 'GET' -Endpoint '/preceptors/preceptor-3/license/view' -Url ($base + '/preceptors/preceptor-3/license/view') -Headers $preceptorHeaders -FormParts @() -OutFile (Join-Path $tempDir 'preceptor-license-view.bin') | Out-Null
if ($tempPreceptorId -gt 0) {
  Invoke-TestJson -Module 'Preceptor' -Method 'PUT' -Endpoint '/preceptors/restore/preceptor-{id}' -Url ($base + '/preceptors/restore/preceptor-' + $tempPreceptorId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
}

Invoke-TestJson -Module 'Inquiry' -Method 'POST' -Endpoint '/inquiries/send' -Url ($base + '/inquiries/send') -Headers $studentHeaders -Body @{ preceptorId = 3; subject = 'Audit Inquiry ' + $now; message = 'Frontend audit inquiry.' } -Session $null | Out-Null
Invoke-TestJson -Module 'Inquiry' -Method 'GET' -Endpoint '/inquiries/my-inquiries [student]' -Url ($base + '/inquiries/my-inquiries') -Headers $studentHeaders -Body $null -Session $null | Out-Null
$preceptorInquiries = Invoke-TestJson -Module 'Inquiry' -Method 'GET' -Endpoint '/inquiries/my-inquiries [preceptor]' -Url ($base + '/inquiries/my-inquiries') -Headers $preceptorHeaders -Body $null -Session $null
Invoke-TestJson -Module 'Inquiry' -Method 'GET' -Endpoint '/inquiries/my-inquiries?inquiryStatus=NEW' -Url ($base + '/inquiries/my-inquiries?inquiryStatus=NEW') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
$preceptorInquiryData = if ($preceptorInquiries -and $preceptorInquiries.data) { $preceptorInquiries.data } else { $preceptorInquiries }
$firstInquiryId = 0
if ($preceptorInquiryData -is [System.Array] -and $preceptorInquiryData.Length -gt 0) { $firstInquiryId = [int]$preceptorInquiryData[0].inquiryId }
if ($firstInquiryId -gt 0) {
  Invoke-TestJson -Module 'Inquiry' -Method 'PATCH' -Endpoint '/inquiries/{id}/read' -Url ($base + '/inquiries/' + $firstInquiryId + '/read') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
}

Invoke-TestJson -Module 'Analytics' -Method 'POST' -Endpoint '/api/v1/analytics/event' -Url ($dbl + '/analytics/event') -Headers $studentHeaders -Body @{ eventType = 'PROFILE_VIEW'; preceptorId = 3 } -Session $null | Out-Null
Invoke-TestJson -Module 'Analytics' -Method 'GET' -Endpoint '/api/v1/analytics/preceptors/3/stats' -Url ($dbl + '/analytics/preceptors/3/stats') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null

Invoke-TestJson -Module 'Plans' -Method 'GET' -Endpoint '/api/v1/subscription-plans' -Url ($dbl + '/subscription-plans') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'GET' -Endpoint '/api/v1/subscriptions/status' -Url ($dbl + '/subscriptions/status') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'GET' -Endpoint '/api/v1/subscriptions/history' -Url ($dbl + '/subscriptions/history') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'GET' -Endpoint '/api/v1/subscriptions/access-check' -Url ($dbl + '/subscriptions/access-check') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'GET' -Endpoint '/api/v1/subscriptions/events' -Url ($dbl + '/subscriptions/events') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'POST' -Endpoint '/api/v1/subscriptions/checkout' -Url ($dbl + '/subscriptions/checkout') -Headers $preceptorHeaders -Body @{ priceId = 1 } -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'POST' -Endpoint '/api/v1/subscriptions/cancel' -Url ($dbl + '/subscriptions/cancel') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'PUT' -Endpoint '/api/v1/subscriptions/update' -Url ($dbl + '/subscriptions/update') -Headers $preceptorHeaders -Body @{ priceId = 1 } -Session $null | Out-Null
Invoke-TestJson -Module 'Subscriptions' -Method 'GET' -Endpoint '/api/v1/subscriptions/billing-portal' -Url ($dbl + '/subscriptions/billing-portal') -Headers $preceptorHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Payments' -Method 'POST' -Endpoint '/api/v1/payments/create-checkout-session' -Url ($dbl + '/payments/create-checkout-session') -Headers $preceptorHeaders -Body @{ preceptorId = 3; billingCycle = 'MONTHLY'; successUrl = 'http://localhost:5173/preceptor/subscription'; cancelUrl = 'http://localhost:5173/preceptor/subscription' } -Session $null | Out-Null

Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/dashboard' -Url ($dbl + '/administration/dashboard') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/settings' -Url ($dbl + '/administration/settings') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/settings/platformName' -Url ($dbl + '/administration/settings/platformName') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'PUT' -Endpoint '/api/v1/administration/settings/platformName' -Url ($dbl + '/administration/settings/platformName') -Headers $adminHeaders -Body @{ value = 'NPaxis Audit' } -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/revenue/summary' -Url ($dbl + '/administration/revenue/summary') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/revenue/transactions' -Url ($dbl + '/administration/revenue/transactions') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestFile -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/dashboard/report' -Url ($dbl + '/administration/dashboard/report') -Headers $adminHeaders -FormParts @() -OutFile (Join-Path $tempDir 'admin-dashboard-report.pdf') | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/analytics/overview' -Url ($dbl + '/administration/analytics/overview') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/analytics/top-preceptors' -Url ($dbl + '/administration/analytics/top-preceptors') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/analytics/trends' -Url ($dbl + '/administration/analytics/trends') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/revenue/by-preceptor' -Url ($dbl + '/administration/revenue/by-preceptor?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'POST' -Endpoint '/api/v1/administration/add-admin' -Url ($dbl + '/administration/add-admin') -Headers $adminHeaders -Body @{ email = $tempAdminEmail; displayName = 'Audit Admin'; password = 'password123' } -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/all-admins' -Url ($dbl + '/administration/all-admins') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/pending' -Url ($dbl + '/administration/preceptors/pending?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/users' -Url ($dbl + '/administration/users') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/user-3' -Url ($dbl + '/administration/user-3') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/users/search' -Url ($dbl + '/administration/users/search?displayName=admin') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'PUT' -Endpoint '/api/v1/administration/user-8/toggle-account' -Url ($dbl + '/administration/user-8/toggle-account?enabled=true') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/students/list' -Url ($dbl + '/administration/students/list?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/students/search' -Url ($dbl + '/administration/students/search?university=Audit&page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/students/detail-5' -Url ($dbl + '/administration/students/detail-5') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'PUT' -Endpoint '/api/v1/administration/students/update-5' -Url ($dbl + '/administration/students/update-5') -Headers $adminHeaders -Body @{ displayName = 'Audit Student Existing'; university = 'Audit University'; program = 'Audit Program'; graduationYear = '2028'; phone = '+1 5551111111' } -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/students/detail-5/inquiries' -Url ($dbl + '/administration/students/detail-5/inquiries') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/list' -Url ($dbl + '/administration/preceptors/list?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/list/search' -Url ($dbl + '/administration/preceptors/list/search?location=Test&page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/detail-3' -Url ($dbl + '/administration/preceptors/detail-3') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'PUT' -Endpoint '/api/v1/administration/preceptors/update-3' -Url ($dbl + '/administration/preceptors/update-3') -Headers $adminHeaders -Body @{ displayName = 'Audit Preceptor Existing'; credentials = @('RN'); specialty = @('Emergency'); location = 'Test City'; phone = '+1 5551000002'; honorarium = '150'; requirements = 'Resume'; isVerified = $false; isPremium = $false; verificationStatus = 'PENDING' } -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/verified/approved' -Url ($dbl + '/administration/preceptors/verified/approved?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/verified/rejected' -Url ($dbl + '/administration/preceptors/verified/rejected?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/3/verification-history' -Url ($dbl + '/administration/preceptors/3/verification-history') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'POST' -Endpoint '/api/v1/administration/preceptors/3/verification-notes' -Url ($dbl + '/administration/preceptors/3/verification-notes?note=Audit%20note&noteType=REVIEW') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/3/billing' -Url ($dbl + '/administration/preceptors/3/billing') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/3/analytics' -Url ($dbl + '/administration/preceptors/3/analytics') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/detail-3/contact' -Url ($dbl + '/administration/preceptors/detail-3/contact') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestFile -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/4/license/download' -Url ($dbl + '/administration/preceptors/4/license/download') -Headers $adminHeaders -FormParts @() -OutFile (Join-Path $tempDir 'admin-license-download.bin') | Out-Null
Invoke-TestFile -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/preceptors/4/license/review' -Url ($dbl + '/administration/preceptors/4/license/review') -Headers $adminHeaders -FormParts @() -OutFile (Join-Path $tempDir 'admin-license-review.bin') | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/webhooks/history' -Url ($dbl + '/administration/webhooks/history?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/webhooks/metrics' -Url ($dbl + '/administration/webhooks/metrics') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'GET' -Endpoint '/api/v1/administration/webhooks/event-missing' -Url ($dbl + '/administration/webhooks/event-missing') -Headers $adminHeaders -Body $null -Session $null | Out-Null
Invoke-TestJson -Module 'Admin' -Method 'POST' -Endpoint '/api/v1/administration/webhooks/event-missing/retry' -Url ($dbl + '/administration/webhooks/event-missing/retry') -Headers $adminHeaders -Body $null -Session $null | Out-Null

Invoke-TestJson -Module 'Catalog' -Method 'GET' -Endpoint '/admin/credentials-specialties/credentials' -Url ($base + '/admin/credentials-specialties/credentials') -Headers $adminHeaders -Body $null -Session $null | Out-Null
$createdCredential = Invoke-TestJson -Module 'Catalog' -Method 'POST' -Endpoint '/admin/credentials-specialties/credentials' -Url ($base + '/admin/credentials-specialties/credentials') -Headers $adminHeaders -Body @{ name = $tempCredentialName; description = 'Audit credential' } -Session $null
$createdCredentialData = if ($createdCredential -and $createdCredential.data) { $createdCredential.data } else { $createdCredential }
$createdCredentialId = if ($createdCredentialData -and $createdCredentialData.id) { [int]$createdCredentialData.id } else { 0 }
if ($createdCredentialId -gt 0) {
  Invoke-TestJson -Module 'Catalog' -Method 'PUT' -Endpoint '/admin/credentials-specialties/credentials/{id}' -Url ($base + '/admin/credentials-specialties/credentials/' + $createdCredentialId) -Headers $adminHeaders -Body @{ name = $tempCredentialName; description = 'Audit credential updated' } -Session $null | Out-Null
}
Invoke-TestJson -Module 'Catalog' -Method 'GET' -Endpoint '/admin/credentials-specialties/specialties' -Url ($base + '/admin/credentials-specialties/specialties') -Headers $adminHeaders -Body $null -Session $null | Out-Null
$createdSpecialty = Invoke-TestJson -Module 'Catalog' -Method 'POST' -Endpoint '/admin/credentials-specialties/specialties' -Url ($base + '/admin/credentials-specialties/specialties') -Headers $adminHeaders -Body @{ name = $tempSpecialtyName; description = 'Audit specialty' } -Session $null
$createdSpecialtyData = if ($createdSpecialty -and $createdSpecialty.data) { $createdSpecialty.data } else { $createdSpecialty }
$createdSpecialtyId = if ($createdSpecialtyData -and $createdSpecialtyData.id) { [int]$createdSpecialtyData.id } else { 0 }
if ($createdSpecialtyId -gt 0) {
  Invoke-TestJson -Module 'Catalog' -Method 'PUT' -Endpoint '/admin/credentials-specialties/specialties/{id}' -Url ($base + '/admin/credentials-specialties/specialties/' + $createdSpecialtyId) -Headers $adminHeaders -Body @{ name = $tempSpecialtyName; description = 'Audit specialty updated' } -Session $null | Out-Null
}

Invoke-TestJson -Module 'Webhook' -Method 'POST' -Endpoint '/webhooks' -Url ($base + '/webhooks') -Headers @{ 'Stripe-Signature' = 'invalid' } -Body @{ id = 'evt_audit'; type = 'customer.subscription.updated' } -Session $null | Out-Null
Invoke-TestJson -Module 'Webhook' -Method 'GET' -Endpoint '/webhooks/events' -Url ($base + '/webhooks/events?page=0&size=10') -Headers $adminHeaders -Body $null -Session $null | Out-Null

if ($createdCredentialId -gt 0) { Invoke-TestJson -Module 'Catalog' -Method 'DELETE' -Endpoint '/admin/credentials-specialties/credentials/{id}' -Url ($base + '/admin/credentials-specialties/credentials/' + $createdCredentialId) -Headers $adminHeaders -Body $null -Session $null | Out-Null }
if ($createdSpecialtyId -gt 0) { Invoke-TestJson -Module 'Catalog' -Method 'DELETE' -Endpoint '/admin/credentials-specialties/specialties/{id}' -Url ($base + '/admin/credentials-specialties/specialties/' + $createdSpecialtyId) -Headers $adminHeaders -Body $null -Session $null | Out-Null }
if ($createdRoleId -gt 0) { Invoke-TestJson -Module 'Role' -Method 'DELETE' -Endpoint '/roles/role-{id}' -Url ($base + '/roles/role-' + $createdRoleId) -Headers $adminHeaders -Body $null -Session $null | Out-Null }
if ($tempStudentId -gt 0) {
  Invoke-TestJson -Module 'Admin' -Method 'DELETE' -Endpoint '/api/v1/administration/students/update-{id}' -Url ($dbl + '/administration/students/update-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
  Invoke-TestJson -Module 'User' -Method 'DELETE' -Endpoint '/users/hard-delete/user-{id}' -Url ($base + '/users/hard-delete/user-' + $tempStudentId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
}
if ($tempPreceptorId -gt 0) {
  Invoke-TestJson -Module 'Preceptor' -Method 'DELETE' -Endpoint '/preceptors/soft-delete/preceptor-{id}' -Url ($base + '/preceptors/soft-delete/preceptor-' + $tempPreceptorId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
  Invoke-TestJson -Module 'Preceptor' -Method 'DELETE' -Endpoint '/preceptors/hard-delete/preceptor-{id}' -Url ($base + '/preceptors/hard-delete/preceptor-' + $tempPreceptorId) -Headers $adminHeaders -Body $null -Session $null | Out-Null
}

$summary = [PSCustomObject]@{
  total = $results.Count
  passed = ($results | Where-Object { $_.ok }).Count
  failed = ($results | Where-Object { -not $_.ok }).Count
}

$output = [PSCustomObject]@{
  generatedAt = (Get-Date).ToString('o')
  summary = $summary
  results = $results
}

$output | ConvertTo-Json -Depth 10 | Set-Content -Path (Join-Path $tempDir 'npaxis-api-audit-results.json')
$summary | ConvertTo-Json -Compress
