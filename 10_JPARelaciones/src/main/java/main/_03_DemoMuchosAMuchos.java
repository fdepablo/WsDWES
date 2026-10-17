package main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Cliente;
import modelo.entidad.Comercial;
import modelo.entidad.Direccion;

import java.util.List;

/** Tercera demostración: varios clientes comparten comerciales mediante una tabla intermedia. */
public class _03_DemoMuchosAMuchos {
    public static void main(String[] args) {
        try (EntityManagerFactory factoria = Persistence.createEntityManagerFactory("RelacionesJPA")) {
            List<Long> idsClientes = asignarComerciales(factoria);
            consultarClientes(factoria, idsClientes);
        }
    }

    /** Guardamos los comerciales explícitamente porque la relación no tiene cascada. */
    private static List<Long> asignarComerciales(EntityManagerFactory factoria) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Comercial lucia = new Comercial("Lucía");
                Comercial mario = new Comercial("Mario");
                gestor.persist(lucia);
                gestor.persist(mario);

                Cliente carmen = new Cliente("Carmen", new Direccion("Calle Luna, 3", "Valencia"));
                Cliente diego = new Cliente("Diego", new Direccion("Calle Mar, 4", "Alicante"));
                gestor.persist(carmen);
                gestor.persist(diego);

                carmen.getComerciales().add(lucia);
                carmen.getComerciales().add(mario);
                diego.getComerciales().add(lucia);
                // Lucía se comparte: hay dos comerciales y tres asociaciones, no tres comerciales.
                transaccion.commit();
                return List.of(carmen.getId(), diego.getId());
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** Recorremos las colecciones con el gestor abierto para permitir su carga diferida. */
    private static void consultarClientes(EntityManagerFactory factoria, List<Long> idsClientes) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                for (Long idCliente : idsClientes) {
                    Cliente cliente = gestor.find(Cliente.class, idCliente);
                    System.out.println("Cliente: " + cliente.getNombre());
                    for (Comercial comercial : cliente.getComerciales()) {
                        System.out.println("  Comercial: " + comercial.getId() + " - " + comercial.getNombre());
                    }
                }
                transaccion.commit();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }
}
