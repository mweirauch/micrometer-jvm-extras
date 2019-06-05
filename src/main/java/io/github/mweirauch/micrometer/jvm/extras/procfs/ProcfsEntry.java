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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ProcfsEntry {

    private static final Logger log = LoggerFactory.getLogger(ProcfsEntry.class);

    private static final long CACHE_DURATION_MS = 1000;

    private final Object lock = new Object();

    private final ProcfsReader reader;

    private final Map<ValueKey, Double> values = new HashMap<>();

    private long lastHandle = -1;

    public interface ValueKey {
        //
    }

    protected ProcfsEntry(ProcfsReader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    public Double get(ValueKey key) {
        Objects.requireNonNull(key);

        collect();
        return values.getOrDefault(key, defaultValue());
    }

    /* default */ final void collect() {
        synchronized (lock) {
            if (lastHandle != -1 && lastHandle + CACHE_DURATION_MS > currentTime()) {
                return;
            }
            try {
                final Map<ValueKey, Double> handledValues = new HashMap<>();
                reader.read((line) -> handle(handledValues, line));

                values.clear();
                values.putAll(handledValues);
                lastHandle = currentTime();
            } catch (IOException e) {
                values.clear();
                log.warn("Failed reading '" + reader.getEntryPath() + "'!", e);
            }
        }
    }

    protected abstract void handle(Map<ValueKey, Double> handledValues,
            String line);

    protected Double defaultValue() {
        return Double.valueOf(-1);
    }

    /* default */ long currentTime() {
        return System.currentTimeMillis();
    }

}
