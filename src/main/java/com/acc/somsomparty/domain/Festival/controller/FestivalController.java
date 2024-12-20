package com.acc.somsomparty.domain.Festival.controller;

import com.acc.somsomparty.domain.Festival.converter.FestivalConverter;
import com.acc.somsomparty.domain.Festival.dto.FestivalResponseDTO;
import com.acc.somsomparty.domain.Festival.entity.Festival;

import com.acc.somsomparty.domain.Festival.service.FestivalQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/festivals")
@RequiredArgsConstructor
public class FestivalController {
    private final FestivalQueryService festivalQueryService;

    @GetMapping("")
    public ResponseEntity<FestivalResponseDTO.FestivalPreViewListDTO> getFestivalList(@RequestParam(defaultValue = "0") Long lastId, @RequestParam(defaultValue = "10") int limit) {
        FestivalResponseDTO.FestivalPreViewListDTO festivalPage = festivalQueryService.getFestivalList(lastId, limit);
        return new ResponseEntity<>(festivalPage, HttpStatus.OK);
    }

    @GetMapping("/{festivalId}")
    public ResponseEntity<FestivalResponseDTO.FestivalPreViewDTO> getFestival(@PathVariable(name = "festivalId") Long festivalId) {
        Festival festival = festivalQueryService.getFestival(festivalId);
        return new ResponseEntity<>(FestivalConverter.festivalPreViewDTO(festival), HttpStatus.OK);
    }
}
