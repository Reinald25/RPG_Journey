package com.rpg.logica.personaje;

import com.rpg.logica.contrato.ActivadorHabilidad;

public class Arquero extends Personaje implements ActivadorHabilidad {
    private boolean ultimoAtaqueEsquivado = false;
    private int cooldownDisparoCertero = 0;

    public Arquero(String nombre) {
        super(nombre, 100, 14, 5); // Stats iniciales de Arquero
        this.clase = "Arquero";
    }

    public boolean isUltimoAtaqueEsquivado() {
        return ultimoAtaqueEsquivado;
    }

    public void resetEsquiva() {
        this.ultimoAtaqueEsquivado = false;
    }

    @Override
    public void recibirDaño(double cantidad) {
        if (Math.random() < 0.20) { // 20% de probabilidad de esquivar por completo
            this.ultimoAtaqueEsquivado = true;
        } else {
            this.ultimoAtaqueEsquivado = false;
            super.recibirDaño(cantidad);
        }
    }

    @Override
    public String usarHabilidad(Personaje objetivo) {
        double daño = this.getDañoBase() * 1.5 - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        objetivo.recibirDaño(daño);
        this.ultimoAtaqueEsquivado = true; // Garantiza esquiva en el próximo ataque enemigo
        resetCooldown();
        return String.format("🏹 ¡DISPARO CERTERO! %s realiza un tiro preciso a %s causando %.1f de daño y entra en sigilo (Esquiva el siguiente ataque al 100%%).", 
                this.nombre, objetivo.getNombre(), daño);
    }

    @Override
    public boolean puedeUsarHabilidad() {
        return cooldownDisparoCertero == 0;
    }

    @Override
    public String getNombreHabilidad() {
        return "Disparo Certero";
    }

    @Override
    public String getDescripcionHabilidad() {
        return "Tiro letal (1.5x ATK) que asegura esquivar el próximo golpe enemigo (Cooldown: 4 turnos).";
    }

    @Override
    public int getCooldownRestante() {
        return cooldownDisparoCertero;
    }

    @Override
    public void reducirCooldown() {
        if (cooldownDisparoCertero > 0) {
            cooldownDisparoCertero--;
        }
    }

    @Override
    public void resetCooldown() {
        cooldownDisparoCertero = 4;
    }

    @Override
    public String atacar(Personaje objetivo) {
        double dañoTotal = this.getDañoBase();
        boolean esTiroDoble = Math.random() < 0.30; // 30% de probabilidad de Tiro Doble
        if (esTiroDoble) {
            dañoTotal *= 1.8;
        }
        
        double daño = dañoTotal - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        
        objetivo.recibirDaño(daño);
        
        if (esTiroDoble) {
            return String.format("🏹 ¡TIRO DOBLE! %s dispara dos flechas veloces a %s causando %.1f de daño.", 
                    this.nombre, objetivo.getNombre(), daño);
        } else {
            return String.format("%s dispara una flecha certera a %s causando %.1f de daño.", 
                    this.nombre, objetivo.getNombre(), daño);
        }
    }

    @Override
    public void subirNivel() {
        this.nivel++;
        this.puntosVidaMax += 18;
        this.puntosVida = this.getPuntosVidaMax();
        this.dañoBase += 4;
        this.defensa += 2;
    }
}
