package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner scanner = new Scanner(System.in);

    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=acf067a0";

    //List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repository;

    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibiMenu() {
        var opcao = -1;

        while(opcao != 0) {
            var menu = """
            1 - Buscar séries
            2 - Buscar episódios
            3 - Listar séries buscadas
            4 - Buscar série por título
            5 - Buscar série por ator
            6 - top 5
            7 - Buscar por Genero
            8 - Filtrar séries

            0 - Sair                                 
            """;

            System.out.println(menu);
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5();
                    break;
                case 7:
                    buscarPorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");

            }
        }

    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = scanner.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDaddos(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();

        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = scanner.nextLine();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()){

            var serieEncontrada = serie.get();

            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDaddos(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);

            repository.save(serieEncontrada);
        } else {
            System.out.println("Serie nao encontrada!");
        }

    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = scanner.nextLine();

        Optional<Serie> serieBuscada = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Nao encontrada");
        }

    }

    private void buscarSeriePorAtor() {
        System.out.println("digite o nome do ator/atirz: ");
        var nomeAtor = scanner.nextLine();
        System.out.println("digite a avaliacao minima: ");
        var avaliacaoMin = scanner.nextDouble();
        scanner.nextLine();

        List<Serie> seriesBuscadas = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacaoMin);

        seriesBuscadas.forEach(s -> System.out.println(s.getTitulo() + "- nota: " + s.getAvaliacao()));
    }

    private void buscarTop5() {
        List<Serie> seriesBuscadas = repository.findTop5ByOrderByAvaliacaoDesc();
        seriesBuscadas.forEach(s -> System.out.println(s.getTitulo() + "- nota: " + s.getAvaliacao()));
    }

    private void buscarPorCategoria() {
        System.out.println("Digite a Categoria da série: ");
        var categoriaBuscada = scanner.nextLine();
        Categoria categoria = Categoria.fromPtbr(categoriaBuscada);
        List<Serie> seriesEncontradas = repository.findByGenero(categoria);
        seriesEncontradas.forEach(System.out::println);
    }

    private void filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        var avaliacao = scanner.nextDouble();
        scanner.nextLine();
        List<Serie> filtroSeries = repository.seriesPorTemporadasEAvaliacao(totalTemporadas, avaliacao);
        System.out.println("*** Séries filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - avaliação: " + s.getAvaliacao()));
    }

}
