package com.batch.loader;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Persona, Long> {
    
    Optional<Persona> findByNombre(String nombre);
    
}
