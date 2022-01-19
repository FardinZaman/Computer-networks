import java.io.IOException;
import java.net.Socket;

public class HTTPClient {
    public static void main(String[] args) throws IOException {
        //Socket socket = new Socket("localhost" , HTTPServer.PORT);

        while(true) {
            Socket socket = new Socket("localhost" , HTTPServer.PORT);
            Thread client = new ThreadWorker2(socket);
            client.start();
        }
    }
}
