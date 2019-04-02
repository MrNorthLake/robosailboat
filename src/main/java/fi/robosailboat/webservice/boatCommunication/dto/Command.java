package fi.robosailboat.webservice.boatCommunication.dto;

import lombok.Data;

@Data
public class Command {

    private int rodderCommand;
    private int sailCommand;

    public Command(final int one, final int two){
        this.rodderCommand = one;
        this.sailCommand = two;
    }
}
