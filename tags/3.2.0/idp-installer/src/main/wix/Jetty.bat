@Echo off
REM Generate msm
setlocal

REM Preconditions

if "%1%" == "" (
  Echo JETTY [APACHE_COMMON_ZIP] [JETTY_DISTRO_ZIP]
  goto done
)
if "%2%" == "" (
  Echo JETTY [APACHE_COMMON_ZIP] [JETTY_DISTRO_ZIP]
  goto done
)

if not defined WIX (
   echo WIX should be installed
   goto done
)

REM Clear up detritus from last time

if exist jetty_contents.wxs (
   del jetty_contents.wxs
)

if exist procrun-extract (
   rd /q /s procrun-extract
)

if exist jetty-extract (
   rd /q /s jetty-extract
)

REM Set up environment

if not defined JAVA_JDK  (
   set JAVA_JDK=%JAVA_HOME%
)

if not defined JAVA_JDK  (
  echo Error: Nether JAVA_JDK nor JAVA_HOME is defined.
  exit /b
)

if not defined JARCMD (
  set JARCMD=%JAVA_JDK%\bin\jar.exe
)

if not exist "%JARCMD%" (
  echo Error: JAVA_HOME is not defined correctly.
  echo Cannot execute %JARCMD%
  exit /b
)

REM Test and extract

if not exist "%1%" (
   echo Error: Could not locate procrun zip %1%
   goto done
)

if not exist %1.asc (
   echo Error: Could not locate signature for procrun zip %1%.asc
   goto done
)

gpg --verify %1.asc %1
if ERRORLEVEL 1 (
   echo Error: Signature check failed on %1%
   goto done
)

mkdir procrun-extract
cd procrun-extract
if exist "%1" (
   "%JARCMD%" xf %1 
) else (
  "%JARCMD%" xf ..\%1 
)
cd ..

if not exist procrun-extract\prunsrv.exe (
   echo could not find prunsrv, is %1% really a procrun source?
   goto done
)

if not exist "%2" (
   echo Error: Could not locate Jetty zip %2%
   goto done
)

if not exist "%2".asc (
   echo Error: Could not locate signature for  jetty zip %2%.asc
   goto done
)

gpg --verify %2.asc %2
if ERRORLEVEL 1 (
   echo Error: Signature check failed on %1%
   goto done
)

mkdir jetty-extract
cd jetty-extract

if exist "%2" (
  "%JARCMD%" xf %2
) else (
  "%JARCMD%" xf ..\%2
)
dir /s jsp.ini 1> nl:a 2> nl:b
if ERRORLEVEL 1 (
  cd ..
  echo "Could not find jsp.ini in Jetty package"
  goto done;
)
cd ..

REM Extract Jetty

for /D %%X in (jetty-extract/*) do set jex=%%X
echo %jex% 1> jetty-extract/%jex%/JETTY_VERSION.TXT
"%WIX%/BIN/HEAT"  dir jetty-extract\%Jex% -platform -gg -dr JETTYROOT -var var.JettySrc -cg JettyGroup -out jetty_contents.wxs -nologo -srd
if ERRORLEVEL 1 goto done

REM TODO extract the Main-Class (via Properties) from start.jar 
set JETTY_CLASS=org.eclipse.jetty.start.Main

REM compile Jetty and procrun contents as well as the merge module for x86

"%WIX%/BIN/CANDLE" -nologo -dJettySrc=jetty-extract\%Jex% -dJettyClass=%JETTY_CLASS% -dProcrunSrc=procrun-extract -dPlatform=x86 -arch x86 jetty_contents.wxs MergeModule.wxs procrun.wxs -dmsitype=x86 -ext WixFirewallExtension
if ERRORLEVEL 1 goto done

REM link for x86

"%WIX%/BIN/LIGHT" -nologo -out Jetty-x86.msm jetty_contents.wixobj procrun.wixobj mergemodule.wixobj -ext WixFirewallExtension  -sw1072
if ERRORLEVEL 1 goto done

Rem tidy
del *.wixobj *.wixpdb

REM compile Jetty and procrun contents as well as the merge module for x64

"%WIX%/BIN/CANDLE" -nologo -dJettySrc=jetty-extract\%Jex% -dJettyClass=%JETTY_CLASS% -dProcrunSrc=procrun-extract -dPlatform=x86 -arch x86 jetty_contents.wxs MergeModule.wxs procrun.wxs -dmsitype=x64  -ext WixFirewallExtension
if ERRORLEVEL 1 goto done

REM link for x64

"%WIX%/BIN/LIGHT" -nologo -out Jetty-x64.msm jetty_contents.wixobj procrun.wixobj mergemodule.wixobj -ext WixFirewallExtension -sw1072
if ERRORLEVEL 1 goto done

dir Jetty-*.msm

REM Tidy up in the Sucessful exit case
   rd /q /s procrun-extract
   rd /q /s jetty-extract
   del *.wixobj *.wixpdb
   del jetty_contents.wxs

:done

