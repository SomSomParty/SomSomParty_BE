package com.acc.somsomparty.domain.Festival.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class FestivalResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FestivalPreViewDTO {
        Long id;
        String name;
        String description;
        LocalDate startDate;
        LocalDate endDate;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FestivalPreViewListDTO {
        List<FestivalResponseDTO.FestivalPreViewDTO> festivals;
        boolean hasNext;
        Long lastId;
    }
}
