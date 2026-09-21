// main enrty point for the PIMS system
import javax.swing.*;

import utilities.UITheme;
import authenication.LoginFrame;

public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalLook();
        // Launch the login window on the EDT(Event Dispatch Thread), reposnsible for handling GUI events in Swing  
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}