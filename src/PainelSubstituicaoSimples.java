import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class PainelSubstituicaoSimples {

    private final JTextArea textAreaArquivos;

    public PainelSubstituicaoSimples(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
    }

    public JPanel criarPainel() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        painelPrincipal.add(criarPainelInput(), BorderLayout.NORTH);
        painelPrincipal.add(criarScrollPaneArquivos(), BorderLayout.CENTER);

        return painelPrincipal;
    }

    private JPanel criarPainelInput() {
        JPanel painelEntrada = new JPanel(new GridBagLayout());
        painelEntrada.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Configurações de Renomeação",
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Adiciona os componentes no painel de entrada
        JTextField textPasta = criarLinhaEntrada("Pasta:", painelEntrada, gbc, 0);
        JTextField textOriginal = criarLinhaEntrada("Nome Original:", painelEntrada, gbc, 1);
        JTextField textNova = criarLinhaEntrada("Alterar Para:", painelEntrada, gbc, 2);

        // Botões
        JButton buttonSelecionar = TelaPrincipal.criarBotao("Selecionar Pasta");
        JButton buttonRenomear = TelaPrincipal.criarBotao("Renomear");

        adicionarBotao(buttonSelecionar, painelEntrada, gbc, 2, 0);
        adicionarBotao(buttonRenomear, painelEntrada, gbc, 2, 2);

        // Informações adicionais
        JLabel labelInfo = new JLabel("Lembrando que a aplicação respeita caracteres em caixa alta.");
        labelInfo.setForeground(Color.GRAY);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        painelEntrada.add(labelInfo, gbc);

        // Ações dos botões
        buttonSelecionar.addActionListener(e -> selecionarPasta(textPasta));
        buttonRenomear.addActionListener(e -> renomearArquivos(textPasta, textOriginal, textNova, painelEntrada));

        return painelEntrada;
    }

    private JTextField criarLinhaEntrada(String labelText, JPanel painel, GridBagConstraints gbc, int linha) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0;
        painel.add(label, gbc);

        JTextField textField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        painel.add(textField, gbc);

        return textField;
    }

    private void adicionarBotao(JButton botao, JPanel painel, GridBagConstraints gbc, int coluna, int linha) {
        gbc.gridx = coluna;
        gbc.gridy = linha;
        gbc.weightx = 0;
        painel.add(botao, gbc);
    }

    private JScrollPane criarScrollPaneArquivos() {
        textAreaArquivos.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaArquivos);
        scrollPane.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Arquivos na Pasta",
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)
        ));
        return scrollPane;
    }

    private void selecionarPasta(JTextField textPasta) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File pastaSelecionada = fileChooser.getSelectedFile();
            textPasta.setText(pastaSelecionada.getAbsolutePath());
            atualizarVisualizacaoArquivos(pastaSelecionada);
        }
    }

    private void renomearArquivos(JTextField textPasta, JTextField textOriginal, JTextField textNova, JPanel painelEntrada) {
        File diretorio = new File(textPasta.getText());
        if (!diretorio.exists() || !diretorio.isDirectory()) {
            exibirMensagem(painelEntrada, "Pasta inválida ou vazia.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String palavraAntiga = textOriginal.getText();
        String palavraNova = textNova.getText();
        File[] arquivos = diretorio.listFiles();

        if (arquivos != null) {
            boolean erroAoRenomear = false;
            for (File arquivo : arquivos) {
                if (arquivo.isFile()) {
                    String novoNome = arquivo.getName().replace(palavraAntiga, palavraNova);
                    if (!arquivo.renameTo(new File(diretorio, novoNome))) {
                        erroAoRenomear = true;
                    }
                }
            }

            String mensagem = erroAoRenomear ? "Renomeação concluída com erros." : "Renomeação concluída!";
            exibirMensagem(painelEntrada, mensagem, erroAoRenomear ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
            atualizarVisualizacaoArquivos(diretorio);
        }
    }

    private void atualizarVisualizacaoArquivos(File diretorio) {
        File[] arquivos = diretorio.listFiles();
        textAreaArquivos.setText("");

        if (arquivos != null) {
            Arrays.sort(arquivos, Comparator.comparing(File::getName));
            for (File arquivo : arquivos) {
                if (arquivo.isFile()) {
                    textAreaArquivos.append(arquivo.getName() + "\n");
                }
            }
        }
    }

    private void exibirMensagem(Component parent, String mensagem, int tipo) {
        JOptionPane.showMessageDialog(parent, mensagem, "Mensagem", tipo);
    }
}
