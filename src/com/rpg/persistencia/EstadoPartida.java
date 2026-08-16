package com.rpg.persistencia;

import com.rpg.logica.enemigo.Enemigo;
import com.rpg.logica.personaje.Personaje;
import com.rpg.mision.Mision;

/**
 * DTO (Data Transfer Object) que encapsula todo el estado serializable
 * de una partida guardada. Usado por {@link ServicioGuardado} para
 * convertir el estado del juego a/desde JSON con Gson.
 */
public class EstadoPartida {

    private Personaje jugador;
    private Enemigo enemigo;
    private Mision misionActiva;
    private String fechaGuardado;

    public EstadoPartida() {
        // Constructor vacío requerido por Gson
    }

    public EstadoPartida(Personaje jugador, Enemigo enemigo, Mision misionActiva, String fechaGuardado) {
        this.jugador = jugador;
        this.enemigo = enemigo;
        this.misionActiva = misionActiva;
        this.fechaGuardado = fechaGuardado;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public Personaje getJugador() { return jugador; }
    public void setJugador(Personaje jugador) { this.jugador = jugador; }

    public Enemigo getEnemigo() { return enemigo; }
    public void setEnemigo(Enemigo enemigo) { this.enemigo = enemigo; }

    public Mision getMisionActiva() { return misionActiva; }
    public void setMisionActiva(Mision misionActiva) { this.misionActiva = misionActiva; }

    public String getFechaGuardado() { return fechaGuardado; }
    public void setFechaGuardado(String fechaGuardado) { this.fechaGuardado = fechaGuardado; }
}
