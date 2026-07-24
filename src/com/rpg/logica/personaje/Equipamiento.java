package com.rpg.logica.personaje;

import com.rpg.logica.item.ItemEquipable;
import com.rpg.logica.item.TipoSlot;

public class Equipamiento {
    private ItemEquipable arma;
    private ItemEquipable armadura;
    private ItemEquipable accesorio;

    public Equipamiento() {
        this.arma = null;
        this.armadura = null;
        this.accesorio = null;
    }

    public ItemEquipable getArma() { return arma; }
    public ItemEquipable getArmadura() { return armadura; }
    public ItemEquipable getAccesorio() { return accesorio; }

    /**
     * Equipa un item en el slot correspondiente.
     * Retorna el item que estaba anteriormente equipado en ese slot, o null.
     */
    public ItemEquipable equipar(TipoSlot slot, ItemEquipable item) {
        ItemEquipable antiguo = null;
        if (slot == TipoSlot.ARMA) {
            antiguo = this.arma;
            this.arma = item;
        } else if (slot == TipoSlot.ARMADURA) {
            antiguo = this.armadura;
            this.armadura = item;
        } else if (slot == TipoSlot.ACCESORIO) {
            antiguo = this.accesorio;
            this.accesorio = item;
        }
        return antiguo;
    }

    /**
     * Desequipa el item del slot correspondiente y lo retorna.
     */
    public ItemEquipable desequipar(TipoSlot slot) {
        ItemEquipable antiguo = null;
        if (slot == TipoSlot.ARMA) {
            antiguo = this.arma;
            this.arma = null;
        } else if (slot == TipoSlot.ARMADURA) {
            antiguo = this.armadura;
            this.armadura = null;
        } else if (slot == TipoSlot.ACCESORIO) {
            antiguo = this.accesorio;
            this.accesorio = null;
        }
        return antiguo;
    }

    public double getBonoDaño() {
        double total = 0;
        if (arma != null) total += arma.getBonoDaño();
        if (armadura != null) total += armadura.getBonoDaño();
        if (accesorio != null) total += accesorio.getBonoDaño();
        return total;
    }

    public double getBonoDefensa() {
        double total = 0;
        if (arma != null) total += arma.getBonoDefensa();
        if (armadura != null) total += armadura.getBonoDefensa();
        if (accesorio != null) total += accesorio.getBonoDefensa();
        return total;
    }

    public double getBonoVidaMax() {
        double total = 0;
        if (arma != null) total += arma.getBonoVidaMax();
        if (armadura != null) total += armadura.getBonoVidaMax();
        if (accesorio != null) total += accesorio.getBonoVidaMax();
        return total;
    }

    public double getBonoManaMax() {
        double total = 0;
        if (arma != null) total += arma.getBonoManaMax();
        if (armadura != null) total += armadura.getBonoManaMax();
        if (accesorio != null) total += accesorio.getBonoManaMax();
        return total;
    }
}
