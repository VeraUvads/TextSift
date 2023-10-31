package com.uva.metaquotestest.data

import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class RemoteDataSource {

    fun upload(uri: String, mask: String, appDirectory: File) = callbackFlow<String> {
        val filePath = "results.log"
        val outputFilePath = File(appDirectory, filePath)
        val fileWriter = FileWriter(outputFilePath)
        val url: URL
        var urlConnection: HttpURLConnection? = null
        try {
            url = URL(uri)
            urlConnection = url
                .openConnection() as HttpURLConnection
            val `in` = urlConnection.inputStream

            BufferedReader(InputStreamReader(`in`)).use { reader ->
                val regexPattern = createRegexFromMask(mask)
                var line: String? = reader.readLine()
                while (line != null && isActive) {
                    val matcher = regexPattern.matcher(line)
                    if (matcher.find()) {
                        fileWriter.write(line)
                        fileWriter.write("\n")
                        trySend(line)
                    }
                    line = reader.readLine()
                }
            }
        } catch (exception: Exception) {
            throw java.lang.Exception(exception.message)
        } finally {
            urlConnection?.disconnect()
            fileWriter.close()
            close()
        }
    }

    private fun createRegexFromMask(mask: String): Pattern {
        val regexPattern = StringBuilder()
        for (char in mask) {
            when (char) {
                '*' -> regexPattern.append(".*")
                '?' -> regexPattern.append(".")
                else -> regexPattern.append(Pattern.quote(char.toString()))
            }
        }
        return Pattern.compile(regexPattern.toString())
    }
}
