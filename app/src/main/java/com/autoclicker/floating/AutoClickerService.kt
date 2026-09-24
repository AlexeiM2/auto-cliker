package com.autoclicker.floating

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageButton
import android.widget.Toast
import kotlin.math.abs

/**
 * Servicio de Accesibilidad con Ventana Flotante Sobrepuesta.
 * - Icono flotante conmutable entre Play y Pausa.
 * - Arrastrable a cualquier zona de la pantalla.
 * - Toques automáticos periódicos cada 2 segundos (2000 ms).
 */
class AutoClickerService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnClose: View
    private var targetView: View? = null

    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    // Periodo solicitado: 2000 ms (2 segundos)
    var clickInterval: Long = 2000L
    
    // Coordenadas del objetivo a presionar (por defecto el centro o la mira)
    var targetX: Float = 0f
    var targetY: Float = 0f

    // Vibrador opcional para sentir el toque
    private var vibrator: Vibrator? = null

    companion object {
        var instance: AutoClickerService? = null
        const val ACTION_STOP_SERVICE = "com.autoclicker.floating.STOP"
        const val EXTRA_INTERVAL = "extra_interval"
    }

    private val clickRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                performTap()
                // Programa el siguiente toque en 2 segundos
                handler.postDelayed(this, clickInterval)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        // Coordenadas iniciales: centro de la pantalla
        val metrics = resources.displayMetrics
        targetX = (metrics.widthPixels / 2).toFloat()
        targetY = (metrics.heightPixels / 2).toFloat()

        initFloatingControls()
        initTargetPointer()

        Toast.makeText(this, "AutoClicker Activo: 2 seg por toque", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun initFloatingControls() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_control, null)
        btnPlayPause = floatingView.findViewById(R.id.btn_play_pause)
        btnClose = floatingView.findViewById(R.id.btn_close)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 200
        }

        windowManager.addView(floatingView, params)

        // Arrastre fluido de la burbuja sin bloquear los clics
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        togglePlayPause()
                    }
                    true
                }
                else -> false
            }
        }

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnClose.setOnClickListener {
            stopClicking()
            disableSelf()
        }
    }

    @SuppressLint("InflateParams")
    private fun initTargetPointer() {
        // Mira de objetivo flotante que indica dónde se presionará en pantalla
        targetView = LayoutInflater.from(this).inflate(R.layout.floating_target, null)
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val targetParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = targetX.toInt() - 40
            y = targetY.toInt() - 40
        }

        windowManager.addView(targetView, targetParams)

        // Arrastrar la mira hacia el botón exacto del juego
        var initX = 0
        var initY = 0
        var touchStartX = 0f
        var touchStartY = 0f

        targetView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = targetParams.x
                    initY = targetParams.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    targetParams.x = initX + (event.rawX - touchStartX).toInt()
                    targetParams.y = initY + (event.rawY - touchStartY).toInt()
                    windowManager.updateViewLayout(targetView, targetParams)
                    // Actualizar las coordenadas reales del toque
                    targetX = (targetParams.x + 40).toFloat()
                    targetY = (targetParams.y + 40).toFloat()
                    true
                }
                else -> false
            }
        }
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        if (isPlaying) {
            // Cambia el icono a PAUSA y empieza a presionar cada 2 segundos
            btnPlayPause.setImageResource(R.drawable.ic_pause)
            btnPlayPause.contentDescription = "Pausa"
            handler.post(clickRunnable)
            Toast.makeText(this, "▶ Iniciado (cada 2s)", Toast.LENGTH_SHORT).show()
        } else {
            // Cambia el icono a PLAY y detiene los toques
            btnPlayPause.setImageResource(R.drawable.ic_play)
            btnPlayPause.contentDescription = "Play"
            handler.removeCallbacks(clickRunnable)
            Toast.makeText(this, "⏸ Pausado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopClicking() {
        isPlaying = false
        handler.removeCallbacks(clickRunnable)
    }

    /**
     * Inyecta el toque en la pantalla mediante la API GestureDescription de Android
     */
    private fun performTap() {
        val path = Path().apply {
            moveTo(targetX, targetY)
        }

        val gestureBuilder = GestureDescription.Builder()
        // Duración del toque físico: 15 milisegundos
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 15))

        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                vibrateBriefly()
            }
        }, null)
    }

    private fun vibrateBriefly() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(12)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == ACTION_STOP_SERVICE) {
                stopClicking()
                disableSelf()
            }
            val newInterval = it.getLongExtra(EXTRA_INTERVAL, -1L)
            if (newInterval > 0) {
                clickInterval = newInterval
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        stopClicking()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClicking()
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
        targetView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        instance = null
    }
}