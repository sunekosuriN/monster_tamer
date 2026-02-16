package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.EnemyActionPattern;

@Repository
public interface EnemyActionPatternRepository extends JpaRepository<EnemyActionPattern, Integer> {
    // 優先度の高い順に取得する
    List<EnemyActionPattern> findByMonsterIdOrderByPriorityDesc(Integer monsterId);
}