import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfiguracoesTema {

    private static final String CONFIG_FILE = System.getenv("APPDATA") + "\\LE_Helper\\config.properties";
    private static final Tema DEFAULT_TEMA = Tema.FLAT_MATERIAL_LIGHTER;
    private static final Logger LOGGER = Logger.getLogger(ConfiguracoesTema.class.getName());

    private JPanel panelConfiguracoes;
    private ButtonGroup temaButtonGroup;
    private JLabel labelPreview;

    public ConfiguracoesTema() {
        panelConfiguracoes = new JPanel(new BorderLayout(10, 10));
        panelConfiguracoes.setBorder(new EmptyBorder(20, 20, 20, 20));
        configurarPainelConfiguracoes();
    }

    public JPanel getPainelConfiguracoes() {
        return panelConfiguracoes;
    }

    private void configurarPainelConfiguracoes() {
        JPanel temaDarkPanel = criarPainelDeTemas("Temas Escuros", true);
        JPanel temaLightPanel = criarPainelDeTemas("Temas Claros", false);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 10));
        panelCentro.add(temaDarkPanel);
        panelCentro.add(temaLightPanel);

        panelConfiguracoes.add(new JLabel("Selecione o tema:", JLabel.CENTER), BorderLayout.NORTH);
        panelConfiguracoes.add(panelCentro, BorderLayout.CENTER);

        labelPreview = criarLabelPreview();
        panelConfiguracoes.add(labelPreview, BorderLayout.SOUTH);

        JButton buttonSalvar = criarBotaoSalvar();
        panelConfiguracoes.add(buttonSalvar, BorderLayout.PAGE_END);
    }

    private JPanel criarPainelDeTemas(String titulo, boolean isDark) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder(titulo));

        if (temaButtonGroup == null) {
            temaButtonGroup = new ButtonGroup();
        }

        for (Tema tema : Tema.values()) {
            if (tema.isDark() == isDark) {
                JRadioButton radioButton = new JRadioButton(tema.getName());
                radioButton.setSelected(tema == DEFAULT_TEMA);

                radioButton.addActionListener(e -> atualizarTema(tema));
                temaButtonGroup.add(radioButton);
                panel.add(radioButton);
            }
        }

        return panel;
    }

    private JLabel criarLabelPreview() {
        JLabel label = new JLabel("Pré-visualização", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setPreferredSize(new Dimension(200, 60));
        return label;
    }

    private JButton criarBotaoSalvar() {
        JButton button = new JButton("Salvar");
        button.addActionListener(e -> salvarConfiguracao());
        return button;
    }

    private void atualizarTema(Tema tema) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(tema.getLookAndFeel());
                FlatLaf.updateUI();

                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                }

                LOGGER.info("Tema aplicado: " + tema.getName());
                labelPreview.setText("Pré-visualização: " + tema.getName());
            } catch (UnsupportedLookAndFeelException e) {
                LOGGER.log(Level.SEVERE, "Erro ao aplicar o tema: " + tema.getName(), e);
            }
        });
    }

    private void salvarConfiguracao() {
        File configFile = new File(CONFIG_FILE);

        try {
            File configDir = configFile.getParentFile();
            if (!configDir.exists() && !configDir.mkdirs()) {
                throw new IOException("Erro ao criar o diretório de configuração.");
            }

            try (OutputStream output = new FileOutputStream(configFile)) {
                Properties prop = new Properties();
                Tema temaSelecionado = getTemaSelecionado();
                prop.setProperty("tema", temaSelecionado.getName());
                prop.store(output, null);
                LOGGER.info("Tema salvo: " + temaSelecionado.getName());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar configuração do tema", e);
        }
    }

    public static void carregarConfiguracao() {
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            criarArquivoConfiguracaoPadrao(configFile);
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);

            String temaName = prop.getProperty("tema", DEFAULT_TEMA.getName());
            Tema tema = Tema.fromName(temaName);

            UIManager.setLookAndFeel(tema.getLookAndFeel());
            FlatLaf.updateUI();

            SwingUtilities.invokeLater(() -> {
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
            });

            LOGGER.info("Tema carregado: " + temaName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar configuração do tema", e);
        }
    }

    private static void criarArquivoConfiguracaoPadrao(File configFile) {
        try {
            if (configFile.getParentFile().mkdirs() || configFile.createNewFile()) {
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write("tema=" + DEFAULT_TEMA.getName());
                    LOGGER.info("Arquivo de configuração criado: " + configFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar arquivo de configuração padrão", e);
        }
    }

    private Tema getTemaSelecionado() {
        for (Enumeration<AbstractButton> buttons = temaButtonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return Tema.fromName(button.getText());
            }
        }
        return DEFAULT_TEMA;
    }

    public enum Tema {
        // Temas escuros
        FLAT_DARK_ORANGE("Flat Dark Orange", new FlatArcDarkOrangeIJTheme(), true),
        FLAT_DRACULA("Flat Dracula", new FlatDraculaIJTheme(), true),
        FLAT_CARBON("Flat Carbon", new FlatCarbonIJTheme(), true),
        FLAT_GITHUB_DARK("Flat GitHub Dark", new FlatGitHubDarkIJTheme(), true),
        FLAT_ONE_DARK("Flat One Dark", new FlatOneDarkIJTheme(), true),
        FLAT_GRADIENT("Flat Gradient", new FlatGradiantoMidnightBlueIJTheme(), true),

        // Temas claros
        FLAT_MAC_LIGHT("Flat Mac Light", new FlatMacLightLaf(), false),
        FLAT_LIGHT("Flat Light", new FlatIntelliJLaf(), false),
        FLAT_INTELLIJ("Flat IntelliJ Light", new FlatIntelliJLaf(), false),
        FLAT_MATERIAL_LIGHTER("Flat Material Lighter", new FlatMaterialLighterIJTheme(), false),
        FLAT_ATOM_ONE_LIGHT("Flat Atom One Light", new FlatAtomOneLightIJTheme(), false),
        FLAT_SAND("Flat Sand Light", new FlatLightLaf(), false);

        private final String name;
        private final LookAndFeel lookAndFeel;
        private final boolean isDark;

        Tema(String name, LookAndFeel lookAndFeel, boolean isDark) {
            this.name = name;
            this.lookAndFeel = lookAndFeel;
            this.isDark = isDark;
        }

        public String getName() {
            return name;
        }

        public LookAndFeel getLookAndFeel() {
            return lookAndFeel;
        }

        public boolean isDark() {
            return isDark;
        }

        public static Tema fromName(String name) {
            for (Tema tema : values()) {
                if (tema.getName().equalsIgnoreCase(name)) {
                    return tema;
                }
            }
            return DEFAULT_TEMA;
        }
    }
}
