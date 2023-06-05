package com.oyco.epiwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
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
        const val ACTION_CHANGE_PANEL_CLICKED = "com.oyco.experience.ACTION_CHANGE_PANEL_CLICKED"
        var lastToken: String? = null
        var panel: String? = "EpiDoor"
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

    @RequiresApi(Build.VERSION_CODES.S)
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
        } else if (intent.action == ACTION_CHANGE_PANEL_CLICKED) {
            val views = RemoteViews(context.packageName, R.layout.epi_widget)
            if (panel == "EpiDoor") {
                panel = "EpiMouli"
                views.setViewVisibility(R.id.epimouli_layout, View.VISIBLE)
                views.setViewVisibility(R.id.refresh_button, View.VISIBLE)
                views.setViewVisibility(R.id.epidoor_layout, View.GONE)
            } else if (panel == "EpiMouli") {
                panel = "EpiDoor"
                views.setViewVisibility(R.id.epidoor_layout, View.VISIBLE)
                views.setViewVisibility(R.id.epimouli_layout, View.GONE)
                views.setViewVisibility(R.id.refresh_button, View.GONE)
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, EpiWidget::class.java))
            appWidgetManager.updateAppWidget(appWidgetIds, views)
        }
    }

    override fun onEnabled(context: Context) {
        Toast.makeText(context, "Don't forget to configure your widget in the settings", Toast.LENGTH_LONG).show()
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
    val intent = Intent(context, EpiWidget::class.java).apply {
        action = EpiWidget.ACTION_CHANGE_PANEL_CLICKED
    }
    views.setOnClickPendingIntent(R.id.switch_panel_button, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE))

    views.setOnClickPendingIntent(R.id.enter_button, getPendingIntentDoorOpen(context, "4eme"))
    views.setOnClickPendingIntent(R.id.foyer_button, getPendingIntentDoorOpen(context, "Foyer"))
    views.setOnClickPendingIntent(R.id.sm1_button, getPendingIntentDoorOpen(context, "SM1"))
    views.setOnClickPendingIntent(R.id.sm2_button, getPendingIntentDoorOpen(context, "SM2"))
    views.setOnClickPendingIntent(R.id.meetup_button, getPendingIntentDoorOpen(context, "Meetup"))
    views.setOnClickPendingIntent(R.id.hub_button, getPendingIntentDoorOpen(context, "HUB"))
    views.setOnClickPendingIntent(R.id.stream_button, getPendingIntentDoorOpen(context, "Stream"))
    views.setOnClickPendingIntent(R.id.incubateur_button, getPendingIntentDoorOpen(context, "Incubateur"))
    views.setOnClickPendingIntent(R.id.admissions_button, getPendingIntentDoorOpen(context, "Admissions"))

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun getPendingIntentDoorOpen(context: Context, doorName: String): PendingIntent {
    val intent = Intent(context, EpiWidget::class.java).apply {
        action = EpiWidget.ACTION_DOOR_OPEN_CLICKED
        data = Uri.parse("https://epilogue.arykow.com/api/doors_open?token=$lastToken&door_name=$doorName")
    }
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
}
