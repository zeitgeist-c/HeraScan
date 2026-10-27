# HeraScan — Android için cihaz üzerinde çalışan belge tarayıcı

> English: [README.md](./README.md)

**Kâğıdı tara, PDF al, geri oku. Hiçbir şey telefondan çıkmaz.**

HeraScan, tek aktiviteli bir Jetpack Compose uygulaması: kamera geçişini veya galeriden seçilen görseli çok sayfalı PDF'e çevirir, metni ML Kit ile çevrimdışı okur ve sonucu yeniden açıp adını değiştirebileceğin veya silebileceğin yerel bir geçmişte tutar.

Her adım — tarama, OCR, geçmiş — cihazda çalışır. Hesap yok, yükleme yok, sunucu yok.

![HeraScan · boş ana ekran](./docs/screenshots/01-ana-ekran.png)

---

## İçindekiler

- [Ne yapar](#ne-yapar)
- [Ekranlar, tek tek](#ekranlar-tek-tek)
  - [1. Ana ekran](#1-ana-ekran)
  - [2. Tarama sonucu](#2-tarama-sonucu)
  - [3. Kırpma ve döndürme](#3-kırpma-ve-döndürme)
  - [4. Belgeyi adlandır](#4-belgeyi-adlandır)
  - [5. Geçmiş](#5-geçmiş)
  - [6. PDF görüntüleyici](#6-pdf-görüntüleyici)
- [Kurulum](#kurulum)
- [İzinler](#izinler)
- [Dosyalar nereye gider](#dosyalar-nereye-gider)
- [Kaynaktan derleme](#kaynaktan-derleme)
- [Bilinen sınırlar](#bilinen-sınırlar)
- [Lisans](#lisans)

---

## Ne yapar

| Yetenek | Ayrıntı |
|---|---|
| **Belge tarayıcı** | ML Kit Document Scanner, FULL modu, galeriden içe aktarma, tarama başına 50 sayfa sınırı |
| **Çıktılar** | sayfa başına JPEG ve birleşik PDF |
| **Çevrimdışı OCR** | cihaz üzerinde ML Kit Text Recognition (Latin), sonuç alt sayfada |
| **PDF görüntüleyici** | taranan PDF içinde uygulama içi sayfalama |
| **Kaydet ve paylaş** | PDF Downloads'a, OCR metni `Documents/HeraScan` altına, FileProvider ile paylaşım |
| **Geçmiş** | Room veritabanı + Paging 3, sayfa boyu 20; aç, yeniden adlandır, sil |
| **Akıllı adlandırma** | varsayılan ad tarih önekli, Türkçe biçimde |
| **Önbellek temizliği** | 24 saatten eski tarama önbelleği kendiliğinden silinir |
| **Türkçe arayüz** | `tr-TR` metinler ve tarih biçimi |
| **Material 3** | Compose, küçük resimler için Coil, sabit görünüm için dinamik renk kapalı |

---

## Ekranlar, tek tek

### 1. Ana ekran

![Ana ekran](./docs/screenshots/01-ana-ekran.png)

Boş durum ve uygulamanın belge yüklü olmayan tek ekranı.

- **Başlık** — logo, `HeraScan` ve çalışan sürüm (`v0.5.8`).
- **Boş illüstrasyon** — *"No Documents Scanned Yet"*, altında ipucu: *"Take a photo or pick from gallery · Up to 50 pages"*.
- **New Scan** — sağ alttaki mavi eylem düğmesi.

İllüstrasyona veya **New Scan**'e dokunmak ML Kit tarayıcısına devreder. Öğrenilecek bir uygulama içi kamera önizlemesi yok: yakalama, kırpma ve filtreleme sistem arayüzünde yapılır.

### 2. Tarama sonucu

![Tarama sonucu](./docs/screenshots/02-sonuc.png)

Tarama kaydedildiğinde sonuç kartı ekranın üstüne gelir.

- **CURRENT SCAN** — belge adı, sayfa sayısı ve sayfa küçük resimleri.
- **PDF bloğu** — üretilen dosya (`Document 27 October 2026 03:10.pdf`), sayfa sayısı ve **View PDF**.
- **ACTIONS** — dört yol, her birinin altında hedefi yazılı:
  - **Share PDF** → *Choose app*
  - **Save PDF** → *Downloads*
  - **Share Image** → *First page*
  - **View PDF** → *In-app*
- **Read All Pages** — *Extract text with OCR · AI-powered*, bütün sayfalar için çevrimdışı OCR geçişi.

Her eylem nereye gittiğini kendi altında yazar; dokunmadan önce tahmin etmek gerekmez.

### 3. Kırpma ve döndürme

![Kırpma ve döndürme](./docs/screenshots/03-kirpma.png)

Sayfalar kesinleşmeden önceki ML Kit inceleme adımı.

- **Filters** — sayfaya uygulanan renk ve kontrast ön ayarları.
- **Crop & rotate** — sayfa başına köşe ayarı ve döndürme.
- **Sayfa şeridi** — `Page 1 of N`, yeni sayfa ekle veya bir sayfayı sil.
- **Discard scans** — bütün partiyi atar.
- **Next** — partiyi kabul eder ve adlandırmaya geçer.

Yakalama bilerek ML Kit'e bırakılmıştır: kenar algılama ve perspektif düzeltme onun işi, uygulama bunları yeniden yazmaz.

### 4. Belgeyi adlandır

![Belgeyi adlandır](./docs/screenshots/04-isimlendir.png)

Taramayı kapatan adlandırma diyaloğu.

- **Başlık** — *Name Your Document*.
- **İpucu** — *"Give it a meaningful name to find it easily later"*.
- **Varsayılan** — tarih önekli, Türkçe biçimde bir ad (`Document 27 October 2026 03:06`), seçili gelir; yazmaya başlayınca değişir.
- **Cancel** / **Save**.

Ad hem PDF dosya adı hem geçmiş kaydı olur; bu yüzden varsayılan rastgele bir kimlik değil, okunabilir bir addır.

### 5. Geçmiş

![Geçmiş](./docs/screenshots/05-gecmis.png)

Belgeler oluştuktan sonraki ana ekran.

- **SCAN HISTORY** — sayfalanmış ızgara, en yenisi önce, sayfa boyu 20.
- Her karo ilk sayfanın küçük resmini, belge adını ve tarihini (`27 Oct 2026`) gösterir.
- **Uzun basma veya silme öğesi** onaydan sonra kaydı kaldırır.

Geçmiş bir Room tablosu ve Paging 3'tür; uzun bir arşiv bile kaydırırken ucuz kalır, ilk ekranı çizmek için bütün satırları yüklemez.

### 6. PDF görüntüleyici

![PDF görüntüleyici](./docs/screenshots/06-pdf-goruntuleyici.png)

Sonuç kartındaki **View PDF** veya bir geçmiş kaydından açılan uygulama içi okuyucu.

- **Sayfa göstergesi** — `Page 1 / 2`.
- **Dikey sayfalama** — ekran başına bir sayfa, aralarında kaydırma.
- **Düzenleme yok** — görüntüleyici okur; belgeyi değiştirmek için yeniden taranır veya silinir.

Paylaşmadan önce PDF'in okunabildiğini kontrol etmenin en hızlı yolu buradan açmaktır.

---

## Kurulum

HeraScan APK olarak dağıtılır:

1. `HeraScan-0.5.8.apk` dosyasını cihaza kopyala.
2. İstenirse o kaynaktan kuruluma izin ver.
3. Uygulamayı aç, ilk taramada kamera iznini ver.

Android 8.0 (API 26) veya üzeri; Android SDK 34 ve Android 15 üzerinde derlenip test edildi.

## İzinler

| İzin | Neden |
|---|---|
| **Kamera** | ML Kit tarayıcı arayüzü üzerinden sayfa yakalama |
| **Depolama / MediaStore** | PDF'i Downloads'a, OCR metnini `Documents/HeraScan` altına yazma |
| **İnternet** | yalnız ML Kit modelinin bir kerelik indirilmesi; sonrasında tarama ve OCR çevrimdışı |

Hesap yok, analitik yok, çökme raporu yok.

## Dosyalar nereye gider

| Ürün | Konum |
|---|---|
| PDF | Downloads (MediaStore üzerinden) |
| OCR metni | `Documents/HeraScan` |
| Geçmiş ve küçük resimler | uygulama içi Room veritabanı |
| Tarama önbelleği | uygulama önbelleği, 24 saat sonra temizlenir |

Geçmiş kaydını silmek uygulamanın kaydını kaldırır; Downloads veya Documents'a yazılmış dosyalar yerinde kalır.

## Kaynaktan derleme

Gereksinimler: JDK 17, Android SDK 34, depodaki Gradle wrapper.

```powershell
./gradlew test lint assembleDebug
```

Sürüm APK'sı:

```powershell
./gradlew assembleRelease
```

İmza yapılandırması `keystore.properties` dosyasından okunur; bu dosya git tarafından yok sayılır ve asla commitlenmez. Dosya yoksa sürüm derlemesi imzasız APK üretir.

## Bilinen sınırlar

- **Paket kimliği `com.seninadin.herascan`.** Cihazdaki uygulama kimliğidir: değiştirmek her kurulu sürümü ayrı bir uygulama yapar ve güncellemeleri keser. Kaynak depo adıyla ilgisi yoktur.
- **OCR yalnız Latin alfabesi.** Başka yazı sistemleri farklı bir ML Kit tanıyıcısı ister.
- **Uygulama içi kamera yok.** Tarama Google'ın ML Kit tarayıcı arayüzünden geçer; Google Play hizmetleri olmayan cihaz tarayamaz.
- **Bulut yedeği yok.** Geçmiş uygulama depolamasında yaşar; kaldırma siler.
- **Toplu dışa aktarma yok.** Belgeler tek tek paylaşılır.

## Lisans

MIT — bkz. [LICENSE](./LICENSE).
