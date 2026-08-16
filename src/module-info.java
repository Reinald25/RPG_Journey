module RPG_Journey {
    requires java.desktop;
    requires com.google.gson;

    opens com.rpg.persistencia to com.google.gson;
    opens com.rpg.logica.personaje to com.google.gson;
    opens com.rpg.logica.enemigo to com.google.gson;
    opens com.rpg.logica.item to com.google.gson;
    opens com.rpg.mision to com.google.gson;
}