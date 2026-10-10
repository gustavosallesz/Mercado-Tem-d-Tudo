# Mercado Tem d'Tudo

Aplicativo desktop em Java para cadastro e consulta de produtos. Esta versao atualiza a interface de dialogos para uma tela de estoque com busca, edicao, remocao confirmada e exportacao CSV.

## Recursos

- Tabela pesquisavel por codigo, data, produto e categoria.
- Cadastro e edicao com validacao de data, campos obrigatorios e preco em reais.
- Remocao com confirmacao e salvamento automatico apos cada alteracao.
- Resumo da quantidade de produtos e da soma dos valores cadastrados.
- Exportacao para CSV compativel com planilhas.
- Persistencia em `produtos.bin`, compativel com os registros da versao anterior.
- Atalhos: `Ctrl+N` para novo produto, `Ctrl+F` para busca e `Delete` para remover a selecao.

## Executar

Baixe `dist/MercadoTemdTudo.jar` e execute com Java 8 ou superior:

```bat
java -jar MercadoTemdTudo.jar
```

No repositorio, o atalho `run.bat` abre o JAR ja compilado. O arquivo antigo `Mercado Tem d'Tudo.exe` foi mantido sem alteracoes como distribuicao legada; a interface atualizada esta no JAR.

## Compilar

Instale um JDK 9 ou superior e execute `build.bat`. O script compila para Java 8 e grava o JAR em `dist/MercadoTemdTudo.jar`. Para apenas executar o JAR, Java 8 ou superior e suficiente.

## Teste

```bat
javac --release 8 -encoding UTF-8 -d build src\Produto.java src\ProdutoRepository.java tests\ProdutoRepositoryTest.java
java -cp build ProdutoRepositoryTest
```

Os dados sao gravados em `produtos.bin` no diretorio de execucao. Mantenha uma copia desse arquivo antes de move-lo entre computadores.