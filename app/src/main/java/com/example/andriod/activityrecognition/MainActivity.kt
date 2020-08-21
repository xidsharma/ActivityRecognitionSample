package com.example.andriod.activityrecognition

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.android.synthetic.main.activity_main.logRecyclerView

class MainActivity : AppCompatActivity() {
    private val request = ActivityTransitionRequest(listOf(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_FOOT)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_FOOT)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build(),
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
    ))

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TRANSITIONS_RECEIVER_ACTION) {
                val result = ActivityTransitionResult.extractResult(intent) ?: return

                for (event in result.transitionEvents) {
                    log.add("${event.transitionType} - ${event.activityType}")
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private val activityRecognitionClient by lazy { ActivityRecognition.getClient(this) }

    private val pendingIntent by lazy {
        PendingIntent.getBroadcast(this, 0, Intent(TRANSITIONS_RECEIVER_ACTION), 0)
    }

    private val log = mutableListOf<String>()

    private var isTracking = false

    private lateinit var adapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(receiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))

        adapter = LogAdapter(log)
        logRecyclerView.adapter = adapter
    }

    fun toggleTrackingActivity(view: View) {
        if (isTracking) stopTrackingActivity() else startTrackingActivity()
    }

    private fun startTrackingActivity() {
        if (canTrackActivity()) {
            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent).addOnSuccessListener {
                isTracking = true
                log.add("Started tracking activity")
                adapter.notifyDataSetChanged()
            }
        } else {
            requestActivityRecognitionPermission()
        }
    }

    private fun stopTrackingActivity() {
        activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent).addOnSuccessListener {
            isTracking = false
            log.add("Stopped tracking activity")
            adapter.notifyDataSetChanged()
        }
    }

    private fun canTrackActivity(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            true
        } else {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_ACTIVITY_RECOGNITION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingActivity()
            } else {
                Toast.makeText(
                    this,
                    "Activity recognition permission required to start tracking activity",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        if (isTracking) stopTrackingActivity()
        unregisterReceiver(receiver)

        super.onDestroy()
    }

    companion object {
        private const val TRANSITIONS_RECEIVER_ACTION = "transitions_receiver_action"
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 10001
    }
}