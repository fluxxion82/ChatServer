package ai.sterling.logger.java

import ai.sterling.logger.Logger
import java.util.logging.Level
import java.util.logging.Logger as JavaLog

object JavaLogger : Logger {
    private val anonLogger = JavaLog.getAnonymousLogger()

    override fun log(
        priority: Logger.Priority,
        explicitTag: String?,
        inferredTag: String,
        message: String?,
        throwable: Throwable?,
        properties: Map<String, String>?
    ) {
        val tagToLog = explicitTag ?: inferredTag
        val messageToLog = message ?: throwable?.message.orEmpty()

        when (priority) {
            Logger.Priority.VERBOSE -> {
                throwable?.let {
                    anonLogger.log(Level.ALL, messageToLog, arrayOf(tagToLog, it))
                } ?: anonLogger.log(Level.ALL, messageToLog, tagToLog)
            }
            Logger.Priority.INFO -> {
                throwable?.let {
                    anonLogger.log(Level.INFO, messageToLog, arrayOf(tagToLog, it))
                } ?: anonLogger.log(Level.INFO, messageToLog, tagToLog)
            }
            Logger.Priority.DEBUG -> {
                throwable?.let {
                    anonLogger.log(Level.CONFIG, messageToLog, arrayOf(tagToLog, it))
                } ?: anonLogger.log(Level.CONFIG, messageToLog, tagToLog)
            }
            Logger.Priority.WARNING -> {
                throwable?.let {
                    anonLogger.log(Level.WARNING, messageToLog, arrayOf(tagToLog, it))
                } ?: anonLogger.log(Level.WARNING, messageToLog, tagToLog)
            }
            Logger.Priority.ERROR -> {
                throwable?.let {
                    anonLogger.log(Level.SEVERE, messageToLog, arrayOf(tagToLog, it))
                } ?: anonLogger.log(Level.SEVERE, messageToLog, tagToLog)
            }
            Logger.Priority.WTF -> {
                throwable?.let {
                    anonLogger.log(Level.SEVERE, messageToLog, arrayOf(tagToLog, it))
                } ?: anonLogger.log(Level.SEVERE, messageToLog, tagToLog)
            }
        }.let { }
    }
}
