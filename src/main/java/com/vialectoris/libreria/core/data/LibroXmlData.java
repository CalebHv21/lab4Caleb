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
            if (archivo.exists()) {
                archivo.delete();
            }

            // Si es necesario, crear el directorio padre
            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                directorio.mkdirs();
            }

            // Crear el archivo XML con estructura básica
            Element raiz = new Element("libros");
            Document documento = new Document(raiz);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());

            // Usar FileOutputStream en modo que sobrescriba el archivo si existe
            FileOutputStream fos = new FileOutputStream(rutaArchivo);
            xmlOutput.output(documento, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void insertar(Libro libro) {
        try {
            // Verificar si el archivo existe, si no, crearlo
            File archivoXml = new File(rutaArchivo);
            if (!archivoXml.exists() || archivoXml.length() == 0) {
                crearArchivoVacio();
            }

            // Cargar el documento existente
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            // Obtener todos los libros existentes
            List<Element> librosExistentes = raiz.getChildren("libro");

            // Verificar si ya existe un libro con este ISBN
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
            // CAMBIO: Reescribimos completamente esta lógica
            int indiceInsercion = 0;
            boolean insertado = false;

            for (int i = 0; i < librosExistentes.size() && !insertado; i++) {
                Element libroExistente = librosExistentes.get(i);
                String tituloExistente = libroExistente.getChildText("titulo");

                // Si el título del nuevo libro es alfabéticamente menor, insertar aquí
                if (tituloExistente != null && libro.getTitulo().compareTo(tituloExistente) < 0) {
                    raiz.addContent(i, elementoLibro);
                    insertado = true;
                }

                indiceInsercion = i + 1;
            }

            // Si no se insertó en ningún lugar intermedio, añadir al final
            if (!insertado) {
                raiz.addContent(elementoLibro);
            }

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
                String isbnExistente = libroElement.getAttributeValue("ISBN");
                if (isbnExistente != null && isbnExistente.equals(isbn)) {
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

                    // Verificar si el autor buscado está entre los autores del libro
                    for (Element idAutorElement : idsAutoresElement.getChildren("idAutor")) {
                        int autorId = Integer.parseInt(idAutorElement.getText());
                        if (autorId == idAutor) {
                            autorEncontrado = true;
                            break;
                        }
                    }

                    // Si el autor está entre los autores del libro, agregarlo al mapa
                    if (autorEncontrado) {
                        String isbn = libroElement.getAttributeValue("ISBN");
                        if (isbn != null) {
                            Libro libro = new Libro();
                            libro.setIsbn(isbn);
                            libro.setTitulo(libroElement.getChildText("titulo"));
                            libro.setAnnoPublicacion(Integer.parseInt(libroElement.getChildText("annoPublicacion")));

                            // Recrear todos los autores del libro
                            for (Element idAutorElement : idsAutoresElement.getChildren("idAutor")) {
                                int autorId = Integer.parseInt(idAutorElement.getText());

                                Autor autorTemp = new Autor();
                                autorTemp.setIdAutor(autorId);

                                if (autorXmlData != null) {
                                    // Obtener datos completos del autor
                                    Optional<Autor> autorCompleto = autorXmlData.findAutorById(autorId);
                                    if (autorCompleto.isPresent()) {
                                        libro.addAutor(autorCompleto.get());
                                    } else {
                                        libro.addAutor(autorTemp);
                                    }
                                } else {
                                    libro.addAutor(autorTemp);
                                }
                            }

                            librosMap.put(isbn, libro);
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