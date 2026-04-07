package com.example.proyectorestaurante

// Importaciones necesarias para los "superpoderes" de Firebase
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// 1. Molde para los Usuarios (Admin, Meseros, etc.)
data class Usuario(
    @DocumentId var id_user: String = "",
    var name: String = "",
    var user: String = "",
    var tipo: Int = 0,
    var infoBank: String = "",
    var id_mesa: String = "" // <--- ¡NUEVO! Con esto sabremos en qué mesa están sentados
)

// 2. Molde para el Menú Predefinido (¡Mejorado para la vista visual!)
data class Producto(
    @DocumentId var id_producto: String = "",
    var nombre: String = "",
    var precio: Double = 0.0,
    var categoria: String = "", // NUEVO: Ej. "Bebidas", "Plato Fuerte", "Postres"
    var descripcion: String = "", // NUEVO: Breve explicación del platillo para antojar al cliente
    var imagenUrl: String = "" // NUEVO: Preparado para cuando subas fotos de la comida
)

// 3. Molde para las Mesas (Generadas por el código QR)
data class Mesa(
    @DocumentId var id_mesa: String = "",
    var codigo: String = "",
    var id_user_creador: String = "",
    var tiempo_estimado: Float = 0f,
    @ServerTimestamp var fecha_creacion: Date? = null, // MEJORADO: Firebase estampará la hora del servidor (evita hackeos de hora del celular)
    var estado_mesa: Int = 0 // 0=Libre, 1=Comiendo, 2=Lista para pagar
)

// 4. Molde para los Pedidos individuales dentro de la mesa
data class Consumo(
    @DocumentId var id_consumo: String = "",
    var id_user: String = "", // Cliente que pidió
    var id_producto: String = "",
    var nombre_producto: String = "",
    var precio_unitario: Double = 0.0,
    var cantidad: Int = 1, // NUEVO: Útil por si alguien pide "3" tacos de un solo clic
    var notas: String = "", // NUEVO: Crucial para la cocina (Ej. "Sin cebolla", "Salsa aparte")
    var conf_mesero: Boolean = false,
    var conf_user: Boolean = false,
    var estado: String = "Pendiente" // Pendiente, Preparando, Entregado
)