import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ThreadWorker2 extends Thread {

    Socket socket;

    String input = null;
    File file = null;
    Scanner scanner = new Scanner(System.in);
    BufferedReader in = null;
    PrintWriter out = null;
    DataOutputStream dos = null;

    static final int BUFFER_SIZE = 4096;

    public ThreadWorker2(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        input = scanner.nextLine();
    }

    @Override
    public void run() {
        try {
            //System.out.println("3");
            //out = new PrintWriter(socket.getOutputStream());
            //dos = new DataOutputStream(socket.getOutputStream());
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //input = scanner.nextLine();
            file = new File(input);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                out.println("UPLOAD " + file.getName());
                out.println(file.length());
                out.println();
                out.flush();

                //System.out.println("3");
                int len;
                byte[] buffer = new byte[BUFFER_SIZE];
                System.out.println("Checkpoint 2");
                while ((len = fis.read(buffer)) > 0) {
                    System.out.println("Checkpoint 2");
                    dos.write(buffer, 0, len);
                }
                dos.flush();
                fis.close();
            } else {
                System.out.println("File not found");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
