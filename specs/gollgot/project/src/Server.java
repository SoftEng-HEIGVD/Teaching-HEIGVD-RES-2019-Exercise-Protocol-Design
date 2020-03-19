import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {

    private int port;

    public Server(int port){
        this.port = port;
    }

    /**
     * Initiates the process. Server creates a socket and binds it to the chosen port. Then waits for
     * clients inside an infinite loop. When client arrives, server will read it's input line.
     */
    public void serveClients(){
        System.out.println("Starting the Receptionist Worker on a new thread ...");
        new Thread(new ReceptionistWorker()).start();
    }


    /**
     * This inner class implements the behaviour of the "Receptionist", whose responsibility is to
     * listen for incoming connection request. As soon as a new client has arrived, the receptionist
     * delegates the processing to "servant" who will execute on its own thread.
     */
    private class ReceptionistWorker implements Runnable{

        @Override
        public void run() {
            ServerSocket serverSocket;

            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occurred on the ServerSocket initialisation : " + e.getMessage());
                return;
            }

            while(true){
                System.out.println("Waiting for a new client on port " + port);
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("A new client has arrived. Starting a new thread and delegating work to a new servant...");
                    new Thread(new ServantWorker(clientSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error occurred on the Socket Client initialisation (server accept) : " + e.getMessage());
                }
            }

        }

    }

    /**
     * This inner class implements the behavior of the "servants", whose
	 * responsibility is to take care of clients once they have connected. This
	 * is where we implement the application protocol logic, more details about the protocol
     * into the protocol.md file
     */
    private class ServantWorker implements Runnable {

        Socket clientSocket;
        BufferedReader in = null;
        PrintWriter out = null;

        ServantWorker(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                this.out = new PrintWriter(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occurred on in / out buffer creation : " + e.getMessage());
            }
        }

        /**
         * Calculate a basic math calculation that respect this format : <nb1> <space> <operator> <space> <nb2>
         * And the operator can be +, -, *, /
         * @param calculation The String line contains the calculation
         * @return "invalid" if an error occurred, the result as a String otherwise
         */
        private String calculate(String calculation){
            int nb1 = 0;
            int nb2 = 0;
            int answer = 0;
            String operation;
            String[] parts = calculation.split(" ");
            boolean error = false;

            // Calculate only NB Operation NB => so 3 parts
            if(parts.length != 3) {
                error = true;
            } else{
                try {
                    nb1 = Integer.parseInt(parts[0]);
                    nb2 = Integer.parseInt(parts[2]);
                } catch(NumberFormatException e){
                    System.out.println("Error occurred on calculation parsing : " + e.getMessage());
                    error = true;
                }

                if(!error) {
                    operation = parts[1];
                    switch (operation) {
                        case "+":
                            answer = nb1 + nb2;
                            break;
                        case "-":
                            answer = nb1 - nb2;
                            break;
                        case "*":
                            answer = nb1 * nb2;
                            break;
                        case "/":
                            try{
                                answer = nb1 / nb2;
                            }catch(ArithmeticException e){
                                System.out.println("Error occurred, arithmetic exception : " + e.getMessage());
                                error = true;
                            }
                            break;
                        default:
                            error = true;
                            break;
                    }
                }

            }

            return error ? "invalid" : String.valueOf(answer);
        }



        @Override
        public void run() {
            String line;
            boolean shouldRun = true;

            out.println("Welcome to the multi-threaded server.");
            out.flush();

            try {
                System.out.println("Reading until client sends BYE or close connection ...");
                while((shouldRun && (line = in.readLine()) != null)){
                    // Client ask for calculation
                    if(line.equalsIgnoreCase("hello")){
                        out.println("Ready");
                        out.flush();
                        // Loop through all calculation the client want to do
                        while(shouldRun && (line = in.readLine()) != null){
                            // Client ask to quit
                            if(line.equalsIgnoreCase("bye")){
                                shouldRun = false;
                            }
                            // Otherwise client may ask for a calculation
                            else{
                                String result = calculate(line);
                                if(!result.equals("invalid")){
                                    out.println(result);
                                }else{
                                    out.println("Error");
                                }
                                out.flush();
                            }
                        }
                    }

                    // Client ask to quit
                    if(line.equalsIgnoreCase("bye")){
                        out.println("Bye");
                        out.flush();
                        break;
                    }

                    out.println("Error");
                    out.flush();
                }

                System.out.println("Cleaning up resources...");
                clientSocket.close();
                in.close();
                out.close();

            } catch (IOException e) {
                // Correctly close the BufferedReader
                if(in != null){
                    try {
                        in.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                // Correctly close the PrintWriter
                if(out != null){
                    out.close();
                }

                // Correctly close the Socket
                if(clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                e.printStackTrace();
                System.out.println("Error occurred on read line : " + e.getMessage());
            }
        }
    }

}
