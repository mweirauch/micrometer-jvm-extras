/*
 * Copyright © 2017 Michael Weirauch (michael.weirauch@gmail.com)
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
package io.github.mweirauch.micrometer.jvm.extras;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsSmaps;
import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsSmaps.KEY;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class ProcessMemoryMetrics implements MeterBinder {

    private final ProcfsSmaps smaps;

    public ProcessMemoryMetrics() {
        this.smaps = new ProcfsSmaps();
    }

    /* default */ ProcessMemoryMetrics(ProcfsSmaps smaps) {
        this.smaps = Objects.requireNonNull(smaps);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (final KEY key : KEY.values()) {
            final String name = "process.memory." + key.name().toLowerCase(Locale.ENGLISH);
            final Id meterId = registry.createId(name, Collections.emptyList(), null, "bytes");
            registry.gauge(meterId, smaps, smapsRef -> smapsRef.get(key));
        }
    }

}
