package com.tp.jpa.repository;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import jakarta.persistence.EntityManager;

import java.util.List;

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
}
