package com.example.securedocumentexchange;

public class ServerFactory extends Thread{
    Integer port;
    public ServerFactory(Integer port){
        this.port = port;
    }

    public void run(){
        Server server = new Server(port);

    }
}
