package com.neusoft.bmpc.tools.hydra;

import java.util.List;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * Hello world!
 *
 */
public class Main 
{
    private static File getFile() {  
        String path = Main.class.getProtectionDomain().getCodeSource()  
                .getLocation().getFile();  
        try{  
            path = java.net.URLDecoder.decode(path, "UTF-8");  
        }catch (java.io.UnsupportedEncodingException e){  
            return null;  
        }  
        return new File(path);  
    } 
    
    private static void printUseage() {
		System.out.println( "useage: java -jar hydra.jar COMMAND" );
		System.out.println( "useage: java -jar hydra.jar -upload SRCFILELIST DST" );
		System.out.println( "useage: java -jar hydra.jar -download HOST:FILEPATH" );
    }
    
    public static void main( String[] args ) throws JSchException
    {
    	if (args.length < 1) {
    		printUseage();
    		return;
    	}
    	
    	Console console = System.console();  
        if (console == null) {  
             throw new IllegalStateException("Console is not available!");  
        }  
 
		String userName = null;
		userName = console.readLine("user name: "); 
		
		char[] password = null;
		password = console.readPassword("password: ");
		
		
		String line="";
		BufferedReader br = null;
		List<String> hosts = new ArrayList<String>();
		String confFile =  getFile().getParent() + "/hosts.conf";
		try {
			br = new BufferedReader(new FileReader(confFile));

			while((line=br.readLine())!=null){
				hosts.add(line);
			}
		} catch (FileNotFoundException e) {
			System.out.println("cannot find hosts.conf at " + confFile);
			return;
		} catch (IOException e) {
			System.out.println("read file error");
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {

				}
			}
		}
		
		if (args[0].equals("-upload")) {
			if (args.length < 3) {
				printUseage();
				return;
			}
			
			String[] srcFileList = args[1].split(",");
			List<File> srcFileListL = new ArrayList<File>(srcFileList.length);
			
			for(String sf : srcFileList) {
				File curFile = new File(sf);
				srcFileListL.add(curFile);
			}
			
			SSHClient sc = new SSHClient("",22,"","");
			for(String host : hosts) {
				System.out.print("upload files to " + host + " ................");
				sc.setHost(host);
				sc.setName(userName);
				sc.setPassword(new String(password));
				
				try {
					sc.uploadFile(srcFileListL, args[2]);
					System.out.println("Finished!");
				} catch (SftpException e) {
					System.out.println("Failed!");
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.out.println("Failed!");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Failed!");
					e.printStackTrace();
				}

			}
		}else if (args[0].equals("-download")) {
			if (args.length < 2) {
				printUseage();
				return;
			}
			
			if (!args[1].contains(":")) {
				printUseage();
				return;
			}
			
			String[] srcFile = args[1].split(":");
			SSHClient sc = new SSHClient("",22,"","");
			sc.setHost(srcFile[0]);
			sc.setName(userName);
			sc.setPassword(new String(password));
			try {
				sc.downloadFile(new File(srcFile[1]), new File("."));
			} catch (SftpException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			if (args.length != 1) {
				printUseage();
				return;
			}
			SSHClient sc = new SSHClient("",22,"","");
			for(String host : hosts) {
				System.out.print("send command to " + host + " ................");
				sc.setHost(host);
				sc.setName(userName);
				sc.setPassword(new String(password));
				
				sc.sendcommand(args[0]);
				System.out.println("Finished!");
			}
		}

    }
}
