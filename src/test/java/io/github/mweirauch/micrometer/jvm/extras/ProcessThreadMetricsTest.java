/*
 * Copyright © 2017-2025 Michael Weirauch (michael.weirauch@gmail.com)
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
import static org.junit.Assert.assertNotNull;
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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class ProcessThreadMetricsTest {

    private final ProcfsStatus status = mock(ProcfsStatus.class);

    @Test
    public void testNullContract() {
        final ProcessThreadMetrics uut = new ProcessThreadMetrics(status);

        assertNotNull(uut);

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    public void testInstantiation() {
        final ProcessThreadMetrics uut = new ProcessThreadMetrics();

        assertNotNull(uut);
    }

    @Test
    public void testGetMetrics() {
        when(status.get(KEY.THREADS)).thenReturn(7D);
        when(status.get(KEY.VOLUNTARY_CTXT_SWITCHES)).thenReturn(4D);
        when(status.get(KEY.NONVOLUNTARY_CTXT_SWITCHES)).thenReturn(5D);

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final ProcessThreadMetrics uut = new ProcessThreadMetrics(status);

        uut.bindTo(registry);

        assertEquals(7D, registry.get("process.threads").gauge().value(), 0.0);

        assertEquals(4.0, registry.get("process.threads.context.switches.voluntary")
                .functionCounter().count(), 0.0);

        assertEquals(5.0, registry.get("process.threads.context.switches.nonvoluntary")
                .functionCounter().count(), 0.0);

        assertEquals(3, registry.getMeters().size());

        verify(status, times(6)).get(any(KEY.class));
        verifyNoMoreInteractions(status);
    }

    @Test
    public void testUnsupported() {
        when(status.get(KEY.THREADS)).thenReturn(-1D);
        when(status.get(KEY.VOLUNTARY_CTXT_SWITCHES)).thenReturn(-1D);
        when(status.get(KEY.NONVOLUNTARY_CTXT_SWITCHES)).thenReturn(-1D);

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final ProcessThreadMetrics uut = new ProcessThreadMetrics(status);

        uut.bindTo(registry);

        assertEquals(0, registry.getMeters().size());

        verify(status, times(3)).get(any(KEY.class));
        verifyNoMoreInteractions(status);
    }

}
