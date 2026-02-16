package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "enemy_action_patterns")
@Data
public class EnemyActionPattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "monster_id")
    private Integer monsterId;

    private Integer priority;

    @Column(name = "condition_type")
    private String conditionType; // TURN_EQ, HP_UNDER など

    @Column(name = "condition_value")
    private Integer conditionValue;

    @Column(name = "activation_rate")
    private Integer activationRate;

    @Column(name = "action_skill_id")
    private Integer actionSkillId;
    
    // 今回追加したカラム
    @Column(name = "target_policy")
    private String targetPolicy; // RANDOM, LOW_HP, HIGH_HP, PLAYER など
}