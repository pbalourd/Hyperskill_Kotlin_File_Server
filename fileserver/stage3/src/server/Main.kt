package server

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.util.*


object Main {
    private const val PORT = 23456
    private const val PUT_COMMAND = "PUT"
    private const val GET_COMMAND = "GET"
    private const val DELETE_COMMAND = "DELETE"
    private const val EXIT_COMMAND = "EXIT"
    private const val SUCCESS_RESPONSE_CODE = "200"
    private const val FORBIDDEN_RESPONSE_CODE = "403"
    private const val NOT_FOUND_RESPONSE_CODE = "404"
    private val fileStorage: FileStorage = FileStorage()
    @JvmStatic
    fun main(args: Array<String>) {
        println("Server started!")
        try {
            ServerSocket(PORT).use { serverSocket ->
                var command = ""
                while (EXIT_COMMAND != command) {
                    serverSocket.accept().use { socket ->
                        DataOutputStream(socket.getOutputStream()).use { output ->
                            DataInputStream(socket.getInputStream()).use { input ->
                                val reader = Scanner(input.readUTF())
                                command = reader.next()
                                when (command) {
                                    PUT_COMMAND -> {
                                        val fileName = reader.next()
                                        val content = reader.nextLine().trim { it <= ' ' }
                                        output.writeUTF(
                                            processPutCommand(fileName, content)
                                        )
                                    }

                                    GET_COMMAND -> output.writeUTF(
                                        processGetCommand(reader.next())
                                    )

                                    DELETE_COMMAND -> output.writeUTF(
                                        processDeleteCommand(reader.next())
                                    )
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

    private fun processPutCommand(fileName: String, content: String): String {
        val isSuccess: Boolean = fileStorage.put(fileName, content)
        return if (isSuccess) {
            SUCCESS_RESPONSE_CODE
        } else {
            FORBIDDEN_RESPONSE_CODE
        }
    }

    private fun processGetCommand(fileName: String): String {
        val content: Optional<String> = fileStorage.get(fileName)
        return if (content.isEmpty) {
            NOT_FOUND_RESPONSE_CODE
        } else {
            SUCCESS_RESPONSE_CODE + content.get()
        }
    }

    private fun processDeleteCommand(fileName: String): String {
        val isSuccess: Boolean = fileStorage.delete(fileName)
        return if (isSuccess) {
            SUCCESS_RESPONSE_CODE
        } else {
            NOT_FOUND_RESPONSE_CODE
        }
    }
}
