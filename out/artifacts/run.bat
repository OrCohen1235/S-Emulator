@echo off
set JAVAFX_LIB=C:\Users\orcoh\Downloads\openjfx-22.0.2_windows-x64_bin-sdk\javafx-sdk-22.0.2\lib
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -jar UI.jar
pause
