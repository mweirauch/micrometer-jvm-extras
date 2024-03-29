/*
 * Copyright © 2019 Michael Weirauch (michael.weirauch@gmail.com)
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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ProcfsStatusBenchmark {

    private ProcfsStatus uub;

    public static void main(String[] args) throws RunnerException {
        final Options options = new OptionsBuilder() //
                .include(ProcfsStatusBenchmark.class.getSimpleName()) //
                .addProfiler(GCProfiler.class) //
                .build();

        new Runner(options).run();
    }

    @Setup
    public void setup() throws URISyntaxException {
        final ProcfsReader reader = new ProcfsReader(
                Paths.get(ProcfsStatusBenchmark.class.getResource("/procfs/").toURI()),
                "status-001.txt");
        uub = new ProcfsStatus(reader);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Fork(value = 5, warmups = 0)
    public void collectSingle() {
        uub.collect();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 3, time = 5, timeUnit = SECONDS)
    public void collectAverage() {
        uub.collect();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 0)
    @Threads(4)
    @Warmup(iterations = 3, time = 5, timeUnit = SECONDS)
    public void collectAverageContended() {
        uub.collect();
    }

}
