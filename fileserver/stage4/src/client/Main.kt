package client

import java.io.*
import java.net.Socket
import java.util.*


object Main {
    private const val IP_ADDRESS = "127.0.0.1"
    private const val port = 23456
    private val directory = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "client" + File.separator + "data" + File.separator
    @JvmStatic
    fun main(args: Array<String>) {
        println("Client started!")
        try {
            Socket(IP_ADDRESS, port).use { socket ->
                DataInputStream(socket.getInputStream()).use { input ->
                    DataOutputStream(socket.getOutputStream()).use { output ->
                        val scanner = Scanner(System.`in`)
                        var getOut = false
                        while (true) {
                            if (getOut) break
                            print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ")
                            val n = scanner.nextLine()
                            when (n.lowercase(Locale.getDefault())) {
                                "1" -> getFile(scanner, input, output)
                                "2" -> saveFile(scanner, input, output)
                                "3" -> deleteFile(scanner, input, output)
                                "exit" -> {
                                    println("The request was sent.")
                                    output.writeUTF("EXIT NOW")
                                    getOut = true
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // delete file from server
    @Throws(IOException::class)
    private fun deleteFile(scanner: Scanner, input: DataInputStream, output: DataOutputStream) {
        print("Do you want to delete the file by name or by id (1 - name, 2 - id): ")
        val getFileByWhat = scanner.nextLine()
        var type = 0
        when (getFileByWhat) {
            "1" -> {
                print("Enter name of the file: ")
                type = 1
            }

            "2" -> {
                print("Enter id: ")
                type = 2
            }
        }
        if (type == 1) {
            val fileName = scanner.nextLine()
            output.writeUTF("DELETE BY_NAME $fileName")
        } else {
            val id = scanner.nextLine()
            output.writeUTF("DELETE BY_ID $id")
        }
        println("The request was sent.")
        // read output from server
        // 404 - file not found, 200 - file found.
        val del = input.readUTF()
        if (del == "404") {
            println("The response says that this file is not found!")
        } else {
            println("The response says that this file was deleted successfully!")
        }
    }

    // save file from  client to server
    @Throws(IOException::class)
    private fun saveFile(scanner: Scanner, input: DataInputStream, output: DataOutputStream) {
        print("Enter name of the file: ")
        val fileNameToSave = scanner.nextLine()
        print("Enter name of the file to be saved on server: ")
        var newFileNameToSave = scanner.nextLine()
        if (newFileNameToSave.length == 0) {
            newFileNameToSave = makeFileName()
        }
        println("The request was sent.")
        val out = saveFileInByteArray(fileNameToSave)
        if (out.code == 403) {
            println("The response says that this file is not found!")
            return
        }
        output.writeUTF("PUT $newFileNameToSave")
        output.writeInt(out.content.size)
        output.write(out.content)
        val result = input.readUTF()
        val split = result.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val code = split[0]
        // the file exists
        if (code == "403") {
            println("The response says that this file already exists!")
        } else {
            val returnId = split[1]
            System.out.printf("Response says that file is saved! ID = %s\n", returnId)
        }
    }

    // make a random file name
    private fun makeFileName(): String {
        val unixTime = System.currentTimeMillis() / 1000L
        return "file$unixTime"
    }

    // get file from server and save it
    @Throws(IOException::class)
    private fun getFile(scanner: Scanner, input: DataInputStream, output: DataOutputStream) {
        print("Do you want to get the file by name or by id (1 - name, 2 - id): ")
        val getFileByWhat = scanner.nextLine()
        var type = 0
        when (getFileByWhat) {
            "1" -> {
                print("Enter name of the file: ")
                type = 1
            }

            "2" -> {
                print("Enter id: ")
                type = 2
            }
        }
        if (type == 1) {
            val fileName = scanner.nextLine()
            output.writeUTF("GET BY_NAME $fileName")
        } else {
            val id = scanner.nextLine()
            output.writeUTF("GET BY_ID $id")
        }
        println("The request was sent.")
        // read output from server
        // 404 - file not found, 200 - file found.
        val code = input.readInt()
        if (code == 404) {
            println("The response says that this file is not found!")
            return
        }
        val length = input.readInt()
        val out = ByteArray(length)
        input.readFully(out, 0, out.size)
        print("The file was downloaded! Specify a name for it: ")
        val newFileName = scanner.nextLine()
        val f = File(directory + newFileName)
        FileOutputStream(f).use { os -> os.write(out) }
        println("File saved on the hard drive!")
    }

    @Throws(IOException::class)
    private fun saveFileInByteArray(name: String): ClientFile {
        val f = File(directory + name)
        if (!f.exists()) {
            return ClientFile(403, byteArrayOf())
        }
        val buffer = ByteArray(f.length().toInt())
        val `is` = FileInputStream(f)
        `is`.read(buffer)
        `is`.close()
        return ClientFile(200, buffer)
    }
}

internal class ClientFile(var code: Int, var content: ByteArray)