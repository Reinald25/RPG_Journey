package com.rpg.mision;

import com.rpg.logica.personaje.Personaje;
import com.rpg.logica.item.ItemEquipable;

/**
 * Misión concreta: <b>Derrotar al Gran Dragón de Lava (JefeDragon)</b>.
 *
 * <p>Esta misión se considera la más difícil y otorga la recompensa más grande.
 * Se activa cuando el {@code ControladorCombate} elige generar un Boss en lugar
 * de un enemigo normal.</p>
 *
 * <p><b>Recompensa (hook onRecompensa):</b> XP masivo + equipamiento garantizado
 * de calidad máxima.</p>
 */
public class MisionEliminarBoss extends Mision {

    private boolean bossDerrotado;

    // ─── Constructor ───────────────────────────────────────────────────────────

    public MisionEliminarBoss() {
        super(
            "Cazador de Dragones",
            "Derrota al Gran Dragón de Lava antes de que acabe contigo. Recompensa épica garantizada."
        );
        this.bossDerrotado = false;
    }

    // ─── Hooks del Template Method ─────────────────────────────────────────────

    @Override
    protected void iniciarMision(Personaje jugador) {
        bossDerrotado = false;
        setEstado(EstadoMision.EN_PROGRESO);
    }

    @Override
    protected boolean desarrollarMision(Personaje jugador) {
        return bossDerrotado;
    }

    @Override
    protected void finalizarMision(Personaje jugador, boolean exito) {
        // Sin efectos de dominio adicionales; la Vista/Controlador muestra mensajes.
    }

    /**
     * Hook de recompensa: XP masivo (300) y equipamiento garantizado de nivel superior.
     */
    @Override
    protected void onRecompensa(Personaje jugador) {
        jugador.ganarExperiencia(300);
        // Equipamiento épico garantizado: generado 2 niveles por encima del actual
        ItemEquipable equipoEpico = ItemEquipable.generarEquipamientoAleatorio(
            jugador.getNivel() + 2, jugador.getClase()
        );
        jugador.agregarItem(equipoEpico);
    }

    // ─── Notificaciones desde el ControladorCombate ───────────────────────────

    /**
     * Marca el boss como derrotado y completa la misión.
     * Llamado por {@code ControladorCombate} cuando el HP del JefeDragon llega a 0.
     */
    @Override
    public void notificarBossDerrotado() {
        if (getEstado() == EstadoMision.EN_PROGRESO) {
            bossDerrotado = true;
            setEstado(EstadoMision.COMPLETADA);
        }
    }

    // ─── Consultas de progreso para la UI ────────────────────────────────────

    @Override
    public String getProgresoTexto() {
        return bossDerrotado ? "¡Dragón derrotado! ✓" : "El Dragón sigue en pie...";
    }

    @Override
    public double getProgresoPorcentaje() {
        return bossDerrotado ? 1.0 : 0.0;
    }
}
