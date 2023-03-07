import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Servidor {
    private JPanel panel1;
    private JTextField textoPuerto;
    private JButton iniciar;
    private JList listaClientes;
    private JList listaFicheros;
    private JButton anadirFichero;
    private JPanel panelIniciar;
    private JLabel labelPuerto;
    private JPanel panelLabels;
    private JLabel labelClientesConectados;
    private JLabel labelFicherosDisponibles;
    private JPanel panelScrollListas;
    private JScrollPane scrollClientes;
    private JScrollPane scrollFicheros;

    private ServerSocket serverSocket;
    private Socket socket;
    private DefaultListModel<String> listadoClientes = new DefaultListModel<>();
    private DefaultListModel<String> listadoFicheros = new DefaultListModel<>();
    private ArrayList<Fichero> ficherosAlmacenados= new ArrayList<>();

    public Servidor() {
        iniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    crearHilos();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        anadirFichero.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anadirFichero();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Servidor");
        frame.setContentPane(new Servidor().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Crea un servidor y crea un hilo por cada cliente que se conecte, así cada cliente sera escuchado por un hilo distino, en caso
     * de no haber sido introducido el puerto saldra un mensaje de error.
     * @throws IOException
     */
    private void crearHilos() throws IOException {
        int numeroPuerto = Integer.parseInt(textoPuerto.getText());
        Thread arrancar = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(numeroPuerto);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                while (!serverSocket.isClosed()) {
                    try {
                        socket = serverSocket.accept();
                        HiloServidor hiloServidor = new HiloServidor(socket, ficherosAlmacenados);
                        hiloServidor.execute();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    recibirIP();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                        });
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        arrancar.start();
    }

    /**
     * Añadimos a la listaClientes los clientes que se conecten al servidor, añadiendo su información de ip y la hora exacta a la que se conecto.
     * @throws IOException
     */
    private void recibirIP() throws IOException {
        listaClientes.setModel(listadoClientes);
        listadoClientes.addElement("Cliente Conectado: " + socket.getInetAddress().getHostAddress() + " Conexion iniciada a: " + LocalDateTime.now());
    }

    /**
     * Añadimos a la listaFicheros los ficheros que el administrador seleccione.
     */
    private void anadirFichero(){
        listaFicheros.setModel(listadoFicheros);
        JFileChooser fichero = new JFileChooser();
        int seleccion = fichero.showOpenDialog(null);
        if(seleccion == JFileChooser.APPROVE_OPTION){
            String nombreFichero = JOptionPane.showInputDialog(null, "Introduce el nombre del fichero", "Nombre fichero", JOptionPane.QUESTION_MESSAGE);
            if (nombreFichero != null) {
                Fichero fichero1 = new Fichero(nombreFichero, fichero.getSelectedFile());
                listadoFicheros.addElement(fichero1.getNombre());
                ficherosAlmacenados.add(fichero1);
            }
        }
    }

}
