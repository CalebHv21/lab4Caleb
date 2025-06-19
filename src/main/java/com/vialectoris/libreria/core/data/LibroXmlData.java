package com.vialectoris.libreria.core.data;

import com.vialectoris.libreria.core.domain.Autor;
import com.vialectoris.libreria.core.domain.Libro;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LibroXmlData {
    private String rutaArchivo;
    private AutorXmlData autorXmlData; // Para obtener datos completos de autores

    /**
     * Constructor que verifica si el archivo existe y lo crea si no
     */
    public LibroXmlData(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            crearArchivoVacio();
        }
    }

    /**
     * Constructor que permite inyectar AutorXmlData
     */
    public LibroXmlData(String rutaArchivo, AutorXmlData autorXmlData) {
        this(rutaArchivo);
        this.autorXmlData = autorXmlData;
    }

    /**
     * Método para crear un archivo XML vacío con la estructura básica
     */
    private void crearArchivoVacio() {
        try {
            Element raiz = new Element("libros");
            Document documento = new Document(raiz);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(documento, new FileOutputStream(rutaArchivo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método insertar ordenado por el título
     */
    public void insertar(Libro libro) {
        try {
            // Cargar el documento existente
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            // Crear elemento para el nuevo libro
            Element elementoLibro = new Element("libro");
            elementoLibro.setAttribute("ISBN", libro.getIsbn());

            elementoLibro.addContent(new Element("titulo").setText(libro.getTitulo()));
            elementoLibro.addContent(new Element("annoPublicacion").setText(String.valueOf(libro.getAnnoPublicacion())));

            // Añadir autores
            Element idsAutores = new Element("idsAutores");
            for (Autor autor : libro.getAutores()) {
                idsAutores.addContent(new Element("idAutor").setText(String.valueOf(autor.getIdAutor())));
            }
            elementoLibro.addContent(idsAutores);

            // Obtener todos los libros para ordenar por título
            List<Element> librosExistentes = raiz.getChildren("libro");

            // Encontrar la posición correcta para insertar basada en el título
            int indiceInsercion = 0;
            for (Element libroExistente : librosExistentes) {
                String tituloExistente = libroExistente.getChildText("titulo");
                if (libro.getTitulo().compareTo(tituloExistente) >= 0) {
                    indiceInsercion++;
                } else {
                    break;
                }
            }

            // Insertar en la posición correcta
            raiz.addContent(indiceInsercion, elementoLibro);

            // Guardar el documento
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(documento, new FileOutputStream(rutaArchivo));

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método findLibroByIsbn (retorna un único registro)
     */
    public Optional<Libro> findLibroByIsbn(String isbn) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            List<Element> libros = raiz.getChildren("libro");

            for (Element libroElement : libros) {
                if (libroElement.getAttributeValue("ISBN").equals(isbn)) {
                    Libro libro = new Libro();
                    libro.setIsbn(isbn);
                    libro.setTitulo(libroElement.getChildText("titulo"));
                    libro.setAnnoPublicacion(Integer.parseInt(libroElement.getChildText("annoPublicacion")));

                    // Obtener autores completos si tenemos AutorXmlData
                    Element idsAutoresElement = libroElement.getChild("idsAutores");
                    if (idsAutoresElement != null) {
                        List<Element> idAutorElements = idsAutoresElement.getChildren("idAutor");
                        for (Element idAutorElement : idAutorElements) {
                            int idAutor = Integer.parseInt(idAutorElement.getText());

                            if (autorXmlData != null) {
                                // Obtener datos completos del autor
                                Optional<Autor> autorCompleto = autorXmlData.findAutorById(idAutor);
                                if (autorCompleto.isPresent()) {
                                    libro.addAutor(autorCompleto.get());
                                }
                            } else {
                                // Solo crear autor con ID como antes
                                Autor autorTemp = new Autor();
                                autorTemp.setIdAutor(idAutor);
                                libro.addAutor(autorTemp);
                            }
                        }
                    }

                    return Optional.of(libro);
                }
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Método findLibrosByIdAutor (retorna varios registros)
     */
    public Map<String, Libro> findLibrosByIdAutor(int idAutor) {
        Map<String, Libro> librosMap = new HashMap<>();

        try {
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            List<Element> libros = raiz.getChildren("libro");

            for (Element libroElement : libros) {
                Element idsAutoresElement = libroElement.getChild("idsAutores");

                if (idsAutoresElement != null) {
                    List<Element> idAutorElements = idsAutoresElement.getChildren("idAutor");

                    for (Element idAutorElement : idAutorElements) {
                        if (Integer.parseInt(idAutorElement.getText()) == idAutor) {
                            String isbn = libroElement.getAttributeValue("ISBN");
                            Libro libro = new Libro();
                            libro.setIsbn(isbn);
                            libro.setTitulo(libroElement.getChildText("titulo"));
                            libro.setAnnoPublicacion(Integer.parseInt(libroElement.getChildText("annoPublicacion")));

                            // Obtener todos los autores del libro
                            for (Element autorElement : idAutorElements) {
                                int autorId = Integer.parseInt(autorElement.getText());

                                if (autorXmlData != null) {
                                    // Obtener datos completos del autor
                                    Optional<Autor> autorCompleto = autorXmlData.findAutorById(autorId);
                                    if (autorCompleto.isPresent()) {
                                        libro.addAutor(autorCompleto.get());
                                    }
                                } else {
                                    // Solo crear autor con ID como antes
                                    Autor autorTemp = new Autor();
                                    autorTemp.setIdAutor(autorId);
                                    libro.addAutor(autorTemp);
                                }
                            }

                            librosMap.put(isbn, libro);
                            break;
                        }
                    }
                }
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return librosMap;
    }
}