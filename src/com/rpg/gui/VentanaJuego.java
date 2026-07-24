package com.rpg.gui;

import com.rpg.logica.personaje.*;
import com.rpg.logica.enemigo.*;
import com.rpg.logica.item.*;
import com.rpg.logica.contrato.*;
import com.rpg.mision.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VentanaJuego extends JFrame {

    private Personaje jugador;
    private Enemigo enemigoActual;

    // Colores Premium
    private static final Color BG_DARK = new Color(15, 23, 42);       // Slate 900
    private static final Color CARD_BG = new Color(30, 41, 59);       // Slate 800
    private static final Color TEXT_LIGHT = new Color(248, 250, 252); // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184); // Slate 400
    private static final Color BORDER_COLOR = new Color(51, 65, 85);  // Slate 700
    
    private static final Color ACCENT_GREEN = new Color(16, 185, 129); // Emerald 500
    private static final Color ACCENT_RED = new Color(239, 68, 68);    // Red 500
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);  // Blue 500
    private static final Color ACCENT_YELLOW = new Color(245, 158, 11); // Amber 500

    private static final Color TERMINAL_BG = new Color(2, 6, 23);       // Slate 950
    private static final Color TERMINAL_TEXT = new Color(34, 197, 94);   // Green 500

    // Componentes UI Jugador
    private JLabel lblJugadorNombre;
    private JLabel lblJugadorNivel;
    private JProgressBar barJugadorVida;
    private JLabel lblJugadorVidaTexto;
    private JProgressBar barJugadorExp;
    private JLabel lblJugadorExpTexto;
    private JLabel lblJugadorAtk;
    private JLabel lblJugadorDef;
    private JLabel lblJugadorMana;

    // Componentes UI Equipamiento Jugador
    private JLabel lblEquipArma;
    private JLabel lblEquipArmadura;
    private JLabel lblEquipAccesorio;
    private JButton btnDesequiparArma;
    private JButton btnDesequiparArmadura;
    private JButton btnDesequiparAccesorio;

    // Componentes UI Enemigo
    private JLabel lblEnemigoNombre;
    private JLabel lblEnemigoNivel;
    private JProgressBar barEnemigoVida;
    private JLabel lblEnemigoVidaTexto;
    private JLabel lblEnemigoAtk;
    private JLabel lblEnemigoDef;

    // Terminal Log
    private JTextArea txtLog;
    
    // Mochila/Inventario
    private JComboBox<String> comboInventario;
    private JButton btnUsarItem;

    // Botones
    private JButton btnAtacar;
    private JButton btnHabilidad;
    private JButton btnGenerarEnemigo;
    private JButton btnNuevoPersonaje;

    public VentanaJuego() {
        // Configuración de la ventana principal
        setTitle("RPG Journey - Edición Premium");
        setSize(920, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // Inicializar datos del juego con selección de clase
        seleccionarPersonaje();
        enemigoActual = new Enemigo(jugador.getNivel());

        // Panel de Título Superior
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(BG_DARK);
        panelHeader.setBorder(new EmptyBorder(15, 20, 0, 20));
        
        JLabel lblTitulo = new JLabel("⚔️ RPG JOURNEY ⚔️");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(ACCENT_BLUE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelHeader.add(lblTitulo, BorderLayout.CENTER);
        add(panelHeader, BorderLayout.NORTH);

        // Panel Central (Estadísticas de Personajes Side-by-Side)
        JPanel panelPersonajes = new JPanel(new GridLayout(1, 2, 20, 0));
        panelPersonajes.setBackground(BG_DARK);
        panelPersonajes.setBorder(new EmptyBorder(10, 20, 10, 20));

        panelPersonajes.add(crearCardJugador());
        panelPersonajes.add(crearCardEnemigo());
        
        // Panel Inferior (Log + Botones)
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBackground(BG_DARK);
        panelInferior.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Consola Terminal
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
        scrollLog.setPreferredSize(new Dimension(0, 180));
        panelInferior.add(scrollLog, BorderLayout.CENTER);

        // Botones de acción lateral/inferior - 6 columnas para el inventario, habilidad y usar ítem
        JPanel panelBotones = new JPanel(new GridLayout(1, 6, 10, 0));
        panelBotones.setBackground(BG_DARK);
        panelBotones.setPreferredSize(new Dimension(0, 50));

        btnAtacar = crearBoton("Atacar ⚔️", ACCENT_RED);
        btnHabilidad = crearBoton("Habilidad ✨", ACCENT_BLUE);

        // Inicialización del combo de inventario
        comboInventario = new JComboBox<>();
        comboInventario.setBackground(CARD_BG);
        comboInventario.setForeground(TEXT_LIGHT);
        comboInventario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comboInventario.setBorder(new LineBorder(BORDER_COLOR, 1));
        comboInventario.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_BLUE : CARD_BG);
                setForeground(TEXT_LIGHT);
                return this;
            }
        });

        btnUsarItem = crearBoton("Usar Ítem 🧪", ACCENT_GREEN);
        btnGenerarEnemigo = crearBoton("Nuevo Enemigo 👺", ACCENT_BLUE);
        btnNuevoPersonaje = crearBoton("Nuevo Personaje 👤", TEXT_MUTED);

        panelBotones.add(btnAtacar);
        panelBotones.add(btnHabilidad);
        panelBotones.add(comboInventario);
        panelBotones.add(btnUsarItem);
        panelBotones.add(btnGenerarEnemigo);
        panelBotones.add(btnNuevoPersonaje);

        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(BG_DARK);
        mainContent.add(panelPersonajes, BorderLayout.CENTER);
        mainContent.add(panelInferior, BorderLayout.SOUTH);
        
        add(mainContent, BorderLayout.CENTER);

        // Registrar Eventos
        btnAtacar.addActionListener(new ActionAtacar());
        btnHabilidad.addActionListener(new ActionHabilidad());
        btnUsarItem.addActionListener(new ActionUsarItem());
        btnGenerarEnemigo.addActionListener(new ActionGenerarEnemigo());
        btnNuevoPersonaje.addActionListener(new ActionNuevoPersonaje());
        btnDesequiparArma.addActionListener(new ActionDesequipar(TipoSlot.ARMA));
        btnDesequiparArmadura.addActionListener(new ActionDesequipar(TipoSlot.ARMADURA));
        btnDesequiparAccesorio.addActionListener(new ActionDesequipar(TipoSlot.ACCESORIO));

        // Mensaje inicial
        log("[SISTEMA] ¡Bienvenido a RPG Journey! Explora, lucha contra enemigos y sube de nivel para volverte invencible.");
        log("[SISTEMA] Tu primer oponente ha aparecido: un " + enemigoActual.getNombre() + " (Nivel " + enemigoActual.getNivel() + ").");
        
        actualizarUI();
    }

    private JPanel crearCardJugador() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Header del Personaje
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(CARD_BG);
        
        lblJugadorNombre = new JLabel("Guerrero Supremo");
        lblJugadorNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblJugadorNombre.setForeground(TEXT_LIGHT);
        
        lblJugadorNivel = new JLabel("Nivel: 1");
        lblJugadorNivel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJugadorNivel.setForeground(TEXT_MUTED);

        header.add(lblJugadorNombre);
        header.add(lblJugadorNivel);
        card.add(header, BorderLayout.NORTH);

        // Stats y Barra de Vida
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Barra de Vida
        gbc.gridy = 0;
        JLabel lblVidaTitle = new JLabel("Puntos de Vida (HP)");
        lblVidaTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVidaTitle.setForeground(TEXT_LIGHT);
        center.add(lblVidaTitle, gbc);

        gbc.gridy = 1;
        barJugadorVida = new JProgressBar(0, 100);
        barJugadorVida.setValue(100);
        barJugadorVida.setForeground(ACCENT_GREEN);
        barJugadorVida.setBackground(TERMINAL_BG);
        barJugadorVida.setBorderPainted(false);
        barJugadorVida.setPreferredSize(new Dimension(0, 20));
        center.add(barJugadorVida, gbc);

        gbc.gridy = 2;
        lblJugadorVidaTexto = new JLabel("100 / 100 HP");
        lblJugadorVidaTexto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblJugadorVidaTexto.setForeground(TEXT_MUTED);
        lblJugadorVidaTexto.setHorizontalAlignment(SwingConstants.RIGHT);
        center.add(lblJugadorVidaTexto, gbc);

        // Barra de Experiencia (EXP)
        gbc.gridy = 3;
        JLabel lblExpTitle = new JLabel("Puntos de Experiencia (EXP)");
        lblExpTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExpTitle.setForeground(TEXT_LIGHT);
        center.add(lblExpTitle, gbc);

        gbc.gridy = 4;
        barJugadorExp = new JProgressBar(0, 100);
        barJugadorExp.setValue(0);
        barJugadorExp.setForeground(ACCENT_YELLOW);
        barJugadorExp.setBackground(TERMINAL_BG);
        barJugadorExp.setBorderPainted(false);
        barJugadorExp.setPreferredSize(new Dimension(0, 20));
        center.add(barJugadorExp, gbc);

        gbc.gridy = 5;
        lblJugadorExpTexto = new JLabel("0 / 100 EXP");
        lblJugadorExpTexto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblJugadorExpTexto.setForeground(TEXT_MUTED);
        lblJugadorExpTexto.setHorizontalAlignment(SwingConstants.RIGHT);
        center.add(lblJugadorExpTexto, gbc);

        // Separador
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 10, 0);
        JSeparator sep = new JSeparator();
        sep.setBackground(BORDER_COLOR);
        sep.setForeground(BORDER_COLOR);
        center.add(sep, gbc);

        // Estadísticas ATK y DEF
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 7;
        lblJugadorAtk = new JLabel("⚔️ Daño Base: 15.0");
        lblJugadorAtk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJugadorAtk.setForeground(TEXT_LIGHT);
        center.add(lblJugadorAtk, gbc);

        gbc.gridy = 8;
        lblJugadorDef = new JLabel("🛡️ Defensa: 6.0");
        lblJugadorDef.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJugadorDef.setForeground(TEXT_LIGHT);
        center.add(lblJugadorDef, gbc);

        gbc.gridy = 9;
        lblJugadorMana = new JLabel("✨ Maná: --");
        lblJugadorMana.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblJugadorMana.setForeground(ACCENT_BLUE);
        center.add(lblJugadorMana, gbc);

        // Separador Equipamiento
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 0, 10, 0);
        JSeparator sep2 = new JSeparator();
        sep2.setBackground(BORDER_COLOR);
        sep2.setForeground(BORDER_COLOR);
        center.add(sep2, gbc);

        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 11;
        JLabel lblEquipTitle = new JLabel("EQUIPAMIENTO DETALLADO");
        lblEquipTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEquipTitle.setForeground(ACCENT_BLUE);
        center.add(lblEquipTitle, gbc);

        // Arma Slot Panel
        gbc.gridy = 12;
        JPanel panelArma = new JPanel(new BorderLayout(5, 0));
        panelArma.setBackground(CARD_BG);
        lblEquipArma = new JLabel("⚔️ Arma: (Vacío)");
        lblEquipArma.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEquipArma.setForeground(TEXT_LIGHT);
        btnDesequiparArma = crearBotonPico("Desequipar", ACCENT_RED);
        panelArma.add(lblEquipArma, BorderLayout.CENTER);
        panelArma.add(btnDesequiparArma, BorderLayout.EAST);
        center.add(panelArma, gbc);

        // Armadura Slot Panel
        gbc.gridy = 13;
        JPanel panelArmadura = new JPanel(new BorderLayout(5, 0));
        panelArmadura.setBackground(CARD_BG);
        lblEquipArmadura = new JLabel("🛡️ Armadura: (Vacío)");
        lblEquipArmadura.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEquipArmadura.setForeground(TEXT_LIGHT);
        btnDesequiparArmadura = crearBotonPico("Desequipar", ACCENT_RED);
        panelArmadura.add(lblEquipArmadura, BorderLayout.CENTER);
        panelArmadura.add(btnDesequiparArmadura, BorderLayout.EAST);
        center.add(panelArmadura, gbc);

        // Accesorio Slot Panel
        gbc.gridy = 14;
        JPanel panelAccesorio = new JPanel(new BorderLayout(5, 0));
        panelAccesorio.setBackground(CARD_BG);
        lblEquipAccesorio = new JLabel("💍 Accesorio: (Vacío)");
        lblEquipAccesorio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEquipAccesorio.setForeground(TEXT_LIGHT);
        btnDesequiparAccesorio = crearBotonPico("Desequipar", ACCENT_RED);
        panelAccesorio.add(lblEquipAccesorio, BorderLayout.CENTER);
        panelAccesorio.add(btnDesequiparAccesorio, BorderLayout.EAST);
        center.add(panelAccesorio, gbc);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardEnemigo() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Header del Enemigo
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

        // Stats y Barra de Vida
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Barra de Vida
        gbc.gridy = 0;
        JLabel lblVidaTitle = new JLabel("Puntos de Vida (HP)");
        lblVidaTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVidaTitle.setForeground(TEXT_LIGHT);
        center.add(lblVidaTitle, gbc);

        gbc.gridy = 1;
        barEnemigoVida = new JProgressBar(0, 100);
        barEnemigoVida.setValue(100);
        barEnemigoVida.setForeground(ACCENT_RED);
        barEnemigoVida.setBackground(TERMINAL_BG);
        barEnemigoVida.setBorderPainted(false);
        barEnemigoVida.setPreferredSize(new Dimension(0, 20));
        center.add(barEnemigoVida, gbc);

        gbc.gridy = 2;
        lblEnemigoVidaTexto = new JLabel("100 / 100 HP");
        lblEnemigoVidaTexto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEnemigoVidaTexto.setForeground(TEXT_MUTED);
        lblEnemigoVidaTexto.setHorizontalAlignment(SwingConstants.RIGHT);
        center.add(lblEnemigoVidaTexto, gbc);

        // Separador
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 10, 0);
        JSeparator sep = new JSeparator();
        sep.setBackground(BORDER_COLOR);
        sep.setForeground(BORDER_COLOR);
        center.add(sep, gbc);

        // Estadísticas ATK y DEF
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 4;
        lblEnemigoAtk = new JLabel("⚔️ Daño Base: 8.0");
        lblEnemigoAtk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEnemigoAtk.setForeground(TEXT_LIGHT);
        center.add(lblEnemigoAtk, gbc);

        gbc.gridy = 5;
        lblEnemigoDef = new JLabel("🛡️ Defensa: 2.0");
        lblEnemigoDef.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEnemigoDef.setForeground(TEXT_LIGHT);
        center.add(lblEnemigoDef, gbc);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JButton crearBoton(String texto, Color baseColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(baseColor.darker());
        btn.setBorder(new LineBorder(baseColor, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover simple
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(baseColor);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(baseColor.darker());
                }
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
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(baseColor.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(baseColor.darker().darker());
                }
            }
        });

        return btn;
    }

    private void log(String msg) {
        txtLog.append(msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void seleccionarPersonaje() {
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
            this, 
            panel, 
            "Creación de Personaje", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );
        
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            nombre = "Héroe";
        }
        
        int claseIndex = comboClase.getSelectedIndex();
        if (result == JOptionPane.OK_OPTION) {
            if (claseIndex == 0) {
                jugador = new Guerrero(nombre);
            } else if (claseIndex == 1) {
                jugador = new Mago(nombre);
            } else if (claseIndex == 2) {
                jugador = new Arquero(nombre);
            } else {
                jugador = new Luchador(nombre);
            }
        } else {
            jugador = new Guerrero("Guerrero Supremo");
        }
        
        jugador.agregarItem(Items.generarPocionAleatoria());
        jugador.agregarItem(Items.generarPocionAleatoria());
    }

    private void actualizarUI() {
        // Datos Jugador
        lblJugadorNombre.setText(jugador.getNombre() + " (" + jugador.getClase() + ")");
        lblJugadorNivel.setText("Nivel: " + jugador.getNivel());
        barJugadorVida.setMaximum((int) jugador.getPuntosVidaMax());
        barJugadorVida.setValue((int) jugador.getPuntosVida());
        lblJugadorVidaTexto.setText(String.format("%.1f / %.1f HP", jugador.getPuntosVida(), jugador.getPuntosVidaMax()));
        
        if (barJugadorExp != null) {
            barJugadorExp.setMaximum(jugador.getExpRequerida());
            barJugadorExp.setValue(jugador.getExp());
        }
        if (lblJugadorExpTexto != null) {
            lblJugadorExpTexto.setText(String.format("%d / %d EXP", jugador.getExp(), jugador.getExpRequerida()));
        }

        lblJugadorAtk.setText(String.format("⚔️ Daño Total: %.1f", jugador.getDañoBase()));
        lblJugadorDef.setText(String.format("🛡️ Defensa Total: %.1f", jugador.getDefensa()));

        if (jugador instanceof Mago) {
            Mago mago = (Mago) jugador;
            lblJugadorMana.setText(String.format("✨ Maná: %.0f / %.0f MP", mago.getMana(), mago.getManaMax()));
            lblJugadorMana.setVisible(true);
        } else {
            lblJugadorMana.setVisible(false);
        }

        // Actualizar Equipamiento en UI
        if (jugador.getEquipamiento() != null) {
            ItemEquipable arma = jugador.getEquipamiento().getArma();
            if (arma != null) {
                lblEquipArma.setText("⚔️ " + arma.getNombre());
                btnDesequiparArma.setEnabled(true);
            } else {
                lblEquipArma.setText("⚔️ Arma: (Vacío)");
                btnDesequiparArma.setEnabled(false);
            }

            ItemEquipable armadura = jugador.getEquipamiento().getArmadura();
            if (armadura != null) {
                lblEquipArmadura.setText("🛡️ " + armadura.getNombre());
                btnDesequiparArmadura.setEnabled(true);
            } else {
                lblEquipArmadura.setText("🛡️ Armadura: (Vacío)");
                btnDesequiparArmadura.setEnabled(false);
            }

            ItemEquipable accesorio = jugador.getEquipamiento().getAccesorio();
            if (accesorio != null) {
                lblEquipAccesorio.setText("💍 " + accesorio.getNombre());
                btnDesequiparAccesorio.setEnabled(true);
            } else {
                lblEquipAccesorio.setText("💍 Accesorio: (Vacío)");
                btnDesequiparAccesorio.setEnabled(false);
            }
        }

        // Datos Enemigo
        if (enemigoActual != null) {
            lblEnemigoNombre.setText(enemigoActual.getNombre());
            lblEnemigoNivel.setText("Nivel: " + enemigoActual.getNivel());
            barEnemigoVida.setMaximum((int) enemigoActual.getPuntosVidaMax());
            barEnemigoVida.setValue((int) enemigoActual.getPuntosVida());
            lblEnemigoVidaTexto.setText(String.format("%.1f / %.1f HP", enemigoActual.getPuntosVida(), enemigoActual.getPuntosVidaMax()));
            lblEnemigoAtk.setText(String.format("⚔️ Daño Base: %.1f", enemigoActual.getDañoBase()));
            lblEnemigoDef.setText(String.format("🛡️ Defensa: %.1f", enemigoActual.getDefensa()));
            
            barEnemigoVida.setVisible(true);
            lblEnemigoVidaTexto.setVisible(true);
            lblEnemigoAtk.setVisible(true);
            lblEnemigoDef.setVisible(true);
        } else {
            lblEnemigoNombre.setText("Sin Oponente");
            lblEnemigoNivel.setText("Nivel: -");
            barEnemigoVida.setVisible(false);
            lblEnemigoVidaTexto.setVisible(false);
            lblEnemigoAtk.setVisible(false);
            lblEnemigoDef.setVisible(false);
        }

        // Actualizar combo box de inventario
        if (comboInventario != null) {
            int selectedIndex = comboInventario.getSelectedIndex();
            comboInventario.removeAllItems();
            java.util.ArrayList<Items> inv = jugador.getInventario();
            if (inv.isEmpty()) {
                comboInventario.addItem("(Mochila Vacía)");
            } else {
                for (Items item : inv) {
                    comboInventario.addItem(item.getNombre());
                }
                if (selectedIndex >= 0 && selectedIndex < inv.size()) {
                    comboInventario.setSelectedIndex(selectedIndex);
                } else if (!inv.isEmpty()) {
                    comboInventario.setSelectedIndex(0);
                }
            }
        }

        // Manejo de estado de botones según salud
        if (jugador.getPuntosVida() <= 0) {
            btnAtacar.setEnabled(false);
            btnHabilidad.setEnabled(false);
            btnHabilidad.setText("Habilidad ✨");
            btnHabilidad.setToolTipText(null);
            btnUsarItem.setEnabled(false);
            btnGenerarEnemigo.setEnabled(false);
            btnDesequiparArma.setEnabled(false);
            btnDesequiparArmadura.setEnabled(false);
            btnDesequiparAccesorio.setEnabled(false);
        } else {
            if (enemigoActual != null && enemigoActual.getPuntosVida() > 0) {
                btnAtacar.setEnabled(true);
                btnUsarItem.setEnabled(!jugador.getInventario().isEmpty());
                
                // Actualizar botón de habilidad
                if (jugador instanceof ActivadorHabilidad) {
                    ActivadorHabilidad act = (ActivadorHabilidad) jugador;
                    btnHabilidad.setToolTipText(act.getDescripcionHabilidad());
                    if (act.puedeUsarHabilidad()) {
                        btnHabilidad.setEnabled(true);
                        btnHabilidad.setText(act.getNombreHabilidad() + " ✨");
                    } else {
                        btnHabilidad.setEnabled(false);
                        if (jugador instanceof Mago && ((Mago) jugador).getMana() < 25) {
                            btnHabilidad.setText("Sin Maná 🔋");
                        } else {
                            btnHabilidad.setText(act.getNombreHabilidad() + " (" + act.getCooldownRestante() + "T) ⏳");
                        }
                    }
                } else {
                    btnHabilidad.setText("Habilidad ✨");
                    btnHabilidad.setEnabled(false);
                    btnHabilidad.setToolTipText(null);
                }
            } else {
                btnAtacar.setEnabled(false);
                btnHabilidad.setEnabled(false);
                btnUsarItem.setEnabled(false);
                if (jugador instanceof ActivadorHabilidad) {
                    btnHabilidad.setText(((ActivadorHabilidad) jugador).getNombreHabilidad() + " ✨");
                } else {
                    btnHabilidad.setText("Habilidad ✨");
                }
            }
            btnGenerarEnemigo.setEnabled(true);
        }
        btnNuevoPersonaje.setEnabled(true);
    }

    // ACCIONES DE LOS BOTONES
    
    private class ActionAtacar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (enemigoActual == null || enemigoActual.getPuntosVida() <= 0) return;

            // Turno del Jugador
            String msgAtaqueJugador = jugador.atacar(enemigoActual);
            log("[COMBATE] " + msgAtaqueJugador);

            if (enemigoActual.getPuntosVida() <= 0) {
                log("[VICTORIA] ¡Has derrotado a " + enemigoActual.getNombre() + "!");
                
                // Otorgar EXP
                int expGanada = 40 + (enemigoActual.getNivel() * 15);
                log("[EXPERIENCIA] ✨ Has ganado " + expGanada + " EXP.");
                boolean subioNivel = jugador.ganarExperiencia(expGanada);
                if (subioNivel) {
                    log("[SISTEMA] ⭐ ¡HAS SUBIDO DE NIVEL AUTOMÁTICAMENTE! Ahora eres Nivel " + jugador.getNivel() + ". Tus estadísticas han aumentado y tu HP se ha restaurado al máximo.");
                }

                Items drop;
                if (Math.random() < 0.50) {
                    drop = Items.generarPocionAleatoria();
                } else {
                    drop = ItemEquipable.generarEquipamientoAleatorio(jugador.getNivel(), jugador.getClase());
                }
                jugador.agregarItem(drop);
                log("[BOTÍN] " + enemigoActual.getNombre() + " dejó caer: " + drop.getNombre() + ". ¡Guardada en tu mochila!");
                log("[SISTEMA] Haz clic en 'Nuevo Enemigo' para invocar otro oponente.");
                actualizarUI();
                return;
            }

            // Contraataque del Enemigo
            String msgAtaqueEnemigo = enemigoActual.atacar(jugador);
            if (jugador instanceof Arquero && ((Arquero) jugador).isUltimoAtaqueEsquivado()) {
                log("[COMBATE] 🍃 ¡" + jugador.getNombre() + " esquivó ágilmente el ataque de " + enemigoActual.getNombre() + "!");
                ((Arquero) jugador).resetEsquiva();
            } else {
                log("[COMBATE] " + msgAtaqueEnemigo);
            }

            if (jugador.getPuntosVida() <= 0) {
                log("[DERROTA] Has sido derrotado por " + enemigoActual.getNombre() + "... Tu aventura ha terminado.");
                log("[SISTEMA] Pulsa 'Reiniciar' para volver a intentarlo.");
            }

            // Avanzar turno (reducir cooldowns)
            jugador.avanzarTurno();
            actualizarUI();
        }
    }

    private class ActionUsarItem implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (jugador.getPuntosVida() <= 0) return;

            int index = comboInventario.getSelectedIndex();
            if (index < 0 || jugador.getInventario().isEmpty()) {
                log("[SISTEMA] No tienes ítems para usar.");
                return;
            }

            Items item = jugador.getInventario().get(index);
            
            if (item instanceof ItemEquipable) {
                ItemEquipable equipable = (ItemEquipable) item;
                if (equipable.getClaseRequerida() != null && !equipable.getClaseRequerida().equalsIgnoreCase(jugador.getClase())) {
                    log("[SISTEMA] ❌ No puedes equipar " + item.getNombre() + ". Clase requerida: " + equipable.getClaseRequerida());
                    return;
                }
                
                boolean exito = jugador.usarItem(index);
                if (exito) {
                    log("[EQUIPO] 🛡️ " + jugador.getNombre() + " se ha equipado: " + item.getNombre());
                    ejecutarContraataqueEnemigo();
                } else {
                    log("[SISTEMA] Error al equipar el ítem.");
                }
            } else {
                double vidaPrevia = jugador.getPuntosVida();
                boolean exito = jugador.usarItem(index);

                if (exito) {
                    double curacionEfectiva = jugador.getPuntosVida() - vidaPrevia;
                    log("[ACCION] 🧪 " + jugador.getNombre() + " bebe una " + item.getNombre() + " y se cura " + String.format("%.1f", curacionEfectiva) + " HP.");
                    ejecutarContraataqueEnemigo();
                } else {
                    log("[SISTEMA] Error al usar el ítem.");
                }
            }

            actualizarUI();
        }

        private void ejecutarContraataqueEnemigo() {
            if (enemigoActual != null && enemigoActual.getPuntosVida() > 0) {
                log("[COMBATE] ¡" + enemigoActual.getNombre() + " aprovechó tu distracción para atacarte!");
                String msgAtaqueEnemigo = enemigoActual.atacar(jugador);
                if (jugador instanceof Arquero && ((Arquero) jugador).isUltimoAtaqueEsquivado()) {
                    log("[COMBATE] 🍃 ¡" + jugador.getNombre() + " esquivó ágilmente el ataque de " + enemigoActual.getNombre() + "!");
                    ((Arquero) jugador).resetEsquiva();
                } else {
                    log("[COMBATE] " + msgAtaqueEnemigo);
                }

                if (jugador.getPuntosVida() <= 0) {
                    log("[DERROTA] Has sido derrotado por " + enemigoActual.getNombre() + "... Tu aventura ha terminado.");
                    log("[SISTEMA] Pulsa 'Reiniciar' para volver a intentarlo.");
                }
            }
        }
    }

    private class ActionHabilidad implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (enemigoActual == null || enemigoActual.getPuntosVida() <= 0) return;
            if (!(jugador instanceof ActivadorHabilidad)) return;

            ActivadorHabilidad act = (ActivadorHabilidad) jugador;
            if (!act.puedeUsarHabilidad()) {
                return;
            }

            // Usar habilidad
            String msgHabilidad = act.usarHabilidad(enemigoActual);
            log("[HABILIDAD] " + msgHabilidad);
            
            if (enemigoActual.getPuntosVida() <= 0) {
                log("[VICTORIA] ¡Has derrotado a " + enemigoActual.getNombre() + "!");
                
                // Otorgar EXP
                int expGanada = 40 + (enemigoActual.getNivel() * 15);
                log("[EXPERIENCIA] ✨ Has ganado " + expGanada + " EXP.");
                boolean subioNivel = jugador.ganarExperiencia(expGanada);
                if (subioNivel) {
                    log("[SISTEMA] ⭐ ¡HAS SUBIDO DE NIVEL AUTOMÁTICAMENTE! Ahora eres Nivel " + jugador.getNivel() + ". Tus estadísticas han aumentado y tu HP se ha restaurado al máximo.");
                }

                Items drop;
                if (Math.random() < 0.50) {
                    drop = Items.generarPocionAleatoria();
                } else {
                    drop = ItemEquipable.generarEquipamientoAleatorio(jugador.getNivel(), jugador.getClase());
                }
                jugador.agregarItem(drop);
                log("[BOTÍN] " + enemigoActual.getNombre() + " dejó caer: " + drop.getNombre() + ". ¡Guardada en tu mochila!");
                log("[SISTEMA] Haz clic en 'Nuevo Enemigo' para invocar otro oponente.");
                actualizarUI();
                return;
            }

            // Contraataque del Enemigo
            String msgAtaqueEnemigo = enemigoActual.atacar(jugador);
            if (jugador instanceof Arquero && ((Arquero) jugador).isUltimoAtaqueEsquivado()) {
                log("[COMBATE] 🍃 ¡" + jugador.getNombre() + " esquivó ágilmente el ataque de " + enemigoActual.getNombre() + "!");
                ((Arquero) jugador).resetEsquiva();
            } else {
                log("[COMBATE] " + msgAtaqueEnemigo);
            }

            if (jugador.getPuntosVida() <= 0) {
                log("[DERROTA] Has sido derrotado por " + enemigoActual.getNombre() + "... Tu aventura ha terminado.");
                log("[SISTEMA] Pulsa 'Reiniciar' para volver a intentarlo.");
            }

            // Avanzar turno (decrementar cooldowns de habilidad del jugador)
            jugador.avanzarTurno();
            actualizarUI();
        }
    }

    private class ActionDesequipar implements ActionListener {
        private TipoSlot slot;

        public ActionDesequipar(TipoSlot slot) {
            this.slot = slot;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (jugador.getPuntosVida() <= 0) return;

            ItemEquipable eq = null;
            if (slot == TipoSlot.ARMA) eq = jugador.getEquipamiento().getArma();
            else if (slot == TipoSlot.ARMADURA) eq = jugador.getEquipamiento().getArmadura();
            else if (slot == TipoSlot.ACCESORIO) eq = jugador.getEquipamiento().getAccesorio();

            if (eq != null) {
                boolean exito = jugador.desequiparItem(slot);
                if (exito) {
                    log("[EQUIPO] 📤 Te has desequipado: " + eq.getNombre());
                    actualizarUI();
                }
            }
        }
    }

    private class ActionGenerarEnemigo implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            enemigoActual = new Enemigo(jugador.getNivel());
            log("[SISTEMA] ¡Ha aparecido un " + enemigoActual.getNombre() + " (Nivel " + enemigoActual.getNivel() + ") listo para luchar!");
            actualizarUI();
        }
    }

    private class ActionNuevoPersonaje implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            seleccionarPersonaje();
            enemigoActual = new Enemigo(jugador.getNivel());
            txtLog.setText("");
            log("[SISTEMA] --- NUEVO PERSONAJE SELECCIONADO ---");
            log("[SISTEMA] Tu héroe ha comenzado (" + jugador.getNombre() + " - " + jugador.getClase() + "). Aparece un " + enemigoActual.getNombre() + " (Nivel " + enemigoActual.getNivel() + ").");
            actualizarUI();
        }
    }
}
