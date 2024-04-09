package com.meetsu.alarmemeian

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.view.View
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

        // Send the SMS with the code and the action to the alarm
        val smsManager = context.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(phoneNumber, null, "$messageprefix$code #$action", null, null)

        // Confirmation to the user
        Snackbar.make(view, context.getString(R.string.sms_sent), Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()

        // Start the SMS receiver to track the incoming SMS
        registerSmsReceiver()

        return true
    }

    // SMS receiver to track the incoming SMS
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == "android.provider.Telephony.SMS_RECEIVED") {
                    val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    processSms(smsMessages.toList())
                }
            }
        }

        // Process the SMS received and sends it as a snackbar
        private fun processSms(messages: List<SmsMessage>) {
            for (message in messages) {

                // Replace "certain_numero" with the desired phone number
                if (message.originatingAddress == phoneNumber) {
                    Snackbar.make(view, "Réponse de l'alarme : ${message.messageBody}", Snackbar.LENGTH_LONG).show()
                    // Unregister the SMS tracker after receiving the SMS
                    unregisterSmsReceiver()
                }
            }
        }
    }

    // Starts the SMS receiver to track for the next incoming SMS from the alarm
    private fun registerSmsReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        context.registerReceiver(smsReceiver, intentFilter)
    }

    // Stops the SMS receiver
    fun unregisterSmsReceiver() {
        context.unregisterReceiver(smsReceiver)
    }
}
