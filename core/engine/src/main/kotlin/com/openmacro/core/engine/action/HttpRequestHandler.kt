package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.HttpRequestConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class HttpRequestHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "http_request"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<HttpRequestConfig>(config.configJson)
        if (parsed.url.isBlank()) return

        val responseBody = withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = URL(parsed.url).openConnection() as HttpURLConnection
                connection.requestMethod = parsed.method.uppercase()
                connection.connectTimeout = 30_000
                connection.readTimeout = 30_000

                // Parse and set headers
                if (parsed.headers.isNotBlank()) {
                    for (line in parsed.headers.lines()) {
                        val colonIdx = line.indexOf(':')
                        if (colonIdx > 0) {
                            val key = line.substring(0, colonIdx).trim()
                            val value = line.substring(colonIdx + 1).trim()
                            connection.setRequestProperty(key, value)
                        }
                    }
                }

                // Write body for methods that support it
                if (parsed.body.isNotBlank() && parsed.method.uppercase() in listOf("POST", "PUT", "PATCH")) {
                    connection.doOutput = true
                    connection.outputStream.bufferedWriter().use { it.write(parsed.body) }
                }

                // Read response
                val inputStream = try {
                    connection.inputStream
                } catch (_: Exception) {
                    connection.errorStream
                }

                val reader = BufferedReader(InputStreamReader(inputStream ?: return@withContext ""))
                val response = reader.readText()
                reader.close()

                val statusCode = connection.responseCode
                context.localVariables["http_response_code"] = statusCode.toString()

                response
            } catch (e: Exception) {
                Log.e(TAG, "HTTP request failed", e)
                "ERROR: ${e.message}"
            } finally {
                connection?.disconnect()
            }
        }

        // Store response in local variables (always available as magic text)
        context.localVariables["http_response_body"] = responseBody

        // Also store in named variable if specified
        if (parsed.saveResponseTo.isNotBlank()) {
            val varName = parsed.saveResponseTo
            if (varName.startsWith("lv_")) {
                context.localVariables[varName] = responseBody
            } else {
                context.variableStore?.setGlobal(varName, responseBody)
            }
        }
    }

    companion object {
        private const val TAG = "HttpRequestHandler"
    }
}
