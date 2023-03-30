package server

import java.io.*
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess


object Main {
    private const val port = 23456
    private val directory = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "server" + File.separator + "data" + File.separator
    private var map = HashMap<Int, String>()
    private val database = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "server" + File.separator + "files.data"

    @JvmStatic
    fun main(args: Array<String>) {
        println("Server started!")
        try {
            ServerSocket(port).use { server ->
                val getOut = AtomicBoolean(false)
                try {
                    val f = File(database)
                    if (f.exists()) {
                        map = deserializeMap()
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
                while (true) {
                    try {
                        server.accept().use { socket ->
                            DataInputStream(socket.getInputStream()).use { input ->
                                DataOutputStream(socket.getOutputStream()).use { output ->
                                    val msg = input.readUTF()
                                    val split =
                                        msg.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    val out: ByteArray?
                                    val fileName: String?
                                    val searchByWhat = split[1]
                                    val action = split[0]
                                    fileName = if (split.size > 2) {
                                        split[2]
                                    } else {
                                        null
                                    }
                                    if (action == "EXIT") {
                                        socket.close()
                                        Thread.currentThread().interrupt()
                                        exitProcess(0)
                                    }
                                    // get file content
                                    if (action == "PUT") {
                                        val length = input.readInt()
                                        out = ByteArray(length)
                                        input.readFully(out, 0, out.size) // read the message
                                    } else {
                                        out = null
                                    }
                                    getOut.set(chooseStuff(output, out, action, searchByWhat, fileName))
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // choose action
    private fun chooseStuff(
        output: DataOutputStream,
        out: ByteArray?,
        action: String,
        searchByWhat: String,
        fileName: String?
    ): Boolean {
        // action is the first word
        var procOut = false
        try {
            when (action.uppercase(Locale.getDefault())) {
                "GET" -> {
                    val getFile: String?
                    getFile = if (fileName!!.matches("[0-9]+".toRegex())) {
                        getFromMap(fileName, 0)
                    } else {
                        getFromMap(fileName, 1)
                    }
                    if (getFile == null) {
                        output.writeInt(404)
                    } else {
                        val fOut = getFileInByteArray(getFile)
                        output.writeInt(fOut.code)
                        output.writeInt(fOut.content.size)
                        output.write(fOut.content)
                    }
                }

                "PUT" -> {
                    val f = File(directory + searchByWhat)
                    if (f.exists()) {
                        output.writeUTF("403 ")
                        exitProcess(0)
                    }
                    FileOutputStream(f).use { os -> os.write(out) }
                    val id = genId()
                    output.writeUTF("200 $id")
                    addToMap(searchByWhat, id)
                }

                "DELETE" -> {
                    val getFile: String?
                    getFile = if (fileName!!.matches("[0-9]+".toRegex())) {
                        delFromMap(fileName, 0)
                    } else {
                        delFromMap(fileName, 1)
                    }
                    if (getFile == null) {
                        output.writeUTF("404")
                    } else {
                        val del = deleteFile(getFile)
                        output.writeUTF(del)
                    }
                }

                "EXIT" -> procOut = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return procOut
    }

    // get file content in bytes array
    @Throws(IOException::class)
    private fun getFileInByteArray(name: String?): MyFile {
        val f = File(directory + name)
        if (!f.exists()) {
            return MyFile(404, byteArrayOf())
        }
        val buffer = ByteArray(f.length().toInt())
        val `is` = FileInputStream(f)
        `is`.read(buffer)
        `is`.close()
        return MyFile(200, buffer)
    }

    // delete file
    private fun deleteFile(name: String?): String {
        val f = File(directory + name)
        if (!f.exists()) {
            return "404"
        }
        f.delete()
        return "200"
    }

    @Synchronized
    private fun getMap(): Map<Int, String> {
        return map
    }

    // get from Map by name or id
    private fun getFromMap(name: String?, type: Int): String? {
        val str = StringBuilder()
        for ((key, value) in map) {
            if (type == 0) {
                if (key == name!!.toInt()) {
                    str.append(value)
                }
            } else {
                if (value == name) {
                    str.append(value)
                }
            }
        }
        return if (str.toString().length == 0) {
            null
        } else {
            str.toString()
        }
    }

    // deserialize Map
    private fun deserializeMap(): HashMap<Int, String> {
        try {
            return SerializationUtils.deserialize(database) as HashMap<Int, String>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HashMap()
    }

    // add to Map and refresh the database file
    @Synchronized
    private fun addToMap(name: String, id: Int) {
        map[id] = name
        try {
            SerializationUtils.serialize(getMap(), database)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // remove from Map and refresh the database file
    @Synchronized
    private fun delFromMap(name: String?, type: Int): String? {
        var found = false
        var getFile: String? = null
        for ((key, value) in map) {
            if (type == 1 && value == name) {
                map.remove(key)
                getFile = value
                found = true
                break
            } else if (type == 0 && key == name!!.toInt()) {
                map.remove(key)
                found = true
                getFile = value
                break
            }
        }
        if (found) {
            try {
                SerializationUtils.serialize(getMap(), database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getFile
    }

    // generate next ID based on saved Map
    private fun genId(): Int {
        var lastId = 0
        for ((key) in map) {
            if (key > lastId) {
                lastId = key
            }
        }
        return ++lastId
    }
}

// serialize - deserialize
internal object SerializationUtils {
    /**
     * Serialize the given object to the file
     */
    @Throws(IOException::class)
    fun serialize(obj: Any?, fileName: String?) {
        val fos = FileOutputStream(fileName)
        val bos = BufferedOutputStream(fos)
        val oos = ObjectOutputStream(bos)
        oos.writeObject(obj)
        oos.close()
    }

    /**
     * Deserialize to an object from the file
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    fun deserialize(fileName: String?): Any {
        val fis = FileInputStream(fileName)
        val bis = BufferedInputStream(fis)
        val ois = ObjectInputStream(bis)
        val obj = ois.readObject()
        ois.close()
        return obj
    }
}

internal class MyFile(var code: Int, var content: ByteArray)