/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package riskclient;

import game.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import game.Game;
import static game.Game.txt_messageBox;
import game.startGame;
import java.util.ArrayList;

import static riskclient.Client.mInput;

/**
 *
 * @author Ruveyda Sultan Uzun
 */
class Listen extends Thread {

    /**
     * @param args the command line arguments
     */
    public void run() {
        
        while (Client.socket.isConnected()) {
            try {
                
                // gelen mesajların alımı
                Message received = (Message) (mInput.readObject());

                switch (received.type) {
                    case Name:
                        break;
                    case RivalConnected:
                         
                        break;
                    case List:
                        // serverdan gelen listeler, arayüze yazılır
                        System.out.println("mesaj list içi");
                        startGame.openGame();
                        Game.thisGame.txt_countries.setText(received.content.toString() + "\n");
                        Game.thisGame.countries = (ArrayList<Integer>) received.content;
                       
                        break;
                    case Disconnect:
                        break;
                    case Text:
                        // chatbox için gelen mesajlar chat boxa yazılır
                        Game.thisGame.txt_messageBox.setText(txt_messageBox.getText() + "\n" + received.content.toString());
                        break;
                    case Selected:
                        // düşman haritada bölgeye saldırdığın uyarı verilir.
                        Game.thisGame.Warning(Integer.parseInt(received.content.toString()));
                        break;
                    case Dice:
                        // zar sonuuc yazdırılır
                       
                        Game.thisGame.enemyResult = Integer.valueOf(received.content.toString());
                        break;
                    case winWar:
                        // savaşı kazanan için bilgi mesajı verilir ve listeler düzenlenir
                        Game.thisGame.countries.remove(received.content);
                        Game.thisGame.txt_countries.setText(Game.thisGame.countries + "\n");
                        Game.thisGame.loseWar(Integer.valueOf(received.content.toString()));
                        break;
                    case loseWar:
                         // savaşı kaybeden için bilgi mesajı verilir ve listeler düzenlenir
                        Game.thisGame.countries.add(Integer.parseInt(received.content.toString()));
                        Game.thisGame.txt_countries.setText(Game.thisGame.countries + "\n");
                        Game.thisGame.winWar(Integer.valueOf(received.content.toString()));
                        break;
                 

                }

            } catch (IOException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}

public class Client {

    // socket
    public static Socket socket;
  // verileri dışa göndermek içişn
    public static ObjectOutputStream mOutput;
   // verileri almak için
    public static ObjectInputStream mInput;
    // Karşıdan gelen verileri dinlemek için
    public static Listen listenMe;

    public static void Start(String ip, int port) {

        try {
            Client.socket = new Socket(ip, port);
            Client.Display("Servera Bağlandı");
            Client.mInput = new ObjectInputStream(Client.socket.getInputStream());
            Client.mOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start();
           

        } catch (IOException ex) {
            Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void Stop() {
        try {
            if (Client.socket != null) {
                Client.listenMe.stop();
                Client.socket.close();
                Client.mOutput.flush();
                Client.mOutput.close();
                Client.mInput.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void Display(String msg) {
        System.out.println(msg);

    }

    public static void Send(Message msg) {
        try {
            Client.mOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {

    }

}
