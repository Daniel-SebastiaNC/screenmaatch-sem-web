package br.com.alura.screenmatch.model;

public enum Categoria {
    ACAO("Action", "ação"),
    ROMANCE("Romance", "romance"),
    COMEDIA("Comedy", "comédia"),
    DRAMA("Drama", "drama"),
    CRIME("Crime", "crime");

    private String categoriaOmdb;
    private String getCategoriaPtbr;

    Categoria(String categoriaOmdb, String getCategoriaPtbr){
        this.categoriaOmdb = categoriaOmdb;
        this.getCategoriaPtbr = getCategoriaPtbr;
    }

    public static Categoria fromString(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }

    public static Categoria fromPtbr(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.getCategoriaPtbr.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }
}
