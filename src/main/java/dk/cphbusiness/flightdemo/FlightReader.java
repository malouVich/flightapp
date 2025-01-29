package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader
{

    public static void main(String[] args)
    {
        try
        {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);
            long minuts = calcTotalFlightTime(flightInfoDTOList, "Lufthansa");
            System.out.println("runde 1: samlet tid for alle lufthansa fly ; " + minuts + " minutter");
            double averageTime = averageFlightTime(flightInfoDTOList, "Lufthansa");
            System.out.println("runde 2: gennemsnits tid for alle lufthansa fly ; " + averageTime + " minutter");
            List<FlightInfoDTO> flightsBetween = flightsBetweenAirports(flightInfoDTOList, "Fukuoka", "Haneda Airport");
            System.out.println("runde 3: Fly i mellem to lufthavne " + flightsBetween);
            //flightsBetween.forEach(System.out::println);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException
    {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList)
    {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
                .map(flight ->
                {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    Duration duration = Duration.between(departure, arrival);
                    FlightInfoDTO flightInfo =
                            FlightInfoDTO.builder()
                                    .name(flight.getFlight().getNumber())
                                    .iata(flight.getFlight().getIata())
                                    .airline(flight.getAirline().getName())
                                    .duration(duration)
                                    .departure(departure)
                                    .arrival(arrival)
                                    .origin(flight.getDeparture().getAirport())
                                    .destination(flight.getArrival().getAirport())
                                    .build();

                    return flightInfo;
                })
                .toList();
        return flightInfoList;
    }

    public static long calcTotalFlightTime(List<FlightInfoDTO> flightList, String airline)
    {
        long result = flightList.stream()
                .filter(flight -> flight.getAirline() !=null)
                .filter(flight -> flight.getAirline().equals(airline))
                .mapToLong(flight -> flight.getDuration().toMinutes())
                .sum();

        return result;
    }

    public static double averageFlightTime(List<FlightInfoDTO> flightList, String airline){
        double averageResult = flightList.stream()
                .filter(flight -> flight.getAirline() !=null)
                .filter(flight -> flight.getAirline().equals(airline))
                .mapToDouble(flight -> flight.getDuration().toMinutes())
                .average()
                .orElse(0.0);

        return averageResult;
    }

//Add a new feature (make a list of flights that are operated between two specific airports. For example, all flights between Fukuoka and Haneda Airport)

    public static List<FlightInfoDTO> flightsBetweenAirports (List<FlightInfoDTO> flightList, String origin, String destination){

        List<FlightInfoDTO> betweenAirports = flightList.stream()
                .filter(flight -> flight.getOrigin() !=null && flight.getDestination() !=null)
                .filter(flight -> flight.getOrigin().equals(origin) && flight.getDestination().equals(destination))
                .collect(Collectors.toList());

        return betweenAirports;


    }
}