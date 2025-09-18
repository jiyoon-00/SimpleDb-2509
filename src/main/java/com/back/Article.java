package com.back;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;                           // ← DB 컬럼명과 동일해야 매핑됨
    private String title;
    private String body;
    private LocalDateTime createdDate;         // ← 테스트가 LocalDateTime 타입을 기대
    private LocalDateTime modifiedDate;
    private boolean blind;
}
