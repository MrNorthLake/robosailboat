package fi.robosailboat.webservice;

//import fi.robosailboat.webservice.robosailboatLib.util.PasswordCreator;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@EnableAutoConfiguration
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class WebserviceApplication {

	public static void main(String[] args) throws MqttException {
		SpringApplication.run(WebserviceApplication.class, args);
		//PasswordCreator.createPassword();


	}

}
