package fi.robosailboat.webservice.robosailboatLib.repository;

import fi.robosailboat.webservice.boatCommunication.dto.SensorData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoggingRepository extends MongoRepository<SensorData,String> {
}
