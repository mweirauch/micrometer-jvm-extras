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

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcfsStatus extends ProcfsEntry {

    public enum KEY implements ValueKey {
        /**
         * Threads
         */
        THREADS
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("^\\w+:\\s+(\\d+)$");

    public ProcfsStatus() {
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
        }
    }

    private static Double parseValue(String line) {
        Objects.requireNonNull(line);

        final Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Double.NaN;
        }

        return Double.parseDouble(matcher.group(1));
    }

}
