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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CgroupReader {

    private static final Logger log = LoggerFactory.getLogger(CgroupReader.class);

    private static final int EXPECTED_CONTROL_FILE_LINE_LENGTH = 1;

    private final CgroupLocator locator;

    private static final class InstanceHolder {

        static final CgroupReader INSTANCE = new CgroupReader(CgroupLocator.getInstance());

    }

    CgroupReader(CgroupLocator locator) {
        this.locator = Objects.requireNonNull(locator);
    }

    static CgroupReader getInstance() {
        return InstanceHolder.INSTANCE;
    }

    ReadResult read(ControlFileDescriptor controlFileDescriptor) {
        Objects.requireNonNull(controlFileDescriptor);

        if (!locator.isCgroupSupported()) {
            return ReadResult.unsupported();
        }

        return locator
                .resolve(controlFileDescriptor) //
                .map(CgroupReader::readFile) //
                .orElse(ReadResult.unsupported());
    }

    private static ReadResult readFile(Path controlFile) {
        Objects.requireNonNull(controlFile);

        log.trace("Reading '{}'", controlFile);

        List<String> lines;
        try {
            lines = Files.readAllLines(controlFile);
        } catch (IOException e) {
            return ReadResult.failure();
        }

        if (lines.size() != EXPECTED_CONTROL_FILE_LINE_LENGTH) {
            return ReadResult.failure();
        }

        return ReadResult.success(lines.get(0).trim());
    }

    /**
     * Descriptor for cgroup control files that provides the relative paths for
     * supported cgroup versions.
     */
    interface ControlFileDescriptor {
        Path controlFileV1();

        Path controlFileV2();
    }

    enum ReadStatus {
        SUCCESS,
        FAILURE,
        UNSUPPORTED,
    }

    static final class ReadResult {

        private final ReadStatus status;

        private final Optional<String> value;

        private ReadResult(ReadStatus status, String value) {
            this.status = Objects.requireNonNull(status);
            this.value = Optional.ofNullable(value);
        }

        Optional<String> getValue() {
            return value;
        }

        ReadStatus getStatus() {
            return status;
        }

        static ReadResult success(String value) {
            return new ReadResult(ReadStatus.SUCCESS, Objects.requireNonNull(value));
        }

        static ReadResult failure() {
            return new ReadResult(ReadStatus.FAILURE, null);
        }

        static ReadResult unsupported() {
            return new ReadResult(ReadStatus.UNSUPPORTED, null);
        }
    }

}
