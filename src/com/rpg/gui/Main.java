package com.rpg.gui;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Inicializar la interfaz gráfica de usuario en el EDT de Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VentanaJuego ventana = new VentanaJuego();
                ventana.setVisible(true);
            }
        });
    }
}