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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mweirauch.micrometer.jvm.extras.sysfs.CgroupReader.ControlFileDescriptor;

class CgroupLocator {

    enum CgroupType {
        INVALID,
        UNSUPPORTED,
        V1,
        V2,
    }

    private static final Logger log = LoggerFactory.getLogger(CgroupLocator.class);

    private static final Path FS_ROOT_DEFAULT = FileSystems.getDefault().getRootDirectories().iterator().next();

    private static final Pattern PROC_SELF_CGROUPS_LINE_PATTERN = Pattern.compile("^\\d+:([^:]+)?:(.+)$");

    private static final int SINGLE_CONTROLLER_SETUP = 1;

    private static final String ROOT_CONTROLLER_NAME = "_root_";

    private final CgroupInfo info;

    private static final class InstanceHolder {

        static final CgroupLocator INSTANCE = new CgroupLocator();

    }

    CgroupLocator() {
        this(FS_ROOT_DEFAULT);
    }

    CgroupLocator(Path fsRoot) {
        Objects.requireNonNull(fsRoot);
        this.info = detect(fsRoot);
    }

    static CgroupLocator getInstance() {
        return InstanceHolder.INSTANCE;
    }

    CgroupInfo getCgroupInfo() {
        return info;
    }

    boolean isCgroupSupported() {
        return info.getType() == CgroupType.V1 || info.getType() == CgroupType.V2;
    }

    Optional<Path> resolve(ControlFileDescriptor controlFileDescriptor) {
        final String controlFileName;
        if (info.getType() == CgroupType.V2) {
            controlFileName = controlFileDescriptor.controlFileV2().toString();
        } else if (info.getType() == CgroupType.V1) {
            controlFileName = controlFileDescriptor.controlFileV1().toString();
        } else {
            return Optional.empty();
        }

        String controllerName = ROOT_CONTROLLER_NAME;
        if (info.getControllerLocations().size() > SINGLE_CONTROLLER_SETUP) {
            // usually v1; derive controller name from control file name
            int dotIndex = controlFileName.indexOf('.');
            if (dotIndex != -1) {
                controllerName = controlFileName.substring(0, dotIndex);
            }
        }

        Optional<Path> controllerPath = Optional.ofNullable(info.getControllerLocations().get(controllerName));

        log.debug("Resolved path for controller '{}': '{}'", controllerName, controllerPath);

        return controllerPath.map(p -> p.resolve(controlFileName));
    }

    private static CgroupInfo detect(Path fsRoot) {
        Path procSelfCgroupPath = fsRoot.resolve("proc/self/cgroup");
        if (!procSelfCgroupPath.toFile().exists()) {
            log.debug("process cgroup not available at: '{}'", procSelfCgroupPath);
            return CgroupInfo.unsupported();
        }

        Path procSelfMountinfoPath = fsRoot.resolve("proc/self/mountinfo");
        if (!procSelfMountinfoPath.toFile().exists()) {
            log.debug("process mountinfo not available at: '{}'", procSelfMountinfoPath);
            return CgroupInfo.unsupported();
        }

        try {
            List<String> procSelfCgroupLines = Files.readAllLines(procSelfCgroupPath);
            if (procSelfCgroupLines.isEmpty()) {
                log.debug("process cgroup information is malformed");
                return CgroupInfo.unsupported();
            }

            final CgroupType type;
            String firstLine = procSelfCgroupLines.get(0);
            Matcher matcher = PROC_SELF_CGROUPS_LINE_PATTERN.matcher(firstLine);
            if (matcher.matches()) {
                String controllerList = matcher.group(1);
                if (controllerList == null || controllerList.isEmpty()) {
                    type = CgroupType.V2; // e.g. "0::/"
                } else {
                    type = CgroupType.V1; // e.g. "0:cpu,cpuacct:/"
                }
            } else {
                log.debug("process cgroup information is not understood");
                return CgroupInfo.unsupported();
            }

            Map<String, Path> controllerLocations = detectControllerLocations(fsRoot,
                    procSelfCgroupLines, procSelfMountinfoPath);

            log.debug("Detected cgroup version: {}", type);

            return CgroupInfo.of(type, controllerLocations);
        } catch (IOException e) {
            log.warn("Failed reading '{}'!", procSelfCgroupPath, e);
            return CgroupInfo.unsupported();
        }
    }

    private static Map<String, Path> detectControllerLocations(Path fsRoot, List<String> procSelfCgroupLines,
            Path procSelfMountinfoPath) throws IOException {
        Map<String, Path> controllerPaths = readCgroupControllerPaths(procSelfCgroupLines);

        if (log.isTraceEnabled()) {
            controllerPaths.forEach((k, v) -> log.trace("Cgroup controller path: {}={}", k, v));
        }

        Map<String, MountInfo> controllerMounts = readCgroupControllerMounts(procSelfMountinfoPath,
                controllerPaths.keySet());

        if (log.isTraceEnabled()) {
            controllerMounts.forEach((k, v) -> log.trace("Cgroup controller mount: {}={}", k, v));
        }

        final Map<String, Path> controllerLocations = buildControllerLocations(fsRoot, controllerPaths,
                controllerMounts);

        if (log.isTraceEnabled()) {
            controllerLocations.forEach((k, v) -> log.trace("Cgroup controller location: {}={}", k, v));
        }

        return controllerLocations;
    }

    @SuppressWarnings("squid:S135")
    private static Map<String, Path> buildControllerLocations(Path fsRoot, Map<String, Path> controllerPaths,
            Map<String, MountInfo> controllerMounts) {
        final Map<String, Path> controllerLocations = new HashMap<>();

        for (Map.Entry<String, Path> entry : controllerPaths.entrySet()) {
            String controllerName = entry.getKey();
            Path controllerPath = entry.getValue();
            MountInfo mountInfo = controllerMounts.get(controllerName);

            if (mountInfo == null) {
                // in v1 the collected "root" controller path doesn't have a mountinfo entry
                continue;
            }

            // resolve controller mount point against filesystem root
            // (basically only effective for testing)
            Path baseControllerLocation = fsRoot.resolve(stripLeadingSlash(mountInfo.getMountPoint()));

            // Process is in a cgroup namespace where paths are relative (shifted) to its
            // namespace root. No need to resolve against mount root as the path is already
            // namespace-relative.
            if (controllerPath.getNameCount() > 0
                    && controllerPath.equals(mountInfo.getMountRoot())) {
                controllerLocations.put(controllerName, baseControllerLocation);
                continue;
            }

            // append the cgroup controller path the cgroup mount point
            baseControllerLocation = baseControllerLocation.resolve(stripLeadingSlash(controllerPath));
            controllerLocations.put(controllerName, baseControllerLocation);
        }
        return controllerLocations;
    }

    private static Path stripLeadingSlash(Path path) {
        if (!path.isAbsolute()) {
            return path;
        }
        return FS_ROOT_DEFAULT.relativize(path);
    }

    private static Map<String, Path> readCgroupControllerPaths(List<String> procSelfCgroupLines) {
        Map<String, Path> controllerPaths = new HashMap<>();

        for (String procSelfCgroupEntry : procSelfCgroupLines) {
            Matcher matcher = PROC_SELF_CGROUPS_LINE_PATTERN.matcher(procSelfCgroupEntry);

            if (!matcher.matches()) {
                continue;
            }

            String controllerList = matcher.group(1);
            String controllerPath = matcher.group(2);

            if (controllerList == null) {
                // root controller (e.g. '0::/')
                controllerPaths.put(ROOT_CONTROLLER_NAME, Paths.get(controllerPath));
            } else {
                // one or more controllers under the same path (e.g. '6:cpu,cpuacct:/')
                String[] controllerNames = controllerList.split(",");
                for (String controllerName : controllerNames) {
                    controllerPaths.put(controllerName, Paths.get(controllerPath));
                }
            }
        }

        return controllerPaths;
    }

    @SuppressWarnings("squid:S135")
    private static Map<String, MountInfo> readCgroupControllerMounts(
            Path procSelfMountinfoPath, Set<String> knownControllers) throws IOException {
        Map<String, MountInfo> cgroupControllerMounts = new HashMap<>();

        List<String> procSelfMountInfoLines = Files.readAllLines(procSelfMountinfoPath);
        for (String procSelfMountInfoEntry : procSelfMountInfoLines) {
            String[] parts = procSelfMountInfoEntry.split(" ");
            if (parts.length < 9 || !parts[8].startsWith("cgroup")) {
                continue;
            }

            String mountRoot = parts[3];
            String mountPoint = parts[4];

            MountInfo mountInfo = new MountInfo(Paths.get(mountRoot), Paths.get(mountPoint));

            // don't iterate the mount options when our only known controller is the root
            // controller; break out as there will be no more suitable mount entries
            if (knownControllers.size() == 1 && knownControllers.contains(ROOT_CONTROLLER_NAME)) {
                cgroupControllerMounts.put(ROOT_CONTROLLER_NAME, mountInfo);
                break;
            }

            // filter options for known controller names - e.g. 'rw,seclabel,cpu,cpuacct'
            // (parts length can differ; always last)
            String options = parts[parts.length - 1];
            String[] optionsSplit = options.split(",");
            List<String> controllerNamesFromOptions = Arrays.stream(optionsSplit)
                    .filter(knownControllers::contains)
                    .collect(Collectors.toList());

            for (String controllerName : controllerNamesFromOptions) {
                cgroupControllerMounts.put(controllerName, mountInfo);
            }
        }

        return cgroupControllerMounts;
    }

    static final class CgroupInfo {

        private final CgroupType type;

        private final Map<String, Path> controllerLocations;

        private CgroupInfo(CgroupType type, Map<String, Path> controllerLocations) {
            this.type = Objects.requireNonNull(type);
            this.controllerLocations = Objects.requireNonNull(controllerLocations);
        }

        CgroupType getType() {
            return this.type;
        }

        Map<String, Path> getControllerLocations() {
            return this.controllerLocations;
        }

        static CgroupInfo unsupported() {
            return new CgroupInfo(CgroupType.UNSUPPORTED, Collections.emptyMap());
        }

        static CgroupInfo of(CgroupType type, Map<String, Path> controllerLocations) {
            return new CgroupInfo(type, controllerLocations);
        }

    }

    private static final class MountInfo {

        private final Path mountRoot;

        private final Path mountPoint;

        MountInfo(Path mountRoot, Path mountPoint) {
            this.mountRoot = Objects.requireNonNull(mountRoot);
            this.mountPoint = Objects.requireNonNull(mountPoint);
        }

        Path getMountRoot() {
            return mountRoot;
        }

        Path getMountPoint() {
            return mountPoint;
        }

        @Override
        public String toString() {
            return "MountInfo{" + "mountRoot='" + mountRoot + "\'" + ", mountPoint='" + mountPoint + "\'" + '}';
        }

    }

}
