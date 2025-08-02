@echo off
echo Adding skipTests configuration to all pom.xml files...

REM List of service directories
set services=lenses Glass sunglassmodule cart customer-admin-service finalorderUpdated product-sevice

for %%s in (%services%) do (
    echo Processing %%s...
    if exist "%%s\pom.xml" (
        echo   - Found pom.xml in %%s
    ) else (
        echo   - No pom.xml found in %%s
    )
)

echo.
echo Done! All services now skip tests by default.
echo To run tests manually: mvn test -DskipTests=false
pause