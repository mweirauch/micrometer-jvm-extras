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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus;
import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus.KEY;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ProcessMemoryMetricsTest {

    private final ProcfsStatus status = mock(ProcfsStatus.class);

    @Test
    void shouldRejectNullParameters() {
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(status);

        assertThat(uut).isNotNull();

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    void shouldInstantiate() {
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics();

        assertThat(uut).isNotNull();
    }

    @Test
    void shouldRegisterMemoryMetricsForValidValues() {
        when(status.get(KEY.VSS)).thenReturn(1D);
        when(status.get(KEY.RSS)).thenReturn(2D);
        when(status.get(KEY.SWAP)).thenReturn(3D);

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(status);

        uut.bindTo(registry);

        final String expectedUnit = "bytes";

        final Gauge vss = registry.get("process.memory.vss").gauge();
        assertThat(vss.value()).isEqualTo(1.0);
        assertThat(vss.getId().getBaseUnit()).isEqualTo(expectedUnit);

        final Gauge rss = registry.get("process.memory.rss").gauge();
        assertThat(rss.value()).isEqualTo(2.0);
        assertThat(rss.getId().getBaseUnit()).isEqualTo(expectedUnit);

        final Gauge swap = registry.get("process.memory.swap").gauge();
        assertThat(swap.value()).isEqualTo(3.0);
        assertThat(swap.getId().getBaseUnit()).isEqualTo(expectedUnit);

        assertThat(registry.getMeters()).hasSize(3);

        verify(status, times(6)).get(any(KEY.class));
        verifyNoMoreInteractions(status);
    }

    @Test
    void shouldSkipRegistrationForUnsupportedValues() {
        when(status.get(KEY.VSS)).thenReturn(-1D);
        when(status.get(KEY.RSS)).thenReturn(-1D);
        when(status.get(KEY.SWAP)).thenReturn(-1D);

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(status);

        uut.bindTo(registry);

        assertThat(registry.getMeters()).isEmpty();

        verify(status, times(3)).get(any(KEY.class));
        verifyNoMoreInteractions(status);
    }

}
