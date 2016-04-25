package com.neusoft.bmpc.tools.hydra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import com.jcraft.jsch.*;


public class SSHClient {
	private String host;
	private int port;
	private String name;
	private String password;
	
	public SSHClient(String host, int port, String name, String password) {
		this.host = host;
		this.port = port;
		this.name = name;
		this.password = password;
	}
	
	public void sendcommand(String command) throws JSchException {
		if (this.host.isEmpty() || (this.port == 0) || this.name.isEmpty() || this.password.isEmpty()) {
			return;
		}
		
		JSch jsch=new JSch();  

	    Session session=jsch.getSession(this.name, this.host, this.port);
	      
	    session.setPassword(this.password);
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect(10000);

	    Channel  channel= session.openChannel("exec");  
	    ((ChannelExec)channel).setCommand(command);
	    channel.connect();
		
	    channel.disconnect();
	    session.disconnect();
		
	}
	
	public  void sendInteractiveCommands(List<String> commandList) throws JSchException, IOException {
		if (this.host.isEmpty() || (this.port == 0) || this.name.isEmpty() || this.password.isEmpty()) {
			return;
		}
		
	      JSch jsch=new JSch();  

	      Session session=jsch.getSession(this.name, this.host, this.port);
	      
	      session.setPassword(this.password);
	      session.setConfig("StrictHostKeyChecking", "no");
	      session.connect();

 
	      ChannelShell  channel=(ChannelShell)session.openChannel("shell");

	      PipedInputStream pis=new PipedInputStream();
	      PipedOutputStream pos=new PipedOutputStream();
	      pis.connect(pos);
	      
	      channel.setInputStream(pis);
	      channel.setOutputStream(System.out);
	      channel.connect();
		
	      for (String command: commandList) {
	    	  
				try{
					 Thread.sleep(500);
					 pos.write(command.getBytes());
					 Thread.sleep(1500);
				}catch(Exception e){
					
				}
	      }
	      
	      pis.close();
	      pos.close();
	      
	      channel.disconnect();
	      session.disconnect();

	}
	
	public void uploadFile(List<File> files, String dst) throws JSchException, SftpException, InterruptedException, IOException {
		for(File file : files) {
			if (!file.exists()) throw new FileNotFoundException();
		}
		
        JSch jsch = new JSch();  
        Session session = jsch.getSession(name, host, port);  
	    session.setPassword(this.password);
	    session.setConfig("StrictHostKeyChecking", "no");
        session.connect();  
        Channel channel = session.openChannel("sftp");  
        channel.connect();  
        ChannelSftp sftpChannel = (ChannelSftp)channel;  
        sftpChannel.cd(dst);  
              
        InputStream in = null;  
        OutputStream out = null;  
        for(int i = 0; i < files.size(); i++){  
            File curFile = files.get(i);    
  
            out = sftpChannel.put(curFile.getName());  
            Thread.sleep(5000);  
              
            in = new FileInputStream(curFile);  
              
            byte [] b = new byte[1024];  
            int n;  
            while ((n = in.read(b)) != -1) {  
                out.write(b, 0, n);  
            }  
        }  
            out.flush();  
            out.close();  
            in.close();  
            sftpChannel.disconnect();  
            session.disconnect();  
            Thread.sleep(500);  

	}
	
	public void downloadFile(File file, File dst) throws JSchException, SftpException, InterruptedException, IOException {

		if (!dst.exists()) throw new IOException("No such directory!");
		
        JSch jsch = new JSch();  
        Session session = jsch.getSession(name, host, port);  
	    session.setPassword(this.password);
	    session.setConfig("StrictHostKeyChecking", "no");
        session.connect();  
        Channel channel = session.openChannel("sftp");  
        channel.connect();  
        ChannelSftp sftpChannel = (ChannelSftp)channel;  
        InputStream is = sftpChannel.get(file.getAbsolutePath()); 
        
        File dstFile = new File(dst.getAbsolutePath(), file.getName());
        FileOutputStream fos = new FileOutputStream(dstFile);
        
        byte[] b = new byte[1024];
        int n;
        while((n =is.read(b)) != -1) {
        	fos.write(b, 0, n);
        }
        
        fos.flush();
        fos.close();
        is.close();
        Thread.sleep(2000);
        
        sftpChannel.disconnect();  
        session.disconnect();  

	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
