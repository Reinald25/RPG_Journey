package com.rpg.persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpg.logica.enemigo.Enemigo;
import com.rpg.logica.item.Items;
import com.rpg.logica.personaje.Personaje;
import com.rpg.mision.Mision;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio centralizado de persistencia que maneja el guardado y carga
 * de partidas del juego RPG Journey usando la librería Gson.
 *
 * <p>Configura el {@link Gson} con los adaptadores custom necesarios para
 * serializar correctamente las jerarquías polimórficas del proyecto
 * (Personaje, Items, Mision).</p>
 *
 * <p><b>SRP:</b> Esta clase tiene una única responsabilidad: leer y escribir
 * el estado de la partida en formato JSON.</p>
 */
public class ServicioGuardado {

    private final Gson gson;

    public ServicioGuardado() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Personaje.class, new AdaptadorPersonaje())
                .registerTypeHierarchyAdapter(Items.class, new AdaptadorItems())
                .registerTypeHierarchyAdapter(Mision.class, new AdaptadorMision())
                .create();
    }

    /**
     * Guarda el estado actual de la partida en un archivo JSON.
     *
     * @param jugador  El personaje del jugador.
     * @param enemigo  El enemigo actual en combate.
     * @param mision   La misión activa (puede ser null).
     * @param rutaArchivo Ruta absoluta del archivo donde guardar.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public void guardarPartida(Personaje jugador, Enemigo enemigo, Mision mision, String rutaArchivo)
            throws IOException {

        String fechaGuardado = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        EstadoPartida estado = new EstadoPartida(jugador, enemigo, mision, fechaGuardado);

        String json = gson.toJson(estado);

        // Asegurar que el directorio padre exista
        File archivo = new File(rutaArchivo);
        File directorioPadre = archivo.getParentFile();
        if (directorioPadre != null && !directorioPadre.exists()) {
            directorioPadre.mkdirs();
        }

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            writer.write(json);
        }
    }

    /**
     * Carga una partida desde un archivo JSON.
     *
     * @param rutaArchivo Ruta absoluta del archivo JSON a leer.
     * @return El {@link EstadoPartida} reconstruido con todos los objetos.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public EstadoPartida cargarPartida(String rutaArchivo) throws IOException {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            throw new FileNotFoundException("No se encontró el archivo de guardado: " + rutaArchivo);
        }

        try (Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, EstadoPartida.class);
        }
    }
}
