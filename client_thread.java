import java.io.*;
import java.lang.*;
import java.net.*;
import static java.lang.Math.toIntExact;

public class client_thread{
	public static void main(String[] args){
        
        Socket sock;
        BufferedReader fromkeyboard = null;
        BufferedReader fromserver = null;
        PrintWriter toserver = null;

		try{
            
            //InputStreamReader isr = null;
            String str;
            String command, option;
            
            System.out.println("---- New client started ----");
            //System.out.println("Enter the port number.");
            fromkeyboard = new BufferedReader(new InputStreamReader(System.in));
            int portnum;
            if(args.length == 0) portnum = 2020;
            else portnum = Integer.parseInt(args[0]);

            sock = new Socket();
            sock.connect(new InetSocketAddress("127.0.0.1", portnum));
            //sock.setSoTimeout(10000);
            //System.out.println("Connecting...");
            
            fromserver = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toserver = new PrintWriter(sock.getOutputStream());
            
            while(true) {
                
                System.out.println("Enter the message");
                str = fromkeyboard.readLine();
                toserver.println(str);
                toserver.flush();
                //System.out.println("count//");
                String[] temp = str.split(" ");
                command = temp[0];
                
                if(temp.length == 1) option = null; //no argument
                else option = temp[1];

                
                if(command.equals("GET")) 
                    receieve(sock.getInputStream(), option);
                
                else if(command.equals("PUT"))
                    send(sock.getOutputStream(), option);
                
                else if(command.equals("CD"))
                    changeDirectory(fromserver);
                
                else if(command.equals("LIST"))
                    fileList(fromserver);
                
                else if(command.equals("QUIT")) break;
                
                else
                    System.out.print("Invalid command. ");
                
            }
        
            System.out.println("---- Client thread terminated ----");
            toserver.close();
            fromserver.close();
            sock.close();

        } catch (Exception e){}
    }
	
	public static void changeDirectory(BufferedReader fromserver) throws Exception{
		
		String dirName = fromserver.readLine();
		System.out.println(dirName);
	}
	
	public static void fileList(BufferedReader fromserver) throws Exception{
		
		String line;
		while (!(line = fromserver.readLine()).equals("EOF")) {
	        System.out.println(line);
	        //System.out.println("test2");
	    }
	}
	
	public static synchronized void send(OutputStream toserver, String option) throws Exception {

		PrintWriter pr = new PrintWriter(toserver);

        try {
            int bytesRead;
        	File myFile = new File(option);
            byte[] mybytearray = new byte[(int) myFile.length()];
            InputStream fis = new FileInputStream(myFile);
            System.out.println("Sending...");
            //BufferedInputStream bis = new BufferedInputStream(fis);
            while(true){
                if(fis.available() > 0){
                    bytesRead = fis.read(mybytearray);
                    toserver.write(mybytearray, 0, bytesRead);
                }
                else break;
            }

            toserver.flush();
            fis.close();

            System.out.println(myFile.getName()+" transferred / "+myFile.length()+" bytes");
        }catch (FileNotFoundException e) {
        	System.out.println("No such file exist");
        }
		
    }
	
	public static void receieve(InputStream fromserver, String option){
        
        //BufferedReader br = new BufferedReader(new InputStreamReader(fromserver));
		try{
            //int fsize = br.read();
            
            int bytesRead;
            File myfile = new File(option);

            if(myfile.toPath().isAbsolute()) myfile = new File(myfile.getName());

	        byte[] mybytearray = new byte[9999999];
	        OutputStream fos = new FileOutputStream(myfile);

            //bytesRead = fromserver.read(mybytearray, 0, mybytearray.length);

            //int init = 0;
            while (true) {
            
                if(fromserver.available() > 0){
                    bytesRead = fromserver.read(mybytearray);
                    fos.write(mybytearray, 0, bytesRead);
                }
                else break;
            }
            //fos.write(mybytearray, 0, bytesRead);
            System.out.println("Receieved "+myfile.getName()+" / "+myfile.length()+" bytes");

	        fos.flush();
            fos.close();

		} catch (IOException e) {
            System.out.println("Reception Failed.");
            e.printStackTrace();
        }

        
	}

} 