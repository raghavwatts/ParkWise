@echo off
title ParkWise - Intelligent Parking Allocation System
cd /d "%~dp0"
java -cp "target\parkwise.jar;lib\mysql-connector-j-8.3.0.jar" com.parkwise.Main
pause
