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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus;
import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus.KEY;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class ProcessMemoryMetricsTest {

    private final ProcfsStatus status = mock(ProcfsStatus.class);

    @Test
    public void testNullContract() {
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(status);

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @SuppressWarnings("unused")
    @Test
    public void testInstantiation() {
        new ProcessMemoryMetrics();
    }

    @Test
    public void testGetMetrics() throws Exception {
        when(status.get(KEY.VSS)).thenReturn(1D);
        when(status.get(KEY.RSS)).thenReturn(2D);
        when(status.get(KEY.SWAP)).thenReturn(3D);

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(status);

        uut.bindTo(registry);

        final String expectedUnit = "bytes";

        final Gauge vss = registry.get("process.memory.vss").gauge();
        assertEquals(1.0, vss.value(), 0.0);
        assertEquals(expectedUnit, vss.getId().getBaseUnit());

        final Gauge rss = registry.get("process.memory.rss").gauge();
        assertEquals(2.0, rss.value(), 0.0);
        assertEquals(expectedUnit, rss.getId().getBaseUnit());

        final Gauge swap = registry.get("process.memory.swap").gauge();
        assertEquals(3.0, swap.value(), 0.0);
        assertEquals(expectedUnit, swap.getId().getBaseUnit());

        verify(status, times(3)).get(any(KEY.class));
        verifyNoMoreInteractions(status);
    }

}
