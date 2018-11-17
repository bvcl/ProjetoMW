package clientside;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientSide {

    private static Socket socket;
    private static PrintWriter outputToServer;
    private PrintWriter fileOutputToServer;
    private static BufferedReader inputFromServer;
    private static FileOutputStream fos;
    private static InputStream is;
    private FileInputStream fis;
    private OutputStream os;
    private static final int PORT = 8000;
    private String rootFolder = "C:/Users/Bruno/Pictures/ProjetoMWClient/";
    private String userFolder = "bvcl"; //Essa folder vai ficar dentro da rootFolder
    boolean Connected;

    public static void main(String[] args) throws InterruptedException {
        String server = "localhost";
        int port = PORT;

        if (args.length >= 1) {
            server = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        new ClientSide(server, port);
    }
    
    private boolean fileExists(File[] files, String filename) {
        boolean exists = false;
        for (File file : files) {
            if (filename.equals(file.getName())) {
                exists = true;
            }
        }
        System.out.println(exists);
        return exists;
    }

    public ClientSide(String server, int port) {
        try {
            socket = new Socket(server, port);
            outputToServer = new PrintWriter(socket.getOutputStream(), true);
            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	fileOutputToServer = new PrintWriter(socket.getOutputStream());

            System.out.println("Client is connected! ");
            Connected = true;
            String line = null;

            Scanner sc = new Scanner(System.in);
            System.out.print("Type command: ");

            while (sc.hasNextLine()) {
                String request = sc.nextLine();

                if (request.startsWith("exit")) {
                    outputToServer.println(request);
                    System.out.println("Application exited!");
                    //outputToServer.flush();
                    break;
                } 
                else if (request.startsWith("pwd")) {
                    outputToServer.println(request);
                    outputToServer.flush();
                } 
                else if (request.startsWith("list")) {
                    outputToServer.println(request);
                    outputToServer.flush();
                } 
                else if (request.startsWith("GET")) {
                    outputToServer.println(request);
                    outputToServer.flush();
                }
                else if (request.startsWith("SEND")) {
                    outputToServer.println(request);
                    outputToServer.flush();
                }
                while (Connected) {
                	if(!request.startsWith("SEND")){
                		line = inputFromServer.readLine();
                        System.out.println(line);
                        if (line.isEmpty()) {
                            Connected = false;
                            if (inputFromServer.ready()) {
                                System.out.println(inputFromServer.readLine());
                            }
                        }
                        if (line.startsWith("Status 400")) {
                            while (!(line = inputFromServer.readLine()).isEmpty()) {
                                System.out.println(line);
                            }
                            break;
                        }
                        if (request.startsWith("GET")) {
                        	String folder = this.rootFolder+this.userFolder+"/";
                            File file = new File(folder+request.substring(4));
                            is = socket.getInputStream();
                            fos = new FileOutputStream(file);

                            byte[] buffer = new byte[socket.getReceiveBufferSize()];
                            int bytesReceived = 0;
                            while ((bytesReceived = is.read(buffer)) >=0) {
                                //while ((bytesReceived = is.read(buffer))>=buffer) {
                                fos.write(buffer, 0, bytesReceived);
                            }
                            request = "";
                            fos.close();
                            is.close();
                        }
                	}
                    
                	else if (request.startsWith("SEND")) {
                    	String filename = request.substring(5);
                    	String folder = this.rootFolder+this.userFolder;
                        File file = new File(folder);
                        File[] files = file.listFiles();
                        if (fileExists(files, filename)) {
                        	String newFileName = folder+"/"+filename;
                            file = new File(newFileName);
                            int fileSize = (int) file.length();
                            fileOutputToServer.print("Status OK\r\n"
                                    + "Size " + fileSize + " KB" + "\r\n"
                                    + "\r\n"
                                    + "File " + filename + " Download was successfully\r\n");
                            fileOutputToServer.flush();
                            // reading files
                            fis = new FileInputStream(file);
                            os = socket.getOutputStream();
                            byte[] buffer = new byte[(1 << 14)-1];
                            int bytesRead = 0;
                            while ((bytesRead = fis.read(buffer))!= -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            os.close();
                            fis.close();
                        } 
                        else {
                        	fileOutputToServer.print("Status 400\r\n"
                                    + "File " + filename + " not found\r\n"
                                    + "\r\n");
                        	fileOutputToServer.flush();
                        }
                        Connected = false;
                    }
                }
                System.out.print("\nType command: ");
                Connected = true;
                socket = new Socket(server, port);
                outputToServer = new PrintWriter(socket.getOutputStream(), true);
                inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            	fileOutputToServer = new PrintWriter(socket.getOutputStream());

            }
            outputToServer.close();
            inputFromServer.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}