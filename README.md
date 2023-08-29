# corda5-testutils [![Maven Central](https://img.shields.io/maven-central/v/com.github.manosbatsis.corda5.testutils/integration-junit5.svg)](https://repo1.maven.org/maven2/com/github/manosbatsis/corda5/testutils/integration-junit5/) [![CI](https://github.com/manosbatsis/corda5-testutils/actions/workflows/gradle.yml/badge.svg)](https://github.com/manosbatsis/corda5-testutils/actions/workflows/gradle.yml)

Test utilities for Corda 5 applications.
At the moment this project provides utilities for integration testing with the Corda 5 Combined Worker.

## Quick Howto

### Install 

In your gradle:

```groovy

    testImplementation("com.github.manosbatsis.corda5.testutils:integration-junit5:1.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
```

### Prepare 

1. Run `./gradlew startCorda` and 
2. Run `./gradlew 5-vNodeSetup` or `./gradlew 4-deployCPIs` as appropriate

See R3's [CSDE Overview](https://docs.r3.com/en/platform/corda/5.0/developing-applications/getting-started/overview.html#csde-corda) for more info.

### Add a Test

The `Corda5NodesExtension` will retrieve the list of virtual nodes from the combined worker 
and expose them as a `NodeHandles` parameter to your test methods. Each node has basic info plus 
utility methods like `waitForFlow` that can be used to initiate flows and wait for a final flow status.

```kotlin
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesConfig
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesExtension
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.model.FlowRequest
import com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles.NodeHandles
import net.corda.v5.base.types.MemberX500Name
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
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
        debug = false
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

## Feedback

Issues, PRs etc. welcome. You can also try pinging me on https://cordaledger.slack.com