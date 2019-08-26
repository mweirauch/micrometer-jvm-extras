/*
 * Copyright © 2016-2019 Michael Weirauch (michael.weirauch@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.mweirauch.micrometer.jvm.extras.procfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcfsReader {

    private static final Logger log = LoggerFactory.getLogger(ProcfsReader.class);

    private static final Map<String, ProcfsReader> instances = new HashMap<>();

    private static final Object instancesLock = new Object();

    private static final Path BASE = Paths.get("/proc", "self");

    private final Path entryPath;

    private final boolean osSupport;

    private ProcfsReader(String entry) {
        this(BASE, entry, false);
    }

    /* default */ ProcfsReader(Path base, String entry) {
        this(base, entry, true);
    }

    /* default */ ProcfsReader(Path base, String entry, boolean forceOSSupport) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(entry);

        this.entryPath = base.resolve(entry);

        this.osSupport = forceOSSupport
                || System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("linux");
    }

    /* default */ Path getEntryPath() {
        return entryPath;
    }

    /* default */ void read(Consumer<String> consumer) throws IOException {
        readPath(entryPath, consumer);
    }

    private void readPath(Path path, Consumer<String> consumer) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(consumer);

        if (!osSupport) {
            return;
        }

        log.trace("Reading '{}'", path);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            for (;;) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                consumer.accept(line);
            }
        }
    }

    /* default */ static ProcfsReader getInstance(String entry) {
        Objects.requireNonNull(entry);

        synchronized (instancesLock) {
            return instances.computeIfAbsent(entry, e -> new ProcfsReader(e));
        }
    }

}
