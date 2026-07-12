# Runtime Engine Architecture

Dokumen ini menjelaskan komponen inti runtime, alur eksekusi, dan titik extension.

## Paket Penting

- API publik runtime: [app/src/main/java/nexa/framework/runtime/api](app/src/main/java/nexa/framework/runtime/api)
- Definisi JSON workspace/flow/node: [app/src/main/java/nexa/framework/runtime/definition](app/src/main/java/nexa/framework/runtime/definition)
- Validasi + compile graph: [app/src/main/java/nexa/framework/runtime/compile](app/src/main/java/nexa/framework/runtime/compile)
- Engine eksekusi: [app/src/main/java/nexa/framework/runtime/engine/DefaultRuntimeEngine.java](app/src/main/java/nexa/framework/runtime/engine/DefaultRuntimeEngine.java)
- Handler input node: [app/src/main/java/nexa/framework/runtime/input](app/src/main/java/nexa/framework/runtime/input)
- Runtime JavaScript: [app/src/main/java/nexa/framework/runtime/js](app/src/main/java/nexa/framework/runtime/js)
- Statistik runtime: [app/src/main/java/nexa/framework/runtime/stats](app/src/main/java/nexa/framework/runtime/stats)

## Alur Deploy

1. Load JSON menjadi WorkspaceDefinition.
2. Validasi topology flow.
3. Compile menjadi CompiledWorkspace / CompiledFlow.
4. Register ke runtime registry.
5. Siap dieksekusi saat runtime aktif.

## Alur Start Runtime

1. Runtime status menjadi aktif.
2. Tiap workspace aktif diproses.
3. Tiap flow aktif diproses.
4. Tiap input node aktif diproses melalui InputNodeHandlerRegistry.

Pemisahan ini penting supaya penambahan tipe input baru tidak mengubah business flow engine utama.

## Eksekusi Message

1. Input menghasilkan seed message.
2. Runtime membuat execution context independen.
3. Message dirutekan ke target node sesuai port.
4. Fan-out diproses paralel via virtual thread.
5. Tiap cabang menerima deep copy message (isolasi mutable state).
6. Output node mengonsumsi message terakhir tanpa forward lanjutan.

## Fan-out dan Fan-in

- Fan-out: satu output node mengirim ke banyak target node.
- Fan-in: satu target node dapat menerima message dari banyak source node.

Model routing ini dikompilasi dari daftar connection di flow JSON.

## Concurrency dan Isolation

- Setiap execution memiliki ExecutionContext sendiri.
- State eksekusi tidak disimpan di node.
- Input policy membatasi maksimum concurrent execution per input node.
- Jika limit tercapai, trigger baru direject.

## Timeout dan Cleanup

- Timeout global dieksekusi lewat scheduler.
- Eksekusi yang timeout/cancel/fail/completed dibersihkan segera.
- Cleanup melepas context data, future task, dan referensi sementara.

## Cara Menambah Tipe Input Baru

1. Tambah implementasi InputNodeHandler baru di paket input.
2. Implement nodeType() sesuai type di JSON.
3. Implement activate(...) menggunakan InputNodeActivationPort.
4. Registrasikan handler baru di constructor DefaultRuntimeEngine.

Jika tipe input memerlukan polling/subscription, seluruh scheduling tetap harus lewat runtime, bukan thread buatan script/user.

## File Kunci Untuk Refactor

- Engine orchestration: [app/src/main/java/nexa/framework/runtime/engine/DefaultRuntimeEngine.java](app/src/main/java/nexa/framework/runtime/engine/DefaultRuntimeEngine.java)
- Input activation contract: [app/src/main/java/nexa/framework/runtime/input/InputNodeActivationPort.java](app/src/main/java/nexa/framework/runtime/input/InputNodeActivationPort.java)
- Input handler registry: [app/src/main/java/nexa/framework/runtime/input/InputNodeHandlerRegistry.java](app/src/main/java/nexa/framework/runtime/input/InputNodeHandlerRegistry.java)
- Flow compile routing: [app/src/main/java/nexa/framework/runtime/compile/FlowCompiler.java](app/src/main/java/nexa/framework/runtime/compile/FlowCompiler.java)
