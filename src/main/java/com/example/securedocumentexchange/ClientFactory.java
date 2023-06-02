package com.example.securedocumentexchange;

public class ClientFactory extends Thread{
    String address;
    Integer port;
    public ClientFactory(String address, Integer port){
        this.address = address;
        this.port = port;
    }
    @Override
    public void run() {
        Client client = new Client(address, port);
    }
}
