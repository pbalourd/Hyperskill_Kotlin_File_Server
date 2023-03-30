package client

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.Socket


object Main {
    private const val SERVER_ADDRESS = "127.0.0.1"
    private const val SERVER_PORT = 23456
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT).use { socket ->
                DataInputStream(socket.getInputStream()).use { input ->
                    DataOutputStream(socket.getOutputStream()).use { output ->
                        println("Client started!")
                        val sentMsg = "Give me everything you have!"
                        output.writeUTF(sentMsg)
                        println("Sent: $sentMsg")
                        val receivedMsg = input.readUTF()
                        println("Received: $receivedMsg")
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}