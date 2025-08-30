package com.coding.projects.Airbnb.service;

import com.coding.projects.Airbnb.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);

}
