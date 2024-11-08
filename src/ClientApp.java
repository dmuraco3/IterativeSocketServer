import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);
        String ipAddr;
        int port, choice, numThreads;
        String cmd = "";


        System.out.println("Enter the IP address: ");
        ipAddr = scnr.nextLine();

        System.out.println("Enter the port number: ");
        port = scnr.nextInt();

        System.out.println("1. Date and Time - the date and time on the server");
        System.out.println("2. Uptime - how long the server has been running since last boot-up");
        System.out.println("3. Memory Use - the current memory usage on the server");
        System.out.println("4. Netstat - lists network connections on the server");
        System.out.println("5. Current Users - list of users currently connected to the server");
        System.out.println("6. Running Processes - list of programs currently running on the server");
        System.out.println("7. Exit");

        System.out.println("Select option to request from server: ");
        choice = scnr.nextInt();

        switch (choice) {
            case 1:
                cmd = "datetime";
                break;
            case 2:
                cmd = "uptime";
                break;
            case 3:
                cmd = "memuse";
                break;
            case 4:
                cmd = "netstat";
                break;
            case 5:
                cmd = "curusr";
                break;
            case 6:
                cmd = "proc";
                break;
            default:
                break;
        }

        System.out.println("Enter the number of request you would like to make (1, 5, 10, 15, 20 or 25): ");
        numThreads = scnr.nextInt();

        RequestThread[] threadPool = new RequestThread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threadPool[i] = new RequestThread(cmd, ipAddr, port);
            threadPool[i].run();
        }

        long totalElapsed = 0;
        long averageElapsed;

        for (RequestThread r : threadPool) {
            try {
                r.thread.join();
                totalElapsed += r.getElapsedTime();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        averageElapsed = totalElapsed / threadPool.length;

        System.out.printf("Total time for %d threads: %d ms \n", numThreads, totalElapsed);
        System.out.printf("Average time for %d threads: %d ms \n", numThreads, averageElapsed);
    }
}

class RequestThread implements Runnable {
    Thread thread;
    private final String cmd;
    private final String ipAddr;
    private final int port;
    private long elapsedTime;

    RequestThread(String cmd, String ipAddr, int port) {
        thread = new Thread(this, "Client Request Thread");
        this.cmd = cmd;
        this.ipAddr = ipAddr;
        this.port = port;
    }

    public void run() {
        try {
            System.out.println("Running thread: " + this.thread.getId());
            Socket socket = new Socket(this.ipAddr, this.port);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // START timing block
            long startTime = System.currentTimeMillis();
            out.writeUTF(cmd);

            String response = in.readUTF();
            System.out.printf("thread #%d got the following response: \n\t%s\n",this.thread.getId(),response);
            long endTime = System.currentTimeMillis();
            // END timing block

            socket.close();

            this.elapsedTime = endTime - startTime;
            System.out.printf("Turn-around time for socket on thread #%d: %dms \n",this.thread.getId(), this.elapsedTime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }
}
