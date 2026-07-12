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

## 🛠️ Panduan Membuat Plugin Nexa

### 1. Struktur Direktori Plugin

Buat modul Maven/Gradle baru dengan struktur berikut (sesuai SOP):

```
my-mqtt-plugin/
├── src/main/java/com/example/mqttplugin/
│   ├── api/                     # Antarmuka yang diekspos ke runtime
│   │   └── MqttPlugin.java      # Interface publik plugin
│   ├── controller/              # Entry point yang meng‑implementasikan antarmuka
│   │   └── MqttPluginController.java
│   ├── service/                 # Logika bisnis (package‑private)
│   │   └── MqttPluginService.java
│   ├── model/                   # Record DTO (immutable)
│   │   └── MqttConfig.java
│   ├── helpers/                 # Utilitas spesifik plugin
│   │   └── MqttClientPoolManager.java
│   └── exception/               # Exception khusus plugin
│       └── MqttPluginException.java
└── src/main/resources/META-INF/services/
    └── nexa.framework.runtime.api.plugin.Plugin   # File berisi nama kelas controller
```

### 2. Definisikan Antarmuka Plugin (`api/MqttPlugin.java`)

```java
package com.example.mqttplugin.api;

/**
 * Antarmuka publik yang harus di‑implementasikan plugin MQTT.
 * Menggunakan Java 25 record untuk konfigurasi yang immutable.
 */
public interface MqttPlugin {
    /**
     * Inisialisasi sumber daya (mis. koneksi pool) sebelum flow dijalankan.
     */
    void init();

    /**
     * Dapatkan node‑node yang akan diregister ke runtime.
     * @return daftar node yang disediakan plugin.
     */
    java.util.List<nexa.framework.runtime.api.node.Node> nodes();
}
```

### 3. Implementasi Controller (`controller/MqttPluginController.java`)

```java
package com.example.mqttplugin.controller;

import com.example.mqttplugin.api.MqttPlugin;
import nexa.framework.runtime.api.plugin.Plugin; // antarmuka runtime plugin
import nexa.framework.runtime.api.node.Node;
import java.util.List;

/**
 * Controller plugin yang di‑load oleh ServiceLoader.
 * Package‑private karena hanya diakses melalui ServiceLoader.
 */
final class MqttPluginController implements Plugin, MqttPlugin {
    private final com.example.mqttplugin.helpers.MqttClientPoolManager poolManager;

    MqttPluginController() {
        // Konstruktor injection manual
        this.poolManager = new com.example.mqttplugin.helpers.MqttClientPoolManager();
    }

    @Override
    public void init() {
        // Inisialisasi pool client MQTT (shared untuk inbound & outbound)
        poolManager.start();
    }

    @Override
    public List<Node> nodes() {
        // Membuat node inbound & outbound yang memakai pool yang sama
        return List.of(
            new com.example.mqttplugin.service.MqttInNode(poolManager),
            new com.example.mqttplugin.service.MqttOutNode(poolManager)
        );
    }
}
```

### 4. Registrasi dengan ServiceLoader

Buat file **`META-INF/services/nexa.framework.runtime.api.plugin.Plugin`** yang berisi satu baris:

```
com.example.mqttplugin.controller.MqttPluginController
```

### 5. Memuat Plugin di Runtime

Runtime engine secara otomatis mencari semua implementasi `Plugin` lewat **`ServiceLoader.load(Plugin.class)`**. Pada saat `engine.startRuntime()` dipanggil, engine akan:
1. Memanggil `init()` pada setiap plugin.
2. Mendaftarkan node‑node yang dikembalikan oleh `nodes()` ke graf flow.

Tidak diperlukan kode tambahan di aplikasi utama; cukup menambahkan JAR plugin ke classpath.

---

## 📡 Use‑Case: MQTT Client dengan Shared Client Pool Manager

### 5.1. `MqttClientPoolManager`

```java
package com.example.mqttplugin.helpers;

import java.util.concurrent.ArrayBlockingQueue;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 * Manager pool client MQTT yang dapat dipakai bersama oleh node inbound dan outbound.
 * Menggunakan pola pool sederhana (ArrayBlockingQueue) untuk meng‑reuse koneksi.
 */
public final class MqttClientPoolManager {
    private final ArrayBlockingQueue<MqttClient> pool = new ArrayBlockingQueue<>(8);

    public void start() {
        // Membuat 4 client dan menambahkannya ke pool
        for (int i = 0; i < 4; i++) {
            try {
                MqttClient client = new MqttClient("tcp://broker:1883", "client" + i);
                client.connect();
                pool.offer(client);
            } catch (Exception e) {
                throw new RuntimeException("Gagal membuat client MQTT", e);
            }
        }
    }

    public MqttClient acquire() throws InterruptedException {
        return pool.take();
    }

    public void release(MqttClient client) {
        pool.offer(client);
    }
}
```

### 5.2. Node Inbound (`MqttInNode`)

```java
package com.example.mqttplugin.service;

import nexa.framework.runtime.api.node.Node;
import nexa.framework.runtime.api.context.ExecutionContext;
import com.example.mqttplugin.helpers.MqttClientPoolManager;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Node yang menerima pesan dari topik MQTT dan meneruskan ke flow.
 */
final class MqttInNode implements Node {
    private final MqttClientPoolManager pool;
    private final String topic = "sensor/data";

    MqttInNode(MqttClientPoolManager pool) {
        this.pool = pool;
    }

    @Override
    public void process(ExecutionContext ctx, String input) {
        try (var client = pool.acquire()) {
            client.subscribe(topic, (t, msg) -> {
                // Convert MQTTMessage ke message internal Nexa
                var message = new nexa.framework.runtime.api.message.Message(msg.getPayload());
                ctx.emit(message);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 5.3. Node Outbound (`MqttOutNode`)

```java
package com.example.mqttplugin.service;

import nexa.framework.runtime.api.node.Node;
import nexa.framework.runtime.api.context.ExecutionContext;
import com.example.mqttplugin.helpers.MqttClientPoolManager;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Node yang meng‑publish hasil ke topik MQTT.
 */
final class MqttOutNode implements Node {
    private final MqttClientPoolManager pool;
    private final String topic = "sensor/processed";

    MqttOutNode(MqttClientPoolManager pool) {
        this.pool = pool;
    }

    @Override
    public void process(ExecutionContext ctx, String input) {
        try (var client = pool.acquire()) {
            var payload = input.getBytes();
            client.publish(topic, new MqttMessage(payload));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 🗂️ Contoh Workspace JSON untuk Plugin MQTT

```json
{
  "id": "workspace-mqtt-demo",
  "enabled": true,
  "resources": [
    {
      "id": "mqtt-pool",
      "type": "mqtt-client-pool",
      "config": {
        "brokerUrl": "tcp://broker:1883",
        "poolSize": 4
      }
    }
  ],
  "flows": [
    {
      "id": "mqtt-flow",
      "nodes": [
        {
          "id": "mqtt-in",
          "type": "com.example.mqttplugin.service.MqttInNode",
          "config": { "topic": "sensor/data" }
        },
        {
          "id": "process",
          "type": "nexa.framework.runtime.domain.scripting.service.ScriptNode",
          "script": "payload -> payload.toUpperCase()"
        },
        {
          "id": "mqtt-out",
          "type": "com.example.mqttplugin.service.MqttOutNode",
          "config": { "topic": "sensor/processed" }
        }
      ],
      "connections": [
        { "from": "mqtt-in", "to": "process" },
        { "from": "process", "to": "mqtt-out" }
      ]
    }
  ]
}
```

Workspace di atas menunjukkan:
- **Resource** `mqtt-pool` didefinisikan sekali dan akan dipakai bersama oleh node inbound & outbound.
- **Flow** berisi tiga node: `mqtt-in` → `process` → `mqtt-out`.
- Node `process` menggunakan **Nexa DSL ScriptNode** untuk transformasi sederhana.

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

---

## 📜 Lisensi
Proyek ini dilisensikan di bawah lisensi internal Kufayeka Industrial Automation.
