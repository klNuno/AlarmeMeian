package com.meetsu.alarmemeian.sms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.core.content.ContextCompat
import com.meetsu.alarmemeian.data.model.AlarmConfig
import com.meetsu.alarmemeian.sms.protocol.AlarmAction
import com.meetsu.alarmemeian.sms.protocol.ProtocolFactory

class SmsService(private val context: Context) {

    private var observerRegistered = false
    private var onResponseReceived: ((String) -> Unit)? = null
    private var currentPhoneNumber: String = ""
    private var commandSentTime: Long = 0

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            checkInboxForResponse()
        }
    }

    private fun checkInboxForResponse() {
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                "${Telephony.Sms.DATE} > ?",
                arrayOf(commandSentTime.toString()),
                "${Telephony.Sms.DATE} DESC"
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val address = it.getString(0) ?: continue
                    val body = it.getString(1) ?: continue
                    if (PhoneNumberUtils.compare(address, currentPhoneNumber)) {
                        onResponseReceived?.invoke(body)
                        unregisterAll()
                        return
                    }
                }
            }
        } catch (_: SecurityException) {
            // READ_SMS not granted
        }
    }

    fun canReadSms(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun sendCommand(config: AlarmConfig, action: AlarmAction, onResponse: (String) -> Unit): Boolean {
        if (!config.isConfigured) return false

        val protocol = ProtocolFactory.getProtocol(config.brand)
        val message = protocol.formatCommand(config.code, action)

        currentPhoneNumber = config.phoneNumber
        onResponseReceived = onResponse
        commandSentTime = System.currentTimeMillis()

        // Always send via Intent — no SEND_SMS permission needed
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${config.phoneNumber}")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        // Listen for response via ContentObserver (READ_SMS)
        if (canReadSms()) {
            registerObserver()
        }
        return true
    }

    private fun registerObserver() {
        if (observerRegistered) return
        try {
            context.contentResolver.registerContentObserver(
                Telephony.Sms.Inbox.CONTENT_URI,
                true,
                smsObserver
            )
            observerRegistered = true
        } catch (_: Exception) {}
    }

    fun unregisterAll() {
        if (!observerRegistered) return
        try {
            context.contentResolver.unregisterContentObserver(smsObserver)
        } catch (_: Exception) {}
        observerRegistered = false
        onResponseReceived = null
    }
}
