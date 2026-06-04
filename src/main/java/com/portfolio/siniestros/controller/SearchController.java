package com.portfolio.siniestros.controller;

import com.portfolio.siniestros.dto.response.SearchResponse;
import com.portfolio.siniestros.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','PERITO')")
    public ResponseEntity<SearchResponse> buscar(@RequestParam String q) {
        return ResponseEntity.ok(searchService.buscar(q));
    }
}
