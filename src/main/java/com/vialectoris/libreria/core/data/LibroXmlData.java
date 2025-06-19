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
            // Asegurarse de que el directorio existe
            File archivo = new File(rutaArchivo);
            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                directorio.mkdirs();
            }

            // Crear el archivo XML con estructura básica
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
     * Método insertar (ordenado por título)
     */
    public void insertar(Libro libro) {
        try {
            // Verificar si el archivo existe, si no, crearlo
            File archivoXml = new File(rutaArchivo);
            if (!archivoXml.exists() || archivoXml.length() == 0) {
                crearArchivoVacio();
            }

            // Cargar el documento existente
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(archivoXml);
            Element raiz = documento.getRootElement();

            // Verificar si ya existe un libro con este ISBN
            List<Element> librosExistentes = raiz.getChildren("libro");
            for (Element libroExistente : librosExistentes) {
                String isbnExistente = libroExistente.getAttributeValue("ISBN");
                if (isbnExistente != null && isbnExistente.equals(libro.getIsbn())) {
                    // Ya existe un libro con este ISBN, no insertar
                    return;
                }
            }

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

            // Encontrar la posición correcta para insertar basada en el título
            int posicionInsercion = 0;
            boolean insertado = false;

            for (int i = 0; i < librosExistentes.size(); i++) {
                String tituloExistente = librosExistentes.get(i).getChildText("titulo");

                // Si el título del nuevo libro es alfabéticamente menor, insertar aquí
                if (tituloExistente != null && libro.getTitulo().compareTo(tituloExistente) < 0) {
                    posicionInsercion = i;
                    insertado = true;
                    break;
                }
                posicionInsercion = i + 1;
            }

            // Insertar el libro en la posición correcta
            raiz.addContent(posicionInsercion, elementoLibro);

            // Guardar el documento
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(documento, new FileOutputStream(rutaArchivo));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método findLibroByIsbn (retorna un único registro)
     */
    public Optional<Libro> findLibroByIsbn(String isbn) {
        try {
            File archivoXml = new File(rutaArchivo);
            if (!archivoXml.exists() || archivoXml.length() == 0) {
                return Optional.empty();
            }

            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(archivoXml);
            Element raiz = documento.getRootElement();

            List<Element> libros = raiz.getChildren("libro");

            for (Element libroElement : libros) {
                String isbnExistente = libroElement.getAttributeValue("ISBN");
                if (isbnExistente != null && isbnExistente.equals(isbn)) {
                    Libro libro = new Libro();
                    libro.setIsbn(isbn);
                    libro.setTitulo(libroElement.getChildText("titulo"));
                    libro.setAnnoPublicacion(Integer.parseInt(libroElement.getChildText("annoPublicacion")));

                    // Obtener autores
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
                                } else {
                                    // Si no hay información, crear un autor básico
                                    Autor autorTemp = new Autor();
                                    autorTemp.setIdAutor(idAutor);
                                    libro.addAutor(autorTemp);
                                }
                            } else {
                                // Si no hay autorXmlData, crear un autor básico
                                Autor autorTemp = new Autor();
                                autorTemp.setIdAutor(idAutor);
                                libro.addAutor(autorTemp);
                            }
                        }
                    }

                    return Optional.of(libro);
                }
            }

        } catch (Exception e) {
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
            // Verificar si el archivo existe
            File archivoXml = new File(rutaArchivo);
            if (!archivoXml.exists() || archivoXml.length() == 0) {
                return librosMap; // Retornar mapa vacío si no hay archivo
            }

            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(archivoXml);
            Element raiz = documento.getRootElement();

            List<Element> libros = raiz.getChildren("libro");

            // Recorrer todos los libros
            for (Element libroElement : libros) {
                Element idsAutoresElement = libroElement.getChild("idsAutores");

                if (idsAutoresElement != null) {
                    boolean autorEncontrado = false;

                    // Verificar si el libro contiene al autor solicitado
                    for (Element idAutorElement : idsAutoresElement.getChildren("idAutor")) {
                        int autorId = Integer.parseInt(idAutorElement.getText());
                        if (autorId == idAutor) {
                            autorEncontrado = true;
                            break;
                        }
                    }

                    // Si el libro contiene al autor solicitado
                    if (autorEncontrado) {
                        String isbn = libroElement.getAttributeValue("ISBN");
                        Libro libro = new Libro();
                        libro.setIsbn(isbn);
                        libro.setTitulo(libroElement.getChildText("titulo"));
                        libro.setAnnoPublicacion(Integer.parseInt(libroElement.getChildText("annoPublicacion")));

                        // Añadir todos los autores del libro
                        for (Element idAutorElement : idsAutoresElement.getChildren("idAutor")) {
                            int autorId = Integer.parseInt(idAutorElement.getText());

                            if (autorXmlData != null) {
                                Optional<Autor> autorCompleto = autorXmlData.findAutorById(autorId);
                                if (autorCompleto.isPresent()) {
                                    libro.addAutor(autorCompleto.get());
                                } else {
                                    Autor autorTemp = new Autor();
                                    autorTemp.setIdAutor(autorId);
                                    libro.addAutor(autorTemp);
                                }
                            } else {
                                Autor autorTemp = new Autor();
                                autorTemp.setIdAutor(autorId);
                                libro.addAutor(autorTemp);
                            }
                        }

                        librosMap.put(isbn, libro);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return librosMap;
    }
}