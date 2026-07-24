package com.rpg.logica.personaje;

import com.rpg.logica.contrato.ActivadorHabilidad;

public class Luchador extends Personaje implements ActivadorHabilidad {
    private int cooldownPuñoSupremo = 0;

    public Luchador(String nombre) {
        super(nombre, 130, 15, 9); // Stats iniciales de Luchador
        this.clase = "Luchador";
    }

    @Override
    public String usarHabilidad(Personaje objetivo) {
        double sacrificio = Math.round(this.puntosVida * 0.15);
        if (sacrificio < 1 && this.puntosVida > 1) sacrificio = 1;
        this.puntosVida -= sacrificio;
        if (this.puntosVida < 1) this.puntosVida = 1; // Evita que se suicide

        double daño = (this.getDañoBase() * 3.0) - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        
        objetivo.recibirDaño(daño);
        resetCooldown();
        
        return String.format("👊 ¡PUÑO SUPREMO! %s sacrifica %.0f HP para liberar un impacto devastador contra %s, infligiendo %.1f de daño físico.", 
                this.nombre, sacrificio, objetivo.getNombre(), daño);
    }

    @Override
    public boolean puedeUsarHabilidad() {
        return cooldownPuñoSupremo == 0 && this.puntosVida > 5;
    }

    @Override
    public String getNombreHabilidad() {
        return "Puño Supremo";
    }

    @Override
    public String getDescripcionHabilidad() {
        return "Sacrifica el 15% del HP actual para infligir daño letal (3x ATK) (Cooldown: 2 turnos).";
    }

    @Override
    public int getCooldownRestante() {
        return cooldownPuñoSupremo;
    }

    @Override
    public void reducirCooldown() {
        if (cooldownPuñoSupremo > 0) {
            cooldownPuñoSupremo--;
        }
    }

    @Override
    public void resetCooldown() {
        cooldownPuñoSupremo = 2;
    }

    @Override
    public String atacar(Personaje objetivo) {
        // A menos vida, más daño infligido (furia pasiva)
        double multiplicadorFuria = 1.0 + (1.0 - (this.puntosVida / this.getPuntosVidaMax()));
        double dañoTotal = this.getDañoBase() * multiplicadorFuria;
        
        double daño = dañoTotal - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        
        objetivo.recibirDaño(daño);
        
        if (multiplicadorFuria > 1.2) {
            return String.format("💢 ¡FURIA DE COMBATE! %s asesta un puñetazo colosal a %s infligiendo %.1f de daño (Furia: +%.0f%% daño).", 
                    this.nombre, objetivo.getNombre(), daño, (multiplicadorFuria - 1.0) * 100);
        } else {
            return String.format("%s golpea con fuerza a %s infligiendo %.1f de daño físico.", 
                    this.nombre, objetivo.getNombre(), daño);
        }
    }

    @Override
    public void subirNivel() {
        this.nivel++;
        this.puntosVidaMax += 30; // Gran aumento de HP al subir de nivel
        this.puntosVida = this.getPuntosVidaMax();
        this.dañoBase += 4.5;
        this.defensa += 2.5;
    }
}
