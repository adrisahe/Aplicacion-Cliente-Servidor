import java.io.File;
import java.io.Serializable;

public class Fichero implements Serializable {
    private String nombre;
    private File ruta;

    public Fichero(String nombre, File ruta){
        this.nombre = nombre;
        this.ruta = ruta;
    }

    public String getNombre() {
        return nombre;
    }

    public File getRuta() {
        return ruta;
    }
}
