@echo off
set JBR_HOME=C:\Program Files\Android\Android Studio\jbr
set WRAPPER_JAR=%~dp0\gradle\wrapper\gradle-wrapper.jar
"%JBR_HOME%\bin\java" -cp "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
