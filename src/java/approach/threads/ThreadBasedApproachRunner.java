package approach.threads;

import javax.swing.*;

public class ThreadBasedApproachRunner {

    public static void main(String[] args){
        System.out.println("ThreadBasedApproach");
        SwingUtilities.invokeLater(FrameController::new);
    }

}
