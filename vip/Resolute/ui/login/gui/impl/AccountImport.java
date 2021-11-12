package vip.Resolute.ui.login.gui.impl;



import vip.Resolute.Resolute;
import vip.Resolute.ui.login.system.Account;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;

public class AccountImport extends JPanel implements ActionListener {

    public JButton openButton;
    private JFileChooser fc;

    public AccountImport() {
        fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        openButton = new JButton("Open a File...");
        openButton.addActionListener(this);
        add(openButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(AccountImport.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        final String[] arguments = line.split(":");

                        for (int i = 0; i < 2; i++)
                            arguments[i].replace(" ", "");

                        Resolute.getAccountManager().getAccounts()
                                .add(new Account(arguments[0], arguments[1], ""));
                    }
                    Resolute.getAccountManager().save();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "An error happened.", "ERROR", JOptionPane.INFORMATION_MESSAGE);
                    Resolute.getNotificationManager().add(new Notification("Error", "An error happened", 5000L, NotificationType.ERROR));
                }
            }
        }
    }

}
