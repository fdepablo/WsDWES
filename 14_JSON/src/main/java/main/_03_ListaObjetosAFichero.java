package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import beans.Persona;

public class _03_ListaObjetosAFichero {
	public static void main(String[] args) {
		
		Persona p1 = new Persona();
		p1.setIdPersona(1);
		p1.setNombre("Homer");
		p1.setEdad(38);
		p1.setApellido("Simpson");
		
		Persona p2 = new Persona();
		p2.setIdPersona(2);
		p2.setNombre("Lisa");
		p2.setEdad(10);
		p2.setApellido("Simpson");
		
		Persona p3 = new Persona();
		p3.setIdPersona(3);
		p3.setNombre("Bart");
		p3.setEdad(12);
		p3.setApellido("Simpson");
		
		Persona p4 = new Persona();
		p4.setIdPersona(4);
		p4.setNombre("Marge");
		p4.setEdad(36);
		p4.setApellido("Simpson");
		
		Persona p5 = new Persona();
		p5.setIdPersona(5);
		p5.setNombre("Maggie");
		p5.setEdad(3);
		p5.setApellido("Simpson");
		
		List<Persona> listaPersonas = new ArrayList<Persona>();
		listaPersonas.add(p1);
		listaPersonas.add(p2);
		listaPersonas.add(p3);
		listaPersonas.add(p4);
		listaPersonas.add(p5);
		
		Gson gson = new Gson();
		String json = gson.toJson(listaPersonas);
		
		Path fichero = Path.of("simpson.json");
		
		try {
			Files.writeString(fichero, json, StandardCharsets.UTF_8);
			System.out.println("Fichero creado: " + fichero.toAbsolutePath());
		} catch (IOException e) {
			System.err.println("No se pudo escribir el fichero: " + e.getMessage());
		}
	}
}
