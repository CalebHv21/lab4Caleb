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
            // Asegurarse de que el directorio existe
            File archivo = new File(rutaArchivo);
            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                directorio.mkdirs();
            }

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
     * Método insertar (adiciona al final del archivo)
     */
    public void insertar(Autor autor) {
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

            // Crear elemento para el nuevo autor
            Element elementoAutor = new Element("autor");
            elementoAutor.setAttribute("idAutor", String.valueOf(autor.getIdAutor()));

            elementoAutor.addContent(new Element("nombre").setText(autor.getNombre()));
            elementoAutor.addContent(new Element("apellidos").setText(autor.getApellidos()));
            elementoAutor.addContent(new Element("nacionalidad").setText(autor.getNacionalidad()));

            // Adicionar al final del archivo
            raiz.addContent(elementoAutor);

            // Guardar el documento
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(documento, new FileOutputStream(rutaArchivo));

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método findAll (retorna todos los registros de autor presentes en el archivo)
     * El método debe retornar un Set de autores
     */
    public Set<Autor> findAll() {
        Set<Autor> autoresSet = new HashSet<>();

        try {
            // Verificar si el archivo existe o está vacío
            File archivoXml = new File(rutaArchivo);
            if (!archivoXml.exists() || archivoXml.length() == 0) {
                return autoresSet; // Retornar conjunto vacío si no hay archivo o está vacío
            }

            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(archivoXml);
            Element raiz = documento.getRootElement();

            List<Element> autores = raiz.getChildren("autor");

            for (Element autorElement : autores) {
                int id = Integer.parseInt(autorElement.getAttributeValue("idAutor"));
                Autor autor = new Autor();
                autor.setIdAutor(id);
                autor.setNombre(autorElement.getChildText("nombre"));
                autor.setApellidos(autorElement.getChildText("apellidos"));
                autor.setNacionalidad(autorElement.getChildText("nacionalidad"));

                autoresSet.add(autor);
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return autoresSet;
    }

    /**
     * Método adicional para buscar autor por ID (útil para LibroXmlData)
     */
    public Optional<Autor> findAutorById(int idAutor) {
        try {
            // Verificar si el archivo existe o está vacío
            File archivoXml = new File(rutaArchivo);
            if (!archivoXml.exists() || archivoXml.length() == 0) {
                return Optional.empty(); // Retornar Optional vacío si no hay archivo o está vacío
            }

            SAXBuilder builder = new SAXBuilder();
            Document documento = builder.build(archivoXml);
            Element raiz = documento.getRootElement();

            List<Element> autores = raiz.getChildren("autor");

            for (Element autorElement : autores) {
                int id = Integer.parseInt(autorElement.getAttributeValue("idAutor"));
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
}