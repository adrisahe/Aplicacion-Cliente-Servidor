import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class DescargasIndividuales{
    public JPanel getPanel1() {
        return panel1;
    }

    private JPanel panel1;
    private JButton botonPausar;
    private JButton botonEliminar;
    private JProgressBar progreso;
    private JPanel panelBotones;
    private JLabel labelNombreDescarga;

    private File ficheroDestino;
    private Socket socket;
    private ObjectInputStream leer;
    public HiloCliente hilo;

    public DescargasIndividuales(File ficheroDestino, Socket socket, ObjectInputStream leer, String nombreDelFichero) {
        this.ficheroDestino = ficheroDestino;
        this.socket = socket;
        this.leer = leer;
        labelNombreDescarga.setText(nombreDelFichero);
        hilo = new HiloCliente(ficheroDestino, leer, socket);
        hilo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                actualizarBarra(evt);
            }
        });
        hilo.execute();
        botonEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //eliminarDescarga();
            }
        });
        botonPausar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausar();
            }
        });
    }

    /**
     * Actualizamos la barra del panel.
     * @param evt
     */
    private void actualizarBarra(PropertyChangeEvent evt){
        if(evt.getPropertyName().equals("progress")){
            progreso.setValue((int) evt.getNewValue());
        }
    }

    private void eliminarDescarga() {
        hilo.cancelar = true;
        panel1.setVisible(false);
        int opcion = JOptionPane.showConfirmDialog(panel1, "¿Desea eliminar la descarga?", "Eliminar descarga", JOptionPane.YES_NO_OPTION);
        if(opcion == 0){
            Thread hiloEliminar = new Thread(new Runnable() {
                @Override
                public void run() {
                    ficheroDestino.delete();
                }
            });
            hiloEliminar.start();
        }
    }

    private void pausar() {
        if(botonPausar.getText().equals("Pausar")){
            botonPausar.setText("Reanudar");
        }
        else {
            botonPausar.setText("Pausar");
        }
        Thread hiloPausa = new Thread(new Runnable() {
            @Override
            public void run() {
                if(botonPausar.getText().equals("Reanudar")){
                    hilo.detener = true;
                }
                else {
                    synchronized (hilo){
                        hilo.detener = false;
                        hilo.notify();
                    }
                }
            }
        });
        hiloPausa.start();
    }

}
