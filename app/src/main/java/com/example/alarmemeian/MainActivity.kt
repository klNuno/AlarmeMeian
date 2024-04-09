package com.example.alarmemeian

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.alarmemeian.databinding.ActivityMainBinding
import com.example.alarmemeian.SMSController

// TODO 1: Add Settings Popup
// TODO 2: Add all the buttons

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var smsController: SMSController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)



        smsController = SMSController(this)
        smsController.loadAlarm()

        binding.ARMER.setOnClickListener {
            confirmationPopup("ARMER")
        }
        binding.DESARMER.setOnClickListener {
            confirmationPopup("DESARMER")
        }
        binding.ARMERPARTIEL.setOnClickListener {
            confirmationPopup("ARMERPARTIEL")
        }

    }

    // Confirmation popup for any buttons related to sending SMS
    private fun confirmationPopup(action: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage(getString(R.string.sms_send_confirmation))
        builder.setPositiveButton("Oui", DialogInterface.OnClickListener { _, _ ->
            smsController.sendSMS(action)
            Snackbar.make(view, "SMS envoyé", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}