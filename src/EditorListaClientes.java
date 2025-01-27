import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditorListaClientes {

    public Map<String, String> clientes; // Usando Map para armazenar clientes como chave-valor
    private Map<String, String> clientesBackup; // Backup da lista original
    private JTextArea textArea;
    private JFrame frame;
    private JTextArea listaClientesArea;
    private JTextField campoCliente;
    public static final String ARQUIVO_CLIENTES = System.getenv("APPDATA") + "\\LE_Helper\\clientes.properties"; // Caminho do arquivo de propriedades

    private Runnable onClientesAtualizados;

    // Construtor para inicializar o editor com a lista de clientes
    public EditorListaClientes(Map<String, String> clientes, JTextArea textArea, Runnable onClientesAtualizados) {
        this.clientes = (clientes != null) ? clientes : new HashMap<>();
        this.textArea = textArea;
        this.onClientesAtualizados = onClientesAtualizados;

        // Backup da lista original
        clientesBackup = new HashMap<>(this.clientes);

        // Verifica e cria o arquivo de propriedades se necessário
        verificarECriarArquivoClientes();

        // Inicializar a janela de edição
        inicializarJanela();

        // Carregar os clientes do arquivo
        carregarClientesDoArquivo();
    }

    // Metodo para verificar e criar o arquivo de clientes se ele não existir
    private static void verificarECriarArquivoClientes() {
        File arquivoClientes = new File(ARQUIVO_CLIENTES);
        File pastaConfig = arquivoClientes.getParentFile();

        // Verifica se o diretório existe; se não, cria
        if (!pastaConfig.exists() && !pastaConfig.mkdirs()) {
            System.err.println("Erro ao criar o diretório de configuração em: " + pastaConfig.getAbsolutePath());
            return;
        }

        // Verifica se o arquivo clientes.properties existe
        if (!arquivoClientes.exists()) {
            try (InputStream recurso = EditorListaClientes.class.getResourceAsStream("/clientes.properties")) {
                if (recurso != null) {
                    // Copia o arquivo de recursos para o diretório de configuração
                    Files.copy(recurso, arquivoClientes.toPath());
                    System.out.println("Arquivo clientes.properties copiado dos recursos para: " + arquivoClientes.getAbsolutePath());
                } else {
                    // Cria o arquivo vazio se o recurso não for encontrado
                    if (arquivoClientes.createNewFile()) {
                        System.out.println("Arquivo clientes.properties criado vazio em: " + arquivoClientes.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao criar ou copiar o arquivo clientes.properties: " + e.getMessage());
            }
        } else {
            System.out.println("Arquivo clientes.properties encontrado em: " + arquivoClientes.getAbsolutePath());
        }
    }


    // Inicializa a interface gráfica
    private void inicializarJanela() {
        frame = new JFrame("Editar Lista de Clientes");
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        // Área de texto para exibir a lista de clientes
        listaClientesArea = new JTextArea();
        listaClientesArea.setEditable(false);
        listaClientesArea.setText(formatarClientesParaExibicao());
        JScrollPane scrollPane = new JScrollPane(listaClientesArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Painel de edição para adicionar/remover clientes
        JPanel painelEdicao = new JPanel(new FlowLayout());
        JLabel labelCliente = new JLabel("Nome do Cliente:");
        campoCliente = new JTextField(20);
        painelEdicao.add(labelCliente);
        painelEdicao.add(campoCliente);

        // Botões de adicionar/remover/editar
        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton botaoAdicionar = new JButton("Adicionar Cliente");
        JButton botaoRemover = new JButton("Remover Cliente");
        JButton botaoEditar = new JButton("Editar Cliente");

        Dimension buttonSize = new Dimension(150, 30);
        botaoAdicionar.setPreferredSize(buttonSize);
        botaoRemover.setPreferredSize(buttonSize);
        botaoEditar.setPreferredSize(buttonSize);

        botaoAdicionar.addActionListener(e -> adicionarCliente());
        botaoRemover.addActionListener(e -> removerCliente());
        botaoEditar.addActionListener(e -> editarCliente());

        panelBotoes.add(botaoAdicionar);
        panelBotoes.add(botaoRemover);
        panelBotoes.add(botaoEditar);

        // Adiciona painel de edição e botões à janela
        frame.add(painelEdicao, BorderLayout.NORTH);
        frame.add(panelBotoes, BorderLayout.SOUTH);

        // Configurar o menu para salvar alterações
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem itemSalvar = new JMenuItem("Salvar");

        itemSalvar.addActionListener(e -> salvarClientes());
        menuArquivo.add(itemSalvar);
        menuBar.add(menuArquivo);
        frame.setJMenuBar(menuBar);

        // Configurar ação ao fechar a janela
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int resposta = JOptionPane.showConfirmDialog(frame, "Deseja salvar as alterações antes de sair?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (resposta == JOptionPane.YES_OPTION) {
                    salvarClientes();
                } else {
                    // Restaura a lista de clientes para o estado original
                    clientes.clear();
                    clientes.putAll(clientesBackup);
                    atualizarListaClientes();
                }
                frame.dispose();
            }
        });
    }

    // Método para formatar a lista de clientes para exibição (garante que a lista seja ordenada)
    private String formatarClientesParaExibicao() {
        // Ordena as chaves (nomes dos clientes) em ordem alfabética
        List<String> listaOrdenada = new ArrayList<>(clientes.keySet());
        Collections.sort(listaOrdenada);

        StringBuilder sb = new StringBuilder();
        for (String cliente : listaOrdenada) {
            sb.append(cliente).append("\n");
        }
        return sb.toString();
    }

    // Método para carregar os clientes do arquivo de propriedades
    public void carregarClientesDoArquivo() {
        File arquivoClientes = new File(ARQUIVO_CLIENTES);
        try (InputStream input = new FileInputStream(arquivoClientes)) {
            Properties prop = new Properties();
            prop.load(input); // Carrega as propriedades do arquivo

            // Adiciona todos os clientes ao mapa
            for (String cliente : prop.stringPropertyNames()) {
                clientes.put(cliente, prop.getProperty(cliente));
            }

            // Atualiza a lista de clientes na interface (se houver uma vinculada)
            if (textArea != null) {
                atualizarListaClientes();
            }
        } catch (IOException ex) {
            System.err.println("Erro ao carregar os clientes do arquivo: " + ARQUIVO_CLIENTES);
            ex.printStackTrace();
        }
    }

    // Método para adicionar um cliente
    private void adicionarCliente() {
        String novoCliente = campoCliente.getText().trim();
        if (novoCliente.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "O nome do cliente não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!clientes.containsKey(novoCliente)) {
            clientes.put(novoCliente, novoCliente); // Utiliza o nome como chave e valor
            campoCliente.setText("");
            atualizarListaClientes();
        } else {
            JOptionPane.showMessageDialog(frame, "Este cliente já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para remover um cliente
    private void removerCliente() {
        String clienteRemover = campoCliente.getText().trim();
        if (clienteRemover.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser removido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clientes.containsKey(clienteRemover)) {
            clientes.remove(clienteRemover);
            campoCliente.setText("");
            atualizarListaClientes();
        } else {
            JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para editar um cliente
    private void editarCliente() {
        String clienteAntigo = campoCliente.getText().trim();
        if (clienteAntigo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser editado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clientes.containsKey(clienteAntigo)) {
            String novoNome = JOptionPane.showInputDialog(frame, "Digite o novo nome para o cliente:", clienteAntigo);
            if (novoNome != null && !novoNome.trim().isEmpty() && !clientes.containsKey(novoNome.trim())) {
                clientes.put(novoNome.trim(), novoNome.trim());
                clientes.remove(clienteAntigo);
                campoCliente.setText("");
                atualizarListaClientes();
            } else {
                JOptionPane.showMessageDialog(frame, "Nome inválido ou já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para atualizar a lista de clientes na interface
    private void atualizarListaClientes() {
        listaClientesArea.setText(formatarClientesParaExibicao());
    }

    // Método para salvar os clientes no arquivo de propriedades
    private void salvarClientes() {
        File arquivoClientes = new File(ARQUIVO_CLIENTES);
        File pastaConfig = arquivoClientes.getParentFile();

        if (!pastaConfig.exists() && !pastaConfig.mkdirs()) {
            System.err.println("Erro ao criar o diretório de configuração em: " + pastaConfig.getAbsolutePath());
            return;
        }

        try (FileOutputStream fileOut = new FileOutputStream(arquivoClientes)) {
            Properties prop = new Properties();
            for (Map.Entry<String, String> cliente : clientes.entrySet()) {
                prop.setProperty(cliente.getKey(), cliente.getValue());
            }
            prop.store(fileOut, "Lista de Clientes");
            System.out.println("Clientes salvos com sucesso em: " + arquivoClientes.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao salvar clientes: " + e.getMessage());
        }
    }

    // Exibe o editor
    public void mostrarEditor() {
        frame.setVisible(true);
        listaClientesArea.setCaretPosition(0); // Garante que a rolagem vá para o topo
    }
}
