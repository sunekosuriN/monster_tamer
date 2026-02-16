package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.EnemyParty;

@Repository
public interface EnemyPartyRepository extends JpaRepository<EnemyParty, Integer> {
    // 指定した階層のパーティリストを取得する
    List<EnemyParty> findByFloor(int floor);
}