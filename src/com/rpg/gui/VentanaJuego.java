package com.rpg.gui;

import com.rpg.controlador.ControladorCombate;
import com.rpg.controlador.IVistaJuego;
import com.rpg.logica.contrato.ActivadorHabilidad;
import com.rpg.logica.enemigo.Enemigo;
import com.rpg.logica.item.ItemEquipable;
import com.rpg.logica.item.Items;
import com.rpg.logica.item.TipoSlot;
import com.rpg.logica.personaje.*;
import com.rpg.mision.EstadoMision;
import com.rpg.mision.Mision;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <b>Vista principal del juego</b> — capa de presentación del patrón MVC.
 *
 * <p>Responsabilidades de esta clase (<b>SRP</b>):
 * <ul>
 *   <li>Construir y mostrar todos los componentes Swing.</li>
 *   <li>Implementar {@link IVistaJuego} para recibir notificaciones del controlador.</li>
 *   <li>Delegar <em>todas</em> las acciones de negocio al {@link ControladorCombate}.</li>
 * </ul>
 * </p>
 *
 * <p>Esta clase <b>no contiene lógica de combate</b>. Los {@code ActionListener}
 * son de 1-3 líneas y solo invocan métodos del controlador.</p>
 */
public class VentanaJuego extends JFrame implements IVistaJuego {

    // ─── Controlador (MVC) ────────────────────────────────────────────────────
    private ControladorCombate controlador;

    // ─── Paleta de colores Premium ────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(15, 23, 42);
    private static final Color CARD_BG       = new Color(30, 41, 59);
    private static final Color TEXT_LIGHT    = new Color(248, 250, 252);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color BORDER_COLOR  = new Color(51, 65, 85);
    private static final Color ACCENT_GREEN  = new Color(16, 185, 129);
    private static final Color ACCENT_RED    = new Color(239, 68, 68);
    private static final Color ACCENT_BLUE   = new Color(59, 130, 246);
    private static final Color ACCENT_YELLOW = new Color(245, 158, 11);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color TERMINAL_BG   = new Color(2, 6, 23);
    private static final Color TERMINAL_TEXT = new Color(34, 197, 94);

    // ─── Componentes UI — Jugador ─────────────────────────────────────────────
    private JLabel lblJugadorNombre;
    private JLabel lblJugadorNivel;
    private JProgressBar barJugadorVida;
    private JLabel lblJugadorVidaTexto;
    private JProgressBar barJugadorExp;
    private JLabel lblJugadorExpTexto;
    private JLabel lblJugadorAtk;
    private JLabel lblJugadorDef;
    private JLabel lblJugadorMana;

    // ─── Componentes UI — Equipamiento ───────────────────────────────────────
    private JLabel lblEquipArma;
    private JLabel lblEquipArmadura;
    private JLabel lblEquipAccesorio;
    private JButton btnDesequiparArma;
    private JButton btnDesequiparArmadura;
    private JButton btnDesequiparAccesorio;

    // ─── Componentes UI — Enemigo ─────────────────────────────────────────────
    private JLabel lblEnemigoNombre;
    private JLabel lblEnemigoNivel;
    private JProgressBar barEnemigoVida;
    private JLabel lblEnemigoVidaTexto;
    private JLabel lblEnemigoAtk;
    private JLabel lblEnemigoDef;

    // ─── Componentes UI — Misión ─────────────────────────────────────────────
    private JLabel lblMisionNombre;
    private JLabel lblMisionDescripcion;
    private JProgressBar barMisionProgreso;
    private JLabel lblMisionProgreso;
    private JLabel lblMisionEstado;

    // ─── Terminal Log ─────────────────────────────────────────────────────────
    private JTextArea txtLog;

    // ─── Inventario ──────────────────────────────────────────────────────────
    private JComboBox<String> comboInventario;
    private JButton btnUsarItem;

    // ─── Botones de Acción ────────────────────────────────────────────────────────────
    private JButton btnAtacar;
    private JButton btnHabilidad;
    private JButton btnGenerarEnemigo;
    private JButton btnNuevoPersonaje;
    private JButton btnGuardar;
    private JButton btnCargar;

    // ══════════════════════════════════════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════════════════════════════════════

    public VentanaJuego() {
        setTitle("RPG Journey - Edición Premium");
        setSize(960, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // 1. Crear el personaje (responsabilidad de la Vista: selección inicial)
        Personaje jugadorInicial = seleccionarPersonaje();
        Enemigo enemigoInicial = new Enemigo(jugadorInicial.getNivel());

        // 2. Inyectar el controlador con la vista (DIP)
        controlador = new ControladorCombate(jugadorInicial, enemigoInicial, this);

        // 3. Construir la UI
        construirUI();
        registrarEventos();

        // 4. Mensaje de bienvenida
        mostrarMensaje("SISTEMA", "¡Bienvenido a RPG Journey! Explora, lucha y sube de nivel.");
        mostrarMensaje("SISTEMA",
            "Tu primer oponente: " + controlador.getEnemigoActual().getNombre()
            + " (Nivel " + controlador.getEnemigoActual().getNivel() + ").");
        if (controlador.getMisionActiva() != null) {
            mostrarMensaje("MISIÓN",
                "📜 Misión activa: " + controlador.getMisionActiva().getNombre()
                + " — " + controlador.getMisionActiva().getDescripcion());
        }

        actualizarUI();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Implementación de IVistaJuego (callbacks del Controlador)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Actualiza todos los componentes de la UI con el estado actual del modelo.
     * Llamado por el controlador después de cada acción.
     */
    @Override
    public void actualizarUI() {
        Personaje jugador = controlador.getJugador();
        Enemigo enemigo   = controlador.getEnemigoActual();

        // ── Jugador ──
        lblJugadorNombre.setText(jugador.getNombre() + " (" + jugador.getClase() + ")");
        lblJugadorNivel.setText("Nivel: " + jugador.getNivel());
        barJugadorVida.setMaximum((int) jugador.getPuntosVidaMax());
        barJugadorVida.setValue((int) jugador.getPuntosVida());
        lblJugadorVidaTexto.setText(
            String.format("%.1f / %.1f HP", jugador.getPuntosVida(), jugador.getPuntosVidaMax()));

        barJugadorExp.setMaximum(jugador.getExpRequerida());
        barJugadorExp.setValue(jugador.getExp());
        lblJugadorExpTexto.setText(
            String.format("%d / %d EXP", jugador.getExp(), jugador.getExpRequerida()));

        lblJugadorAtk.setText(String.format("⚔️ Daño Total: %.1f", jugador.getDañoBase()));
        lblJugadorDef.setText(String.format("🛡️ Defensa Total: %.1f", jugador.getDefensa()));

        if (jugador instanceof Mago mago) {
            lblJugadorMana.setText(
                String.format("✨ Maná: %.0f / %.0f MP", mago.getMana(), mago.getManaMax()));
            lblJugadorMana.setVisible(true);
        } else {
            lblJugadorMana.setVisible(false);
        }

        // ── Equipamiento ──
        if (jugador.getEquipamiento() != null) {
            actualizarSlotEquip(lblEquipArma, btnDesequiparArma, "⚔️",
                jugador.getEquipamiento().getArma(), "Arma");
            actualizarSlotEquip(lblEquipArmadura, btnDesequiparArmadura, "🛡️",
                jugador.getEquipamiento().getArmadura(), "Armadura");
            actualizarSlotEquip(lblEquipAccesorio, btnDesequiparAccesorio, "💍",
                jugador.getEquipamiento().getAccesorio(), "Accesorio");
        }

        // ── Enemigo ──
        if (enemigo != null) {
            lblEnemigoNombre.setText(enemigo.getNombre());
            lblEnemigoNivel.setText("Nivel: " + enemigo.getNivel());
            barEnemigoVida.setMaximum((int) enemigo.getPuntosVidaMax());
            barEnemigoVida.setValue((int) enemigo.getPuntosVida());
            lblEnemigoVidaTexto.setText(
                String.format("%.1f / %.1f HP", enemigo.getPuntosVida(), enemigo.getPuntosVidaMax()));
            lblEnemigoAtk.setText(String.format("⚔️ Daño Base: %.1f", enemigo.getDañoBase()));
            lblEnemigoDef.setText(String.format("🛡️ Defensa: %.1f", enemigo.getDefensa()));
            setVisibleEnemigoStats(true);
        } else {
            lblEnemigoNombre.setText("Sin Oponente");
            lblEnemigoNivel.setText("Nivel: -");
            setVisibleEnemigoStats(false);
        }

        // ── Inventario ──
        actualizarComboInventario();

        // ── Misión ──
        actualizarPanelMision();

        // ── Estado de botones ──
        actualizarEstadoBotones();
    }

    @Override
    public void mostrarMensaje(String tag, String mensaje) {
        txtLog.append("[" + tag + "] " + mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    @Override
    public void limpiarLog() {
        txtLog.setText("");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Construcción de la UI
    // ══════════════════════════════════════════════════════════════════════════

    private void construirUI() {
        // ── Header ──
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(BG_DARK);
        panelHeader.setBorder(new EmptyBorder(15, 20, 0, 20));
        JLabel lblTitulo = new JLabel("⚔️ RPG JOURNEY ⚔️");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(ACCENT_BLUE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelHeader.add(lblTitulo, BorderLayout.CENTER);
        add(panelHeader, BorderLayout.NORTH);

        // ── Panel central: Personajes + Misión ──
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBackground(BG_DARK);
        panelCentro.setBorder(new EmptyBorder(10, 20, 0, 20));

        JPanel panelPersonajes = new JPanel(new GridLayout(1, 2, 20, 0));
        panelPersonajes.setBackground(BG_DARK);
        panelPersonajes.add(crearCardJugador());
        panelPersonajes.add(crearCardEnemigo());
        panelCentro.add(panelPersonajes, BorderLayout.CENTER);
        panelCentro.add(crearPanelMision(), BorderLayout.SOUTH);

        // ── Panel inferior: Log + Botones ──
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBackground(BG_DARK);
        panelInferior.setBorder(new EmptyBorder(0, 20, 20, 20));

        txtLog = new JTextArea();
        txtLog.setBackground(TERMINAL_BG);
        txtLog.setForeground(TERMINAL_TEXT);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtLog.setEditable(false);
        txtLog.setMargin(new Insets(10, 10, 10, 10));
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(new LineBorder(BORDER_COLOR, 1));
        scrollLog.setPreferredSize(new Dimension(0, 160));
        panelInferior.add(scrollLog, BorderLayout.CENTER);
        panelInferior.add(crearPanelBotones(), BorderLayout.SOUTH);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BG_DARK);
        mainContent.add(panelCentro, BorderLayout.CENTER);
        mainContent.add(panelInferior, BorderLayout.SOUTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel crearPanelMision() {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBackground(new Color(20, 30, 50));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_PURPLE, 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // Header de misión
        JPanel headerMision = new JPanel(new BorderLayout());
        headerMision.setBackground(new Color(20, 30, 50));

        JLabel lblMisionTitulo = new JLabel("📜 MISIÓN ACTIVA");
        lblMisionTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMisionTitulo.setForeground(ACCENT_PURPLE);

        lblMisionEstado = new JLabel("PENDIENTE");
        lblMisionEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMisionEstado.setForeground(ACCENT_YELLOW);
        lblMisionEstado.setHorizontalAlignment(SwingConstants.RIGHT);

        headerMision.add(lblMisionTitulo, BorderLayout.WEST);
        headerMision.add(lblMisionEstado, BorderLayout.EAST);
        panel.add(headerMision, BorderLayout.NORTH);

        // Cuerpo de misión
        JPanel cuerpoMision = new JPanel(new GridBagLayout());
        cuerpoMision.setBackground(new Color(20, 30, 50));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets = new Insets(2, 0, 2, 0);

        g.gridy = 0;
        lblMisionNombre = new JLabel("Cargando misión...");
        lblMisionNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMisionNombre.setForeground(TEXT_LIGHT);
        cuerpoMision.add(lblMisionNombre, g);

        g.gridy = 1;
        lblMisionDescripcion = new JLabel(" ");
        lblMisionDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMisionDescripcion.setForeground(TEXT_MUTED);
        cuerpoMision.add(lblMisionDescripcion, g);

        g.gridy = 2;
        barMisionProgreso = new JProgressBar(0, 100);
        barMisionProgreso.setValue(0);
        barMisionProgreso.setForeground(ACCENT_PURPLE);
        barMisionProgreso.setBackground(TERMINAL_BG);
        barMisionProgreso.setBorderPainted(false);
        barMisionProgreso.setPreferredSize(new Dimension(0, 14));
        cuerpoMision.add(barMisionProgreso, g);

        g.gridy = 3;
        lblMisionProgreso = new JLabel("0 / 0");
        lblMisionProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMisionProgreso.setForeground(ACCENT_PURPLE);
        lblMisionProgreso.setHorizontalAlignment(SwingConstants.RIGHT);
        cuerpoMision.add(lblMisionProgreso, g);

        panel.add(cuerpoMision, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCardJugador() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(15, 15, 15, 15)));

        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(CARD_BG);
        lblJugadorNombre = new JLabel("Héroe");
        lblJugadorNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblJugadorNombre.setForeground(TEXT_LIGHT);
        lblJugadorNivel = new JLabel("Nivel: 1");
        lblJugadorNivel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJugadorNivel.setForeground(TEXT_MUTED);
        header.add(lblJugadorNombre);
        header.add(lblJugadorNivel);
        card.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        // Vida
        gbc.gridy = 0;
        center.add(etiquetaSeccion("Puntos de Vida (HP)"), gbc);
        gbc.gridy = 1;
        barJugadorVida = new JProgressBar(0, 100);
        barJugadorVida.setForeground(ACCENT_GREEN);
        barJugadorVida.setBackground(TERMINAL_BG);
        barJugadorVida.setBorderPainted(false);
        barJugadorVida.setPreferredSize(new Dimension(0, 18));
        center.add(barJugadorVida, gbc);
        gbc.gridy = 2;
        lblJugadorVidaTexto = etiquetaDerecha("100 / 100 HP");
        center.add(lblJugadorVidaTexto, gbc);

        // EXP
        gbc.gridy = 3;
        center.add(etiquetaSeccion("Puntos de Experiencia (EXP)"), gbc);
        gbc.gridy = 4;
        barJugadorExp = new JProgressBar(0, 100);
        barJugadorExp.setForeground(ACCENT_YELLOW);
        barJugadorExp.setBackground(TERMINAL_BG);
        barJugadorExp.setBorderPainted(false);
        barJugadorExp.setPreferredSize(new Dimension(0, 18));
        center.add(barJugadorExp, gbc);
        gbc.gridy = 5;
        lblJugadorExpTexto = etiquetaDerecha("0 / 100 EXP");
        center.add(lblJugadorExpTexto, gbc);

        // Separador
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 0, 8, 0);
        JSeparator sep = new JSeparator();
        sep.setBackground(BORDER_COLOR);
        sep.setForeground(BORDER_COLOR);
        center.add(sep, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        // Stats
        gbc.gridy = 7;
        lblJugadorAtk = etiquetaStat("⚔️ Daño Total: 15.0");
        center.add(lblJugadorAtk, gbc);
        gbc.gridy = 8;
        lblJugadorDef = etiquetaStat("🛡️ Defensa Total: 6.0");
        center.add(lblJugadorDef, gbc);
        gbc.gridy = 9;
        lblJugadorMana = new JLabel("✨ Maná: --");
        lblJugadorMana.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJugadorMana.setForeground(ACCENT_BLUE);
        center.add(lblJugadorMana, gbc);

        // Equipamiento
        gbc.gridy = 10;
        gbc.insets = new Insets(8, 0, 8, 0);
        JSeparator sep2 = new JSeparator();
        sep2.setBackground(BORDER_COLOR);
        sep2.setForeground(BORDER_COLOR);
        center.add(sep2, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        gbc.gridy = 11;
        JLabel lblEquipTitle = new JLabel("EQUIPAMIENTO DETALLADO");
        lblEquipTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEquipTitle.setForeground(ACCENT_BLUE);
        center.add(lblEquipTitle, gbc);

        gbc.gridy = 12;
        JPanel panelArma = crearSlotEquip("⚔️ Arma: (Vacío)");
        lblEquipArma = (JLabel) ((BorderLayout) panelArma.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        btnDesequiparArma = (JButton) ((BorderLayout) panelArma.getLayout()).getLayoutComponent(BorderLayout.EAST);
        center.add(panelArma, gbc);

        gbc.gridy = 13;
        JPanel panelArmadura = crearSlotEquip("🛡️ Armadura: (Vacío)");
        lblEquipArmadura = (JLabel) ((BorderLayout) panelArmadura.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        btnDesequiparArmadura = (JButton) ((BorderLayout) panelArmadura.getLayout()).getLayoutComponent(BorderLayout.EAST);
        center.add(panelArmadura, gbc);

        gbc.gridy = 14;
        JPanel panelAccesorio = crearSlotEquip("💍 Accesorio: (Vacío)");
        lblEquipAccesorio = (JLabel) ((BorderLayout) panelAccesorio.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        btnDesequiparAccesorio = (JButton) ((BorderLayout) panelAccesorio.getLayout()).getLayoutComponent(BorderLayout.EAST);
        center.add(panelAccesorio, gbc);

        JScrollPane scrollCenter = new JScrollPane(center);
        scrollCenter.setBorder(null);
        scrollCenter.setOpaque(false);
        scrollCenter.getViewport().setOpaque(false);
        scrollCenter.getVerticalScrollBar().setUnitIncrement(12);
        scrollCenter.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(scrollCenter, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearCardEnemigo() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(15, 15, 15, 15)));

        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(CARD_BG);
        lblEnemigoNombre = new JLabel("Enemigo");
        lblEnemigoNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEnemigoNombre.setForeground(ACCENT_RED);
        lblEnemigoNivel = new JLabel("Nivel: 1");
        lblEnemigoNivel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEnemigoNivel.setForeground(TEXT_MUTED);
        header.add(lblEnemigoNombre);
        header.add(lblEnemigoNivel);
        card.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        gbc.gridy = 0;
        center.add(etiquetaSeccion("Puntos de Vida (HP)"), gbc);
        gbc.gridy = 1;
        barEnemigoVida = new JProgressBar(0, 100);
        barEnemigoVida.setForeground(ACCENT_RED);
        barEnemigoVida.setBackground(TERMINAL_BG);
        barEnemigoVida.setBorderPainted(false);
        barEnemigoVida.setPreferredSize(new Dimension(0, 18));
        center.add(barEnemigoVida, gbc);
        gbc.gridy = 2;
        lblEnemigoVidaTexto = etiquetaDerecha("100 / 100 HP");
        center.add(lblEnemigoVidaTexto, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(8, 0, 8, 0);
        JSeparator sep = new JSeparator();
        sep.setBackground(BORDER_COLOR);
        sep.setForeground(BORDER_COLOR);
        center.add(sep, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);

        gbc.gridy = 4;
        lblEnemigoAtk = etiquetaStat("⚔️ Daño Base: 8.0");
        center.add(lblEnemigoAtk, gbc);
        gbc.gridy = 5;
        lblEnemigoDef = etiquetaStat("🛡️ Defensa: 2.0");
        center.add(lblEnemigoDef, gbc);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelBotones() {
        JPanel panelBotones = new JPanel(new GridLayout(2, 4, 10, 8));
        panelBotones.setBackground(BG_DARK);
        panelBotones.setPreferredSize(new Dimension(0, 100));

        btnAtacar        = crearBoton("Atacar ⚔️", ACCENT_RED);
        btnHabilidad     = crearBoton("Habilidad ✨", ACCENT_BLUE);
        comboInventario  = crearComboInventario();
        btnUsarItem      = crearBoton("Usar Ítem 🧪", ACCENT_GREEN);
        btnGenerarEnemigo = crearBoton("Nuevo Enemigo 👺", ACCENT_BLUE);
        btnNuevoPersonaje = crearBoton("Nuevo Personaje 👤", TEXT_MUTED);
        btnGuardar       = crearBoton("Guardar 💾", ACCENT_YELLOW);
        btnCargar        = crearBoton("Cargar 📂", ACCENT_PURPLE);

        panelBotones.add(btnAtacar);
        panelBotones.add(btnHabilidad);
        panelBotones.add(comboInventario);
        panelBotones.add(btnUsarItem);
        panelBotones.add(btnGenerarEnemigo);
        panelBotones.add(btnNuevoPersonaje);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCargar);

        return panelBotones;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Registro de eventos — ActionListeners delegan al Controlador (1-3 líneas)
    // ══════════════════════════════════════════════════════════════════════════

    private void registrarEventos() {
        // Atacar — delega al controlador
        btnAtacar.addActionListener(e -> controlador.atacar());

        // Habilidad — delega al controlador
        btnHabilidad.addActionListener(e -> controlador.usarHabilidad());

        // Usar ítem — delega al controlador con el índice seleccionado
        btnUsarItem.addActionListener(e -> controlador.usarItem(comboInventario.getSelectedIndex()));

        // Generar enemigo — delega al controlador
        btnGenerarEnemigo.addActionListener(e -> controlador.generarNuevoEnemigo());

        // Nuevo personaje — crea personaje en la vista y lo pasa al controlador
        btnNuevoPersonaje.addActionListener(e -> {
            Personaje nuevo = seleccionarPersonaje();
            controlador.reiniciar(nuevo);
        });

        // Desequipar — delega al controlador con el slot correspondiente
        btnDesequiparArma.addActionListener(e -> controlador.desequipar(TipoSlot.ARMA));
        btnDesequiparArmadura.addActionListener(e -> controlador.desequipar(TipoSlot.ARMADURA));
        btnDesequiparAccesorio.addActionListener(e -> controlador.desequipar(TipoSlot.ACCESORIO));

        // Guardar partida — abre JFileChooser para seleccionar archivo de guardado
        btnGuardar.addActionListener(e -> {
            JFileChooser fileChooser = crearFileChooserJson();
            fileChooser.setDialogTitle("Guardar Partida");
            int resultado = fileChooser.showSaveDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                if (!ruta.toLowerCase().endsWith(".json")) {
                    ruta += ".json";
                }
                controlador.guardarPartida(ruta);
            }
        });

        // Cargar partida — abre JFileChooser para seleccionar archivo JSON
        btnCargar.addActionListener(e -> {
            JFileChooser fileChooser = crearFileChooserJson();
            fileChooser.setDialogTitle("Cargar Partida");
            int resultado = fileChooser.showOpenDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                controlador.cargarPartida(ruta);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos de actualización de sub-componentes
    // ══════════════════════════════════════════════════════════════════════════

    private void actualizarSlotEquip(JLabel lbl, JButton btn, String emoji, ItemEquipable equip, String nombre) {
        if (equip != null) {
            lbl.setText(emoji + " " + equip.getNombre());
            btn.setEnabled(true);
        } else {
            lbl.setText(emoji + " " + nombre + ": (Vacío)");
            btn.setEnabled(false);
        }
    }

    private void setVisibleEnemigoStats(boolean visible) {
        barEnemigoVida.setVisible(visible);
        lblEnemigoVidaTexto.setVisible(visible);
        lblEnemigoAtk.setVisible(visible);
        lblEnemigoDef.setVisible(visible);
    }

    private void actualizarComboInventario() {
        int selectedIndex = comboInventario.getSelectedIndex();
        comboInventario.removeAllItems();
        java.util.ArrayList<Items> inv = controlador.getJugador().getInventario();
        if (inv.isEmpty()) {
            comboInventario.addItem("(Mochila Vacía)");
        } else {
            for (Items item : inv) {
                comboInventario.addItem(item.getNombre());
            }
            int idx = (selectedIndex >= 0 && selectedIndex < inv.size()) ? selectedIndex : 0;
            comboInventario.setSelectedIndex(idx);
        }
    }

    private void actualizarPanelMision() {
        Mision mision = controlador.getMisionActiva();
        if (mision == null) {
            lblMisionNombre.setText("Sin misión activa");
            lblMisionDescripcion.setText(" ");
            barMisionProgreso.setValue(0);
            lblMisionProgreso.setText("—");
            lblMisionEstado.setText("—");
            return;
        }

        lblMisionNombre.setText(mision.getNombre());
        lblMisionDescripcion.setText("<html>" + mision.getDescripcion() + "</html>");
        barMisionProgreso.setValue((int) (mision.getProgresoPorcentaje() * 100));
        lblMisionProgreso.setText(mision.getProgresoTexto());

        // Color del estado
        EstadoMision estado = mision.getEstado();
        lblMisionEstado.setText("● " + estado.name());
        lblMisionEstado.setForeground(switch (estado) {
            case PENDIENTE   -> TEXT_MUTED;
            case EN_PROGRESO -> ACCENT_YELLOW;
            case COMPLETADA  -> ACCENT_GREEN;
            case FALLIDA     -> ACCENT_RED;
        });

        // Color de la barra según estado
        barMisionProgreso.setForeground(switch (estado) {
            case COMPLETADA  -> ACCENT_GREEN;
            case FALLIDA     -> ACCENT_RED;
            default          -> ACCENT_PURPLE;
        });
    }

    private void actualizarEstadoBotones() {
        Personaje jugador = controlador.getJugador();
        Enemigo enemigo   = controlador.getEnemigoActual();
        boolean jugadorVivo   = jugador.getPuntosVida() > 0;
        boolean enemigoActivo = enemigo != null && enemigo.getPuntosVida() > 0;

        if (!jugadorVivo) {
            btnAtacar.setEnabled(false);
            btnHabilidad.setEnabled(false);
            btnUsarItem.setEnabled(false);
            btnGenerarEnemigo.setEnabled(false);
            btnDesequiparArma.setEnabled(false);
            btnDesequiparArmadura.setEnabled(false);
            btnDesequiparAccesorio.setEnabled(false);
        } else if (enemigoActivo) {
            btnAtacar.setEnabled(true);
            btnUsarItem.setEnabled(!jugador.getInventario().isEmpty());
            btnGenerarEnemigo.setEnabled(true);
            actualizarBotonHabilidad(jugador);
        } else {
            btnAtacar.setEnabled(false);
            btnHabilidad.setEnabled(false);
            btnUsarItem.setEnabled(false);
            btnGenerarEnemigo.setEnabled(true);
            actualizarBotonHabilidad(jugador); // Muestra el nombre aunque esté deshabilitado
        }
        btnNuevoPersonaje.setEnabled(true);
    }

    private void actualizarBotonHabilidad(Personaje jugador) {
        if (jugador instanceof ActivadorHabilidad act) {
            btnHabilidad.setToolTipText(act.getDescripcionHabilidad());
            Enemigo enemigo = controlador.getEnemigoActual();
            boolean enemigoActivo = enemigo != null && enemigo.getPuntosVida() > 0;

            if (enemigoActivo && act.puedeUsarHabilidad()) {
                btnHabilidad.setEnabled(true);
                btnHabilidad.setText(act.getNombreHabilidad() + " ✨");
            } else {
                btnHabilidad.setEnabled(false);
                if (jugador instanceof Mago mago && mago.getMana() < 25) {
                    btnHabilidad.setText("Sin Maná 🔋");
                } else if (act.getCooldownRestante() > 0) {
                    btnHabilidad.setText(act.getNombreHabilidad() + " (" + act.getCooldownRestante() + "T) ⏳");
                } else {
                    btnHabilidad.setText(act.getNombreHabilidad() + " ✨");
                }
            }
        } else {
            btnHabilidad.setText("Habilidad ✨");
            btnHabilidad.setEnabled(false);
            btnHabilidad.setToolTipText(null);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Diálogo de selección de personaje (responsabilidad de la Vista)
    // ══════════════════════════════════════════════════════════════════════════

    private Personaje seleccionarPersonaje() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(BG_DARK);

        JLabel lblNom = new JLabel("Nombre del Héroe:");
        lblNom.setForeground(TEXT_LIGHT);
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextField txtNombre = new JTextField("Héroe");
        txtNombre.setBackground(CARD_BG);
        txtNombre.setForeground(TEXT_LIGHT);
        txtNombre.setCaretColor(TEXT_LIGHT);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setBorder(new LineBorder(BORDER_COLOR, 1));

        JLabel lblClase = new JLabel("Selecciona Clase:");
        lblClase.setForeground(TEXT_LIGHT);
        lblClase.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] clases = {"Guerrero ⚔️", "Mago 🔮", "Arquero 🏹", "Luchador 🥊"};
        JComboBox<String> comboClase = new JComboBox<>(clases);
        comboClase.setBackground(CARD_BG);
        comboClase.setForeground(TEXT_LIGHT);
        comboClase.setFont(new Font("Segoe UI", Font.BOLD, 14));
        comboClase.setBorder(new LineBorder(BORDER_COLOR, 1));

        panel.add(lblNom);
        panel.add(txtNombre);
        panel.add(lblClase);
        panel.add(comboClase);

        UIManager.put("OptionPane.background", BG_DARK);
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("Button.background", CARD_BG);
        UIManager.put("Button.foreground", TEXT_LIGHT);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Creación de Personaje",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) nombre = "Héroe";

        Personaje personaje;
        if (result == JOptionPane.OK_OPTION) {
            personaje = switch (comboClase.getSelectedIndex()) {
                case 1  -> new Mago(nombre);
                case 2  -> new Arquero(nombre);
                case 3  -> new Luchador(nombre);
                default -> new Guerrero(nombre);
            };
        } else {
            personaje = new Guerrero("Guerrero Supremo");
        }

        // Pociones iniciales
        personaje.agregarItem(Items.generarPocionAleatoria());
        personaje.agregarItem(Items.generarPocionAleatoria());
        return personaje;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Helpers de construcción de widgets
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel crearSlotEquip(String textoInicial) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(CARD_BG);
        JLabel lbl = new JLabel(textoInicial);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_LIGHT);
        JButton btn = crearBotonPico("Desequipar", ACCENT_RED);
        btn.setEnabled(false);
        panel.add(lbl, BorderLayout.CENTER);
        panel.add(btn, BorderLayout.EAST);
        return panel;
    }

    /**
     * Crea un JFileChooser preconfigurado con filtro .json y estilo oscuro.
     */
    private JFileChooser crearFileChooserJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }

    private JComboBox<String> crearComboInventario() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT_LIGHT);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        combo.setBorder(new LineBorder(BORDER_COLOR, 1));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_BLUE : CARD_BG);
                setForeground(TEXT_LIGHT);
                return this;
            }
        });
        return combo;
    }

    private JButton crearBoton(String texto, Color baseColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(baseColor.darker());
        btn.setBorder(new LineBorder(baseColor, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(baseColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(baseColor.darker());
            }
        });
        return btn;
    }

    private JButton crearBotonPico(String texto, Color baseColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(baseColor.darker().darker());
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(baseColor, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(baseColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(baseColor.darker().darker());
            }
        });
        return btn;
    }

    private JLabel etiquetaSeccion(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_LIGHT);
        return lbl;
    }

    private JLabel etiquetaDerecha(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        return lbl;
    }

    private JLabel etiquetaStat(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_LIGHT);
        return lbl;
    }
}
