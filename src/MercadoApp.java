import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.temporal.ChronoField;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

public class MercadoApp extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");
	private static final DateTimeFormatter DATA_ENTRADA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);
	private static final DateTimeFormatter DATA_CURTA = new DateTimeFormatterBuilder()
			.appendPattern("dd/MM/")
			.appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
			.toFormatter().withResolverStyle(ResolverStyle.STRICT);
	private static final Color INK = new Color(24, 31, 39);
	private static final Color MUTED = new Color(103, 114, 124);
	private static final Color BLUE = new Color(0, 111, 190);
	private static final Color PAPER = new Color(243, 246, 248);
	private static final Color LINE = new Color(220, 227, 232);

	private final ProdutoRepository repository = new ProdutoRepository();
	private final List<Produto> produtos = new ArrayList<>();
	private final ProdutoTableModel tableModel = new ProdutoTableModel(produtos);
	private final JTable tabela = new JTable(tableModel);
	private final TableRowSorter<ProdutoTableModel> sorter = new TableRowSorter<>(tableModel);
	private final JTextField busca = new JTextField();
	private final JLabel quantidadeResumo = new JLabel("0 produtos");
	private final JLabel valorResumo = new JLabel("R$ 0,00");
	private final JLabel status = new JLabel("Pronto");

	private MercadoApp() {
		setTitle("Mercado Tem d'Tudo | Produtos");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(820, 560));
		setSize(1040, 700);
		setLocationRelativeTo(null);
		getContentPane().setBackground(PAPER);
		setLayout(new BorderLayout());

		carregarProdutos();
		add(criarCabecalho(), BorderLayout.NORTH);
		add(criarConteudo(), BorderLayout.CENTER);
		add(criarRodape(), BorderLayout.SOUTH);
		configurarTeclas();
		atualizarResumo();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			configurarAparencia();
			new MercadoApp().setVisible(true);
		});
	}

	private static void configurarAparencia() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
			UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 14));
		}
	}

	private JPanel criarCabecalho() {
		JPanel header = new JPanel(new BorderLayout(16, 0));
		header.setBackground(INK);
		header.setBorder(BorderFactory.createEmptyBorder(24, 30, 22, 30));

		JPanel titulo = new JPanel(new BorderLayout(0, 5));
		titulo.setOpaque(false);
		JLabel nome = new JLabel("Mercado Tem d'Tudo");
		nome.setForeground(Color.WHITE);
		nome.setFont(new Font("SansSerif", Font.BOLD, 25));
		JLabel descricao = new JLabel("Cadastro e consulta de produtos");
		descricao.setForeground(new Color(190, 203, 213));
		descricao.setFont(new Font("SansSerif", Font.PLAIN, 14));
		titulo.add(nome, BorderLayout.NORTH);
		titulo.add(descricao, BorderLayout.SOUTH);

		JPanel resumo = new JPanel(new BorderLayout(0, 4));
		resumo.setOpaque(false);
		quantidadeResumo.setForeground(Color.WHITE);
		quantidadeResumo.setFont(new Font("SansSerif", Font.BOLD, 16));
		valorResumo.setForeground(new Color(143, 205, 250));
		valorResumo.setFont(new Font("SansSerif", Font.BOLD, 20));
		resumo.add(quantidadeResumo, BorderLayout.NORTH);
		resumo.add(valorResumo, BorderLayout.SOUTH);
		header.add(titulo, BorderLayout.WEST);
		header.add(resumo, BorderLayout.EAST);
		return header;
	}

	private JPanel criarConteudo() {
		JPanel conteudo = new JPanel(new BorderLayout(0, 16));
		conteudo.setBackground(PAPER);
		conteudo.setBorder(BorderFactory.createEmptyBorder(22, 30, 18, 30));

		JPanel barra = new JPanel(new BorderLayout(12, 0));
		barra.setOpaque(false);
		busca.setFont(new Font("SansSerif", Font.PLAIN, 15));
		busca.putClientProperty("JTextField.placeholderText", "Buscar por nome, categoria, data ou código");
		busca.getAccessibleContext().setAccessibleName("Buscar produtos");
		busca.setToolTipText("Filtra a tabela conforme você digita");
		busca.setPreferredSize(new Dimension(300, 42));
		busca.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
				BorderFactory.createEmptyBorder(0, 11, 0, 11)));
		busca.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent event) { filtrar(); }
			@Override public void removeUpdate(DocumentEvent event) { filtrar(); }
			@Override public void changedUpdate(DocumentEvent event) { filtrar(); }
		});

		JPanel acoes = new JPanel();
		acoes.setOpaque(false);
		acoes.setLayout(new javax.swing.BoxLayout(acoes, javax.swing.BoxLayout.X_AXIS));
		JButton novo = botao("Novo produto", true);
		JButton editar = botao("Editar", false);
		JButton remover = botao("Remover", false);
		JButton exportar = botao("Exportar CSV", false);
		novo.addActionListener(event -> adicionarProduto());
		editar.addActionListener(event -> editarProduto());
		remover.addActionListener(event -> removerProduto());
		exportar.addActionListener(event -> exportarCsv());
		acoes.add(novo);
		acoes.add(javax.swing.Box.createHorizontalStrut(8));
		acoes.add(editar);
		acoes.add(javax.swing.Box.createHorizontalStrut(8));
		acoes.add(remover);
		acoes.add(javax.swing.Box.createHorizontalStrut(8));
		acoes.add(exportar);
		barra.add(busca, BorderLayout.CENTER);
		barra.add(acoes, BorderLayout.EAST);

		configurarTabela();
		JScrollPane scroll = new JScrollPane(tabela);
		scroll.setBorder(BorderFactory.createLineBorder(LINE));
		scroll.getViewport().setBackground(Color.WHITE);
		conteudo.add(barra, BorderLayout.NORTH);
		conteudo.add(scroll, BorderLayout.CENTER);
		return conteudo;
	}

	private JPanel criarRodape() {
		JPanel rodape = new JPanel(new BorderLayout());
		rodape.setBackground(Color.WHITE);
		rodape.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LINE),
				BorderFactory.createEmptyBorder(11, 30, 11, 30)));
		status.setForeground(MUTED);
		status.setFont(new Font("SansSerif", Font.PLAIN, 13));
		rodape.add(status, BorderLayout.WEST);
		return rodape;
	}

	private JButton botao(String texto, boolean primario) {
		JButton button = new JButton(texto);
		button.setFont(new Font("SansSerif", Font.BOLD, 13));
		button.setFocusPainted(true);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(primario ? BLUE : LINE),
				BorderFactory.createEmptyBorder(9, 13, 9, 13)));
		if (primario) {
			button.setBackground(BLUE);
			button.setForeground(Color.WHITE);
		} else {
			button.setBackground(Color.WHITE);
			button.setForeground(INK);
		}
		return button;
	}

	private void configurarTabela() {
		tabela.setRowSorter(sorter);
		tabela.setFillsViewportHeight(true);
		tabela.setRowHeight(42);
		tabela.setShowVerticalLines(false);
		tabela.setGridColor(new Color(232, 237, 240));
		tabela.setSelectionBackground(new Color(220, 239, 252));
		tabela.setSelectionForeground(INK);
		tabela.setFont(new Font("SansSerif", Font.PLAIN, 14));
		tabela.setAutoCreateRowSorter(false);
		tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
		tabela.getTableHeader().setBackground(new Color(246, 248, 249));
		tabela.getTableHeader().setForeground(INK);
		tabela.getTableHeader().setPreferredSize(new Dimension(0, 42));
		tabela.getAccessibleContext().setAccessibleName("Lista de produtos");
		tabela.getColumnModel().getColumn(0).setMaxWidth(80);
		tabela.getColumnModel().getColumn(1).setMaxWidth(130);
		tabela.getColumnModel().getColumn(4).setMaxWidth(150);
		tabela.getColumnModel().getColumn(4).setPreferredWidth(130);
		tabela.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			{
				setHorizontalAlignment(RIGHT);
			}
			@Override protected void setValue(Object value) {
				setText(value instanceof BigDecimal ? NumberFormat.getCurrencyInstance(BRASIL).format(value) : "");
			}
		});
		tabela.setDefaultEditor(Object.class, null);
		tabela.getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				status.setText(tabela.getSelectedRow() < 0 ? "Pronto" : "Produto selecionado");
			}
		});
		tabela.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseClicked(java.awt.event.MouseEvent event) {
				if (event.getClickCount() == 2 && tabela.getSelectedRow() >= 0) editarProduto();
			}
		});
	}

	private void configurarTeclas() {
		getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control N"), "novoProduto");
		getRootPane().getActionMap().put("novoProduto", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent event) { adicionarProduto(); }
		});
		getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "buscar");
		getRootPane().getActionMap().put("buscar", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent event) { busca.requestFocusInWindow(); busca.selectAll(); }
		});
		getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "removerProduto");
		getRootPane().getActionMap().put("removerProduto", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent event) {
				if (tabela.isFocusOwner()) removerProduto();
			}
		});
	}

	private void carregarProdutos() {
		try {
			produtos.addAll(repository.carregar());
			produtos.sort(Comparator.comparingInt(Produto::getCodigo));
		} catch (IOException exception) {
			JOptionPane.showMessageDialog(this,
					"Não foi possível carregar produtos.bin. O arquivo foi mantido intacto.\n\n" + exception.getMessage(),
					"Falha ao carregar", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void filtrar() {
		String texto = busca.getText().trim();
		if (texto.isEmpty()) {
			sorter.setRowFilter(null);
		} else {
			sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
		}
		status.setText(sorter.getViewRowCount() + " de " + produtos.size() + " produtos");
	}

	private void adicionarProduto() {
		ProdutoFormulario form = new ProdutoFormulario(this, null);
		if (form.mostrar()) {
			int codigo = produtos.stream().mapToInt(Produto::getCodigo).max().orElse(0) + 1;
			produtos.add(new Produto(codigo, form.data, form.nome, form.categoria, form.preco));
			persistir("Produto cadastrado.");
		}
	}

	private void editarProduto() {
		Produto produto = produtoSelecionado();
		if (produto == null) return;
		ProdutoFormulario form = new ProdutoFormulario(this, produto);
		if (form.mostrar()) {
			produto.atualizar(form.data, form.nome, form.categoria, form.preco);
			persistir("Produto atualizado.");
		}
	}

	private void removerProduto() {
		Produto produto = produtoSelecionado();
		if (produto == null) return;
		int resposta = JOptionPane.showConfirmDialog(this,
				"Remover “" + produto.getNome() + "” do cadastro?", "Confirmar remoção",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (resposta == JOptionPane.YES_OPTION) {
			produtos.remove(produto);
			persistir("Produto removido.");
		}
	}

	private Produto produtoSelecionado() {
		int linha = tabela.getSelectedRow();
		if (linha < 0) {
			ToolkitBeep.beep();
			status.setText("Selecione um produto na tabela primeiro.");
			return null;
		}
		return produtos.get(tabela.convertRowIndexToModel(linha));
	}

	private void persistir(String mensagem) {
		try {
			repository.salvar(produtos);
			atualizarResumo();
			status.setText(mensagem + " Dados salvos em produtos.bin.");
		} catch (IOException exception) {
			atualizarResumo();
			status.setText("A alteração está na tela, mas não foi salva.");
			JOptionPane.showMessageDialog(this, "Não foi possível salvar produtos.bin.\n\n" + exception.getMessage(),
					"Falha ao salvar", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void atualizarResumo() {
		tableModel.fireTableDataChanged();
		quantidadeResumo.setText(produtos.size() == 1 ? "1 produto" : produtos.size() + " produtos");
		BigDecimal total = produtos.stream().map(produto -> BigDecimal.valueOf(produto.getPreco()))
				.map(valor -> valor.setScale(2, RoundingMode.HALF_UP))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		valorResumo.setText(NumberFormat.getCurrencyInstance(BRASIL).format(total));
	}

	private void exportarCsv() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Exportar cadastro de produtos");
		chooser.setSelectedFile(new java.io.File("produtos.csv"));
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(chooser.getSelectedFile()), "UTF-8"))) {
			writer.write('\ufeff');
			writer.write("Codigo;Data;Produto;Categoria;Preco\r\n");
			for (Produto produto : produtos) {
				writer.write(produto.getCodigo() + ";" + csv(produto.getData()) + ";" + csv(produto.getNome()) + ";"
						+ csv(produto.getCategoria()) + ";" + csv(String.format(BRASIL, "%.2f", produto.getPreco())) + "\r\n");
			}
			status.setText("Arquivo exportado: " + chooser.getSelectedFile().getName());
		} catch (IOException exception) {
			JOptionPane.showMessageDialog(this, "Não foi possível exportar o arquivo.\n\n" + exception.getMessage(),
					"Falha ao exportar", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String csv(String valor) {
		return "\"" + valor.replace("\"", "\"\"") + "\"";
	}

	static String normalizarData(String valor) {
		String texto = valor.trim();
		DateTimeParseException erro = null;
		for (DateTimeFormatter formato : new DateTimeFormatter[] { DATA_ENTRADA, DATA_CURTA }) {
			try {
				return LocalDate.parse(texto, formato).format(DATA_ENTRADA);
			} catch (DateTimeParseException exception) {
				erro = exception;
			}
		}
		throw erro == null ? new DateTimeParseException("Data inválida", texto, 0) : erro;
	}

	private static float lerPreco(String texto) {
		String valor = texto.trim().replace("R$", "").trim();
		NumberFormat formato = NumberFormat.getNumberInstance(BRASIL);
		ParsePosition posicao = new ParsePosition(0);
		Number numero = formato.parse(valor, posicao);
		if (numero == null || posicao.getIndex() != valor.length()) {
			throw new NumberFormatException("Digite um valor como 12,50.");
		}
		float preco = numero.floatValue();
		if (!Float.isFinite(preco) || preco < 0) {
			throw new NumberFormatException("O preço deve ser um número positivo.");
		}
		return preco;
	}

	private static class ProdutoTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUNAS = { "Código", "Data", "Produto", "Categoria", "Preço" };
		private final List<Produto> produtos;

		ProdutoTableModel(List<Produto> produtos) { this.produtos = produtos; }
		@Override public int getRowCount() { return produtos.size(); }
		@Override public int getColumnCount() { return COLUNAS.length; }
		@Override public String getColumnName(int coluna) { return COLUNAS[coluna]; }
		@Override public Class<?> getColumnClass(int coluna) { return coluna == 0 ? Integer.class : coluna == 4 ? BigDecimal.class : String.class; }
		@Override public boolean isCellEditable(int linha, int coluna) { return false; }
		@Override public Object getValueAt(int linha, int coluna) {
			Produto produto = produtos.get(linha);
			switch (coluna) {
			case 0: return produto.getCodigo();
			case 1: return produto.getData();
			case 2: return produto.getNome();
			case 3: return produto.getCategoria();
			case 4: return BigDecimal.valueOf(produto.getPreco()).setScale(2, RoundingMode.HALF_UP);
			default: throw new IndexOutOfBoundsException("Coluna inválida");
			}
		}
	}

	private static class ProdutoFormulario extends JDialog {
		private static final long serialVersionUID = 1L;
		private final JTextField dataField = new JTextField(18);
		private final JTextField nomeField = new JTextField(24);
		private final JTextField categoriaField = new JTextField(24);
		private final JTextField precoField = new JTextField(18);
		private boolean confirmado;
		private String data;
		private String nome;
		private String categoria;
		private float preco;

		ProdutoFormulario(JFrame owner, Produto produto) {
			super(owner, produto == null ? "Novo produto" : "Editar produto", true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLayout(new BorderLayout(0, 18));
			JPanel campos = new JPanel(new GridBagLayout());
			campos.setBackground(Color.WHITE);
			campos.setBorder(BorderFactory.createEmptyBorder(22, 24, 4, 24));
			adicionarCampo(campos, 0, "Data (DD/MM/AAAA)", dataField);
			adicionarCampo(campos, 1, "Nome do produto", nomeField);
			adicionarCampo(campos, 2, "Categoria", categoriaField);
			adicionarCampo(campos, 3, "Preço (R$)", precoField);
			if (produto != null) {
				dataField.setText(produto.getData());
				nomeField.setText(produto.getNome());
				categoriaField.setText(produto.getCategoria());
				precoField.setText(String.format(BRASIL, "%.2f", produto.getPreco()));
			} else {
				dataField.setText(LocalDate.now().format(DATA_ENTRADA));
			}

			JPanel botoes = new JPanel();
			botoes.setBackground(Color.WHITE);
			botoes.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
			JButton cancelar = new JButton("Cancelar");
			JButton salvar = new JButton("Salvar produto");
			salvar.setBackground(BLUE);
			salvar.setForeground(Color.WHITE);
			cancelar.addActionListener(event -> dispose());
			salvar.addActionListener(event -> validarESalvar());
			botoes.add(cancelar);
			botoes.add(salvar);
			add(campos, BorderLayout.CENTER);
			add(botoes, BorderLayout.SOUTH);
			getRootPane().setDefaultButton(salvar);
			pack();
			setLocationRelativeTo(owner);
		}

		private void adicionarCampo(JPanel painel, int linha, String rotulo, JTextField campo) {
			GridBagConstraints label = new GridBagConstraints();
			label.gridx = 0; label.gridy = linha; label.anchor = GridBagConstraints.LINE_START;
			label.insets = new Insets(8, 0, 4, 18);
			JLabel texto = new JLabel(rotulo);
			texto.setFont(new Font("SansSerif", Font.BOLD, 13));
			painel.add(texto, label);
			GridBagConstraints input = new GridBagConstraints();
			input.gridx = 1; input.gridy = linha; input.weightx = 1; input.fill = GridBagConstraints.HORIZONTAL;
			input.insets = new Insets(8, 0, 4, 0);
			campo.setFont(new Font("SansSerif", Font.PLAIN, 15));
			campo.setToolTipText(rotulo);
			painel.add(campo, input);
		}

		boolean mostrar() { setVisible(true); return confirmado; }

		private void validarESalvar() {
			try {
				data = normalizarData(dataField.getText());
				nome = nomeField.getText().trim();
				categoria = categoriaField.getText().trim();
				if (nome.isEmpty() || categoria.isEmpty()) {
					throw new IllegalArgumentException("Preencha o nome e a categoria do produto.");
				}
				preco = lerPreco(precoField.getText());
				confirmado = true;
				dispose();
			} catch (DateTimeParseException exception) {
				mostrarErro("Informe uma data válida no formato DD/MM/AAAA.");
				dataField.requestFocusInWindow();
			} catch (IllegalArgumentException exception) {
				mostrarErro(exception.getMessage());
			}
		}

		private void mostrarErro(String mensagem) {
			JOptionPane.showMessageDialog(this, mensagem, "Confira os dados", JOptionPane.WARNING_MESSAGE);
		}
	}

	private static final class ToolkitBeep {
		static void beep() { java.awt.Toolkit.getDefaultToolkit().beep(); }
	}
}