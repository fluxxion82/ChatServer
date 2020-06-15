package ai.sterling.logger.java.di

import ai.sterling.logger.java.JavaLogger
import dagger.Module
import dagger.Provides

@Module
class JavaLoggingModule {

    @Provides
    fun androidLogger() = JavaLogger
}
