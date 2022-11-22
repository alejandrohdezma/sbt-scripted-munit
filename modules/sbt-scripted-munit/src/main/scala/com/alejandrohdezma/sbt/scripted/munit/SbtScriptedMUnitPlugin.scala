/*
 * Copyright 2022 Alejandro Hernández <https://github.com/alejandrohdezma>
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

package com.alejandrohdezma.sbt.scripted.munit

import java.util.concurrent.atomic.AtomicBoolean

import scala.Console
import scala.util.control.NoStackTrace

import sbt.AutoPlugin
import sbt.Keys._
import sbt.Setting
import sbt.settingKey
import sbt.taskKey
import sbt.testing.Event
import sbt.testing.EventHandler
import sbt.testing.Status
import sbt.testing.TaskDef

import munit._

object SbtScriptedMUnitPlugin extends AutoPlugin {

  case object TestsFailed extends RuntimeException("Some tests failed") with NoStackTrace

  object autoImport {

    type FunSuite = munit.FunSuite

    type Assertions = munit.Assertions

    val Assertions = munit.Assertions

    type FunFixtures = munit.FunFixtures

    type Tag = munit.Tag

    val munitSuites = settingKey[Seq[(String, Suite)]] {
      "The list of suites to test as name-suite pairs"
    }

    val munitScripted = taskKey[Unit] {
      "Executes the suites in `munitSuites`"
    }

  }

  import autoImport._

  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    munitSuites := Nil,
    munitScripted := {
      val framework = new Framework

      val testsFailed = new AtomicBoolean(false)

      val logger = streams.value.log

      val eventHandler: EventHandler = {
        case event if event.isFailure =>
          testsFailed.set(true)
          logger.error(s"  ${Console.RED}==> X ${event.name} ${Console.RESET} ${event.failure.getOrElse("")}")
        case event if event.isIgnored =>
          logger.warn(s"  ${Console.YELLOW}==> i ${event.name} ${Console.RESET}")
        case event =>
          logger.info(s"  ${Console.GREEN}+ ${event.name} ${Console.RESET}")
      }

      munitSuites.value.foreach { case (name, suite) =>
        val runner  = framework.runner(Array("+l"), Array.empty, suite.getClass().getClassLoader())
        val taskDef = new TaskDef(suite.getClass().getName(), framework.munitFingerprint, false, Array.empty)
        val tasks   = runner.tasks(Array(taskDef))

        logger.info(s"${Console.GREEN}$name:${Console.RESET}")
        tasks.foreach(_.execute(eventHandler, Array.empty))
      }

      if (testsFailed.get()) throw TestsFailed // scalafix:ok
    }
  )

  implicit private class EventOps(event: Event) {

    val failure: Option[String] =
      if (event.throwable().isDefined) Some(event.throwable().get().toString) // scalafix:ok
      else None

    val isFailure: Boolean = event.status() match {
      case Status.Error | Status.Failure | Status.Canceled | Status.Pending => true
      case _                                                                => false
    }

    val isIgnored: Boolean = event.status() match {
      case Status.Ignored | Status.Skipped => true
      case _                               => false
    }

    val name: String =
      event.fullyQualifiedName().replaceAll(""".*\$\$anon\$\d+\.""", "")

  }

}
