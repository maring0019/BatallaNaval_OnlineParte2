package com.codeoftheweb.salvo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;


@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
    //Para solucionar el problema de que acepta dos usuarios con mismo nombre, problema de concurrencia, hilos.
  // @Look(LockModeType.PESSIMISTIC_READ)
    Player findByUserName(String userName);
}


