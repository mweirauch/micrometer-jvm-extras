/*
 * Copyright © 2017-2026 Michael Weirauch (michael.weirauch@gmail.com)
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
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import io.github.mweirauch.micrometer.jvm.extras.procfs.ProcfsStatus.KEY;

class ProcfsStatusTest {

    private static Path basePath;

    @BeforeAll
    static void beforeAll() throws URISyntaxException {
        basePath = Paths.get(ProcfsStatusTest.class.getResource("/procfs/").toURI());
    }

    @Test
    void shouldRejectNullParameters() {
        final ProcfsStatus uut = new ProcfsStatus(mock(ProcfsReader.class));

        assertThat(uut).isNotNull();

        final NullPointerTester npt = new NullPointerTester();

        npt.testConstructors(uut.getClass(), Visibility.PACKAGE);
        npt.testStaticMethods(uut.getClass(), Visibility.PACKAGE);
        npt.testInstanceMethods(uut, Visibility.PACKAGE);
    }

    @Test
    void shouldReturnSingletonInstance() {
        assertThat(ProcfsStatus.getInstance()).isSameAs(ProcfsStatus.getInstance());
    }

    @Test
    void shouldParseStatusFileCorrectly() {
        final ProcfsStatus uut = new ProcfsStatus(new ProcfsReader(basePath, "status-001.txt"));

        assertThat(uut.get(KEY.THREADS)).isEqualTo(55.0);
        assertThat(uut.get(KEY.VSS)).isEqualTo(8678297600L);
        assertThat(uut.get(KEY.RSS)).isEqualTo(1031479296L);
        assertThat(uut.get(KEY.SWAP)).isEqualTo(0.0);
        assertThat(uut.get(KEY.VOLUNTARY_CTXT_SWITCHES)).isEqualTo(4.0);
        assertThat(uut.get(KEY.NONVOLUNTARY_CTXT_SWITCHES)).isEqualTo(1.0);
    }

    @Test
    void shouldReturnDefaultOnReadFailure() throws IOException {
        final ProcfsReader reader = mock(ProcfsReader.class);
        doThrow(new IOException("fail")).when(reader).read(any());

        final ProcfsStatus uut = new ProcfsStatus(reader);

        assertThat(uut.get(KEY.THREADS)).isEqualTo(-1.0);
    }

}
