package com.dti.dronesimulator.repository;
import com.dti.dronesimulator.model.Pedido;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PedidoRepository {
    private final Map<Long, Pedido> pedidos = new HashMap<>();
    private Long nextId = 1000L;

    public void save(Pedido pedido) {
        if (pedido.getId() == null) { 
            pedido.setId(nextId++); 
        }
        pedidos.put(pedido.getId(), pedido);
    }
    
    public Pedido findById(Long id) {
        return pedidos.get(id);
    }

    public List<Pedido> findPendingOrders() {
        return pedidos.values().stream()
                .filter(p -> !p.isAlocado())
                .toList();
    }
}