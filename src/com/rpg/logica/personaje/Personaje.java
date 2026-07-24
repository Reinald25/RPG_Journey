package com.rpg.logica.personaje;

import java.util.ArrayList;
import com.rpg.logica.contrato.ActivadorHabilidad;
import com.rpg.logica.item.Items;
import com.rpg.logica.item.ItemEquipable;
import com.rpg.logica.item.TipoSlot;

public class Personaje {
    protected ArrayList<Items> inventario = new ArrayList<>();
    protected String nombre;
    protected String clase;
    protected int nivel;
    protected double puntosVida;
    protected double puntosVidaMax;
    protected double dañoBase;
    protected double defensa;
    protected Equipamiento equipamiento;
    protected int exp;
    protected int expRequerida;

    public Personaje(String nombre, double puntosVidaMax, double dañoBase, double defensa) {
        this.nombre = nombre;
        this.puntosVidaMax = puntosVidaMax;
        this.puntosVida = puntosVidaMax; // Inicia con vida al máximo
        this.nivel = 1;
        this.dañoBase = dañoBase;
        this.defensa = defensa;
        this.clase = "Aventurero";
        this.equipamiento = new Equipamiento();
        this.exp = 0;
        this.expRequerida = 100;
    }

    public String getNombre() { return nombre; }
    public String getClase() { return clase; }
    public int getNivel() { return nivel; }
    public int getExp() { return exp; }
    public int getExpRequerida() { return expRequerida; }
    public double getPuntosVida() { return puntosVida; }
    public double getPuntosVidaMax() { 
        return puntosVidaMax + (equipamiento != null ? equipamiento.getBonoVidaMax() : 0); 
    }
    public double getDañoBase() { 
        return dañoBase + (equipamiento != null ? equipamiento.getBonoDaño() : 0); 
    }
    public double getDefensa() { 
        return defensa + (equipamiento != null ? equipamiento.getBonoDefensa() : 0); 
    }
    public Equipamiento getEquipamiento() { return equipamiento; }

    public String atacar(Personaje objetivo) {
        double daño = this.getDañoBase() - objetivo.getDefensa();
        if (daño <= 0) daño = 1; // Al menos 1 de daño
        objetivo.recibirDaño(daño);
        return String.format("%s ataca a %s causando %.1f de daño.", this.nombre, objetivo.getNombre(), daño);
    }

    public void recibirDaño(double cantidad) {
        this.puntosVida -= cantidad;
        if (this.puntosVida < 0) {
            this.puntosVida = 0;
        }
    }

    public void curar(double cantidad) {
        this.puntosVida += cantidad;
        if (this.puntosVida > this.getPuntosVidaMax()) {
            this.puntosVida = this.getPuntosVidaMax();
        }
    }

    public void subirNivel() {
        this.nivel++;
        this.puntosVidaMax += 15; // Sube vida max
        this.puntosVida = this.getPuntosVidaMax(); // Cura al subir nivel (incluye bonos)
        this.dañoBase += 3;       // Aumenta daño
        this.defensa += 2;        // Aumenta defensa
        this.expRequerida = this.nivel * 100;
    }

    public boolean ganarExperiencia(int cantidad) {
        this.exp += cantidad;
        boolean subioNivel = false;
        while (this.exp >= this.expRequerida) {
            this.exp -= this.expRequerida;
            this.subirNivel();
            this.expRequerida = this.nivel * 100;
            subioNivel = true;
        }
        return subioNivel;
    }

    public void agregarItem(Items item) {
        this.inventario.add(item);
    }

    public boolean usarItem(int index) {
        if (index >= 0 && index < inventario.size()) {
            Items item = inventario.get(index);
            if (item instanceof ItemEquipable) {
                ItemEquipable equipable = (ItemEquipable) item;
                // Validar restricción de clase
                if (equipable.getClaseRequerida() != null && !equipable.getClaseRequerida().equalsIgnoreCase(this.clase)) {
                    return false; // Restricción fallida
                }
                inventario.remove(index);
                ItemEquipable antiguo = equipamiento.equipar(equipable.getSlot(), equipable);
                if (antiguo != null) {
                    inventario.add(antiguo);
                }
                // Ajustar vida si supera el nuevo máximo
                double maxVida = this.getPuntosVidaMax();
                if (this.puntosVida > maxVida) {
                    this.puntosVida = maxVida;
                }
                return true;
            } else {
                inventario.remove(index);
                this.curar(item.getCuracion());
                return true;
            }
        }
        return false;
    }

    public boolean desequiparItem(TipoSlot slot) {
        if (equipamiento == null) return false;
        ItemEquipable antiguo = equipamiento.desequipar(slot);
        if (antiguo != null) {
            inventario.add(antiguo);
            // Ajustar vida si supera el nuevo máximo
            double maxVida = this.getPuntosVidaMax();
            if (this.puntosVida > maxVida) {
                this.puntosVida = maxVida;
            }
            return true;
        }
        return false;
    }

    public void avanzarTurno() {
        if (this instanceof ActivadorHabilidad) {
            ((ActivadorHabilidad) this).reducirCooldown();
        }
    }

    public ArrayList<Items> getInventario() {
        return inventario;
    }

    public void subirVidaMaxEspecial(double cant) {
        this.puntosVidaMax += cant;
        this.puntosVida = this.getPuntosVidaMax();
    }

    public void subirDañoEspecial(double cant) {
        this.dañoBase += cant;
    }

    public void subirDefensaEspecial(double cant) {
        this.defensa += cant;
    }
}
