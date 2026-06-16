package com.tp.jpa.repository;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de Pedido. Además del CRUD heredado implementa consultas por
 * usuario y por estado.
 */
public class PedidoRepository extends BaseRepository<Pedido> {

    public PedidoRepository() {
        super(Pedido.class);
    }

    /**
     * Retorna los pedidos activos del usuario indicado.
     */
    public List<Pedido> buscarPorUsuario(Long idUsuario) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL: navega desde Usuario a través de u.pedidos y filtra por
            // u.id = :uid y p.eliminado = false (excluye bajas lógicas).
            String jpql = "SELECT p FROM Usuario u JOIN u.pedidos p "
                    + "WHERE u.id = :uid AND p.eliminado = false";
            return em.createQuery(jpql, Pedido.class)
                    .setParameter("uid", idUsuario)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retorna los pedidos activos que coinciden con el estado indicado.
     */
    public List<Pedido> buscarPorEstado(Estado estado) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL: retorna los pedidos activos con el estado indicado.
            // Útil para filtrar PENDIENTE, CONFIRMADO, TERMINADO o CANCELADO.
            String jpql = "SELECT p FROM Pedido p "
                    + "WHERE p.estado = :estado AND p.eliminado = false";
            return em.createQuery(jpql, Pedido.class)
                    .setParameter("estado", estado)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Da de alta un pedido completo dentro de una única transacción atómica.
     * Recibe el id del usuario, la forma de pago y un mapa idProducto->cantidad.
     * Recupera cada Producto gestionado, valida disponibilidad y stock, arma los
     * DetallePedido, calcula el total y descuenta el stock. Si algo falla, hace
     * rollback completo. Retorna el Pedido persistido (con sus detalles cargados)
     * o null si el usuario no existe o está dado de baja.
     */
    public Pedido crearPedido(Long usuarioId, FormaPago formaPago, Map<Long, Integer> items) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario usuario = em.find(Usuario.class, usuarioId);
            if (usuario == null || usuario.isEliminado()) {
                tx.rollback();
                return null;
            }
            Pedido pedido = Pedido.builder()
                    .fecha(LocalDate.now())
                    .estado(Estado.PENDIENTE)
                    .formaPago(formaPago)
                    .build();
            for (Map.Entry<Long, Integer> item : items.entrySet()) {
                Producto producto = em.find(Producto.class, item.getKey());
                int cantidad = item.getValue();
                if (producto == null || producto.isEliminado()
                        || !Boolean.TRUE.equals(producto.getDisponible())
                        || producto.getStock() < cantidad) {
                    throw new IllegalStateException(
                            "Producto inválido o sin stock suficiente (ID " + item.getKey() + ").");
                }
                // addDetallePedido crea el DetallePedido y calcula su subtotal.
                pedido.addDetallePedido(cantidad, producto);
                // Producto gestionado: el descuento de stock se sincroniza al commit.
                producto.setStock(producto.getStock() - cantidad);
            }
            pedido.calcularTotal();
            em.persist(pedido);
            // Asocia el pedido al usuario (setea usuario_id al hacer commit).
            usuario.getPedidos().add(pedido);
            em.flush();
            pedido.getDetalles().size(); // fuerza la carga antes de cerrar el EM
            tx.commit();
            return pedido;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Retorna el usuario al que pertenece un pedido (consulta inversa, ya que
     * Pedido no conoce su Usuario). Optional.empty() si no tiene.
     */
    public Optional<Usuario> buscarUsuarioPorPedido(Long pedidoId) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL: busca el usuario cuya colección de pedidos contiene el id dado.
            String jpql = "SELECT u FROM Usuario u JOIN u.pedidos p WHERE p.id = :pid";
            return em.createQuery(jpql, Usuario.class)
                    .setParameter("pid", pedidoId)
                    .getResultList()
                    .stream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
