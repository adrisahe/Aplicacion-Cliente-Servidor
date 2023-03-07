import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class HiloCliente extends SwingWorker {

    private File ficheroDestino;
    private ObjectInputStream leer;
    private Socket socket;
    public boolean detener;
    public boolean cancelar;
    public boolean permitir;


    public HiloCliente(File ficheroDestino, ObjectInputStream leer, Socket socket) {
        this.ficheroDestino = ficheroDestino;
        this.leer = leer;
        this.socket = socket;
        this.detener = false;
        this.cancelar = false;
    }

    @Override
    protected Object doInBackground() throws Exception {
        recibirFichero();
        return null;
    }
    private void recibirFichero() throws IOException, InterruptedException {
            FileOutputStream fileOutputStream = new FileOutputStream(ficheroDestino);
            long tamano = leer.readLong();
            byte[] buffer = new byte[1024];
            int bytesLeidos;
            long totalLeido = 0;
            while (totalLeido < tamano && (bytesLeidos = leer.read(buffer)) > 0 && !cancelar) {
                while (detener) {
                    synchronized (this) {
                        this.wait();
                    }
                }
                fileOutputStream.write(buffer, 0, bytesLeidos);
                totalLeido += bytesLeidos;
                long porcentaje = totalLeido * 100 / tamano;
                setProgress((int) porcentaje);
                System.out.println(totalLeido);
            }
            fileOutputStream.close();
    }
}
