/*
 * Copyright © 2026 Michael Weirauch (michael.weirauch@gmail.com)
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
package io.github.mweirauch.micrometer.jvm.extras.sysfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ReadResult;

class CgroupReaderTest {

    @Test
    void shouldInstantiate() {
        CgroupReader uut = CgroupReader.getInstance();

        assertThat(uut).isNotNull();
    }

    @Test
    void shouldReturnSingletonInstance() {
        assertThat(CgroupReader.getInstance()).isSameAs(CgroupReader.getInstance());
    }

    @Test
    void shouldHandleUnsupportedCgroup() {
        CgroupLocator locator = mock(CgroupLocator.class);
        when(locator.isCgroupSupported()).thenReturn(false);

        CgroupReader uut = new CgroupReader(locator);

        ReadResult result = uut.read(StubKey.STUB);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CgroupReader.ReadStatus.UNSUPPORTED);
        assertThat(result.getValue()).isEmpty();
    }

    @Test
    void shouldHandleUnresolvedControlFile() {
        CgroupLocator locator = mock(CgroupLocator.class);
        when(locator.isCgroupSupported()).thenReturn(true);
        when(locator.resolve(StubKey.STUB)).thenReturn(Optional.empty());

        CgroupReader uut = new CgroupReader(locator);

        ReadResult result = uut.read(StubKey.STUB);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CgroupReader.ReadStatus.UNSUPPORTED);
        assertThat(result.getValue()).isEmpty();
    }

    @Test
    void shouldHandleExceptionDuringRead() {
        CgroupLocator locator = mock(CgroupLocator.class);
        when(locator.isCgroupSupported()).thenReturn(true);
        when(locator.resolve(StubKey.STUB)).thenReturn(Optional.of(Paths.get("nonexistent-file")));

        CgroupReader uut = new CgroupReader(locator);

        ReadResult result = uut.read(StubKey.STUB);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CgroupReader.ReadStatus.FAILURE);
        assertThat(result.getValue()).isEmpty();
    }

    @Test
    void shouldHandleUnsupportedControlFileContent() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            CgroupLocator locator = mock(CgroupLocator.class);
            when(locator.isCgroupSupported()).thenReturn(true);
            Path unsupportedContentPath = Paths.get("unsupported-content");
            when(locator.resolve(StubKey.STUB)).thenReturn(Optional.of(unsupportedContentPath));

            mockedFiles
                    .when(() -> Files.readAllLines(unsupportedContentPath))
                    .thenReturn(Arrays.asList("one", "two"));

            CgroupReader uut = new CgroupReader(locator);

            ReadResult result = uut.read(StubKey.STUB);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(CgroupReader.ReadStatus.FAILURE);
            assertThat(result.getValue()).isEmpty();
        }
    }

    private enum StubKey implements CgroupReader.ControlFileDescriptor {

        STUB;

        @Override
        public Path controlFileV1() {
            return Paths.get("stub-v1");
        }

        @Override
        public Path controlFileV2() {
            return Paths.get("stub-v2");
        }

    }

}
