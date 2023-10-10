/*
 * Choose proper lib to finish it
 * 
 */
import java.io.*;
import java.net.*;
public class EchoClient(){
    private Socket clientSocket;//Connect to sever
    private PrintWriter out;//Send data to server
    private BufferedReader in;//Receive from server
    private BufferedReader stdIn;//Read input from console
    /*
     * Constructor
     * This is where the client establishes a connection to the server 
     * using the provided host and port.
     * It also initializes the input and output streams.
     */
    public EchoClient(String host,int port)throws IOException{
        clientSocket = new Socket(host, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }
    /*
     * This method reads a line from the standard input (console), 
     * sends it to the server, 
     * then reads and prints the response from the server.
     */
    public void communicate() throws IOException {
        String userInput;
        if ((userInput = stdIn.readLine()) != null) {
          out.println(userInput);
          System.out.println(in.readLine());
        }
      }
    /*
    * Close all the opened sources
    */
    public void close() throws IOException {
        in.close();
        out.close();
        stdIn.close();
        clientSocket.close();
      }
    public static void main(String[] args) throws IOException {
    try {
      EchoClient client = new EchoClient(args[0], Integer.parseInt(args[1]));
      client.communicate();
      client.close();
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: java EchoClient <host> <port>");
    } catch (NumberFormatException e) {
      System.out.println("Usage: java EchoClient <host> <port>");
    } catch (ConnectException e) {
      System.out.println("Connection refused. Is the server running?");
    } catch (UnknownHostException e) {
      System.out.println("Unknown host. Is the server running?");
    }
  }
}
