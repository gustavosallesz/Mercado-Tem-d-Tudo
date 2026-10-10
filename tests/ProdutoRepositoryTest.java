import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.List;

public class ProdutoRepositoryTest {
	public static void main(String[] args) throws Exception {
		Path pasta = Files.createTempDirectory("mercado-repository-test");
		Path arquivo = pasta.resolve("produtos.bin");
		ProdutoRepository repository = new ProdutoRepository(arquivo);

		if (!repository.carregar().isEmpty()) throw new AssertionError("Arquivo ausente deve iniciar vazio.");

		List<Produto> esperados = Arrays.asList(
				new Produto(1, "10/06/2026", "Arroz", "Alimentos", 24.90f),
				new Produto(2, "11/06/2026", "Sabão", "Limpeza", 8.50f));
		repository.salvar(esperados);

		List<Produto> carregados = repository.carregar();
		if (ObjectStreamClass.lookup(Produto.class).getSerialVersionUID() != -47070160147770107L) {
			throw new AssertionError("Identificador serial mudou em relacao a versao anterior.");
		}
		if (carregados.size() != 2) throw new AssertionError("Quantidade de registros incorreta.");
		if (!"Arroz".equals(carregados.get(0).getNome())) throw new AssertionError("Nome não persistido.");
		if (Math.abs(carregados.get(1).getPreco() - 8.50f) > 0.001f) throw new AssertionError("Preço não persistido.");
		if (!"27/04/2025".equals(MercadoApp.normalizarData("27/04/25"))) {
			throw new AssertionError("Data curta da versão anterior não foi normalizada.");
		}
		if (!"29/02/2024".equals(MercadoApp.normalizarData("29/02/2024"))) {
			throw new AssertionError("Data bissexta válida não foi aceita.");
		}
		try {
			MercadoApp.normalizarData("29/02/2025");
			throw new AssertionError("Data impossível foi aceita.");
		} catch (java.time.format.DateTimeParseException expected) {
			// Data invalida esperada.
		}

		if (args.length > 0) {
			Path legadoPath = Paths.get(args[0]);
			if (Files.exists(legadoPath) && Files.size(legadoPath) > 0) {
				List<Produto> legado = new ProdutoRepository(legadoPath).carregar();
				System.out.println("Leitura do arquivo legado passou (" + legado.size() + " registros).");
			} else {
				System.out.println("Fixture legado vazio; UID compativel validado.");
			}
		}

		Files.deleteIfExists(arquivo);
		Files.deleteIfExists(pasta);
		System.out.println("ProdutoRepositoryTest passou.");
	}
}