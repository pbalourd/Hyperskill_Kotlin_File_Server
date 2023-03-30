package server

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.util.*


class FileStorage {
    init {
        createBaseDirIfNotExists()
    }

    private fun createBaseDirIfNotExists() {
        val baseDir = File(BASE_DIR_PATH)
        if (!baseDir.exists() || !baseDir.isDirectory) {
            baseDir.mkdirs()
        }
    }

    fun put(fileName: String, content: String?): Boolean {
        val file = getFileBy(fileName)
        if (file.exists()) {
            return false
        } else {
            try {
                FileWriter(file).use { fileWriter -> fileWriter.write(content) }
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    private fun getFileBy(fileName: String): File {
        return File(BASE_DIR_PATH + fileName)
    }

    operator fun get(fileName: String): Optional<String> {
        val file = getFileBy(fileName)
        return if (!file.exists() || !file.isFile) {
            Optional.empty()
        } else {
            try {
                Optional.of(String(Files.readAllBytes(file.toPath())))
            } catch (e: IOException) {
                e.printStackTrace()
                Optional.empty()
            }
        }
    }

    fun delete(fileName: String): Boolean {
        val file = getFileBy(fileName)
        return if (!file.exists() || !file.isFile) {
            false
        } else {
            file.delete()
        }
    }

    companion object {
        private const val BASE_DIR_PATH = "./src/server/data/"
    }
}