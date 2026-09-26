package modelo.servicio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Persona;

import java.util.List;

/** Reúne las operaciones JPA que necesita la interfaz web. */
public class PersonaServicio implements AutoCloseable {
    private final EntityManagerFactory factoria = Persistence.createEntityManagerFactory("PersonasPU");

    /** Cada petición usa su propio EntityManager; la factoría se comparte. */
    public List<Persona> listar() {
        try (EntityManager gestor = factoria.createEntityManager()) {
            return gestor.createQuery("select p from Persona p order by p.id", Persona.class).getResultList();
        }
    }

    public Persona buscar(long id) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            return gestor.find(Persona.class, id);
        }
    }

    /** Guarda una persona nueva dentro de una transacción. */
    public void crear(String nombre, int edad, double peso) {
        validar(nombre, edad, peso);
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                gestor.persist(new Persona(nombre.trim(), edad, peso));
                transaccion.commit();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** Modifica la entidad gestionada: el commit sincroniza los cambios. */
    public boolean actualizar(long id, String nombre, int edad, double peso) {
        validar(nombre, edad, peso);
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Persona persona = gestor.find(Persona.class, id);
                if (persona == null) {
                    transaccion.rollback();
                    return false;
                }
                persona.setNombre(nombre.trim());
                persona.setEdad(edad);
                persona.setPeso(peso);
                transaccion.commit();
                return true;
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** remove requiere la entidad gestionada obtenida con find. */
    public boolean eliminar(long id) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Persona persona = gestor.find(Persona.class, id);
                if (persona == null) {
                    transaccion.rollback();
                    return false;
                }
                gestor.remove(persona);
                transaccion.commit();
                return true;
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    private void validar(String nombre, int edad, double peso) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 80 || edad < 0 || edad > 150
                || !Double.isFinite(peso) || peso <= 0) {
            throw new IllegalArgumentException("Introduce un nombre, una edad entre 0 y 150 y un peso positivo.");
        }
    }

    @Override
    public void close() {
        factoria.close();
    }
}
