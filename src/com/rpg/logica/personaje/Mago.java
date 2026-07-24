package com.rpg.logica.personaje;

import com.rpg.logica.contrato.ActivadorHabilidad;

public class Mago extends Personaje implements ActivadorHabilidad {
    private double mana;
    private double manaMax;

    public Mago(String nombre) {
        super(nombre, 80, 12, 4); // Menos vida/daño físico/defensa inicial, pero con magia
        this.clase = "Mago";
        this.manaMax = 60;
        this.mana = 60;
    }

    public double getManaMax() {
        return manaMax + (equipamiento != null ? equipamiento.getBonoManaMax() : 0);
    }

    public double getMana() {
        return mana;
    }

    @Override
    public String usarHabilidad(Personaje objetivo) {
        if (this.mana < 25) {
            return String.format("❌ %s intenta lanzar Bola de Fuego, pero no tiene suficiente Maná (requiere 25 MP).", this.nombre);
        }
        this.mana -= 25;
        double dañoHechizo = this.getDañoBase() * 3.5; // Gran daño mágico
        double daño = dañoHechizo - (objetivo.getDefensa() * 0.25); // Ignora el 75% de la defensa enemiga
        if (daño <= 0) daño = 1;
        
        objetivo.recibirDaño(daño);
        return String.format("🔥 ¡BOLA DE FUEGO! %s lanza un conjuro explosivo a %s consumiendo 25 MP e infligiendo %.1f de daño mágico.", 
                this.nombre, objetivo.getNombre(), daño);
    }

    @Override
    public boolean puedeUsarHabilidad() {
        return this.mana >= 25;
    }

    @Override
    public String getNombreHabilidad() {
        return "Bola de Fuego";
    }

    @Override
    public String getDescripcionHabilidad() {
        return "Incendia al enemigo (3.5x ATK, ignora 75% DEF) consumiendo 25 MP.";
    }

    @Override
    public int getCooldownRestante() {
        return 0; // Limitada por maná, sin cooldown de turnos
    }

    @Override
    public void reducirCooldown() {
        // No aplica
    }

    @Override
    public void resetCooldown() {
        // No aplica
    }

    @Override
    public String atacar(Personaje objetivo) {
        // Ataque básico de bastón que recupera maná
        this.mana += 15;
        if (this.mana > this.getManaMax()) {
            this.mana = this.getManaMax();
        }
        double daño = this.getDañoBase() - objetivo.getDefensa();
        if (daño <= 0) daño = 1;
        
        objetivo.recibirDaño(daño);
        return String.format("🔮 %s realiza un ataque básico de bastón a %s causando %.1f de daño y canaliza 15 MP.", 
                this.nombre, objetivo.getNombre(), daño);
    }

    @Override
    public void subirNivel() {
        this.nivel++;
        this.puntosVidaMax += 12; // Menos vida por nivel
        this.puntosVida = this.getPuntosVidaMax();
        this.dañoBase += 3;       
        this.defensa += 1.5;
        this.manaMax += 20;       // Aumenta maná máximo
        this.mana = this.getManaMax(); // Recupera todo el maná (incluye bonos)
    }

    @Override
    public void curar(double cantidad) {
        super.curar(cantidad);
        // Las pociones también recuperan un poco de maná para el Mago (por ejemplo, el 50% de la curación)
        this.mana += cantidad * 0.5;
        if (this.mana > this.getManaMax()) {
            this.mana = this.getManaMax();
        }
    }
}
