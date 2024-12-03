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
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.*;


public class FrameController {

    private JFrame frame;
    private final AnimationScreen screen;

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


    private void spawnRandomCars(int num, TrafficController trafficController, BlockedRoadHelper blockedRoadHelper) {
        new Thread(() -> {
            int n = num;
            int idIt = 0;
            while (n > 0) {
                Location loc = Spawner.getLocationFromRandomLocation();
                if (loc == null) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Car c = new Car(screen, loc, trafficController, blockedRoadHelper, idIt++);
                screen.appendCar(c);
                Thread t = new Thread(c);
                t.start();
                n--;
                try {
                    Thread.sleep(num - 4 <= n ? 150 : 400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}


