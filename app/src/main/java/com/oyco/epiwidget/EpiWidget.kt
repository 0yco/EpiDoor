package com.oyco.epiwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.oyco.epiwidget.EpiWidget.Companion.lastToken
import okhttp3.*
import java.io.IOException

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [EpiWidgetConfigureActivity]
 */
class EpiWidget : AppWidgetProvider() {

    companion object {
        @RequiresApi(Build.VERSION_CODES.S)
        fun updateAppWidget(settingsActivity: SettingsActivity, appWidgetManager: AppWidgetManager?, appWidgetId: Int) {
            if (appWidgetManager != null) {
                com.oyco.epiwidget.updateAppWidget(settingsActivity, appWidgetManager, appWidgetId)
            }
        }

        const val ACTION_DOOR_OPEN_CLICKED = "com.oyco.experience.ACTION_DOOR_OPEN_CLICKED"
        var lastToken: String? = null
    }

    private val handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_DOOR_OPEN_CLICKED) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(intent.data?.toString() ?: "")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                    println("Error making GET request: ${e.message}")
                    handler.post {
                        Toast.makeText(context, "Error opening door", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle success
                    println("GET request successful: ${response.body?.string()}")
                    handler.post {
                        Toast.makeText(context, "Door opened", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun onEnabled(context: Context) {
        Toast.makeText(context, "Force exit if buttons are not updated in the Widget", Toast.LENGTH_LONG).show()
    }
}

@RequiresApi(Build.VERSION_CODES.S)
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.epi_widget)

    // Get the token
    lastToken = PreferenceManager.getDefaultSharedPreferences(context).getString("token", null)

    // Set up buttons visibility
    views.setViewVisibility(R.id.enter_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_enter", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.foyer_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_foyer", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.sm1_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_sm1", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.sm2_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_sm2", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.meetup_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_meetup", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.hub_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_hub", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.stream_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_stream", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.incubateur_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_incubateur", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.admissions_button, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_admissions", true)) View.VISIBLE else View.GONE)

    // Set up columns visibility
    views.setViewVisibility(R.id.row1, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_enter", true) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_foyer", true) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_hub", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.column1, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_sm1", true) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_sm2", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.column2, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_meetup", true) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_stream", true)) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.column3, if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_incubateur", true) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_admissions", true)) View.VISIBLE else View.GONE)


    // Set up the click listeners
    views.setOnClickPendingIntent(R.id.enter_button, getPendingIntent(context, "4eme"))
    views.setOnClickPendingIntent(R.id.foyer_button, getPendingIntent(context, "Foyer"))
    views.setOnClickPendingIntent(R.id.sm1_button, getPendingIntent(context, "SM1"))
    views.setOnClickPendingIntent(R.id.sm2_button, getPendingIntent(context, "SM2"))
    views.setOnClickPendingIntent(R.id.meetup_button, getPendingIntent(context, "Meetup"))
    views.setOnClickPendingIntent(R.id.hub_button, getPendingIntent(context, "HUB"))
    views.setOnClickPendingIntent(R.id.stream_button, getPendingIntent(context, "Stream"))
    views.setOnClickPendingIntent(R.id.incubateur_button, getPendingIntent(context, "Incubateur"))
    views.setOnClickPendingIntent(R.id.admissions_button, getPendingIntent(context, "Admissions"))

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun getPendingIntent(context: Context, doorName: String): PendingIntent {
    val intent = Intent(context, EpiWidget::class.java).apply {
        action = EpiWidget.ACTION_DOOR_OPEN_CLICKED
        data = Uri.parse("https://tekme.eu/api/doors_open?token=$lastToken&door_name=$doorName")
    }
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
}