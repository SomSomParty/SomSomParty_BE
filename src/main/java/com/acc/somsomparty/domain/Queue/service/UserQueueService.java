package com.acc.somsomparty.domain.Queue.service;

import com.acc.somsomparty.domain.Queue.config.SqsSender;
import com.acc.somsomparty.global.exception.CustomException;
import com.acc.somsomparty.global.exception.error.ErrorCode;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final SqsSender sqsSender;
    private static final ConcurrentHashMap<String, AtomicInteger> invocationCounts = new ConcurrentHashMap<>();
    // 사용자 대기 queue의 key
    private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
    // 사용자 대기 queue를 scan 하기 위한 key
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";
    // 사용자 대기 완료 queue의 key
    private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

    // 유저 대기열 등록
    // redis의 sorted set을 대기열로 사용 ( key : user PK , value : unix timestamp)
    // 현재 본인의 순위를 return 함
    public Mono<Long> registerWaitQueue(final String queue, final String email) {
        var unixTimestamp = Instant.now().getEpochSecond(); // 현재 시간
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), email, unixTimestamp)
                .filter(i -> i) // add를 성공하면 true, 실패하면 false를 return
                .switchIfEmpty(Mono.error(new CustomException(ErrorCode.QUEUE_ALREADY_REGISTERED_USER))) // false -> 유저가 이미 queue에 등록 된 경우
                .flatMap(i -> { // true ->
                    // SQS에
                    String messageContent = email + "가 " + queue + " 대기열로 입장함";
                    // 동기적으로 메시지를 전송하고 결과를 받음
                    SendResult<String> result = sqsSender.send(messageContent);
                    log.info("메시지 전송 결과: {}", result);

                    // 대기열에서 유저의 rank 조회
                    return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), email);
                })
                .map(i -> i >= 0 ? i + 1 : i); // rank에 1 더해서 리턴(rank는 0부터 시작)
    }

    // 대기열에서 대기 중인 사용자를 꺼낸 후 , 대기 완료 queue에 insert
    // 두 번째 매개변수의 count 수 만큼의 유저의 수를 대기열에 먼저 들어온 순으로 pop 한다음 proccedQueue에 insert
    // insert된 유저 count를 return
    // 동기적 처리
    public Mono<Long> allowUser(final String queue, final Long count) {
        return Mono.fromCallable(() -> {
            // 호출 횟수 추적
            String key = "allowUser-" + queue;
            int currentInvocationCount = invocationCounts
                    .computeIfAbsent(key, k -> new AtomicInteger(0))
                    .incrementAndGet();

            log.info("{}번째 allowUser 호출: queue={}, count={}", currentInvocationCount, queue, count);

            // 대기열에서 유저를 꺼낸다
            Set<String> members = redisTemplate.opsForZSet().range(USER_QUEUE_WAIT_KEY.formatted(queue), 0, count - 1);  // count만큼 유저를 꺼내기

            if (members == null || members.isEmpty()) {
                return 0L;  // 대기열에 꺼낼 유저가 없다면 0 반환
            } else {
                redisTemplate.opsForZSet().remove(USER_QUEUE_WAIT_KEY.formatted(queue), members.toArray());
            }

            members.forEach(member -> {
                log.info("{}번째 allowUser 처리 중: member={}", currentInvocationCount, member);
                // 각 멤버를 대기 완료 큐에 추가
                redisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue), member, Instant.now().getEpochSecond());
            });
            // 처리된 유저 수를 반환
            return (long) members.size();
        });
    }

    // 분산락 적용
    public Mono<Long> allowUserWithLock(final String queue, final Long count) {
        String lockKey = "LOCK:" + queue;
        RLock rLock = redissonClient.getLock(lockKey);

        return Mono.fromCallable(() -> {
                    // 락 획득 대기 시간 및 유지 시간 설정
                    boolean isLocked = rLock.tryLock(5, 3, TimeUnit.SECONDS);
                    if (!isLocked) {
                        throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
                    }
                    log.info("락 시작");
                    return isLocked;
                })
                .flatMap(ignored -> {
                    // 락을 획득한 후 allowUser() 비즈니스 로직 실행
                    return allowUser(queue, count)
                            .doOnSuccess(result -> log.info("비즈니스 로직 완료: {} members allowed", result))
                            .doOnError(error -> log.error("비즈니스 로직 중 오류 발생", error));
                })
                .doFinally(signalType -> {
                    // 비즈니스 로직 완료 후 락 해제
                    if (rLock.isHeldByCurrentThread()) {
                        log.info("락 해제");
                        rLock.unlock();
                    }
                });
    }

    // 유저의 진입이 허용되었는지 check
    // id(PK)가 있는지 proceed queue에서 찾음
    // rank()가 0 이상의 값을 반환하면, rank >= 0이 true가 되어 입장이 허용된 상태로 판단,
    // rank()가 null을 반환하면, defaultIfEmpty(-1L)에 의해 -1L이 반환되고, rank >= 0이 false가 되어 입장이 허용되지 않은 상태로 판단
    public Mono<Boolean> isAllowed(final String queue, final String email) {
        log.info("유저의 진입이 허용되었는지 체크");
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue), email)
                .defaultIfEmpty(-1L) // 아직 대기 완료 대기열에 없다면 -1를 return
                .map(rank -> rank >= 0); // 있다면 rank를 return
    }

    // 대기열에서 몇 번째 순위인지를 알려주는 함수
    public Mono<Long> getRank(final String queue, final String email) {
        log.info("email = {}", email);
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), email)
                .defaultIfEmpty(-1L) // 대기열에 없다면 -1을 return
                .map(rank -> rank >= 0 ? rank + 1 : rank)
                .onErrorReturn(-1L); // 오류 발생 시 -1 반환
    }

    // 유저를 입장 허용 큐로 이동시킴
    // 대기열queue에서 입장허용 queue로 유저를 옮기는 함수 ( 3명씩 )
    // sqs에서 메세지가 오면 트리거되서 작동됨
    @SqsListener(value = "spring-sqs")
    public void moveUsersToAllowedQueue(Acknowledgement acknowledgement) {
        // 허용할 유저 수 (3명)
        var maxAllowUserCount = 3L;

        // 대기열에서 유저를 스캔하여 입장 허용 큐로 이동
        reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                        .match(USER_QUEUE_WAIT_KEY_FOR_SCAN) // 모든 대기열 확인
                        .count(100) // key를 100개 뽑음
                        .build())
                .map(key -> key.split(":")[2]) // 축제+id 형태의 redis key 2번째 값 추출 ("users:queue:festival15:wait" 에서 'festival15' 추출)
                .flatMap(queue -> allowUserWithLock(queue, maxAllowUserCount).map(allowed -> Tuples.of(queue, allowed))
                        .doOnNext(tuple -> log.info("Tried %d and allowed %d members of %s queue".formatted(maxAllowUserCount, tuple.getT2(), tuple.getT1()))))
                .doFinally(signalType -> {
                    // 처리 완료 후 ACK 전송
                    if (signalType != SignalType.ON_ERROR) {
                        log.info("완료");
                        acknowledgement.acknowledge();  // 큐 제거
                    }
                })
                .subscribe();
    }

    // 페이지 이탈시 대기열에서 삭제
    public Mono<Void> removeUserFromWaitQueue(final String queue, final String email) {
        log.info("유저 삭제 시도: queue = {}, email = {}", queue, email);
        return reactiveRedisTemplate.opsForZSet().remove(USER_QUEUE_WAIT_KEY.formatted(queue), email)
                .doOnSuccess(count -> log.info("삭제된 유저 수: {}", count))
                .then(); // Mono<Void> 반환
    }

    // 타겟 페이지로 이동 시 대기 완료 열에서 삭제
    public Mono<Void> removeUserFromWaitProceedQueue(final String queue, final String email) {
        log.info("유저 삭제 시도: queue = {}, email = {}", queue, email);
        return reactiveRedisTemplate.opsForZSet().remove(USER_QUEUE_PROCEED_KEY.formatted(queue), email)
                .doOnSuccess(count -> log.info("삭제된 유저 수: {}", count))
                .then(); // Mono<Void> 반환
    }
}