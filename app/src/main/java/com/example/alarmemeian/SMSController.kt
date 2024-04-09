package com.example.alarmemeian

import android.content.Context
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class SMSController(private val context: Context) {

    private var phoneNumber: String = ""
    private var code: String = ""
    private var messageprefix: String = "#ORION"
    private val view: View = (context as AppCompatActivity).findViewById<View>(android.R.id.content)

    // Save alarm settings on the phone
    fun saveAlarm(phoneNumber: String, code: String) {
        this.code = code
        this.phoneNumber = phoneNumber

        // Saving on phone
        val sharedPref = context.getSharedPreferences("alarm", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("code", code)
        editor.putString("phoneNumber", phoneNumber)
        editor.apply()
    }

    // Load alarm settings from the phone
    fun loadAlarm() {
        // If there is no saved alarm, tell the user
        try {
            val sharedPref = context.getSharedPreferences("alarm", Context.MODE_PRIVATE)
            code = sharedPref.getString("code", null) ?: code
            phoneNumber = sharedPref.getString("phoneNumber", null) ?: phoneNumber
        } catch (e: Exception) {
            noAlarmError()
        }
    }

    // Check if the alarm is configured
    private fun alarmConfigured(): Boolean {
        return code != "" && phoneNumber != ""
    }

    // Sends a toast to the user if there is no alarm configured
    private fun noAlarmError() {
        Snackbar.make(view, context.getString(R.string.no_alarm), Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    // Function to send SMS and all the checks before to ensure all the conditions are met
    fun sendSMS(action: String): Boolean {
        // Check if the alarm is configured
        if (!alarmConfigured()) {
            noAlarmError()
            return false
        }

        // Check privileges for sending SMS, if not granted, popup a permission request
        if (context.checkSelfPermission(android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            (context as AppCompatActivity).requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), 1)
            return false
        }

        // Send the SMS
        val smsManager = context.getSystemService(SmsManager::class.java)

        // Sending the SMS
        smsManager.sendTextMessage(phoneNumber, null, "$messageprefix$code #$action", null, null)

        Snackbar.make(view, context.getString(R.string.sms_sent), Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
        return true
    }


}