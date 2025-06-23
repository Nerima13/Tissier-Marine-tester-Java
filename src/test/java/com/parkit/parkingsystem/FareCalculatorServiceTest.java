package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

    private FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    
    private static ParkingSpot parkingSpotCarNotAvailable;
    private static ParkingSpot parkingSpotBikeNotAvailable;
    private static ParkingSpot parkingSpotEmptyNotAvailable;

    @Mock
    TicketDAO ticketDAO;

    @BeforeAll
    public static void setUp() {
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
        fareCalculatorService = new FareCalculatorService(ticketDAO);
    }

    private void setUpTicket(Date inTime, Date outTime, ParkingSpot parkingSpot) {
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    }

    @Test
    public void calculateFareCar(){
    	// GIVEN
    	Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour parking time ago
        Date outTime = new Date();

        setUpTicket(inTime, outTime, parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        assertEquals(ticket.getPrice(), 1 * Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    void calculateFareBike() {
    	// GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 60 * 60 * 1000); // 1 hour parking time ago
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        setUpTicket(inTime, outTime, parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    void calculateFareUnkownType() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 60 * 60 * 1000); // 1 hour parking time ago
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        setUpTicket(inTime, outTime, parkingSpot);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareCarWithFutureInTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour parking time later
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        setUpTicket(inTime, outTime, parkingSpot);

        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare

        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        setUpTicket(inTime, outTime, parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 24 hours parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        setUpTicket(inTime, outTime, parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
    
    @Test
    void calculateFareCarWithLessThanThirtyMinParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 15 * 60 * 1000); // 15 minutes parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(0.0, ticket.getPrice());
    }
    
    @Test
    void calculateFareBikeWithLessThanThirtyMinParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 15 * 60 * 1000); // 15 minutes parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        setUpTicket(inTime, outTime, parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }
    
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
    	Date inTime = new Date();
    	inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000)); // 30 minutes parking time or less should be free
        Date outTime = new Date();
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        setUpTicket(inTime, outTime, parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }
    
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
    	Date inTime = new Date();
    	inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000)); // 30 minutes parking time or less should be free
        Date outTime = new Date();
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        setUpTicket(inTime, outTime, parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }
    
    @Test
    public void calculateFareCarWithDiscount() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h parking time ago
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        setUpTicket(inTime, outTime, parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.hasVisited("ABCDEF")).thenReturn(true);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0.95 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h parking time ago
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        setUpTicket(inTime, outTime, parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.hasVisited("ABCDEF")).thenReturn(true);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0.95 * Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }
}
