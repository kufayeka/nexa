# Nexa Framework 🚀

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://jdk.java.net/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Domain--Driven-blue.svg)]()
[![DI](https://img.shields.io/badge/Dependency%20Injection-Pure%20DI-red.svg)]()

Nexa adalah **industrial automation low-code platform** modern berperforma tinggi yang dibangun di atas Java. Nexa dirancang untuk menduplikasi perilaku eksekusi visual flow ala **Node-RED**, tetapi diimplementasikan sebagai runtime Java tingkat produksi (*production‑grade*) yang tangguh, modular, mudah dipelihara, dan hemat memori.

---

## 🌟 Fitur Utama

* **Concurrency Ringan (Virtual Threads)**: Setiap jalur aliran data downstream dieksekusi secara asinkronus dan paralel menggunakan Java Virtual Threads, memungkinkan penanganan jutaan transaksi pesan tanpa membebani thread carrier OS.
* **Pure Dependency Injection (Pure DI)**: Mengeliminasi overhead pemindaian classpath dan startup lag dengan perakitan dependensi manual bertipe aman (*compile‑time checked*) melalui pola *Composition Root*.
* **Domain‑Driven Design (DDD)**: Struktur paket diorganisasikan secara ketat berdasarkan domain fungsional bisnis (Workspace, Deployment, Execution, Scheduler, Statistics, Scripting) demi mencegah kode spaghetti.
* **Isolasi State Pesan (Deep Copy)**: Transformasi data yang terjadi di cabang rute yang berbeda terisolasi secara aman menggunakan algoritma deep‑copy berbasis *Switch Pattern Matching* Java 25.
* **Built‑in Nexa DSL Engine**: Memiliki compiler dan runtime terintegrasi untuk mengeksekusi skrip transformasi data performa tinggi dengan fitur null‑safety (`?.`, `??`) dan integrasi Java host extensions.
* **Lock‑free Statistics**: Pencatatan metrik operasional secara konkuren menggunakan `LongAdder` untuk meminimalkan contention antar thread worker.

---

## 📂 Struktur Proyek (Setelah Refactor Core & nexa‑api)

```
nexa-framework/
├── .agents/                 # Panduan kustomisasi aturan agen AI
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/nexa/framework/
│   │   │   │   ├── App.java                   # Entrypoint aplikasi demo
│   │   │   │   └── runtime/
│   │   │   │       ├── api/                   # Antarmuka publik (exposed ke plugin lain)
│   │   │   │       └── domain/                # Modul fungsional (DDD)
│   │   │   │           ├── workspace/         # Manajemen JSON & model definition
│   │   │   │           ├── deployment/        # Validasi topologi & compiler graf
│   │   │   │           ├── execution/         # Orkestrasi pesan & virtual threads
│   │   │   │           ├── scheduler/         # Penjadwal pemicu input node
│   │   │   │           ├── statistics/        # Akumulator metrik flow
│   │   │   │           └── scripting/         # Compiler & engine Nexa DSL
│   │   │   └── resources/                     # Service Loader configurations
│   │   └── test/                              # Pengujian unit & stress test
├── nexa-api/                # Modul terpisah yang hanya berisi antarmuka (`api/`)
│   └── src/main/java/nexa/framework/api/   # Contoh: RuntimeEngine, RuntimeConfiguration, Plugin
└── README-NEXA-DSL.md       # Spesifikasi bahasa pemrograman Nexa DSL
```

**Perubahan penting**:
- Semua antarmuka publik dipindahkan ke modul **`nexa-api`** sehingga plugin eksternal dapat mengambilnya tanpa tergantung pada implementasi.
- Implementasi layanan (`service/`) kini *package‑private* dan diregistrasi melalui **ServiceLoader** di `META-INF/services/`.
- **Composition Root** (`DefaultRuntimeEngine`) berada di paket `execution.service` dan menerima dependensi via constructor injection.
- Penambahan modul `nexa-api` memungkinkan **plugin eksternal** dibangun sebagai proyek terpisah yang hanya meng‑import API.

---

## 📖 Dokumentasi Lanjutan

Untuk informasi detail tentang API runtime, lihat modul **`nexa-api`** di repository ini atau baca file Javadoc pada paket `nexa.framework.runtime.api`.

---

## 📜 Lisensi

Proyek ini dilisensikan di bawah lisensi internal Kufayeka Industrial Automation.

## 🛠️ Quick Start

### 1. Prasyarat
* Java Development Kit (JDK) 25 atau lebih baru.
* Gradle build tool.

### 2. Membangun Proyek
```powershell
./gradlew.bat build
```

### 3. Menjalankan Unit & Stress Test
```powershell
./gradlew.bat test
```

### 4. Contoh Inisialisasi Runtime di Java

```java
import nexa.framework.runtime.api.*;
import nexa.framework.runtime.domain.execution.service.DefaultRuntimeEngine;
import nexa.framework.runtime.domain.workspace.service.WorkspaceJsonLoader;
import nexa.framework.runtime.domain.workspace.model.WorkspaceDefinition;

import java.time.Duration;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // 1. Definisikan Output Consumer untuk menangani hasil eksekusi node paling ujung (OUTPUT)
        OutputConsumer consumer = (context, nodeId, message) -> {
            System.out.println("Output dari Node [" + nodeId + "]: " + message.values());
        };

        // 2. Buat instance engine runtime utama (Composition Root)
        RuntimeEngine engine = new DefaultRuntimeEngine(
                new RuntimeConfiguration(Duration.ofSeconds(10)), // Timeout eksekusi 10s
                consumer
        );

        // 3. Muat file JSON workspace definisi
        WorkspaceJsonLoader loader = new WorkspaceJsonLoader();
        WorkspaceDefinition workspaceDef = loader.fromFile(Paths.get("workspace.json"));

        // 4. Deploy dan nyalakan runtime
        engine.deploy(workspaceDef);
        engine.startRuntime();
    }
}
```

---

## 📖 Dokumentasi Lanjutan

Untuk mempelajari cara menulis skrip logika transformasi pada executor node di Nexa, baca spesifikasi lengkap dan use case di:
👉 **[Spesifikasi Lengkap Nexa DSL V1](README-NEXA-DSL.md)**

Untuk membuat cara membuat plugin NEXA Framework kunjungi:

👉 **[Panduan Membuat Plugin Nexa Framework](README-NEXA-PLUGIN.md)**

---

## 📜 Lisensi
Proyek ini dilisensikan di bawah lisensi internal Kufayeka Industrial Automation.
