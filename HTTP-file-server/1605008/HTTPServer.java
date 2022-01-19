import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HTTPServer {
    static final int PORT = 6788;

    public static String readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return String.valueOf(fileData);
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        String root = "root";
        File rootFile = new File(root);

        if(!rootFile.exists())
        {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Give root : ");
            String givenRoot = scanner.nextLine();
            root = givenRoot;
        }

        while(true)
        {
            Socket socket = serverSocket.accept();

            Thread thread = new ThreadWorker(socket , root);
            thread.start();

        }

    }
}