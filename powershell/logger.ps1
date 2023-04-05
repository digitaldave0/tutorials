function WriteLog
{
    Param ([string]$LogString)
    $LogFile = "c:\$(gc env:computername).log"
    $DateTime = "[{0:MM/dd/yy} {0:HH:mm:ss}]" -f (Get-Date)
    $LogMessage = "$Datetime $LogString"
    Add-content $LogFile -value $LogMessage
}


Function test{
WriteLog "start"
try{
1..4000 | % { WriteLog "loop $_" } -ErrorAction Stop
}
catch{
Write-Host "error found"
}
finally{
WriteLog "completed"
    }
}
