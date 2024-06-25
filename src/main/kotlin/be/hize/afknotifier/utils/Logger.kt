package be.hize.afknotifier.utils

import be.hize.afknotifier.AFKNotifier
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.time.Duration.Companion.days

class Logger(filePath: String) {

    private val format = SimpleDateFormat("HH:mm:ss")
    private val fileName = "$PREFIX_PATH$filePath.log"

    companion object {

        private var LOG_DIRECTORY = File("config/afknotifier/logs")
        private var PREFIX_PATH: String
        var hasDone = false

        init {
            val format = SimpleDateFormat("yyyy_MM_dd/HH_mm_ss").format(System.currentTimeMillis())
            PREFIX_PATH = "config/afknotifier/logs/$format/"
        }
    }

    private lateinit var logger: Logger

    private fun getLogger(): Logger {
        if (::logger.isInitialized) {
            return logger
        }

        val initLogger = initLogger()
        this.logger = initLogger
        return initLogger
    }

    private fun initLogger(): Logger {
        val logger = Logger.getLogger("AFKNotifier-Logger-" + System.nanoTime())
        try {
            createParent(File(fileName))
            val handler = FileHandler(fileName)
            handler.encoding = "utf-8"
            logger.addHandler(handler)
            logger.useParentHandlers = false
            handler.formatter = object : Formatter() {
                override fun format(logRecord: LogRecord): String {
                    val message = logRecord.message
                    return format.format(System.currentTimeMillis()) + " $message\n"
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (!hasDone && HypixelUtils.onHypixel) {
            hasDone = true
            val directoryFiles = LOG_DIRECTORY.listFiles() ?: run {
                println("log directory has no files")
                return logger
            }
            AFKNotifier.coroutineScope.launch {
                val timeToDelete = 14.days

                for (file in directoryFiles) {
                    val path = file.toPath()
                    try {
                        val attributes = Files.readAttributes(path, BasicFileAttributes::class.java)
                        val creationTime = attributes.creationTime().toMillis()
                        val timeSinceCreation = SimpleTimeMark(creationTime).passedSince()
                        if (timeSinceCreation > timeToDelete) {
                            if (!file.deleteRecursively()) {
                                println("failed to delete directory: ${file.name}")
                            }
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        println("Error: Unable to get creation date.")
                    }
                }
            }
        }

        return logger
    }

    private fun createParent(file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.isDirectory) {
            parent.mkdirs()
        }
    }

    fun log(text: String?) {
        getLogger().info(text)
    }
}
