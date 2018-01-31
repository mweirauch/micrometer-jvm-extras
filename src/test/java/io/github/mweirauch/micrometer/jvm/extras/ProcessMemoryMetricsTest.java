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

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsSmaps;
import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsSmaps.KEY;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class ProcessMemoryMetricsTest {

    private final ProcfsSmaps smaps = mock(ProcfsSmaps.class);

    @Test
    public void testNullContract() {
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(smaps);

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
        when(smaps.get(KEY.VSS)).thenReturn(1D);
        when(smaps.get(KEY.RSS)).thenReturn(2D);
        when(smaps.get(KEY.PSS)).thenReturn(3D);
        when(smaps.get(KEY.SWAP)).thenReturn(4D);
        when(smaps.get(KEY.SWAPPSS)).thenReturn(5D);

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final ProcessMemoryMetrics uut = new ProcessMemoryMetrics(smaps);

        uut.bindTo(registry);

        final Gauge vss = registry.get("process.memory.vss").gauge();
        assertEquals(1.0, vss.value(), 0.0);
        assertEquals("bytes", vss.getId().getBaseUnit());

        final Gauge rss = registry.get("process.memory.rss").gauge();
        assertEquals(2.0, rss.value(), 0.0);
        assertEquals("bytes", rss.getId().getBaseUnit());

        final Gauge pss = registry.get("process.memory.pss").gauge();
        assertEquals(3.0, pss.value(), 0.0);
        assertEquals("bytes", pss.getId().getBaseUnit());

        final Gauge swap = registry.get("process.memory.swap").gauge();
        assertEquals(4.0, swap.value(), 0.0);
        assertEquals("bytes", swap.getId().getBaseUnit());

        final Gauge swappss = registry.get("process.memory.swappss").gauge();
        assertEquals(5.0, swappss.value(), 0.0);
        assertEquals("bytes", swappss.getId().getBaseUnit());

        verify(smaps, times(5)).get(any(KEY.class));
        verifyNoMoreInteractions(smaps);
    }

}
