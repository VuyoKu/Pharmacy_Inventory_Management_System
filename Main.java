// main enrty point for the PIMS system
import javax.swing.*;

import utilities.UITheme;
import authenication.LoginFrame;

public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalLook();
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}