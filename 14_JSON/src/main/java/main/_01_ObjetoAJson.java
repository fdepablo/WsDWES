package main;

import com.google.gson.Gson;

import beans.Persona;

public class _01_ObjetoAJson {
	public static void main(String[] args) {
		Persona p1 = new Persona();
		p1.setIdPersona(3);
		p1.setNombre("Harry");
		p1.setEdad(18);
		p1.setApellido("Potter");
		
		// Gson escapa los valores y forma un JSON válido sin concatenar cadenas a mano.
		Gson gson = new Gson();
		String json = gson.toJson(p1);
		System.out.println(json);
	}
}
