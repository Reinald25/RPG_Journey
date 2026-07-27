package com.rpg.mision;

import com.rpg.logica.personaje.Personaje;

/**
 * Clase abstracta que define el <b>Template Method</b> para todas las misiones del juego.
 *
 * <p>El flujo de ejecución está fijado en {@link #ejecutar(Personaje)} y no puede
 * ser alterado por las subclases. Solo los pasos específicos (hooks abstractos)
 * son delegados a cada implementación concreta.</p>
 *
 * <p>Flujo garantizado:
 * <pre>
 *   PENDIENTE
 *      │
 *      ▼
 *   iniciarMision()        ← obligatorio, define setup inicial
 *      │
 *      ▼
 *   desarrollarMision()    ← obligatorio, puede invocarse por pasos (combate a combate)
 *      │
 *      ▼
 *   finalizarMision()      ← obligatorio, aplica consecuencias
 *      │
 *      ▼
 *   onRecompensa()         ← hook opcional, solo si exito == true
 *      │
 *      ▼
 *   COMPLETADA / FALLIDA
 * </pre>
 * </p>
 *
 * <p><b>SOLID:</b> Principio Abierto/Cerrado — se agregan nuevos tipos de misión
 * creando subclases, sin modificar esta clase ni la GUI.</p>
 */
public abstract class Mision {

    // ─── Atributos de identidad ────────────────────────────────────────────────

    private final String nombre;
    private final String descripcion;
    private EstadoMision estado;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param nombre      Nombre corto de la misión que se mostrará en la UI.
     * @param descripcion Descripción larga con el objetivo de la misión.
     */
    protected Mision(String nombre, String descripcion) {
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.estado      = EstadoMision.PENDIENTE;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TEMPLATE METHOD (flujo fijo — final para que nadie lo rompa)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Ejecuta el flujo completo de la misión de forma secuencial y no modificable.
     * La GUI nunca debe llamar a los pasos individuales directamente.
     *
     * @param jugador El personaje del jugador que participa en la misión.
     */
    public final void ejecutar(Personaje jugador) {
        estado = EstadoMision.EN_PROGRESO;
        iniciarMision(jugador);
        boolean exito = desarrollarMision(jugador);
        finalizarMision(jugador, exito);
        if (exito) {
            onRecompensa(jugador);
        }
        estado = exito ? EstadoMision.COMPLETADA : EstadoMision.FALLIDA;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Hooks abstractos — las subclases DEBEN implementarlos
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Configura el estado inicial de la misión (ej: mostrar mensaje de inicio,
     * registrar rondas iniciales, etc.).
     *
     * @param jugador El personaje del jugador.
     */
    protected abstract void iniciarMision(Personaje jugador);

    /**
     * Desarrolla la lógica principal de la misión. Este método puede delegar
     * el avance real al controlador mediante {@link #notificarEnemigoDerrotado()}
     * y {@link #notificarRondaCompletada()}.
     *
     * @param jugador El personaje del jugador.
     * @return {@code true} si la misión fue completada con éxito, {@code false} si falló.
     */
    protected abstract boolean desarrollarMision(Personaje jugador);

    /**
     * Aplica las consecuencias finales de la misión (mensajes, registros, etc.).
     *
     * @param jugador El personaje del jugador.
     * @param exito   {@code true} si fue exitosa, {@code false} si falló.
     */
    protected abstract void finalizarMision(Personaje jugador, boolean exito);

    // ══════════════════════════════════════════════════════════════════════════
    //  Hook opcional — las subclases PUEDEN sobreescribirlo
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Aplica la recompensa al jugador cuando la misión se completa con éxito.
     * Las subclases sobreescriben este método para definir recompensas específicas.
     * Por defecto no hace nada.
     *
     * @param jugador El personaje del jugador que recibirá la recompensa.
     */
    protected void onRecompensa(Personaje jugador) {
        // Sin recompensa por defecto — las subclases lo sobreescriben si aplica
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos de avance — llamados por el ControladorCombate desde afuera
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Notifica a la misión que un enemigo normal fue derrotado.
     * Las subclases que rastrean kills sobreescriben este método.
     */
    public void notificarEnemigoDerrotado() {
        // Hook vacío por defecto
    }

    /**
     * Notifica a la misión que una ronda de supervivencia fue superada.
     * Las subclases de supervivencia sobreescriben este método.
     */
    public void notificarRondaCompletada() {
        // Hook vacío por defecto
    }

    /**
     * Notifica a la misión que un jefe fue derrotado.
     */
    public void notificarBossDerrotado() {
        // Hook vacío por defecto
    }

    /**
     * Notifica a la misión que el jugador murió, lo que la marca como fallida.
     */
    public void notificarJugadorDerrotado() {
        if (estado == EstadoMision.EN_PROGRESO) {
            estado = EstadoMision.FALLIDA;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Consultas de estado — usadas por el ControladorCombate y la Vista
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * @return {@code true} si la misión ya ha concluido (éxito o fallo).
     */
    public boolean estaFinalizada() {
        return estado == EstadoMision.COMPLETADA || estado == EstadoMision.FALLIDA;
    }

    /** @return El estado actual de la misión. */
    public EstadoMision getEstado() {
        return estado;
    }

    /** Permite al controlador cambiar el estado de forma controlada. */
    protected void setEstado(EstadoMision nuevoEstado) {
        this.estado = nuevoEstado;
    }

    /** @return Nombre corto de la misión. */
    public String getNombre() {
        return nombre;
    }

    /** @return Descripción completa del objetivo. */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @return Texto de progreso para mostrar en la UI (ej: "2/3 enemigos").
     *         Las subclases lo sobreescriben para dar información específica.
     */
    public String getProgresoTexto() {
        return estado.name();
    }

    /**
     * @return Progreso entre 0.0 y 1.0 para la barra de progreso de la UI.
     */
    public double getProgresoPorcentaje() {
        return 0.0;
    }
}
