/*
 * GNU General Public License version 2
 *
 * Copyright (C) 2019-2020 JetBrains s.r.o.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
  kotlin("jvm")
  application
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

application {
  mainClass.set("org.jetbrains.projector.demo.OriginalMain")
}

group = "org.jetbrains.projector"
version = "1.0-SNAPSHOT"

val jarOriginalConf: Configuration by configurations.creating

val kotlinVersion: String by project
val projectorServerVersion: String by project
val targetJvm: String by project

tasks.withType<KotlinJvmCompile> {
  kotlinOptions {
    jvmTarget = targetJvm
  }
}

configurations.all {
  // disable caching of -SNAPSHOT dependencies
  resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
  implementation("com.github.JetBrains.projector-server:projector-server:$projectorServerVersion")

  jarOriginalConf("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  jarOriginalConf("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}

fun Project.inline(conf: Configuration): Iterable<Any> {
  return conf.map { it: File -> if (it.isDirectory) it else zipTree(it) }
}

val jarOriginal by tasks.creating(Jar::class) {
  manifest {
    attributes(
      "Main-Class" to "org.jetbrains.projector.demo.OriginalMain",
    )
  }

  exclude("META-INF/versions/9/module-info.class")

  archiveBaseName.set("${project.name}-original")

  from(inline(jarOriginalConf))

  with(tasks.jar.get())
}

val jarHeadlessSupport by tasks.creating(Jar::class) {
  manifest {
    attributes(
      "Main-Class" to "org.jetbrains.projector.demo.HeadlessSupportingMain",
    )
  }

  exclude("META-INF/versions/9/module-info.class")

  archiveBaseName.set("${project.name}-headless-support")

  from(inline(configurations.runtimeClasspath.get()))

  with(tasks.jar.get())
}
