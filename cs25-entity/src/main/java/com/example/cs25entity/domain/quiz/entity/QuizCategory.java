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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
public class QuizCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
	private String categoryType;

    //대분류면 null
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private QuizCategory parent;

    //소분류
    @Setter
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizCategory> children = new ArrayList<>();

    @Builder
    public QuizCategory(String categoryType, QuizCategory parent) {
        this.categoryType = categoryType;
        this.parent = parent;
    }

    /**
     * 부모가 존재하면 true, 없으면 false를 반환하는 메서드
     * @return true/false
     */
    public boolean isChildCategory(){
        return parent != null;
    }

}
