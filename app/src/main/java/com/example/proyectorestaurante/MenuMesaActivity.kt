package com.example.proyectorestaurante

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class MenuMesaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var idMesa: String
    private lateinit var idUsuario: String
    private var idAnfitrionActual: String = ""

    private lateinit var tvNombreMesa: TextView
    private lateinit var tvCodigoMesaCard: TextView
    private lateinit var tvSaludoUsuario: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_mesa)

        idMesa = intent.getStringExtra("ID_DE_LA_MESA") ?: ""
        idUsuario = intent.getStringExtra("ID_DEL_USUARIO") ?: ""

        tvNombreMesa = findViewById(R.id.tvNombreMesa)
        tvCodigoMesaCard = findViewById(R.id.tvCodigoMesaCard)
        tvSaludoUsuario = findViewById(R.id.tvSaludoUsuario)

        cargarDatosMesa()
        cargarDatosUsuario()

        findViewById<ImageView>(R.id.btnSalir).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnAbrirMenu).setOnClickListener {
            mostrarMenuDeslizable() // ¡Abre el nuevo menú!
        }

        findViewById<ImageView>(R.id.btnParticipantes).setOnClickListener {
            mostrarParticipantes()
        }
    }

    // ==========================================
    // 1. LÓGICA DEL MENÚ DESLIZABLE (BOTTOM SHEET)
    // ==========================================
    private fun mostrarMenuDeslizable() {
        // Creamos la "sábana" que sube desde abajo
        val bottomSheetDialog = BottomSheetDialog(this)
        val vistaMenu = layoutInflater.inflate(R.layout.layout_bottom_menu, null)
        bottomSheetDialog.setContentView(vistaMenu)

        val contenedorPlatillos = vistaMenu.findViewById<LinearLayout>(R.id.contenedorPlatillos)

        // Lista de prueba simulando la Base de Datos (Luego la bajaremos de Firebase)
        val productosPrueba = listOf(
            Producto("1", "Coca-Cola", 2.50),
            Producto("2", "Pizza Margarita", 14.50),
            Producto("3", "Tacos al Pastor (3 pzas)", 8.99),
            Producto("4", "Agua Mineral", 2.00)
        )

        // Por cada producto, inflamos su diseño y le damos vida a sus botones
        for (prod in productosPrueba) {
            val itemVista = layoutInflater.inflate(R.layout.item_producto, null)

            itemVista.findViewById<TextView>(R.id.tvNombreProd).text = prod.nombre
            itemVista.findViewById<TextView>(R.id.tvPrecioProd).text = "$${prod.precio}"

            val btnAgregar = itemVista.findViewById<Button>(R.id.btnAgregarInicial)
            val layoutCantidad = itemVista.findViewById<LinearLayout>(R.id.layoutCantidad)
            val btnMenos = itemVista.findViewById<Button>(R.id.btnMenos)
            val btnMas = itemVista.findViewById<Button>(R.id.btnMas)
            val tvCantidad = itemVista.findViewById<TextView>(R.id.tvCantidad)

            var cantidadSeleccionada = 0

            // Al darle "+ Agregar" por primera vez
            btnAgregar.setOnClickListener {
                cantidadSeleccionada = 1
                tvCantidad.text = cantidadSeleccionada.toString()
                btnAgregar.visibility = View.GONE
                layoutCantidad.visibility = View.VISIBLE
            }

            // Al sumarle (+)
            btnMas.setOnClickListener {
                cantidadSeleccionada++
                tvCantidad.text = cantidadSeleccionada.toString()
            }

            // Al restarle (-)
            btnMenos.setOnClickListener {
                cantidadSeleccionada--
                if (cantidadSeleccionada <= 0) {
                    // Si llega a 0, ocultamos el selector y volvemos a mostrar el botón original
                    btnAgregar.visibility = View.VISIBLE
                    layoutCantidad.visibility = View.GONE
                    cantidadSeleccionada = 0
                } else {
                    tvCantidad.text = cantidadSeleccionada.toString()
                }
            }

            // Agregamos la fila terminada a la lista
            contenedorPlatillos.addView(itemVista)
        }

        bottomSheetDialog.show()
    }

    // ==========================================
    // 2. LÓGICA DE PARTICIPANTES Y ANFITRIÓN
    // ==========================================
    private fun mostrarParticipantes() {
        db.collection("usuarios").whereEqualTo("id_mesa", idMesa).get()
            .addOnSuccessListener { documentos ->

                // Usamos this@MenuMesaActivity para arreglar el error del context
                val bottomSheetDialog = BottomSheetDialog(this@MenuMesaActivity)

                // Contenedor principal de la lista
                val layoutPrincipal = LinearLayout(this@MenuMesaActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(50, 50, 50, 50)
                }

                val titulo = TextView(this@MenuMesaActivity).apply {
                    text = "Conectados a la Mesa"
                    textSize = 20f
                    setTextColor(android.graphics.Color.BLACK)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, 30)
                }
                layoutPrincipal.addView(titulo)

                for (doc in documentos) {
                    val user = doc.toObject(Usuario::class.java)
                    var etiquetaNombre = user.name

                    if (user.id_user == idAnfitrionActual) etiquetaNombre += " 👑"
                    if (user.id_user == idUsuario) etiquetaNombre += " (Tú)"

                    // 1. Inflamos el diseño bonito que acabamos de crear
                    val itemVista = layoutInflater.inflate(R.layout.item_participante, null)

                    val tvNombre = itemVista.findViewById<TextView>(R.id.tvNombreParticipante)
                    val btnCeder = itemVista.findViewById<Button>(R.id.btnCeder)

                    tvNombre.text = etiquetaNombre

                    // 2. Lógica del botón Ceder
                    // Solo mostramos el botón si YO soy el anfitrión y el de la lista es alguien más
                    if (idUsuario == idAnfitrionActual && user.id_user != idUsuario) {
                        btnCeder.visibility = View.VISIBLE

                        btnCeder.setOnClickListener {
                            bottomSheetDialog.dismiss()
                            preguntarCederAnfitrion(user.id_user, user.name)
                        }
                    }

                    layoutPrincipal.addView(itemVista)
                }

                bottomSheetDialog.setContentView(layoutPrincipal)
                bottomSheetDialog.show()
            }
    }

    private fun preguntarCederAnfitrion(idNuevoAnfitrion: String, nombre: String) {
        AlertDialog.Builder(this)
            .setTitle("Ceder el control")
            .setMessage("¿Estás seguro de convertir a $nombre en el nuevo anfitrión?")
            .setPositiveButton("Sí, ceder") { _, _ ->
                db.collection("mesas").document(idMesa)
                    .update("id_user_creador", idNuevoAnfitrion)
                    .addOnSuccessListener {
                        Toast.makeText(this, "El anfitrión ha cambiado", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ==========================================
    // 3. CARGA DE DATOS BÁSICOS DE FIREBASE
    // ==========================================
    private fun cargarDatosMesa() {
        db.collection("mesas").document(idMesa).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val mesa = snapshot.toObject(Mesa::class.java)
                if (mesa != null) {
                    tvCodigoMesaCard.text = mesa.codigo
                    idAnfitrionActual = mesa.id_user_creador
                    tvNombreMesa.text = "Mesa ${mesa.codigo}"
                }
            }
        }
    }

    private fun cargarDatosUsuario() {
        db.collection("usuarios").document(idUsuario).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val usuario = doc.toObject(Usuario::class.java)
                tvSaludoUsuario.text = "Hola, ${usuario?.name}"
            }
        }
    }
}