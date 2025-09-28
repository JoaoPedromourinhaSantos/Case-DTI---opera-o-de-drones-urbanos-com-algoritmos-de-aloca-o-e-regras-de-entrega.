package com.dti.dronesimulator.repository;
import com.dti.dronesimulator.model.Drone;
import com.dti.dronesimulator.enums.DroneState;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Repository
public class DroneRepository {
    private final Map<Long, Drone> drones = new HashMap<>();
    private Long nextId = 1L;

    public DroneRepository() {
        // Drones de exemplo com diferentes capacidades (X kg) e autonomias (Y km)
        drones.put(nextId, new Drone(nextId++, 10.0, 50.0)); 
        drones.put(nextId, new Drone(nextId++, 5.0, 30.0)); 
        drones.put(nextId, new Drone(nextId++, 8.0, 60.0));
    }

    public Collection<Drone> findAll() { return drones.values(); }
    public Drone findById(Long id) { return drones.get(id); }
    
    // Método para atualizar o estado do drone no repositório (importante após simulação)
    public void save(Drone drone) {
        if (drone.getId() != null) {
            drones.put(drone.getId(), drone);
        }
    }
    
    public List<Drone> findAvailableDrones() {
        return drones.values().stream()
                // Apenas drones IDLE (na base e prontos para voar)
                .filter(d -> d.getEstado() == DroneState.IDLE) 
                .toList();
    }
}