package com.rpg.persistencia;

import com.google.gson.*;
import com.rpg.mision.*;

import java.lang.reflect.Type;

/**
 * Adaptador Gson custom para la jerarquía polimórfica de {@link Mision}.
 *
 * <p>Serializa un campo {@code "tipo"} para distinguir entre
 * {@link MisionCombate}, {@link MisionSobrevivir} y {@link MisionEliminarBoss},
 * y restaura correctamente los campos internos (progreso, meta, estado) al
 * deserializar.</p>
 */
public class AdaptadorMision implements JsonSerializer<Mision>, JsonDeserializer<Mision> {

    @Override
    public JsonElement serialize(Mision src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        // ── Tipo concreto ──
        obj.addProperty("tipo", obtenerTipo(src));

        // ── Campos comunes ──
        obj.addProperty("nombre", src.getNombre());
        obj.addProperty("descripcion", src.getDescripcion());
        obj.addProperty("estado", src.getEstado().name());

        // ── Campos específicos ──
        if (src instanceof MisionCombate mc) {
            obj.addProperty("enemigosMeta", mc.getEnemigosMeta());
            obj.addProperty("enemigosDerrotados", mc.getEnemigosDerrotados());
        } else if (src instanceof MisionSobrevivir ms) {
            obj.addProperty("rondasMeta", ms.getRondasMeta());
            obj.addProperty("rondasSuperadas", ms.getRondasSuperadas());
        } else if (src instanceof MisionEliminarBoss meb) {
            obj.addProperty("bossDerrotado", meb.getProgresoPorcentaje() >= 1.0);
        }

        return obj;
    }

    @Override
    public Mision deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String tipo = obj.get("tipo").getAsString();

        Mision mision = crearInstancia(tipo, obj);
        restaurarEstado(mision, obj);

        return mision;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos auxiliares
    // ══════════════════════════════════════════════════════════════════════════

    private String obtenerTipo(Mision src) {
        if (src instanceof MisionCombate) return "MisionCombate";
        if (src instanceof MisionSobrevivir) return "MisionSobrevivir";
        if (src instanceof MisionEliminarBoss) return "MisionEliminarBoss";
        return "Mision";
    }

    /**
     * Crea la instancia de misión correcta usando los constructores existentes.
     */
    private Mision crearInstancia(String tipo, JsonObject obj) {
        return switch (tipo) {
            case "MisionCombate" -> new MisionCombate(obj.get("enemigosMeta").getAsInt());
            case "MisionSobrevivir" -> new MisionSobrevivir(obj.get("rondasMeta").getAsInt());
            case "MisionEliminarBoss" -> new MisionEliminarBoss();
            default -> throw new JsonParseException("Tipo de misión desconocido: " + tipo);
        };
    }

    /**
     * Restaura el estado interno de la misión (progreso, estado) usando reflexión.
     */
    private void restaurarEstado(Mision mision, JsonObject obj) {
        try {
            // ── Estado de la misión ──
            String estadoStr = obj.get("estado").getAsString();
            EstadoMision estado = EstadoMision.valueOf(estadoStr);
            var fieldEstado = Mision.class.getDeclaredField("estado");
            fieldEstado.setAccessible(true);
            fieldEstado.set(mision, estado);

            // ── Progreso específico por tipo ──
            if (mision instanceof MisionCombate mc) {
                int derrotados = obj.get("enemigosDerrotados").getAsInt();
                var field = MisionCombate.class.getDeclaredField("enemigosDerrotados");
                field.setAccessible(true);
                field.setInt(mc, derrotados);
            } else if (mision instanceof MisionSobrevivir ms) {
                int superadas = obj.get("rondasSuperadas").getAsInt();
                var field = MisionSobrevivir.class.getDeclaredField("rondasSuperadas");
                field.setAccessible(true);
                field.setInt(ms, superadas);
            } else if (mision instanceof MisionEliminarBoss meb) {
                boolean derrotado = obj.has("bossDerrotado") && obj.get("bossDerrotado").getAsBoolean();
                var field = MisionEliminarBoss.class.getDeclaredField("bossDerrotado");
                field.setAccessible(true);
                field.setBoolean(meb, derrotado);
            }
        } catch (Exception e) {
            throw new JsonParseException("Error restaurando estado de misión: " + e.getMessage(), e);
        }
    }
}
