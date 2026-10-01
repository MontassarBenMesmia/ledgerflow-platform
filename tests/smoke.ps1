$ErrorActionPreference = 'Stop'
$tokenResponse = Invoke-RestMethod -Method Post -Uri 'http://localhost/realms/ledgerflow/protocol/openid-connect/token' -ContentType 'application/x-www-form-urlencoded' -Body @{client_id='ledgerflow-web';username='operator';password='operator123';grant_type='password'}
$headers = @{Authorization="Bearer $($tokenResponse.access_token)"}
$accounts = Invoke-RestMethod -Uri 'http://localhost/api/ledger/accounts' -Headers $headers
$key = [guid]::NewGuid().ToString()
$body = @{sourceAccountId=$accounts[0].id;destinationAccountId=$accounts[1].id;amount=10.25;currency='EUR'} | ConvertTo-Json
$first = Invoke-RestMethod -Method Post -Uri 'http://localhost/api/ledger/transfers' -Headers ($headers + @{'Idempotency-Key'=$key}) -ContentType 'application/json' -Body $body
$retry = Invoke-RestMethod -Method Post -Uri 'http://localhost/api/ledger/transfers' -Headers ($headers + @{'Idempotency-Key'=$key}) -ContentType 'application/json' -Body $body
if ($first.transferId -ne $retry.transferId) { throw 'Idempotency check failed' }
Start-Sleep -Seconds 3
$reconciliation = Invoke-RestMethod -Uri 'http://localhost/api/projection/reconciliation' -Headers $headers
if ($reconciliation.status -ne 'HEALTHY' -or $reconciliation.projectedTransfers -lt 1) { throw 'Projection or reconciliation check failed' }
Write-Host "Smoke test passed: $($first.transferId) was projected and reconciled."
