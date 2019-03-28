package com.chatroom.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class ReadDataFromServer extends  Thread {
    private  final Socket client;

    public ReadDataFromServer(Socket client) {
        this.client = client;
    }


    @Override
    public void run() {
        try {
            InputStream clientInput=client.getInputStream();
            Scanner scanner= new Scanner(clientInput);

            while(true){
                String message=scanner.nextLine();
                System.out.println("一条来自服务器的消息："+message);

                if(message.equals("bye")){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
