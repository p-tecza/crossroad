package approach.threads;

import approach.threads.traffic.BlockedRoadHelper;
import approach.threads.traffic.TrafficController;
import approach.threads.utils.DriveDirection;
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

        screen = new AnimationScreen(Spawner.getCrossroadLock());

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
        BlockedRoadHelper blockedRoadHelper = new BlockedRoadHelper(trafficController);
        spawnRandomCars(32, trafficController, blockedRoadHelper);
    }


    private void spawnRandomCars(int num, TrafficController trafficController, BlockedRoadHelper blockedRoadHelper){
        new Thread(() -> {
            int n = num;
            int idIt = 0;
            while (n > 0) {
                Location loc = Spawner.getLocationFromRandomLocation();
                if (loc == null) {
                    try {
//                        System.out.println("LOC JEST NULL, czekamy");
//                        Thread.sleep(new Random().nextInt(1000, 2000));
                        Thread.sleep(400);
//                        System.out.println("LECIMY");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
//                System.out.println("Respię samochodzik nr." + (num - n+1));
                Car c = new Car(screen, loc, trafficController, blockedRoadHelper, idIt++);
                screen.appendCar(c);
                Thread t = new Thread(c);
                t.start();
                n--;
                try {
                    Thread.sleep(num - 4 <= n ? 125 : 600);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

//                Car c2 = new Car(screen, Location.LEFT, trafficController, DriveDirection.STRAIGHT);
//                screen.appendCar(c2);
//                Thread t2 = new Thread(c2);
//                t2.start();
            }
        }).start();
    }

}


