import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {
	private final Path arquivo;

	public ProdutoRepository() {
		this(Paths.get("produtos.bin"));
	}

	ProdutoRepository(Path arquivo) {
		this.arquivo = arquivo;
	}

	public List<Produto> carregar() throws IOException {
		if (!Files.exists(arquivo)) {
			return new ArrayList<>();
		}

		try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo.toFile()))) {
			Object objeto = entrada.readObject();
			if (!(objeto instanceof ArrayList<?>)) {
				throw new IOException("O arquivo de produtos tem um formato inesperado.");
			}

			List<Produto> produtos = new ArrayList<>();
			for (Object item : (ArrayList<?>) objeto) {
				if (!(item instanceof Produto)) {
					throw new IOException("O arquivo contém um registro incompatível.");
				}
				produtos.add((Produto) item);
			}
			return produtos;
		} catch (ClassNotFoundException | ClassCastException exception) {
			throw new IOException("Não foi possível ler o arquivo de produtos.", exception);
		}
	}

	public void salvar(List<Produto> produtos) throws IOException {
		Path pasta = arquivo.toAbsolutePath().getParent();
		if (pasta != null) {
			Files.createDirectories(pasta);
		}

		Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
		try (ObjectOutputStream saida = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(temporario.toFile())))) {
			saida.writeObject(new ArrayList<>(produtos));
		}

		try {
			Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}