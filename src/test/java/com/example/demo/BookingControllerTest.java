package com.example.demo;

import com.example.demo.Booking.controllerBooking.BookingController;
import com.example.demo.Booking.dtoBooking.Booking.*;
import com.example.demo.Booking.entityBooking.BookingStatus;
import com.example.demo.Booking.servicesBooking.BookingService;
import com.example.demo.ExceptionHandler.BookingException;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@ActiveProfiles("test")
class BookingControllerTest {

    /*@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private BookingResponse buildBookingResponse(Long id, BookingStatus status) {
        BookingResponse response = new BookingResponse();
        response.setId(id);
        response.setGuestId(1L);
        response.setGuestName("John Doe");
        response.setHotelId(1L);
        response.setHotelName("Grand Hotel");
        response.setRoomId(1L);
        response.setRoomType("DELUXE");
        response.setCheckInDate(LocalDateTime.now().plusDays(5));
        response.setCheckOutDate(LocalDateTime.now().plusDays(8));
        response.setTotalAmount(new BigDecimal("450.00"));
        response.setStatus(status);
        return response;
    }

    private CreateBookingRequest buildCreateRequest() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuestId(1L);
        request.setRoomId(1L);
        request.setNumberOfGuests(2);
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(8));
        request.setCheckInTime(LocalTime.of(15, 0));
        request.setCheckOutTime(LocalTime.of(11, 0));
        return request;
    }

    // ── Create Booking ──────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void createBooking_withValidData_returns201() throws Exception {
        CreateBookingRequest request = buildCreateRequest();
        BookingResponse response = buildBookingResponse(1L, BookingStatus.PENDING);

        Mockito.when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.guestName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void createBooking_withRoomUnavailable_returns400() throws Exception {
        CreateBookingRequest request = buildCreateRequest();

        Mockito.when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenThrow(new BookingException("Room is not available for selected dates"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Get Booking ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getBooking_whenExists_returns200() throws Exception {
        BookingResponse response = buildBookingResponse(1L, BookingStatus.CONFIRMED);

        Mockito.when(bookingService.getBooking(1L)).thenReturn(response);

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser
    void getBooking_whenNotFound_returns404() throws Exception {
        Mockito.when(bookingService.getBooking(999L))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/api/bookings/999"))
                .andExpect(status().isNotFound());
    }

    // ── Pay Booking ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void payBooking_withValidAmount_returns200() throws Exception {
        PayBookingRequest request = new PayBookingRequest();
        request.setAmount(new BigDecimal("300.00"));

        BookingResponse response = buildBookingResponse(1L, BookingStatus.CONFIRMED);
        response.setAmountPaid(new BigDecimal("300.00"));

        Mockito.when(bookingService.payBooking(eq(1L), any(PayBookingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/bookings/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser
    void payBooking_withCancelledBooking_returns400() throws Exception {
        PayBookingRequest request = new PayBookingRequest();
        request.setAmount(new BigDecimal("100.00"));

        Mockito.when(bookingService.payBooking(eq(1L), any(PayBookingRequest.class)))
                .thenThrow(new BookingException("Cannot pay cancelled booking"));

        mockMvc.perform(post("/api/bookings/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Cancel Booking ──────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void cancelBooking_returns200() throws Exception {
        CancelBookingRequest request = new CancelBookingRequest();
        request.setReason("Change of plans");

        BookingResponse response = buildBookingResponse(1L, BookingStatus.CANCELLED);
        response.setCancellationReason("Change of plans");

        Mockito.when(bookingService.cancelBooking(eq(1L), any(CancelBookingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/bookings/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ── Expire Pending Bookings ─────────────────────────────────────────────

    @Test
    @WithMockUser
    void expirePendingBookings_returns200() throws Exception {
        Mockito.doNothing().when(bookingService).expireOverduePendingBookings();

        mockMvc.perform(post("/api/bookings/expire-pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Expired pending bookings processed"));
    }

    // ── Filter Bookings ─────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void filterBookings_withStatusFilter_returns200() throws Exception {
        BookingSummary summary = new BookingSummary();
        summary.setId(1L);
        summary.setGuestName("John Doe");
        summary.setStatus(BookingStatus.CONFIRMED);

        Mockito.when(bookingService.filterBookings(any(com.example.demo.Booking.filterBooking.BookingFilterRequest.class)))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/bookings/filter")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].guestName").value("John Doe"));
    }

    // ── Check Availability ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    void checkAvailability_whenAvailable_returns200() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(8);
        LocalTime checkInTime = LocalTime.of(15, 0);
        LocalTime CheckOutTime = LocalTime.of(12, 0);

        Mockito.when(bookingService.isRoomAvailable(1L, checkIn, checkOut, checkInTime, CheckOutTime)).thenReturn(true);
        Mockito.when(bookingService.getQuote(1L, checkIn, checkOut, checkInTime, CheckOutTime))
                .thenReturn(new BigDecimal("450.00"));

        mockMvc.perform(get("/api/bookings/availability")
                        .param("roomId", "1")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString())
                        .param("checkInTime", checkInTime.toString())
                        .param("checkOutTime", CheckOutTime.toString())
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.quote").value(450.00));
    }

    @Test
    @WithMockUser
    void checkAvailability_whenNotAvailable_returns200() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(8);
        LocalTime checkInTime = LocalTime.of(15, 0);
        LocalTime CheckOutTime = LocalTime.of(12, 0);

        Mockito.when(bookingService.isRoomAvailable(1L,checkIn, checkOut, checkInTime , CheckOutTime)).thenReturn(false);

        mockMvc.perform(get("/api/bookings/availability")
                        .param("roomId", "1")
                                                .param("checkIn", checkIn.toString())
                                                .param("checkOut", checkOut.toString())
                                                .param("checkInTime", checkInTime.toString())
                                                .param("checkOutTime", CheckOutTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    // ── Get Quote ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getQuote_returns200() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(8);
        LocalTime checkInTime = LocalTime.of(15, 0);
        LocalTime CheckOutTime = LocalTime.of(12, 0);

        Mockito.when(bookingService.getQuote(1L, checkIn, checkOut , checkInTime , CheckOutTime))
                .thenReturn(new BigDecimal("450.00"));

        mockMvc.perform(get("/api/bookings/quote")
                        .param("roomId", "1")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString())
                        .param("checkInTime", checkInTime.toString())
                        .param("checkOutTime", CheckOutTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nights").value(3))
                .andExpect(jsonPath("$.total").value(450.00));
    }

    // ── Guest History ───────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getGuestHistory_returns200() throws Exception {
        BookingSummary summary = new BookingSummary();
        summary.setId(1L);
        summary.setGuestName("John Doe");

        Mockito.when(bookingService.getGuestBookingHistory(1L)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/bookings/guest/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ── Upcoming Bookings ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getUpcomingBookings_returns200() throws Exception {
        Mockito.when(bookingService.getUpcomingBookings()).thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }*/
}

