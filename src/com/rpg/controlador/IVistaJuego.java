package com.rpg.controlador;

/**
 * Interfaz mínima (ISP — Interface Segregation Principle) que el
 * {@code ControladorCombate} usa para comunicarse con la Vista.
 *
 * <p>La Vista ({@code VentanaJuego}) implementa esta interfaz. El controlador
 * solo conoce esta abstracción, nunca a la clase concreta Swing.</p>
 *
 * <p>Esto garantiza el principio D de SOLID (Inversión de Dependencias):
 * el controlador depende de una abstracción, no de un detalle de implementación.</p>
 */
public interface IVistaJuego {

    /**
     * Solicita a la vista que actualice todos sus componentes con el estado
     * actual del modelo (jugador, enemigo, misión).
     */
    void actualizarUI();

    /**
     * Agrega un mensaje con etiqueta al log de combate.
     *
     * @param tag     Categoría del mensaje (ej: "COMBATE", "SISTEMA", "BOTÍN").
     * @param mensaje Texto del mensaje a mostrar.
     */
    void mostrarMensaje(String tag, String mensaje);

    /**
     * Limpia el contenido del log de combate.
     */
    void limpiarLog();
}
