package org.gradle.wrapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * TRANSPARENT EDUCATIONAL BOOTSTRAP, NOT THE OFFICIAL GRADLE WRAPPER JAR.
 * Downloads and launches the official Gradle 8.9 distribution once on first use.
 * In your IDE, you can replace it with the official Gradle wrapper using:
 *     gradle wrapper --gradle-version 8.9
 */
public final class GradleWrapperMain {
    private static final String URL = "https://services.gradle.org/distributions/gradle-8.9-bin.zip";

    public static void main(String[] args) throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        Path home = Path.of(System.getProperty("user.home"), ".gradle", "wrapper", "dists", "focusflow-gradle-8.9");
        Path exe = home.resolve(windows ? "gradle-8.9/bin/gradle.bat" : "gradle-8.9/bin/gradle");
        if (!Files.isRegularFile(exe)) {
            System.out.println("Downloading official Gradle 8.9 distribution...");
            Files.createDirectories(home);
            Path zipFile = Files.createTempFile(home, "gradle-8.9-", ".zip");
            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS).build();
                HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).GET().build();
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(zipFile));
                if (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode());
                try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(zipFile))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        Path target = home.resolve(entry.getName()).normalize();
                        if (!target.startsWith(home)) throw new IOException("Unsafe ZIP path: " + entry.getName());
                        if (entry.isDirectory()) {
                            Files.createDirectories(target);
                        } else {
                            Files.createDirectories(target.getParent());
                            Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                        zip.closeEntry();
                    }
                }
            } finally { Files.deleteIfExists(zipFile); }
        }
        if (!Files.isRegularFile(exe)) throw new IOException("Gradle executable not found: " + exe);
        if (!windows) exe.toFile().setExecutable(true);
        List<String> cmd = new ArrayList<>();
        if (windows) { cmd.add("cmd.exe"); cmd.add("/c"); }
        cmd.add(exe.toString());
        cmd.addAll(Arrays.asList(args));
        Process process = new ProcessBuilder(cmd).inheritIO().start();
        System.exit(process.waitFor());
    }
}
