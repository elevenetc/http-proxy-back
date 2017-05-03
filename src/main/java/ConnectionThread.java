import java.net.Socket;

/**
 * Created by eugene.levenetc on 19/04/2017.
 */
public class ConnectionThread extends Thread {

    private Socket socket;

    public ConnectionThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
