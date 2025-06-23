package com.parkit.parkingsystem;


import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private Ticket ticket;

    @BeforeEach
    public void setUpPerTest() {
        try {

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
    
    private Ticket createFakeTicket() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        return ticket;
    }
    
    @Test
    void testProcessIncomingVehicle() throws Exception {
    	// GIVEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

        // WHEN
        parkingService.processIncomingVehicle();

        // THEN
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
    }
    
    @Test
    public void processExitingVehicleTest() throws Exception {
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.hasVisited("ABCDEF")).thenReturn(false);

        
        parkingService.processExitingVehicle();
        
        verify(ticketDAO, times(1)).getTicket("ABCDEF");
        verify(ticketDAO, times(2)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).hasVisited("ABCDEF");
    }
    
    @Test
    void testProcessIncomingVehicle() throws Exception {
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        
        parkingService.processIncomingVehicle();
        
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
    }
    
    @Test
    void processExitingVehicleTestUnableUpdate() throws Exception {
    	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.hasVisited("ABCDEF")).thenReturn(false);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getTicket("ABCDEF");
        verify(ticketDAO, times(2)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).hasVisited("ABCDEF");
    }
    
    @Test
    void testGetNextParkingNumberIfAvailable() throws Exception {
    	when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
        assertTrue(parkingSpot.isAvailable());

        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }
    
    @Test
    void testGetNextParkingNumberIfAvailableParkingNotFound() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(parkingSpot);
        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }
    
    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception {
    	when(inputReaderUtil.readSelection()).thenReturn(3);

    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
    	assertNull(parkingSpot);
        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any());
    }
}
