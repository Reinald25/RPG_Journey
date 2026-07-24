package com.rpg.logica.contrato;

import com.rpg.logica.personaje.Personaje;

public interface ActivadorHabilidad {
    /**
     * Ejecuta la habilidad especial contra el objetivo.
     * Retorna un mensaje describiendo lo ocurrido en combate.
     */
    String usarHabilidad(Personaje objetivo);

    /**
     * Retorna verdadero si la habilidad está lista para usarse
     * (sin cooldown y con recursos suficientes, ej: maná).
     */
    boolean puedeUsarHabilidad();

    /**
     * Retorna el nombre de la habilidad.
     */
    String getNombreHabilidad();

    /**
     * Retorna una breve descripción de los efectos de la habilidad.
     */
    String getDescripcionHabilidad();

    /**
     * Retorna los turnos de cooldown restantes (0 si está lista).
     */
    int getCooldownRestante();

    /**
     * Reduce en 1 el cooldown de la habilidad.
     */
    void reducirCooldown();

    /**
     * Reinicia el cooldown de la habilidad al valor máximo.
     */
    void resetCooldown();
}
