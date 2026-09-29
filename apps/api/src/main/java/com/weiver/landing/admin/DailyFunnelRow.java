package com.weiver.landing.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyFunnelRow(
    LocalDate day,
    long uniqueVisitors,
    long sessions,
    long reservingVisitors,
    long reservations,
    BigDecimal visitorConversionRate
) {}
