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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ControlFileDescriptor;
import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ReadResult;

public class CgroupMemory {

    private static final Logger log = LoggerFactory.getLogger(CgroupMemory.class);

    // 0x7FFFFFFFFFFFF000L
    private static final String LIMIT_SOFT_UNLIMITED_CGROUP_V1 = "9223372036854771712";

    // 0x7FFFFFFFFFFFF000L
    private static final String LIMIT_HARD_UNLIMITED_CGROUP_V1 = "9223372036854771712";

    private static final String LIMIT_SOFT_UNLIMITED_CGROUP_V2 = "0";

    private static final String LIMIT_HARD_UNLIMITED_CGROUP_V2 = "max";

    private final CgroupReader reader;

    private static final class InstanceHolder {

        static final CgroupMemory INSTANCE = new CgroupMemory();

    }

    CgroupMemory() {
        this(CgroupReader.getInstance());
    }

    CgroupMemory(CgroupReader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    public static CgroupMemory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Value get(KEY key) {
        Objects.requireNonNull(key);

        ReadResult readResult = reader.read(key);

        return map(key, readResult);
    }

    private static Value map(KEY key, ReadResult readResult) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(readResult);

        Value value = Value.unsupported();

        switch (readResult.getStatus()) {
            case SUCCESS:
                String stringValue = readResult.getValue().orElse(null);

                if (isUnlimitedValue(key, stringValue)) {
                    value = Value.unlimited();
                } else {
                    try {
                        Long longValue = Long.parseLong(stringValue);
                        value = Value.resolved(longValue);
                    } catch (NumberFormatException e) {
                        log.warn("Failed parsing '{}'!", stringValue, e);
                        value = Value.unsupported();
                    }
                }
                break;
            case FAILURE:
            case UNSUPPORTED:
                value = Value.unsupported();
                break;
        }
        return value;
    }

    private static boolean isUnlimitedValue(KEY key, String stringValue) {
        Objects.requireNonNull(key);

        if (stringValue == null) {
            return false;
        }

        if (key == KEY.LIMIT_HARD || key == KEY.LIMIT_SWAP) {
            return LIMIT_HARD_UNLIMITED_CGROUP_V2.equals(stringValue) ||
                    LIMIT_HARD_UNLIMITED_CGROUP_V1.equals(stringValue);
        }

        if (key == KEY.LIMIT_SOFT) {
            return LIMIT_SOFT_UNLIMITED_CGROUP_V2.equals(stringValue) ||
                    LIMIT_SOFT_UNLIMITED_CGROUP_V1.equals(stringValue);
        }

        return false;
    }

    /**
     * Control file descriptors for memory-related cgroup control files.
     */
    public enum KEY implements ControlFileDescriptor {
        /**
         * Soft memory limit
         */
        LIMIT_SOFT(
                Paths.get("memory.soft_limit_in_bytes"), //
                Paths.get("memory.low")), //
        /**
         * Hard memory limit
         */
        LIMIT_HARD(
                Paths.get("memory.limit_in_bytes"), //
                Paths.get("memory.max")), //
        /**
         * Memory + swap limit (cgroup v1)
         * Swap limit (cgroup v2)
         */
        LIMIT_SWAP(
                Paths.get("memory.memsw.limit_in_bytes"), //
                Paths.get("memory.swap.max")); //

        private final Path controlFileV1;

        private final Path controlFileV2;

        KEY(Path controlFileV1, Path controlFileV2) {
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

    public static final class Value {

        public enum Status {
            RESOLVED,
            UNLIMITED,
            UNSUPPORTED,
        }

        private final Status status;

        private final Double doubleValue;

        private Value(Status status, double doubleValue) {
            this.status = Objects.requireNonNull(status);
            this.doubleValue = Objects.requireNonNull(doubleValue);
        }

        public Status getStatus() {
            return status;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public static Value resolved(double doubleValue) {
            return new Value(Status.RESOLVED, doubleValue);
        }

        public static Value unlimited() {
            return new Value(Status.UNLIMITED, 0.0d);
        }

        public static Value unsupported() {
            return new Value(Status.UNSUPPORTED, -1.0d);
        }

    }

}
