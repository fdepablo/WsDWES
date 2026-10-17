package main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import modelo.entidad.Cliente;
import modelo.entidad.DatosBancarios;
import modelo.entidad.Direccion;

/** Primera demostración: dirección embebida y relación uno a uno con los datos bancarios. */
public class _01_DemoUnoAUno {
    public static void main(String[] args) {
        try (EntityManagerFactory factoria = Persistence.createEntityManagerFactory("RelacionesJPA")) {
            Long idCliente = crearCliente(factoria);
            consultarCliente(factoria, idCliente);
        }
    }

    /** La cascada PERSIST guarda los datos bancarios al guardar el cliente. */
    private static Long crearCliente(EntityManagerFactory factoria) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Direccion direccion = new Direccion("Calle Mayor, 1", "Madrid");
                //La dirección va embebida en la tabla de cliente
                Cliente cliente = new Cliente("Ana", direccion);
                //Los datos bancarios van en una tabla nueva
                cliente.setDatosBancarios(new DatosBancarios("Banco de ejemplo", "CUENTA-DEMO"));
                gestor.persist(cliente);
                transaccion.commit();
                return cliente.getId();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }

    /** Un nuevo gestor permite comprobar los datos que se han guardado. */
    private static void consultarCliente(EntityManagerFactory factoria, Long idCliente) {
        try (EntityManager gestor = factoria.createEntityManager()) {
            EntityTransaction transaccion = gestor.getTransaction();
            try {
                transaccion.begin();
                Cliente cliente = gestor.find(Cliente.class, idCliente);
                System.out.println("Cliente: " + cliente.getId() + " - " + cliente.getNombre());
                System.out.println("Ciudad (embebida): " + cliente.getDireccion().getCiudad());
                System.out.println("Banco: " + cliente.getDatosBancarios().getBanco());
                transaccion.commit();
            } finally {
                if (transaccion.isActive()) transaccion.rollback();
            }
        }
    }
}
