package com.odougle.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.odougle.sms.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var smsSenderBroadcast : SmsSenderBroadCast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if(tm.phoneType == TelephonyManager.PHONE_TYPE_NONE){
            Toast.makeText(this, "Dispositivo não suporta SMS", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        val permissions = arrayOf(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.RECEIVE_SMS
        )
        val hasPermissions = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if(!hasPermissions){
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
        smsSenderBroadcast = SmsSenderBroadcast()
        registerReceiver(smsSenderBroadcast, IntentFilter(ACTION_SEND))
        registerReceiver(smsSenderBroadcast, IntentFilter(ACTION_DELIVERED))
    }

    override fun onPause() {
        super.onPause()
        if(smsSenderBroadcast != null)
            unregisterReceiver(smsSenderBroadcast)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(!grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
            Toast.makeText(this, "Voce necessita aceitar as permissoes", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun sendSmsClick(view: android.view.View) {
        val pitSent = PendingIntent.getBroadcast(
            this, 0, Intent(ACTION_SENT), 0
        )
        val pitDelivered = PendingIntent.getBroadcast(
            this, 0, Intent(ACTION_DELIVERED), 0
        )
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            binding.edtPhoneNumber.text.toString(), null,
            binding.edtMessage.text.toString(),
            pitSent,
            pitDelivered
        )

    }
}