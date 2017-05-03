import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by eugene.levenetc on 19/04/2017.
 */
public class SocketThread extends Thread {

    volatile boolean started = true;
    volatile boolean connected = true;
    private String host;
    int port;
    CallbackApi api;

    public SocketThread(String host, int port, CallbackApi callbackApis) {
        this.host = host;
        this.port = port;
        this.api = callbackApis;
    }

    @Override
    public void run() {


        while (started) {

            try {

                System.out.println("connected");

                connected = true;
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                final OutThread outThread = new OutThread(out);
                final InThread inThread = new InThread(in, outThread, api);
                outThread.start();
                outThread.start();
                inThread.start();

                Utils.sleep();

                Utils.close(socket, in, out);

            } catch (IOException e) {
                System.out.println("connection error:" + e.getMessage());
                Utils.sleep(1000);
                //e.printStackTrace();
            } finally {
                connected = false;
                System.out.println("disconnected");
            }
        }


    }

    static class InThread extends Thread {

        private BufferedReader reader;
        private OutThread out;
        private CallbackApi api;
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        public InThread(BufferedReader reader, OutThread out, CallbackApi api) {

            this.reader = reader;
            this.out = out;
            this.api = api;
        }

        public void push(String message) {
            messages.offer(message);
        }

        @Override
        public void run() {
            String line;
            try {
                System.out.println("waiting for message");
                while ((line = reader.readLine()) != null) {
                    final Command command = JsonEncoder.toObject(line, Command.class);
                    System.out.println("message reveived:" + command);
                    System.out.println("making request...");
                    System.out.println("received result");
                    makeRequest(command);
                    out.push(JsonEncoder.toString(command) + "\n");
                    System.out.println("waiting for message");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void makeRequest(Command command) {
            Object response = api.call();
            command.result = String.valueOf(response);
        }
    }


    static class OutThread extends Thread {

        private boolean connected = true;
        private BufferedWriter writer;
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        public OutThread(BufferedWriter writer) {
            this.writer = writer;
        }

        public void push(String message) {
            messages.offer(message);
        }

        @Override
        public void run() {
            while (connected) {
                try {
                    final String message = messages.take();
                    System.out.println("writing out result");
                    writer.write(message);
                    writer.flush();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Command {
        int data;
        String result;
        Map<String, String> params;

        @Override
        public String toString() {
            return "Command{" +
                    "data=" + data +
                    ", result='" + result + '\'' +
                    ", params=" + params +
                    '}';
        }
    }
}
