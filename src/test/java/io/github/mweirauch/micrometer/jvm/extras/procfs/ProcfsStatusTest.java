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
package io.github.mweirauch.micrometer.jvm.extras.procfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus.KEY;

public class ProcfsStatusTest {

    private static Path BASE;

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        BASE = Paths.get(ProcfsStatusTest.class.getResource("/procfs/").toURI());
    }

    @Test
    public void testNullContract() {
        final ProcfsStatus uut = new ProcfsStatus(mock(ProcfsReader.class));

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    public void testInstantiation() {
        assertSame(ProcfsStatus.getInstance(), ProcfsStatus.getInstance());
    }

    @Test
    public void testSimple() {
        final ProcfsStatus uut = new ProcfsStatus(new ProcfsReader(BASE, "status-001.txt"));

        assertEquals(Double.valueOf(55), uut.get(KEY.THREADS));
        assertEquals(Double.valueOf(8678297600L), uut.get(KEY.VSS));
        assertEquals(Double.valueOf(1031479296L), uut.get(KEY.RSS));
        assertEquals(Double.valueOf(0), uut.get(KEY.SWAP));
    }

    @Test
    public void testReturnDefaultValuesOnReaderFailure() throws IOException {
        final ProcfsReader reader = mock(ProcfsReader.class);
        doThrow(new IOException("fail")).when(reader).read(any());

        final ProcfsStatus uut = new ProcfsStatus(reader);

        assertEquals(Double.valueOf(-1), uut.get(KEY.THREADS));
    }

}
