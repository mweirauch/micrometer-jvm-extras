/*
 * Copyright © 2016 Michael Weirauch (michael.weirauch@gmail.com)
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsEntry.ValueKey;
import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsReader.ReadResult;

public class ProcfsEntryTest {

    private final ProcfsReader reader = mock(ProcfsReader.class);

    private final ProcfsEntry uut = new TestProcfsEntry(reader);

    @Test
    public void testNullContract() {
        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    public void testValueHandling() throws IOException {
        final List<String> lines1 = Arrays.asList("1", "2", "3");
        final List<String> lines2 = Arrays.asList("1", "2");
        final ReadResult readResult1 = new ReadResult(lines1, 0);
        final ReadResult readResult2 = new ReadResult(lines2, 1);
        when(reader.read()).thenReturn(
                // first three calls to get()
                readResult1, readResult1, readResult1,
                // last three calls to get()
                readResult2, readResult2, readResult2);

        assertEquals(Long.valueOf(1), uut.get(TestKey.ONE));
        assertEquals(Long.valueOf(2), uut.get(TestKey.TWO));
        assertEquals(Long.valueOf(3), uut.get(TestKey.THREE));

        assertEquals(Long.valueOf(1), uut.get(TestKey.ONE));
        assertEquals(Long.valueOf(2), uut.get(TestKey.TWO));
        assertEquals(Long.valueOf(-1), uut.get(TestKey.THREE));
    }

    @Test
    public void testCollectUpdated() throws Exception {
        final List<String> lines1 = Arrays.asList("1");
        final ReadResult readResult = spy(new ReadResult(lines1, 0));
        when(reader.read()).thenReturn(readResult);
        final ProcfsEntry entry = spy(uut);

        entry.get(TestKey.ONE);

        verify(reader).read();
        verifyNoMoreInteractions(reader);
        verify(readResult).getReadTime();
        verify(readResult).getLines();
        verifyNoMoreInteractions(readResult);
        verify(entry).get(TestKey.ONE);
        verify(entry).reset();
        verify(entry).handle(lines1);
        verify(entry).defaultValue();
        verify(entry).inc(TestKey.ONE, 1);
        verifyNoMoreInteractions(entry);
    }

    @Test
    public void testCollectCached() throws Exception {
        final List<String> lines1 = Arrays.asList("1");
        final ReadResult readResult = spy(new ReadResult(lines1, 0));
        when(reader.read()).thenReturn(
                // new
                readResult,
                // cached
                readResult);
        final ProcfsEntry entry = spy(uut);

        entry.collect();
        entry.collect();

        verify(reader, times(2)).read();
        verifyNoMoreInteractions(reader);
        verify(readResult, times(2)).getReadTime();
        verify(readResult).getLines();
        verifyNoMoreInteractions(readResult);
        verify(entry).reset();
        verify(entry).handle(lines1);
        verify(entry).inc(TestKey.ONE, 1L);
        verifyNoMoreInteractions(entry);
    }

    @Test
    public void testCollectSharedReader() throws IOException {
        final List<String> lines1 = Arrays.asList("1");
        final List<String> lines2 = Arrays.asList("2");
        final ReadResult readResult1 = spy(new ReadResult(lines1, 0));
        final ReadResult readResult2 = spy(new ReadResult(lines2, 1));
        when(reader.read()).thenReturn(
                // new 1
                readResult1,
                // cached 1
                readResult1,
                // new 2
                readResult2,
                // cached 2
                readResult2);

        final ProcfsEntry entry1 = spy(uut);
        final ProcfsEntry entry2 = spy(new TestProcfsEntry(reader));

        entry1.collect();
        entry2.collect();

        verify(reader, times(2)).read();
        verifyNoMoreInteractions(reader);
        verify(readResult1, times(2)).getReadTime();
        verify(readResult1, times(2)).getLines();
        verifyNoMoreInteractions(readResult1);
        verify(entry1).reset();
        verify(entry1).handle(lines1);
        verify(entry1).inc(TestKey.ONE, 1L);
        verifyNoMoreInteractions(entry1);
        verify(entry2).reset();
        verify(entry2).handle(lines1);
        verify(entry2).inc(TestKey.ONE, 1L);
        verifyNoMoreInteractions(entry2);

        entry2.collect();
        entry1.collect();

        verify(reader, times(4)).read();
        verifyNoMoreInteractions(reader);
        verify(readResult2, times(4)).getReadTime();
        verify(readResult2, times(2)).getLines();
        verifyNoMoreInteractions(readResult2);
        verify(entry1, times(2)).reset();
        verify(entry1).handle(lines2);
        verify(entry1, times(2)).inc(TestKey.ONE, 1L);
        verifyNoMoreInteractions(entry1);
        verify(entry2, times(2)).reset();
        verify(entry2).handle(lines2);
        verify(entry2, times(2)).inc(TestKey.ONE, 1L);
        verifyNoMoreInteractions(entry2);
    }

    private enum TestKey implements ValueKey {
        ONE, TWO, THREE
    }

    private static class TestProcfsEntry extends ProcfsEntry {

        protected TestProcfsEntry(ProcfsReader reader) {
            super(reader);
        }

        @Override
        protected void reset() {
            EnumSet.allOf(TestKey.class).forEach(key -> values.put(key, new AtomicLong(-1)));
        }

        @Override
        protected void handle(Collection<String> lines) {
            Objects.requireNonNull(lines);

            switch (lines.size()) {
            case 0:
                break;
            case 1:
                inc(TestKey.ONE, 1);
                break;
            case 2:
                inc(TestKey.ONE, 1);
                inc(TestKey.TWO, 2);
                break;
            case 3:
                inc(TestKey.ONE, 1);
                inc(TestKey.TWO, 2);
                inc(TestKey.THREE, 3);
                break;
            default:
                throw new IllegalArgumentException(
                        "A maximum of 3 lines is supported. Fix your test!");
            }
        }

    }

}
