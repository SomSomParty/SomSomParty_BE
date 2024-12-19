package com.acc.somsomparty.domain.Festival.service;


import com.acc.somsomparty.domain.Festival.dto.FestivalResponseDTO;
import com.acc.somsomparty.domain.Festival.entity.Festival;

import java.time.LocalDate;
import java.util.List;

public interface FestivalQueryService {
    FestivalResponseDTO.FestivalPreViewListDTO getFestivalList(Long lastId, int offset);
    Festival getFestival(Long festivalId);
    List<Festival> getFestivalListByStartTime(LocalDate date);
}
