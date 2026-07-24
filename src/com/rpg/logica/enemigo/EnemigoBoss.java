package com.rpg.logica.enemigo;

import com.rpg.logica.personaje.Personaje;

public abstract class EnemigoBoss extends Enemigo {
    
    public EnemigoBoss(String nombre, double puntosVidaMax, double dañoBase, double defensa) {
        super(1); // constructor base de Enemigo
        this.nombre = nombre;
        this.puntosVidaMax = puntosVidaMax;
        this.puntosVida = puntosVidaMax;
        this.dañoBase = dañoBase;
        this.defensa = defensa;
        this.clase = "Jefe";
    }

    /**
     * Contrato abstracto para el ataque especial que todo jefe debe implementar.
     */
    public abstract String ataqueEspecial(Personaje objetivo);
}
