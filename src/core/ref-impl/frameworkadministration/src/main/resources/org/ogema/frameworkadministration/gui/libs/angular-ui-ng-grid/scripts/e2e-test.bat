@REM
@REM This file is part of OGEMA.
@REM
@REM OGEMA is free software: you can redistribute it and/or modify
@REM it under the terms of the GNU General Public License version 3
@REM as published by the Free Software Foundation.
@REM
@REM OGEMA is distributed in the hope that it will be useful,
@REM but WITHOUT ANY WARRANTY; without even the implied warranty of
@REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
@REM GNU General Public License for more details.
@REM
@REM You should have received a copy of the GNU General Public License
@REM along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
@REM

@echo off

REM Windows script for running e2e tests
REM You have to run server and capture some browser first
REM
REM Requirements:
REM - NodeJS (http://nodejs.org/)
REM - Karma (npm install -g karma)

set BASE_DIR=%~dp0
karma start "%BASE_DIR%\..\config\karma-e2e.conf.js" %*