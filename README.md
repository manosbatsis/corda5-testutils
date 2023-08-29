# corda5-testutils

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

    // Optional config for extension
    val config = Corda5NodesConfig(debug = true)

    // Corda5 nodes extension provides the NodeHandles
    @Test
    fun recordingFlowTests(nodeHandles: NodeHandles) {
        // Get node handles
        val aliceNode = nodeHandles.getByCommonName("Alice")
        val bobNode = nodeHandles.getByCommonName("Bob")

        // Create state
        val myFlowArgs = MyFlowArgs(aliceNode.memberX500Name, bobNode.memberX500Name)
        val createdStatus = aliceNode.waitForFlow(
            FlowRequest(
                flowClassName =  MyFlow::class.java.canonicalName,
                requestBody = myFlowArgs
            )
        )
        logger.info("Create flow status: $createdStatus")
        assertTrue(createdStatus.isSuccess())
    }
}
```
