package main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Persona;

import java.util.List;

/** Ejecuta las operaciones básicas de JPA en el mismo orden que el ejemplo antiguo. */
public class DemoJpa {
    public static void main(String[] args) {
        EntityManagerFactory factoria = Persistence.createEntityManagerFactory("PersonasPU");
        try {
            Long idAna = crear(factoria, new Persona("Ana", 25, 62.5));
            Long idLuis = crear(factoria, new Persona("Luis", 31, 78.0));
            System.out.println("Creada Ana con ID " + idAna + "; Luis con ID " + idLuis);

            System.out.println("Búsqueda por ID: " + buscar(factoria, idAna));
            System.out.println("Actualización: " + actualizar(factoria, idAna, "Ana María", 26));
            Persona separada = buscar(factoria, idAna);
            separada.setPeso(63.0);
            fusionar(factoria, separada);
            System.out.println("Personas tras actualizar:");
            listar(factoria);

            System.out.println("Borrado de Luis: " + borrar(factoria, idLuis));
            System.out.println("Personas tras borrar:");
            listar(factoria);
        } finally {
            factoria.close();
        }
    }

    /** persist incorpora una entidad nueva al contexto y el commit guarda la fila. */
    private static Long crear(EntityManagerFactory factoria, Persona persona) {
        EntityManager gestor = factoria.createEntityManager();
        EntityTransaction transaccion = gestor.getTransaction();
        try {
            transaccion.begin();
            gestor.persist(persona);
            transaccion.commit();
            return persona.getId();
        } finally {
            if (transaccion.isActive()) transaccion.rollback();
            gestor.close();
        }
    }

    /** find busca por clave primaria y devuelve null si no existe. */
    private static Persona buscar(EntityManagerFactory factoria, Long id) {
        EntityManager gestor = factoria.createEntityManager();
        try {
            return gestor.find(Persona.class, id);
        } finally {
            gestor.close();
        }
    }

    /** Cambia una entidad gestionada; JPA sincroniza los cambios al confirmar. */
    private static boolean actualizar(EntityManagerFactory factoria, Long id, String nombre, int edad) {
        EntityManager gestor = factoria.createEntityManager();
        EntityTransaction transaccion = gestor.getTransaction();
        try {
            transaccion.begin();
            Persona persona = gestor.find(Persona.class, id);
            if (persona != null) {
                persona.setNombre(nombre);
                persona.setEdad(edad);
            }
            transaccion.commit();
            return persona != null;
        } finally {
            if (transaccion.isActive()) transaccion.rollback();
            gestor.close();
        }
    }

    /** merge copia los cambios de una entidad separada a otra entidad gestionada. */
    private static void fusionar(EntityManagerFactory factoria, Persona separada) {
        EntityManager gestor = factoria.createEntityManager();
        EntityTransaction transaccion = gestor.getTransaction();
        try {
            transaccion.begin();
            Persona gestionada = gestor.merge(separada);
            System.out.println("Entidad enviada gestionada: " + gestor.contains(separada));
            System.out.println("Entidad devuelta gestionada: " + gestor.contains(gestionada));
            transaccion.commit();
        } finally {
            if (transaccion.isActive()) transaccion.rollback();
            gestor.close();
        }
    }

    /** JPQL consulta entidades Persona, no el nombre físico de la tabla. */
    private static void listar(EntityManagerFactory factoria) {
        EntityManager gestor = factoria.createEntityManager();
        try {
            List<Persona> personas = gestor.createQuery(
                    "select p from Persona p order by p.id", Persona.class).getResultList();
            for (Persona persona : personas) {
                System.out.println("  " + persona);
            }
        } finally {
            gestor.close();
        }
    }

    /** remove necesita una entidad gestionada, obtenida aquí con find. */
    private static boolean borrar(EntityManagerFactory factoria, Long id) {
        EntityManager gestor = factoria.createEntityManager();
        EntityTransaction transaccion = gestor.getTransaction();
        try {
            transaccion.begin();
            Persona persona = gestor.find(Persona.class, id);
            if (persona != null) gestor.remove(persona);
            transaccion.commit();
            return persona != null;
        } finally {
            if (transaccion.isActive()) transaccion.rollback();
            gestor.close();
        }
    }
}
