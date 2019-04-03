package fi.robosailboat.webservice.robosailboatLib.repository;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggingRepository extends MongoRepository<SensorData,String> {
}
