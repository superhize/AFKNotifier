package be.hize.afknotifier.config.core

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.config.Features
import be.hize.afknotifier.data.CopyErrorCommand
import be.hize.afknotifier.utils.SimpleTimeMark
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.concurrent.fixedRateTimer

object ConfigManager {
    val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .enableComplexMapKeySerialization()
            .create()

    lateinit var features: Features

    private var configDirectory = File("config/afknotifier")
    private var configFile: File? = null
    lateinit var processor: MoulConfigProcessor<Features>

    fun firstLoad() {
        if (ConfigManager::features.isInitialized) {
            println("Loading config despite config being already loaded?")
        }
        configDirectory.mkdir()

        configFile = File(configDirectory, "config.json")

        println("Trying to load config from $configFile")

        if (configFile!!.exists()) {
            try {
                val builder = StringBuilder()

                println("load-config-now")
                BufferedReader(FileReader(configFile!!)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        builder.append(line).append('\n')
                    }
                }
                val configJson = gson.fromJson(builder.toString(), JsonObject::class.java)
                features = gson.fromJson(configJson, Features::class.java)

                println("Loaded config from file")
            } catch (error: Exception) {
                error.printStackTrace()
                val backupFile = configFile!!.resolveSibling("config-${SimpleTimeMark.now().toMillis()}-backup.json")
                println("Exception while reading $configFile. Will load blank config and save backup to $backupFile")
                println("Exception was $error")
                try {
                    configFile!!.copyTo(backupFile)
                } catch (e: Exception) {
                    println("Could not create backup for config file")
                    e.printStackTrace()
                }
            }
        }

        if (!ConfigManager::features.isInitialized) {
            println("Creating blank config and saving to file")
            features = Features()
            saveConfig("blank config")
        }

        fixedRateTimer(name = "afknotifier-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            try {
                saveConfig("auto-save-60s")
            } catch (error: Throwable) {
                CopyErrorCommand.logError(error, "Error auto-saving config!")
            }
        }

        val features = AFKNotifier.feature
        processor = MoulConfigProcessor(AFKNotifier.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        val configProcessorDriver = ConfigProcessorDriver(processor)
        configProcessorDriver.processConfig(features)
    }

    fun saveConfig(reason: String) {
        println("saveConfig: $reason")
        val file = configFile ?: throw Error("Can not save config, configFile is null!")
        try {
            println("Saving config file")
            file.parentFile.mkdirs()
            val unit = file.parentFile.resolve("config.json.write")
            unit.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
                writer.write(gson.toJson(AFKNotifier.feature))
            }

            Files.move(
                unit.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (e: IOException) {
            println("Could not save config file to $file")
            e.printStackTrace()
        }
    }
}
