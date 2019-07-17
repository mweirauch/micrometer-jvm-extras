# micrometer-jvm-extras

A set of additional JVM process metrics for [micrometer.io](https://micrometer.io/).

[![Apache License 2](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/mweirauch/micrometer-jvm-extras/master/LICENSE)
[![Travis CI](https://img.shields.io/travis/mweirauch/micrometer-jvm-extras/master.svg?maxAge=300)](https://travis-ci.org/mweirauch/micrometer-jvm-extras)
[![Codacy grade](https://img.shields.io/codacy/grade/3ace40206b314f72a690a00be45c9a5a.svg?maxAge=300)](https://www.codacy.com/app/mweirauch/micrometer-jvm-extras)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.mweirauch/micrometer-jvm-extras.svg?maxAge=300)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.mweirauch%22%20AND%20a%3A%22micrometer-jvm-extras%22)

## Motivation

* get "real" memory usage of the JVM beyond its managed parts
* get ahold of that info from within the JVM in environments where you can't
  instrument from the outside (e.g. PaaS)

## Usage

```xml
<dependency>
    <groupId>io.github.mweirauch</groupId>
    <artifactId>micrometer-jvm-extras</artifactId>
    <version>x.y.z</version>
</dependency>
```

```java
    /* Plain Java */
    final MeterRegistry registry = new SimpleMeterRegistry();
    new ProcessMemoryMetrics().bindTo(registry);
    new ProcessThreadMetrics().bindTo(registry);
```

```java
    /* With Spring */
    @Bean
    public MeterBinder processMemoryMetrics() {
        return new ProcessMemoryMetrics();
    }

    @Bean
    public MeterBinder processThreadMetrics() {
        return new ProcessThreadMetrics();
    }
```

## Available Metrics

### ProcessMemoryMetrics

`ProcessMemoryMetrics` reads process-level memory information from `/proc/self/status`.
All `Meter`s are reporting in `bytes`.

> Please note that `procfs` is only available on Linux-based systems.

* `process.memory.vss`: Virtual set size. The amount of virtual memory the process can access.
  Mostly irrelevant, but included for completeness sake.
* `process.memory.rss`: Resident set size. The amount of process memory currently in RAM.
* `process.memory.swap`: The amount of process memory paged out to swap.

> Starting with `0.2.0` support for proportional set size metrics (`pss` and `swappss`) will
> be dropped. The reasoning behind this is the overhead of processing the huge `/proc/self/smaps` file
> which in turn imposes a CPU, memory and query duration overhead in bigger processes.
> Instrumentation should be accurate and lightweight and the non-proportional set size metrics (`rss` and
> `swap`) provide these properties as they are still the de-facto go-to information with regard to
> memory utilization.

* `process.memory.pss`: Proportional set size. The amount of process memory currently in RAM,
  accounting for shared pages among processes. This metric is the most accurate in
  terms of "real" memory usage.
* `process.memory.swappss`: The amount of process memory paged out to swap accounting for
  shared memory among processes. Since Linux 4.3. Will return `-1` if your
  kernel is older. As with `pss`, the most accurate metric to watch.

### ProcessThreadMetrics

`ProcessThreadMetrics` reads process-level thread information from `/proc/self/status`.

> Please note that `procfs` is only available on Linux-based systems.

* `process.threads`: The number of process threads as seen by the operating system.

## Notes

* `procfs` data is cached for `1000ms` in order to relief the filesystem pressure
  when `Meter`s based on this data are queried by the registry one after
  another on collection run.
* Snapshot builds are pushed to [Sonatype Nexus Snapshot Repository](https://oss.sonatype.org/content/repositories/snapshots/io/github/mweirauch/micrometer-jvm-extras/) on successful `master` builds.
