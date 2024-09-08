/*
 * Copyright © 2016-2024 Michael Weirauch (michael.weirauch@gmail.com)
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsEntry.ValueKey;

public class ProcfsEntryTest {

    private final StubProcfsReader reader = spy(new StubProcfsReader());

    private final ProcfsEntry uut = new StubProcfsEntry(reader);

    @Test
    public void testNullContract() {
        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    public void testValueHandling() throws IOException {
        reader.addStubLines(Arrays.asList("1", "2", "3"));
        reader.addStubLines(Arrays.asList("1", "2"));

        final ProcfsEntry spy = spy(uut);
        when(spy.currentTime()).thenReturn(1000L);

        assertEquals(Double.valueOf(1), spy.get(StubKey.ONE));
        assertEquals(Double.valueOf(2), spy.get(StubKey.TWO));
        assertEquals(Double.valueOf(3), spy.get(StubKey.THREE));

        when(spy.currentTime()).thenReturn(2000L);

        assertEquals(Double.valueOf(1), spy.get(StubKey.ONE));
        assertEquals(Double.valueOf(2), spy.get(StubKey.TWO));
        assertEquals(Double.valueOf(-1), spy.get(StubKey.THREE));

        // 5 for the checks and 2 for the update of the last handling time + 1 because
        // of double-checked locking
        verify(spy, times(8)).currentTime();
        verify(reader, times(2)).read(any());
    }

    @Test
    public void testReaderFailure() throws IOException {
        doThrow(new IOException("fail")).when(reader).read(any());
        final ProcfsEntry spy = spy(uut);

        assertEquals(Double.valueOf(-1), spy.get(StubKey.ONE));
        assertEquals(Double.valueOf(-1), spy.get(StubKey.TWO));
        assertEquals(Double.valueOf(-1), spy.get(StubKey.THREE));

        verify(spy, times(3)).defaultValue();
    }

    private enum StubKey implements ValueKey {
        ONE, TWO, THREE
    }

    private static class StubProcfsEntry extends ProcfsEntry {

        protected StubProcfsEntry(ProcfsReader reader) {
            super(reader);
        }

        @Override
        protected void handle(Map<ValueKey, Double> values, String line) {
            Objects.requireNonNull(values);
            Objects.requireNonNull(line);

            switch (line) {
                case "1":
                    values.put(StubKey.ONE, 1D);
                    break;
                case "2":
                    values.put(StubKey.TWO, 2D);
                    break;
                case "3":
                    values.put(StubKey.THREE, 3D);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Only values '1' to '3' are supported. Fix your test!");
            }
        }

    }

    private static class StubProcfsReader extends ProcfsReader {

        private static final Logger log = LoggerFactory.getLogger(StubProcfsReader.class);

        private final List<List<String>> stubLines = new ArrayList<>();

        private int iterations;

        protected StubProcfsReader() {
            super(Paths.get("/"), "gone");
        }

        @Override
        protected void read(Consumer<String> consumer) throws IOException {
            if (log.isTraceEnabled()) {
                log.trace("Reading '{}'", getEntryPath());
            }

            stubLines.get(iterations++).forEach(consumer);
        }

        protected void addStubLines(List<String> lines) {
            this.stubLines.add(lines);
        }

    }

}
