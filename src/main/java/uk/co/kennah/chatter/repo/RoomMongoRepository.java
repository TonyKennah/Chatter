package uk.co.kennah.chatter.repo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import uk.co.kennah.chatter.model.RoomMongoDocument;

@Repository
@Profile("prod")
public interface RoomMongoRepository extends MongoRepository<RoomMongoDocument, String> {}