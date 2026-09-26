package main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Cliente;
import modelo.entidad.Comercial;
import modelo.entidad.DatosBancarios;
import modelo.entidad.Direccion;
import modelo.entidad.Pedido;

import java.time.LocalDateTime;
import java.util.List;

/** Recorre las relaciones de menor a mayor complejidad desde un solo sentido. */
public class DemoRelaciones {
    public static void main(String[] args) {
        try (EntityManagerFactory factoria = Persistence.createEntityManagerFactory("RelacionesPU")) {
            Long idCliente = crearClienteConDireccionYDatos(factoria);
            crearPedidos(factoria, idCliente);
            asignarComerciales(factoria, idCliente);
            consultar(factoria, idCliente);
        }
    }

    /** @Embedded no crea otra tabla; @OneToOne con PERSIST guarda ambos objetos. */
    private static Long crearClienteConDireccionYDatos(EntityManagerFactory factoria) {
        Cliente cliente = new Cliente("Ana", new Direccion("Calle Mayor, 1", "Madrid"));
        cliente.setDatosBancarios(new DatosBancarios("Banco de ejemplo", "CUENTA-DEMO"));
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                gestor.persist(cliente);
                transaccion.commit();
                System.out.println("Cliente y datos bancarios creados. ID: " + cliente.getId());
                return cliente.getId();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** La FK de la relación uno-a-muchos está en cada pedido. */
    private static void crearPedidos(EntityManagerFactory factoria, Long idCliente) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Cliente cliente = gestor.find(Cliente.class, idCliente);
                gestor.persist(new Pedido("PED-A", LocalDateTime.now(), cliente));
                gestor.persist(new Pedido("PED-B", LocalDateTime.now(), cliente));
                transaccion.commit();
                System.out.println("Dos pedidos asociados al cliente " + idCliente);
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** Primero guarda los comerciales: no hay cascada sobre datos compartidos. */
    private static void asignarComerciales(EntityManagerFactory factoria, Long idCliente) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Comercial lucia = new Comercial("Lucía");
                Comercial mario = new Comercial("Mario");
                gestor.persist(lucia);
                gestor.persist(mario);
                Cliente cliente = gestor.find(Cliente.class, idCliente);
                cliente.getComerciales().add(lucia);
                cliente.getComerciales().add(mario);
                transaccion.commit();
                System.out.println("Dos comerciales asociados al cliente " + idCliente);
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** La consulta JPQL sustituye la lista inversa Cliente.pedidos. */
    private static void consultar(EntityManagerFactory factoria, Long idCliente) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            Cliente cliente = gestor.find(Cliente.class, idCliente);
            System.out.println("Cliente: " + cliente.getNombre() + ", ciudad: " + cliente.getDireccion().getCiudad());
            System.out.println("Banco: " + cliente.getDatosBancarios().getBanco());

            List<Pedido> pedidos = gestor.createQuery(
                    "select p from Pedido p where p.cliente.id = :id order by p.id", Pedido.class)
                    .setParameter("id", idCliente)
                    .getResultList();
            for (Pedido pedido : pedidos) {
                System.out.println("Pedido: " + pedido.getCodigo() + " -> " + pedido.getCliente().getNombre());
            }
            for (Comercial comercial : cliente.getComerciales()) {
                System.out.println("Comercial: " + comercial.getNombre());
            }
        }
    }
}
