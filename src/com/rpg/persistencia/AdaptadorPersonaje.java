package com.rpg.persistencia;

import com.google.gson.*;
import com.rpg.logica.enemigo.Enemigo;
import com.rpg.logica.enemigo.JefeDragon;
import com.rpg.logica.item.ItemEquipable;
import com.rpg.logica.item.Items;
import com.rpg.logica.item.TipoSlot;
import com.rpg.logica.personaje.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Adaptador Gson custom para la jerarquía polimórfica de {@link Personaje}.
 *
 * <p>Serializa un campo extra {@code "tipo"} para preservar la clase concreta
 * (Guerrero, Mago, Arquero, Luchador, Enemigo, JefeDragon) y restaura
 * correctamente los campos específicos de cada subclase al deserializar.</p>
 */
public class AdaptadorPersonaje implements JsonSerializer<Personaje>, JsonDeserializer<Personaje> {

    @Override
    public JsonElement serialize(Personaje src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        // ── Tipo concreto ──
        obj.addProperty("tipo", obtenerTipo(src));

        // ── Campos base de Personaje ──
        obj.addProperty("nombre", src.getNombre());
        obj.addProperty("clase", src.getClase());
        obj.addProperty("nivel", src.getNivel());
        obj.addProperty("puntosVida", src.getPuntosVida());
        // Se guarda puntosVidaMax sin bonos de equipamiento
        obj.addProperty("puntosVidaMaxBase", obtenerPuntosVidaMaxBase(src));
        obj.addProperty("dañoBase", obtenerDañoBaseReal(src));
        obj.addProperty("defensa", obtenerDefensaReal(src));
        obj.addProperty("exp", src.getExp());
        obj.addProperty("expRequerida", src.getExpRequerida());

        // ── Inventario ──
        JsonArray invArray = new JsonArray();
        for (Items item : src.getInventario()) {
            invArray.add(context.serialize(item, Items.class));
        }
        obj.add("inventario", invArray);

        // ── Equipamiento ──
        JsonObject equipObj = new JsonObject();
        Equipamiento eq = src.getEquipamiento();
        if (eq != null) {
            if (eq.getArma() != null) equipObj.add("arma", context.serialize(eq.getArma(), Items.class));
            if (eq.getArmadura() != null) equipObj.add("armadura", context.serialize(eq.getArmadura(), Items.class));
            if (eq.getAccesorio() != null) equipObj.add("accesorio", context.serialize(eq.getAccesorio(), Items.class));
        }
        obj.add("equipamiento", equipObj);

        // ── Campos específicos por subclase ──
        if (src instanceof Mago mago) {
            obj.addProperty("mana", mago.getMana());
            // manaMax sin bonos de equipamiento
            double manaMaxBase = mago.getManaMax() - (eq != null ? eq.getBonoManaMax() : 0);
            obj.addProperty("manaMaxBase", manaMaxBase);
        }
        if (src instanceof Guerrero) {
            obj.addProperty("cooldown", ((com.rpg.logica.contrato.ActivadorHabilidad) src).getCooldownRestante());
        }
        if (src instanceof Arquero) {
            obj.addProperty("cooldown", ((com.rpg.logica.contrato.ActivadorHabilidad) src).getCooldownRestante());
        }
        if (src instanceof Luchador) {
            obj.addProperty("cooldown", ((com.rpg.logica.contrato.ActivadorHabilidad) src).getCooldownRestante());
        }

        return obj;
    }

    @Override
    public Personaje deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String tipo = obj.get("tipo").getAsString();
        String nombre = obj.get("nombre").getAsString();

        // ── Crear instancia de la subclase correcta ──
        Personaje personaje = crearInstancia(tipo, nombre, obj);

        // ── Restaurar campos base ──
        restaurarCamposBase(personaje, obj);

        // ── Restaurar inventario ──
        if (obj.has("inventario")) {
            JsonArray invArray = obj.getAsJsonArray("inventario");
            personaje.getInventario().clear();
            for (JsonElement elem : invArray) {
                Items item = context.deserialize(elem, Items.class);
                personaje.agregarItem(item);
            }
        }

        // ── Restaurar equipamiento ──
        if (obj.has("equipamiento")) {
            JsonObject equipObj = obj.getAsJsonObject("equipamiento");
            Equipamiento eq = personaje.getEquipamiento();
            if (equipObj.has("arma")) {
                ItemEquipable arma = (ItemEquipable) context.deserialize(equipObj.get("arma"), Items.class);
                eq.equipar(TipoSlot.ARMA, arma);
            }
            if (equipObj.has("armadura")) {
                ItemEquipable armadura = (ItemEquipable) context.deserialize(equipObj.get("armadura"), Items.class);
                eq.equipar(TipoSlot.ARMADURA, armadura);
            }
            if (equipObj.has("accesorio")) {
                ItemEquipable accesorio = (ItemEquipable) context.deserialize(equipObj.get("accesorio"), Items.class);
                eq.equipar(TipoSlot.ACCESORIO, accesorio);
            }
        }

        // ── Restaurar campos específicos ──
        restaurarCamposEspecificos(personaje, obj);

        return personaje;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos auxiliares
    // ══════════════════════════════════════════════════════════════════════════

    private String obtenerTipo(Personaje src) {
        if (src instanceof JefeDragon) return "JefeDragon";
        if (src instanceof Enemigo) return "Enemigo";
        if (src instanceof Guerrero) return "Guerrero";
        if (src instanceof Mago) return "Mago";
        if (src instanceof Arquero) return "Arquero";
        if (src instanceof Luchador) return "Luchador";
        return "Personaje";
    }

    /**
     * Obtiene puntosVidaMax base (sin bonos de equipamiento) usando reflexión
     * para acceder al campo protegido.
     */
    private double obtenerPuntosVidaMaxBase(Personaje src) {
        try {
            var field = Personaje.class.getDeclaredField("puntosVidaMax");
            field.setAccessible(true);
            return field.getDouble(src);
        } catch (Exception e) {
            // Fallback: restar bonos de equipamiento
            double bono = src.getEquipamiento() != null ? src.getEquipamiento().getBonoVidaMax() : 0;
            return src.getPuntosVidaMax() - bono;
        }
    }

    private double obtenerDañoBaseReal(Personaje src) {
        try {
            var field = Personaje.class.getDeclaredField("dañoBase");
            field.setAccessible(true);
            return field.getDouble(src);
        } catch (Exception e) {
            double bono = src.getEquipamiento() != null ? src.getEquipamiento().getBonoDaño() : 0;
            return src.getDañoBase() - bono;
        }
    }

    private double obtenerDefensaReal(Personaje src) {
        try {
            var field = Personaje.class.getDeclaredField("defensa");
            field.setAccessible(true);
            return field.getDouble(src);
        } catch (Exception e) {
            double bono = src.getEquipamiento() != null ? src.getEquipamiento().getBonoDefensa() : 0;
            return src.getDefensa() - bono;
        }
    }

    /**
     * Crea una instancia de la subclase correcta. Usa constructores existentes
     * y luego sobreescribe los campos con reflexión.
     */
    private Personaje crearInstancia(String tipo, String nombre, JsonObject obj) {
        return switch (tipo) {
            case "Guerrero" -> new Guerrero(nombre);
            case "Mago" -> new Mago(nombre);
            case "Arquero" -> new Arquero(nombre);
            case "Luchador" -> new Luchador(nombre);
            case "JefeDragon" -> new JefeDragon(1); // nivel temporal, se sobreescribe
            case "Enemigo" -> new Enemigo(1);       // nivel temporal, se sobreescribe
            default -> new Personaje(nombre, 100, 10, 5);
        };
    }

    /**
     * Restaura los campos base del Personaje usando reflexión para acceder
     * a los campos protegidos sin necesidad de setters públicos.
     */
    private void restaurarCamposBase(Personaje personaje, JsonObject obj) {
        try {
            setField(personaje, "nombre", obj.get("nombre").getAsString());
            setField(personaje, "clase", obj.get("clase").getAsString());
            setIntField(personaje, "nivel", obj.get("nivel").getAsInt());
            setDoubleField(personaje, "puntosVida", obj.get("puntosVida").getAsDouble());
            setDoubleField(personaje, "puntosVidaMax", obj.get("puntosVidaMaxBase").getAsDouble());
            setDoubleField(personaje, "dañoBase", obj.get("dañoBase").getAsDouble());
            setDoubleField(personaje, "defensa", obj.get("defensa").getAsDouble());
            setIntField(personaje, "exp", obj.get("exp").getAsInt());
            setIntField(personaje, "expRequerida", obj.get("expRequerida").getAsInt());
        } catch (Exception e) {
            throw new JsonParseException("Error restaurando campos base del personaje: " + e.getMessage(), e);
        }
    }

    private void restaurarCamposEspecificos(Personaje personaje, JsonObject obj) {
        try {
            if (personaje instanceof Mago) {
                if (obj.has("mana")) {
                    var fieldMana = Mago.class.getDeclaredField("mana");
                    fieldMana.setAccessible(true);
                    fieldMana.setDouble(personaje, obj.get("mana").getAsDouble());
                }
                if (obj.has("manaMaxBase")) {
                    var fieldManaMax = Mago.class.getDeclaredField("manaMax");
                    fieldManaMax.setAccessible(true);
                    fieldManaMax.setDouble(personaje, obj.get("manaMaxBase").getAsDouble());
                }
            }
            if (personaje instanceof Guerrero && obj.has("cooldown")) {
                var field = Guerrero.class.getDeclaredField("cooldownGolpeColosal");
                field.setAccessible(true);
                field.setInt(personaje, obj.get("cooldown").getAsInt());
            }
            if (personaje instanceof Arquero && obj.has("cooldown")) {
                var field = Arquero.class.getDeclaredField("cooldownDisparoCertero");
                field.setAccessible(true);
                field.setInt(personaje, obj.get("cooldown").getAsInt());
            }
            if (personaje instanceof Luchador && obj.has("cooldown")) {
                var field = Luchador.class.getDeclaredField("cooldownPuñoSupremo");
                field.setAccessible(true);
                field.setInt(personaje, obj.get("cooldown").getAsInt());
            }
        } catch (Exception e) {
            throw new JsonParseException("Error restaurando campos específicos: " + e.getMessage(), e);
        }
    }

    // ── Helpers de reflexión ──────────────────────────────────────────────────

    private void setField(Object target, String fieldName, String value) throws Exception {
        var field = Personaje.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setIntField(Object target, String fieldName, int value) throws Exception {
        var field = Personaje.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(target, value);
    }

    private void setDoubleField(Object target, String fieldName, double value) throws Exception {
        var field = Personaje.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setDouble(target, value);
    }
}
