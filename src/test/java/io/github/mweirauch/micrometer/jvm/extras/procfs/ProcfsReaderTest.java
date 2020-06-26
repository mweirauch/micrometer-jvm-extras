/*
 * Copyright © 2016-2019 Michael Weirauch (michael.weirauch@gmail.com)
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

public class ProcfsReaderTest {

    private static Path BASE;

    private final List<String> consumedLines = new ArrayList<>();

    private final Consumer<String> consumer = (line) -> consumedLines.add(line);

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        BASE = Paths.get(ProcfsReaderTest.class.getResource("/procfs/").toURI());
    }

    @Test
    public void testNullContract() throws Exception {
        final ProcfsReader uut = new ProcfsReader(BASE, "smaps-001.txt");

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    public void testReadProcSelfNonExistant() throws Exception {
        final ProcfsReader uut = new ProcfsReader(BASE, "stub");

        assertThrows(NoSuchFileException.class, () -> uut.read(consumer));
    }

    @Test
    public void testNoOsSupport() throws Exception {
        System.setProperty("os.name", "SomeOS");
        final ProcfsReader uut = new ProcfsReader(BASE, "stub", false);

        uut.read(consumer);

        assertEquals(0, consumedLines.size());
    }

    @Test
    public void testRead() throws Exception {
        final ProcfsReader uut = new ProcfsReader(BASE, "smaps-001.txt");

        uut.read(consumer);

        assertEquals(17, consumedLines.size());
        assertEquals("Size:                  4 kB", consumedLines.get(1));
        assertEquals("Locked:                0 kB", consumedLines.get(16));
    }

    @Test
    public void testGetInstance() throws Exception {
        final ProcfsReader instance1 = ProcfsReader.getInstance("foo");
        final ProcfsReader instance2 = ProcfsReader.getInstance("foo");

        assertSame(instance1, instance2);

        final ProcfsReader instance3 = ProcfsReader.getInstance("bar");

        assertNotSame(instance3, instance1);
        assertNotSame(instance3, instance2);
    }

}
