# Uso (PowerShell o cmd): powershell -ExecutionPolicy Bypass -File cobertura.ps1
# Lee el XML de JaCoCo y muestra el % total y las 10 clases con más líneas sin cubrir.
$f = Get-ChildItem app\build -Recurse -Filter jacocoTestReport.xml | Select-Object -First 1
if (-not $f) { Write-Host "No hay jacocoTestReport.xml. Corre: gradlew testDebugUnitTest jacocoTestReport"; exit 1 }
$t = Get-Content $f.FullName -Raw -Encoding UTF8

# Totales del reporte: contadores que quedan después del último </package>
$tot = [regex]::Match($t, '(?s)</package>(?!.*</package>)(.*)</report>').Groups[1].Value
Write-Host "== COBERTURA TOTAL =="
[regex]::Matches($tot, '<counter type="(\w+)" missed="(\d+)" covered="(\d+)"/>') | ForEach-Object {
  $m = [int]$_.Groups[2].Value; $c = [int]$_.Groups[3].Value
  "{0,-12} {1,6:P1}   ({2} cubiertas / {3} total)" -f $_.Groups[1].Value, ($c / [math]::Max(1, $m + $c)), $c, ($m + $c)
}

# Clases con más líneas sin cubrir
Write-Host "`n== 10 CLASES CON MAS LINEAS SIN CUBRIR =="
$filas = [regex]::Matches($t, '(?s)<class name="([^"]+)".*?</class>') | ForEach-Object {
  $l = [regex]::Match($_.Value, '<counter type="LINE" missed="(\d+)" covered="(\d+)"/>')
  if ($l.Success) {
    [pscustomobject]@{ Clase = $_.Groups[1].Value.Replace('com/aprendoiasantiago/', ''); Sin_cubrir = [int]$l.Groups[1].Value; Cubiertas = [int]$l.Groups[2].Value }
  }
}
$filas | Sort-Object Sin_cubrir -Descending | Select-Object -First 10 | Format-Table -AutoSize
Write-Host "HTML: app\build\reports\jacoco\jacocoTestReport\html\index.html"
