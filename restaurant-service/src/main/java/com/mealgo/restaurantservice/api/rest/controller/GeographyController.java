package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.geography.GeographyResult;
import com.mealgo.restaurantservice.application.port.in.geography.ListGeographyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/geography")
public class GeographyController {
    private final ListGeographyUseCase geography;

    @GetMapping("/provinces")
    @Operation(summary = "List provinces and cities")
    public List<GeographyResult> listProvinces() {
        return geography.listProvinces();
    }

    @GetMapping("/provinces/{provinceCode}/wards")
    @Operation(summary = "List wards belonging to a province or city")
    public List<GeographyResult> listWards(@PathVariable Integer provinceCode) {
        return geography.listWards(provinceCode);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidCode(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail provinceNotFound(NoSuchElementException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
