# Nexa Framework Runtime

Nexa Framework adalah runtime workflow event-driven untuk industrial automation, dibangun dengan Java 25.

Tujuan utama proyek ini adalah eksekusi workflow yang:

- Stabil untuk operasi 24/7
- Mudah dipahami engineer baru
- Mudah di-debug saat incident
- Aman untuk eksekusi paralel

## Struktur Dokumentasi

- Runtime architecture: [app/src/main/java/nexa/framework/runtime/README.md](app/src/main/java/nexa/framework/runtime/README.md)
- Runtime API contract: [app/src/main/java/nexa/framework/runtime/api/README.md](app/src/main/java/nexa/framework/runtime/api/README.md)
- Workflow JSON guide: [app/src/main/resources/workspaces/README.md](app/src/main/resources/workspaces/README.md)

## Alur Utama Runtime

1. Workspace JSON di-load.
2. Workspace divalidasi dan di-compile menjadi graph executable.
3. Runtime di-start.
4. Semua InputNode diaktivasi berdasarkan tipe input.
5. Eksekusi message berjalan sepanjang graph, termasuk fan-out/fan-in.
6. Statistik runtime tersedia lewat API internal.

## Aturan Operasional Penting

- Runtime tidak mengeksekusi JSON langsung.
- Runtime mengeksekusi hasil compile graph.
- Trigger API ada untuk manual trigger dari external system (misal visual editor), bukan jalur utama timed input.
- Input timed-trigger berjalan otomatis saat runtime aktif.

## Menjalankan Project

```powershell
.\gradlew.bat test
.\gradlew.bat run
```
