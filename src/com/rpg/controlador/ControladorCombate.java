package com.rpg.controlador;

import com.rpg.logica.contrato.ActivadorHabilidad;
import com.rpg.logica.enemigo.Enemigo;
import com.rpg.logica.enemigo.JefeDragon;
import com.rpg.logica.item.ItemEquipable;
import com.rpg.logica.item.Items;
import com.rpg.logica.item.TipoSlot;
import com.rpg.logica.personaje.Arquero;
import com.rpg.logica.personaje.Mago;
import com.rpg.logica.personaje.Personaje;
import com.rpg.mision.EstadoMision;
import com.rpg.mision.Mision;
import com.rpg.mision.MisionCombate;
import com.rpg.mision.MisionEliminarBoss;
import com.rpg.mision.MisionSobrevivir;

/**
 * <b>Controlador de Combate</b> — capa de control del patrón MVC.
 *
 * <p>Centraliza toda la lógica de negocio que antes estaba dispersa en los
 * {@code ActionListener} de {@code VentanaJuego}:
 * <ul>
 *   <li>Gestión del turno de ataque del jugador y contraataque del enemigo.</li>
 *   <li>Procesamiento de victoria: XP, drops, notificación a la misión activa.</li>
 *   <li>Uso de habilidades especiales.</li>
 *   <li>Uso y desequipado de ítems.</li>
 *   <li>Generación de nuevos enemigos (normal o Boss al 20%).</li>
 *   <li>Gestión del ciclo de vida de la misión activa.</li>
 * </ul>
 * </p>
 *
 * <p><b>SOLID:</b>
 * <ul>
 *   <li><b>S (SRP):</b> Solo orquesta el combate; la UI es responsabilidad de la Vista.</li>
 *   <li><b>D (DIP):</b> Depende de {@link IVistaJuego} (abstracción), no de {@code VentanaJuego} (detalle).</li>
 *   <li><b>O (OCP):</b> Para agregar un tipo de misión basta con crear una subclase de {@link Mision}.</li>
 * </ul>
 * </p>
 *
 * <p>Este controlador es POJO puro — no importa nada de Swing, lo que facilita
 * pruebas unitarias sin entorno gráfico.</p>
 */
public class ControladorCombate {

    // ─── Dependencias ──────────────────────────────────────────────────────────

    /** Referencia a la Vista, inyectada por DIP. */
    private final IVistaJuego vista;

    // ─── Estado del modelo ─────────────────────────────────────────────────────

    private Personaje jugador;
    private Enemigo enemigoActual;
    private Mision misionActiva;

    /** Probabilidad de que aparezca un Boss en lugar de un enemigo normal. */
    private static final double PROB_BOSS = 0.20;

    // ─── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param jugador       El personaje del jugador (ya creado por la Vista).
     * @param enemigoInicial El primer enemigo del juego.
     * @param vista         La Vista que implementa {@link IVistaJuego}.
     */
    public ControladorCombate(Personaje jugador, Enemigo enemigoInicial, IVistaJuego vista) {
        this.jugador       = jugador;
        this.enemigoActual = enemigoInicial;
        this.vista         = vista;
        // Arranca con una misión de combate básica por defecto
        iniciarNuevaMision();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Acciones de combate — llamadas por los ActionListener de la Vista
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Ejecuta el turno completo: ataque del jugador → (si el enemigo sobrevive)
     * contraataque del enemigo. Luego avanza el cooldown del jugador.
     */
    public void atacar() {
        if (!combateActivo()) return;

        // ── Turno del Jugador ──
        String msgAtaque = jugador.atacar(enemigoActual);
        vista.mostrarMensaje("COMBATE", msgAtaque);

        if (enemigoActual.getPuntosVida() <= 0) {
            procesarVictoria(esBoss());
            return;
        }

        // ── Contraataque del Enemigo ──
        ejecutarContraataqueEnemigo();

        // ── Avanzar cooldowns ──
        jugador.avanzarTurno();
        
        // ── Notificar ronda a misión ──
        if (misionActiva != null && jugador.getPuntosVida() > 0) {
            misionActiva.notificarRondaCompletada();
            verificarMisionCompletada();
        }

        vista.actualizarUI();
    }

    /**
     * Ejecuta la habilidad especial del jugador (si implementa {@link ActivadorHabilidad})
     * y procesa el contraataque del enemigo si éste sobrevive.
     */
    public void usarHabilidad() {
        if (!combateActivo()) return;
        if (!(jugador instanceof ActivadorHabilidad)) return;

        ActivadorHabilidad act = (ActivadorHabilidad) jugador;
        if (!act.puedeUsarHabilidad()) return;

        // ── Habilidad ──
        String msgHabilidad = act.usarHabilidad(enemigoActual);
        vista.mostrarMensaje("HABILIDAD", msgHabilidad);

        if (enemigoActual.getPuntosVida() <= 0) {
            procesarVictoria(esBoss());
            return;
        }

        // ── Contraataque ──
        ejecutarContraataqueEnemigo();

        // ── Avanzar cooldowns ──
        jugador.avanzarTurno();

        // ── Notificar ronda a misión ──
        if (misionActiva != null && jugador.getPuntosVida() > 0) {
            misionActiva.notificarRondaCompletada();
            verificarMisionCompletada();
        }

        vista.actualizarUI();
    }

    /**
     * Usa el ítem en la posición {@code index} del inventario del jugador y,
     * si hay enemigo activo, ejecuta el contraataque como penalización.
     *
     * @param index Índice del ítem seleccionado en el combo de la UI.
     */
    public void usarItem(int index) {
        if (jugador.getPuntosVida() <= 0) return;
        if (index < 0 || jugador.getInventario().isEmpty()) {
            vista.mostrarMensaje("SISTEMA", "No tienes ítems para usar.");
            return;
        }

        Items item = jugador.getInventario().get(index);

        if (item instanceof ItemEquipable equipable) {
            if (equipable.getClaseRequerida() != null
                    && !equipable.getClaseRequerida().equalsIgnoreCase(jugador.getClase())) {
                vista.mostrarMensaje("SISTEMA",
                    "❌ No puedes equipar " + item.getNombre()
                    + ". Clase requerida: " + equipable.getClaseRequerida());
                return;
            }
            boolean exito = jugador.usarItem(index);
            if (exito) {
                vista.mostrarMensaje("EQUIPO", "🛡️ " + jugador.getNombre() + " se ha equipado: " + item.getNombre());
                ejecutarContraataqueEnemigo();
            } else {
                vista.mostrarMensaje("SISTEMA", "Error al equipar el ítem.");
            }
        } else {
            double vidaPrevia = jugador.getPuntosVida();
            boolean exito = jugador.usarItem(index);
            if (exito) {
                double curacion = jugador.getPuntosVida() - vidaPrevia;
                vista.mostrarMensaje("ACCION",
                    "🧪 " + jugador.getNombre() + " bebe una " + item.getNombre()
                    + " y se cura " + String.format("%.1f", curacion) + " HP.");
                ejecutarContraataqueEnemigo();
            } else {
                vista.mostrarMensaje("SISTEMA", "Error al usar el ítem.");
            }
        }

        vista.actualizarUI();
    }

    /**
     * Desequipa el ítem del slot indicado y lo devuelve al inventario.
     *
     * @param slot El slot a vaciar (ARMA, ARMADURA o ACCESORIO).
     */
    public void desequipar(TipoSlot slot) {
        if (jugador.getPuntosVida() <= 0) return;

        ItemEquipable eq = null;
        if (slot == TipoSlot.ARMA)      eq = jugador.getEquipamiento().getArma();
        else if (slot == TipoSlot.ARMADURA)  eq = jugador.getEquipamiento().getArmadura();
        else if (slot == TipoSlot.ACCESORIO) eq = jugador.getEquipamiento().getAccesorio();

        if (eq != null && jugador.desequiparItem(slot)) {
            vista.mostrarMensaje("EQUIPO", "📤 Te has desequipado: " + eq.getNombre());
            vista.actualizarUI();
        }
    }

    /**
     * Genera un nuevo enemigo. Con un 20% de probabilidad invoca al
     * {@code JefeDragon} y activa {@link MisionEliminarBoss}; el resto
     * del tiempo invoca un {@link Enemigo} normal.
     */
    public void generarNuevoEnemigo() {
        if (Math.random() < PROB_BOSS) {
            enemigoActual = new JefeDragon(jugador.getNivel());
            // Reemplaza la misión actual por una de Boss
            misionActiva = new MisionEliminarBoss();
            misionActiva.ejecutar(jugador); // Inicia el flujo Template Method (pendiente → en progreso)
            vista.mostrarMensaje("JEFE",
                "☠️ ¡Ha aparecido el " + enemigoActual.getNombre()
                + " (Nivel " + enemigoActual.getNivel() + ")! ¡NUEVA MISIÓN: Cazador de Dragones!");
        } else {
            enemigoActual = new Enemigo(jugador.getNivel());
            vista.mostrarMensaje("SISTEMA",
                "¡Ha aparecido un " + enemigoActual.getNombre()
                + " (Nivel " + enemigoActual.getNivel() + ") listo para luchar!");
        }
        vista.actualizarUI();
    }

    /**
     * Reemplaza el jugador actual (cuando se crea un nuevo personaje).
     * También reinicia la misión activa.
     *
     * @param nuevoJugador El nuevo personaje seleccionado.
     */
    public void reiniciar(Personaje nuevoJugador) {
        this.jugador       = nuevoJugador;
        this.enemigoActual = new Enemigo(nuevoJugador.getNivel());
        iniciarNuevaMision();
        vista.limpiarLog();
        vista.mostrarMensaje("SISTEMA", "--- NUEVO PERSONAJE SELECCIONADO ---");
        vista.mostrarMensaje("SISTEMA",
            "Tu héroe ha comenzado (" + jugador.getNombre() + " - " + jugador.getClase()
            + "). Aparece un " + enemigoActual.getNombre()
            + " (Nivel " + enemigoActual.getNivel() + ").");
        vista.actualizarUI();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Lógica interna — private
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Procesa la victoria del jugador sobre el enemigo actual:
     * otorga XP, genera drop, notifica a la misión activa.
     *
     * @param eraUnBoss {@code true} si el enemigo vencido era un JefeDragon.
     */
    private void procesarVictoria(boolean eraUnBoss) {
        vista.mostrarMensaje("VICTORIA", "¡Has derrotado a " + enemigoActual.getNombre() + "!");

        // ── EXP ──
        int expGanada = 40 + (enemigoActual.getNivel() * 15);
        vista.mostrarMensaje("EXPERIENCIA", "✨ Has ganado " + expGanada + " EXP.");
        boolean subioNivel = jugador.ganarExperiencia(expGanada);
        if (subioNivel) {
            vista.mostrarMensaje("SISTEMA",
                "⭐ ¡HAS SUBIDO DE NIVEL! Ahora eres Nivel " + jugador.getNivel()
                + ". Tu HP se ha restaurado al máximo.");
        }

        // ── Drop ──
        Items drop;
        if (Math.random() < 0.50) {
            drop = Items.generarPocionAleatoria();
        } else {
            drop = ItemEquipable.generarEquipamientoAleatorio(jugador.getNivel(), jugador.getClase());
        }
        jugador.agregarItem(drop);
        vista.mostrarMensaje("BOTÍN",
            enemigoActual.getNombre() + " dejó caer: " + drop.getNombre() + ". ¡Guardada en tu mochila!");

        // ── Notificar a la misión activa ──
        if (misionActiva != null && misionActiva.getEstado() == EstadoMision.EN_PROGRESO) {
            if (eraUnBoss) {
                misionActiva.notificarBossDerrotado();
            } else {
                misionActiva.notificarEnemigoDerrotado();
            }
            verificarMisionCompletada();
        }

        vista.mostrarMensaje("SISTEMA", "Haz clic en 'Nuevo Enemigo' para invocar otro oponente.");
        vista.actualizarUI();
    }

    /**
     * Ejecuta el contraataque del enemigo. Maneja la mecánica de esquiva del Arquero.
     */
    private void ejecutarContraataqueEnemigo() {
        if (enemigoActual == null || enemigoActual.getPuntosVida() <= 0) return;

        String msgEnemigo = enemigoActual.atacar(jugador);

        // Mecánica de esquiva del Arquero
        if (jugador instanceof Arquero arquero && arquero.isUltimoAtaqueEsquivado()) {
            vista.mostrarMensaje("COMBATE",
                "🍃 ¡" + jugador.getNombre() + " esquivó ágilmente el ataque de "
                + enemigoActual.getNombre() + "!");
            arquero.resetEsquiva();
        } else {
            vista.mostrarMensaje("COMBATE", msgEnemigo);
        }

        if (jugador.getPuntosVida() <= 0) {
            vista.mostrarMensaje("DERROTA",
                "Has sido derrotado por " + enemigoActual.getNombre() + "... Tu aventura ha terminado.");
            vista.mostrarMensaje("SISTEMA", "Pulsa 'Nuevo Personaje' para volver a intentarlo.");
            if (misionActiva != null) {
                misionActiva.notificarJugadorDerrotado();
            }
            vista.actualizarUI();
        }
    }

    /**
     * Verifica si la misión activa fue completada y, de ser así, aplica la
     * recompensa (vía el hook {@code onRecompensa()} del Template Method) y
     * arranca una nueva misión.
     */
    private void verificarMisionCompletada() {
        if (misionActiva == null) return;
        if (misionActiva.getEstado() == EstadoMision.COMPLETADA) {
            vista.mostrarMensaje("MISIÓN",
                "🏆 ¡MISIÓN COMPLETADA: " + misionActiva.getNombre() + "! Recompensa aplicada.");
            // El hook onRecompensa se ejecutó en ejecutar() — aquí solo lanzamos la siguiente
            iniciarNuevaMision();
            vista.mostrarMensaje("MISIÓN",
                "📜 Nueva misión activa: " + misionActiva.getNombre());
        }
    }

    /**
     * Selecciona aleatoriamente el próximo tipo de misión y la inicia.
     */
    private void iniciarNuevaMision() {
        int tipo = (int) (Math.random() * 3);
        misionActiva = switch (tipo) {
            case 0  -> new MisionCombate(3);
            case 1  -> new MisionSobrevivir(5);
            default -> new MisionCombate(5); // sin Boss aquí; los Boss se activan al generarEnemigo
        };
        // Ejecuta el Template Method para poner la misión EN_PROGRESO
        misionActiva.ejecutar(jugador);
    }

    /**
     * @return {@code true} si hay un combate activo (jugador y enemigo vivos).
     */
    private boolean combateActivo() {
        return enemigoActual != null
            && enemigoActual.getPuntosVida() > 0
            && jugador.getPuntosVida() > 0;
    }

    /**
     * @return {@code true} si el enemigo actual es un JefeDragon.
     */
    private boolean esBoss() {
        return enemigoActual instanceof JefeDragon;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Getters para la Vista
    // ══════════════════════════════════════════════════════════════════════════

    /** @return El personaje del jugador activo. */
    public Personaje getJugador() { return jugador; }

    /** @return El enemigo en combate actual. */
    public Enemigo getEnemigoActual() { return enemigoActual; }

    /** @return La misión activa, o {@code null} si no hay ninguna. */
    public Mision getMisionActiva() { return misionActiva; }
}
