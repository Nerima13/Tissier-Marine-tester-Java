package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Date;



@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static FareCalculatorService  fareCalculatorService;

    private static final int hours = 24;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        fareCalculatorService = new FareCalculatorService(ticketDAO);
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();    
    }

    @AfterAll
    public static void tearDown(){
    }

    @Test
    public void testParkingACar() {
        // Arrange
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Act
        parkingService.processIncomingVehicle();

        // Assert
        assertNotNull(ticketDAO.getTicket("ABCDEF"));
        assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }

    @Test
    public void testParkingLotExit() {
        // Arrange
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        testParkingACar();

        // Act
        parkingService.processExitingVehicle();

        // Assert
        assertNotNull(ticketDAO.getTicket("ABCDEF"));
        assertEquals(0, ticketDAO.getTicket("ABCDEF").getPrice());
        assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
    }

    @Test
    public void calculateFareCarRecurrentUser(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        testParkingACar();
        parkingService.processExitingVehicle();

        testParkingACar();
        parkingService.processExitingVehicle();

        modifyOutTime();
        fareCalculatorService.calculateFare(ticketDAO.getTicket("ABCDEF"));

        double expectedPrice = hours *Fare.CAR_RATE_PER_HOUR * 0.95;
        assertEquals(expectedPrice, ticketDAO.getTicket("ABCDEF").getPrice(), 0.01);
    }

    private void modifyOutTime(){
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() + (hours * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour

        Ticket lastTicket = ticketDAO.getTicket("ABCDEF");
        lastTicket.setOutTime(outTime);
        ticketDAO.updateTicket(lastTicket);
    }
}
