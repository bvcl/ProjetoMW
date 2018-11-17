package serverside;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ServerSide {

    private BufferedReader inputFromClient;
    private BufferedReader fileInputFromClient;
    private PrintWriter outputToClient;
    private FileInputStream fis;
    private OutputStream os;
    private static FileOutputStream fos;
    private static InputStream is;
    private static final int PORT = 8000;
    private ServerSocket serverSocket;
    private Socket socket;
    private String rootFolder = "C:/Users/Bruno/Pictures/ProjetoMWServer/";
    private String userFolder = "bvcl"; //Essa folder vai ficar dentro da rootFolder
    
    public static void main(String[] args) {
        int port = PORT;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        new ServerSide(port);
    }

    private boolean fileExists(File[] files, String filename) {
        boolean exists = false;
        for (File file : files) {
            if (filename.equals(file.getName())) {
                exists = true;
            }
        }
        return exists;
    }

    public ServerSide(int port) {
        // create a server socket
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("Error in server socket creation.");
            System.exit(0);
        }

        while (true) {
            try {

                socket = serverSocket.accept();

                outputToClient = new PrintWriter(socket.getOutputStream());
                inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            	fileInputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    String request = inputFromClient.readLine();

                    if (!request.startsWith("exit") && !request.startsWith("pwd") && !request.startsWith("list") && !request.startsWith("GET") && !request.startsWith("SEND")) {
                        outputToClient.println("Wrong request\r\n"
                                + "\r\n");
                    } 
                    else if (request.startsWith("exit")) {
                        break;
                    } 
                    else if (request.startsWith("pwd")) {
                    	String folder = this.rootFolder+this.userFolder;
                        File file = new File(folder);
                        outputToClient.print("Status OK\r\n"
                                + "Lines 1\r\n"
                                + "\r\n"
                                + "Working dir: " + file.getName() + "\r\n");
                    } 
                    else if (request.startsWith("list")) {
                    	String folder = this.rootFolder+this.userFolder;
                        File file = new File(folder);
                        File[] files = file.listFiles();
                        System.out.println(files.length);
                        String [] fileNames = new String[files.length];
                        int position = 0;
                        for(File f : files) {	 	
                			fileNames[position]=f.getName();
                			position++;
                         }
                        outputToClient.print("Status OK\r\n"
                                + files.length + " Files: " + "\r\n"
                                + "\r\n"
                                + Arrays.toString(fileNames).substring(1, Arrays.toString(fileNames).length() - 1) + "\r\n");
                    } 
                    else if (request.startsWith("GET")) {
                        String filename = request.substring(4);
                        String folder = this.rootFolder+this.userFolder;
                        File file = new File(folder);
                        File[] files = file.listFiles();

                        if (fileExists(files, filename)) {
                        	String newFileName = folder+"/"+filename; 
                            file = new File(newFileName);
                            int fileSize = (int) file.length();
                            outputToClient.print("Status OK\r\n"
                                    + "Size " + fileSize + " KB" + "\r\n"
                                    + "\r\n"
                                    + "File " + filename + " Download was successfully\r\n");
                            outputToClient.flush();
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
                        } else {
                            outputToClient.print("Status 400\r\n"
                                    + "File " + filename + " not found\r\n"
                                    + "\r\n");
                            outputToClient.flush();
                        }
                    }
                    else if (request.startsWith("SEND")) {
                    	String line = fileInputFromClient.readLine();
                    	System.out.println(line);
                    	String folder = this.rootFolder+this.userFolder+"/";
                        File file = new File(folder+request.substring(5));
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
                    outputToClient.flush();
                    socket = new Socket(); 
                    socket = serverSocket.accept();
                    outputToClient = new PrintWriter(socket.getOutputStream());
                    inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                	fileInputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                }
            } catch (IOException e) {
            	e.printStackTrace();
                //System.err.println(e);
            }
        }
    }
}