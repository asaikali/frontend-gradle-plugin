package org.siouan.frontendgradleplugin.infrastructure.gradle;

import static java.util.Arrays.asList;
import static org.siouan.frontendgradleplugin.test.util.GradleBuildAssertions.assertTaskFailed;
import static org.siouan.frontendgradleplugin.test.util.GradleBuildAssertions.assertTaskSkipped;
import static org.siouan.frontendgradleplugin.test.util.GradleBuildAssertions.assertTaskSuccess;
import static org.siouan.frontendgradleplugin.test.util.GradleBuildAssertions.assertTaskUpToDate;
import static org.siouan.frontendgradleplugin.test.util.GradleBuildFiles.createBuildFile;
import static org.siouan.frontendgradleplugin.test.util.GradleHelper.runGradle;
import static org.siouan.frontendgradleplugin.test.util.GradleHelper.runGradleAndExpectFailure;
import static org.siouan.frontendgradleplugin.test.util.Resources.getResourcePath;
import static org.siouan.frontendgradleplugin.test.util.Resources.getResourceUrl;
import static org.siouan.frontendgradleplugin.test.util.TaskTypes.buildNodeTaskDefinition;
import static org.siouan.frontendgradleplugin.test.util.TaskTypes.buildNpmYarnTaskDefinition;
import static org.siouan.frontendgradleplugin.test.util.TaskTypes.buildNpxTaskDefinition;

import java.io.IOException;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.siouan.frontendgradleplugin.FrontendGradlePlugin;
import org.siouan.frontendgradleplugin.test.util.FrontendMapBuilder;

/**
 * Functional tests to verify the {@link RunNode} task type, the {@link RunNpx} task type, the {@link RunNpmYarn} task
 * type in a Gradle build.  Test cases uses fake Node.js/Yarn distributions, to avoid the download overhead. The 'yarn'
 * and 'npm' executables in these distributions simply call the 'node' executable with the same arguments.
 */
class TaskTypesWithGlobalDistributionsFuncTest {

    private static final String RUN_NODE_TASK_NAME = "myRunNodeTask";

    private static final String RUN_NPM_YARN_TASK_NAME = "myRunNpmYarnTask";

    private static final String RUN_NPX_TASK_NAME = "myRunNpxTask";

    @TempDir
    Path temporaryDirectoryPath;

    private Path projectDirectoryPath;

    private Path temporaryScriptPath;

    @BeforeEach
    void setUp() {
        projectDirectoryPath = temporaryDirectoryPath;
        temporaryScriptPath = temporaryDirectoryPath.resolve("script.js");
    }

    @Test
    void shouldFailRunningCustomTasksWhenNodeExecutableDoesNotExist() throws IOException {
        final FrontendMapBuilder frontendMapBuilder = new FrontendMapBuilder().nodeDistributionProvided(true);
        final String runNodeTaskDefinition = buildNodeTaskDefinition(RUN_NODE_TASK_NAME,
            temporaryScriptPath.toString().replace("\\", "\\\\"));
        final String runNpxTaskDefinition = buildNpxTaskDefinition(RUN_NPX_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "--version");
        final String runNpmYarnTaskDefinition = buildNpmYarnTaskDefinition(RUN_NPM_YARN_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "run another-script");
        createBuildFile(projectDirectoryPath, frontendMapBuilder.toMap(),
            String.join("\n", runNodeTaskDefinition, runNpxTaskDefinition, runNpmYarnTaskDefinition));
        final Path nodejsHomePath = getResourcePath("node-dist-without-node");

        final BuildResult result1 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, null,
            RUN_NODE_TASK_NAME);

        assertTaskSkipped(result1, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskFailed(result1, RUN_NODE_TASK_NAME);

        final BuildResult result2 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, null,
            RUN_NPX_TASK_NAME);

        assertTaskSkipped(result2, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskFailed(result2, RUN_NPX_TASK_NAME);

        final BuildResult result3 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, null,
            RUN_NPM_YARN_TASK_NAME);

        assertTaskSkipped(result3, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskFailed(result3, RUN_NPM_YARN_TASK_NAME);
    }

    @Test
    void shouldFailRunningCustomTasksWhenNpxExecutableDoesNotExist() throws IOException {
        final FrontendMapBuilder frontendMapBuilder = new FrontendMapBuilder().nodeDistributionProvided(true);
        final String runNodeTaskDefinition = buildNodeTaskDefinition(RUN_NODE_TASK_NAME,
            temporaryScriptPath.toString().replace("\\", "\\\\"));
        final String runNpxTaskDefinition = buildNpxTaskDefinition(RUN_NPX_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "--version");
        final String runNpmYarnTaskDefinition = buildNpmYarnTaskDefinition(RUN_NPM_YARN_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "run another-script");
        createBuildFile(projectDirectoryPath, frontendMapBuilder.toMap(),
            String.join("\n", runNodeTaskDefinition, runNpxTaskDefinition, runNpmYarnTaskDefinition));
        final Path nodejsHomePath = getResourcePath("node-dist-without-npx");

        final BuildResult result1 = runGradle(projectDirectoryPath, nodejsHomePath, null, RUN_NODE_TASK_NAME);

        assertTaskSkipped(result1, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result1, RUN_NODE_TASK_NAME);

        final BuildResult result2 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, null,
            RUN_NPX_TASK_NAME);

        assertTaskSkipped(result2, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskFailed(result2, RUN_NPX_TASK_NAME);

        final BuildResult result3 = runGradle(projectDirectoryPath, nodejsHomePath, null, RUN_NPM_YARN_TASK_NAME);

        assertTaskSkipped(result3, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result3, RUN_NPM_YARN_TASK_NAME);
    }

    @Test
    void shouldFailRunningCustomTasksWhenNpmExecutableDoesNotExist() throws IOException {
        final FrontendMapBuilder frontendMapBuilder = new FrontendMapBuilder().nodeDistributionProvided(true);
        final String runNodeTaskDefinition = buildNodeTaskDefinition(RUN_NODE_TASK_NAME,
            temporaryScriptPath.toString().replace("\\", "\\\\"));
        final String runNpxTaskDefinition = buildNpxTaskDefinition(RUN_NPX_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "--version");
        final String runNpmYarnTaskDefinition = buildNpmYarnTaskDefinition(RUN_NPM_YARN_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "run another-script");
        createBuildFile(projectDirectoryPath, frontendMapBuilder.toMap(),
            String.join("\n", runNodeTaskDefinition, runNpxTaskDefinition, runNpmYarnTaskDefinition));
        final Path nodejsHomePath = getResourcePath("node-dist-without-npm");

        final BuildResult result1 = runGradle(projectDirectoryPath, nodejsHomePath, null, RUN_NODE_TASK_NAME);

        assertTaskSkipped(result1, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result1, RUN_NODE_TASK_NAME);

        final BuildResult result2 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, null,
            RUN_NPX_TASK_NAME);

        assertTaskSkipped(result2, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        // Failure because npx requires npm.
        assertTaskFailed(result2, RUN_NPX_TASK_NAME);

        final BuildResult result3 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, null,
            RUN_NPM_YARN_TASK_NAME);

        assertTaskSkipped(result3, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskFailed(result3, RUN_NPM_YARN_TASK_NAME);
    }

    @Test
    void shouldFailRunningCustomTasksWhenYarnExecutableDoesNotExist() throws IOException {
        final FrontendMapBuilder frontendMapBuilder = new FrontendMapBuilder()
            .nodeVersion("12.18.3")
            .nodeDistributionUrl(getResourceUrl("node-v12.18.3.zip"))
            .yarnEnabled(true)
            .yarnDistributionProvided(true);
        final String runNodeTaskDefinition = buildNodeTaskDefinition(RUN_NODE_TASK_NAME,
            temporaryScriptPath.toString().replace("\\", "\\\\"));
        final String runNpmYarnTaskDefinition = buildNpmYarnTaskDefinition(RUN_NPM_YARN_TASK_NAME,
            asList(FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, FrontendGradlePlugin.YARN_INSTALL_TASK_NAME),
            "run another-script");
        createBuildFile(projectDirectoryPath, frontendMapBuilder.toMap(),
            String.join("\n", runNodeTaskDefinition, runNpmYarnTaskDefinition));
        final Path nodejsHomePath = getResourcePath("node-dist-provided");
        final Path yarnHomePath = projectDirectoryPath.resolve("yarn-dist");

        final BuildResult result1 = runGradle(projectDirectoryPath, nodejsHomePath, yarnHomePath, RUN_NODE_TASK_NAME);

        assertTaskSuccess(result1, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result1, RUN_NODE_TASK_NAME);

        final BuildResult result2 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, yarnHomePath,
            RUN_NPM_YARN_TASK_NAME);

        assertTaskUpToDate(result2, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSkipped(result2, FrontendGradlePlugin.YARN_INSTALL_TASK_NAME);
        assertTaskFailed(result2, RUN_NPM_YARN_TASK_NAME);
    }

    @Test
    void shouldRunCustomTasks() throws IOException {
        final FrontendMapBuilder frontendMapBuilder = new FrontendMapBuilder().nodeDistributionProvided(true);
        final String runNodeTaskDefinition = buildNodeTaskDefinition(RUN_NODE_TASK_NAME,
            temporaryScriptPath.toString().replace("\\", "\\\\"));
        final String runNpxTaskDefinition = buildNpxTaskDefinition(RUN_NPX_TASK_NAME,
            FrontendGradlePlugin.NODE_INSTALL_TASK_NAME, "--version");
        final String runNpmYarnTaskDefinition = buildNpmYarnTaskDefinition(RUN_NPM_YARN_TASK_NAME,
            FrontendGradlePlugin.INSTALL_TASK_NAME, "run another-script");
        createBuildFile(projectDirectoryPath, frontendMapBuilder.toMap(),
            String.join("\n", runNodeTaskDefinition, runNpxTaskDefinition, runNpmYarnTaskDefinition));
        final Path nodejsHomePath = getResourcePath("node-dist-provided");
        final Path yarnHomePath = getResourcePath("yarn-dist-provided");

        final BuildResult result1 = runGradle(projectDirectoryPath, nodejsHomePath, yarnHomePath, RUN_NODE_TASK_NAME);

        assertTaskSkipped(result1, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result1, RUN_NODE_TASK_NAME);

        final BuildResult result2 = runGradle(projectDirectoryPath, nodejsHomePath, yarnHomePath, RUN_NPX_TASK_NAME);

        assertTaskSkipped(result2, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result2, RUN_NPX_TASK_NAME);

        final BuildResult result3 = runGradle(projectDirectoryPath, nodejsHomePath, yarnHomePath,
            RUN_NPM_YARN_TASK_NAME);

        assertTaskSkipped(result3, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSkipped(result3, FrontendGradlePlugin.YARN_INSTALL_TASK_NAME);
        assertTaskSuccess(result3, FrontendGradlePlugin.INSTALL_TASK_NAME);
        assertTaskSuccess(result3, RUN_NPM_YARN_TASK_NAME);

        frontendMapBuilder.yarnEnabled(true).yarnDistributionProvided(true);
        createBuildFile(projectDirectoryPath, frontendMapBuilder.toMap(),
            String.join("\n", runNodeTaskDefinition, runNpxTaskDefinition, runNpmYarnTaskDefinition));

        final BuildResult result4 = runGradle(projectDirectoryPath, nodejsHomePath, yarnHomePath, RUN_NODE_TASK_NAME);

        assertTaskSkipped(result4, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSuccess(result4, RUN_NODE_TASK_NAME);

        final BuildResult result5 = runGradleAndExpectFailure(projectDirectoryPath, nodejsHomePath, yarnHomePath,
            RUN_NPX_TASK_NAME);

        assertTaskSkipped(result5, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskFailed(result5, RUN_NPX_TASK_NAME);

        final BuildResult result6 = runGradle(projectDirectoryPath, nodejsHomePath, yarnHomePath,
            RUN_NPM_YARN_TASK_NAME);

        assertTaskSkipped(result6, FrontendGradlePlugin.NODE_INSTALL_TASK_NAME);
        assertTaskSkipped(result6, FrontendGradlePlugin.YARN_INSTALL_TASK_NAME);
        assertTaskSuccess(result6, FrontendGradlePlugin.INSTALL_TASK_NAME);
        assertTaskSuccess(result6, RUN_NPM_YARN_TASK_NAME);
    }
}
