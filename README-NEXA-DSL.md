# Nexa DSL V1

Dokumen ini menjelaskan cara memprogram node executor `language = "nexa"` di Nexa Framework.

Nexa DSL adalah bahasa scripting kecil untuk logic flow. Fokusnya:

- compile cepat
- runtime cepat
- syntax ringkas
- cukup aman untuk transformasi data flow
- mudah di-embed ke runtime Java

V1 bukan general purpose language penuh. Bahasa ini dibuat untuk executor node, bukan untuk menggantikan Java.

## Status V1

Yang sudah tersedia:

- `val`, `var`
- `null`
- `?.`
- `??`
- object literal
- array literal
- property access
- index access
- assignment ke property/index
- `if / else if / else`
- `switch / case / default`
- `for (init; condition; update)`
- `return`
- `fun` function declaration
- `fun (...) => ...` lambda expression
- closure sederhana
- built-in `msg`
- built-in `Json`
- built-in `Math`
- built-in `DateTime`
- built-in `Regex`
- built-in `send`
- konversi `toString`, `toBool`, `toNumber`, `toDate`, `toDateTime`
- array methods dasar dan higher-order
- string methods dasar
- regex matching

Yang belum tersedia:

- `asset`
- `eventSys`
- `action`
- `await`
- `while`
- `break`
- `continue`
- `for in`
- `for of`
- class
- import/module syntax
- static type declaration
- user-defined extension method syntax
- async function

## Filosofi Runtime

Nexa DSL saat ini memakai model type runtime, bukan static typing compile-time.

Artinya:

- error type mismatch akan muncul saat runtime
- nilai property yang tidak ada dibaca sebagai `null`
- `undefined` tidak ada
- number belum dibedakan tegas menjadi `int32`, `float64`, `uint32`, dan seterusnya

## 1. Quick Start

Contoh script singkat:

```nexa
val rawSpeed = msg.payload?.speed ?? 0
val speed = rawSpeed.toNumber()

if (speed >= 120) {
    msg.payload = {
        level: "alarm",
        speed: speed,
        text: `High speed: ${speed}`
    }
    send("alarm", msg)
    return
}

msg.payload = {
    level: "ok",
    speed: speed
}

send(msg)
```

## 2. Model Nilai

Nilai runtime yang dipakai evaluator:

- `Number`
- `Boolean`
- `String`
- `Date`
- `DateTime`
- `Array`
- `Object`
- `Function`
- `null`

Catatan:

- hanya ada `null`, tidak ada `undefined`
- object property yang tidak ditemukan akan menghasilkan `null`
- optional chaining hanya berlaku di property access `?.`
- index access pada array/string/object yang di luar batas akan menghasilkan `null`

## 3. Variabel

### 3.1 `val`

`val` adalah variabel read-only.

```nexa
val machine = "Taiyo1"
```

Setelah dibuat, `val` tidak boleh diassign ulang.

### 3.2 `var`

`var` adalah variabel mutable.

```nexa
var total = 0
total += 10
```

### 3.3 Deklarasi tanpa initializer

Jika variabel tidak diberi initializer, nilainya `null`.

```nexa
var note
```

## 4. Null Safety

### 4.1 Optional Chaining `?.`

Jika target bernilai `null`, hasil akses adalah `null`.

```nexa
val speed = msg.payload?.speed
```

### 4.2 Null Coalescing `??`

Mengembalikan sisi kanan jika sisi kiri `null`.

```nexa
val speed = msg.payload?.speed ?? 0
```

### 4.3 Kombinasi umum

```nexa
val speed = (msg.payload?.speed ?? "0").toNumber()
```

## 5. Nilai Primitive

### 5.1 Number

Contoh:

```nexa
val count = 10
val ratio = 12.5
```

### 5.2 Boolean

```nexa
val enabled = true
```

### 5.3 String

```nexa
val machine = "Taiyo1"
```

### 5.4 Null

```nexa
val empty = null
```

## 6. Object

### 6.1 Object Literal

```nexa
val payload = {
    machine: "Taiyo1",
    status: "RUN",
    count: 10
}
```

### 6.2 Property Access

```nexa
val status = payload.status
```

### 6.3 Property Assignment

```nexa
payload.status = "STOP"
```

### 6.4 Missing Property

Jika property tidak ada:

```nexa
val value = payload.unknown
```

hasilnya `null`.

### 6.5 Nested Property

```nexa
msg.payload = {
    machine: {
        name: "Taiyo1"
    }
}

msg.payload.machine.name = "Taiyo-A"
```

Catatan:

- parent object harus sudah ada
- runtime belum membuat nested object otomatis saat chain assignment

## 7. Array

### 7.1 Array Literal

```nexa
val values = [1, 2, 3]
```

### 7.2 Index Access

```nexa
val first = values[0]
```

### 7.3 Index Assignment

```nexa
values[1] = 99
```

### 7.4 Out of Range

Jika index di luar batas saat baca:

- hasil `null`

Jika index di luar batas saat assign:

- runtime error

## 8. Operator

### 8.1 Arithmetic Operator

- `+`
- `-`
- `*`
- `/`
- `%`

```nexa
val total = 10 + 5 * 2
```

### 8.2 Comparison Operator

- `==`
- `!=`
- `>`
- `>=`
- `<`
- `<=`

```nexa
if (speed >= 100) {
    send("fast", msg)
}
```

### 8.3 Logical Operator

- `&&`
- `||`
- `!`

### 8.4 Assignment Operator

- `=`
- `+=`
- `-=`
- `*=`
- `/=`

## 9. Control Flow

### 9.1 if / else if / else

```nexa
if (speed > 120) {
    msg.payload.level = "high"
} else if (speed > 80) {
    msg.payload.level = "medium"
} else {
    msg.payload.level = "low"
}
```

Catatan:

- tidak ada keyword `elseif`
- gunakan `else if`

### 9.2 switch

Contoh:

```nexa
switch (msg.payload?.state ?? "-") {
    case "run":
        msg.payload.label = "Running"
    case "idle":
        msg.payload.label = "Idle"
    default:
        msg.payload.label = "Unknown"
}
```

Perilaku `switch` di Nexa:

- hanya menjalankan case pertama yang match
- tidak ada fallthrough
- tidak butuh `break`
- `default` opsional

### 9.3 for

V1 hanya mendukung bentuk klasik:

```nexa
var total = 0
for (var index = 0; index < 5; index += 1) {
    total += index
}
```

Belum tersedia:

- `while`
- `for in`
- `for of`

### 9.4 return

Menghentikan function atau script.

```nexa
if (msg.payload == null) {
    return
}
```

## 10. Function

Function di Nexa adalah first-class value.

Artinya:

- bisa disimpan ke variabel
- bisa dipass ke method seperti `map` dan `filter`
- bisa di-return dari function lain

### 10.1 Function Declaration

Bentuk expression body:

```nexa
fun square(value) => value * value
```

Bentuk block body:

```nexa
fun sum(a, b) {
    return a + b
}
```

### 10.2 Function Expression / Lambda

```nexa
val doubleIt = fun (value) => value * 2
```

Block body:

```nexa
val classify = fun (speed) {
    if (speed > 100) {
        return "fast"
    }
    return "normal"
}
```

### 10.3 Closure

Lambda bisa menangkap variabel luar.

```nexa
val factor = 3
val mapper = fun (item) => item * factor
```

### 10.4 Argumen Function

Aturan saat ini:

- argumen ekstra akan diabaikan
- parameter yang tidak mendapat nilai akan berisi `null`

Contoh:

```nexa
fun sample(a, b) {
    return [a, b]
}

val value = sample(10)
```

hasil `b` akan `null`.

## 11. Template String

Gunakan backtick.

```nexa
val machine = "Taiyo1"
val text = `Hallo, ${machine}`
```

Template expression akan dievaluasi sebagai expression Nexa biasa.

## 12. Type Conversion

Method conversion yang tersedia:

- `toString()`
- `toBool()`
- `toNumber()`
- `toDate()`
- `toDateTime()`

### 12.1 `toString()`

Mengubah value menjadi string.

```nexa
val text = 120.toString()
```

### 12.2 `toBool()`

Truthiness rule:

- `null` -> `false`
- `false` -> `false`
- `0` -> `false`
- `""` -> `false`
- array kosong -> `false`
- object kosong -> `false`
- selain itu -> `true`

### 12.3 `toNumber()`

Input yang didukung:

- `Number`
- `String` numerik
- `Boolean`

Contoh:

```nexa
val speed = "1500".toNumber()
```

Jika string bukan angka valid, runtime error.

### 12.4 `toDate()`

Input yang didukung:

- ISO date string, contoh `2026-07-12`
- ISO datetime string, contoh `2026-07-12T10:15:30Z`
- `DateTime`
- `Date`

### 12.5 `toDateTime()`

Input yang didukung:

- ISO datetime string
- `Date`
- `DateTime`

## 13. String

String method yang tersedia:

- `length`
- `trim()`
- `replace(from, to)`
- `replaceAll(pattern, replacement)`
- `split(separator)`
- `startsWith(value)`
- `endsWith(value)`
- `includes(value)`
- `substring(start, end?)`
- `slice(start, end?)`
- `toUpperCase()`
- `toLowerCase()`
- `match(pattern)`

### 13.1 `length`

Mengembalikan panjang string.

```nexa
val size = "Taiyo".length
```

### 13.2 `trim()`

Menghapus spasi awal dan akhir.

```nexa
val value = "  abc  ".trim()
```

### 13.3 `replace(from, to)`

Mengganti literal string.

```nexa
val value = "A-100".replace("-", "_")
```

### 13.4 `replaceAll(pattern, replacement)`

Mengganti memakai regex Java.

```nexa
val value = "A   B".replaceAll("\\s+", "-")
```

### 13.5 `split(separator)`

```nexa
val parts = "Setup/1".split("/")
```

Hasilnya array string.

### 13.6 `startsWith(value)`

```nexa
val ok = "WO-100".startsWith("WO-")
```

### 13.7 `endsWith(value)`

```nexa
val ok = "report.json".endsWith(".json")
```

### 13.8 `includes(value)`

```nexa
val ok = "Production".includes("duct")
```

### 13.9 `substring(start, end?)`

Mengambil bagian string dari index `start` sampai sebelum `end`.

```nexa
val part = "Taiyo".substring(0, 3)
```

### 13.10 `slice(start, end?)`

Mirip `substring`, tetapi mendukung index negatif.

```nexa
val tail = "Taiyo".slice(-2)
```

### 13.11 `toUpperCase()`

### 13.12 `toLowerCase()`

### 13.13 `match(pattern)`

Mengembalikan array hasil match regex.

```nexa
val matches = "WO-100-A".match("[A-Z]+")
```

## 14. Array Method

Array method yang tersedia:

- `length`
- `push(...items)`
- `pop()`
- `shift()`
- `unshift(...items)`
- `slice(start, end?)`
- `splice(start, deleteCount?, ...items)`
- `includes(value)`
- `indexOf(value)`
- `join(separator?)`
- `map(callback)`
- `filter(callback)`
- `reduce(callback, initialValue?)`
- `forEach(callback)`
- `find(callback)`
- `some(callback)`
- `every(callback)`

### 14.1 `length`

Mengembalikan jumlah item.

```nexa
val size = values.length
```

### 14.2 `push(...items)`

Menambah item ke akhir array.

Return:

- panjang array baru

```nexa
var values = [1, 2]
values.push(3, 4)
```

### 14.3 `pop()`

Menghapus item terakhir.

Return:

- item yang dihapus
- `null` jika array kosong

### 14.4 `shift()`

Menghapus item pertama.

Return:

- item yang dihapus
- `null` jika array kosong

### 14.5 `unshift(...items)`

Menambah item ke depan array.

Return:

- panjang array baru

### 14.6 `slice(start, end?)`

Mengembalikan array baru.

Mendukung index negatif.

```nexa
val values = [1, 2, 3, 4]
val tail = values.slice(-2)
```

### 14.7 `splice(start, deleteCount?, ...items)`

Mengubah array asli.

Return:

- array item yang dihapus

```nexa
var values = [1, 2, 3, 4]
val removed = values.splice(1, 2, 9, 10)
```

Hasil:

- `values` menjadi `[1, 9, 10, 4]`
- `removed` menjadi `[2, 3]`

### 14.8 `includes(value)`

```nexa
val ok = [1, 2, 3].includes(2)
```

### 14.9 `indexOf(value)`

Return index pertama, atau `-1` jika tidak ditemukan.

### 14.10 `join(separator?)`

Menggabungkan elemen array menjadi string.

Default separator adalah `,`.

```nexa
val text = [1, 2, 3].join("-")
```

### 14.11 `map(callback)`

Mengembalikan array baru hasil transformasi.

Signature callback:

```nexa
fun (item, index, array) => ...
```

Contoh:

```nexa
val doubled = [1, 2, 3].map(fun (item) => item * 2)
```

### 14.12 `filter(callback)`

Mengembalikan array baru berisi item yang callback-nya truthy.

```nexa
val active = [0, 1, 2, 3].filter(fun (item) => item > 1)
```

### 14.13 `reduce(callback, initialValue?)`

Menggabungkan array menjadi satu nilai.

Signature callback:

```nexa
fun (accumulator, item, index, array) => ...
```

```nexa
val total = [10, 20, 30].reduce(fun (acc, item) => acc + item, 0)
```

Catatan:

- jika array kosong dan tidak ada `initialValue`, runtime error

### 14.14 `forEach(callback)`

Menjalankan callback per item.

Return:

- `null`

```nexa
var trace = []
[1, 2, 3].forEach(fun (item, index) {
    trace.push(`${index}:${item}`)
})
```

### 14.15 `find(callback)`

Mengembalikan item pertama yang match, atau `null`.

### 14.16 `some(callback)`

Mengembalikan `true` jika minimal satu item match.

### 14.17 `every(callback)`

Mengembalikan `true` jika semua item match.

## 15. Json

Built-in `Json` menyediakan:

- `Json.parse(text)`
- `Json.stringify(value)`

### 15.1 `Json.parse(text)`

Mengubah text JSON menjadi object/array runtime.

```nexa
val parsed = Json.parse("{\"items\":[1,2,3]}")
```

### 15.2 `Json.stringify(value)`

Mengubah value menjadi text JSON.

```nexa
val text = Json.stringify({ ok: true, count: 10 })
```

## 16. Math

Built-in `Math` menyediakan:

- `Math.abs(x)`
- `Math.round(x)`
- `Math.floor(x)`
- `Math.ceil(x)`
- `Math.max(a, b, ...)`
- `Math.min(a, b, ...)`
- `Math.random()`
- `Math.sin(x)`
- `Math.cos(x)`
- `Math.sqrt(x)`
- `Math.pow(a, b)`
- `Math.log(x)`

### 16.1 `Math.abs(x)`

### 16.2 `Math.round(x)`

Return dibulatkan ke integer terdekat.

### 16.3 `Math.floor(x)`

### 16.4 `Math.ceil(x)`

### 16.5 `Math.max(a, b, ...)`

### 16.6 `Math.min(a, b, ...)`

### 16.7 `Math.random()`

Return number acak antara `0` dan `1`.

### 16.8 `Math.sin(x)`

### 16.9 `Math.cos(x)`

### 16.10 `Math.sqrt(x)`

### 16.11 `Math.pow(a, b)`

### 16.12 `Math.log(x)`

Contoh:

```nexa
val safe = Math.max(0, -10)
val rounded = Math.round(10.6)
```

## 17. Date dan DateTime

### 17.1 `DateTime.now()`

Menghasilkan runtime `DateTime`.

```nexa
val now = DateTime.now()
```

### 17.2 `toISOString()`

Tersedia untuk:

- `Date`
- `DateTime`

```nexa
val isoDate = "2026-07-12".toDate().toISOString()
val isoDateTime = DateTime.now().toISOString()
```

### 17.3 Catatan

Yang belum ada:

- arithmetic date
- timezone conversion API
- date diff
- date formatting custom

## 18. Regex

Built-in `Regex` menyediakan:

- `Regex.match(text, pattern)`
- `Regex.replace(text, pattern, replacement)`

String juga menyediakan:

- `string.match(pattern)`

### 18.1 `Regex.match(text, pattern)`

Mengembalikan array hasil match.

### 18.2 `Regex.replace(text, pattern, replacement)`

Mengganti memakai regex Java.

### 18.3 `string.match(pattern)`

Shortcut untuk match dari string instance.

Contoh:

```nexa
val matches = Regex.match("A-100", "[A-Z]")
val replaced = Regex.replace("A-100", "-", "_")
val local = "WO-100-A".match("[A-Z]+")
```

Catatan:

- regex literal seperti `/abc/g` belum ada

## 19. msg

`msg` adalah object mutable utama untuk input/output script.

Contoh:

```nexa
msg.payload = {
    branch: "ok",
    count: 1
}

send(msg)
```

Nested update jika parent sudah ada:

```nexa
msg.payload.count = msg.payload.count + 1
```

## 20. send

`send` adalah built-in function untuk emit message ke output port.

Bentuk yang tersedia:

```nexa
send(msg)
send("default", msg)
send("success", msg)
send(["success", "audit"], msg)
```

Aturan:

- message harus object
- target port bisa string atau array string

## 21. Host Extension dari Java

Runtime bisa menerima global baru dari Java / JAR.

Model saat ini:

- tidak memakai syntax `import`
- feature host diinjeksi sebagai global object

Kontrak integrasi:

1. implement `NexaRuntimeExtension`
2. return global namespace lewat `globals()`
3. namespace object implement `NexaHostObject`
4. method return `NexaRuntime.NexaCallable` atau object/namespace lain
5. register lewat `META-INF/services`

Contoh concept:

```java
public final class MesExtension implements NexaRuntimeExtension {
    @Override
    public Map<String, Object> globals() {
        return Map.of("Mes", new MesHostObject());
    }
}
```

Pemakaian di script:

```nexa
val result = Mes.lookupWorkOrder("WO-100")
msg.payload = result
send(msg)
```

## 22. Contoh Program

### 22.1 Contoh Dasar

```nexa
var values = [1, 2, 3]
var total = 0

for (var index = 0; index < values.length; index += 1) {
    total += values[index]
}

val machine = msg.payload?.machine ?? "unknown"
val now = DateTime.now().toISOString()
var status = "IDLE"

if (total > 3) {
    status = "RUN"
}

msg.payload = {
    machine: machine,
    total: total,
    status: status,
    createdAt: now,
    text: `Hallo, ${machine}`
}

send("default", msg)
```

### 22.2 Contoh dengan switch

```nexa
val state = msg.payload?.state ?? "-"

switch (state) {
    case "run":
        msg.payload.label = "Running"
    case "idle":
        msg.payload.label = "Idle"
    default:
        msg.payload.label = "Unknown"
}

send(msg)
```

### 22.3 Contoh Function dan Koleksi

```nexa
fun square(value) => value * value

fun sumAll(values) {
    return values.reduce(fun (acc, item) => acc + item, 0)
}

val factor = 3
val mapper = fun (item) => item * factor
val values = [1, 2, 3, 4]
val mapped = values.map(mapper)
val filtered = mapped.filter(fun (item) => item > 6)
val total = sumAll(filtered)

msg.payload = {
    mapped: mapped,
    filtered: filtered,
    total: total,
    squared: square(5)
}

send(msg)
```

### 22.4 Contoh Parsing JSON

```nexa
val source = "{\"items\":[1,2,3],\"name\":\"taiyo\"}"
val parsed = Json.parse(source)

msg.payload = {
    count: parsed.items.length,
    upper: parsed.name.toUpperCase()
}

send(msg)
```

### 22.5 Contoh Filter Array Object

```nexa
val jobs = [
    { code: "WO-1", good: 10 },
    { code: "WO-2", good: 0 },
    { code: "WO-3", good: 7 }
]

val now = DateTime.now().toISOString()
val active = jobs.filter(fun (job) => job.good > 0)
val labels = active.map(fun (job) => `${job.code}:${job.good}`)

msg.payload = {
    now,
    activeCount: active.length,
    labels: labels
}

send(msg)
```

## 23. Ringkasan Gap V1

Yang masih belum ada:

- `asset`
- `eventSys`
- `action`
- `await`
- `while`
- `break`
- `continue`
- `for in`
- `for of`
- class
- import/module syntax
- user-defined extension method syntax
- static type annotation
- async host call

## 24. Prioritas Lanjutan yang Masuk Akal

Urutan realistis berikutnya:

1. `while`
2. `break`
3. `continue`
4. host API `asset`
5. host API `eventSys`
6. host API `action`
7. async host bridge terbatas
8. import/module layer di atas host extension
