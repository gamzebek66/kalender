package com.hellomateo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TerminRepository extends MongoRepository<Termin, String> {

}