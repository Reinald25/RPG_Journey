package com.rpg.mision;

import com.rpg.logica.personaje.Personaje;

/**
 * Misión concreta: <b>Sobrevivir N rondas de combate consecutivas</b>.
 *
 * <p>Una "ronda" se cuenta cada vez que el jugador sobrevive a un contraataque
 * del enemigo, sin importar el resultado del combate (victoria o nuevo enemigo).
 * El {@code ControladorCombate} llama a {@link #notificarRondaCompletada()} después
 * de cada intercambio de ataques en que el jugador sigue con vida.</p>
 *
 * <p><b>Recompensa (hook onRecompensa):</b> Mejora permanente de stats aleatorios
 * (vida máxima, daño o defensa).</p>
 */
public class MisionSobrevivir extends Mision {

    private final int rondasMeta;
    private int rondasSuperadas;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param rondasMeta Número de rondas a sobrevivir sin morir.
     */
    public MisionSobrevivir(int rondasMeta) {
        super(
            "Irrompible",
            "Sobrevive " + rondasMeta + " rondas de combate sin caer. Prueba tu resistencia."
        );
        this.rondasMeta      = rondasMeta;
        this.rondasSuperadas = 0;
    }

    /** Constructor por defecto: meta de 5 rondas. */
    public MisionSobrevivir() {
        this(5);
    }

    // ─── Hooks del Template Method ─────────────────────────────────────────────

    @Override
    protected void iniciarMision(Personaje jugador) {
        rondasSuperadas = 0;
        setEstado(EstadoMision.EN_PROGRESO);
    }

    @Override
    protected boolean desarrollarMision(Personaje jugador) {
        return rondasSuperadas >= rondasMeta;
    }

    @Override
    protected void finalizarMision(Personaje jugador, boolean exito) {
        // Sin efectos de dominio adicionales.
    }

    /**
     * Hook de recompensa: mejora aleatoria de uno de los tres stats principales.
     */
    @Override
    protected void onRecompensa(Personaje jugador) {
        int stat = (int) (Math.random() * 3);
        switch (stat) {
            case 0 -> {
                jugador.subirVidaMaxEspecial(30);
            }
            case 1 -> {
                jugador.subirDañoEspecial(5);
            }
            default -> {
                jugador.subirDefensaEspecial(4);
            }
        }
    }

    // ─── Notificaciones desde el ControladorCombate ───────────────────────────

    /**
     * Incrementa el contador de rondas superadas. Llamado por el controlador
     * después de cada turno completo en que el jugador sobrevivió.
     */
    @Override
    public void notificarRondaCompletada() {
        if (getEstado() == EstadoMision.EN_PROGRESO) {
            rondasSuperadas++;
            if (rondasSuperadas >= rondasMeta) {
                setEstado(EstadoMision.COMPLETADA);
            }
        }
    }

    // ─── Consultas de progreso para la UI ────────────────────────────────────

    @Override
    public String getProgresoTexto() {
        return rondasSuperadas + " / " + rondasMeta + " rondas sobrevividas";
    }

    @Override
    public double getProgresoPorcentaje() {
        return (double) rondasSuperadas / rondasMeta;
    }

    public int getRondasSuperadas() { return rondasSuperadas; }
    public int getRondasMeta()      { return rondasMeta; }
}
