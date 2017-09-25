package com.example

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

class FoodPlugin : Plugin<Project> {
    open class FooTask : DefaultTask() {

        @OutputDirectory
        lateinit var generatedDir: File

        @TaskAction
        fun generateStuff() {
            val spec = TypeSpec.classBuilder("Food")
                    .addModifiers(Modifier.PUBLIC)
                    .addField(FieldSpec.builder(String::class.java, "FOOD", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("\$S", "FOOD")
                            .build())
                    .build()

            val outputDir = File(generatedDir, "com/example")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            File(outputDir, "Food.java").writer().use {
                JavaFile.builder("com.example", spec)
                        .build()
                        .writeTo(it)
            }
        }
    }

    override fun apply(project: Project) {
        project.afterEvaluate {
            val android = project.extensions.findByName("android") as? BaseExtension ?: return@afterEvaluate
            val variants: DomainObjectSet<out BaseVariant> = when (android) {
                is AppExtension -> android.applicationVariants
                is LibraryExtension -> android.libraryVariants
                else -> return@afterEvaluate
            }

            val fooRoot = File(project.buildDir, "generated${File.separator}source${File.separator}food")
            variants.forEach { variant ->
                val fooTask = project.tasks.create("generateFood${variant.name.capitalize()}", com.example.FoodPlugin.FooTask::class.java) .apply {
                    generatedDir = File(fooRoot, variant.dirName)
                }
                variant.registerJavaGeneratingTask(fooTask, File(fooRoot, variant.dirName))
            }
        }
    }
}
