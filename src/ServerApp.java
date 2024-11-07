import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) {
        int port = 3333;  // specify the port you want the server to listen on

        Server server = new Server(port);
        try {
            server.startServer();
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class Server {
    private int port;
    private ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        this.serverSocket = new ServerSocket(this.port);

        String header = "Now listening for connections on " + this.serverSocket.getInetAddress().getHostAddress() + ":" + this.serverSocket.getLocalPort();
        System.out.println(header);
        System.out.println("_".repeat(header.length()));
        System.out.println();

        while (true) {
            Socket socket = this.serverSocket.accept();
            System.out.println("Accepted a socket connection from " + socket.getRemoteSocketAddress());

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String line = in.readUTF();

            System.out.println("Received command: " + line);
            String cmd_output = null;
            try {
                cmd_output = runClientCommand(line);
                reply(socket, cmd_output);
            } catch (Exception e) {
                reply(socket, e.getMessage());
            }

            socket.close();
        }
    }

    private void reply(Socket socket, String reply_out) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(reply_out);
    }

    private String runClientCommand(String cmd) throws Exception {
        switch (cmd) {
            case "datetime":
                return dateTime();
            case "uptime":
                return upTime();
            case "memuse":
                return memoryUsage();
            case "netstat":
                return netStat();
            case "curusr":
                return currentUsers();
            case "proc":
                return runningProcesses();
            default:
                throw new Exception("Command not recognized");
        }
    }

    private String dateTime() {
        return execCommand("date");
    }

    private String upTime() {
        return execCommand("uptime -p");
    }

    private String memoryUsage() {
        return execCommand("free -h | awk '/^Mem:/ {print \"Total: \"$2\", Used: \"$3\", Free: \"$4}'");
    }

    private String netStat() {
        return execCommand("netstat -an | grep ESTABLISHED");
    }

    private String currentUsers() {
        return execCommand("who");
    }

    private String runningProcesses() {
        return execCommand("ps -al");
    }

    private String execCommand(String cmd) {
        StringBuilder output = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = stdOutput.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString().trim();
    }
}