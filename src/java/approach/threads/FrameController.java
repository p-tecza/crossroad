package approach.threads;

import approach.threads.traffic.TrafficController;
import approach.threads.utils.Location;
import approach.threads.utils.Spawner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Math.*;


public class FrameController {

    private JFrame frame;
    private Queue<Car> cars;
    private Queue<ScheduledFuture<?>> bulletsHandles;
    private ScheduledExecutorService animationExecutor;
    private AnimationScreen screen;
    private int nBullets;
    private boolean animationPaused;

    public FrameController() {
        frame = new JFrame("Crossroad");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.GREEN);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLayout(new GridLayout());

        screen = new AnimationScreen(Spawner.getCarObjectsLock());

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {

            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent event) {

            }

            @Override
            public void windowDeiconified(WindowEvent event) {

            }
        });
        frame.add(screen);

        frame.pack();
        frame.setVisible(true);
        screen.setGraphics();
        TrafficController trafficController = new TrafficController();
        spawnRandomCars(32, trafficController);
    }


    private void spawnRandomCars(int num, TrafficController trafficController){
        new Thread(() -> {
            int n = num;
            while (n > 0) {
                Location loc = Spawner.getLocationFromRandomLocation();
                if (loc == null) {
                    try {
//                        System.out.println("LOC JEST NULL, czekamy");
//                        Thread.sleep(new Random().nextInt(1000, 2000));
                        Thread.sleep(800);
//                        System.out.println("LECIMY");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                System.out.println("Respię samochodzik nr." + (num - n+1));
                Car c = new Car(screen, loc, trafficController);
                screen.appendCar(c);
                Thread t = new Thread(c);
                t.start();
                n--;
                try {
                    Thread.sleep(num - 4 <= n ? 250 : 1200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}


