package com.chatroom.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ExecuteClient implements  Runnable {

    private final Socket client;
    private String password;
    private static final Map<String,Socket> ONLINE_USER_MAP=new ConcurrentHashMap<String, Socket>();

    private static final Map<Socket ,String> account=new ConcurrentHashMap<>();

    private static final Map<String,String>  Account_Key=new ConcurrentHashMap<>();

    public ExecuteClient(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            //获取客户端输入
            InputStream clientInput=this.client.getInputStream();
            Scanner scanner=new Scanner(clientInput);


            while(true){
                String line =scanner.nextLine();

                /*
                注册：userName：<name>
                登录：login:<userName>:<password>
                私聊：private:<name>:<message>
                群聊：group:<message>
                退出：bye
                 */

                if(line.split("\\:")[0].equalsIgnoreCase("userName")){
                    String userName=line.split("\\:")[1];
                    this.register(userName,client);
                    this.setPassWord(userName,password);
                    continue;
                }

                if(line.equalsIgnoreCase("login")){
                    String [] segments=line.split("\\:" );
                    String userName=segments[1];
                    String password=segments[2];
                    this.login(userName,password);
                    continue;
                }

                if(line.split("\\:")[0].equalsIgnoreCase("private")){
                    String []segments=line.split("\\:");
                    String userName=segments[1];
                    String message=segments[2];
                    this.privateChat(userName,message);

                }

                if(line.split("\\:")[0].equalsIgnoreCase("group")){
                    String message =line.split("\\:")[1];
                    this.groupChat(message);
                    continue;
                }


                if(line.equalsIgnoreCase("bye")){
                    this.quit();
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //设置密码
    private void setPassWord(String userName, String password) throws IOException {
        sendMessage(this.client,"请设置密码：密码格式为字母、数字的组合");
        InputStream clientInput=this.client.getInputStream();
        Scanner sc = new Scanner(clientInput) ;
        password=sc.next();
        if(password.length()<6||password.length()>15){
            sendMessage(this.client,"请输入指定长度的密码");

            setPassWord(userName,password);
        }else {
            Account_Key.put(userName,password);
            sendMessage(this.client,"密码设置成功！");
        }
    }

    //登录
    private void login(String currentUserName, String password) throws IOException {
        sendMessage(this.client,"请输入账号和密码:");
        InputStream clientInput=this.client.getInputStream();
        Scanner sc = new Scanner(clientInput) ;
        String line=sc.nextLine();
        currentUserName=line.split(" ")[0];
        password=line.split(" ")[1];
        if(currentUserName.equals(Account_Key.get(password))
                && password.equals(Account_Key.get(currentUserName))) {
            printOnlineUser();
            sendMessage(this.client, currentUserName + "登录成功！");
        }else{
            sendMessage(this.client,"密码或账号输入错误，请重新输入：");
            login(currentUserName,password);
        }
    }

    //获取当前用户的密码：
    private String getPassword() {
        String currentUserPassord="";
        for(Map.Entry<Socket,String > entry:account.entrySet()){
            if(this.client.equals(entry.getValue())){
                currentUserPassord=entry.getValue();
                break;
            }
        }
        return currentUserPassord;
    }
    //退出
    private void quit() {
        String currentName=this.getCurrentUserName();
        System.out.println("用户"+currentName+"下线!");

        Socket socket=ONLINE_USER_MAP.get(currentName);
        this.sendMessage(socket,"bye");
        ONLINE_USER_MAP.remove(currentName);
        printOnlineUser();
    }

    //群聊
    private void groupChat(String message) {
        for(Socket socket:ONLINE_USER_MAP.values()){
            //群聊中自己发送的消息自己不接收
            if(socket.equals(this.client)){
                continue;
            }
            this.sendMessage(socket,this.getCurrentUserName());
        }
    }

    //私聊
    private void privateChat(String userName, String message) {
        String currentUserName=this.getCurrentUserName();
        Socket target=ONLINE_USER_MAP.get(userName);
        if(target!=null){
            this.sendMessage(target,currentUserName+
            "对你说"+message);
        }
    }

    //获取当前用户的名称
    private String getCurrentUserName() {
        String currentUserName="";
        for(Map.Entry<String,Socket>entry: ONLINE_USER_MAP.entrySet()){
            if(this.client.equals(entry.getValue())){
                currentUserName=entry.getKey();
                break;
            }
        }
        return currentUserName;
    }

    //注册：
    private void register(String userName, Socket client) {
        sendMessage(this.client,"欢迎使用聊天室！");
        ONLINE_USER_MAP.put(userName,client);
        sendMessage(this.client,userName+"注册成功！");
    }

    private void sendMessage(Socket socket, String message) {
        try {
            OutputStream clientOutputStream=socket.getOutputStream();
            OutputStreamWriter writer=new OutputStreamWriter(clientOutputStream);
            writer.write(message+"\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //打印当前在线用户
    private void printOnlineUser() {
        System.out.println("当前在线用户数："+ONLINE_USER_MAP.size());
       for( Map.Entry<String ,Socket>entry:ONLINE_USER_MAP.entrySet()){
           System.out.println(entry.getKey());
       }
    }
}
