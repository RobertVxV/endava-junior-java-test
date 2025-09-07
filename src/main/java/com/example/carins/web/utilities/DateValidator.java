package com.example.carins.web.utilities;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class DateValidator {

    private static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.now().plusYears(50);

    public LocalDate validateAndParse(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid date format. Use YYYY-MM-DD");
        }

        if (date.isBefore(MIN_DATE) || date.isAfter(MAX_DATE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Date must be between %s and %s", MIN_DATE, MAX_DATE));
        }

        return date;
    }
}