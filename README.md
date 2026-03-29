# HeraScan — Yeni Tasarım Dosyaları

## Dosya Konumları

Aşağıdaki dosyaları projenizde belirtilen konumlara kopyalayın:

```
herascan_redesign/
├── ui/
│   ├── theme/
│   │   ├── Color.kt       →  app/src/main/java/com/seninadin/herascan/ui/theme/Color.kt
│   │   ├── Type.kt        →  app/src/main/java/com/seninadin/herascan/ui/theme/Type.kt
│   │   └── Theme.kt       →  app/src/main/java/com/seninadin/herascan/ui/theme/Theme.kt
│   └── components/
│       ├── ActiveScanSection.kt  →  ...herascan/ui/components/ActiveScanSection.kt
│       ├── HistorySection.kt     →  ...herascan/ui/components/HistorySection.kt
│       ├── DialogComponents.kt   →  ...herascan/ui/components/DialogComponents.kt
│       ├── PdfViewer.kt          →  ...herascan/ui/components/PdfViewer.kt
│       └── MainActivity.kt       →  app/src/main/java/com/seninadin/herascan/MainActivity.kt
```

> **Not:** `MainActivity.kt` dosyası `components/` klasöründe değil,
> `com/seninadin/herascan/` kök paketinde yer alır.

---

## Opsiyonel: DM Sans Fontu Ekleme

Tasarım sistemi şu an sistem fontunu (Roboto) kullanmaktadır.
DM Sans fontunu eklemek için:

### 1. app/build.gradle bağımlılığını ekleyin
```kotlin
implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")
```

### 2. Type.kt dosyasındaki yorum satırlarını kaldırın
`Type.kt` içindeki `// DM Sans ile kullanmak için...` bloğunun
yorumunu kaldırın ve `private val AppFont = FontFamily.Default`
satırını `private val AppFont = DmSans` ile değiştirin.

### 3. res/values/font_certs.xml dosyasını ekleyin
Google Fonts sertifikası dosyasını projenize ekleyin.
(Android Studio → File → New → Android Resource File → font_certs.xml)
İçeriği Google'ın resmi dökümantasyonundan alabilirsiniz:
https://developer.android.com/develop/ui/compose/text/fonts#downloadable-fonts

---

## Renk Sistemi Özeti

| Renk          | Açık Tema   | Koyu Tema   | Kullanım                     |
|---------------|-------------|-------------|------------------------------|
| Primary Blue  | `#1558C8`   | `#5B9EFF`   | Ana buton, vurgu, logo       |
| Trust Green   | `#0A7C55`   | `#22D49A`   | Kaydet, PDF banner, onay     |
| OCR Amber     | `#D97706`   | `#FBBB40`   | OCR butonu, metin çıkarma    |
| Background    | `#EEF2F7`   | `#0B1322`   | Ekran arka planı             |
| Surface       | `#FFFFFF`   | `#131F30`   | Kartlar, paneller            |

---

## Değişen Tasarım Kararları

- `dynamicColor = false` → Android 12+ dinamik renkleri marka renklerini ezmez
- `skipPartiallyExpanded = true` → OCR bottom sheet tam açılır
- `HeraScanShapes` → Tutarlı köşe yarıçapları (8/12/16/20 dp)
- `outlineVariant` → Kart kenarlıkları için yeni hafif renk tonu
- `SectionLabel()` → Tüm bölüm başlıkları için paylaşılan composable
