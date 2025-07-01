package com.example.cs25service.domain.profile.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ProfileWrongQuizResponseDto {

    private final String userId;

    private final List<WrongQuizDto> wrongQuizList;

    private final int totalCount;
    private final int totalPages;
    private final int currentPage;
    private final int size;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final boolean isLast;

    public ProfileWrongQuizResponseDto(String userId, List<WrongQuizDto> wrongQuizList, Page page) {
        this.userId = userId;
        this.wrongQuizList = wrongQuizList;

        this.totalCount = (int)page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.size = page.getSize();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.isLast = page.isLast();
    }
}
