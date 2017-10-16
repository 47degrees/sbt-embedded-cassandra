/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
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

package sbtembeddedcassandra.io

import java.io.File

import cats.syntax.either._
import sbtorgpolicies.io.ReplaceTextEngine

import scala.reflect.io.Path

object IOUtils {

  def copyFile(
      workingDir: File,
      inputResource: String,
      variables: Map[String, String],
      outputFileName: String): Either[Throwable, File] = {

    def readCassandraFile: Either[Throwable, String] =
      Either.catchNonFatal {
        scala.io.Source
          .fromInputStream(getClass.getResourceAsStream(inputResource))
          .mkString
      }

    val replaceTextEngine = new ReplaceTextEngine
    val outputFile        = new File(workingDir, outputFileName)
    val replacements = variables.map {
      case (key, value) => "\\$\\{" + key + "\\}" -> value
    }

    for {
      _       <- replaceTextEngine.fileWriter.createDir(workingDir)
      content <- readCassandraFile
      _       <- replaceTextEngine.fileWriter.writeContentToFile(content, outputFile.getAbsolutePath)
      _ <- if (variables.isEmpty) Right(Nil)
      else {
        replaceTextEngine.replaceTexts(replacements, List(outputFile), _ => true)
      }
    } yield outputFile
  }

  def deleteDir(pathFile: File, recreate: Boolean = true): Either[Throwable, Unit] =
    Either.catchNonFatal {
      val path: Path = Path(pathFile)
      path.deleteRecursively()
      if (recreate) path.createDirectory(force = true)
      (): Unit
    }

  def readStatements(pathDir: String): Either[Throwable, List[String]] =
    Either
      .catchNonFatal(
        scala.io.Source.fromInputStream(getClass.getResourceAsStream(pathDir)).mkString)
      .map(_.split(";").filter(_.trim.nonEmpty).toList)

}
