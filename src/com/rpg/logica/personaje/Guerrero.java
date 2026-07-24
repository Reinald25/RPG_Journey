package com.rpg.logica.personaje;

import com.rpg.logica.contrato.ActivadorHabilidad;

public class Guerrero extends Personaje implements ActivadorHabilidad {
    private int cooldownGolpeColosal = 0;

    public Guerrero(String nombre) {
        super(nombre, 120, 18, 8); // Estadísticas iniciales de Guerrero
        this.clase = "Guerrero";
    }

    @Override
    public String usarHabilidad(Personaje objetivo) {
        double daño = this.getDañoBase() * 2.2;
        // Ignora defensa por completo (no se resta getDefensa del objetivo)
        if (daño <= 0) daño = 1;
        objetivo.recibirDaño(daño);
        resetCooldown();
        return String.format("💥 ¡%s usa GOLPE COLOSAL! Asesta un impacto demoledor que ignora la defensa de %s infligiendo %.1f de daño físico.", 
                this.nombre, objetivo.getNombre(), daño);
    }

    @Override
    public boolean puedeUsarHabilidad() {
        return cooldownGolpeColosal == 0;
    }

    @Override
    public String getNombreHabilidad() {
        return "Golpe Colosal";
    }

    @Override
    public String getDescripcionHabilidad() {
        return "Daño masivo (2.2x ATK) ignorando la defensa del enemigo (Cooldown: 3 turnos).";
    }

    @Override
    public int getCooldownRestante() {
        return cooldownGolpeColosal;
    }

    @Override
    public void reducirCooldown() {
        if (cooldownGolpeColosal > 0) {
            cooldownGolpeColosal--;
        }
    }

    @Override
    public void resetCooldown() {
        cooldownGolpeColosal = 3;
    }

    @Override
    public String atacar(Personaje objetivo) {
        double dañoTotal = this.getDañoBase();
        boolean esCritico = Math.random() < 0.25; // 25% de probabilidad de crítico
        if (esCritico) {
            dañoTotal *= 2.0;
        }
        
        double daño = dañoTotal - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        
        objetivo.recibirDaño(daño);
        
        if (esCritico) {
            return String.format("¡GOLPE CRÍTICO! ⚔️ %s asesta un tajo devastador a %s infligiendo %.1f de daño físico.", 
                    this.nombre, objetivo.getNombre(), daño);
        } else {
            return String.format("%s ataca con su gran espada a %s infligiendo %.1f de daño físico.", 
                    this.nombre, objetivo.getNombre(), daño);
        }
    }

    @Override
    public void subirNivel() {
        this.nivel++;
        this.puntosVidaMax += 25; // Guerrero escala más vida
        this.puntosVida = this.getPuntosVidaMax();
        this.dañoBase += 5;       // Guerrero escala más daño
        this.defensa += 3;        // Guerrero escala más defensa
    }
}
