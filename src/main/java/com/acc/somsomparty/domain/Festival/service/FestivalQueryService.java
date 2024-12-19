package com.acc.somsomparty.domain.Festival.service;


import com.acc.somsomparty.domain.Festival.dto.FestivalResponseDTO;
import com.acc.somsomparty.domain.Festival.entity.Festival;

public interface FestivalQueryService {
    FestivalResponseDTO.FestivalPreViewListDTO getFestivalList(Long lastId, int offset);
    Festival getFestival(Long festivalId);

    FestivalResponseDTO.FestivalPreViewListDTO searchFestival(Long lastId, int limit, String keyword);
}
