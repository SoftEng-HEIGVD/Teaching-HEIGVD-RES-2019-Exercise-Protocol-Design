import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This servers has to goal to calculate some operations
 * @author Potet Bastien
 */
public class CalculateServer {

    static final Logger LOG = Logger.getLogger(CalculateServer.class.getName());

    private final int LISTEN_PORT = 49500;
    private final static int BUFFER_SIZE = 1024;
    private final String GOOD_BYE_MSG = "GOOD BYE";
    private char[] buffer = new char[BUFFER_SIZE];

    /**
     * This method does the entire processing.
     */
    public void start() {
        LOG.info("Starting CalculateServer...");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {

            serverSocket = new ServerSocket(LISTEN_PORT);
            logServerSocketAddress(serverSocket);

            while (true) {
                clientSocket = serverSocket.accept();

                logSocketAddress(clientSocket);

                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream());

                // Get the message "Hello"

                int sizeMsg = reader.read(buffer, 0, BUFFER_SIZE);

                LOG.info(new String(buffer,0,sizeMsg));

                writer.println("Hello I'm a Bastien server");
                writer.flush();

                // Get the operation to do
                // e.g. : 2 5 + 8 + => 15

                sizeMsg = reader.read(buffer,0,BUFFER_SIZE);

                String calculateAsked = new String(buffer,0,sizeMsg);
                calculateAsked = calculateAsked.replaceAll("([\\n\\r])", "");

                while (!calculateAsked.equals(GOOD_BYE_MSG)) {

                    LOG.info(String.format("Calcul to do : %s", calculateAsked));

                    String[] calculSplited = calculateAsked.split(" ");

                    Queue<Integer> numbers = new LinkedList<>();

                    for (String current : calculSplited) {

                        if (current.matches("^([+*/\\-])$")) {
                            Integer fstOperand = numbers.poll();
                            Integer sndOperand = numbers.poll();
                            int tmp = 0;
                            switch (current.charAt(0)) {
                                case '+':
                                    tmp = fstOperand + sndOperand;
                                    break;
                                case '-':
                                    tmp = fstOperand - sndOperand;
                                    break;
                                case '*':
                                    tmp = fstOperand * sndOperand;
                                    break;
                                case '/':
                                    tmp = fstOperand / sndOperand;
                                    break;
                                default:
                                    break;
                            }
                            numbers.add(tmp);
                        } else {
                            numbers.add(Integer.valueOf(current));
                        }
                    }

                    writer.println(String.format("Result : %d", numbers.poll()));
                    writer.flush();

                    sizeMsg = reader.read(buffer,0,BUFFER_SIZE);

                    calculateAsked = new String(buffer,0,sizeMsg);
                    calculateAsked = calculateAsked.replaceAll("(\\n|\\r)", "");
                }

                writer.close();
                reader.close();
                clientSocket.close();

                LOG.info("The client finished the connection");

            }

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
        } finally {
            LOG.log(Level.INFO, "We are done. Cleaning up resources, closing streams and sockets...");
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(CalculateServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            writer.close();
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(CalculateServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(CalculateServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * A utility method to print server socket information
     *
     * @param serverSocket the socket that we want to log
     */
    private void logServerSocketAddress(ServerSocket serverSocket) {
        LOG.log(Level.INFO, "       Local IP address: {0}", new Object[]{serverSocket.getLocalSocketAddress()});
        LOG.log(Level.INFO, "             Local port: {0}", new Object[]{Integer.toString(serverSocket.getLocalPort())});
        LOG.log(Level.INFO, "               is bound: {0}", new Object[]{serverSocket.isBound()});
    }

    /**
     * A utility method to print socket information
     *
     * @param clientSocket the socket that we want to log
     */
    private void logSocketAddress(Socket clientSocket) {
        LOG.log(Level.INFO, "       Local IP address: {0}", new Object[]{clientSocket.getLocalAddress()});
        LOG.log(Level.INFO, "             Local port: {0}", new Object[]{Integer.toString(clientSocket.getLocalPort())});
        LOG.log(Level.INFO, "  Remote Socket address: {0}", new Object[]{clientSocket.getRemoteSocketAddress()});
        LOG.log(Level.INFO, "            Remote port: {0}", new Object[]{Integer.toString(clientSocket.getPort())});
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");

        CalculateServer server = new CalculateServer();
        server.start();
    }

}