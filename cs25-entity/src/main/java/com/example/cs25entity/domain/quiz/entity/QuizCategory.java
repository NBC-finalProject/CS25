package com.example.cs25entity.domain.quiz.entity;

import com.example.cs25common.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class QuizCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryType;

    //대분류면 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QuizCategory parent;

    //소분류
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<QuizCategory> children = new ArrayList<>();

    @Builder
    public QuizCategory(String categoryType, QuizCategory parent) {
        this.categoryType = categoryType;
        this.parent = parent;
    }

    public boolean isParentCategory(){
        return parent == null;
    }
}
