package com.kmptv.shared_core.cli

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.services.SessionManager
import kotlinx.coroutines.runBlocking

/**
 * CLI commands for session management.
 */
class SessionCommands(private val sessionManager: SessionManager) {

    fun execute(args: Array<String>): Int = runBlocking {
        when {
            args.isEmpty() -> {
                printHelp(); 1
            }
            args[0] == "--help" || args[0] == "-h" -> {
                printHelp(); 0
            }
            args[0] == "create-guest" -> createGuestSession(args.drop(1).toTypedArray())
            args[0] == "login" -> authenticateUser(args.drop(1).toTypedArray())
            args[0] == "status" -> getSessionStatus(args.drop(1).toTypedArray())
            args[0] == "logout" -> endSession()
            else -> {
                println("Error: Unknown session command '${args[0]}'")
                printHelp(); 1
            }
        }
    }

    private suspend fun createGuestSession(args: Array<String>): Int {
        var deviceId = "cli-device"
        var platform = Platform.AndroidTV

        for (arg in args) {
            when {
                arg.startsWith("--device-id=") -> deviceId = arg.substringAfter("=")
                arg.startsWith("--platform=")  -> runCatching {
                    Platform.valueOf(arg.substringAfter("="))
                }.getOrNull()?.let { platform = it }
            }
        }

        val deviceInfo = DeviceInfoDefaults.forPlatform(platform = platform, deviceId = deviceId)
        val result = sessionManager.createGuestSession(deviceInfo)

        return if (result.isSuccess) {
            val session = result.getOrThrow()
            println("Guest session created successfully")
            println("Session ID: ${session.sessionId}")
            println("Device ID: ${session.deviceInfo.deviceId}")
            println("Session Timeout: ${session.sessionTimeout / 1000} seconds")
            0
        } else {
            result.onFailure { println("Error: ${it.message}") }
            1
        }
    }

    private suspend fun authenticateUser(args: Array<String>): Int {
        var username: String? = null
        var password: String? = null
        for (arg in args) {
            when {
                arg.startsWith("--username=") -> username = arg.substringAfter("=")
                arg.startsWith("--password=") -> password = arg.substringAfter("=")
            }
        }
        if (username == null || password == null) {
            println("Error: Both username and password are required")
            println("Usage: shared-core session login --username=<user> --password=<pass>")
            return 1
        }

        val credentials = UserCredentials(username = username, password = password)
        val result = sessionManager.authenticateUser(credentials)

        return if (result.isSuccess) {
            val session = result.getOrThrow()
            println("Authentication successful")
            println("Session ID: ${session.sessionId}")
            println("User ID: ${session.userId}")
            println("Session Timeout: ${session.sessionTimeout / 1000} seconds")
            0
        } else {
            result.onFailure { println("Error: ${it.message}") }
            1
        }
    }

    private suspend fun getSessionStatus(args: Array<String>): Int {
        var format = "table"
        for (arg in args) {
            if (arg.startsWith("--format=")) format = arg.substringAfter("=")
        }

        val session = sessionManager.getCurrentSession()
        if (session == null) {
            when (format) {
                "json" -> println(
                    """
                    {
                      "success": false,
                      "message": "No active session",
                      "data": null
                    }
                    """.trimIndent(),
                )
                else -> println("No active session")
            }
            return 1
        }

        val isValid = sessionManager.isSessionValid()
        when (format) {
            "json" -> println(
                """
                {
                  "success": true,
                  "data": {
                    "sessionId": "${session.sessionId}",
                    "userId": "${session.userId ?: "guest"}",
                    "isAuthenticated": ${session.isAuthenticated},
                    "isValid": $isValid,
                    "lastActivity": ${session.lastActivity},
                    "sessionTimeout": ${session.sessionTimeout},
                    "remainingTime": ${session.getRemainingTime()},
                    "sessionType": "${session.getSessionType()}"
                  }
                }
                """.trimIndent(),
            )
            else -> {
                println("Session Status:")
                println("Session ID: ${session.sessionId}")
                println("User ID: ${session.userId ?: "guest"}")
                println("Authenticated: ${session.isAuthenticated}")
                println("Valid: $isValid")
                println("Session Type: ${session.getSessionType()}")
                println("Last Activity: ${TimeFormat.formatDateTime(session.lastActivity)}")
                println("Remaining Time: ${session.getRemainingTime() / 1000} seconds")
                println("Device: ${session.deviceInfo.model} (${session.deviceInfo.deviceId})")
            }
        }
        return 0
    }

    private suspend fun endSession(): Int {
        sessionManager.endSession()
        println("Session ended successfully")
        return 0
    }

    private fun printHelp() {
        println(
            """
            Session Management Commands

            Usage: shared-core session <command> [options]

            Commands:
              create-guest       Create a guest session
              login             Authenticate user
              status            Show current session status
              logout            End current session

            Options:
              --device-id=ID     Device identifier (for create-guest)
              --platform=NAME    Platform name: AndroidTV | AppleTV (for create-guest)
              --username=USER    Username (for login)
              --password=PASS    Password (for login)
              --format=FORMAT    Output format: json, table (default: table)

            Examples:
              shared-core session create-guest --device-id=test-device
              shared-core session login --username=testuser --password=password123
              shared-core session status --format=json
              shared-core session logout
            """.trimIndent(),
        )
    }
}
