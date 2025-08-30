package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.entity.Booking;
import com.coding.projects.Airbnb.entity.User;
import com.coding.projects.Airbnb.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService{

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {

        log.info("Creating session for payment with id {}" ,booking.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info(String.valueOf(booking.getAmount()));

        try {
            // CREATE THE CUSTOMER PARAMS
            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();
            // PASS TO THE CUSTOMER
            Customer customer = Customer.create(customerCreateParams);


            // CREATE THE SESSION PARAMS
            SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName() +" : "+ booking.getRoom().getType())
                                                                    .setDescription("Booking id "+ booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            // PASS TO THE SESSION
            Session session = Session.create(sessionCreateParams);

            booking.setPaymentSessionId(session.getId());

            bookingRepository.save(booking);

            log.info("session created successfully for payment with id {}" ,booking.getId());
            return session.getUrl();

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
}
