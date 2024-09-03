package br.com.alura.screenmatch.service;

public interface IConverteDados {
    <T> T obterDaddos(String json, Class<T> classe);
}
