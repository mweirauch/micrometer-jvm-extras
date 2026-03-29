/*
 * Copyright © 2017-2026 Michael Weirauch (michael.weirauch@gmail.com)
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

import java.util.Locale;
import java.util.Objects;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupMemory;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupMemory.Value;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class ProcessMemoryMetrics implements MeterBinder {

    private final ProcfsStatus status;

    private final CgroupMemory memory;

    public ProcessMemoryMetrics() {
        this(ProcfsStatus.getInstance(), CgroupMemory.getInstance());
    }

    ProcessMemoryMetrics(ProcfsStatus status, CgroupMemory memory) {
        this.status = Objects.requireNonNull(status);
        this.memory = Objects.requireNonNull(memory);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        ProcfsStatus.KEY[] keys = { ProcfsStatus.KEY.VSS, ProcfsStatus.KEY.RSS, ProcfsStatus.KEY.SWAP };
        for (ProcfsStatus.KEY key : keys) {
            if (status.get(key) == -1D) {
                continue;
            }

            String name = "process.memory." + key.name().toLowerCase(Locale.ENGLISH);
            Gauge.builder(name, status, statusRef -> value(key)) //
                    .baseUnit("bytes") //
                    .register(registry);
        }

        for (CgroupMemory.KEY key : CgroupMemory.KEY.values()) {
            if (memory.get(key).getStatus() != Value.Status.RESOLVED) {
                continue;
            }

            String name = "process.memory." + key.name().toLowerCase(Locale.ENGLISH) //
                    .replace("_", ".");
            double value = memory.get(key).getDoubleValue();
            // we register a constant value only (no further runtime queries)
            Gauge.builder(name, () -> value) //
                    .baseUnit("bytes") //
                    .register(registry);
        }
    }

    private Double value(ProcfsStatus.KEY key) {
        return status.get(key);
    }

}
