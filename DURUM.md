# HeraScan — DURUM (ortak — Desktop + Telegram okur)

**Güncelleme:** 2026-09-30
**Versiyon:** v0.5.8 (son tag; `versionCode = 9`)
**Branch:** master
**Ağaç:** temiz (hedef)
**Not:** v0.5.8 commit'i önceki WIP'i de içerir (AGP 8.13.2 + Gradle 8.13 geçişi diğer makineden + bu turdaki 21-skill düzeltmeleri).

## Son tur (2026-09-30, 21 skill)
- Denetim (salt-okunur, 3 paralel grup): 0 kritik, 12 orta, ~25 küçük bulgu. debug atlandı (bilinen bug yok).
- Düzeltme: OCR ortak çekirdek + iptal/stale guard, kaydet çift-dokunuş + boş-ad guard, URI kalıcılığı (private kopya), sabit kullanıcı mesajları, PdfRenderer try/finally, Logger, DateFormats, MediaStore ortak kalıp, paylaşım boyut/UUID guard, Coil/paylaşım yardımcıları, shape token birliği, 12 yeni string (TR+EN), 6 yeni test.
- Kapılar (DriveFS kilidi yüzünden repo dışı kopyada): test 10/10, lintDebug temiz, assembleDebug + assembleRelease başarılı.
- Ertelenen (gerekçeli): BOM 2023.08→2024.10+ (Kotlin 2.x ister), targetSdk 34→35 (planlı), destructive-migration kaldırma (Migration + test ister), OcrRepository taşıma (davranış riski; dedup yapıldı), StoragePermission config-change (yapısal; dokümante).

## Sırada
- Kullanıcı söyler.

## Bağımlılık
- AGP 8.13.2 + Gradle 8.13 (v0.5.8'e dahil). Bekleyen ayrı işler: Kotlin 1.9→2.x, compose BOM 2023.08, targetSdk 34→35/36.
