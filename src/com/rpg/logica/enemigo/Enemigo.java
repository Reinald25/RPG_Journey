package com.rpg.logica.enemigo;

import com.rpg.logica.personaje.Personaje;

public class Enemigo extends Personaje {
    
    private static final String[] NOMBRES_ENEMIGOS = {
        "Trasgo de las Sombras", "Orco Furioso", "Esqueleto Guerrero", "Mago Oscuro", "Dragón de Lava"
    };

    public Enemigo(int nivelJugador) {
        super(
            obtenerNombreAleatorio(), 
            40 + (nivelJugador * 12),  // Vida max escala con el nivel del jugador
            6 + (nivelJugador * 2.5),  // Daño escala
            1 + (nivelJugador * 1.5)   // Defensa escala
        );
        this.nivel = nivelJugador + (int)(Math.random() * 3 - 1); // Nivel cercano al jugador
        if (this.nivel < 1) this.nivel = 1;
    }

    private static String obtenerNombreAleatorio() {
        int index = (int) (Math.random() * NOMBRES_ENEMIGOS.length);
        return NOMBRES_ENEMIGOS[index];
    }
}
