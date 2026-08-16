package com.rpg.persistencia;

import com.google.gson.*;
import com.rpg.logica.item.ItemEquipable;
import com.rpg.logica.item.Items;
import com.rpg.logica.item.TipoSlot;
import java.lang.reflect.Type;

/**
 * Adaptador Gson custom para la jerarquía {@link Items} /
 * {@link ItemEquipable}.
 *
 * <p>
 * Serializa un campo {@code "tipo"} para distinguir entre un ítem consumible
 * simple y un {@link ItemEquipable} con sus atributos de equipo.
 * </p>
 */
public class AdaptadorItems implements JsonSerializer<Items>, JsonDeserializer<Items> {

    @Override
    public JsonElement serialize(Items src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        if (src instanceof ItemEquipable equipable) {
            obj.addProperty("tipo", "ItemEquipable");
            // Nombre base sin los bonos formateados
            obj.addProperty("nombreBase", obtenerNombreBase(equipable));
            obj.addProperty("curacion", src.getCuracion());
            obj.addProperty("slot", equipable.getSlot().name());
            obj.addProperty("bonoDaño", equipable.getBonoDaño());
            obj.addProperty("bonoDefensa", equipable.getBonoDefensa());
            obj.addProperty("bonoVidaMax", equipable.getBonoVidaMax());
            obj.addProperty("bonoManaMax", equipable.getBonoManaMax());
            obj.addProperty("claseRequerida", equipable.getClaseRequerida());
        } else {
            obj.addProperty("tipo", "Items");
            obj.addProperty("nombre", src.getNombre());
            obj.addProperty("curacion", src.getCuracion());
        }

        return obj;
    }

    @Override
    public Items deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String tipo = obj.get("tipo").getAsString();

        if ("ItemEquipable".equals(tipo)) {
            String nombreBase = obj.get("nombreBase").getAsString();
            TipoSlot slot = TipoSlot.valueOf(obj.get("slot").getAsString());
            double bonoDaño = obj.get("bonoDaño").getAsDouble();
            double bonoDefensa = obj.get("bonoDefensa").getAsDouble();
            double bonoVidaMax = obj.get("bonoVidaMax").getAsDouble();
            double bonoManaMax = obj.get("bonoManaMax").getAsDouble();
            String claseRequerida = obj.has("claseRequerida") && !obj.get("claseRequerida").isJsonNull()
                    ? obj.get("claseRequerida").getAsString()
                    : null;

            return new ItemEquipable(nombreBase, slot, bonoDaño, bonoDefensa, bonoVidaMax, bonoManaMax, claseRequerida);
        } else {
            String nombre = obj.get("nombre").getAsString();
            int curacion = obj.get("curacion").getAsInt();
            return new Items(nombre, curacion);
        }
    }

    /**
     * Obtiene el nombre base del ItemEquipable accediendo al campo privado
     * de la clase padre Items, ya que getNombre() de ItemEquipable añade
     * los bonos formateados.
     */
    private String obtenerNombreBase(ItemEquipable equipable) {
        try {
            var field = Items.class.getDeclaredField("nombre");
            field.setAccessible(true);
            return (String) field.get(equipable);
        } catch (Exception e) {
            // Fallback: usar getNombre() completo
            return equipable.getNombre();
        }
    }
}
