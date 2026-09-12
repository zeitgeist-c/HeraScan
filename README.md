# HeraScan — On-device document scanner for Android

> Türkçe için: [README.tr.md](./README.tr.md)

Single-activity Jetpack Compose app: scan to JPEG/PDF, view PDFs, run offline OCR, and keep a local scan history. Everything on-device — ML Kit, Room, MediaStore.

![Empty home screen (emulator)](./docs/screenshots/01-ana-ekran.png)

## Features

- **Document scanner** — ML Kit Document Scanner, FULL mode, gallery import, 50-page limit, JPEG + PDF output.
- **Offline OCR** — on-device ML Kit Text Recognition (Latin); result in a bottom sheet.
- **PDF viewer** — in-app viewing screen.
- **Save + share** — save PDF to device and OCR text to `Documents/HeraScan` (MediaStore); share PDF/JPG via FileProvider chooser.
- **Scan history** — Room database + Paging 3 (page 20); reopen and delete from history.
- **Smart naming** — rename dialog with date-prefixed default names (TR format).
- **Material3 UI** — Coil image loading, dynamic color off.
- **Hygiene** — cache older than 24h cleaned automatically.
- **Turkish localization** — `tr-TR` strings and dates.
- **Tests** — JUnit + Espresso + Compose UI tests.

## Quick start

Requirements: Android Studio + JDK 17 + Android SDK 34.

- Android Studio: open project → sync → Run.
- CLI: `.\gradlew.bat test lint assembleDebug` → APK at `app/build/outputs/apk/debug/app-debug.apk`.
- Release: `vX.Y.Z` tag → GitHub Actions builds and publishes `HeraScan-X.Y.Z.apk` (see `.github/workflows/release.yml`).

App: `com.seninadin.herascan`, single `:app` module, minSdk 24, target/compileSdk 34.

## Project structure

```text
app/src/main/java/com/seninadin/herascan/
  MainActivity.kt     app shell (scanner, FAB, top bar, bottom sheet, dialog, viewer)
  MainViewModel.kt    scan state, OCR, history jobs
  data/               ScanRepository (Paging 20)
  db/                 Room database, ScanHistory, DAO
  ui/components/      scan section, history, PDF viewer, dialogs
  ui/theme/           Material3 theme
  utils/FileUtils.kt  cleanup, share, savePdf, saveText
```

## Contributing

Small PRs, `./gradlew test lint` green before push. See [.github/PULL_REQUEST_TEMPLATE.md](./.github/PULL_REQUEST_TEMPLATE.md).

## License

See [LICENSE](./LICENSE) (pending — to be confirmed).
