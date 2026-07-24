package com.rpg.logica.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Items {
    private String nombre;
    private int curacion;

    public Items(String nombre, int curacion) {
        this.nombre = nombre;
        this.curacion = curacion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCuracion() {
        return curacion;
    }

    /**
     * Genera de forma aleatoria una poción de curación que cura 100, 75, 50 o 25.
     * Aplica HashSet para almacenar los valores únicos de curación y ArrayList
     * para acceder a ellos de forma aleatoria a través de un índice.
     * 
     * @return Una instancia de Items que representa la poción de curación seleccionada.
     */
    public static Items generarPocionAleatoria() {
        // 1. Aplicar HashSet para almacenar los valores únicos de curación sin duplicados
        HashSet<Integer> conjuntoCuracion = new HashSet<>();
        conjuntoCuracion.add(100);
        conjuntoCuracion.add(75);
        conjuntoCuracion.add(50);
        conjuntoCuracion.add(25);

        // 2. Aplicar ArrayList para traspasar los elementos del conjunto y poder usar índices
        ArrayList<Integer> listaCuracion = new ArrayList<>(conjuntoCuracion);

        // 3. Generación aleatoria del índice para seleccionar el valor de curación
        Random random = new Random();
        int indiceAleatorio = random.nextInt(listaCuracion.size());
        int valorCuracion = listaCuracion.get(indiceAleatorio);

        // Retornamos un nuevo Item de poción con el valor de curación obtenido
        return new Items("Poción de Curación (" + valorCuracion + " HP)", valorCuracion);
    }
}
