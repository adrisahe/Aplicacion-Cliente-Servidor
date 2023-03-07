import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class HiloServidor extends SwingWorker<Void, Integer> {
    private Socket socket;
    private ArrayList<Fichero> ficherosAlmacenados;
    private ObjectOutputStream escribir;
    private ObjectInputStream leer;

    public HiloServidor(Socket socket, ArrayList<Fichero> ficherosAlmacenados) {
        this.socket = socket;
        this.ficherosAlmacenados = ficherosAlmacenados;
    }

    @Override
    protected Void doInBackground() throws Exception {
        enviarLista();
        recibirOpcion();
        return null;
    }

    /**
     * Enviamos al cliente el nombre de los ficheros que se encuentran en la listaFicheros.
     * @throws IOException
     */
    private void enviarLista() throws IOException {
        leer = new ObjectInputStream(socket.getInputStream());
        escribir = new ObjectOutputStream(socket.getOutputStream());
        for (int i = 0; i < ficherosAlmacenados.size(); i++){
            escribir.writeBoolean(true);
            escribir.writeObject(ficherosAlmacenados.get(i).getNombre());
        }
        escribir.writeBoolean(false);
        escribir.flush();
    }

    /**
     * Recibimos la opcion que nos envia el usuario y le enviamos el fichero correspondiente, cuando finalice la descarga podremos descargar
     * otro fichero, nunca antes de que acabe la descarga.
     * @throws IOException
     */
    private void recibirOpcion() throws IOException {
        do {
            int opcion = leer.readInt();
            File fichero = new File(String.valueOf(ficherosAlmacenados.get(opcion).getRuta()));
            escribir.writeLong(ficherosAlmacenados.get(opcion).getRuta().length());

            FileInputStream fileInputStream = new FileInputStream(fichero);
            byte[] buffer = new byte[1024];
            int bytesLeidos = 0;
            while ((bytesLeidos = fileInputStream.read(buffer)) > 0) {
                escribir.write(buffer, 0, bytesLeidos);
                escribir.flush();
            }
            fileInputStream.close();
        }
        while (!socket.isClosed());
    }

}
