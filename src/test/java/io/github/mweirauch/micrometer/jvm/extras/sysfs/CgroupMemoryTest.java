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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupMemory.KEY;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupMemory.Value;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ReadResult;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ReadStatus;

@SuppressWarnings({ "PMD.TooManyMethods", "squid:S5976" })
class CgroupMemoryTest {

    @Test
    void shouldInstantiate() {
        CgroupMemory uut = CgroupMemory.getInstance();

        assertThat(uut).isNotNull();
    }

    @Test
    void shouldReturnSingletonInstance() {
        assertThat(CgroupMemory.getInstance()).isSameAs(CgroupMemory.getInstance());
    }

    @Test
    void shouldHandleErroneousSuccessReadResult() {
        CgroupReader reader = mock(CgroupReader.class);

        // construct "theoretical" failure result (technically not possible)
        ReadResult readResult = mock(ReadResult.class);
        when(readResult.getStatus()).thenReturn(ReadStatus.SUCCESS);
        when(readResult.getValue()).thenReturn(Optional.empty());

        when(reader.read(any(CgroupReader.ControlFileDescriptor.class)))
                .thenReturn(readResult);

        CgroupMemory uut = new CgroupMemory(reader);

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNSUPPORTED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(-1.0d);
    }

    @Test
    void shouldHandleUnparsableSuccessReadResult() {
        CgroupReader reader = mock(CgroupReader.class);

        ReadResult readResult = mock(ReadResult.class);
        when(readResult.getStatus()).thenReturn(ReadStatus.SUCCESS);
        when(readResult.getValue()).thenReturn(Optional.of("not-a-number"));

        when(reader.read(any(CgroupReader.ControlFileDescriptor.class)))
                .thenReturn(readResult);

        CgroupMemory uut = new CgroupMemory(reader);

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNSUPPORTED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(-1.0d);
    }

    @Test
    void shouldHandleFailedReadResult() {
        CgroupReader reader = mock(CgroupReader.class);

        when(reader.read(any(CgroupReader.ControlFileDescriptor.class)))
                .thenReturn(ReadResult.failure());

        CgroupMemory uut = new CgroupMemory(reader);

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNSUPPORTED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(-1.0d);
    }

    @Test
    void shouldHandleUnsupportedReadResult() {
        CgroupReader reader = mock(CgroupReader.class);

        when(reader.read(any(CgroupReader.ControlFileDescriptor.class)))
                .thenReturn(ReadResult.unsupported());

        CgroupMemory uut = new CgroupMemory(reader);

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNSUPPORTED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(-1.0d);
    }

    @Test
    void shouldHandleV1HostUnlimitedMemory() throws Exception {
        CgroupMemory uut = unit("v1/host-unlimited");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(0.0d);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(0.0d);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(0.0d);
    }

    @Test
    void shouldHandleV2HostUnlimitedMemory() throws Exception {
        CgroupMemory uut = unit("v2/host-unlimited");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(0.0d);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(0.0d);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(0.0d);
    }

    @Test
    void shouldHandleV1HostLimitedMemory() throws Exception {
        CgroupMemory uut = unit("v1/host-soft-hard-swap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(536870912L);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(805306368L);
    }

    @Test
    void shouldHandleV2HostLimitedMemory() throws Exception {
        CgroupMemory uut = unit("v2/host-soft-hard-swap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(536870912L);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(805306368L);
    }

    @Test
    void shouldHandleV1HostLimitedMemoryNoSwap() throws Exception {
        CgroupMemory uut = unit("v1/host-soft-hard-noswap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(536870912L);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        // v1 behaviour: (memory) + (memory-swap - memory) = 640m + (640m - 640m) = 640m
        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(671088640L);
    }

    @Test
    void shouldHandleV2HostLimitedMemoryNoSwap() throws Exception {
        CgroupMemory uut = unit("v2/host-soft-hard-noswap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(536870912L);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isZero();
    }

    @Test
    void shouldHandleV1HostLimitedMemoryHardOnly() throws Exception {
        CgroupMemory uut = unit("v1/host-hard");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSoft.getDoubleValue()).isZero();

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSwap.getDoubleValue()).isZero();
    }

    @Test
    void shouldHandleV2HostLimitedMemoryHardOnly() throws Exception {
        CgroupMemory uut = unit("v2/host-hard");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSoft.getDoubleValue()).isZero();

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSwap.getDoubleValue()).isZero();
    }

    /*
     * French PaaS provider; Ubuntu 20.04 LTS; 256MB container
     */
    @Test
    void shouldHandleV1DockerLimitedMemoryHardSwap() throws Exception {
        CgroupMemory uut = unit("v1/docker-hard-swap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.UNLIMITED);
        assertThat(valueSoft.getDoubleValue()).isZero();

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(268435456L);

        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(536870912L);
    }

    /*
     * Fedora 30; Docker 1.13.1
     * docker run --rm -ti --memory-reservation 512m --memory 640m --memory-swap
     * 768m alpine:3.3
     */
    @Test
    void shouldHandleV1DockerLimitedMemory() throws Exception {
        CgroupMemory uut = unit("v1/docker-soft-hard-swap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(536870912L);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        // v1 behaviour: (memory) + (memory-swap - memory) = 640m + (768m - 640m) = 768m
        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(805306368L);
    }

    /*
     * Fedora 41; Docker 27.3.1
     * docker run --rm -ti --memory-reservation 512m --memory 640m --memory-swap
     * 768m alpine
     */
    @Test
    void shouldHandleV2DockerLimitedMemory() throws Exception {
        CgroupMemory uut = unit("v2/docker-soft-hard-swap");

        Value valueSoft = uut.get(KEY.LIMIT_SOFT);
        Value valueHard = uut.get(KEY.LIMIT_HARD);
        Value valueSwap = uut.get(KEY.LIMIT_SWAP);

        assertThat(valueSoft.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSoft.getDoubleValue()).isEqualTo(536870912L);

        assertThat(valueHard.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueHard.getDoubleValue()).isEqualTo(671088640L);

        // v2 behaviour: (memory-swap) - (memory) = 768m - 640m = 128m
        assertThat(valueSwap.getStatus()).isEqualTo(CgroupMemory.Value.Status.RESOLVED);
        assertThat(valueSwap.getDoubleValue()).isEqualTo(134217728L);
    }

    private static CgroupMemory unit(String fixturePath) throws URISyntaxException {
        return new CgroupMemory(reader(fixturePath));
    }

    private static CgroupReader reader(String fixturePath) throws URISyntaxException {
        Path root = Paths.get(CgroupMemoryTest.class.getResource("/cgroup").toURI()).resolve(fixturePath);
        return new CgroupReader(new CgroupLocator(root));
    }

}
