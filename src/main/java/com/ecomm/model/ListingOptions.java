package com.ecomm.model;

import java.util.List;

/** The fixed choices offered in the listing forms and filters. */
public final class ListingOptions {

    public static final List<String> FUEL_TYPES =
            List.of("Gasoline", "Diesel", "Electric", "Hybrid", "Plug-in Hybrid", "CNG", "LPG");

    public static final List<String> TRANSMISSIONS =
            List.of("Manual", "Automatic", "Semi-Automatic", "CVT");

    public static final List<String> BODY_TYPES =
            List.of("Sedan", "SUV", "Coupe", "Hatchback", "Wagon", "Convertible", "Pickup", "Electric", "Minivan");

    private ListingOptions() {
    }
}
