import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

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

        // Configuração da janela
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        // Painel para a lista de clientes
        listaClientesArea = new JTextArea();
        listaClientesArea.setEditable(false);
        listaClientesArea.setText(String.join("\n", clientes));
        listaClientesArea.setFont(new Font("Monospaced", Font.PLAIN, 14));  // Fonte monoespaçada para melhor visualização
        listaClientesArea.setBackground(Color.LIGHT_GRAY);  // Fundo leve para destacar
        JScrollPane scrollPane = new JScrollPane(listaClientesArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));  // Tamanho fixo da área de visualização
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
        botaoAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String novoCliente = campoCliente.getText().trim();
                if (novoCliente.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "O nome do cliente não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!clientes.contains(novoCliente)) {  // Garante que não haverá duplicatas
                    clientes.add(novoCliente);
                    campoCliente.setText("");  // Limpa o campo de texto
                    atualizarListaClientes();
                } else {
                    JOptionPane.showMessageDialog(frame, "Este cliente já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Ação do botão Remover
        botaoRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clienteRemover = campoCliente.getText().trim();
                if (clienteRemover.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser removido.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (clientes.contains(clienteRemover)) {
                    clientes.remove(clienteRemover);
                    campoCliente.setText("");  // Limpa o campo de texto
                    atualizarListaClientes();
                } else {
                    JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Ação do botão Editar
        botaoEditar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clienteAntigo = campoCliente.getText().trim();
                if (clienteAntigo.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Digite o nome do cliente a ser editado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (clientes.contains(clienteAntigo)) {
                    String novoNome = JOptionPane.showInputDialog(frame, "Digite o novo nome para o cliente:", clienteAntigo);
                    if (novoNome != null && !novoNome.trim().isEmpty() && !clientes.contains(novoNome.trim())) {
                        // Substitui o cliente na lista
                        int indice = clientes.indexOf(clienteAntigo);
                        clientes.set(indice, novoNome.trim());
                        campoCliente.setText("");  // Limpa o campo de texto
                        atualizarListaClientes();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Nome inválido ou já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Cliente não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
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

    // Método para atualizar a lista de clientes na JTextArea
    private void atualizarListaClientes() {
        listaClientesArea.setText(String.join("\n", clientes));
        // Atualiza também no painel de visualização principal
        textArea.setText(String.join("\n", clientes));
    }

    // Método para exibir a janela de edição
    public void mostrarEditor() {
        frame.setVisible(true);
    }
}
