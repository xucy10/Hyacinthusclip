package moe.luminolmc.riceear;

import io.sigpipe.jbsdiff.InvalidHeaderException;
import io.sigpipe.jbsdiff.Patch;
import moe.luminolmc.riceear.update.AutoUpdate;
import org.apache.commons.compress.compressors.CompressorException;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;

public record PatchEntry(
        String location,
        byte[] originalHash,
        byte[] patchHash,
        byte[] outputHash,
        String originalPath,
        String patchPath,
        String outputPath
) {
    private static boolean announced = false;

    public static PatchEntry[] parse(final BufferedReader reader) throws IOException {
        var result = new PatchEntry[8];

        int index = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            final PatchEntry data = parseLine(line);
            if (data == null) {
                continue;
            }

            if (index == result.length) {
                result = Arrays.copyOf(result, index * 2);
            }
            result[index++] = data;
        }

        if (index != result.length) {
            return Arrays.copyOf(result, index);
        } else {
            return result;
        }
    }

    private static PatchEntry parseLine(final String line) {
        if (line.isBlank()) {
            return null;
        }
        if (line.startsWith("#")) {
            return null;
        }

        final var parts = line.split("\t");
        if (parts.length != 7) {
            throw new IllegalStateException("Invalid patch data line: " + line);
        }

        return new PatchEntry(
                parts[0],
                Util.fromHex(parts[1]),
                Util.fromHex(parts[2]),
                Util.fromHex(parts[3]),
                parts[4],
                parts[5],
                parts[6]
        );
    }

    public void applyPatch(final Map<String, Map<String, URL>> urls, final Path originalRootDir, final Path repoDir) throws IOException {
        final Path inputDir = originalRootDir.resolve("META-INF").resolve(this.location);
        final Path targetDir = repoDir.resolve(this.location);

        final Path inputFile = inputDir.resolve(this.originalPath);
        final Path outputFile = targetDir.resolve(this.outputPath);

        if (Files.exists(outputFile) && Util.isFileValid(outputFile, this.outputHash)) {
            urls.get(this.location).put(this.originalPath, outputFile.toUri().toURL());
            return;
        }

        if (!announced) {
            Riceear.logger.info("Applying patches");
            announced = true;
        }

        Riceear.logger.info("  Applying patch to " + this.originalPath);

        final byte[] originalBytes = Util.readBytes(inputFile);
        if (!Util.isDataValid(originalBytes, this.originalHash)) {
            throw new IllegalStateException("Original file hash mismatch for " + this.originalPath);
        }

        final Path patchFile = inputDir.resolve(this.patchPath);
        final byte[] patchBytes = Util.readBytes(patchFile);
        if (!Util.isDataValid(patchBytes, this.patchHash)) {
            throw new IllegalStateException("Patch file hash mismatch for " + this.patchPath);
        }

        try {
            if (!Files.isDirectory(targetDir)) {
                Files.createDirectories(targetDir);
            }

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Patch.patch(originalBytes, patchBytes, outputStream);
            final byte[] outputBytes = outputStream.toByteArray();
            if (!Util.isDataValid(outputBytes, this.outputHash)) {
                throw new IllegalStateException("Output hash mismatch for " + this.outputPath);
            }

            try (final OutputStream out = Files.newOutputStream(outputFile, CREATE, WRITE, TRUNCATE_EXISTING)) {
                out.write(outputBytes);
            }

            urls.get(this.location).put(this.originalPath, outputFile.toUri().toURL());
        } catch (final InvalidHeaderException | CompressorException e) {
            throw new IOException("Failed to apply patch", e);
        }
    }
}