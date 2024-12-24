package com.acc.somsomparty.domain.Festival.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class FestivalRequestDTO {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
