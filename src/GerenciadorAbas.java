import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GerenciadorAbas {

    // Constantes de título e opções
    private static final String ABA_NOMENCLATURA_ARQUIVOS = "Nomenclatura de Arquivos";
    private static final String ABA_GERENCIAMENTO_PLANILHAS = "Gerenciamento de Planilhas";
    private static final String ABA_ORGANIZACAO_PASTAS = "Organização de Pastas";

    private static final Color TAB_SELECTED_COLOR = new Color(0xEB5E28);
    private static final Font TAB_FONT = new Font("Arial", Font.PLAIN, 14);

    private JTabbedPane mainTabbedPane;
    private JTextArea textAreaArquivos;

    // Construtor principal
    public GerenciadorAbas(JTextArea textAreaArquivos) {
        this.textAreaArquivos = textAreaArquivos;
        configurarUI();
        criarAbasPrincipais();
    }

    // Configuração inicial da interface
    private void configurarUI() {
        UIManager.put("TabbedPane.selected", TAB_SELECTED_COLOR);
        UIManager.put("TabbedPane.font", TAB_FONT);
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
    }

    // Criação das abas principais
    private void criarAbasPrincipais() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        mainTabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        adicionarAba(ABA_NOMENCLATURA_ARQUIVOS, criarPainelNomenclatura());
        adicionarAba(ABA_GERENCIAMENTO_PLANILHAS, criarPainelGerenciamentoPlanilhas());
        adicionarAba(ABA_ORGANIZACAO_PASTAS, criarPainelOrganizacaoPastas());
    }

    // Adiciona uma aba ao painel principal
    private void adicionarAba(String titulo, JPanel conteudo) {
        mainTabbedPane.addTab(titulo, conteudo);
    }

    // Painel de Nomenclatura de Arquivos
    private JPanel criarPainelNomenclatura() {
        JTabbedPane subTabbedPane = new JTabbedPane();
        subTabbedPane.addTab("Substituição Simples", new PainelSubstituicaoSimples(textAreaArquivos).criarPainel());
        subTabbedPane.addTab("Renomear e Ordenar", new PainelRenomearOrdenar(textAreaArquivos).criarPainel());

        return criarPainelComBorda(subTabbedPane);
    }

    // Painel de Gerenciamento de Planilhas
    private JPanel criarPainelGerenciamentoPlanilhas() {
        JTabbedPane subTabbedPane = new JTabbedPane();
        subTabbedPane.addTab("Mesclagem de Planilhas", new PainelMesclaPlanilha(textAreaArquivos).criarPainel());
        subTabbedPane.addTab("Processamento Agitel", new PainelProcessamentoAgitel(textAreaArquivos).criarPainel());

        return criarPainelComBorda(subTabbedPane);
    }

    // Painel de Organização de Pastas
    private JPanel criarPainelOrganizacaoPastas() {
        JPanel painelOrganizacao = new PainelOrganizacaoPastas(textAreaArquivos).criarPainel();
        return criarPainelComBorda(painelOrganizacao);
    }

    // Utilitário para criar painéis com borda padrão
    private JPanel criarPainelComBorda(JComponent conteudo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY)
        ));
        panel.add(conteudo, BorderLayout.CENTER);
        return panel;
    }

    // Getter para o painel principal
    public JTabbedPane getMainTabbedPane() {
        return mainTabbedPane;
    }
}
