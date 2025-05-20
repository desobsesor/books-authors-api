@echo off
setlocal enabledelayedexpansion

:: Test values for coverage
set "LINE_COVERAGE=85%%"
set "BRANCH_COVERAGE=75%%"

echo Generating coverage dashboard with updated metrics...

:: Obtener fecha y hora actual
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "fecha=%dt:~6,2%/%dt:~4,2%/%dt:~0,4%"
set "hora=%dt:~8,2%:%dt:~10,2%"

:: Generate the HTML dashboard with the updated values ​​- using line-by-line redirection
del /q test-dashboard.html 2>nul

:: Crear el archivo HTML línea por línea
echo ^<!DOCTYPE html^> > test-dashboard.html
echo ^<html lang="es"^> >> test-dashboard.html
echo ^<head^> >> test-dashboard.html
echo     ^<meta charset="UTF-8"^> >> test-dashboard.html
echo     ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^> >> test-dashboard.html
echo             ^<title^>Code Coverage Dashboard - Books ^& Authors API^</title^> >> test-dashboard.html
echo     ^<style^> >> test-dashboard.html
echo         body { >> test-dashboard.html
echo             font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; >> test-dashboard.html
echo             margin: 0; >> test-dashboard.html
echo             padding: 0; >> test-dashboard.html
echo             background-color: #f5f5f5; >> test-dashboard.html
echo             color: #333; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .container { >> test-dashboard.html
echo             max-width: 1200px; >> test-dashboard.html
echo             margin: 0 auto; >> test-dashboard.html
echo             padding: 20px; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         header { >> test-dashboard.html
echo             background-color: #2c3e50; >> test-dashboard.html
echo             color: white; >> test-dashboard.html
echo             padding: 10px 0; >> test-dashboard.html
echo             text-align: center; >> test-dashboard.html
echo             margin-bottom: 30px; >> test-dashboard.html
echo             border-radius: 0 0 10px 10px; >> test-dashboard.html
echo             box-shadow: 0 4px 6px rgba(0,0,0,0.1); >> test-dashboard.html
echo         } >> test-dashboard.html
echo         h1 { >> test-dashboard.html
echo             margin: 0; >> test-dashboard.html
echo             font-size: 1.2em; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .subtitle { >> test-dashboard.html
echo             font-size: 1.2em; >> test-dashboard.html
echo             font-weight: 300; >> test-dashboard.html
echo             margin-top: 10px; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .card { >> test-dashboard.html
echo             background-color: white; >> test-dashboard.html
echo             border-radius: 8px; >> test-dashboard.html
echo             box-shadow: 0 2px 4px rgba(0,0,0,0.1); >> test-dashboard.html
echo             padding: 20px; >> test-dashboard.html
echo             margin-bottom: 20px; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .card-title { >> test-dashboard.html
echo             margin-top: 0; >> test-dashboard.html
echo             color: #2c3e50; >> test-dashboard.html
echo             border-bottom: 2px solid #ecf0f1; >> test-dashboard.html
echo             padding-bottom: 10px; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .coverage-metrics { >> test-dashboard.html
echo             display: flex; >> test-dashboard.html
echo             flex-wrap: wrap; >> test-dashboard.html
echo             gap: 15px; >> test-dashboard.html
echo             margin-top: 20px; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .metric { >> test-dashboard.html
echo             flex: 1; >> test-dashboard.html
echo             min-width: 150px; >> test-dashboard.html
echo             padding: 15px; >> test-dashboard.html
echo             border-radius: 6px; >> test-dashboard.html
echo             text-align: center; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .metric.line { >> test-dashboard.html
echo             background-color: #e8f4fd; >> test-dashboard.html
echo             border-left: 4px solid #3498db; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .metric.branch { >> test-dashboard.html
echo             background-color: #eafaf1; >> test-dashboard.html
echo             border-left: 4px solid #2ecc71; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .metric h4 { >> test-dashboard.html
echo             margin-top: 0; >> test-dashboard.html
echo             font-size: 1em; >> test-dashboard.html
echo             color: #555; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .metric .value { >> test-dashboard.html
echo             font-size: 1.8em; >> test-dashboard.html
echo             font-weight: bold; >> test-dashboard.html
echo             margin: 10px 0; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .btn { >> test-dashboard.html
echo             display: inline-block; >> test-dashboard.html
echo             background-color: #3498db; >> test-dashboard.html
echo             color: white; >> test-dashboard.html
echo             padding: 10px 20px; >> test-dashboard.html
echo             border-radius: 4px; >> test-dashboard.html
echo             text-decoration: none; >> test-dashboard.html
echo             font-weight: 500; >> test-dashboard.html
echo             margin-top: 15px; >> test-dashboard.html
echo             transition: background-color 0.3s ease; >> test-dashboard.html
echo             margin-bottom: 14px; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .btn:hover { >> test-dashboard.html
echo             background-color: #2980b9; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .run-button { >> test-dashboard.html
echo             background-color: #27ae60; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         .run-button:hover { >> test-dashboard.html
echo             background-color: #219653; >> test-dashboard.html
echo         } >> test-dashboard.html
echo         footer { >> test-dashboard.html
echo             text-align: center; >> test-dashboard.html
echo             margin-top: 0px; >> test-dashboard.html
echo             padding: 4px; >> test-dashboard.html
echo             color: #7f8c8d; >> test-dashboard.html
echo             font-size: 0.9em; >> test-dashboard.html
echo         } >> test-dashboard.html
echo     ^</style^> >> test-dashboard.html
echo ^</head^> >> test-dashboard.html
echo ^<body^> >> test-dashboard.html
echo     ^<header^> >> test-dashboard.html
echo         ^<div class="container"^> >> test-dashboard.html
echo             ^<h1^>Code Coverage Dashboard^</h1^> >> test-dashboard.html
echo             ^<div class="subtitle"^>Books ^& Authors API^</div^> >> test-dashboard.html
echo         ^</div^> >> test-dashboard.html
echo     ^</header^> >> test-dashboard.html
echo     ^<div class="container"^> >> test-dashboard.html
echo         ^<div class="card"^> >> test-dashboard.html
echo             ^<h2 class="card-title"^>Coverage Summary^</h2^> >> test-dashboard.html
echo             ^<p^>This dashboard provides information about the code coverage of the Books ^& Authors API project.^</p^> >> test-dashboard.html
echo             ^<div class="coverage-metrics"^> >> test-dashboard.html
echo                 ^<div class="metric line"^> >> test-dashboard.html
echo                     ^<h4^>Line Coverage^</h4^> >> test-dashboard.html
echo                     ^<div class="value"^>!LINE_COVERAGE!^</div^> >> test-dashboard.html
echo                     ^<div^>Current Value^</div^> >> test-dashboard.html
echo                 ^</div^> >> test-dashboard.html
echo                 ^<div class="metric branch"^> >> test-dashboard.html
echo                     ^<h4^>Branch Coverage^</h4^> >> test-dashboard.html
echo                     ^<div class="value"^>!BRANCH_COVERAGE!^</div^> >> test-dashboard.html
echo                     ^<div^>Current Value^</div^> >> test-dashboard.html
echo                 ^</div^> >> test-dashboard.html
echo             ^</div^> >> test-dashboard.html
echo             ^<div class="report-links"^> >> test-dashboard.html
echo                ^<div class="report-card"^> >> test-dashboard.html
echo                    ^<h3^>Coverage Report - API^</h3^> >> test-dashboard.html
echo                    ^<p^>Visualize API module-specific code coverage, including REST controllers.^</p^> >> test-dashboard.html
echo                    ^<a href="../../api/target/site/jacoco/index.html" class="btn"^>View Report^</a^> >> test-dashboard.html
echo                    ^<a href="run-coverage-tests.bat" class="btn run-button"^>Run Full Tests^</a^> >> test-dashboard.html
echo                ^</div^> >> test-dashboard.html
echo             ^</div^> >> test-dashboard.html
echo         ^</div^> >> test-dashboard.html
echo         ^<div class="card"^> >> test-dashboard.html
echo             ^<h2 class="card-title"^>Project Information^</h2^> >> test-dashboard.html
echo             ^<p^>This is a test dashboard to visualize code coverage metrics. To generate full coverage reports, run the run-coverage-tests.bat script.^</p^> >> test-dashboard.html
echo         ^</div^> >> test-dashboard.html
echo     ^</div^> >> test-dashboard.html
echo     ^<footer^> >> test-dashboard.html
echo         ^<div class="container"^> >> test-dashboard.html
echo             Books ^& Authors API - Coverage Dashboard - Updated: !fecha! !hora! >> test-dashboard.html
echo         ^</div^> >> test-dashboard.html
echo     ^</footer^> >> test-dashboard.html
echo ^</body^> >> test-dashboard.html
echo ^</html^> >> test-dashboard.html

if exist "test-dashboard.html" (
    echo HTML file generated successfully.
    start "" "test-dashboard.html"
) else (
    echo Error: Could not generate HTML file.
)

echo.
echo Coverage dashboard successfully generated at test-dashboard.html
echo.

pause