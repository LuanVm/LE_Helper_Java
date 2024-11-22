import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorListaClientes {

    private List<String> clientes;
    private JTextArea textArea;
    private JFrame frame;
    private JTextArea listaClientesArea;
    private JTextField campoCliente;


    public EditorListaClientes(List<String> clientes, JTextArea textArea) {
        this.clientes = clientes;
        this.textArea = textArea;
        this.frame = new JFrame("Editar Lista de Clientes");

        carregarClientes(); // Carregar a lista de clientes do arquivo

        // Configuração da janela
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        // Painel para a lista de clientes
        listaClientesArea = new JTextArea();
        listaClientesArea.setEditable(false);
        listaClientesArea.setText(String.join("\n", clientes));
        JScrollPane scrollPane = new JScrollPane(listaClientesArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Painel para edição de cliente
        JPanel painelEdicao = new JPanel(new FlowLayout());
        JLabel labelCliente = new JLabel("Nome do Cliente:");
        campoCliente = new JTextField(20);
        painelEdicao.add(labelCliente);
        painelEdicao.add(campoCliente);

        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout());
        JButton botaoAdicionar = new JButton("Adicionar Cliente");
        JButton botaoRemover = new JButton("Remover Cliente");
        JButton botaoEditar = new JButton("Editar Cliente");

        // Ajusta todos os botões para terem o mesmo tamanho
        Dimension buttonSize = new Dimension(150, 30);
        botaoAdicionar.setPreferredSize(buttonSize);
        botaoRemover.setPreferredSize(buttonSize);
        botaoEditar.setPreferredSize(buttonSize);

        // Ação do botão Adicionar
        botaoAdicionar.addActionListener(e -> {
            String novoCliente = campoCliente.getText().trim();
            if (novoCliente.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "O nome do cliente não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!clientes.contains(novoCliente)) {
                clientes.add(novoCliente); // Adiciona o cliente à lista
                Collections.sort(clientes); // Ordena a lista de clientes em ordem alfabética
                campoCliente.setText(""); // Limpa o campo de texto
                atualizarListaClientes(); // Atualiza a exibição da lista
                salvarClientes(); // Salva após cada alteração
            } else {
                JOptionPane.showMessageDialog(frame, "Este cliente já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ação do botão Remover
        botaoRemover.addActionListener(e -> {
            String clienteRemover = campoCliente.getText().trim();
            if (clienteRemover.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser removido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (clientes.contains(clienteRemover)) {
                clientes.remove(clienteRemover);
                campoCliente.setText("");
                atualizarListaClientes();
                salvarClientes(); // Salva após cada alteração
            } else {
                JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ação do botão Editar
        botaoEditar.addActionListener(e -> {
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
                    salvarClientes(); // Salva após cada alteração
                } else {
                    JOptionPane.showMessageDialog(frame, "Nome inválido ou já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Adiciona os botões ao painel de botões
        panelBotoes.add(botaoAdicionar);
        panelBotoes.add(botaoRemover);
        panelBotoes.add(botaoEditar);

        // Adiciona o painel de edição e o painel de botões
        frame.add(painelEdicao, BorderLayout.NORTH);
        frame.add(panelBotoes, BorderLayout.SOUTH);
    }

    private void atualizarListaClientes() {
        listaClientesArea.setText(String.join("\n", clientes));
        textArea.setText(String.join("\n", clientes));
    }

    private void carregarClientes() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PainelOrganizacaoPastas.ARQUIVO_CLIENTES))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                clientes.add(linha.trim());
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar clientes: " + e.getMessage());
        }
    }

    private void salvarClientes() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PainelOrganizacaoPastas.ARQUIVO_CLIENTES))) {
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
    }
}
