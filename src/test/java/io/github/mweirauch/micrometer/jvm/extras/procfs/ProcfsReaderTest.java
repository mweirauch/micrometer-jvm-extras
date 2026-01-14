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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

class ProcfsReaderTest {

    private static Path basePath;

    private final List<String> consumedLines = new ArrayList<>();

    @BeforeAll
    static void beforeAll() throws URISyntaxException {
        basePath = Paths.get(ProcfsReaderTest.class.getResource("/procfs/").toURI());
    }

    @Test
    void shouldRejectNullParameters() {
        final ProcfsReader uut = new ProcfsReader(basePath, "status-001.txt");

        assertThat(uut).isNotNull();

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    void shouldThrowExceptionForNonexistentFile() {
        final ProcfsReader uut = new ProcfsReader(basePath, "stub");

        assertThatThrownBy(() -> uut.read(consumedLines::add))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void shouldSkipReadingOnUnsupportedOS() throws Exception {
        System.setProperty("os.name", "SomeOS");
        final ProcfsReader uut = new ProcfsReader(basePath, "stub", false);

        uut.read(consumedLines::add);

        assertThat(consumedLines).isEmpty();
    }

    @Test
    void shouldReadAllLinesFromStatusFile() throws Exception {
        final ProcfsReader uut = new ProcfsReader(basePath, "status-001.txt");

        uut.read(consumedLines::add);

        assertThat(consumedLines).hasSize(53);
        assertThat(consumedLines.get(17)).isEqualTo("VmSize:\t 8474900 kB");
        assertThat(consumedLines.get(21)).isEqualTo("VmRSS:\t 1007304 kB");
    }

    @Test
    void shouldCacheInstancesByEntryName() {
        final ProcfsReader instance1 = ProcfsReader.getInstance("foo");
        final ProcfsReader instance2 = ProcfsReader.getInstance("foo");

        assertThat(instance1).isSameAs(instance2);

        final ProcfsReader instance3 = ProcfsReader.getInstance("bar");

        assertThat(instance3)
                .isNotSameAs(instance1)
                .isNotSameAs(instance2);
    }

}
