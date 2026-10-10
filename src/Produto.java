import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

public class Produto implements Serializable {
	private static final long serialVersionUID = -47070160147770107L;

	int codigo;
	String data;
	String nome;
	String categoria;
	float preco;

	public Produto(int codigo, String data, String nome, String categoria, float preco) {
		this.codigo = codigo;
		this.data = data;
		this.nome = nome;
		this.categoria = categoria;
		this.preco = preco;
	}

	public int getCodigo() {
		return codigo;
	}

	public String getData() {
		return data;
	}

	public String getNome() {
		return nome;
	}

	public String getCategoria() {
		return categoria;
	}

	public float getPreco() {
		return preco;
	}

	public void atualizar(String data, String nome, String categoria, float preco) {
		this.data = data;
		this.nome = nome;
		this.categoria = categoria;
		this.preco = preco;
	}

	@Override
	public String toString() {
		NumberFormat formatador = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
		return "Código: " + codigo + "\nData: " + data + "\nNome: " + nome + "\nCategoria: " + categoria
				+ "\nPreço: " + formatador.format(preco);
	}
}