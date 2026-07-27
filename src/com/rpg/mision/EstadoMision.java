package com.rpg.mision;

/**
 * Enum que representa los posibles estados del ciclo de vida de una Mision.
 *
 * <p>Flujo esperado:
 * <pre>
 *   PENDIENTE → EN_PROGRESO → COMPLETADA
 *                           ↘ FALLIDA
 * </pre>
 * </p>
 */
public enum EstadoMision {

    /** La misión ha sido creada pero aún no ha comenzado. */
    PENDIENTE,

    /** La misión está activa y el jugador está trabajando en ella. */
    EN_PROGRESO,

    /** El jugador cumplió todos los objetivos de la misión. */
    COMPLETADA,

    /** El jugador falló la misión (ej: murió antes de completarla). */
    FALLIDA
}
