package com.example.proyectorestaurante

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.util.*

class ClienteActivity : AppCompatActivity() {

    // Instancia de nuestra Base de Datos Firebase
    private val db = FirebaseFirestore.getInstance()

    // Variables de UI
    lateinit var layoutUnirse: LinearLayout
    lateinit var layoutCrear: LinearLayout
    lateinit var layoutMostrarQR: LinearLayout

    lateinit var btnTabUnirse: Button
    lateinit var btnTabCrear: Button

    // Variables de datos
    var codigoMesaActual: String? = null
    var idUsuarioActual: String? = null

    // Configuramos el escáner (¡Ahora con nuestra clase vertical!)
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            // El usuario escaneó el QR, lo pegamos en el cuadrito de texto automáticamente
            findViewById<EditText>(R.id.etCodigoMesa).setText(result.contents)
            Toast.makeText(this, "Código detectado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente)

        // 1. Conectar vistas de Layouts (Secciones)
        layoutUnirse = findViewById(R.id.layoutUnirse)
        layoutCrear = findViewById(R.id.layoutCrear)
        layoutMostrarQR = findViewById(R.id.layoutMostrarQR)

        // 2. Conectar Botones y Textos
        btnTabUnirse = findViewById(R.id.btnTabUnirse)
        btnTabCrear = findViewById(R.id.btnTabCrear)
        val btnEscanear = findViewById<LinearLayout>(R.id.btnEscanear)

        val etNombreUnirse = findViewById<EditText>(R.id.etNombreUnirse)
        val etCodigoMesa = findViewById<EditText>(R.id.etCodigoMesa)
        val btnAccionUnirse = findViewById<Button>(R.id.btnAccionUnirse)

        val etNombreCrear = findViewById<EditText>(R.id.etNombreCrear)
        val etNombreMesa = findViewById<EditText>(R.id.etNombreMesa)
        val btnAccionCrear = findViewById<Button>(R.id.btnAccionCrear)

        // --- LÓGICA DE LAS PESTAÑAS (TABS) ---
        btnTabUnirse.setOnClickListener {
            // Cambiamos colores para mostrar que está activo
            btnTabUnirse.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
            btnTabUnirse.setTextColor(android.graphics.Color.parseColor("#000000"))
            btnTabCrear.setBackgroundColor(android.graphics.Color.parseColor("#F0F2F5"))
            btnTabCrear.setTextColor(android.graphics.Color.parseColor("#757575"))

            layoutUnirse.visibility = View.VISIBLE
            layoutCrear.visibility = View.GONE
            layoutMostrarQR.visibility = View.GONE
        }

        btnTabCrear.setOnClickListener {
            btnTabCrear.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
            btnTabCrear.setTextColor(android.graphics.Color.parseColor("#000000"))
            btnTabUnirse.setBackgroundColor(android.graphics.Color.parseColor("#F0F2F5"))
            btnTabUnirse.setTextColor(android.graphics.Color.parseColor("#757575"))

            layoutCrear.visibility = View.VISIBLE
            layoutUnirse.visibility = View.GONE
            layoutMostrarQR.visibility = View.GONE
        }

        // --- LÓGICA: BOTÓN ESCANEAR CÁMARA ---
        btnEscanear.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Enfoca el código de la mesa")
            options.setBeepEnabled(true)
            // ¡MAGIA! Usamos nuestra clase fantasma para bloquear la pantalla en vertical
            options.setCaptureActivity(CaptureActivityPortrait::class.java)
            options.setOrientationLocked(true)
            barcodeLauncher.launch(options)
        }

        // --- LÓGICA: CREAR MESA (ANFITRIÓN) ---
        btnAccionCrear.setOnClickListener {
            val nombreAnfitrion = etNombreCrear.text.toString()
            if (nombreAnfitrion.isEmpty()) {
                Toast.makeText(this, "Escribe tu nombre primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAccionCrear.text = "Creando..."
            btnAccionCrear.isEnabled = false

            // 1. Generamos un código corto aleatorio (Ej: X9B2MW)
            val codigoGenerado = UUID.randomUUID().toString().substring(0, 6).uppercase()
            codigoMesaActual = codigoGenerado

            // 2. Guardamos al Anfitrión en Firebase (¡Añadimos el id_mesa!)
            val nuevoUsuario = Usuario(name = nombreAnfitrion, tipo = 3, id_mesa = codigoGenerado) // 3=Cliente

            db.collection("usuarios").add(nuevoUsuario)
                .addOnSuccessListener { docUser ->
                    idUsuarioActual = docUser.id

                    // 3. Guardamos la Mesa en Firebase unida a este anfitrión
                    val nuevaMesa = Mesa(
                        codigo = codigoGenerado,
                        id_user_creador = idUsuarioActual!!,
                        estado_mesa = 1
                    )

                    db.collection("mesas").document(codigoGenerado).set(nuevaMesa).addOnSuccessListener {
                        // ¡Éxito! Mostramos la pantalla del QR
                        mostrarPantallaQR(codigoGenerado)
                    }
                }
                .addOnFailureListener { error ->
                    // ¡AQUÍ VA EL ERROR DE FIREBASE!
                    btnAccionCrear.text = "+ Crear Mesa"
                    btnAccionCrear.isEnabled = true
                    Toast.makeText(this, "Error al conectar: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }

        // --- LÓGICA: UNIRSE A MESA EXISTENTE ---
        btnAccionUnirse.setOnClickListener {
            val nombreInvitado = etNombreUnirse.text.toString()
            val codigoLeido = etCodigoMesa.text.toString().uppercase()

            if (nombreInvitado.isEmpty() || codigoLeido.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAccionUnirse.text = "Buscando..."

            // Buscamos si la mesa existe en Firebase
            db.collection("mesas").document(codigoLeido).get().addOnSuccessListener { documento ->
                if (documento.exists()) {
                    // La mesa sí existe. Creamos al usuario invitado. (¡Añadimos el id_mesa!)
                    val nuevoUsuario = Usuario(name = nombreInvitado, tipo = 3, id_mesa = codigoLeido)
                    db.collection("usuarios").add(nuevoUsuario).addOnSuccessListener { docUser ->
                        // Teletransporte al menú
                        irAlMenu(codigoLeido, docUser.id)
                    }
                } else {
                    Toast.makeText(this, "Código de mesa inválido", Toast.LENGTH_LONG).show()
                    btnAccionUnirse.text = "Unirse a la Mesa"
                }
            }
        }

        // Botón final del anfitrión para entrar
        findViewById<Button>(R.id.btnEntrarAnfitrion).setOnClickListener {
            irAlMenu(codigoMesaActual!!, idUsuarioActual!!)
        }

        // Botón de Volver (Flecha)
        findViewById<ImageView>(R.id.btnVolver).setOnClickListener {
            finish() // Cierra esta pantalla y regresa a MainActivity
        }
    }

    private fun mostrarPantallaQR(codigo: String) {
        layoutCrear.visibility = View.GONE
        layoutMostrarQR.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvCodigoGenerado).text = codigo

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmapFormatoQR: Bitmap = barcodeEncoder.encodeBitmap(codigo, BarcodeFormat.QR_CODE, 500, 500)
            findViewById<ImageView>(R.id.ivQRGenerado).setImageBitmap(bitmapFormatoQR)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun irAlMenu(idMesa: String, idUser: String) {
        val intentoMenu = Intent(this, MenuMesaActivity::class.java)
        intentoMenu.putExtra("ID_DE_LA_MESA", idMesa)
        intentoMenu.putExtra("ID_DEL_USUARIO", idUser)
        startActivity(intentoMenu)
        finish() // Cerramos esta pantalla para que no puedan volver atrás con el botón del celular
    }
}