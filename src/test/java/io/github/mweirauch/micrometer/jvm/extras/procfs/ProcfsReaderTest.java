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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsReader.ReadResult;

public class ProcfsReaderTest {

    private static Path BASE;

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

    @Test(expected = IOException.class)
    public void testReadProcSelfNonExistant() throws Exception {
        final ProcfsReader uut = spy(new ProcfsReader(BASE, "stub"));
        when(uut.read(anyLong())).thenCallRealMethod();
        when(uut.readPath(any())).thenThrow(new IOException("THROW"));

        uut.read();
    }

    @Test
    public void testRead() throws Exception {
        final ProcfsReader uut = new ProcfsReader(BASE, "smaps-001.txt");

        final ReadResult result = uut.read();

        assertNotNull(result);
        assertEquals(17, result.getLines().size());
        assertEquals("Size:                  4 kB", result.getLines().get(1));
        assertEquals("Locked:                0 kB", result.getLines().get(16));
    }

    @Test
    public void testCacheResultMissInitialAndSubsequent() throws IOException {
        final ProcfsReader uut = new ProcfsReader(BASE, "smaps-001.txt");
        final ProcfsReader spy = spy(uut);
        when(spy.currentTime()).thenReturn(1000L);

        ReadResult result = spy.read(1000L + ProcfsReader.CACHE_DURATION_MS + 10);

        assertEquals(1000L, result.getReadTime());
        assertEquals(1000L, spy.lastReadTime);
        assertEquals(spy.lastReadTime, result.getReadTime());

        when(spy.currentTime()).thenReturn(2000L);

        result = spy.read(spy.lastReadTime + ProcfsReader.CACHE_DURATION_MS + 10);

        assertEquals(2000L, result.getReadTime());
        assertEquals(2000L, spy.lastReadTime);
        assertEquals(spy.lastReadTime, result.getReadTime());

        verify(spy, times(2)).readPath(any());
        verify(spy, times(2)).cacheResult(any(), any());
    }

    @Test
    public void testCacheResultHit() throws IOException {
        final ProcfsReader uut = new ProcfsReader(BASE, "smaps-001.txt");
        final ProcfsReader spy = spy(uut);
        when(spy.currentTime()).thenReturn(1000L);

        ReadResult result = spy.read();

        assertEquals(1000L, result.getReadTime());
        assertEquals(1000L, spy.lastReadTime);
        assertEquals(spy.lastReadTime, result.getReadTime());

        when(spy.currentTime()).thenReturn(2000L);

        result = spy.read(spy.lastReadTime + ProcfsReader.CACHE_DURATION_MS - 10);

        assertEquals(1000L, result.getReadTime());
        assertEquals(1000L, spy.lastReadTime);
        assertEquals(spy.lastReadTime, result.getReadTime());

        verify(spy).readPath(any());
        verify(spy).cacheResult(any(), any());
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
