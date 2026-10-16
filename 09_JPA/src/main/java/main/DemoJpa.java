package main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Persona;

import java.util.List;

/** Ejecuta el CRUD y compara dos formas de modificar una entidad con JPA. */
public class DemoJpa {
    public static void main(String[] args) {
        try (EntityManagerFactory factoria = Persistence.createEntityManagerFactory("PersonasJPA")) {
            Long idAna = crear(factoria, new Persona("Ana", 25, 62.5));
            Long idLuis = crear(factoria, new Persona("Luis", 31, 78.0));
            System.out.println("Creada Ana con ID " + idAna + "; Luis con ID " + idLuis);

            System.out.println("Búsqueda por ID: " + buscar(factoria, idAna));
            System.out.println("Actualización con find: " + actualizarConFind(factoria, idAna, "Ana María", 26));
            System.out.println("Resultado con find: " + buscar(factoria, idAna));
            Persona separada = buscar(factoria, idAna);
            // buscar ya ha cerrado su gestor: modificamos una entidad separada.
            separada.setNombre("Ana López");
            separada.setEdad(27);
            actualizarConMerge(factoria, separada);
            System.out.println("Resultado con merge: " + buscar(factoria, idAna));
            System.out.println("Personas tras actualizar:");
            listar(factoria);

            System.out.println("Borrado de Luis: " + borrar(factoria, idLuis));
            System.out.println("Personas tras borrar:");
            listar(factoria);
        }
    }

    /** persist incorpora una entidad nueva al contexto y el commit guarda la fila. */
    private static Long crear(EntityManagerFactory factoria, Persona persona) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                gestor.persist(persona);
                transaccion.commit();
                return persona.getId();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** find busca por clave primaria y devuelve null si no existe. */
    private static Persona buscar(EntityManagerFactory factoria, Long id) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Persona persona = gestor.find(Persona.class, id);
                transaccion.commit();
                return persona;
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /**
     * Utiliza este enfoque cuando buscamos y modificamos la persona con el mismo gestor.
     * La entidad obtenida con find queda gestionada: basta con cambiar sus campos mediante
     * setters y confirmar la transacción. No es necesario llamar a merge.
     * Aquí modificamos solo nombre y edad; el peso conserva el valor que hemos leído.
     * Este enfoque permite elegir explícitamente los campos que actualiza un formulario.
     *
     * @param factoria factoría con la que se abre el gestor de esta operación
     * @param id identificador de la persona que queremos modificar
     * @param nombre nuevo nombre
     * @param edad nueva edad
     * @return true si la persona existe y se actualiza; false si no existe
     */
    private static boolean actualizarConFind(EntityManagerFactory factoria, Long id, String nombre, int edad) {
        try (EntityManager gestor = factoria.createEntityManager()) {
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
            }
        }
    }

    /**
     * Utiliza este enfoque cuando ya tenemos una persona separada de su gestor,
     * por ejemplo, porque el gestor que la obtuvo se ha cerrado, y hemos cambiado sus datos.
     * merge copia esos datos a una entidad gestionada por el nuevo gestor; el objeto recibido
     * sigue separado. Si queremos hacer más cambios dentro de esta transacción, debemos usar
     * la instancia devuelta por merge. El commit confirma los cambios en la base de datos.
     *
     * En esta entidad, merge copia nombre, edad y peso, aunque solo hayamos cambiado
     * nombre y edad. Un null o un 0 no significa "no modificar": también puede sobrescribir
     * el dato guardado, o provocar un error si incumple una restricción. Por eso usamos una
     * persona completa obtenida previamente de la base, no un objeto con campos sin rellenar.
     * Esto describe el estado que se copia; no implica que el UPDATE generado incluya
     * necesariamente todas las columnas.
     *
     * @param factoria factoría con la que se abre el nuevo gestor
     * @param separada persona modificada fuera de su gestor original; en este ejemplo,
     *                 su ID corresponde a una persona que sabemos que existe en la base
     */
    private static void actualizarConMerge(EntityManagerFactory factoria, Persona separada) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Persona gestionada = gestor.merge(separada);
                // Ampliación opcional: contains indica si este gestor gestiona el objeto,
                // no si la persona existe en la base de datos.
                System.out.println("Entidad enviada gestionada: " + gestor.contains(separada));
                System.out.println("Entidad devuelta gestionada: " + gestor.contains(gestionada));
                transaccion.commit();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** JPQL consulta entidades Persona, no el nombre físico de la tabla. */
    private static void listar(EntityManagerFactory factoria) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                List<Persona> personas = gestor.createQuery(
                        "select p from Persona p order by p.id", Persona.class).getResultList();
                transaccion.commit();
                for (Persona persona : personas) {
                    System.out.println("  " + persona);
                }
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** remove necesita una entidad gestionada, obtenida aquí con find. */
    private static boolean borrar(EntityManagerFactory factoria, Long id) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Persona persona = gestor.find(Persona.class, id);
                if (persona != null) gestor.remove(persona);
                transaccion.commit();
                return persona != null;
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }
}
