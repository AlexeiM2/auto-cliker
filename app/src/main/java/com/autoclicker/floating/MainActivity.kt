package com.autoclicker.floating

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatusOverlay: TextView
    private lateinit var tvStatusAccessibility: TextView
    private lateinit var etInterval: EditText
    private lateinit var btnToggleService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatusOverlay = findViewById(R.id.tv_status_overlay)
        tvStatusAccessibility = findViewById(R.id.tv_status_accessibility)
        etInterval = findViewById(R.id.et_interval)
        btnToggleService = findViewById(R.id.btn_toggle_service)

        val btnReqOverlay = findViewById<Button>(R.id.btn_request_overlay)
        val btnReqAccessibility = findViewById<Button>(R.id.btn_request_accessibility)

        // Por defecto: 2000 milisegundos (2 segundos)
        etInterval.setText("2000")

        btnReqOverlay.setOnClickListener {
            requestOverlayPermission()
        }

        btnReqAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        btnToggleService.setOnClickListener {
            if (isAccessibilityServiceEnabled()) {
                val interval = etInterval.text.toString().toLongOrNull() ?: 2000L
                AutoClickerService.instance?.let { service ->
                    service.clickInterval = interval
                    Toast.makeText(this, "Intervalo fijado en ${interval}ms", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Primero activa el Servicio de Accesibilidad", Toast.LENGTH_LONG).show()
                openAccessibilitySettings()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        val hasAccessibility = isAccessibilityServiceEnabled()

        tvStatusOverlay.text = if (hasOverlay) "✔ Concedido" else "❌ Pendiente"
        tvStatusOverlay.setTextColor(if (hasOverlay) 0xFF4CAF50.toInt() else 0xFFE91E63.toInt())

        tvStatusAccessibility.text = if (hasAccessibility) "✔ Activado" else "❌ Desactivado"
        tvStatusAccessibility.setTextColor(if (hasAccessibility) 0xFF4CAF50.toInt() else 0xFFE91E63.toInt())

        if (hasOverlay && hasAccessibility) {
            btnToggleService.isEnabled = true
            btnToggleService.text = "¡AutoClicker Listo! Abre tu juego"
        } else {
            btnToggleService.isEnabled = false
            btnToggleService.text = "Concede los permisos arriba"
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permiso de superposición ya concedido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Busca 'AutoClicker Flotante' y actívalo", Toast.LENGTH_LONG).show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == packageName &&
                service.resolveInfo.serviceInfo.name.contains("AutoClickerService")) {
                return true
            }
        }
        return false
    }
}