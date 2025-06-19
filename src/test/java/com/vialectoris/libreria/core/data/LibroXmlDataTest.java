package com.vialectoris.libreria.core.data;

import com.vialectoris.libreria.core.domain.Autor;
import com.vialectoris.libreria.core.domain.Libro;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibroXmlDataTest {
    private static final String rutaArchivo = "libros_test.xml";
    private LibroXmlData libroData;
    private List<Autor> autores;

    @BeforeEach
    void setup() {
        // Eliminar archivo de prueba si existe
        new File(rutaArchivo).delete();
        // Crear nueva instancia de LibroXmlData
        libroData = new LibroXmlData(rutaArchivo);

        // Crear lista de autores para usar en los tests
        autores = new ArrayList<>();
        autores.add(new Autor(1, "Gabriel", "García Márquez", "Colombiana"));
    }

    @Test
    void insertar_ordenadoPorTitulo() throws Exception {
        // Crear libros con títulos en orden inverso
        Libro libro1 = new Libro("111", "Zoología", 1990);
        libro1.setAutores(autores);
        Libro libro2 = new Libro("222", "Aventuras", 1980);
        libro2.setAutores(autores);

        libroData.insertar(libro1);
        libroData.insertar(libro2);

        // Verificamos directamente en el XML si están ordenados por título
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new File(rutaArchivo));
        Element root = doc.getRootElement();
        List<Element> libros = root.getChildren("libro");

        assertEquals(2, libros.size());

        // El primer libro debe ser "Aventuras"
        assertEquals("Aventuras", libros.get(0).getChildText("titulo"));
        // El segundo libro debe ser "Zoología"
        assertEquals("Zoología", libros.get(1).getChildText("titulo"));
    }
}