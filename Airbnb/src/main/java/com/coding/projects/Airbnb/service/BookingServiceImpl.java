package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.dto.BookingDto;
import com.coding.projects.Airbnb.dto.BookingRequest;
import com.coding.projects.Airbnb.dto.GuestDto;
import com.coding.projects.Airbnb.dto.HotelReportDto;
import com.coding.projects.Airbnb.entity.*;
import com.coding.projects.Airbnb.entity.enums.BookingStatus;
import com.coding.projects.Airbnb.exception.ResourceNotFoundException;
import com.coding.projects.Airbnb.exception.UnAuthorisedException;
import com.coding.projects.Airbnb.repository.*;
import com.coding.projects.Airbnb.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCancelParams;
import com.stripe.param.RefundCreateParams;
import jakarta.servlet.http.PushBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.coding.projects.Airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements IBookingService{

    private final GuestRepository guestRepository;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private  final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;


    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("INITIALISING THE ROOM BETWEEN {} TO {}", bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        Hotel hotel = hotelRepository
                .findById(bookingRequest.getHotelId())
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id {}"+ bookingRequest.getHotelId()));

        Room room = roomRepository
                .findById(bookingRequest.getRoomId())
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id {}"+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository
                .findAndLockAvailableInventory(
                        room.getId(),
                        bookingRequest.getCheckInDate(),
                        bookingRequest.getCheckOutDate(),
                        bookingRequest.getRoomsCount()
                );
        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;
        if(inventoryList.size()!= daysCount){
            throw new IllegalStateException("Rooms not available anymore!!!");
        }

        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking = Booking
                .builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("ADDING GUEST FOR BOOKING WITH ID {}", bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id {}"+ bookingId));

        User user = getCurrentUser();
        log.info("Authenticated User ID: {}", user.getId());
        log.info("Booking Owner User ID: {}", booking.getUser().getId());

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking does not belong to this user with id "+ user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("SORRY BOOKING HAS ALREADY EXPIRED!");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("SORRY BOOKING IS IN WRONG STATUS, CANNOT PROCEED TO ADD GUEST!");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

//    PAYMENT RELATED METHOD
    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id "+ bookingId)
        );

        User user = getCurrentUser();

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking does not belong to this user with id "+ user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("SORRY BOOKING HAS ALREADY EXPIRED!");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"/payments/success", frontendUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;

    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            Session session = Session.GSON.fromJson(event.getData().getObject().toJson(), Session.class);
            if(session == null) return;

            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                    .orElseThrow(()-> new ResourceNotFoundException("Booking not found for sessionId "+ sessionId ));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                    booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),
                    booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("SUCCESSFULLY CONFIRMED THE BOOKING FOR ID {} ", booking.getId());

        }else{
            log.warn("unhandled event type {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id "+ bookingId)
        );

        User user = getCurrentUser();

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking does not belong to this user with id "+ user.getId());
        }

        if(booking.getBookingStatus()!= BookingStatus.CONFIRMED){
            throw new IllegalStateException("Can only cancel confirmed booking");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        // HANDLE REFUND

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id "+ bookingId)
        );

        User user = getCurrentUser();

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnAuthorisedException("Booking does not belong to this user with id "+ user.getId());
        }

        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id {}"+ hotelId));

        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("Hotel does not belong to this user with id "+ user.getId());
        }

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings
                .stream()
                .map((element) -> modelMapper.map(element, BookingDto.class)).collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {

        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id {}"+ hotelId));

        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("Hotel does not belong to this user with id "+ user.getId());
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = startDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);
        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings==0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);


        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUser(user)
                .stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());

    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
