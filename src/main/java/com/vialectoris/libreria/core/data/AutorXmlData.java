package com.vialectoris.libreria.core.data;

import com.vialectoris.libreria.core.domain.Autor;
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

public class AutorXmlData {
    private String rutaArchivo;

    /**
     * Constructor que verifica si el archivo existe y lo crea si no
     */
    public AutorXmlData(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            crearArchivoVacio();
        }
    }

    /**
     * Método para crear un archivo XML vacío con la estructura básica
     */
    private void crearArchivoVacio() {
        try {
            Element raiz = new Element("autores");
            Document documento = new Document(raiz);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(documento, new FileOutputStream(rutaArchivo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método insertar ordenado por apellidos
     */
    public void insertar(Autor autor) {
        try {
            // Cargar el documento existente
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            // Crear elemento para el nuevo autor
            Element elementoAutor = new Element("autor");
            elementoAutor.addContent(new Element("idAutor").setText(String.valueOf(autor.getIdAutor())));
            elementoAutor.addContent(new Element("nombre").setText(autor.getNombre()));
            elementoAutor.addContent(new Element("apellidos").setText(autor.getApellidos()));
            elementoAutor.addContent(new Element("nacionalidad").setText(autor.getNacionalidad()));

            // Obtener todos los autores para ordenar por apellidos
            List<Element> autoresExistentes = raiz.getChildren("autor");

            // Encontrar la posición correcta para insertar basada en los apellidos
            int indiceInsercion = 0;
            for (Element autorExistente : autoresExistentes) {
                String apellidosExistente = autorExistente.getChildText("apellidos");
                if (autor.getApellidos().compareTo(apellidosExistente) >= 0) {
                    indiceInsercion++;
                } else {
                    break;
                }
            }

            // Insertar en la posición correcta
            raiz.addContent(indiceInsercion, elementoAutor);

            // Guardar el documento
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(documento, new FileOutputStream(rutaArchivo));

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método findAutorById (retorna un único registro)
     */
    public Optional<Autor> findAutorById(int idAutor) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            List<Element> autores = raiz.getChildren("autor");

            for (Element autorElement : autores) {
                int id = Integer.parseInt(autorElement.getChildText("idAutor"));
                if (id == idAutor) {
                    Autor autor = new Autor();
                    autor.setIdAutor(id);
                    autor.setNombre(autorElement.getChildText("nombre"));
                    autor.setApellidos(autorElement.getChildText("apellidos"));
                    autor.setNacionalidad(autorElement.getChildText("nacionalidad"));

                    return Optional.of(autor);
                }
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Método findAutoresByNacionalidad (retorna varios registros)
     */
    public Map<Integer, Autor> findAutoresByNacionalidad(String nacionalidad) {
        Map<Integer, Autor> autoresMap = new HashMap<>();

        try {
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            List<Element> autores = raiz.getChildren("autor");

            for (Element autorElement : autores) {
                String nacionalidadAutor = autorElement.getChildText("nacionalidad");

                if (nacionalidadAutor.equalsIgnoreCase(nacionalidad)) {
                    int id = Integer.parseInt(autorElement.getChildText("idAutor"));
                    Autor autor = new Autor();
                    autor.setIdAutor(id);
                    autor.setNombre(autorElement.getChildText("nombre"));
                    autor.setApellidos(autorElement.getChildText("apellidos"));
                    autor.setNacionalidad(nacionalidadAutor);

                    autoresMap.put(id, autor);
                }
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return autoresMap;
    }

    /**
     * Método para obtener todos los autores
     */
    public Map<Integer, Autor> findAllAutores() {
        Map<Integer, Autor> autoresMap = new HashMap<>();

        try {
            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(new File(rutaArchivo));
            Element raiz = documento.getRootElement();

            List<Element> autores = raiz.getChildren("autor");

            for (Element autorElement : autores) {
                int id = Integer.parseInt(autorElement.getChildText("idAutor"));
                Autor autor = new Autor();
                autor.setIdAutor(id);
                autor.setNombre(autorElement.getChildText("nombre"));
                autor.setApellidos(autorElement.getChildText("apellidos"));
                autor.setNacionalidad(autorElement.getChildText("nacionalidad"));

                autoresMap.put(id, autor);
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return autoresMap;
    }
}