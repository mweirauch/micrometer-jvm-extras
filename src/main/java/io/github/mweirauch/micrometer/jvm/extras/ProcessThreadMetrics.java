/*
 * Copyright © 2017-2019 Michael Weirauch (michael.weirauch@gmail.com)
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

import java.util.Objects;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus;
import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus.KEY;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class ProcessThreadMetrics implements MeterBinder {

    private final ProcfsStatus status;

    public ProcessThreadMetrics() {
        this.status = ProcfsStatus.getInstance();
    }

    /* default */ ProcessThreadMetrics(ProcfsStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("process.threads", status, statusRef -> value(KEY.THREADS))//
                .description("The number of process threads")//
                .register(registry);
    }

    private Double value(KEY key) {
        return status.get(key);
    }

}
