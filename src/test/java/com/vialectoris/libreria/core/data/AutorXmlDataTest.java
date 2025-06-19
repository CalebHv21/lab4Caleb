package com.vialectoris.libreria.core.data;

import com.vialectoris.libreria.core.domain.Autor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AutorXmlDataTest {
    private static final String rutaArchivo = "autores_test.xml";
    private AutorXmlData autorData;

    @BeforeEach
    void setup() {
        new File(rutaArchivo).delete();
        autorData = new AutorXmlData(rutaArchivo);
    }

    @Test
    void insertar_alFinal() throws Exception {
        // Crear autores
        Autor autor1 = new Autor(1, "Gabriel", "García Márquez", "Colombiana");
        Autor autor2 = new Autor(2, "Isabel", "Allende", "Chilena");

        // Insertar autores
        autorData.insertar(autor1);
        autorData.insertar(autor2);

        // Verificar directamente en el XML que están en el orden de inserción
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new File(rutaArchivo));
        Element root = doc.getRootElement();
        List<Element> autores = root.getChildren("autor");

        assertEquals(2, autores.size());

        // Verificar orden de inserción
        assertEquals("1", autores.get(0).getAttributeValue("idAutor"));
        assertEquals("Gabriel", autores.get(0).getChildText("nombre"));

        assertEquals("2", autores.get(1).getAttributeValue("idAutor"));
        assertEquals("Isabel", autores.get(1).getChildText("nombre"));
    }

    @Test
    void findAll_con_autoresExistentes() {
        // Insertar autores
        Autor autor1 = new Autor(1, "Gabriel", "García Márquez", "Colombiana");
        Autor autor2 = new Autor(2, "Isabel", "Allende", "Chilena");

        autorData.insertar(autor1);
        autorData.insertar(autor2);

        // Buscar todos los autores
        Set<Autor> autores = autorData.findAll();

        assertEquals(2, autores.size());
        assertTrue(autores.contains(autor1));
        assertTrue(autores.contains(autor2));
    }

    @Test
    void findAll_sinAutores() {
        // Buscar autores en archivo vacío
        Set<Autor> autores = autorData.findAll();

        assertTrue(autores.isEmpty());
    }
}