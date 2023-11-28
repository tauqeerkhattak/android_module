package com.tauqeer.myattachapp

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


class MainActivity : AppCompatActivity() {
    private lateinit var flutterEngine: FlutterEngine
    private val flutterEngineID: String = "FLUTTER_CACHED_ENGINE"
    private val channelID: String = "MY_METHOD_CHANNEL_ID"
    private lateinit var methodChannel: MethodChannel
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flutterEngine = FlutterEngine(this)

        setContentView(R.layout.activity_main)

        // Cache the FlutterEngine to be used by FlutterActivity.
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
        FlutterEngineCache
            .getInstance()
            .put(flutterEngineID, flutterEngine)

        // Set Method Channel Call Handler
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelID)
        methodChannel.setMethodCallHandler { call, res ->
            onMethodCall(call, res)
        }

        prefs = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val count: Int = prefs.getInt("data", -1)
        if (count < 0) {
            val editor: Editor = prefs.edit()
            editor.putInt("data", 0)
            editor.apply()
        }

        val startFlutterButton: AppCompatButton = findViewById(R.id.startFlutterButton)

        startFlutterButton.setOnClickListener {
            try {
                Toast.makeText(this, "Button pressed!", Toast.LENGTH_LONG).show()
                startActivity(
                    FlutterActivity.withCachedEngine(flutterEngineID).build(this)
                )
                Log.d("LOG", "FLUTTER ACTIVITY LAUNCHED")
            } catch (e: Exception) {
                Log.wtf("ERROR", "RECEIVED ERROR")
                if (e.message != null) {
                    Log.wtf("ERROR", e.message)
                }
            }
        }
    }

    private fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.d("METHOD CALLED", "NAME IS ${call.method}")
        if (call.method.equals("getData")) {
            val count: Int = prefs.getInt("data", 0)
            val editor: Editor = prefs.edit()
            editor.putInt("data", count + 1)
            editor.apply()
            result.success("Data in preferences is: $count")
        } else {
            Log.d("ERROR", "No method exists with name ${call.method}")
            result.success("No method found!")
        }
    }
}