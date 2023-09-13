package de.devicez.agent.installer;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Slf4j
public class InstallerFrame extends JFrame {

    private final JTextField hostname;
    private final JTextField port;
    private DataCallback callback;

    public InstallerFrame() throws HeadlessException {
        setTitle("DeviceZ – Installer");
        setSize(300, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

        hostname = new JTextField();
        port = new JTextField();

        final JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panel);

        final JLabel instruction = new JLabel("Please configure server ...", SwingConstants.CENTER);
        instruction.setFont(new Font(instruction.getFont().getName(), Font.BOLD, instruction.getFont().getSize()));
        panel.add(instruction, BorderLayout.NORTH);

        final JPanel labels = new JPanel(new GridLayout(0, 1));
        final JPanel inputs = new JPanel(new GridLayout(0, 1));
        panel.add(labels, BorderLayout.WEST);
        panel.add(inputs, BorderLayout.CENTER);

        labels.add(new JLabel("Address: "));
        inputs.add(hostname);
        labels.add(new JLabel("Port: "));
        inputs.add(port);

        final JButton install = new JButton("Install");
        panel.add(install, BorderLayout.SOUTH);

        install.addActionListener(e -> {
            callback.apply(hostname.getText(), Integer.parseInt(port.getText()));
        });

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setCallback(final DataCallback callback) {
        this.callback = callback;
    }

    @FunctionalInterface
    public interface DataCallback {
        void apply(String hostname, int port);
    }
}
