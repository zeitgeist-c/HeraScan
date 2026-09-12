# HeraScan — Android için cihaz-içi belge tarayıcı

> English: [README.md](./README.md)

Tek aktiviteli Jetpack Compose uygulaması: JPEG/PDF tarama, PDF görüntüleme, çevrimdışı OCR ve yerel tarama geçmişi. Her şey cihazda — ML Kit, Room, MediaStore.

![Boş ana ekran (emulator)](./docs/screenshots/01-ana-ekran.png)

## Özellikler

- **Belge tarayıcı** — ML Kit Document Scanner, FULL mod, galeriden içe aktarma, 50 sayfa limit, JPEG + PDF çıktı.
- **Çevrimdışı OCR** — cihazda ML Kit metin tanıma (Latin); sonuç alt sayfada.
- **PDF görüntüleyici** — uygulama içi görüntüleme ekranı.
- **Kaydet + paylaş** — PDF ve OCR metni `Documents/HeraScan` (MediaStore); FileProvider seçimli paylaşım.
- **Tarama geçmişi** — Room + Paging 3 (sayfa 20); geçmişten açma/silme.
- **Akıllı adlandırma** — tarih önekli varsayılan adlı yeniden adlandırma (TR format).
- **Material3 arayüz** — Coil görsel yükleme, dinamik renk kapalı.
- **Temizlik** — 24 saatten eski önbellek otomatik silinir.
- **Türkçe yerelleştirme** — `tr-TR` metin ve tarihler.
- **Testler** — JUnit + Espresso + Compose UI testleri.

## Hızlı başlangıç

Gereksinim: Android Studio + JDK 17 + Android SDK 34.

- Android Studio: projeyi aç → sync → Run.
- CLI: `.\gradlew.bat test lint assembleDebug` → APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Sürüm: `vX.Y.Z` etiketi → GitHub Actions `HeraScan-X.Y.Z.apk` yayımlar (bkz. `.github/workflows/release.yml`).

Uygulama: `com.seninadin.herascan`, tek `:app` modülü, minSdk 24, target/compileSdk 34.

## Proje yapısı

```text
app/src/main/java/com/seninadin/herascan/
  MainActivity.kt     uygulama kabuğu (tarayıcı, FAB, üst bar, alt sayfa, dialog, viewer)
  MainViewModel.kt    tarama durumu, OCR, geçmiş işleri
  data/               ScanRepository (Paging 20)
  db/                 Room veritabanı, ScanHistory, DAO
  ui/components/      tarama bölümü, geçmiş, PDF viewer, dialoglar
  ui/theme/           Material3 tema
  utils/FileUtils.kt  temizlik, paylaşım, savePdf, saveText
```

## Katkı

Küçük PR, push öncesi `./gradlew test lint` yeşil. Bkz. [.github/PULL_REQUEST_TEMPLATE.md](./.github/PULL_REQUEST_TEMPLATE.md).

## Lisans

Bkz. [LICENSE](./LICENSE) (bekliyor — teyit edilecek).
