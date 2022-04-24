/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package riskserver;

import com.sun.webkit.ThemeClient;
import game.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ruveyda Sultan Uzun
 */
public class SClient {

    ArrayList<Integer> countries = new ArrayList();
    ArrayList<Integer> countries2 = new ArrayList();

    int id;
    public String name = "NoName";
    Socket soket;
    ObjectOutputStream sOutput;
    ObjectInputStream sInput;
    //client gelenleri bekleme threadi
    Listen listenThread;
    //2 client eşleştirme threadi
    PairingThread pairThread;
    //düşman client
    SClient enemy;
    //eşleşme kontrolu
    public boolean paired = false;

    public SClient(Socket gelenSoket, int id) {
        this.soket = gelenSoket;
        this.id = id;
        try {
            this.sOutput = new ObjectOutputStream(this.soket.getOutputStream());
            this.sInput = new ObjectInputStream(this.soket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        //thread nesneleri
        this.listenThread = new Listen(this);
        this.pairThread = new PairingThread(this);

    }

    //clienta mesaj gönderimi
    public void Send(Message message) {
        try {
            this.sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    //clienti dinleme threadi
   
    class Listen extends Thread {

        SClient TheClient;

        //thread nesne alması için yapıcı metod
        Listen(SClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //clientlar baglandığı sürece çalış
            while (TheClient.soket.isConnected()) {
                try {
                   // client gelen mesajlar
                    Message received = (Message) (TheClient.sInput.readObject());
                  
                    switch (received.type) {
                        case Name:
                            TheClient.name = received.content.toString();
                            // nameler gönderildikten sonra eşleşme başlar
                            TheClient.pairThread.start();
                            break;
                        case Disconnect:
                            break;
                        case Text:
                            //chatbox mesaj iletimleri için gelen mesaj diğer oyuncuya gönderilir
                            Server.Send(TheClient.enemy, received);
                            break;
                        case Dice:
                            // zar sonucu karşı rakibe gönderilir
                            System.out.println("dice içi : " + Integer.valueOf(received.content.toString()));
                            Server.Send(TheClient.enemy, received);

                        case loseWar:
                            // savaşı kaybeden taraf için mesaj gösterilir
                            Server.Send(TheClient.enemy, received);
                        case winWar:
                                // savaşı kazanan taraf için mesaj gösterilir
                            Server.Send(TheClient.enemy, received);

                        case Selected:
                            
                            Server.Send(TheClient.enemy, received);
                            break;
                      

                    }

                } catch (IOException ex) {
                    Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                    //client bağlantısı kopar ise listeden silinir
                    Server.Clients.remove(TheClient);

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                    //client bağlantısı kopar ise listeden silinir
                    Server.Clients.remove(TheClient);
                }
            }

        }
    }

  
    class PairingThread extends Thread {
// 2 client eşleştirme için kullanılan thread
        SClient TheClient;

        PairingThread(SClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //client bağlı olduğuı zaman ve eşleşme olmadığı zaman çalışacak
            while (TheClient.soket.isConnected() && TheClient.paired == false) {
                try {
                    //lock mekanizması
                    //sadece bir client içeri grebilir
                    //diğerleri release olana kadar bekler
                    Server.pairTwo.acquire(1);

                    //client eğer eşleşmemişse gir
                    if (!TheClient.paired) {
                        SClient crival = null;
                        //eşleşme sağlanana kadar dön
                        while (crival == null && TheClient.soket.isConnected()) {
                            //liste içerisinde eş arıyor
                            for (SClient clnt : Server.Clients) {
                                if (TheClient != clnt && clnt.enemy == null) {
                                    //eşleşme sağlandı ve gerekli işaretlemeler yapıldı
                                    crival = clnt;
                                    crival.paired = true;
                                    crival.enemy = TheClient;
                                    TheClient.enemy = crival;
                                    TheClient.paired = true;
                                    break;
                                }
                            }
                           
                            sleep(1000);
                        }
                        
                        // eşleşme olduğunda her iki tarfada bölge bilgisi gönderilir.
                   

                        countries.add(1);
                        countries.add(2);
                        countries.add(3);
                        countries.add(8);
                        countries.add(9);
                        countries.add(12);
                        countries.add(11);
                        countries.add(13);
                        countries.add(6);
                        countries.add(14);
                        countries2.add(10);
                        countries2.add(5);
                        countries2.add(7);
                        countries2.add(4);
                        countries2.add(15);
                        countries2.add(16);
                        countries2.add(17);
                        countries2.add(18);
                        countries2.add(19);
                        countries2.add(20);

                        Message msg1 = new Message(Message.Message_Type.RivalConnected);
                        msg1.content = TheClient.name;
                        Server.Send(TheClient.enemy, msg1);

                        Message msg2 = new Message(Message.Message_Type.RivalConnected);
                        msg2.content = enemy.name;
                        Server.Send(TheClient, msg2);

                        Message msg3 = new Message(Message.Message_Type.List);
                        msg3.content = countries;
                        Server.Send(TheClient.enemy, msg3);

                        Message msg4 = new Message(Message.Message_Type.List);
                        msg4.content = countries2;
                        Server.Send(TheClient, msg4);

                    }
                    // lock maknşzması 
                    Server.pairTwo.release(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PairingThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
