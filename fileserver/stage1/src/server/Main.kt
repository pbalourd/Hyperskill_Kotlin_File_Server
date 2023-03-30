package server

import java.util.*
import java.util.regex.Pattern


object Main {
    private val server: MutableSet<String> = LinkedHashSet()
    @JvmStatic
    fun main(args: Array<String>) {
        val input = Scanner(System.`in`)
        val regex = "exit|(add|get|delete)\\s.+"
        val pattern = Pattern.compile(regex)
        var command = ""
        do {
            val query = input.nextLine()
            if (!pattern.matcher(query).matches()) {
                println("Query not supported")
            } else if (query == "exit") {
                break
            } else {
                val request = query.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                command = request[0]
                val filename = request[1]
                when (command) {
                    "add" -> add(filename)
                    "get" -> Main[filename]
                    "delete" -> delete(filename)
                }
            }
        } while (true)
    }

    private fun add(file: String) {
        if (!server.add(file) || !file.matches("file([1-9]|10)".toRegex())) {
            println("Cannot add the file $file")
        } else {
            println("The file $file added successfully")
        }
    }

    private operator fun get(file: String) {
        if (server.contains(file)) {
            println("The file $file was sent")
        } else {
            println("The file $file not found")
        }
    }

    private fun delete(file: String) {
        if (server.remove(file)) {
            println("The file $file was deleted")
        } else {
            println("The file $file not found")
        }
    }
}