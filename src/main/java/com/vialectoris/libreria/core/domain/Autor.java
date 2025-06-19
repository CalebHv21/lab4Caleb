package com.vialectoris.libreria.core.domain;

import java.util.Objects;

public class Autor {
    private int idAutor;
    private String nombre;
    private String apellidos;
    private String nacionalidad;

    public Autor() {
    }

    public Autor(int idAutor, String nombre, String apellidos, String nacionalidad) {
        this.idAutor = idAutor;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.nacionalidad = nacionalidad;
    }

    public int getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    // Necesarios para que Set<Autor> funcione correctamente
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Autor autor = (Autor) obj;
        return idAutor == autor.idAutor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAutor);
    }
}