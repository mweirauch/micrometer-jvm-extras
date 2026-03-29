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
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupLocator.CgroupType;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ControlFileDescriptor;

@SuppressWarnings("PMD.TooManyMethods")
class CgroupLocatorTest {

    @Test
    void shouldInstantiate() {
        CgroupLocator uut = CgroupLocator.getInstance();

        assertThat(uut).isNotNull();
    }

    @Test
    void shouldReturnSingletonInstance() {
        assertThat(CgroupLocator.getInstance()).isSameAs(CgroupLocator.getInstance());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "detection-issues/missing-procselfcgroup",
            "detection-issues/missing-procselfmountinfo",
            "detection-issues/empty-procselfcgroup",
            "detection-issues/unknown-procselfcgroup"
    })
    void shouldHandleDetectionIssues(String testCase) {
        CgroupLocator uut = unit(testRoot(testCase));

        assertThat(uut.isCgroupSupported()).isFalse();
        assertThat(uut.getCgroupInfo().getType()).isEqualTo(CgroupType.UNSUPPORTED);
    }

    @Test
    void shouldHandleNonMatchingProcSelfCgroupEntries() {
        CgroupLocator uut = unit(testRoot("detection-issues/nonmatching-procselfcgroup"));

        assertThat(uut.isCgroupSupported()).isTrue();
        assertThat(uut.getCgroupInfo().getType()).isEqualTo(CgroupType.V2);
    }

    @Test
    void shouldHandleIOExceptionDuringDetection() {
        Path testRoot = testRoot("v2/host-unlimited");
        Path procSelfCgroupPath = testRoot.resolve("proc/self/cgroup");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles
                    .when(() -> Files.readAllLines(procSelfCgroupPath))
                    .thenThrow(new IOException("Simulated IO error"));

            CgroupLocator uut = unit(testRoot);

            assertThat(uut.isCgroupSupported()).isFalse();
            assertThat(uut.getCgroupInfo().getType()).isEqualTo(CgroupType.UNSUPPORTED);
        }
    }

    @Test
    void shouldDetectCgroupV1() {
        CgroupLocator uut = unit(testRoot("v1/host-unlimited"));

        assertThat(uut.isCgroupSupported()).isTrue();
        assertThat(uut.getCgroupInfo().getType()).isEqualTo(CgroupType.V1);
    }

    @Test
    void shouldDetectCgroupV1Shifted() {
        CgroupLocator uut = unit(testRoot("v1/docker-hard-swap"));

        assertThat(uut.isCgroupSupported()).isTrue();
        assertThat(uut.getCgroupInfo().getType()).isEqualTo(CgroupType.V1);
    }

    @Test
    void shouldDetectCgroupV2() {
        CgroupLocator uut = unit(testRoot("v2/host-unlimited"));

        assertThat(uut.isCgroupSupported()).isTrue();
        assertThat(uut.getCgroupInfo().getType()).isEqualTo(CgroupType.V2);
    }

    @Test
    void shouldResolveNoPathOnUnsupportedCgroup() {
        CgroupLocator uut = unit(testRoot("detection-issues/missing-procselfcgroup"));

        assertThat(uut.isCgroupSupported()).isFalse();
        assertThat(uut.resolve(StubKey.MEMORY_FOO)).isEmpty();
    }

    @Test
    void shouldResolveCgroupV1ControlFile() {
        CgroupLocator uut = unit(testRoot("v1/host-hard"));

        assertThat(uut.isCgroupSupported()).isTrue();
        assertThat(uut.resolve(StubKey.MEMORY_FOO))
                .get(InstanceOfAssertFactories.PATH)
                .endsWithRaw(Paths.get("sys/fs/cgroup/memory/hard/memory.foo1"));
    }

    @Test
    void shouldResolveCgroupV2ControlFile() {
        CgroupLocator uut = unit(testRoot("v2/host-hard"));

        assertThat(uut.isCgroupSupported()).isTrue();
        assertThat(uut.resolve(StubKey.MEMORY_BAR))
                .get(InstanceOfAssertFactories.PATH)
                .endsWithRaw(Paths.get("sys/fs/cgroup/hard/memory.bar2"));
    }

    private static Path testRoot(String fixturePath) {
        try {
            return Paths.get(CgroupLocatorTest.class.getResource("/cgroup").toURI()).resolve(fixturePath);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Test setup failure: invalid resource URI", e);
        }
    }

    private static CgroupLocator unit(Path testRoot) {
        return new CgroupLocator(testRoot);
    }

    /**
     * ControlFileDescriptor stubs for testing. The controller name (derived from
     * the path before the first dot) must still match the test data, but the
     * control file itself must not exist.
     */
    private enum StubKey implements ControlFileDescriptor {

        MEMORY_FOO(
                Paths.get("memory.foo1"), //
                Paths.get("memory.foo2")), //
        MEMORY_BAR(
                Paths.get("memory.bar1"), //
                Paths.get("memory.bar2")); //

        private final Path controlFileV1;

        private final Path controlFileV2;

        StubKey(Path controlFileV1, Path controlFileV2) {
            this.controlFileV1 = Objects.requireNonNull(controlFileV1);
            this.controlFileV2 = Objects.requireNonNull(controlFileV2);
        }

        @Override
        public Path controlFileV1() {
            return controlFileV1;
        }

        @Override
        public Path controlFileV2() {
            return controlFileV2;
        }

    }

}
