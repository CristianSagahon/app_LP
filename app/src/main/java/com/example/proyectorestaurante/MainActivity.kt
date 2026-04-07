package com.example.proyectorestaurante

import android.content.Intent
import android.os.Bundle
import android.view.View // <-- Importamos View porque nuestras tarjetas actúan como botones
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajuste automático de los márgenes de la pantalla (evita que el diseño se meta bajo la cámara/notch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*
         * 1. CONEXIÓN CON EL DISEÑO VISUAL
         * Como en el XML usamos MaterialCardView en lugar de Button para lograr ese diseño moderno,
         * aquí en Kotlin los tratamos como "View" genéricos. Siguen teniendo la capacidad de hacer clic.
         */
        val botonCliente = findViewById<View>(R.id.cardCliente)
        val botonMesero = findViewById<View>(R.id.cardMesero)
        val botonAdmin = findViewById<View>(R.id.cardAdmin)

        /*
         * 2. LÓGICA DE CLICS
         * ¿Qué pasa cuando el usuario toca alguna de las tarjetas?
         */

        // Bloque del Cliente
        botonCliente.setOnClickListener {
            // Creamos el "vehículo" (Intent) para viajar de esta pantalla a la del Cliente
            val intento = Intent(this, ClienteActivity::class.java)
            startActivity(intento) // ¡Pisamos el acelerador y arrancamos la pantalla!
        }

        // Bloque del Mesero (Provisional)
        botonMesero.setOnClickListener {
            // Mostramos un mensaje temporal en la parte de abajo de la pantalla
            Toast.makeText(this, "Área de Meseros en construcción...", Toast.LENGTH_SHORT).show()
        }

        // Bloque del Admin (Provisional)
        botonAdmin.setOnClickListener {
            // Mostramos un mensaje temporal
            Toast.makeText(this, "Área de Admin en construcción...", Toast.LENGTH_SHORT).show()
        }
    }
}