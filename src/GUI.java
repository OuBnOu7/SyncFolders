import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GUI extends JFrame implements ActionListener {
    private JTextField txtFieldSource;
    private JTextField txtFieldDestination;
    private JButton btnClone;
    private JButton btnSync;
    private JButton btnStopSync; // Nouveau bouton d'arrêt de la synchronisation
    private Thread syncThread; // Thread de synchronisation en cours

    public GUI() {
        setTitle("File Sync App");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create GUI components
        JLabel label1 = new JLabel("Dossier A: ");
        JLabel label2 = new JLabel("Dossier B: ");
        txtFieldSource = new JTextField(20);
        txtFieldDestination = new JTextField(20);
        btnClone = new JButton("Clone");
        btnSync = new JButton("Sync");
        btnStopSync = new JButton("Arrêter"); // Nouveau bouton d'arrêt de la synchronisation

        // Add components to layout

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(label1);
        panel.add(txtFieldSource);

        JPanel panelbis = new JPanel(new FlowLayout());
        panelbis.add(label2);
        panelbis.add(txtFieldDestination);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnClone);
        buttonPanel.add(btnSync);
        buttonPanel.add(btnStopSync); // Ajout du bouton d'arrêt de la synchronisation

        // Add panels to frame
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(panelbis, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set frame visibility
        setVisible(true);

        // Add listeners for buttons
        btnClone.addActionListener(this);
        btnSync.addActionListener(this);
        btnStopSync.addActionListener(this); // Ajout du listener pour le bouton d'arrêt de la synchronisation
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClone) {
            String sourcePath = txtFieldSource.getText();
            String destinationPath = txtFieldDestination.getText();

            File sourceFolder = new File(sourcePath);
            File destinationFolder = new File(destinationPath);

            try {
                Clone.cloneFolder(sourceFolder, destinationFolder);
                JOptionPane.showMessageDialog(this, "Le dossier A a été cloné dans le dossier B", "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors du clonage du dossier A dans le dossier B: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnSync) {
            String sourcePath = txtFieldSource.getText();
            String destinationPath = txtFieldDestination.getText();

            File sourceFolder = new File(sourcePath);
            File destinationFolder = new File(destinationPath);

            // Utiliser un thread distinct pour l'opération de synchronisation
            syncThread = new Thread(() -> {
                try {
                    Sync.syncFolders(sourceFolder, destinationFolder);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la synchronisation des dossiers A et B: " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            });
            syncThread.start(); // Démarrer le thread de synchronisation
        } else if (e.getSource() == btnStopSync) { // Boutond'arrêt de la synchronisation
            // Arrêter le thread de synchronisation s'il est en cours d'exécution
            if (syncThread != null && syncThread.isAlive()) {
                syncThread.interrupt(); // Interruption du thread de synchronisation
                JOptionPane.showMessageDialog(this, "La synchronisation a été arrêtée", "Arrêt de la synchronisation",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Aucune synchronisation en cours", "Aucune synchronisation en cours",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI());
    }
}


