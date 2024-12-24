package com.acc.somsomparty.domain.Festival.service;

import com.acc.somsomparty.domain.Festival.converter.FestivalConverter;
import com.acc.somsomparty.domain.Festival.dto.FestivalResponseDTO;
import com.acc.somsomparty.domain.Festival.entity.Festival;
import com.acc.somsomparty.domain.Festival.repository.FestivalRepository;
import com.acc.somsomparty.global.exception.CustomException;
import com.acc.somsomparty.global.exception.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalQueryServiceImpl implements FestivalQueryService {
    private final FestivalRepository festivalRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public FestivalResponseDTO.FestivalPreViewListDTO getFestivalList(Long lastId, int limit) {
        List<Festival> result;
        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        if (lastId.equals(0L)) {
            result = festivalRepository.findAllByOrderByCreatedAtDesc(pageRequest).getContent();
        }
        else {
            Festival festival = festivalRepository.findById(lastId).orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
            result = festivalRepository.findByCreatedAtLessThanOrderByCreatedAtDesc(festival.getCreatedAt(), pageRequest).getContent();
        }
        return generateFestivalPreviewListDTO(result, limit);
    }

    @Override
    public Festival getFestival(Long festivalId) {
        return festivalRepository.findById(festivalId).orElseThrow(() -> new CustomException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    @Override
    public FestivalResponseDTO.FestivalPreViewListDTO searchFestival(Long lastId, int limit, String keyword) {
        // Redis 캐시 키 생성
        String cacheKey = "festival::search::" + keyword.toLowerCase() + "::" + lastId + "::" + limit;

        Object cachedResult = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResult != null) {
            // 캐시된 결과 반환
            return objectMapper.convertValue(cachedResult, FestivalResponseDTO.FestivalPreViewListDTO.class);
        }

        // 캐시에 없으면 DB에서 검색
        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Festival> festivals = festivalRepository.searchByKeyword(lastId, keyword.toLowerCase(), pageRequest).getContent();
        FestivalResponseDTO.FestivalPreViewListDTO responseDTO = generateFestivalPreviewListDTO(festivals, limit);

        // Redis에 결과 캐싱 (TTL 10분 설정)
        redisTemplate.opsForValue().set(cacheKey, responseDTO, 5, TimeUnit.MINUTES);

        return responseDTO;
    }

    private FestivalResponseDTO.FestivalPreViewListDTO generateFestivalPreviewListDTO(List<Festival> festivals, int limit) {
        boolean hasNext = festivals.size() > limit;
        Long lastId = null;
        if (hasNext) {
            festivals = festivals.subList(0, festivals.size() - 1); // 마지막 항목 제외
            lastId = festivals.get(festivals.size() - 1).getId(); // 마지막 항목의 ID를 커서로 설정
        }
        List<FestivalResponseDTO.FestivalPreViewDTO> list = festivals
                .stream()
                .map(FestivalConverter::festivalPreViewDTO)
                .collect(Collectors.toList());

        return FestivalConverter.festivalPreViewListDTO(list, hasNext, lastId);
    }
}
