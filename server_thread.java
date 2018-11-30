import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.ArrayList;
import java.nio.file.Paths;

public class server_thread extends Thread{

    private static int nn;
	public static void main(String[] args) throws Exception {

        int portnum;
	    //System.out.println("**Enter the port number");
	    BufferedReader fromkeyboard = new BufferedReader(new InputStreamReader(System.in));
        //final ArrayList<Thread> threads = new ArrayList<Thread>(10);
        if(args.length == 0) portnum = 2020;
        else portnum = Integer.parseInt(args[0]);
        ServerSocket ss = new ServerSocket(portnum);
        System.out.println("**Server Started");
        nn=0; 
        while(true) {
            
            try{
                System.out.println("**Waiting for the request...");
                final Socket clientsocket = ss.accept();
                nn += 1;
                System.out.println("**Got a client #"+nn);
    
                Thread t = new Thread(new Runnable(){
                    public void run(){
            
                        try {
            
                            File nowdirectory = new File(".");
                            BufferedReader fromclient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
                            PrintWriter toclient = new PrintWriter(clientsocket.getOutputStream());
                            while(true) {
            
                                String str = fromclient.readLine();
                                
                                String command, option;
                                String[] temp = str.split(" ");
                                command = temp[0];
                                
                                if(temp.length == 1) option = null; //no argument
                                else option = temp[1];
                                
                                if(command.equals("GET"))
                                    send(clientsocket.getOutputStream(), nowdirectory, option);
                                
                                else if(command.equals("PUT"))
                                    receiveFile(clientsocket.getInputStream(), nowdirectory, option);
                                
                                else if(command.equals("CD"))
                                    nowdirectory = changeDirectory(option, nowdirectory, toclient);
                                
                                else if(command.equals("LIST"))
                                    fileList(option, nowdirectory, toclient);
                                
                                else if(command.equals("QUIT")) {
                                    System.out.println("**A Connection terminate.");
                                    nn--;
                                    try{
                                        clientsocket.close();
                                    } catch(IOException kk){}
                                    break;
                                }  
                            }
            
                        } catch (Exception e){
                            e.printStackTrace();
                        }  
                    }
                });
                //threads.add(t);
                t.start();

            } catch (IOException ee){
                ee.printStackTrace();
                break;
            }
        }

        System.out.println("**Server Terminated");
        fromkeyboard.close();
        ss.close();

    }

	public static File changeDirectory(String dirName, File nowdirectory, PrintWriter toclient) throws Exception{
		
		if(dirName == null) {
			toclient.println(nowdirectory.getCanonicalPath());
		}
		else {
			boolean result = false;
			File dir = new File(dirName);
		
			if(!dir.toPath().isAbsolute()) { //relative path
                    
                nowdirectory = new File(dir.getCanonicalPath());
                result = (System.setProperty("user.dir", dir.getAbsolutePath()) != null);
			}
			else { //absolute path
                if(!dir.exists() || !dir.isDirectory())
                    toclient.println("Failed - directory name is invalid");

                else{
                    nowdirectory = new File(nowdirectory, dirName);
                    result = (System.setProperty("user.dir", dir.getAbsolutePath()) != null);
                }
			}
			toclient.println(nowdirectory.getCanonicalPath());
		}
		
		toclient.flush();
		return nowdirectory;
		
	}
	
	public static void fileList(String dirName, File nowdirectory, PrintWriter toclient) {
				
		File[] files = null;
		
		if(dirName == null) {
			files = nowdirectory.listFiles();
			for(File f: files) {
				String fileName = f.getName();
				
				if(f.isDirectory()) {
					toclient.println(fileName+",-");
				}
				else {
					toclient.println(fileName+","+f.length());
				}
			}
		}
		
		else{
			File dir = new File(dirName);
			if(!dir.isDirectory())
				toclient.println("Failed - directory name is invalid");			
			
			else {
				
				files = dir.listFiles();
				for(File f: files) {
					String fileName = f.getName();
					
					if(f.isDirectory()) {
						toclient.println(fileName+",-");
					}
					else {
						toclient.println(fileName+","+f.length());
					}
				}
			}
		}
		toclient.println("EOF");
		toclient.flush();
		
	}
	
    public static void send(OutputStream toclient, File nowdirectory, String option){

        PrintWriter pr = new PrintWriter(toclient);

    	try{
            
            int bytesRead;
            File myFile = new File(option);

            if(!myFile.toPath().isAbsolute()) myFile = new File(nowdirectory, myFile.getName());

    		byte[] mybytearray = new byte[(int) myFile.length()];
            InputStream is = new FileInputStream(myFile);
            //BufferedInputStream bis = new BufferedInputStream(fis);
            while(true){
                if(is.available() > 0){
                    bytesRead = is.read(mybytearray);
                    toclient.write(mybytearray, 0, bytesRead);
                }
                else break;
            }
            System.out.println("**Sending...");
            //toclient.write((int)myFile.length());
            toclient.flush();
            is.close();

    	} catch (Exception e) {
    		pr.println("Failed - Such file does not exist!");
    		pr.flush();
        }
        //pr.close();
        //toclient.close();
        
    }

    public static synchronized void receiveFile(InputStream fromclient, File nowdirectory, String option){
        
    	try {
            //InputStream fromclient = new Isock.getInputStream();
    		int bytesRead;
            byte[] mybytearray = new byte[9999999];
            //System.out.println(nowdirectory.getCanonicalPath());
            File myfile = new File(nowdirectory.getCanonicalPath(), option);

            OutputStream fos = new FileOutputStream(myfile.getCanonicalPath());
            //BufferedOutputStream bos = new BufferedOutputStream(fos);
            while(true){
                if(fromclient.available() > 0){
                    bytesRead = fromclient.read(mybytearray);
                    fos.write(mybytearray, 0, bytesRead);
                }
                else break;
            }

            fos.flush();
            fos.close();
            //fromclient.close();

    	}catch (IOException e) {
            System.out.println("Reception Failed.");

    	}
        
    }
} 

