package com.rpg.logica.item;

import java.util.Random;

public class ItemEquipable extends Items {
    private TipoSlot slot;
    private double bonoDaño;
    private double bonoDefensa;
    private double bonoVidaMax;
    private double bonoManaMax;
    private String claseRequerida; // null si es para todas las clases

    public ItemEquipable(String nombre, TipoSlot slot, double bonoDaño, double bonoDefensa, 
                         double bonoVidaMax, double bonoManaMax, String claseRequerida) {
        super(nombre, 0); // Curación es 0 para ítems equipables
        this.slot = slot;
        this.bonoDaño = bonoDaño;
        this.bonoDefensa = bonoDefensa;
        this.bonoVidaMax = bonoVidaMax;
        this.bonoManaMax = bonoManaMax;
        this.claseRequerida = claseRequerida;
    }

    public TipoSlot getSlot() { return slot; }
    public double getBonoDaño() { return bonoDaño; }
    public double getBonoDefensa() { return bonoDefensa; }
    public double getBonoVidaMax() { return bonoVidaMax; }
    public double getBonoManaMax() { return bonoManaMax; }
    public String getClaseRequerida() { return claseRequerida; }

    @Override
    public String getNombre() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getNombre());
        sb.append(" (");
        
        boolean first = true;
        if (bonoDaño > 0) {
            sb.append("+").append(String.format("%.0f", bonoDaño)).append(" ATK");
            first = false;
        }
        if (bonoDefensa > 0) {
            if (!first) sb.append(", ");
            sb.append("+").append(String.format("%.0f", bonoDefensa)).append(" DEF");
            first = false;
        }
        if (bonoVidaMax > 0) {
            if (!first) sb.append(", ");
            sb.append("+").append(String.format("%.0f", bonoVidaMax)).append(" HP");
            first = false;
        }
        if (bonoManaMax > 0) {
            if (!first) sb.append(", ");
            sb.append("+").append(String.format("%.0f", bonoManaMax)).append(" MP");
            first = false;
        }
        sb.append(")");
        if (claseRequerida != null) {
            sb.append(" [").append(claseRequerida).append("]");
        }
        return sb.toString();
    }

    /**
     * Genera de forma aleatoria un equipamiento adecuado para la clase del jugador
     * y escalado según el nivel del jugador.
     */
    public static ItemEquipable generarEquipamientoAleatorio(int nivel, String claseJugador) {
        Random rand = new Random();
        int tipoRoll = rand.nextInt(10); // 0-3: Arma, 4-7: Armadura, 8-9: Accesorio
        
        TipoSlot slotSeleccionado;
        if (tipoRoll < 4) {
            slotSeleccionado = TipoSlot.ARMA;
        } else if (tipoRoll < 8) {
            slotSeleccionado = TipoSlot.ARMADURA;
        } else {
            slotSeleccionado = TipoSlot.ACCESORIO;
        }

        double bonoDaño = 0;
        double bonoDefensa = 0;
        double bonoVidaMax = 0;
        double bonoManaMax = 0;
        String nombre = "";
        String claseReq = (slotSeleccionado == TipoSlot.ACCESORIO) ? null : claseJugador;

        if (slotSeleccionado == TipoSlot.ARMA) {
            // Generación de Armas según clase
            double baseAtk = 3 + (nivel * 2.5);
            bonoDaño = Math.round(baseAtk + rand.nextDouble() * 3);
            
            if ("Guerrero".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Espada de Bronce", "Espadón de Hierro", "Mandoble de Acero Templado", "Gran Hacha Rúnica", "Filo del Inframundo"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
            } else if ("Mago".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Varita de Aprendiz", "Bastón de Fresno", "Cetro del Conjurador", "Bastón del Archicónclave", "Orbe Prohibido del Vacío"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
                bonoManaMax = Math.round(10 + (nivel * 8) + rand.nextDouble() * 10);
                bonoDaño = Math.round(bonoDaño * 0.7); // Menos daño físico directo
            } else if ("Arquero".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Arco Corto Simple", "Arco Largo de Tejo", "Ballesta de Repetición", "Arco de Precisión Élfico", "Susurro de la Tempestad"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
            } else if ("Luchador".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Vendas de Pelea", "Guanteletes de Hierro", "Nudilleras de Acero", "Garras de Bronce Reforzado", "Nudillos del Dragón Relámpago"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
                bonoVidaMax = Math.round(5 + (nivel * 4) + rand.nextDouble() * 5);
            } else {
                nombre = "Espada Básica";
            }
            nombre = "⚔️ " + nombre;

        } else if (slotSeleccionado == TipoSlot.ARMADURA) {
            // Generación de Armaduras según clase
            double baseDef = 1 + (nivel * 1.5);
            bonoDefensa = Math.round(baseDef + rand.nextDouble() * 2);

            if ("Guerrero".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Jubón de Cuero Rígido", "Cota de Malla", "Peto de Acero Brillante", "Armadura de Placas Titánica", "Armadura Gótica Eterna"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
                bonoDefensa = Math.round(bonoDefensa * 1.3); // Guerreros tienen más defensa
            } else if ("Mago".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Túnica de Lino Sencilla", "Manto del Aprendiz", "Túnica del Conjurador", "Túnica de Astrólogo Celeste", "Vestiduras del Fénix Arcano"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
                bonoDefensa = Math.round(bonoDefensa * 0.7); // Magos tienen menos defensa
                bonoManaMax = Math.round(5 + (nivel * 6) + rand.nextDouble() * 8);
            } else if ("Arquero".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Chaleco de Cuero Rústico", "Jubón Tachonado", "Manto del Explorador", "Capa del Cazador Nocturno", "Armadura Élfica de Viento"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
            } else if ("Luchador".equalsIgnoreCase(claseJugador)) {
                String[] nombres = {"Faja de Pelea Ligera", "Arnés de Pecho Rudo", "Cinto del Gladiador", "Armadura del Campeón", "Coraza de Piel de Behemoth"};
                nombre = obtenerNombrePorNivel(nombres, nivel);
                bonoVidaMax = Math.round(10 + (nivel * 6) + rand.nextDouble() * 10);
            } else {
                nombre = "Ropa de Viajero";
            }
            nombre = "🛡️ " + nombre;

        } else {
            // Generación de Accesorios (Para todas las clases)
            String[] nombres = {"Anillo de Cobre", "Anillo de Plata de Vida", "Colgante del Héroe", "Amuleto de Rubí Fino", "Ojo de Dragón Supremo"};
            nombre = "💍 " + obtenerNombrePorNivel(nombres, nivel);
            
            // Atributos mixtos para accesorios
            int rollAcc = rand.nextInt(4);
            if (rollAcc == 0) {
                bonoVidaMax = Math.round(10 + (nivel * 10) + rand.nextDouble() * 15);
            } else if (rollAcc == 1) {
                bonoManaMax = Math.round(10 + (nivel * 8) + rand.nextDouble() * 12);
            } else if (rollAcc == 2) {
                bonoDaño = Math.round(2 + (nivel * 1.5) + rand.nextDouble() * 2);
            } else {
                bonoDefensa = Math.round(1 + (nivel * 1.2) + rand.nextDouble() * 1.5);
            }
        }

        return new ItemEquipable(nombre, slotSeleccionado, bonoDaño, bonoDefensa, bonoVidaMax, bonoManaMax, claseReq);
    }

    private static String obtenerNombrePorNivel(String[] nombres, int nivel) {
        int idx = (nivel - 1);
        if (idx < 0) idx = 0;
        if (idx >= nombres.length) idx = nombres.length - 1;
        return nombres[idx];
    }
}
