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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LibroXmlDataTest {
    private String rutaArchivo;
    private LibroXmlData libroData;
    private List<Autor> autores;

    @BeforeEach
    void setup() {
        // Usar un archivo diferente para cada test para evitar interferencias
        rutaArchivo = "libros_test_" + UUID.randomUUID().toString() + ".xml";

        try {
            // Eliminar archivo de prueba si existe (por si acaso)
            File archivo = new File(rutaArchivo);
            if (archivo.exists()) {
                archivo.delete();
            }

            // Crear nueva instancia de LibroXmlData
            libroData = new LibroXmlData(rutaArchivo);

            // Crear lista de autores para usar en los tests
            autores = new ArrayList<>();
            autores.add(new Autor(1, "Gabriel", "García Márquez", "Colombiana"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en la configuración de la prueba", e);
        }
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

    @Test
    void insertar_cuando_libroEsDuplicado_no_inserta() throws Exception {
        // Crear libro original
        Libro libro1 = new Libro("123", "Cien años de soledad", 1967);
        libro1.setAutores(autores);

        // Crear libro duplicado (mismo ISBN)
        Libro libro2 = new Libro("123", "Otro título", 2000);
        libro2.setAutores(autores);

        // Insertar el primer libro
        libroData.insertar(libro1);

        // Intentar insertar el duplicado
        libroData.insertar(libro2);

        // Verificar que solo hay un libro en el XML
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new File(rutaArchivo));
        Element root = doc.getRootElement();
        List<Element> libros = root.getChildren("libro");

        assertEquals(1, libros.size());
        assertEquals("Cien años de soledad", libros.get(0).getChildText("titulo"));
    }

    @Test
    void findLibroByIsbn_con_libro_existente() {
        // Insertar un libro
        Libro libro = new Libro("456", "El amor en los tiempos del cólera", 1985);
        libro.setAutores(autores);
        libroData.insertar(libro);

        // Buscar el libro por ISBN
        Optional<Libro> libroEncontrado = libroData.findLibroByIsbn("456");

        assertTrue(libroEncontrado.isPresent());
        assertEquals("456", libroEncontrado.get().getIsbn());
        assertEquals("El amor en los tiempos del cólera", libroEncontrado.get().getTitulo());
        assertEquals(1985, libroEncontrado.get().getAnnoPublicacion());
    }

    @Test
    void findLibroByIsbn_con_libro_noExistente() {
        // Buscar un libro que no existe
        Optional<Libro> libroEncontrado = libroData.findLibroByIsbn("999");

        assertFalse(libroEncontrado.isPresent());
    }

    @Test
    void findLibrosByIdAutor_con_idAutor_Existente() {
        // Crear autores
        Autor autor1 = new Autor(1, "Gabriel", "García Márquez", "Colombiana");
        Autor autor2 = new Autor(2, "Isabel", "Allende", "Chilena");

        List<Autor> autoresLibro1 = new ArrayList<>();
        autoresLibro1.add(autor1);

        List<Autor> autoresLibro2 = new ArrayList<>();
        autoresLibro2.add(autor1);
        autoresLibro2.add(autor2);

        List<Autor> autoresLibro3 = new ArrayList<>();
        autoresLibro3.add(autor2);

        // Insertar libros
        Libro libro1 = new Libro("111", "Cien años de soledad", 1967);
        libro1.setAutores(autoresLibro1);

        Libro libro2 = new Libro("222", "El amor en los tiempos del cólera", 1985);
        libro2.setAutores(autoresLibro2);

        Libro libro3 = new Libro("333", "La casa de los espíritus", 1982);
        libro3.setAutores(autoresLibro3);

        libroData.insertar(libro1);
        libroData.insertar(libro2);
        libroData.insertar(libro3);

        // Buscar libros del autor 1
        Map<String, Libro> librosAutor1 = libroData.findLibrosByIdAutor(1);

        assertEquals(2, librosAutor1.size());
        assertTrue(librosAutor1.containsKey("111"));
        assertTrue(librosAutor1.containsKey("222"));
    }

    @Test
    void findLibrosByIdAutor_con_idAutor_NoExistente() {
        // Insertar un libro
        Libro libro = new Libro("111", "Cien años de soledad", 1967);
        libro.setAutores(autores);
        libroData.insertar(libro);

        // Buscar libros de un autor que no existe
        Map<String, Libro> librosAutor = libroData.findLibrosByIdAutor(999);

        assertTrue(librosAutor.isEmpty());
    }
}