/*
 * Copyright © 2017-2022 Michael Weirauch (michael.weirauch@gmail.com)
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

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcfsStatus extends ProcfsEntry {

    public enum KEY implements ValueKey {
        /**
         * Threads
         */
        THREADS,
        /**
         * Virtual set size
         */
        VSS,
        /**
         * Resident set size
         */
        RSS,
        /**
         * Paged out memory
         */
        SWAP,
        /**
         * Voluntary content switches
         */
        VOLUNTARY_CTXT_SWITCHES,
        /**
         * Non-voluntary context switches
         */
        NONVOLUNTARY_CTXT_SWITCHES
    }

    private static final Pattern VAL_LINE_PATTERN = Pattern.compile("^\\w+:\\s+(\\d+)$");

    private static final Pattern KB_LINE_PATTERN = Pattern.compile("^\\w+:\\s+(\\d+)\\skB$");

    private static final int KILOBYTE = 1024;

    private static class InstanceHolder {

        /* default */ static final ProcfsStatus INSTANCE = new ProcfsStatus();

    }

    /* default */ ProcfsStatus() {
        super(ProcfsReader.getInstance("status"));
    }

    /* default */ ProcfsStatus(ProcfsReader reader) {
        super(reader);
    }

    @Override
    protected void handle(Map<ValueKey, Double> values, String line) {
        Objects.requireNonNull(values);
        Objects.requireNonNull(line);

        if (line.startsWith("Threads:")) {
            values.put(KEY.THREADS, parseValue(line));
        } else if (line.startsWith("VmSize:")) {
            values.put(KEY.VSS, parseKiloBytes(line) * KILOBYTE);
        } else if (line.startsWith("VmRSS:")) {
            values.put(KEY.RSS, parseKiloBytes(line) * KILOBYTE);
        } else if (line.startsWith("VmSwap:")) {
            values.put(KEY.SWAP, parseKiloBytes(line) * KILOBYTE);
        } else if (line.startsWith("voluntary_ctxt_switches:")) {
            values.put(KEY.VOLUNTARY_CTXT_SWITCHES, parseValue(line));
        } else if (line.startsWith("nonvoluntary_ctxt_switches:")) {
            values.put(KEY.NONVOLUNTARY_CTXT_SWITCHES, parseValue(line));
        }
    }

    private static Double parseValue(String line) {
        Objects.requireNonNull(line);

        final Matcher matcher = VAL_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Double.NaN;
        }

        return Double.parseDouble(matcher.group(1));
    }

    private static Double parseKiloBytes(String line) {
        Objects.requireNonNull(line);

        final Matcher matcher = KB_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Double.NaN;
        }

        return Double.parseDouble(matcher.group(1));
    }

    public static ProcfsStatus getInstance() {
        return InstanceHolder.INSTANCE;
    }

}
