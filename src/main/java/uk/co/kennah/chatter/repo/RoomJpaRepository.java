package uk.co.kennah.chatter.repo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.co.kennah.chatter.model.RoomJpaEntity;

@Repository
@Profile("local")
public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, String> {}

