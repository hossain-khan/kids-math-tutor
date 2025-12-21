package dev.hossain.mathtutor.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dev.hossain.mathtutor.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import timber.log.Timber

/**
 * Firebase Analytics implementation of [AnalyticsService].
 *
 * This is the production implementation that sends analytics data to Firebase.
 * Uses Metro DI with [ContributesBinding] to automatically provide this implementation
 * when [AnalyticsService] is injected.
 *
 * @param context Application context for Firebase initialization
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class FirebaseAnalyticsService
    constructor(
        @ApplicationContext private val context: Context,
    ) : AnalyticsService {
        private val firebaseAnalytics: FirebaseAnalytics by lazy {
            FirebaseAnalytics.getInstance(context)
        }

        override fun logScreenView(
            screenName: String,
            screenClass: String,
            parameters: Map<String, Any>,
        ) {
            Timber.d("Analytics: Screen view - $screenName")

            val bundle =
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                    putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.ifEmpty { screenName })
                    parameters.forEach { (key, value) ->
                        putParameter(key, value)
                    }
                }

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }

        override fun logEvent(
            eventName: String,
            parameters: Map<String, Any>,
        ) {
            Timber.d("Analytics: Event - $eventName with ${parameters.size} parameters")

            val bundle =
                Bundle().apply {
                    parameters.forEach { (key, value) ->
                        putParameter(key, value)
                    }
                }

            firebaseAnalytics.logEvent(eventName, bundle)
        }

        override fun setUserProperty(
            propertyName: String,
            value: String,
        ) {
            Timber.d("Analytics: User property - $propertyName = $value")
            firebaseAnalytics.setUserProperty(propertyName, value)
        }

        override fun logError(
            error: Throwable,
            context: String,
            isFatal: Boolean,
        ) {
            Timber.e(error, "Analytics: Error - $context (fatal=$isFatal)")

            logEvent(
                AnalyticsEvent.ERROR_OCCURRED,
                mapOf(
                    AnalyticsParam.ERROR_MESSAGE to (error.message ?: "Unknown error"),
                    AnalyticsParam.ERROR_CONTEXT to context,
                    AnalyticsParam.IS_FATAL to isFatal,
                ),
            )
        }

        override fun setAnalyticsEnabled(enabled: Boolean) {
            Timber.d("Analytics: Collection enabled = $enabled")
            firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        }

        /**
         * Helper to add parameter to Bundle based on type.
         */
        private fun Bundle.putParameter(
            key: String,
            value: Any,
        ) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Double -> putDouble(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value.toString())
            }
        }
    }
