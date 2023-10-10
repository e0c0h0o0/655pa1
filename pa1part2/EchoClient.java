import java.io.*;
import java.net.*;

public class EchoClient {
  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;
  // private BufferedReader stdIn;
  private String msgType;
  private int numOfProbes;
  private int msgSize;
  private int serverDelay;
  private int indexOfProbe;
  private double totalTime;

  public EchoClient(
    String host, int port,
    String msgType, int numOfProbes, int msgSize, int serverDelay
  ) throws IOException {
    clientSocket = new Socket(host, port);
    out = new PrintWriter(clientSocket.getOutputStream(), true);
    in = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
    // stdIn = new BufferedReader(new InputStreamReader(System.in));
    this.msgType = msgType;
    this.numOfProbes = numOfProbes;
    this.msgSize = msgSize;
    this.serverDelay = serverDelay;
    this.indexOfProbe = 0;
    this.totalTime = 0;
  }

  public void csp() throws IOException {
    String line = String.format("s %s %d %d %d", msgType, numOfProbes, msgSize, serverDelay);
    out.println(line);
    String reply;
    if ((reply = in.readLine()) != null) {
      if (!reply.equals("200 OK: Ready")) {
        System.out.println(reply);
        System.exit(1);
      }
    } else {
      System.out.println("No reply from server");
      System.exit(1);
    }
  }

  String msgPayload() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < msgSize; i++) {
      sb.append("a");
    }
    return sb.toString();
  }

  public void mp() throws IOException {
    String line = String.format("m %d %s", indexOfProbe, msgPayload());
    out.println(line);
    String reply;
    long startTime = System.nanoTime();
    if ((reply = in.readLine()) != null) {
      if (!reply.equals(line)) {
        System.out.println(reply);
        System.exit(1);
      }
    } else {
      System.out.println("No reply from server");
      System.exit(1);
    }
    long endTime = System.nanoTime();
    totalTime += (endTime - startTime) / 1000000000.0;
    indexOfProbe++;
  }

  public void ctp() throws IOException {
    String line = "t";
    out.println(line);
    String reply;
    if ((reply = in.readLine()) != null) {
      if (!reply.equals("200 OK: Closing Connection")) {
        System.out.println(reply);
        System.exit(1);
      }
    } else {
      System.out.println("No reply from server");
      System.exit(1);
    }
  }

  public void close() throws IOException {
    in.close();
    out.close();
    clientSocket.close();
  }

  public void summary() {
    if (msgType.equals("rtt")) {
      System.out.println("RTT: " + totalTime / numOfProbes * 1000.0 + " ms");
    } else if (msgType.equals("tput")) {
      System.out.println("Throughput: " + (msgSize * numOfProbes) / totalTime + " Bps");
    }
  }

  public void run() throws IOException {
    csp();
    for (int i = 0; i < numOfProbes; i++) {
      mp();
    }
    ctp();
    summary();
  }

  public static void main(String[] args) throws IOException {
    try {
      String msgType = args[2];
      int msgSize = Integer.parseInt(args[3]);
      int numOfProbes = 15;
      if (args.length > 4) {
        numOfProbes = Integer.parseInt(args[4]);
      }
      int serverDelay = 0;
      if (args.length > 5) {
        serverDelay = Integer.parseInt(args[5]);
      }
      EchoClient client = new EchoClient(args[0], Integer.parseInt(args[1]), msgType, numOfProbes, msgSize, serverDelay);
      client.run();
      client.close();
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: java EchoClient <host> <port> <msgType(rtt or tput)> <msgSize> [<numOfProbes> [<serverDelay ms>]]");
    } catch (NumberFormatException e) {
      System.out.println("Usage: java EchoClient <host> <port> <msgType(rtt or tput)> <msgSize> [<numOfProbes> [<serverDelay ms>]]");
    } catch (ConnectException e) {
      System.out.println("Connection refused. Is the server running?");
    } catch (UnknownHostException e) {
      System.out.println("Unknown host. Is the server running?");
    }
  }
}
