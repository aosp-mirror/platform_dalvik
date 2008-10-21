@echo off
REM Copyright (C) 2007 The Android Open Source Project
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM     http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

REM don't modify the caller's environment
setlocal

REM Locate dx.jar in the directory where dx.bat was found and start it.

REM Set up prog to be the path of this script, including following symlinks,
REM and set up progdir to be the fully-qualified pathname of its directory.
set prog=%~f0

REM Change current directory to where dx is, to avoid issues with directories
REM containing whitespaces.
cd /d %~dp0

set jarfile=dx.jar
set frameworkdir=

if exist %frameworkdir%%jarfile% goto JarFileOk
    set frameworkdir=lib\

if exist %frameworkdir%%jarfile% goto JarFileOk
    set frameworkdir=..\framework\

:JarFileOk

set jarpath=%frameworkdir%%jarfile%

set javaOpts=

REM If you want DX to have more memory when executing, uncomment the
REM following line and adjust the value accordingly. Use "java -X" for
REM a list of options you can pass here.
REM 
REM set javaOpts=-Xmx256M

call java %javaOpts% -Djava.ext.dirs=%frameworkdir% -jar %jarpath% %*

