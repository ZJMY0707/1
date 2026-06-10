@echo off
chcp 65001 >nul
cd /d "%~dp0.."
java --module-path client\target --add-modules javafx.controls,javafx.fxml -jar client\target\client-1.0.0.jar
