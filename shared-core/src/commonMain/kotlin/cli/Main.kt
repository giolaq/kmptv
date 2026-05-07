package com.kmptv.shared_core.cli

import com.kmptv.shared_core.di.ServiceLocator

class CLI {
    private val contentRepository = ServiceLocator.contentRepository()
    private val sessionManager = ServiceLocator.sessionManager()

    fun execute(args: Array<String>): Int {
        return try {
            when {
                args.isEmpty() -> {
                    printHelp()
                    0
                }
                args[0] == "--help" || args[0] == "-h" -> {
                    printHelp()
                    0
                }
                args[0] == "--version" -> {
                    println("shared-core 0.1.0")
                    0
                }
                args[0] == "content" -> {
                    ContentCommands(contentRepository).execute(args.drop(1).toTypedArray())
                }
                args[0] == "session" -> {
                    SessionCommands(sessionManager).execute(args.drop(1).toTypedArray())
                }
                args[0] == "health" -> {
                    HealthCommands().execute(args.drop(1).toTypedArray())
                }
                else -> {
                    println("Error: Unknown command '${args[0]}'")
                    printHelp()
                    1
                }
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
            1
        }
    }

    private fun printHelp() {
        println("""
            KMPTV Shared Core CLI v0.1.0

            Usage: shared-core <command> [options]

            Commands:
              content    Manage content items
              session    Manage user sessions
              health     Check system health

            Global Options:
              --help, -h    Show this help message
              --version     Show version information

            Examples:
              shared-core content list
              shared-core session create-guest --device-id=test
              shared-core health --verbose

            For command-specific help, use:
              shared-core <command> --help
        """.trimIndent())
    }
}
