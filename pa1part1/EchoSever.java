import java.io.*;
import java.net.*;

public class EchoServer {
  private ServerSocket serverSocket;

  public void start(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    while (true)
      new EchoClientHandler(serverSocket.accept()).start();
  }

  public void stop() throws IOException {
    serverSocket.close();
  }

  private static class EchoClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public EchoClientHandler(Socket socket) {
      this.clientSocket = socket;
    }

    public void run() {
      try {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          out.println(inputLine);
        }

        in.close();
        out.close();
        clientSocket.close();
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  public static void main(String[] args) throws IOException {
    try {
      EchoServer server = new EchoServer();
      server.start(Integer.parseInt(args[0]));
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: java EchoServer <port>");
    } catch (NumberFormatException e) {
      System.out.println("Usage: java EchoServer <port>");
    } catch (BindException e) {
      System.out.println("Port already in use. Is the server already running?");
    } catch (IOException e) {
      System.out.println("Error starting server: " + e);
    }
  }
}