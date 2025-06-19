package com.vialectoris.libreria.core.domain;

import java.util.ArrayList;
import java.util.List;

public class Libro {
    private String isbn;
    private String titulo;
    private int annoPublicacion;
    private List<Autor> autores;

    public Libro() {
        this.autores = new ArrayList<>();
    }

    public Libro(String isbn, String titulo, int annoPublicacion) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.annoPublicacion = annoPublicacion;
        this.autores = new ArrayList<>();
    }

    public Libro(String isbn, String titulo, int annoPublicacion, List<Autor> autores) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.annoPublicacion = annoPublicacion;
        this.autores = new ArrayList<>(autores); // Copia defensiva
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getAnnoPublicacion() {
        return annoPublicacion;
    }

    public void setAnnoPublicacion(int annoPublicacion) {
        this.annoPublicacion = annoPublicacion;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = new ArrayList<>(autores); // Copia defensiva
    }

    public void addAutor(Autor autor) {
        this.autores.add(autor);
    }

    public void removeAutor(Autor autor) {
        this.autores.remove(autor);
    }
}