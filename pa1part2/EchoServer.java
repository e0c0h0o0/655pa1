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
    private String msgType;
    private int numOfProbes;
    private int msgSize;
    private int serverDelay;
    private int indexOfProbe;

    public EchoClientHandler(Socket socket) {
      this.clientSocket = socket;
      indexOfProbe = 0;
    }

    private void error_reply(String line) throws IOException {
      out.println(line);
      clientSocket.close();
    }

    public boolean connectionSetupPhase(String line) throws IOException {
      String[] tokens = line.split(" ");
      if (tokens.length != 5) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      if (tokens[0].equals("s") == false) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      msgType = tokens[1];
      if (!msgType.equals("rtt") && !msgType.equals("tput")) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      try {
        numOfProbes = Integer.parseInt(tokens[2]);
      } catch (NumberFormatException e) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      if (numOfProbes <= 0) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      try {
        msgSize = Integer.parseInt(tokens[3]);
      } catch (NumberFormatException e) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      if (msgSize <= 0) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      try {
        serverDelay = Integer.parseInt(tokens[4]);
      } catch (NumberFormatException e) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      if (serverDelay < 0) {
        error_reply("404 ERROR: Invalid Connection Setup Message");
        return false;
      }
      return true;

    }

    private boolean measurePhase(String line) throws IOException {
      String[] tokens = line.split(" ");
      if (tokens.length != 3) {
        error_reply("404 ERROR1: Invalid Measurement Message");
        return false;
      }

      if (tokens[0].equals("m") == false) {
        error_reply("404 ERROR2: Invalid Measurement Message");
        return false;
      }

      int seqNum;
      try {
        seqNum = Integer.parseInt(tokens[1]);
      } catch (NumberFormatException e) {
        error_reply("404 ERROR3: Invalid Measurement Message");
        return false;
      }
      if (seqNum != indexOfProbe) {
        error_reply("404 ERROR4: Invalid Measurement Message");
        return false;
      }

      if (tokens[2].length() != msgSize) {
        error_reply("404 ERROR5: Invalid Measurement Message");
        return false;
      }

      indexOfProbe++;

      return true;
    }

    public void stopClient() throws IOException {
      in.close();
      out.close();
      clientSocket.close();
    }

    private boolean connectionTerminationPhase(String line) throws IOException {
      if (line.equals("t")) {
        out.println("200 OK: Closing Connection");
        return true;
      } else {
        error_reply("404 ERROR: Invalid Connection Termination Message");
        return false;
      }
    }

    public void run() {
      try {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        if ((inputLine = in.readLine()) != null) {
          // System.out.println(inputLine);
          if (connectionSetupPhase(inputLine) == false) {
            stopClient();
            return;
          }
          out.println("200 OK: Ready");
        }
        while (indexOfProbe < numOfProbes) {
          if ((inputLine = in.readLine()) != null) {
            // System.out.println(inputLine);
            if (measurePhase(inputLine) == false) {
              stopClient();
              return;
            }
            if (serverDelay > 0) {
              System.out.println("Sleeping for " + serverDelay + " ms");
              Thread.sleep(serverDelay);
            }
            out.println(inputLine);
          }
        }

        if ((inputLine = in.readLine()) != null) {
          if (connectionTerminationPhase(inputLine) == false) {
            stopClient();
            return;
          }
        }

        stopClient();
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
