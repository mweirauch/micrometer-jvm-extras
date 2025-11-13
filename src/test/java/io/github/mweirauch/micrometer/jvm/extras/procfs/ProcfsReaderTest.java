/*
 * Copyright © 2016-2025 Michael Weirauch (michael.weirauch@gmail.com)
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class ProcfsReaderTest {

    private static Path basePath;

    private final List<String> consumedLines = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        basePath = Paths.get(ProcfsReaderTest.class.getResource("/procfs/").toURI());
    }

    @Test
    public void testNullContract() {
        final ProcfsReader uut = new ProcfsReader(basePath, "status-001.txt");

        assertNotNull(uut);

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    public void testReadProcSelfNonExistant() {
        final ProcfsReader uut = new ProcfsReader(basePath, "stub");

        assertThrows(NoSuchFileException.class, () -> uut.read(consumedLines::add));
    }

    @Test
    public void testNoOsSupport() throws Exception {
        System.setProperty("os.name", "SomeOS");
        final ProcfsReader uut = new ProcfsReader(basePath, "stub", false);

        uut.read(consumedLines::add);

        assertEquals(0, consumedLines.size());
    }

    @Test
    public void testRead() throws Exception {
        final ProcfsReader uut = new ProcfsReader(basePath, "status-001.txt");

        uut.read(consumedLines::add);

        assertEquals(53, consumedLines.size());
        assertEquals("VmSize:\t 8474900 kB", consumedLines.get(17));
        assertEquals("VmRSS:\t 1007304 kB", consumedLines.get(21));
    }

    @Test
    public void testGetInstance() {
        final ProcfsReader instance1 = ProcfsReader.getInstance("foo");
        final ProcfsReader instance2 = ProcfsReader.getInstance("foo");

        assertSame(instance1, instance2);

        final ProcfsReader instance3 = ProcfsReader.getInstance("bar");

        assertNotSame(instance3, instance1);
        assertNotSame(instance3, instance2);
    }

}
