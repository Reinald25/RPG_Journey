package com.rpg.logica.enemigo;

import com.rpg.logica.personaje.Personaje;

public class JefeDragon extends EnemigoBoss {
    
    public JefeDragon(int nivelJugador) {
        super(
            "Gran Dragón de Lava", 
            150 + (nivelJugador * 25), 
            12 + (nivelJugador * 4), 
            5 + (nivelJugador * 2)
        );
        this.nivel = nivelJugador + 2;
    }

    @Override
    public String ataqueEspecial(Personaje objetivo) {
        double daño = this.getDañoBase() * 2.0 - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        objetivo.recibirDaño(daño);
        return String.format("🔥 ¡ALIENTO DE FUEGO! %s lanza una bocanada de fuego abrasador a %s infligiendo %.1f de daño físico.", 
                this.nombre, objetivo.getNombre(), daño);
    }
}
