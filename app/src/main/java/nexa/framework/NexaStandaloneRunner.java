package nexa.framework;

import nexa.framework.runtime.api.OutputConsumer;
import nexa.framework.runtime.api.RuntimeConfiguration;
import nexa.framework.runtime.api.RuntimeEngine;
import nexa.framework.runtime.domain.execution.service.DefaultRuntimeEngine;
import nexa.framework.runtime.domain.statistics.model.RuntimeStatisticsSnapshot;
import nexa.framework.runtime.domain.workspace.model.WorkspaceDefinition;
import nexa.framework.runtime.domain.workspace.service.WorkspaceJsonLoader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * NexaStandaloneRunner bertindak sebagai runner mandiri (standalone executable)
 * untuk menjalankan workspace dari file JSON di luar lingkungan test framework JUnit.
 */
public final class NexaStandaloneRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("        Nexa Runtime Standalone Runner           ");
        System.out.println("=================================================");

        // 1. Tentukan path berkas JSON workspace
        File baseDir = new File(".").getAbsoluteFile();
        String pathStr = args.length > 0 ? args[0] : "workspaces/timed_stress_workspace.json";
        File file = new File(pathStr);
        if (!file.isAbsolute()) {
            file = new File(baseDir, pathStr);
        }

        if (!file.exists()) {
            // Coba fallback ke app/ folder jika dijalankan dari root
            File fallback = new File(baseDir, "app/" + pathStr);
            if (fallback.exists()) {
                file = fallback;
            } else {
                // Coba fallback default kedua
                File stressFallback = new File(baseDir, "workspaces/super_stress_workspace.json");
                File stressFallbackApp = new File(baseDir, "app/workspaces/super_stress_workspace.json");
                if (stressFallback.exists()) {
                    file = stressFallback;
                } else if (stressFallbackApp.exists()) {
                    file = stressFallbackApp;
                } else {
                    System.err.println("[Error] Berkas JSON tidak ditemukan di " + file.getAbsolutePath());
                    System.exit(1);
                    return;
                }
            }
        }

        Path jsonPath = file.toPath();
        System.out.println("[standalone] Membaca workspace dari: " + jsonPath.toAbsolutePath());

        // 2. Muat workspace definition menggunakan Loader bawaan
        WorkspaceJsonLoader loader = new WorkspaceJsonLoader();
        WorkspaceDefinition workspaceDef = loader.fromFile(jsonPath);
        System.out.println("[standalone] Workspace '" + workspaceDef.id() + "' berhasil dimuat.");

        // 3. Setup output consumer (mencetak hasil eksekusi node output ke console)
        OutputConsumer outputConsumer = (context, nodeId, message) -> {
            System.out.println(String.format("[%s][OUT][%s] Payload: %s",
                    Instant.now().toString(), nodeId, message.values().get("payload")));
        };

        // 4. Inisialisasi Engine
        RuntimeEngine runtime = new DefaultRuntimeEngine(
                new RuntimeConfiguration(Duration.ofSeconds(15)),
                outputConsumer
        );

        // 5. Deploy & Jalankan
        System.out.println("[standalone] Memulai kompilasi graf dan deploy...");
        runtime.deploy(workspaceDef);
        System.out.println("[standalone] Menghidupkan runtime...");
        runtime.startRuntime();

        // Tentukan durasi running (default 10 detik agar tidak berjalan selamanya secara tidak sengaja)
        int runDuration = Integer.getInteger("run.duration", 10);
        System.out.println("[standalone] Runtime aktif. Tekan Ctrl+C untuk menghentikan.");
        if (runDuration > 0) {
            System.out.println("[standalone] Runner akan otomatis berhenti setelah " + runDuration + " detik.");
        } else {
            System.out.println("[standalone] Runner akan berjalan tanpa batas waktu (indefinitely).");
        }

        // Registrasikan shutdown hook agar program keluar dengan bersih
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[standalone] Mematikan runtime engine...");
            runtime.stopRuntime();
            System.out.println("[standalone] Selesai. Sampai jumpa!");
        }));

        // Loop untuk memantau statistik aktivitas flow dengan batas waktu
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (runDuration * 1000L);

        try {
            while (runDuration <= 0 || System.currentTimeMillis() < endTime) {
                TimeUnit.SECONDS.sleep(2);
                if (workspaceDef.flows() != null && !workspaceDef.flows().isEmpty()) {
                    String sampleFlowId = workspaceDef.flows().getFirst().id();
                    RuntimeStatisticsSnapshot stats = runtime.statistics(workspaceDef.id(), sampleFlowId);
                    System.out.println(String.format("[STATS][%s] Completed: %d | Failed: %d | Running: %d",
                            sampleFlowId, stats.completed(), stats.failed(), stats.running()));
                }
            }
            if (runDuration > 0) {
                System.out.println("[standalone] Batas durasi eksekusi tercapai (" + runDuration + " detik). Mematikan...");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
