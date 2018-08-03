@echo off
rem -- ----------------------------------------------------------------
rem -- 2014-02-17/FK: Changes to javac: -source and -target omitted
rem -- 2014-02-06/FK: Bat-compile reintroduced when no Ant available
rem -- 17-nov-2004/FK Javac 1.5 compatible
rem -- 20-oct-2004/FK
rem -- This file compiles the ChatServer.
rem -- Arguments: 
rem --   With no arguments compiles the service.
rem --   clean             Cleans out the build tree.
rem --   cleanall          Cleans out the build and dist trees.
rem --
rem -- At the end of a successfull compilation, jar-files should be
rem -- in the LIB directory. From there they should be moved onto
rem -- a web server for deployment.
rem --
rem -- Fredrik Kilander, fki@kth.se
rem -- ----------------------------------------------------------------

rem -- The root of the development file tree, defined as this directory.

set ROOT=%~dp0..


rem -- The location of files generated for distribution.

set DST=%ROOT%\dist


rem -- The location of class-files generated by the compilers.

set BLD=%ROOT%\build


rem -- The location the Jini libraries needed by the compiler.

set LIB=%ROOT%\lib


rem -- The location of the top of the source file hierarchy.

set SRC=%ROOT%\src


rem -- The classpath for the Java compiler.

set CLP=%BLD%;%LIB%\jini-core.jar;%LIB%\jini-ext.jar


rem -- The fully qualified package name.

set PNR=dsv.pis.chat.server


rem -- The path to the package root (relative the start of the source
rem -- tree). We use the substution syntax available
rem -- in the SET command to replace each period with a backslash.

set PPR=%PNR:.=\%


rem -- Examine the commandline argument and do what we recognize.

if "%1"=="clean" call :clean & goto :eof
if "%1"=="cleanall" call :cleanall & goto :eof
if not "%1"=="" echo Unrecognized argument %1 & goto :eof

rem -- ----------------------------------------------------------------
rem -- Ensure that directories we need exist.
rem -- ----------------------------------------------------------------

if not exist "%DST%" mkdir "%DST%" && echo %DST% created
if not exist "%DST%" echo Error: can not find %DST% & goto :eof

if not exist "%BLD%" mkdir "%BLD%" && echo %BLD% created
if not exist "%BLD%" echo Error: can not find %BLD% & goto :eof

rem -- ----------------------------------------------------------------
rem -- Compile the server
rem -- ----------------------------------------------------------------

rem -- The name of the main class

set MCL=ChatServer

echo Compiling %MCL%

echo Javac...

rem -- Run the Java compiler.
rem -- -source level    Compatibiltiy level in source code
rem -- -target level    Compatibility level of generated code
rem -- -d dir           The destination directory for class-files.
rem -- -classpath list  A list of directories and jar-files where javac should
rem --                  look for other classes referred to by the sources.
rem -- -sourcepath dir  Where javac should start looking for sources arranged
rem --                  in a package naming directory tree. Must end with '\'.
rem -- ...              The files to compile.

set LVL=1.4

rem -- javac -source %LVL% -target %LVL% -d "%BLD%" -classpath "%CLP%" -sourcepath "%SRC%" "%SRC%\%PPR%\%MCL%.java"

javac -d "%BLD%" -classpath "%CLP%" -sourcepath "%SRC%" "%SRC%\%PPR%\%MCL%.java"

IF ERRORLEVEL 1 GOTO :eof

echo Rmic...

rem -- Run the RMI compiler. This will create the _stub and _skel
rem -- classes that are necessary for RMI (and thus also Jini).
rem -- -d dir           The destination directory for class-files.
rem -- -classpath list  A list of directories and jar-files where javac should
rem --                  look for other classes referred to by the sources.
rem -- -depend          Consider recompilation of classes that are referred
rem --                  to from other classes, not just the sources.
rem -- -vcompat         Generate stub and skel compatible with 1.1 and 1.2. This
rem --                  was the default prior to 5.0.
rem -- -v1.1            Generate skeleton classes also compatible with JDK 1.1
rem -- -v1.2 (default)  Generate only stub classes
rem -- ...              The classes to compile.


rmic -d "%BLD%" -classpath "%CLP%" -depend -vcompat %PNR%.%MCL%

IF ERRORLEVEL 1 GOTO :eof


set MNF=%ROOT%\mf\%MCL%.manifest

echo Jar...

rem -- Create the jar-files. First the server application itself with
rem -- the manifest inside. The manifest file enables the jar-file to
rem -- run like an application because it identifies the main class to
rem -- the loading JVM (as well as other jar-files needed from the codebase).
rem --
rem -- Syntax: jar options filelist -C dir file(s)
rem -- jar     The Java archiver (quite similar to tar, the unix archiver)
rem -- c       Create a new archive
rem -- u       Update archive (add/replace files in the jar-file)
rem -- m       Include information from file in the manifest (archive info)
rem -- f       The archive is named on the command line
rem -- -C dir  Change directory to dir before reading files
rem -- file(s) File(s) to include in the archive
rem --
rem -- Schematic subsequent invocations: jar uf file1 -C dir file(s)
rem -- f       The archive is named file1

rem -- The main application jar.

jar cmf "%MNF%" "%DST%/%MCL%.jar" -C "%BLD%" "%PPR%"

rem -- The classes needed by a client that wants to talk to the service.

jar cf "%DST%/%MCL%-dl.jar" -C "%BLD%" "%PPR%/%MCL%Interface.class"
jar uf "%DST%/%MCL%-dl.jar" -C "%BLD%" "%PPR%/ChatNotification.class"
jar uf "%DST%/%MCL%-dl.jar" -C "%BLD%" "%PPR%/%MCL%_Stub.class"
jar uf "%DST%/%MCL%-dl.jar" -C "%BLD%" "%PPR%/%MCL%_Skel.class"

goto :eof

rem -- ----------------------------------------------------------------
rem -- Clean out the server's class tree
rem -- ----------------------------------------------------------------

:clean

del /Q "%BLD%\%PPR%\*.class" 2>nul

goto :eof

:cleanall

rmdir /Q /S "%BLD%" 2>nul
rmdir /Q /S "%DST%" 2>nul

goto :eof