package com.acc.somsomparty.domain.Festival.converter;

import com.acc.somsomparty.domain.Festival.dto.FestivalResponseDTO;
import com.acc.somsomparty.domain.Festival.entity.Festival;

import java.util.List;

public class FestivalConverter {
    public static FestivalResponseDTO.FestivalPreViewDTO festivalPreViewDTO(Festival festival) {
        return FestivalResponseDTO.FestivalPreViewDTO.builder()
                .id(festival.getId())
                .name(festival.getName())
                .description(festival.getDescription())
                .startDate(festival.getStartDate())
                .endDate(festival.getEndDate())
                .build();
    }

    public static FestivalResponseDTO.FestivalPreViewListDTO festivalPreViewListDTO(List<FestivalResponseDTO.FestivalPreViewDTO> festivals, boolean hasNext, Long lastId) {
        return FestivalResponseDTO.FestivalPreViewListDTO.builder()
                .festivals(festivals)
                .hasNext(hasNext)
                .lastId(lastId)
                .build();
    }
}
