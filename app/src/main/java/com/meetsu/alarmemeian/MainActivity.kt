package com.meetsu.alarmemeian


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.meetsu.alarmemeian.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar


// TODO 1 : Vérifier que ça marche sur toutes tailles de tel/
// TODO 2 : Day / Night mode implémentation (retirer celui auto ou l'adapter pour l'appli)
// TODO 2.1 : Fix les couleurs des boutons pour qu'ils soient pas bizarres en night mode
// TODO 3 : Faire partie à propos qui va chercher la version de l'appli
// TODO NOW 4 : Renommez le package de l'appli et faire un icone (préparer l'appli pour apk en soit)
// TODO DONE 5 : Faire système d'attente de sms de la part de l'alarme
// TODO 5.1 : Mettre un gif de chargement sur l'action selectionnée le temps que le sms de
//            l'alarme arrive et bloquer les autres envois de sms
// TODO 6 : Faire un mini-readme
// TODO 7 : Repasser vite fait sur tout le code pour le commenter
// TODO DONE 8 : Remettre le num de tel et le code quand on en a déjà mit un dans les paramètres

class MainActivity  : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var smsController: SMSController

    // What's being done when launching the alarm app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        smsController = SMSController(this)
        smsController.loadAlarm()
        permissionCheck()
        allSetOnClickListener(binding.root)
    }

    // Set all the onClickListeners for the buttons
    private fun allSetOnClickListener(view: View) {
        val buttonActions = mapOf(
            binding.alarmOn to { confirmationPopup("ARMER", getString(R.string.alarm_on_confirmation)) },
            binding.alarmSensor to { confirmationPopup("DESARMER", getString(R.string.alarm_sensor_confirmation)) },
            binding.alarmOff to { confirmationPopup("ARMERPARTIEL", getString(R.string.alarm_off_confirmation)) },
            binding.alarmStatus to { smsController.sendSMS("STATUT") },
            binding.appInfo to { showInfoDialog() },
            binding.appSettings to { showSettingsDialog(view) }
        )

        for ((button, action) in buttonActions) {
            button.setOnClickListener { action() }
        }
    }
    // Confirmation popup for any buttons related to sending SMS
    private fun confirmationPopup(action: String, actionText : String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
               .setMessage(actionText)
               .setPositiveButton(getString(R.string.oui)) { _, _ ->
                   smsController.sendSMS(action)
               }
            .setNegativeButton(getString(R.string.non)) { _, _ -> }
            .show()
    }

    private fun showSettingsDialog(view: View) {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.settings_layout, null)

        // Set the phone number and code in the dialog if they are already set
        val phoneNumber = dialogView.findViewById<EditText>(R.id.phoneNumber)
        val code = dialogView.findViewById<EditText>(R.id.code)
        phoneNumber.setText(smsController.getPhoneNumber())
        code.setText(smsController.getCode())

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.settings_alarm))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { dialog: DialogInterface, _: Int ->
                smsController.saveAlarm(phoneNumber.text.toString(), code.text.toString())
                dialog.dismiss()
                Snackbar.make(view, getString(R.string.settings_saved), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    private fun showInfoDialog() {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.info_layout, null)
        val version = dialogView.findViewById<TextView>(R.id.version)
        version.text = getString(R.string.app_version)
        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    // Check privileges for sending SMS, receive SMS and read SMS. If any of them is not granted, ask for all of them
    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.READ_SMS
                ),
                1
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        smsController.unregisterSmsReceiver()
    }
}