object Versions {
    const val kotlin = "1.7.21"
    const val serialization = "1.0.1"
    const val strikt = "0.33.0"
    const val mockk = "1.12.1"
    const val jUnit = "5.8.2"
    const val spring = "5.3.13"
}

abstract class DependencyGroup(
    val group: String,
    val version: String
) {
    fun dependency(
        name: String,
        group: String = this.group,
        version: String = this.version
    ) = "$group:$name:$version"
}

object Deps {

    const val strikt = "io.strikt:strikt-core:${Versions.strikt}"
    const val jUnit = "org.junit.jupiter:junit-jupiter:${Versions.jUnit}"

}
