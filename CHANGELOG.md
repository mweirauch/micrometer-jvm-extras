# Changelog

## [0.2.1](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.2.1) (2020-12-15)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/0.2.0...0.2.1)

Maintenance release.

**Merged pull requests:**

- Bump guava-testlib from 30.0-jre to 30.1-jre [\#109](https://github.com/mweirauch/micrometer-jvm-extras/pull/109) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jmh.version from 1.23 to 1.27 [\#106](https://github.com/mweirauch/micrometer-jvm-extras/pull/106) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump mockito-core from 3.3.3 to 3.6.28 [\#105](https://github.com/mweirauch/micrometer-jvm-extras/pull/105) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump micrometer-core from 1.1.15 to 1.1.19 [\#104](https://github.com/mweirauch/micrometer-jvm-extras/pull/104) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump guava-testlib from 29.0-jre to 30.0-jre [\#101](https://github.com/mweirauch/micrometer-jvm-extras/pull/101) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump junit from 4.13 to 4.13.1 [\#100](https://github.com/mweirauch/micrometer-jvm-extras/pull/100) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump basepom-oss from 29 to 31 [\#75](https://github.com/mweirauch/micrometer-jvm-extras/pull/75) ([mweirauch](https://github.com/mweirauch))
- Switch to Junit 4.13s assertThrows [\#74](https://github.com/mweirauch/micrometer-jvm-extras/pull/74) ([mweirauch](https://github.com/mweirauch))
- Bump micrometer-core from 1.1.7 to 1.1.15 [\#73](https://github.com/mweirauch/micrometer-jvm-extras/pull/73) ([mweirauch](https://github.com/mweirauch))
- Bump guava-testlib from 28.1-jre to 29.0-jre [\#68](https://github.com/mweirauch/micrometer-jvm-extras/pull/68) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 3.1.0 to 3.3.3 [\#64](https://github.com/mweirauch/micrometer-jvm-extras/pull/64) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.21 to 1.23 [\#60](https://github.com/mweirauch/micrometer-jvm-extras/pull/60) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit from 4.12 to 4.13 [\#58](https://github.com/mweirauch/micrometer-jvm-extras/pull/58) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.28 to 1.7.30 [\#56](https://github.com/mweirauch/micrometer-jvm-extras/pull/56) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [0.2.0](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.2.0) (2019-11-03)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/0.1.4...0.2.0)

This release removes support for the proportional set size metrics 'pss' and 'swappss'. They were quite expensive to collect and added to the metrics collection overhead quite noticeably with a CPU usage and memory allocation penalty. The go-to metrics 'rss' and 'swap' are still there of course.


**Implemented enhancements:**

- Sonar code analysis recommendations [\#47](https://github.com/mweirauch/micrometer-jvm-extras/pull/47) ([mweirauch](https://github.com/mweirauch))
- Drop support for proportional set size metrics \(pss, swappss\) [\#46](https://github.com/mweirauch/micrometer-jvm-extras/pull/46) ([mweirauch](https://github.com/mweirauch))

**Merged pull requests:**

- Bump mockito-core from 2.28.2 to 3.1.0 [\#43](https://github.com/mweirauch/micrometer-jvm-extras/pull/43) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump micrometer-core from 1.1.6 to 1.1.7 [\#42](https://github.com/mweirauch/micrometer-jvm-extras/pull/42) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump micrometer-core from 1.1.5 to 1.1.6 [\#41](https://github.com/mweirauch/micrometer-jvm-extras/pull/41) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump guava-testlib from 28.0-jre to 28.1-jre [\#40](https://github.com/mweirauch/micrometer-jvm-extras/pull/40) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.27 to 1.7.28 [\#39](https://github.com/mweirauch/micrometer-jvm-extras/pull/39) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.26 to 1.7.27 [\#38](https://github.com/mweirauch/micrometer-jvm-extras/pull/38) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump basepom-oss from 28 to 29 [\#35](https://github.com/mweirauch/micrometer-jvm-extras/pull/35) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump micrometer-core from 1.1.4 to 1.1.5 [\#33](https://github.com/mweirauch/micrometer-jvm-extras/pull/33) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump guava-testlib from 27.1-jre to 28.0-jre [\#32](https://github.com/mweirauch/micrometer-jvm-extras/pull/32) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [0.1.4](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.1.4) (2019-06-07)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/0.1.3...0.1.4)

**Implemented enhancements:**

- procfs: drop caching of read lines [\#30](https://github.com/mweirauch/micrometer-jvm-extras/pull/30) ([mweirauch](https://github.com/mweirauch))
- smaps+status: improve line matching performance \(+JMH benchmarks\) [\#22](https://github.com/mweirauch/micrometer-jvm-extras/pull/22) ([mweirauch](https://github.com/mweirauch))
- Push snapshot builds to Sonatype Nexus repository [\#16](https://github.com/mweirauch/micrometer-jvm-extras/pull/16) ([mweirauch](https://github.com/mweirauch))
- Increase procfs content cache duration to 1000ms [\#15](https://github.com/mweirauch/micrometer-jvm-extras/pull/15) ([mweirauch](https://github.com/mweirauch))

**Merged pull requests:**

- Bump mockito-core from 2.27.0 to 2.28.2 [\#29](https://github.com/mweirauch/micrometer-jvm-extras/pull/29) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.26.0 to 2.27.0 [\#28](https://github.com/mweirauch/micrometer-jvm-extras/pull/28) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump micrometer-core from 1.1.3 to 1.1.4 [\#27](https://github.com/mweirauch/micrometer-jvm-extras/pull/27) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.25.1 to 2.26.0 [\#26](https://github.com/mweirauch/micrometer-jvm-extras/pull/26) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Updated PMD ruleset from scratch for 6.x [\#31](https://github.com/mweirauch/micrometer-jvm-extras/pull/31) ([mweirauch](https://github.com/mweirauch))
- Bump mockito-core from 2.24.5 to 2.25.1 [\#25](https://github.com/mweirauch/micrometer-jvm-extras/pull/25) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump guava-testlib from 27.0.1-jre to 27.1-jre [\#24](https://github.com/mweirauch/micrometer-jvm-extras/pull/24) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump basepom-oss from 27 to 28 [\#21](https://github.com/mweirauch/micrometer-jvm-extras/pull/21) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.24.0 to 2.24.5 [\#20](https://github.com/mweirauch/micrometer-jvm-extras/pull/20) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j-api from 1.7.25 to 1.7.26 [\#19](https://github.com/mweirauch/micrometer-jvm-extras/pull/19) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j-simple from 1.7.25 to 1.7.26 [\#18](https://github.com/mweirauch/micrometer-jvm-extras/pull/18) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump micrometer-core from 1.1.2 to 1.1.3 [\#17](https://github.com/mweirauch/micrometer-jvm-extras/pull/17) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.23.4 to 2.24.0 [\#14](https://github.com/mweirauch/micrometer-jvm-extras/pull/14) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump micrometer-core from 1.1.1 to 1.1.2 [\#13](https://github.com/mweirauch/micrometer-jvm-extras/pull/13) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [0.1.3](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.1.3) (2019-01-08)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/0.1.2...0.1.3)

**Merged pull requests:**

- Bump guava-testlib from 23.6-jre to 27.0.1-jre [\#12](https://github.com/mweirauch/micrometer-jvm-extras/pull/12) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Update to micrometer-core:1.1.1 [\#11](https://github.com/mweirauch/micrometer-jvm-extras/pull/11) ([mweirauch](https://github.com/mweirauch))
- Travis CI: also build with openjdk11 [\#10](https://github.com/mweirauch/micrometer-jvm-extras/pull/10) ([mweirauch](https://github.com/mweirauch))
- update to basepom:25 [\#9](https://github.com/mweirauch/micrometer-jvm-extras/pull/9) ([mweirauch](https://github.com/mweirauch))
- Update to micrometer-core:1.0.0-rc.9 [\#8](https://github.com/mweirauch/micrometer-jvm-extras/pull/8) ([mweirauch](https://github.com/mweirauch))
- update to micrometer-core:1.0.0-rc.7 [\#7](https://github.com/mweirauch/micrometer-jvm-extras/pull/7) ([mweirauch](https://github.com/mweirauch))
- Travis CI updates [\#6](https://github.com/mweirauch/micrometer-jvm-extras/pull/6) ([mweirauch](https://github.com/mweirauch))

## [0.1.2](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.1.2) (2017-11-06)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/0.1.1...0.1.2)

**Merged pull requests:**

- update to micrometer-core:1.0.0-rc.3 [\#5](https://github.com/mweirauch/micrometer-jvm-extras/pull/5) ([mweirauch](https://github.com/mweirauch))

## [0.1.1](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.1.1) (2017-10-08)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/0.1.0...0.1.1)

**Implemented enhancements:**

- Meter for process threads [\#3](https://github.com/mweirauch/micrometer-jvm-extras/pull/3) ([mweirauch](https://github.com/mweirauch))

**Fixed bugs:**

- ProcessMemoryMetrics garbage-collected in Spring Configuration [\#1](https://github.com/mweirauch/micrometer-jvm-extras/issues/1)

**Merged pull requests:**

- update micrometer-core and mockito [\#4](https://github.com/mweirauch/micrometer-jvm-extras/pull/4) ([mweirauch](https://github.com/mweirauch))
- Simplify value handling [\#2](https://github.com/mweirauch/micrometer-jvm-extras/pull/2) ([mweirauch](https://github.com/mweirauch))

## [0.1.0](https://github.com/mweirauch/micrometer-jvm-extras/tree/0.1.0) (2017-09-20)

[Full Changelog](https://github.com/mweirauch/micrometer-jvm-extras/compare/32e6a76ced2684420fce441e98b50d84d7b5ec3f...0.1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
