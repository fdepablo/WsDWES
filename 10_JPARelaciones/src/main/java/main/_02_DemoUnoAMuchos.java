package main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Cliente;
import modelo.entidad.Direccion;
import modelo.entidad.Pedido;

import java.time.LocalDateTime;
import java.util.List;

/** Segunda demostración: varios pedidos pertenecen a un cliente mediante ManyToOne. */
public class _02_DemoUnoAMuchos {
    public static void main(String[] args) {
        try (EntityManagerFactory factoria = Persistence.createEntityManagerFactory("RelacionesJPA")) {
            Long idCliente = crearPedidos(factoria);
            consultarPedidos(factoria, idCliente);
        }
    }

    /** Cada pedido guarda la referencia al mismo cliente; no hay cascada hacia él. */
    private static Long crearPedidos(EntityManagerFactory factoria) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                // Creamos nuestros propios datos: no depende de ejecutar _01_DemoUnoAUno.
                Cliente cliente = new Cliente("Beatriz", new Direccion("Calle Sol, 2", "Sevilla"));
                gestor.persist(cliente);
                gestor.persist(new Pedido("PED-A", LocalDateTime.now(), cliente));
                gestor.persist(new Pedido("PED-B", LocalDateTime.now(), cliente));
                transaccion.commit();
                return cliente.getId();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /**
     * Busca mediante JPQL los pedidos del cliente cuyo ID recibimos.
     * Como Cliente no tiene una lista de pedidos, los obtenemos consultando
     * el atributo cliente de cada Pedido.
     */
    private static void consultarPedidos(EntityManagerFactory factoria, Long idCliente) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                // :id es un parámetro con nombre, similar al ? de un PreparedStatement.
                // setParameter("id", idCliente) le asigna el valor, como setLong(1, idCliente)
                // en JDBC, pero usando su nombre en lugar de su posición.
                List<Pedido> pedidos = gestor.createQuery(
                        "select p from Pedido p where p.cliente.id = :id order by p.id", Pedido.class)
                        .setParameter("id", idCliente)
                        .getResultList();
                for (Pedido pedido : pedidos) {
                    System.out.println("Pedido: " + pedido.getCodigo() + " -> cliente "
                            + pedido.getCliente().getId() + " - " + pedido.getCliente().getNombre());
                }
                transaccion.commit();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }
}
