package com.batch.loader;

import java.util.Optional;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonaItemProcessor implements ItemProcessor<Persona, Persona> {
    
    @Autowired
    public PersonRepository pesonaRepository;
    
    @Override
    public Persona process(final Persona person) throws Exception {
        Optional<Persona> personaBuscada = pesonaRepository.findByNombre(person.getNombre());

        // aca los nombres son unicos, entonces si viene un nombre con un apellido diferente se va a modificar por este
        if (!personaBuscada.isPresent()) {
            return person;
        } else {
            if (person.getApellido().equals(personaBuscada.get().getApellido())) {
                return null;
            } else {
                pesonaRepository.delete(personaBuscada.get());
                return person;
            }
        }
        
    }
    
}
