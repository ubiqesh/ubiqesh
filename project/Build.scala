import sbt._
import Keys._

object YagniBuild extends Build {

    lazy val root = Project(id = "yagni",
                            base = file(".")) aggregate(`yagni-protocol`, `yagni-api`, `yagni-edge`,`yagni-central`)

    lazy val `yagni-protocol` = Project(id = "yagni-protocol",
                           base = file("yagni-protocol/java"))

    lazy val `yagni-api` = Project(id = "yagni-api",
                           base = file("yagni-api")) dependsOn(`yagni-protocol`)

    lazy val `yagni-edge` = Project(id = "yagni-edge",
                           base = file("yagni-edge")) dependsOn(`yagni-api`)

    lazy val `yagni-central` = Project(id = "yagni-central",
                           base = file("yagni-central")) dependsOn(`yagni-api`)


}