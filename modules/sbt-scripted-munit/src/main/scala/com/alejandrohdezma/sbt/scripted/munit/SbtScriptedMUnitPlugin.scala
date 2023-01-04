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

import munit._
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier

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
      val testsFailed = new AtomicBoolean(false)

      val logger = streams.value.log

      val listener = new RunListener {

        val failed = new ThreadLocal[Boolean]

        override def testFailure(failure: Failure): Unit = {
          failed.set(true)
          testsFailed.set(true)
          val name      = failure.getDescription().name
          val throwable = Option(failure.getException()).flatMap(t => Option(t.getMessage()))
          logger.error(s"  ${Console.RED}==> X $name ${Console.RESET} ${throwable.getOrElse("")}")
        }

        override def testIgnored(description: Description): Unit =
          logger.warn(s"  ${Console.YELLOW}==> i ${description.name} ${Console.RESET}")

        override def testFinished(description: Description): Unit =
          if (!failed.get)
            logger.info(s"  ${Console.GREEN}+ ${description.name} ${Console.RESET}")

      }

      val notifier = new RunNotifier {}
      notifier.addListener(listener)

      munitSuites.value.foreach { case (name, suite) =>
        logger.info(s"${Console.GREEN}$name:${Console.RESET}")

        val runner = new MUnitRunner(suite.getClass(), () => suite)
        runner.run(notifier)
      }

      if (testsFailed.get()) throw TestsFailed // scalafix:ok
    }
  )

  implicit private class DescriptionOps(description: Description) {

    def name = {
      val displayName = description.getDisplayName()

      displayName.substring(0, displayName.lastIndexOf("("))
    }

  }

}
