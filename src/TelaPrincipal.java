import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TelaPrincipal {

    private static JFrame frame;
    private static JTextArea textAreaArquivos;
    private static Map<String, String> clientes;
    private static EditorListaClientes editorListaClientes;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConfiguracoesTema.carregarConfiguracao();
            mostrarSplashScreen();
            inicializarClientes();
            criarJanelaPrincipal();
        });
    }

    /**
     * Exibe a splash screen durante o carregamento da aplicação.
     */
    private static void mostrarSplashScreen() {
        JPanel splashPanel = criarPainelSplash();
        JWindow splashScreen = new JWindow();
        splashScreen.getContentPane().add(splashPanel);
        splashScreen.setSize(140, 80);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setVisible(true);

        // Simula o tempo de carregamento
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        splashScreen.dispose();
    }

    /**
     * Inicializa a lista de clientes e carrega do arquivo.
     */
    private static void inicializarClientes() {
        clientes = new HashMap<>();
        editorListaClientes = new EditorListaClientes(clientes, null, null);
        editorListaClientes.carregarClientesDoArquivo();
    }

    /**
     * Cria a janela principal da aplicação.
     */
    private static void criarJanelaPrincipal() {
        frame = new JFrame("Livre Escolha - Utilities");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1280, 720));

        // Configura o ícone da aplicação
        configurarIconeJanela();

        // Configura os componentes da janela
        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);

        GerenciadorAbas gerenciadorAbas = new GerenciadorAbas(textAreaArquivos);

        // Configuração do tema
        ConfiguracoesTema configuracoesTema = new ConfiguracoesTema();
        JDialog dialogConfiguracoes = criarDialogo(frame, "Configurações", configuracoesTema.getPainelConfiguracoes());

        JButton botaoTema = criarBotao("Temas");
        botaoTema.addActionListener(e -> dialogConfiguracoes.setVisible(true));

        JPanel painelRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelRodape.add(botaoTema);

        frame.add(painelRodape, BorderLayout.SOUTH);
        frame.add(gerenciadorAbas.getMainTabbedPane(), BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Cria o painel de splash screen.
     *
     * @return o painel configurado.
     */
    private static JPanel criarPainelSplash() {
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharBackgroundGradiente((Graphics2D) g);
            }
        };

        JLabel labelCarregando = new JLabel("Carregando...", SwingConstants.CENTER);
        labelCarregando.setFont(new Font("Open Sans", Font.BOLD, 14));
        labelCarregando.setForeground(Color.WHITE);

        splashPanel.setLayout(new BorderLayout());
        splashPanel.add(labelCarregando, BorderLayout.CENTER);

        return splashPanel;
    }

    /**
     * Desenha o fundo gradiente no painel de splash.
     */
    private static void desenharBackgroundGradiente(Graphics2D g2) {
        try {
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x4CAF50), 0, 80, new Color(0x2E8B57));
            g2.setPaint(gp);

            int[] xPoints = {0, 70, 140};
            int[] yPoints = {0, 80, 0};
            g2.fillPolygon(xPoints, yPoints, 3);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Configura o ícone da janela principal.
     */
    private static void configurarIconeJanela() {
        BufferedImage imagemOriginal = carregarImagem("/logo.png");
        if (imagemOriginal != null) {
            Image imagemEscalada = imagemOriginal.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            BufferedImage imagemRedimensionada = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = imagemRedimensionada.createGraphics();
            g2d.drawImage(imagemEscalada, 0, 0, null);
            g2d.dispose();
            frame.setIconImage(imagemRedimensionada);
        }
    }

    /**
     * Carrega uma imagem de um caminho específico.
     *
     * @param caminho o caminho do recurso da imagem.
     * @return a imagem carregada ou null em caso de falha.
     */
    public static BufferedImage carregarImagem(String caminho) {
        try {
            return ImageIO.read(TelaPrincipal.class.getResource(caminho));
        } catch (IOException e) {
            System.err.println("Erro ao carregar imagem: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cria um diálogo modal configurado.
     *
     * @param parent  a janela principal.
     * @param titulo  o título do diálogo.
     * @param conteudo o painel de conteúdo.
     * @return o diálogo configurado.
     */
    private static JDialog criarDialogo(JFrame parent, String titulo, JPanel conteudo) {
        JDialog dialogo = new JDialog(parent, titulo, true);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(parent);
        dialogo.getContentPane().add(conteudo);
        return dialogo;
    }

    /**
     * Cria um botão com as configurações padrão.
     *
     * @param texto o texto do botão.
     * @return o botão configurado.
     */
    public static JButton criarBotao(String texto) {
        JButton botao = new JButton(texto);
        botao.setPreferredSize(new Dimension(120, 24));
        botao.setMargin(new Insets(5, 10, 5, 10));
        return botao;
    }
}
