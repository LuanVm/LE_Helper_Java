import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Files;

public class PainelOrganizacaoPastas {

    private Map<String, String> clientes;  // Usando Map para armazenar clientes
    private JTextArea textAreaArquivos;
    private Map<File, File> historicoOrganizacao = new HashMap<>();
    private File directory;
    private JLabel statusLabel;
    private boolean aguardandoConfirmacao = false;
    private JPanel opcoesPanel;
    private JTextPane infoTextPane;
    private JTextPane infoTextPane2;
    private JTextPane infoLabel;
    private JTextPane infoLabel2;
    private JCheckBox checkBoxCriarSubpastas;
    private JCheckBox checkBoxJuntarArquivos;

    private static final Logger LOGGER = Logger.getLogger(PainelOrganizacaoPastas.class.getName());

    public PainelOrganizacaoPastas() {
        this.clientes = carregarClientesDoArquivo();  // Carrega clientes diretamente do arquivo
        this.textAreaArquivos = new JTextArea();
        this.statusLabel = new JLabel("Pronto para organizar!");
        this.historicoOrganizacao = new HashMap<>();
    }

    public PainelOrganizacaoPastas(JTextArea textAreaArquivos) {
        this.clientes = carregarClientesDoArquivo(); // Carrega os clientes aqui também
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

    // Método para carregar os clientes a partir do arquivo .properties
    private Map<String, String> carregarClientesDoArquivo() {
        Map<String, String> clientesMap = new HashMap<>();
        File arquivoClientes = new File(EditorListaClientes.ARQUIVO_CLIENTES);  // Caminho para o arquivo clientes.properties

        try (InputStream input = new FileInputStream(arquivoClientes)) {
            Properties prop = new Properties();
            prop.load(input);  // Carrega as propriedades do arquivo

            // Adiciona todos os clientes ao mapa
            for (String cliente : prop.stringPropertyNames()) {
                clientesMap.put(cliente, prop.getProperty(cliente));  // Adiciona o cliente ao mapa
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar clientes do arquivo: " + EditorListaClientes.ARQUIVO_CLIENTES, ex);
        }

        return clientesMap;
    }

    // Exibe a pré-visualização dos arquivos e clientes
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

    // Atualiza a visualização dos arquivos na área de texto
    private void atualizarVisualizacaoArquivos(File pastaSelecionada) {
        if (pastaSelecionada == null || !pastaSelecionada.exists()) {
            textAreaArquivos.setText("Nenhuma pasta selecionada.");
            return;
        }

        StringBuilder conteudo = new StringBuilder();
        // Itera sobre os arquivos na pasta selecionada
        for (File file : Objects.requireNonNull(pastaSelecionada.listFiles())) {
            // Adiciona o nome de cada arquivo ao conteúdo
            conteudo.append(file.getName()).append("\n");
        }
        // Exibe os arquivos na área de texto
        textAreaArquivos.setText(conteudo.toString());
        statusLabel.setText("Arquivos na pasta atualizados.");
    }

    // Método para gerar uma pré-visualização dos arquivos na pasta
    private Map<String, List<File>> gerarPreVisualizacao(File directory) {
        Map<String, List<File>> clienteArquivos = new HashMap<>();
        File[] arquivos = directory.listFiles();

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.isFile()) {
                    // Extrai o nome do cliente baseado no nome do arquivo
                    String nomeCliente = extrairNomeCliente(arquivo.getName());
                    if (!nomeCliente.isEmpty()) {
                        // Agrupa os arquivos pelo nome do cliente
                        clienteArquivos.computeIfAbsent(nomeCliente, k -> new ArrayList<>()).add(arquivo);
                    }
                }
            }
        }
        return clienteArquivos;
    }

    // Extrai o nome do cliente com base no nome do arquivo
    private String extrairNomeCliente(String nomeArquivo) {
        if (clientes == null || clientes.isEmpty()) {
            return "";  // Retorna vazio se não houver clientes
        }
        for (Map.Entry<String, String> entry : clientes.entrySet()) {
            String cliente = entry.getKey();  // Chave do Map
            // Verifica se o nome do arquivo começa com o nome do cliente seguido de "_" ou espaço
            if (nomeArquivo.startsWith(cliente + "_") || nomeArquivo.startsWith(cliente + " ")) {
                return cliente;
            }
        }
        return "";  // Retorna vazio se nenhum cliente for encontrado
    }

    public JPanel criarPainel() {
        JPanel painelPrincipal  = new JPanel(new BorderLayout());
        painelPrincipal .setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Opções de organização (painel único)
        opcoesPanel = new JPanel(new GridBagLayout());

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
        buttonSelecionar.setToolTipText("Clique para selecionar a pasta que deseja organizar");
        inputPanel.add(buttonSelecionar, gbc);

        // Criação do botão de editar lista de clientes
        JButton buttonEditarClientes = new JButton("Editar Lista de Clientes");
        buttonEditarClientes.addActionListener(e -> {
            if (clientes == null) {
                clientes = new HashMap<>(); // Inicializa a lista se for nula
            }

            EditorListaClientes editor = new EditorListaClientes(clientes, textAreaArquivos, () -> {
                atualizarVisualizacaoArquivos(directory); // Atualiza a visualização
                statusLabel.setText("Lista de clientes atualizada.");
            });
            editor.mostrarEditor();
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
        painelPrincipal .add(inputPanel, BorderLayout.NORTH);
        painelPrincipal .add(painelInferior, BorderLayout.CENTER);

        // Painel para os botões e o status
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton buttonReverter = TelaPrincipal.criarBotao("Reverter");
        buttonPanel.add(buttonOrganizar);
        buttonPanel.add(buttonReverter);

        // Adiciona o JLabel de status abaixo dos botões
        buttonPanel.add(statusLabel);
        painelPrincipal .add(buttonPanel, BorderLayout.SOUTH);

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

        // Ação do botão Organizar
        buttonOrganizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = textPasta.getText();
                directory = new File(pasta);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();

                    // Validação da entrada
                    if (files == null || files.length == 0) {
                        statusLabel.setText("A pasta selecionada está vazia.");
                        return;
                    }

                    if (checkBoxCriarSubpastas.isSelected()) {
                        JProgressBar progressBar = new JProgressBar(0, files.length);
                        progressBar.setStringPainted(true);
                        organizarArquivos(directory, progressBar);
                        atualizarVisualizacaoArquivos(directory);
                        statusLabel.setText("Organização concluída com sucesso!");
                    } else if (checkBoxJuntarArquivos.isSelected()) {
                        // Juntar arquivos em subpastas sem confirmação
                        JProgressBar progressBar = new JProgressBar(0, files.length);
                        progressBar.setStringPainted(true);
                        juntarArquivosEmSubpastas(directory, progressBar);
                        atualizarVisualizacaoArquivos(directory);
                        statusLabel.setText("Arquivos juntados na pasta raiz com sucesso!");
                    } else {
                        // Gera pré-visualização sem fazer alterações
                        Map<String, List<File>> preVisualizacao = gerarPreVisualizacao(directory);
                        exibirPreVisualizacao(preVisualizacao, textAreaArquivos);

                        // Atualiza os botões
                        buttonOrganizar.setText("Confirmar Alt.");
                        buttonReverter.setText("Cancelar");
                        aguardandoConfirmacao = true;
                    }
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

                    // Reseta os botões para o estado original após reversão
                    buttonOrganizar.setText("Organizar");
                    buttonReverter.setText("Reverter");
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

        return painelPrincipal ;
    }

    // Método para organizar arquivos em diretórios
    private void organizarArquivos(File directory, JProgressBar progressBar) {
        Map<String, String> clientesMap = carregarClientesDoArquivo();
        Map<String, List<File>> clienteArquivos = gerarPreVisualizacao(directory);
        historicoOrganizacao.clear();

        int totalArquivos = clienteArquivos.values().stream().mapToInt(List::size).sum();
        progressBar.setMaximum(totalArquivos); // Inicializa o progresso
        int pastasCriadas = 0, arquivosMovidos = 0, arquivosIgnorados = 0;

        for (Map.Entry<String, List<File>> entry : clienteArquivos.entrySet()) {
            String nomePasta = entry.getKey();

            if (clientesMap.containsKey(nomePasta)) {
                File novaPasta = new File(directory, nomePasta);

                if (!novaPasta.exists() && novaPasta.mkdir()) {
                    pastasCriadas++;
                }

                for (File arquivo : entry.getValue()) {
                    if (arquivo.isHidden() || "desktop.ini".equalsIgnoreCase(arquivo.getName())) {
                        arquivosIgnorados++;
                        continue;
                    }

                    try {
                        File novoArquivo = new File(novaPasta, arquivo.getName());
                        if (novoArquivo.exists()) {
                            String novoNome = gerarNomeUnico(novoArquivo);
                            novoArquivo = new File(novaPasta, novoNome);
                        }
                        Files.move(arquivo.toPath(), novoArquivo.toPath());
                        historicoOrganizacao.put(novoArquivo, arquivo);
                        arquivosMovidos++;
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Erro ao mover o arquivo: " + arquivo.getName(), e);
                        arquivosIgnorados++;
                    }
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            } else {
                arquivosIgnorados += entry.getValue().size();
                progressBar.setValue(progressBar.getValue() + entry.getValue().size());
            }
        }

        statusLabel.setText("Organização completa! Pastas criadas: " + pastasCriadas +
                ", Arquivos movidos: " + arquivosMovidos +
                ", Arquivos ignorados: " + arquivosIgnorados);
    }

    // Gera um nome único para evitar conflitos
    private String gerarNomeUnico(File arquivo) {
        String nomeBase = arquivo.getName();
        String extensao = "";
        int pontoIndex = nomeBase.lastIndexOf(".");
        if (pontoIndex > 0) {
            extensao = nomeBase.substring(pontoIndex);
            nomeBase = nomeBase.substring(0, pontoIndex);
        }
        int contador = 1;
        File novoArquivo;
        do {
            novoArquivo = new File(arquivo.getParent(), nomeBase + "_" + contador + extensao);
            contador++;
        } while (novoArquivo.exists());
        return novoArquivo.getName();
    }

    // Método para reverter a última organização realizada
    private void reverterUltimaOrganizacao(File directory) {
        for (Map.Entry<File, File> entry : historicoOrganizacao.entrySet()) {
            File arquivoNovo = entry.getKey();
            File arquivoAntigo = entry.getValue();

            try {
                // Move o arquivo de volta para o local original
                Files.move(arquivoNovo.toPath(), arquivoAntigo.toPath());
                // Remove a pasta se ela estiver vazia após o movimento do arquivo
                arquivoNovo.getParentFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Atualiza o status após a reversão
        statusLabel.setText("Reversão concluída com sucesso!");
        // Atualiza a visualização dos arquivos após a reversão
        atualizarVisualizacaoArquivos(directory);
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

