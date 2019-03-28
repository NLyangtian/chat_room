package com.chatroom.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class WriteDataToServer extends Thread{

    private  final Socket client;

    public WriteDataToServer(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            OutputStream clientOutput=client.getOutputStream();
            OutputStreamWriter writer=new OutputStreamWriter(clientOutput);
            Scanner scanner=new Scanner(System.in);
            System.out.println("请输入消息：");
            while(true){

                String message=scanner.nextLine();
                System.out.println("请输入消息：");
                //给服务器发送消息
                writer.write(message+"\n");
                writer.flush();

                if(message.equals("bye")){
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
