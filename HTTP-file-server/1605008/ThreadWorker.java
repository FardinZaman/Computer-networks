import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;

public class ThreadWorker extends Thread {
    Socket socket;
    String root;
    //String root = "root";

    private static final int BUFFER_SIZE = 4000;

    File file = new File("log.txt");
    FileWriter fileWriter;

    public ThreadWorker(Socket socket , String root) throws IOException {
        this.socket = socket;
        this.root = root;
        fileWriter = new FileWriter(file, true);
    }

    void response(String msg , String type , String content){

        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

            printWriter.write(msg + " \r\n");
            fileWriter.write(msg + " \r\n");

            printWriter.write("Server: Java HTTP Server: 1.0\r\n");
            fileWriter.write("Server: Java HTTP Server: 1.0\r\n");
            printWriter.write("Date: " + new Date() + "\r\n");
            fileWriter.write("Date: " + new Date() + "\r\n");

            if (content != null) {
                printWriter.write("Content-Type: " + type + "\r\n");
                fileWriter.write("Content-Type: " + type + "\r\n");
                printWriter.write("Content-Length: " + content.length() + "\r\n");

                if(msg.equals("HTTP/1.1 404 NOT FOUND"))
                    fileWriter.write("Content-Length: 0" + "\r\n");
                else
                    fileWriter.write("Content-Length: " + content.length() + "\r\n");
            } else {
                printWriter.write("Content-Length: 0" + "\r\n");
                fileWriter.write("Content-Length: 0\" + \"\\r\\n");
            }

            printWriter.write("\r\n");

            if (content != null) {
                printWriter.write(content);
            }

            printWriter.flush();

            printWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void downloader(File file){
        long size = file.length();
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

            FileInputStream fileInputStream = new FileInputStream(file);

            printWriter.write("HTTP/1.1 200 OK\r\n");
            fileWriter.write("HTTP/1.1 200 OK\r\n");
            printWriter.write("Server: Java HTTP Server: 1.0\r\n");
            fileWriter.write("Server: Java HTTP Server: 1.0\r\n");
            printWriter.write("Date: " + new Date() + "\r\n");
            fileWriter.write("Date: " + new Date() + "\r\n");

            String realType = Files.probeContentType(file.toPath());

            printWriter.write("Content-Type: " + realType + "\r\n");
            fileWriter.write("Content-Type: " + realType + "\r\n");
            printWriter.write("Content-Length: " + size + "\r\n");
            fileWriter.write("Content-Length: " + size + "\r\n");

            printWriter.write("Content-Transfer-Encoding: binary\r\n");
            fileWriter.write("Content-Transfer-Encoding: binary\r\n");
            printWriter.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n");
            fileWriter.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n");
            printWriter.write("\r\n");
            fileWriter.write("\r\n");
            printWriter.flush();

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, length);
            }
            dataOutputStream.flush();
            printWriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        BufferedReader bufferedReader;
        //PrintWriter printWriter = null;
        String input = null;

        try {
            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(in);
            //printWriter = new PrintWriter(socket.getOutputStream());
            input = bufferedReader.readLine();
            //fileWriter.write(input + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Request : " + input);

        //printWriter.write("HTTP/1.1 200 OK\r\n");
        //printWriter.write("Server: Java HTTP Server: 1.0\r\n");
        //printWriter.write("Date: " + new Date() + "\r\n");
        //printWriter.write("Content-Type: text/html\r\n");

        if(input == null)
        {
            try {
                System.out.println("File not found");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            fileWriter.write(input + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] split = input.split(" ");

        String method = split[0];
        String path = split[1];

        path = path.replace("%20" , " ");

        if(input.length() > 0)
        {
            if(method.startsWith("GET"))
            {
                String content = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                        "\t\t<link rel=\"icon\" href=\"data:,\"></head><br>\n";

                content += "<body><ul>\n";

                int slashCounter = 0;

                for(int i=0 ; i<path.length() ; i++)
                {
                    if(path.charAt(i) == '/')
                        slashCounter++;
                }

                if(slashCounter == 1)
                {
                    if (!path.equals("/") && !(path.equals("/" + root)))
                        root = "";
                    File file = new File(root);
                    //File file = new File(root);

                    if(! file.exists())
                    {
                        content = "<html>HTTP/1.1 404 NOT FOUND</html>";
                        /*try {
                            fileWriter.write("HTTP/1.1 404 NOT FOUND");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        //System.out.println("3");
                        response("HTTP/1.1 404 NOT FOUND" , null , content);
                        try {
                            socket.close();
                            fileWriter.write("\n\n\n");
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //fileWriter.write("\n\n\n");
                        //fileWriter.close();

                        return;
                    }

                    else if(file.isDirectory()) {

                        for (File f : file.listFiles()) {

                            if(f.isDirectory()) {
                                content += "<li><b><a href=\"" + f.getPath() + "\">" + f.getName() + "</a></b></li>";
                            }

                            if(f.isFile()){
                                content += "<li><a href=\"" + f.getPath() + "\">" + f.getName() + "</a></li>";
                            }
                        }

                        content += "</ul></body></html>";

                        response("HTTP/1.1 200 OK" , "text/html" , content);
                    }

                    else if(file.isFile())
                    {
                        downloader(file);
                    }

                    //response("HTTP/1.1 200 OK" , "text/html" , content);

                    //printWriter.write("Content-Length: " + content.length() + "\r\n");
                    //printWriter.write("\r\n");
                    //printWriter.write(content);
                    //printWriter.flush();
                }

                else
                {
                    File file = new File("." + path);

                    if(! file.exists())
                    {
                        content = "<html>HTTP/1.1 404 NOT FOUND</html>";
                        //System.out.println("3");
                        /*try {
                            fileWriter.write("HTTP/1.1 404 NOT FOUND");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        response("HTTP/1.1 404 NOT FOUND" , null , content);
                        try {
                            socket.close();
                            fileWriter.write("\n\n\n");
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    else if(file.isDirectory()) {

                        for (File f : file.listFiles()) {
                            String link = f.getParentFile().getName() + "/" + f.getName();

                            if(f.isDirectory()) {
                                content += "<li><b><a href=\"" + link + "\">" + f.getName() + "</a></b></li>";
                            }

                            if(f.isFile()){
                                content += "<li><a href=\"" + link + "\">" + f.getName() + "</a></li>";
                            }
                            //content += "<li><b><a href=\"" + link + "\">" + f.getName() + "</a></b></li>";
                        }

                        content += "</ul></body></html>";

                        response("HTTP/1.1 200 OK" , "text/html" , content);
                    }

                    else if(file.isFile())
                    {
                        downloader(file);
                    }

                    //response("HTTP/1.1 200 OK" , "text/html" , content);

                    //printWriter.write("Content-Length: " + content.length() + "\r\n");
                    //printWriter.write("\r\n");
                    //printWriter.write(content);
                    //printWriter.flush();
                }
            }

            else if(method.toUpperCase().startsWith("UPLOAD"))
            {
                //System.out.println("3");
                //System.out.println(path);
                path = input.replace("UPLOAD ", "");

                File file = new File(root + "\\" + path);

                int len;
                byte[] buffer = new byte[BUFFER_SIZE];

                DataInputStream dataInputStream = null;
                FileOutputStream fileOutputStream = null;
                BufferedInputStream bufferedInputStream = null;

                try {
                    fileOutputStream = new FileOutputStream(file);
                    bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                    dataInputStream = new DataInputStream(bufferedInputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try{
                    while((len = dataInputStream.read(buffer , 0 , BUFFER_SIZE)) > 0)
                    {
                        fileOutputStream.write(buffer , 0 , len);
                    }

                    fileOutputStream.close();
                    dataInputStream.close();
                    bufferedInputStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        try {
            socket.close();
            fileWriter.write("\n\n\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
