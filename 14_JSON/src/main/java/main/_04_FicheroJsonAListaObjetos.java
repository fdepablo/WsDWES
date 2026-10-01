package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import beans.Persona;

public class _04_FicheroJsonAListaObjetos {
	public static void main(String[] args) {
		Path fichero = Path.of("simpson.json");
		
		try {
			String json = Files.readString(fichero, StandardCharsets.UTF_8);
			Gson gson = new Gson();
			List<Persona> grupoPersonas = gson.fromJson(json, new TypeToken<List<Persona>>(){}.getType());
			if (grupoPersonas == null) {
				System.err.println("El fichero no contiene una lista de personas.");
				return;
			}
			for(Persona p : grupoPersonas) {
				System.out.println(p);
			}
		} catch (IOException e) {
			System.err.println("No se pudo leer el fichero: " + e.getMessage());
		}
	}
}
