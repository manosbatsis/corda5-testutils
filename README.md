# corda5-testutils [![Maven Central](https://img.shields.io/maven-central/v/com.github.manosbatsis.corda5.testutils/integration-junit5.svg)](https://repo1.maven.org/maven2/com/github/manosbatsis/corda5/testutils/integration-junit5/) [![CI](https://github.com/manosbatsis/corda5-testutils/actions/workflows/gradle.yml/badge.svg)](https://github.com/manosbatsis/corda5-testutils/actions/workflows/gradle.yml)

Test utilities for Corda 5 applications.
At the moment this project provides utilities for integration testing with the Corda 5 Combined Worker.

## Prerequisites

Same as [CSDE](https://docs.r3.com/en/platform/corda/5.0/developing-applications/getting-started/prerequisites.html#software-prerequisites) 

- Azul Zulu JDK 11
- Git ~v2.24.1
- Docker Engine ~v20.X.Y or Docker Desktop ~v3.5.X
- Corda CLI, see [Installing the Corda CLI](https://docs.r3.com/en/platform/corda/5.0/developing-applications/tooling/installing-corda-cli.html)

### Gradle Dependencies

In your gradle:

```groovy

    testImplementation("com.github.manosbatsis.corda5.testutils:integration-junit5:1.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
```

## Quick Howto

The JUnit5 extension will automatically launch, setup VNodes and (re)deploy CPIs 
to the Combined Worker as needed by default. The config exposes three modes:

- SHARED: Default, described above. 
- PER_CLASS: Will force a fresh Combined worker for the current Test Class.
- NONE: Completely disables automation for the Combined Worker to enable manual or external management.


### Create a Test

The `Corda5NodesExtension` will retrieve the list of virtual nodes from the combined worker 
and expose them as a `NodeHandles` parameter to your test methods. Each node has basic info plus 
utility methods like `waitForFlow` that can be used to initiate flows and wait for a final flow status.

```kotlin
import com.github.manosbatsis.corda5.testutils.integration.junit5.CombinedWorkerMode
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesConfig
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesExtension
import com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles.NodeHandles
import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowRequest
import net.corda.v5.base.types.MemberX500Name
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue

// Add the Corda5 nodes extension
@ExtendWith(Corda5NodesExtension::class)
open class DemoApplicationTests {

    // Optional config for extension, values bellow are defaults
    val config = Corda5NodesConfig(
        authUsername = "admin",
        authPassword = "admin",
        baseUrl = "https://localhost:8888/api/v1/",
        httpMaxWaitSeconds = 60,
        debug = false,
        projectDir = Corda5NodesConfig.gradleRootDir,
        combinedWorkerMode = CombinedWorkerMode.SHARED,
        // Optional, null by default
        objectMapperConfigurer = { objectMapper: ObjectMapper ->
            objectMapper.registerModule(myModule)
        }
    )

    // The Corda5NodesExtension provides the NodeHandles
    @Test
    fun recordingFlowTests(nodeHandles: NodeHandles) {
        // Get node handles
        val aliceNode = nodeHandles.getByCommonName("Alice")
        val bobNode = nodeHandles.getByCommonName("Bob")

        // Call flow
        val myFlowArgs = MyFlowArgs(aliceNode.memberX500Name, bobNode.memberX500Name)
        val createdStatus = aliceNode.waitForFlow(
            FlowRequest(
                flowClassName =  MyFlow::class.java.canonicalName,
                requestBody = myFlowArgs
            )
        )
        // Check flow status
        assertTrue(createdStatus.isSuccess())
    }
}
```

## Github Action

Using Github for your CI? Check out [corda5-cli-action](https://github.com/manosbatsis/corda5-cli-action).

## Feedback

Issues, PRs etc. welcome. You can also try pinging me on https://cordaledger.slack.com
