package com.kmptv.shared_core.cli

import com.kmptv.shared_core.models.ContentItem
import com.kmptv.shared_core.models.TimeFormat
import com.kmptv.shared_core.models.nowMillis
import com.kmptv.shared_core.repositories.ContentRepository
import kotlinx.coroutines.runBlocking

/**
 * CLI commands for content management.
 */
class ContentCommands(private val contentRepository: ContentRepository) {

    fun execute(args: Array<String>): Int = runBlocking {
        when {
            args.isEmpty() -> {
                printHelp(); 1
            }
            args[0] == "--help" || args[0] == "-h" -> {
                printHelp(); 0
            }
            args[0] == "list" -> listContent(args.drop(1).toTypedArray())
            args[0] == "get" -> getContent(args.drop(1).toTypedArray())
            args[0] == "search" -> searchContent(args.drop(1).toTypedArray())
            args[0] == "access" -> markAccessed(args.drop(1).toTypedArray())
            else -> {
                println("Error: Unknown content command '${args[0]}'")
                printHelp(); 1
            }
        }
    }

    private suspend fun listContent(args: Array<String>): Int {
        var limit = 50
        var offset = 0
        var format = "table"

        for (arg in args) {
            when {
                arg.startsWith("--limit=")  -> limit  = arg.substringAfter("=").toIntOrNull() ?: 50
                arg.startsWith("--offset=") -> offset = arg.substringAfter("=").toIntOrNull() ?: 0
                arg.startsWith("--format=") -> format = arg.substringAfter("=")
            }
        }

        val result = contentRepository.getContentItems(limit, offset)
        return if (result.isSuccess) {
            val items = result.getOrThrow()
            when (format) {
                "json" -> println(
                    """
                    {
                      "success": true,
                      "data": {
                        "items": [
                          ${items.joinToString(",\n              ") { itemToJson(it) }}
                        ],
                        "totalCount": ${items.size}
                      },
                      "timestamp": "${nowMillis()}"
                    }
                    """.trimIndent(),
                )
                else -> {
                    println("ID           | Title            | Type  | Offline | Last Accessed")
                    println("------------ | ---------------- | ----- | ------- | -------------")
                    items.forEach { item ->
                        val lastAccessed = item.lastAccessed?.let { TimeFormat.formatDate(it) } ?: "Never"
                        val offlineCol = (if (item.isOfflineAvailable) "Yes" else "No").padEnd(7)
                        println(
                            "${item.id.padEnd(12)} | ${item.title.take(16).padEnd(16)} | " +
                                "${item.contentType.name.padEnd(5)} | $offlineCol | $lastAccessed",
                        )
                    }
                }
            }
            0
        } else {
            result.onFailure { println("Error: ${it.message}") }
            1
        }
    }

    private suspend fun getContent(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("Error: Content ID required")
            println("Usage: shared-core content get <content-id> [--format=json|table]")
            return 1
        }
        val contentId = args[0]
        var format = "table"
        for (arg in args.drop(1)) {
            if (arg.startsWith("--format=")) format = arg.substringAfter("=")
        }

        val result = contentRepository.getContentItem(contentId)
        return if (result.isSuccess) {
            val item = result.getOrThrow()
            if (item != null) {
                when (format) {
                    "json" -> println(itemToJson(item))
                    else -> {
                        println("Content Details:")
                        println("ID: ${item.id}")
                        println("Title: ${item.title}")
                        println("Type: ${item.contentType}")
                        println("Description: ${item.description ?: "N/A"}")
                        println("Offline Available: ${item.isOfflineAvailable}")
                        println("Focusable: ${item.focusable}")
                        println(
                            "Last Accessed: " +
                                (item.lastAccessed?.let { TimeFormat.formatDateTime(it) } ?: "Never"),
                        )
                    }
                }
                0
            } else {
                println("Error: Content with ID '$contentId' not found")
                1
            }
        } else {
            result.onFailure { println("Error: ${it.message}") }
            1
        }
    }

    private suspend fun searchContent(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("Error: Search query required")
            println("Usage: shared-core content search <query> [--format=json|table]")
            return 1
        }
        val query = args[0]
        var format = "table"
        for (arg in args.drop(1)) {
            if (arg.startsWith("--format=")) format = arg.substringAfter("=")
        }

        val result = contentRepository.searchContent(query)
        return if (result.isSuccess) {
            val items = result.getOrThrow()
            when (format) {
                "json" -> println(
                    """
                    {
                      "success": true,
                      "data": {
                        "query": "$query",
                        "items": [
                          ${items.joinToString(",\n              ") { itemToJson(it) }}
                        ],
                        "resultCount": ${items.size}
                      }
                    }
                    """.trimIndent(),
                )
                else -> {
                    println("Search Results for '$query':")
                    println("ID           | Title            | Type  | Description")
                    println("------------ | ---------------- | ----- | -----------")
                    items.forEach { item ->
                        println(
                            "${item.id.padEnd(12)} | ${item.title.take(16).padEnd(16)} | " +
                                "${item.contentType.name.padEnd(5)} | ${(item.description ?: "").take(20)}",
                        )
                    }
                    println("\nTotal results: ${items.size}")
                }
            }
            0
        } else {
            result.onFailure { println("Error: ${it.message}") }
            1
        }
    }

    private suspend fun markAccessed(args: Array<String>): Int {
        if (args.isEmpty()) {
            println("Error: Content ID required")
            println("Usage: shared-core content access <content-id>")
            return 1
        }
        val result = contentRepository.markContentAccessed(args[0])
        return if (result.isSuccess) {
            println("Content '${args[0]}' marked as accessed")
            0
        } else {
            result.onFailure { println("Error: ${it.message}") }
            1
        }
    }

    private fun itemToJson(item: ContentItem): String = """
        {
          "id": "${item.id}",
          "title": "${item.title}",
          "contentType": "${item.contentType}",
          "description": "${item.description ?: ""}",
          "isOfflineAvailable": ${item.isOfflineAvailable},
          "focusable": ${item.focusable},
          "lastAccessed": ${item.lastAccessed}
        }
    """.trimIndent()

    private fun printHelp() {
        println(
            """
            Content Management Commands

            Usage: shared-core content <command> [options]

            Commands:
              list                List all content items
              get <id>           Get specific content item
              search <query>     Search for content
              access <id>        Mark content as accessed

            Options:
              --limit=N          Limit number of results (default: 50)
              --offset=N         Skip N results (default: 0)
              --format=FORMAT    Output format: json, table (default: table)

            Examples:
              shared-core content list --limit=10 --format=json
              shared-core content get content-001
              shared-core content search "comedy"
              shared-core content access content-123
            """.trimIndent(),
        )
    }
}
