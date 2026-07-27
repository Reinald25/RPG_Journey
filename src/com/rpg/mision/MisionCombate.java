package com.rpg.mision;

import com.rpg.logica.personaje.Personaje;
import com.rpg.logica.item.Items;
import com.rpg.logica.item.ItemEquipable;

/**
 * Misión concreta: <b>Eliminar N enemigos normales</b>.
 *
 * <p>El Template Method {@code ejecutar()} inicia la misión con un mensaje,
 * y el "desarrollo" se resuelve de forma incremental: cada vez que el
 * {@code ControladorCombate} llama a {@link #notificarEnemigoDerrotado()},
 * el contador avanza. El método {@link #desarrollarMision(Personaje)} solo
 * verifica si ya se alcanzó el objetivo.</p>
 *
 * <p><b>Recompensa (hook onRecompensa):</b> XP bonus proporcional al objetivo
 * y un ítem de botín extra agregado al inventario del jugador.</p>
 */
public class MisionCombate extends Mision {

    private final int enemigosMeta;
    private int enemigosDerrotados;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param enemigosMeta Número de enemigos a eliminar para completar la misión.
     */
    public MisionCombate(int enemigosMeta) {
        super(
            "Guerrero Imparable",
            "Elimina " + enemigosMeta + " enemigos normales para demostrar tu dominio en combate."
        );
        this.enemigosMeta      = enemigosMeta;
        this.enemigosDerrotados = 0;
    }

    /** Constructor por defecto: meta de 3 enemigos. */
    public MisionCombate() {
        this(3);
    }

    // ─── Hooks del Template Method ─────────────────────────────────────────────

    @Override
    protected void iniciarMision(Personaje jugador) {
        // El mensaje de inicio se registra mediante el controlador; aquí solo
        // reseteamos el estado interno por si la misión se reutiliza.
        enemigosDerrotados = 0;
        setEstado(EstadoMision.EN_PROGRESO);
    }

    /**
     * Verifica si el jugador ya alcanzó la meta de kills.
     * El avance real ocurre en {@link #notificarEnemigoDerrotado()}.
     */
    @Override
    protected boolean desarrollarMision(Personaje jugador) {
        return enemigosDerrotados >= enemigosMeta;
    }

    @Override
    protected void finalizarMision(Personaje jugador, boolean exito) {
        // Los mensajes de resultado son responsabilidad de la Vista/Controlador.
        // Aquí no hay efectos de dominio adicionales.
    }

    /**
     * Hook de recompensa: otorga XP bonus y agrega un ítem al inventario.
     */
    @Override
    protected void onRecompensa(Personaje jugador) {
        int xpBonus = 50 * enemigosMeta;
        jugador.ganarExperiencia(xpBonus);

        // Drop de equipo o poción aleatoria como botín de misión
        Items recompensa;
        if (Math.random() < 0.6) {
            recompensa = ItemEquipable.generarEquipamientoAleatorio(jugador.getNivel(), jugador.getClase());
        } else {
            recompensa = Items.generarPocionAleatoria();
        }
        jugador.agregarItem(recompensa);
    }

    // ─── Notificaciones desde el ControladorCombate ───────────────────────────

    /**
     * Incrementa el contador de enemigos derrotados y marca la misión como
     * completada si se alcanza la meta. Llamado por {@code ControladorCombate}
     * cada vez que un enemigo normal es vencido.
     */
    @Override
    public void notificarEnemigoDerrotado() {
        if (getEstado() == EstadoMision.EN_PROGRESO) {
            enemigosDerrotados++;
            if (enemigosDerrotados >= enemigosMeta) {
                setEstado(EstadoMision.COMPLETADA);
            }
        }
    }

    // ─── Consultas de progreso para la UI ────────────────────────────────────

    @Override
    public String getProgresoTexto() {
        return enemigosDerrotados + " / " + enemigosMeta + " enemigos derrotados";
    }

    @Override
    public double getProgresoPorcentaje() {
        return (double) enemigosDerrotados / enemigosMeta;
    }

    public int getEnemigosDerrotados() { return enemigosDerrotados; }
    public int getEnemigosMeta()       { return enemigosMeta; }
}
