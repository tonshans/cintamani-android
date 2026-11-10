
# Cintamani Android: The Capture Node

*Menangkap pemikiran secepat kilat, sebelum mereka menguap.*

---

## Apa itu Cintamani Android?
**Cintamani Android** adalah aplikasi *penangkap pemikiran* (The Capture Node) yang dirancang secara radikal untuk membuang segala gesekan (*friction*) saat mencatat. Aplikasi ini adalah pintu masuk menuju ekosistem [Cintamani Server](https://github.com/tonshans/cintamani), sebuah *Personal AI Knowledge Graph* yang hidup di mesin lokal Anda.

Pernahkah Anda mendapatkan ide brilian saat sedang di jalan, namun ide itu hilang karena Anda malas membuka aplikasi pencatat yang berat, bingung memikirkan judul, atau sibuk mencari folder yang tepat? 

Cintamani Android memecahkan masalah tersebut. Layar utama mutlak hanya berisi satu kotak teks dan sebuah tombol simpan. **Capture First, Organize Later.** 

---

## Fitur Utama: Cepat, Jujur, & Tanpa Beban

- **Zero-Friction Cognitive Offloading:** Buka aplikasi, ketik atau bicara, tekan Simpan. Tidak ada form judul, tidak ada pemilihan folder, tidak ada penamaan *tag*. Waktu dari pikiran muncul hingga tersimpan kurang dari 3 detik.
- **Voice-to-Text Instan:** Tangan sedang sibuk? Tekan tombol *Voice Note*, bicarakan ide Anda, dan aplikasi akan mengubahnya menjadi teks secara otomatis.
- **Silent Metadata Enrichment (Sensor Senyap):** Tanpa Anda sadari, setiap kali Anda mencatat, aplikasi ini menyematkan metadata penting:
  - *Kecepatan Ketik (WPM)* & *Jumlah Backspace* (mendeteksi *flow* atau keraguan).
  - *Dinamika Vokal* (mendeteksi intonasi dan kecepatan bicara saat Anda menggunakan suara).
  - *Geolokasi & Waktu Presisi* (membantu AI di server memahami konteks di mana dan kapan ide tersebut muncul).
- **The Safe Space (Anti-Sensor Diri):** Tidak ada tombol "Share to X/Twitter". Karena data ini 100% rahasia, Anda bebas menuliskan ide paling mentah, pertanyaan bodoh, atau keluhan terdalam tanpa takut dihakimi.
- **Offline-First & Zero Loading:** Terjebak di area tanpa sinyal? Tidak masalah. Catatan langsung masuk ke *database* SQLite lokal Anda tanpa jeda *loading* sedikit pun.

---

## Hubungan dengan Cintamani Server (Mesin Intelijen)
Cintamani Android adalah "sensor" perpanjangan tangan dari **Cintamani Server** (repositori utama). 

Aplikasi Android ini **tidak** melakukan analisis AI apa pun (agar aplikasi tetap sangat ringan dan cepat). Setelah catatan terkumpul di ponsel, Anda menghubungkan aplikasi ini ke jaringan WiFi rumah (atau via VPN Tailscale), lalu menekan tombol **SYNC TO BRAIN NOW**. 

Di Server, ribuan catatan (*micro-notes*) Anda akan dianalisis oleh **8 Mesin Kognitif AI Lokal**. Server akan:
- Menemukan hubungan tersembunyi antara ide Anda yang ditulis hari ini dengan keluhan Anda 6 bulan lalu (*Serendipity Engine*).
- Mencari celah asumsi dan titik buta dari rencana Anda (*Blindspot Detector*).
- Mengubah kumpulan renungan menjadi daftar tugas nyata dan draf proyek matang.

*Android menangkap data mentah, Server mengekstrak kebijaksanaannya.*

---

## Antarmuka: Cognitive Neo-Brutalism
Aplikasi ini tidak dirancang dengan desain *material* biasa yang membosankan, melainkan menggunakan gaya **Cognitive Neo-Brutalism**. Dengan garis batas tebal, bayangan padat, dan sudut tajam, aplikasi ini terasa solid dan utilitarian—seperti memegang alat pertukangan profesional.

Anda dapat memilih dari 4 palet tema yang ramah mata dari laci pengaturan:
1. **The Architect's Draft (Light)**
2. **Cognitive Terminal (Dark - Default)**
3. **Industrial Archive (Light)**
4. **Midnight Blueprint (Dark)**

---

## ⚙Panduan Penggunaan & Instalasi (Getting Started)

### 1. Prasyarat & Instalasi
1. Pastikan Anda sudah menjalankan **Cintamani Server** dan mendapatkan `Server Base URL` serta `API Token`.
2. *Clone* repositori ini:
   ```bash
   git clone [https://github.com/tonshans/cintamani-android.git](https://github.com/tonshans/cintamani-android.git)

```

3. Buka folder proyek menggunakan **Android Studio**.
4. Lakukan *sync* Gradle dan klik **Run** untuk menginstal aplikasi ke ponsel Android Anda. (Minimal Android 8.0 / API 26).



### 2. Konfigurasi Awal di Ponsel

1. Buka aplikasi Cintamani di ponsel Anda. Anda akan langsung melihat kotak teks besar.
2. Usap layar dari kiri ke kanan (atau ketuk ikon pengaturan) untuk membuka laci samping (*Settings Drawer*).


3. Masukkan **Server Base URL** (contoh: `http://192.168.1.50:8000`).
4. Masukkan **API Token** rahasia yang Anda dapatkan dari instalasi Cintamani Server.


5. Ketuk **SAVE CONFIG**.
6. (Opsional) Ketuk **TEST CONNECTION** untuk memastikan ponsel dan server sudah bisa saling menyapa.



### 3. Alur Penggunaan Sehari-hari

* **Mencatat:** Cukup buka aplikasi, ketik ide Anda, lalu tekan **CAPTURE**. Lakukan ini berkali-kali sepanjang hari.


* **Menyinkronkan:** Saat Anda siap "mengosongkan pikiran", buka laci pengaturan, dan tekan **SYNC TO BRAIN NOW**. Seluruh catatan Anda akan terkirim dengan aman ke server rumah Anda.


* **Membersihkan:** Setelah sinkronisasi berhasil, catatan lokal aman dihapus. Tekan **CLEAR SYNCED NOTES** untuk mengosongkan memori ponsel Anda (catatan aslinya kini sudah aman dianalisis di server).



> *Mulailah menangkap ide secepat kilat. Biarkan AI yang memikirkan sisanya.*

