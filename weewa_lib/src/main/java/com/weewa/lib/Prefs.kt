package com.weewa.lib

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object Prefs {
    private val SERVER_PORT = "SERVER_PORT"
    private val SERVER_PORT2 = "SERVER_PORT2"
    private val DOWNLOAD_PATH = "DOWNLOAD_PATH"

    fun defaultPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    inline fun SharedPreferences.Editor.put(pair: Pair<String, Any>) {
        val key = pair.first
        when (val value = pair.second) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
    }

    var SharedPreferences.serverPort
        get() = getInt(SERVER_PORT, 0)
        set(value) {
            editMe {
                it.put(SERVER_PORT to value)
            }
        }

    var SharedPreferences.serverPort2
        get() = getInt(SERVER_PORT2, 0)
        set(value) {
            editMe {
                it.put(SERVER_PORT2 to value)
            }
        }

    var SharedPreferences.downloadPath
        get() = getString(DOWNLOAD_PATH, WeewaLib.shared().getFilesDir())
        set(value) {
            editMe {
                it.put(DOWNLOAD_PATH to (value ?: ""))
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                it.clear()
            }
        }
}