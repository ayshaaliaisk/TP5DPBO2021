/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import static modulgame.dbConnection.con;
import static modulgame.dbConnection.stm;

/**
 *
 * @author Aysha Alia
 */
public class Game extends Canvas implements Runnable{
    Window window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private int score = 0;
    private int final_score = 0;
    private String username = "";
    
    private int time = 20;
//    private int time_normal = 10;
//    private int time_hard = 5;
    
    private Thread thread;
    private boolean running = false;
    
    
    
    private Handler handler;
    
    public enum STATE{
        Game,
        GameOver
    };
    
    public STATE gameState = STATE.Game;
    
    public Game(String uname){
        window = new Window(WIDTH, HEIGHT, "Tugas praktikum 5", this);
        
        handler = new Handler();
        
        this.addKeyListener(new KeyInput(handler, this));
        
        if(gameState == STATE.Game){
            handler.addObject(new Items(100,150, ID.Item));
            handler.addObject(new Items(200,350, ID.Item));
            handler.addObject(new Player(200,200, ID.Player));
            handler.addObject(new Player2(200,250, ID.Player2));
            handler.addObject(new Musuh(300,250, ID.Musuh));
        }
        username = uname;
    }

    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while(delta >= 1){
                tick();
                delta--;
            }
            if(running){
                render();
                frames++;
            }
            
            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                //System.out.println("FPS: " + frames);
                frames = 0;
                if(gameState == STATE.Game){
                    if(time>0){
                        time--;
                    }else{
                        gameState = STATE.GameOver;
                    }
                }
            }
        }
        
        stop();
        
    }
    
    public int acak() {
    return (int) ((Math.random() * (300 - 0)) + 0);
}
    
    private void tick(){
        handler.tick();
        if(gameState == STATE.Game){
            GameObject playerObject = null;
            GameObject player2Object = null;
            GameObject musuhObject = null;
            for(int i=0;i< handler.object.size(); i++){
                if(handler.object.get(i).getId() == ID.Player){
                   playerObject = handler.object.get(i);
                }else if(handler.object.get(i).getId() == ID.Player2){
                   player2Object = handler.object.get(i);
                }
            }
            if(playerObject != null){
                for(int i=0;i< handler.object.size(); i++){
                    if(handler.object.get(i).getId() == ID.Item){
                        if(checkCollision(playerObject, handler.object.get(i), player2Object, musuhObject)){
                            playSound("/Eat.wav");
                            handler.removeObject(handler.object.get(i));
                            handler.addObject(new Items(acak(),acak(), ID.Item));
                            score = score + 25;
                            time = time + 2;
                            final_score = score + time;
                            break;
                        }
                    }
                }
            }else if(player2Object != null){
                for(int i=0;i< handler.object.size(); i++){
                    if(handler.object.get(i).getId() == ID.Item){
                        if(checkCollision(playerObject, handler.object.get(i), player2Object, musuhObject)){
                            playSound("/Eat.wav");
                            handler.removeObject(handler.object.get(i));
                            handler.addObject(new Items(acak(),acak(), ID.Item));
                            score = score + 25;
                            time = time + 2;
                            final_score = score + time;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static boolean checkCollision(GameObject player, GameObject item, GameObject player2, GameObject musuh){
        boolean result = false;
        
        int sizePlayer = 50;
        int sizePlayer2 = 50;
        int sizeItem = 20;
        int sizeMusuh = 30;
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int player2Left = player.x;
        int player2Right = player2.x + sizePlayer2;
        int player2Top = player2.y;
        int player2Bottom = player2.y + sizePlayer2;
        
        int itemLeft = item.x;
        int itemRight = item.x + sizeItem;
        int itemTop = item.y;
        int itemBottom = item.y + sizeItem;
        
//        int musuhLeft = musuh.x;
//        int musuhRight = musuh.x + sizeMusuh;
//        int musuhTop = musuh.y;
//        int musuhBottom = musuh.y + sizeMusuh;
        
        if((playerRight > itemLeft ) &&
        (playerLeft < itemRight) &&
        (itemBottom > playerTop) &&
        (itemTop < playerBottom)
        ){
            result = true;
        }
        
        if((player2Right > itemLeft ) &&
        (player2Left < itemRight) &&
        (itemBottom > player2Top) &&
        (itemTop < player2Bottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        
        g.setColor(Color.decode("#F1f3f3"));
        g.fillRect(0, 0, WIDTH, HEIGHT);
                
        
        
        if(gameState ==  STATE.Game){
            handler.render(g);
            
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), 20, 20);

            g.setColor(Color.BLACK);
            g.drawString("Time: " +Integer.toString(time), WIDTH-120, 20);
        }else{
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 3F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH/2 - 120, HEIGHT/2 - 30);

            currentFont = g.getFont();
            Font newScoreFont = currentFont.deriveFont(currentFont.getSize() * 0.5F);
            g.setFont(newScoreFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), WIDTH/2 - 50, HEIGHT/2 - 10);
            
            g.setColor(Color.BLACK);
            g.drawString("Press Space to Continue", WIDTH/2 - 100, HEIGHT/2 + 30);
        }
                
        g.dispose();
        bs.show();
    }
    
    public static int clamp(int var, int min, int max){
        if(var >= max){
            return var = max;
        }else if(var <= min){
            return var = min;
        }else{
            return var;
        }
    }
    
    public void close(){
        window.CloseWindow();
    }
    
    public void playSound(String filename){
        try {
            // Open an audio input stream.
            URL url = this.getClass().getResource(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } catch (LineUnavailableException e) {
           e.printStackTrace();
        }
    
    }
    
    public String getUsername(){
        return username;
    }
    public String getScore(){
        
        return Integer.toString(score);
    }
    
    public String getTime(){
        
        return Integer.toString(time);
    }
    
    public String getFinal(){
        
        return Integer.toString(final_score);
    }
}
