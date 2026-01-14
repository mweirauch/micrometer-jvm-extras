/*
 * Copyright © 2016-2026 Michael Weirauch (michael.weirauch@gmail.com)
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsEntry.ValueKey;

class ProcfsEntryTest {

    private final StubProcfsReader reader = spy(new StubProcfsReader());

    private final ProcfsEntry uut = new StubProcfsEntry(reader);

    @Test
    void shouldRejectNullParameters() {
        final NullPointerTester npt = new NullPointerTester();

        assertThat(uut).isNotNull();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    void shouldCacheValuesAndRefreshWhenDataChanges() throws IOException {
        reader.addStubLines(Arrays.asList("1", "2", "3"));
        reader.addStubLines(Arrays.asList("1", "2"));

        final ProcfsEntry spy = spy(uut);
        when(spy.currentTime()).thenReturn(1000L);

        assertThat(spy.get(StubKey.ONE)).isEqualTo(1.0);
        assertThat(spy.get(StubKey.TWO)).isEqualTo(2.0);
        assertThat(spy.get(StubKey.THREE)).isEqualTo(3.0);

        // advance time within the caching window
        when(spy.currentTime()).thenReturn(1100L);

        assertThat(spy.get(StubKey.ONE)).isEqualTo(1.0);
        assertThat(spy.get(StubKey.TWO)).isEqualTo(2.0);
        assertThat(spy.get(StubKey.THREE)).isEqualTo(3.0);

        // advance time beyond the caching window
        when(spy.currentTime()).thenReturn(2000L);

        assertThat(spy.get(StubKey.ONE)).isEqualTo(1.0);
        assertThat(spy.get(StubKey.TWO)).isEqualTo(2.0);
        assertThat(spy.get(StubKey.THREE)).isEqualTo(-1.0);

        // 8 for the checks and 2 for the update of the last handling time + 1 because
        // of double-checked locking
        verify(spy, times(11)).currentTime();
        verify(reader, times(2)).read(any());
    }

    @Test
    void shouldReturnDefaultValueWhenReadingFails() throws IOException {
        doThrow(new IOException("fail")).when(reader).read(any());
        final ProcfsEntry spy = spy(uut);

        assertThat(spy.get(StubKey.ONE)).isEqualTo(-1.0);
        assertThat(spy.get(StubKey.TWO)).isEqualTo(-1.0);
        assertThat(spy.get(StubKey.THREE)).isEqualTo(-1.0);

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
