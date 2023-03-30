package client

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.util.*


object Main {
    private const val SERVER_ADDRESS = "127.0.0.1"
    private const val SERVER_PORT = 23456
    private const val EXIT_COMMAND = "exit"
    private const val CREATE_FILE_COMMAND = "2"
    private const val GET_FILE_COMMAND = "1"
    private const val DELETE_FILE_COMMAND = "3"
    private const val SUCCESS_RESPONSE_CODE = "200"
    private const val FORBIDDEN_RESPONSE_CODE = "403"
    private const val NOT_FOUND_RESPONSE_CODE = "404"
    private const val PUT_COMMAND = "PUT"
    private const val GET_COMMAND = "GET"
    private const val DELETE_COMMAND = "DELETE"
    private val scanner = Scanner(System.`in`)
    @JvmStatic
    fun main(args: Array<String>) {
        println("Client started!")
        print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ")
        val command = scanner.next()
        when (command) {
            CREATE_FILE_COMMAND -> createFile()
            GET_FILE_COMMAND -> fileContent
            DELETE_FILE_COMMAND -> deleteFile()
            EXIT_COMMAND -> shutdownServer()
        }
    }

    private fun createFile() {
        print("Enter filename: ")
        val fileName = scanner.next()
        print("Enter file content: ")
        var content = scanner.next().trim { it <= ' ' }
        content = content + scanner.nextLine()
        val responseCode = sendRequestAndReadResponse(PUT_COMMAND + " " + fileName + " " + content)
        if (SUCCESS_RESPONSE_CODE == responseCode) {
            println("The response says that the file was created!")
        } else if (FORBIDDEN_RESPONSE_CODE == responseCode) {
            println("The response says that creating the file was forbidden!")
        }
    }

    private fun sendRequestAndReadResponse(request: String): String {
        try {
            Socket(SERVER_ADDRESS, SERVER_PORT).use { socket ->
                DataInputStream(socket.getInputStream()).use { input ->
                    DataOutputStream(socket.getOutputStream()).use { output ->
                        output.writeUTF(request)
                        println("The request was sent.")
                        return input.readUTF()
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val fileContent: Unit
        private get() {
            print("Enter filename: ")
            val fileName = scanner.next()
            val response = sendRequestAndReadResponse(GET_COMMAND + " " + fileName)
            if (NOT_FOUND_RESPONSE_CODE == response) {
                println("The response says that the file was not found!")
            } else {
                println("The content of the file is: $response")
            }
        }

    private fun deleteFile() {
        print("Enter filename: ")
        val fileName = scanner.next()
        val responseCode = sendRequestAndReadResponse(DELETE_COMMAND + " " + fileName)
        if (SUCCESS_RESPONSE_CODE == responseCode) {
            println("The response says that the file was successfully deleted!")
        } else if (NOT_FOUND_RESPONSE_CODE == responseCode) {
            println("The response says that the file was not found!")
        }
    }

    private fun shutdownServer() {
        sendRequestWithoutResponse(EXIT_COMMAND.uppercase(Locale.getDefault()))
    }

    private fun sendRequestWithoutResponse(request: String) {
        try {
            Socket(SERVER_ADDRESS, SERVER_PORT).use { socket ->
                DataOutputStream(socket.getOutputStream()).use { output ->
                    output.writeUTF(request)
                    println("The request was sent.")
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
