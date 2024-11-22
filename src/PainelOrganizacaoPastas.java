import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PainelOrganizacaoPastas {

    // Lista de clientes predefinidos
    public static final String ARQUIVO_CLIENTES = "clientes.txt";
    public static final List<String> CLIENTES = new ArrayList<>();


    private JTextArea textAreaArquivos;
    private Map<File, File> historicoOrganizacao = new HashMap<>();
    private File directory;
    private int numSecoesNomeCliente = 2;
    private JLabel statusLabel;
    private boolean aguardandoConfirmacao = false;

    // Constantes para o número de seções
    private static final int MIN_SECOES_NOME_CLIENTE = 1;
    private static final int MAX_SECOES_NOME_CLIENTE = 3;

    private static final Logger LOGGER = Logger.getLogger(PainelOrganizacaoPastas.class.getName());

    private JCheckBox checkBoxCriarSubpastas;
    private JCheckBox checkBoxJuntarArquivos;

    // Componentes para ambas as opções
    private JPanel opcoesPanel;
    private JTextPane infoTextPane;
    private JTextPane infoTextPane2;
    private JTextPane infoLabel;
    private JTextPane infoLabel2;

    private Map<String, List<File>> gerarPreVisualizacao(File directory, List<String> excecoes) {
        Map<String, List<File>> clienteArquivos = new HashMap<>();

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String nomeArquivo = file.getName();
                String nomeCliente = extrairNomeCliente(nomeArquivo, excecoes);

                if (!nomeCliente.isEmpty()) {
                    clienteArquivos.computeIfAbsent(nomeCliente, k -> new ArrayList<>()).add(file);
                }
            }
        }

        return clienteArquivos;
    }


    private void exibirPreVisualizacao(Map<String, List<File>> preVisualizacao, JTextArea textArea) {
        textArea.setText("");
        for (Map.Entry<String, List<File>> entry : preVisualizacao.entrySet()) {
            String nomePasta = entry.getKey();
            List<File> arquivosNaPasta = entry.getValue();

            textArea.append("[" + nomePasta + "]\n");
            for (File arquivo : arquivosNaPasta) {
                textArea.append("  - " + arquivo.getName() + "\n");
            }
        }
    }

    public PainelOrganizacaoPastas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
        this.statusLabel = new JLabel("Pronto para organizar!");
        this.infoLabel = new JTextPane();
        this.infoLabel.setEditable(false);
        this.infoLabel.setOpaque(false);
        this.infoLabel.setContentType("text/html");
        this.infoLabel2 = new JTextPane();
        this.infoLabel2.setEditable(false);
        this.infoLabel2.setOpaque(false);
        this.infoLabel2.setContentType("text/html");
    }

    public JPanel criarPainel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Opções de organização (painel único)
        opcoesPanel = new JPanel(new GridBagLayout());
        //opcoesPanel.setBorder(new TitledBorder("Opções de Organização"));

        GridBagConstraints gbcConfig = new GridBagConstraints();
        gbcConfig.insets = new Insets(5, 5, 5, 5);
        gbcConfig.fill = GridBagConstraints.HORIZONTAL;
        gbcConfig.anchor = GridBagConstraints.WEST;

        // Painel para os campos de entrada (GridBagLayout)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Configurações de Organização",
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)
        ));

        // Pasta
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        JLabel labelPasta = new JLabel("Pasta:");
        inputPanel.add(labelPasta, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.9;
        JTextField textPasta = new JTextField(20);
        inputPanel.add(textPasta, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton buttonSelecionar = TelaPrincipal.criarBotao("Selecionar Pasta");
        buttonSelecionar.setToolTipText("Clique para selecionar a pasta que deseja organizar"); // Adiciona tooltip
        inputPanel.add(buttonSelecionar, gbc);
        JTextField textExcecoes = new JTextField(0);

        // Criação do botão de editar lista de clientes
        JButton buttonEditarClientes = new JButton("Editar Lista de Clientes");
        buttonEditarClientes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Instancia o editor de clientes e exibe a janela
                EditorListaClientes editor = new EditorListaClientes(CLIENTES, textAreaArquivos);
                editor.mostrarEditor();
            }
        });

        // Adiciona o botão ao painel de opções
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(buttonEditarClientes, gbc);

        // Checkboxes (mantém na mesma linha)
        gbcConfig.gridx = 0;
        gbcConfig.gridy = 2;
        gbcConfig.gridwidth = 3;
        checkBoxCriarSubpastas = new JCheckBox("Criar e organizar em subpastas", true);
        //
         //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         //
         //
        inputPanel.add(checkBoxCriarSubpastas, gbcConfig);

        gbcConfig.gridx = 0;
        gbcConfig.gridy = 3;
        gbcConfig.gridwidth = 2;
        checkBoxJuntarArquivos = new JCheckBox("Buscar e juntar arquivos em subpastas");
        inputPanel.add(checkBoxJuntarArquivos, gbcConfig);

        // Componentes para a opção "Criação e organização em Subpastas"
        gbcConfig.gridx = 0;
        gbcConfig.gridy = 1;
        gbcConfig.gridwidth = 2;
        gbcConfig.weighty = 1.0;

        gbcConfig.weightx = 1.0;
        gbcConfig.gridx = 0;
        gbcConfig.gridy = 1;
        infoTextPane = new JTextPane();
        infoTextPane.setEditable(false);
        infoTextPane.setOpaque(false);
        infoTextPane.setContentType("text/html");
        infoTextPane.setText("<html>A organização das pastas é realizada conforme banco inserido no app, para mais instruções, consultar T.I</html>");
        infoTextPane.setForeground(Color.GRAY);
        inputPanel.add(infoTextPane, gbcConfig);

        // Novo texto informativo para a opção "Busca e junção de arquivos em subpastas"
        gbcConfig.gridx = 0;
        gbcConfig.gridy = 3;
        infoTextPane2 = new JTextPane();
        infoTextPane2.setEditable(false);
        infoTextPane2.setOpaque(false);
        infoTextPane2.setContentType("text/html");
        infoTextPane2.setForeground(Color.GRAY);
        inputPanel.add(infoTextPane2, gbcConfig);

        // Adiciona o painel de opções à aba
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        inputPanel.add(opcoesPanel, gbc);

        // Botão Organizar
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton buttonOrganizar = TelaPrincipal.criarBotao("Organizar");
        inputPanel.add(buttonOrganizar, gbc);


        // Painel para exibir a área de texto dos arquivos e a pré-visualização
        JPanel painelInferior = new JPanel(new GridBagLayout()); // GridBagLayout para melhor controle do layout

        // Área de visualização de arquivos
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // Distribui o espaço horizontalmente
        gbc.weighty = 1.0; // Expande verticalmente para preencher o espaço
        gbc.fill = GridBagConstraints.BOTH;

        JPanel painelArquivos = new JPanel(new BorderLayout());
        painelArquivos.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "Arquivos na Pasta", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12)));

        textAreaArquivos = new JTextArea(10, 40);
        textAreaArquivos.setEditable(false);

        JScrollPane scrollPaneArquivos = new JScrollPane(textAreaArquivos);
        painelArquivos.add(scrollPaneArquivos, BorderLayout.CENTER);

        painelInferior.add(painelArquivos, gbc);

        // Painel para a pré-visualização
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel painelPreVisualizacao = new JPanel(new BorderLayout());
        painelPreVisualizacao.setBorder(new TitledBorder("Pré-visualização da Organização"));
        JTextArea textAreaPreVisualizacao = new JTextArea(10, 40);
        textAreaPreVisualizacao.setEditable(false);
        JScrollPane scrollPanePreVisualizacao = new JScrollPane(textAreaPreVisualizacao);
        painelPreVisualizacao.add(scrollPanePreVisualizacao, BorderLayout.CENTER);
        painelInferior.add(painelPreVisualizacao, gbc);

        // Adiciona os painéis à aba
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(painelInferior, BorderLayout.CENTER);

        // Painel para os botões e o status
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonReverter = TelaPrincipal.criarBotao("Reverter");
        buttonPanel.add(buttonOrganizar);
        buttonPanel.add(buttonReverter);

        // Adiciona o JLabel de status abaixo dos botões
        buttonPanel.add(statusLabel);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Ação do botão Selecionar
        buttonSelecionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    textPasta.setText(selectedFile.getAbsolutePath());
                    atualizarVisualizacaoArquivos(selectedFile);
                    statusLabel.setText("Pronto para organizar!"); // Limpa o status ao selecionar uma nova pasta
                }
            }
        });

        // Ação do botão Organizar (adaptada para as novas funcionalidades)
        buttonOrganizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                String excecoesStr = textExcecoes.getText();
                List<String> excecoes = Arrays.asList(excecoesStr.split(","));

                directory = new File(pasta);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();

                    // Validação da entrada
                    if (files == null || files.length == 0) {
                        statusLabel.setText("A pasta selecionada está vazia.");
                        return;
                    }

                    if (aguardandoConfirmacao) {
                        // Confirmação recebida, prossegue com a organização
                        JProgressBar progressBar = new JProgressBar(0, files.length);
                        progressBar.setStringPainted(true);

                        if (checkBoxCriarSubpastas.isSelected()) {
                            organizarArquivos(directory, excecoes, progressBar);
                        } else if (checkBoxJuntarArquivos.isSelected()) {
                            juntarArquivosEmSubpastas(directory, progressBar);
                        }

                        buttonOrganizar.setText("Organizar");
                        buttonReverter.setText("Reverter");
                        aguardandoConfirmacao = false;
                        statusLabel.setText("Organização concluída com sucesso!");
                        atualizarVisualizacaoArquivos(directory); // Atualiza a exibição dos arquivos organizados

                    }

                    // Primeira vez que o botão é clicado, gera a pré-visualização e pede confirmação
                    Map<String, List<File>> preVisualizacao = gerarPreVisualizacao(directory, excecoes);
                    exibirPreVisualizacao(preVisualizacao, textAreaArquivos); // Exibe no painel de arquivos

                    buttonOrganizar.setText("Confirmar Alt.");
                    buttonReverter.setText("Cancelar");
                    aguardandoConfirmacao = true;

                } else {
                    statusLabel.setText("Pasta não encontrada ou inválida.");
                }
            }
        });


        // Ação do botão Reverter
        buttonReverter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (aguardandoConfirmacao) {
                    // Cancela a organização pendente
                    buttonOrganizar.setText("Organizar");
                    buttonReverter.setText("Reverter");
                    aguardandoConfirmacao = false;

                    // Restaura a visualização original dos arquivos
                    atualizarVisualizacaoArquivos(directory);

                    // Limpa a pré-visualização
                    textAreaPreVisualizacao.setText("");

                    statusLabel.setText("Organização cancelada.");
                } else {
                    // Reverte a última organização
                    if (directory != null && directory.exists()) {
                        if (checkBoxCriarSubpastas.isSelected()) {
                            reverterUltimaOrganizacao(directory);
                        } else if (checkBoxJuntarArquivos.isSelected()) {
                            // Lógica para reverter a junção de arquivos (implementar conforme necessário)
                            statusLabel.setText("Reversão da junção de arquivos não implementada.");
                        }

                        // Atualiza a visualização de arquivos
                        atualizarVisualizacaoArquivos(directory);

                        // Atualiza a mensagem de status
                        statusLabel.setText("Reversão concluída com sucesso!");

                        // Limpa a pré-visualização
                        textAreaPreVisualizacao.setText("");
                    } else {
                        statusLabel.setText("Nenhuma organização realizada para reverter.");
                    }
                }
            }
        });

        infoLabel.setVisible(true);
        infoLabel2.setVisible(false);

        // Adiciona um listener para garantir que apenas uma checkbox seja selecionada por vez
        // e controlar a visibilidade dos componentes
        ActionListener checkBoxListener = e -> {
            JCheckBox source = (JCheckBox) e.getSource();
            if (source.isSelected()) {
                if (source == checkBoxCriarSubpastas) {
                    checkBoxJuntarArquivos.setSelected(false);
                    infoLabel.setVisible(true);
                    infoLabel2.setVisible(false);
                    infoTextPane.setVisible(true);
                    infoTextPane2.setVisible(false);
                } else {
                    checkBoxCriarSubpastas.setSelected(false);
                    infoLabel.setVisible(false);
                    infoLabel2.setVisible(true);
                    infoTextPane.setVisible(false);
                    infoTextPane2.setVisible(true);
                }
                opcoesPanel.revalidate(); // Atualiza o layout do painel de opções
                opcoesPanel.repaint();
            }
        };
        checkBoxCriarSubpastas.addActionListener(checkBoxListener);
        checkBoxJuntarArquivos.addActionListener(checkBoxListener);

        return panel;
    }

    // Metodo para organizar arquivos em diretórios
    private void organizarArquivos(File directory, List<String> excecoes, JProgressBar progressBar) {
        Map<String, List<File>> clienteArquivos = gerarPreVisualizacao(directory, excecoes);
        historicoOrganizacao.clear();
        int pastasCriadas = 0;
        int arquivosMovidos = 0;
        int arquivosIgnorados = 0;

        for (Map.Entry<String, List<File>> entry : clienteArquivos.entrySet()) {
            String nomePasta = entry.getKey();
            File novaPasta = new File(directory, nomePasta);

            try {
                if (!novaPasta.exists() && !novaPasta.mkdir()) { // Verifica se a pasta já existe ou se pode ser criada
                    throw new IOException("Erro ao criar a pasta: " + nomePasta);
                }

                for (File arquivo : entry.getValue()) {
                    try {
                        File novoArquivo = new File(novaPasta, arquivo.getName());
                        Files.move(arquivo.toPath(), novoArquivo.toPath());
                        historicoOrganizacao.put(novoArquivo, arquivo);
                        arquivosMovidos++;
                    } catch (IOException e) {
                        // Tratamento de erro mais específico
                        String mensagemErro = "Erro ao mover o arquivo: " + arquivo.getName() + " para " + novaPasta.getAbsolutePath() + "\n" +
                                "Motivo: " + e.getMessage();
                        JOptionPane.showMessageDialog(null, mensagemErro, "Erro ao Mover Arquivo", JOptionPane.ERROR_MESSAGE);
                        LOGGER.log(Level.SEVERE, mensagemErro, e); // Log do erro
                        arquivosIgnorados++;
                    }
                }
            } catch (IOException ex) {
                // Tratamento de erro ao criar a pasta
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Erro ao Criar Pasta", JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "Erro ao criar a pasta: " + nomePasta, ex); // Log do erro
            }

            progressBar.setValue(progressBar.getValue() + entry.getValue().size());
        }

        // Atualiza a mensagem de status com o resultado da organização
        statusLabel.setText("Organização completa! Pastas criadas: " + pastasCriadas +
                ", Arquivos movidos: " + arquivosMovidos +
                ", Arquivos ignorados: " + arquivosIgnorados);
    }

    // Metodo para reverter a última organização realizada
    private void reverterUltimaOrganizacao(File directory) {
        for (Map.Entry<File, File> entry : historicoOrganizacao.entrySet()) {
            File arquivoNovo = entry.getKey();
            File arquivoAntigo = entry.getValue();

            try {
                Files.move(arquivoNovo.toPath(), arquivoAntigo.toPath());
                arquivoNovo.getParentFile().delete(); // Deleta a pasta se ela estiver vazia após mover o arquivo
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Atualiza a mensagem de status após a reversão
        statusLabel.setText("Reversão concluída com sucesso!");
        atualizarVisualizacaoArquivos(directory); // Atualiza a visualização após a reversão
    }

    // Atualiza a visualização dos arquivos na área de texto
    private void atualizarVisualizacaoArquivos(File pastaSelecionada) {
        StringBuilder conteudo = new StringBuilder();
        for (File file : Objects.requireNonNull(pastaSelecionada.listFiles())) {
            conteudo.append(file.getName()).append("\n");
        }
        textAreaArquivos.setText(conteudo.toString());

        // Adiciona uma mensagem de status indicando que a atualização foi concluída
        statusLabel.setText("Arquivos na pasta atualizados.");
    }

    // Extrai o nome do cliente com base no padrão estabelecido
    private String extrairNomeCliente(String nomeArquivo, List<String> excecoes) {
        // Verifica se o nome do arquivo inicia com um cliente conhecido e tem um delimitador em seguida
        for (String cliente : CLIENTES) {
            if (nomeArquivo.startsWith(cliente + "_") || nomeArquivo.startsWith(cliente + " ")) {
                return cliente;
            }
        }
        // Caso contrário, retorna vazio
        return "";
    }

    private void juntarArquivosEmSubpastas(File directory, JProgressBar progressBar) {
        List<File> allFiles = new ArrayList<>();
        Set<File> directoriesToDelete = new HashSet<>();
        collectFilesRecursively(directory, allFiles, directoriesToDelete);

        for (File file : allFiles) {
            try {
                if (file.isFile()) {
                    Path source = file.toPath();
                    Path target = new File(directory, file.getName()).toPath();
                    Files.move(source, target);
                    progressBar.setValue(progressBar.getValue() + 1);

                    directoriesToDelete.add(file.getParentFile());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erro ao mover o arquivo: " + file.getName(), e);
            }
        }

        for (File dir : directoriesToDelete) {
            try {
                if (dir.isDirectory() && dir.listFiles().length == 0) {
                    Files.delete(dir.toPath());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erro ao excluir a pasta: " + dir.getAbsolutePath(), e);
            }
        }

        atualizarVisualizacaoArquivos(directory);
        statusLabel.setText("Arquivos juntados na pasta raiz com sucesso!");
    }

    // Metodo auxiliar para percorrer subpastas recursivamente
    private void collectFilesRecursively(File dir, List<File> allFiles, Set<File> directoriesToDelete) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    allFiles.add(file);
                } else if (file.isDirectory()) {
                    collectFilesRecursively(file, allFiles, directoriesToDelete);
                    directoriesToDelete.add(file);
                }
            }
        }
    }
}