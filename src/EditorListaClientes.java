import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorListaClientes {

    private List<String> clientes; // Referência à lista compartilhada
    private List<String> clientesBackup; // Backup da lista original
    private JTextArea textArea;
    private JFrame frame;
    private JTextArea listaClientesArea;
    private JTextField campoCliente;

    private static final String ARQUIVO_CLIENTES = ".my-app-config/clientes.txt";

    public EditorListaClientes(List<String> clientes, JTextArea textArea) {
        this.clientes = clientes != null ? clientes : new ArrayList<>();
        this.textArea = textArea;
        this.frame = new JFrame("Editar Lista de Clientes");

        carregarClientes();

        // Criar um backup da lista original para manter o estado
        clientesBackup = new ArrayList<>(clientes);

        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        listaClientesArea = new JTextArea();
        listaClientesArea.setEditable(false);
        listaClientesArea.setText(String.join("\n", clientes));
        JScrollPane scrollPane = new JScrollPane(listaClientesArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel painelEdicao = new JPanel(new FlowLayout());
        JLabel labelCliente = new JLabel("Nome do Cliente:");
        campoCliente = new JTextField(20);
        painelEdicao.add(labelCliente);
        painelEdicao.add(campoCliente);

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

        // Barra de menu com a opção de salvar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem itemSalvar = new JMenuItem("Salvar");

        itemSalvar.addActionListener(e -> salvarClientesManual());
        menuArquivo.add(itemSalvar);
        menuBar.add(menuArquivo);
        frame.setJMenuBar(menuBar);

        frame.add(painelEdicao, BorderLayout.NORTH);
        frame.add(panelBotoes, BorderLayout.SOUTH);

        // Definir ação ao fechar a janela
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
                    clientes.addAll(clientesBackup);
                    atualizarListaClientes();
                }
                frame.dispose();
            }
        });
    }

    private void adicionarCliente() {
        String novoCliente = campoCliente.getText().trim();
        if (novoCliente.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "O nome do cliente não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!clientes.contains(novoCliente)) {
            clientes.add(novoCliente);
            Collections.sort(clientes);
            campoCliente.setText("");
            atualizarListaClientes();
        } else {
            JOptionPane.showMessageDialog(frame, "Este cliente já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerCliente() {
        String clienteRemover = campoCliente.getText().trim();
        if (clienteRemover.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser removido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clientes.contains(clienteRemover)) {
            clientes.remove(clienteRemover);
            campoCliente.setText("");
            atualizarListaClientes();
        } else {
            JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarCliente() {
        String clienteAntigo = campoCliente.getText().trim();
        if (clienteAntigo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser editado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clientes.contains(clienteAntigo)) {
            String novoNome = JOptionPane.showInputDialog(frame, "Digite o novo nome para o cliente:", clienteAntigo);
            if (novoNome != null && !novoNome.trim().isEmpty() && !clientes.contains(novoNome.trim())) {
                int indice = clientes.indexOf(clienteAntigo);
                clientes.set(indice, novoNome.trim());
                campoCliente.setText("");
                atualizarListaClientes();
            } else {
                JOptionPane.showMessageDialog(frame, "Nome inválido ou já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarListaClientes() {
        listaClientesArea.setText(String.join("\n", clientes));
        textArea.setText(String.join("\n", clientes));

        // Garante que a rolagem volte ao topo
        listaClientesArea.setCaretPosition(0);
    }

    private void salvarClientesManual() {
        salvarClientes();
        JOptionPane.showMessageDialog(frame, "Clientes salvos com sucesso!", "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    private void carregarClientes() {
        String diretorioUsuario = System.getProperty("user.home");
        File pastaConfig = new File(diretorioUsuario, ".my-app-config");
        File arquivoClientes = new File(pastaConfig, "clientes.txt");

        // Cria a pasta, se necessário
        if (!pastaConfig.exists() && !pastaConfig.mkdirs()) {
            System.err.println("Erro ao criar o diretório de configuração.");
            return;
        }

        // Se o arquivo não existir, tenta copiá-lo do resources
        if (!arquivoClientes.exists()) {
            try (InputStream resource = getClass().getResourceAsStream("/clientes.txt")) {
                if (resource != null) {
                    Files.copy(resource, arquivoClientes.toPath());
                } else {
                    System.err.println("Arquivo de clientes padrão não encontrado em resources.");
                }
            } catch (IOException e) {
                System.err.println("Erro ao copiar arquivo de clientes padrão: " + e.getMessage());
            }
        }

        // Carrega os clientes do arquivo
        if (arquivoClientes.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivoClientes))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    String cliente = linha.trim();
                    if (!cliente.isEmpty() && !clientes.contains(cliente)) {
                        clientes.add(cliente);
                    }
                }
                Collections.sort(clientes);
            } catch (IOException e) {
                System.err.println("Erro ao carregar clientes: " + e.getMessage());
            }
        }
    }

    private void salvarClientes() {
        String diretorioUsuario = System.getProperty("user.home");
        File pastaConfig = new File(diretorioUsuario, ".my-app-config");
        File arquivoClientes = new File(pastaConfig, "clientes.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoClientes))) {
            for (String cliente : clientes) {
                writer.write(cliente);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar clientes: " + e.getMessage());
        }
    }

    public void mostrarEditor() {
        frame.setVisible(true);

        // Garante que a rolagem esteja no topo ao abrir a janela
        listaClientesArea.setCaretPosition(0);
    }
}
