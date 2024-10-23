# Tasklet

**Tasklet**은 Spring Batch의 Step 구성 요소중 하나이며 단일 작업이나 일련의 작업을 한곳에서 수행할 때 사용함. `단순한` 작업을 처리하는 데 적합하다.

## Tasklet의 구조와 작동 방식

Tasklet은 org.springframework.batch.core.step.tasklet.Tasklet 인터페이스를 구현하여 정의됨. Tasklet 인터페이스는 단 하나의
메서드 execute를 제공. 이 메서드는 Step의 실행 로직을 트리거 됨, RepeatStatus를 반환하여 해당 Task를 종료 여부를 결정함.

```java
public interface Tasklet {

  RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception;
}
```

- **StepContribution**: 현재 Step의 상태와 실행 정보를 담고 있다.
- **ChunkContext**: Step 실행에 관련된 컨텍스트 정보를 제공.
- **RepeatStatus**: Step이 추가로 실행이 필요한지 여부를 나타낸다.
    - `FINISHED` 또는 `CONTINUABLE` 값을 가질 수 있음.

## Tasklet 예제 샘플
[Tasklet Example Code](src/main/java/io/bk/spring/batch/tasklet/SpringBatchApplication.java)
1. 간단한 json 파일 정보를 읽는다.
2. 해당 데이터를 각각의 `DB1`, `DB2`에 저장한다.
1. 저장되는 각 `DB스킴`의 구조는 다르다.

### 체크 포인트
1. 전체 사이클 `예외 처리` 핸들링
    1. json 파일 validation
    2. DB 저장 시 무결성
    3. 컨텐츠 validation
2. 에러 발생 시 재 처리 로직
    1. 특정 라인부터 재시작
    2. 롤백 후 재시작
    3. 에러 데이터를 따로 적재 후 컨티뉴 처리
4. 모니터링 작업
    5. 시작 완료에 대한 로그
    6. 에러 발생에 대한 상태 로그
4. 테스트 코드 작성


