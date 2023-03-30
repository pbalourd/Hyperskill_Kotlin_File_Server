package server

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket


object Main {
    private const val SERVER_ADDRESS = "127.0.0.1"
    private const val SERVER_PORT = 23456
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            ServerSocket(SERVER_PORT, 50, InetAddress.getByName(SERVER_ADDRESS)).use { server ->
                println("Server started!")
                server.accept().use { socket ->
                    DataInputStream(socket.getInputStream()).use { input ->
                        DataOutputStream(socket.getOutputStream()).use { output ->
                            val msg = input.readUTF() // read a message from the client
                            println("Received: $msg")
                            val sentMsg = "All files were sent!"
                            if (msg == "Give me everything you have!") {
                                output.writeUTF(sentMsg) // resend it to the client
                                println("Sent: $sentMsg")
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}