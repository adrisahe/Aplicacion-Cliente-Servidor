import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class Cliente {
    private JPanel panel1;
    private JTextField textoPuerto;
    private JList listaFicheros;
    private JPanel panelDescargas;
    private JButton conectar;
    private JPanel panelConexion;
    private JLabel labelPuerto;
    private JLabel labelIP;
    private JLabel ipCliente;
    private JLabel labelFicherosDisponible;
    private JLabel labelDescargasCurso;
    private JScrollPane scrollLista;
    private JScrollPane scrollPanelDescargas;
    private JButton botonDescargar;
    private JTextField textFieldIP;

    private Socket socket;
    private DefaultListModel<String> listadoFicheros = new DefaultListModel<>();
    private ObjectOutputStream escribir;
    private ObjectInputStream leer;


    public Cliente() throws IOException {
        botonDescargar.setEnabled(false);
        conectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    crearCliente();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        botonDescargar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    pedirFichero();
                } catch (IOException | InterruptedException | ExecutionException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Cliente");
        frame.setContentPane(new Cliente().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cambiamos el tipo de panel para poder añadir las descargas de manera vertical.
     */
    private void createUIComponents() {
        panelDescargas = new JPanel();
        BoxLayout nuevoLayout = new BoxLayout(panelDescargas,BoxLayout.Y_AXIS);
        panelDescargas.setLayout(nuevoLayout);
    }

    /**
     * Creamos el cliente que se conectará al servidor, mostramos la ip del cliente,
     * leemos la lista de los nombres que nos envia el HiloServidor y los añadimos a la listaficheros del cliente.
     * @throws IOException
     */
    private void crearCliente() throws IOException {
        int numeroPuerto = Integer.parseInt(textoPuerto.getText());
        String numeroIP = textFieldIP.getText();
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(numeroIP, numeroPuerto);
                    escribir = new ObjectOutputStream(socket.getOutputStream());
                    listaFicheros.setModel(listadoFicheros);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                botonDescargar.setEnabled(true);
                                leer = new ObjectInputStream(socket.getInputStream());
                                boolean control = true;
                                while (control) {
                                    control = leer.readBoolean();
                                    if (!control) {
                                        break;
                                    }
                                    listadoFicheros.addElement((String) leer.readObject());
                                    conectar.setEnabled(false);
                                }
                            } catch (IOException | ClassNotFoundException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    });
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        hilo.start();
    }

    /**
     * Pedimos el fichero al servidor y añadimos un panel de descarga al panel de descargas
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void pedirFichero() throws IOException, ExecutionException, InterruptedException {
        int opcion = listaFicheros.getSelectedIndex();
        String nombreDelFichero = listadoFicheros.get(opcion);
        escribir.writeInt(opcion);
        escribir.flush();

        File ficheroDestino = new File((String) listaFicheros.getSelectedValue());

        DescargasIndividuales descarga = new DescargasIndividuales(ficheroDestino, socket, leer, nombreDelFichero);
        panelDescargas.add(descarga.getPanel1());
        panelDescargas.revalidate();
        scrollPanelDescargas.revalidate();
        botonDescargar.setEnabled(false);

        descarga.hilo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("state") && evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    System.out.println(SwingUtilities.isEventDispatchThread());
                    botonDescargar.setEnabled(true);
                }
            }
        });
    }
}
