package net.somewhatcity.boiler.core.sources;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DSPPipelinePanel extends JPanel {

    // -- Linker Bereich: Ausgänge --
    private DefaultListModel<String> outputsListModel = new DefaultListModel<>();
    private JList<String> outputsList = new JList<>(outputsListModel);

    // Textfeld und Button, um neue Ausgänge hinzuzufügen
    private JTextField newOutputNameField = new JTextField(10);
    private JButton addOutputButton = new JButton("Ausgang hinzufügen");

    // -- Rechter Bereich: DSP-Kette --
    private DefaultListModel<String> dspChainModel = new DefaultListModel<>();
    private JList<String> dspChainList = new JList<>(dspChainModel);

    // Auswahl für neue DSP-Elemente und Button zum Hinzufügen
    private JComboBox<String> dspComboBox = new JComboBox<>(new String[]{"Gain", "Lowpass", "Highpass", "Flanger"});
    private JButton addDSPButton = new JButton("DSP hinzufügen");

    // Buttons zum Verschieben der DSP-Elemente
    private JButton moveUpButton = new JButton("▲");     // Pfeil nach oben
    private JButton moveDownButton = new JButton("▼");   // Pfeil nach unten

    // -- Unterer Bereich: DSP-Konfiguration --
    private JPanel dspConfigPanel = new JPanel();
    private JLabel dspConfigLabel = new JLabel("Keine DSP ausgewählt");
    private JButton removeDSPButton = new JButton("DSP löschen");

    // -- Neuer Button: "Als PNG speichern"
    private JButton saveAsPNGButton = new JButton("Als PNG speichern");

    // -- Status-/Fehleranzeigen --
    private JLabel statusLabel = new JLabel(" "); // Zeigt Meldungen oder Fehler an

    // -- Datenmodell: pro Ausgang eine DSP-Kette --
    private Map<String, DefaultListModel<String>> dspChainsPerOutput = new HashMap<>();

    public DSPPipelinePanel() {
        super(new BorderLayout(5, 5));
        initUI();
        initListeners();
    }

    private void initUI() {
        // ---------------------
        // Linker Bereich (Ausgänge)
        // ---------------------
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Ausgänge"));

        // Ausgänge in ScrollPane packen
        JScrollPane outputsScrollPane = new JScrollPane(outputsList);
        leftPanel.add(outputsScrollPane, BorderLayout.CENTER);

        // Unten: Textfeld + Button zum Hinzufügen eines neuen Ausgangs
        JPanel addOutputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addOutputPanel.add(new JLabel("Neuer Ausgang:"));
        addOutputPanel.add(newOutputNameField);
        addOutputPanel.add(addOutputButton);

        leftPanel.add(addOutputPanel, BorderLayout.SOUTH);

        // ---------------------
        // Rechter Bereich (DSP-Kette)
        // ---------------------
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        // Oberer Teil: DSP-Kette
        JPanel rightTopPanel = new JPanel(new BorderLayout(5, 5));
        rightTopPanel.setBorder(BorderFactory.createTitledBorder("DSP-Kette"));

        // DSP-Auswahl und -Hinzufügen (ganz oben)
        JPanel dspAddPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dspAddPanel.add(new JLabel("Neues DSP-Element:"));
        dspAddPanel.add(dspComboBox);
        dspAddPanel.add(addDSPButton);

        rightTopPanel.add(dspAddPanel, BorderLayout.NORTH);

        // Mittlerer Teil: Liste der DSP-Elemente + Verschiebe-Buttons
        JPanel dspListPanel = new JPanel(new BorderLayout(5, 5));

        JScrollPane dspChainScrollPane = new JScrollPane(dspChainList);
        dspListPanel.add(dspChainScrollPane, BorderLayout.CENTER);

        JPanel moveButtonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        moveButtonsPanel.add(moveUpButton);
        moveButtonsPanel.add(moveDownButton);

        dspListPanel.add(moveButtonsPanel, BorderLayout.EAST);

        rightTopPanel.add(dspListPanel, BorderLayout.CENTER);

        // ---------------------
        // Unterer Teil: DSP-Konfiguration
        // ---------------------
        dspConfigPanel.setLayout(new BorderLayout());
        dspConfigPanel.setBorder(BorderFactory.createTitledBorder("DSP-Konfiguration"));

        // Label für aktuelle DSP-Konfiguration
        dspConfigPanel.add(dspConfigLabel, BorderLayout.CENTER);

        // Button "DSP löschen" am unteren Rand
        JPanel removeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        removeButtonPanel.add(removeDSPButton);
        dspConfigPanel.add(removeButtonPanel, BorderLayout.SOUTH);

        rightPanel.add(rightTopPanel, BorderLayout.CENTER);
        rightPanel.add(dspConfigPanel, BorderLayout.SOUTH);

        // ---------------------
        // Haupt-Panel füllen
        // ---------------------
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Statuspanel + "Als PNG speichern" Button
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);

        // Button für PNG-Speicherung rechts platzieren
        statusPanel.add(Box.createHorizontalStrut(50));
        statusPanel.add(saveAsPNGButton);

        add(statusPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        // Ausgang hinzufügen
        addOutputButton.addActionListener(e -> {
            String neuerAusgangName = newOutputNameField.getText().trim();
            if (neuerAusgangName.isEmpty()) {
                setStatusMessage("Bitte einen gültigen Namen eingeben!", true);
                return;
            }
            if (dspChainsPerOutput.containsKey(neuerAusgangName)) {
                setStatusMessage("Ein Ausgang mit diesem Namen existiert bereits!", true);
                return;
            }

            outputsListModel.addElement(neuerAusgangName);
            dspChainsPerOutput.put(neuerAusgangName, new DefaultListModel<>());
            setStatusMessage("Ausgang '" + neuerAusgangName + "' hinzugefügt.", false);

            newOutputNameField.setText("");
        });

        // Auswahl eines Ausgangs -> Kette wechseln
        outputsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedOutput = outputsList.getSelectedValue();
                if (selectedOutput != null) {
                    dspChainModel = dspChainsPerOutput.get(selectedOutput);
                    dspChainList.setModel(dspChainModel);
                    dspChainList.clearSelection();
                    dspConfigLabel.setText("Keine DSP ausgewählt");
                    setStatusMessage("Ausgang '" + selectedOutput + "' ausgewählt.", false);
                }
            }
        });

        // DSP hinzufügen
        addDSPButton.addActionListener(e -> {
            String selectedOutput = outputsList.getSelectedValue();
            if (selectedOutput == null) {
                setStatusMessage("Bitte zunächst einen Ausgang in der Liste wählen.", true);
                return;
            }
            String dspToAdd = (String) dspComboBox.getSelectedItem();
            if (dspToAdd != null) {
                dspChainModel.addElement(dspToAdd);
                setStatusMessage("DSP '" + dspToAdd + "' hinzugefügt.", false);
            }
        });

        // DSP-Element auswählen
        dspChainList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedDSP = dspChainList.getSelectedValue();
                if (selectedDSP != null) {
                    dspConfigLabel.setText("Konfiguration für: " + selectedDSP);
                    setStatusMessage("DSP '" + selectedDSP + "' zur Konfiguration ausgewählt.", false);
                } else {
                    dspConfigLabel.setText("Keine DSP ausgewählt");
                }
            }
        });

        // DSP löschen
        removeDSPButton.addActionListener(e -> {
            int selectedIndex = dspChainList.getSelectedIndex();
            if (selectedIndex < 0) {
                setStatusMessage("Bitte erst ein DSP-Element auswählen.", true);
                return;
            }
            String removed = dspChainModel.get(selectedIndex);
            dspChainModel.remove(selectedIndex);
            dspConfigLabel.setText("Keine DSP ausgewählt");
            setStatusMessage("DSP '" + removed + "' gelöscht.", false);
        });

        // DSP verschieben (hoch/runter)
        moveUpButton.addActionListener(e -> moveSelectedDSP(-1));
        moveDownButton.addActionListener(e -> moveSelectedDSP(1));

        // Button "Als PNG speichern"
        saveAsPNGButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Als PNG speichern");
            // Setze einen Standard-Dateinamen, falls gewünscht
            fileChooser.setSelectedFile(new File("dsp_pipeline.png"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File chosenFile = fileChooser.getSelectedFile();
                try {
                    saveAsPNG(chosenFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    setStatusMessage("Fehler beim Speichern: " + ioException.getMessage(), true);
                }
            }
        });
    }

    private void moveSelectedDSP(int direction) {
        int selectedIndex = dspChainList.getSelectedIndex();
        if (selectedIndex < 0) {
            setStatusMessage("Bitte erst ein DSP-Element auswählen.", true);
            return;
        }
        int newIndex = selectedIndex + direction;
        if (newIndex < 0 || newIndex >= dspChainModel.size()) {
            setStatusMessage("Das DSP-Element kann nicht weiter verschoben werden.", true);
            return;
        }
        String itemToMove = dspChainModel.get(selectedIndex);
        dspChainModel.remove(selectedIndex);
        dspChainModel.add(newIndex, itemToMove);

        dspChainList.setSelectedIndex(newIndex);
        setStatusMessage("DSP '" + itemToMove + "' verschoben.", false);
    }

    /**
     * Speichert das aktuelle UI als PNG in die angegebene Datei.
     *
     * @param file Ziel-PNG-Datei
     * @throws IOException wenn beim Schreiben ein Fehler auftritt
     */
    public void saveAsPNG(File file) throws IOException {
        // Aktuelle Größe ermitteln
        int w = getWidth();
        int h = getHeight();

        // Falls das Panel noch nie sichtbar war, kann w/h = 0 sein.
        // In dem Fall weichen wir auf die PreferredSize aus:
        if (w <= 0 || h <= 0) {
            Dimension pref = getPreferredSize();
            w = pref.width;
            h = pref.height;
        }

        // Bild erstellen
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Panel komplett in das Bild rendern
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h); // Hintergrund weiß
        printAll(g2);           // oder print(g2)
        g2.dispose();

        // PNG schreiben
        ImageIO.write(image, "png", file);

        setStatusMessage("Panel als PNG gespeichert: " + file.getAbsolutePath(), false);
    }

    private void setStatusMessage(String message, boolean isError) {
        if (isError) {
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        }
        statusLabel.setText(message);
    }

    /**
     * (Optional) Zum Testen kann man dieses Panel in einem JFrame anzeigen.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DSPPipelinePanel panel = new DSPPipelinePanel();

            // Give it a reasonable size:
            panel.setPreferredSize(new Dimension(800, 600));
            // Force the panel to have that size:
            panel.setSize(panel.getPreferredSize());

            // Run the layout so subcomponents can measure themselves:
            panel.doLayout();
            panel.validate();
            panel.revalidate();

            try {
                panel.saveAsPNG(new File("out2.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
