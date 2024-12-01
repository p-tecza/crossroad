package approach.threads;

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

        screen = new AnimationScreen();


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
        spawnRandomCars(1);
    }


    private void spawnRandomCars(int n){
        while(n-- > 0){
            Car c = new Car(screen);
            screen.appendCar(c);
            new Thread(c).start();
        }
    }

}


