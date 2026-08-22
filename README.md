# HeraScan

HeraScan, Android için belge tarama, PDF görüntüleme ve cihaz üzerinde OCR özellikleri sunan bir uygulamadır.

## Gereksinimler

- Android Studio
- JDK 17
- Android SDK 34

## Derleme ve test

```powershell
.\gradlew.bat test lint assembleDebug
```

Üretilen APK `app/build/outputs/apk/debug/app-debug.apk` konumundadır.

## Sürümleme

Uygulamanın sürümü `app/build.gradle.kts` içindeki `versionCode` ve `versionName` alanlarında tutulur. `vX.Y.Z` biçimindeki bir Git etiketi GitHub'a gönderildiğinde GitHub Actions testleri çalıştırır, APK'yı derler ve `HeraScan-X.Y.Z.apk` adıyla bir GitHub Release yayımlar.
