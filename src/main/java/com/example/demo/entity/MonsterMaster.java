package com.example.demo.entity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "monsters_master")
@Data
public class MonsterMaster {
    
    public static final int SKILL_ID_ATTACK = 901;
    public static final int SKILL_ID_DEFENSE = 902;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String name;

    // ★SQLダンプに合わせ、(name = "hp") 等に変更
    @Column(name = "hp")
    private Integer hp;

    @Column(name = "armor")
    private Integer armor;

    @Column(name = "attack")
    private Integer attack;

    @Column(name = "speed")
    private Integer speed;
    
    @Column(name = "skill_1")
    private Integer skill1;
    
    @Column(name = "skill_2")
    private Integer skill2;
    
    @Column(name = "skill_3")
    private Integer skill3;
    
    @Column(name = "image_url")
    private String imageUrl;

    public List<Integer> getSkillIdList() {
        Set<Integer> skillSet = new LinkedHashSet<>();
        if (this.skill1 != null && this.skill1 > 0) skillSet.add(this.skill1);
        if (this.skill2 != null && this.skill2 > 0) skillSet.add(this.skill2);
        if (this.skill3 != null && this.skill3 > 0) skillSet.add(this.skill3);
        skillSet.add(SKILL_ID_ATTACK);
        skillSet.add(SKILL_ID_DEFENSE);
        return new ArrayList<>(skillSet);
    }

    // NULL安全のためのゲッター
    public int getHp() { return hp != null ? hp : 0; }
    public int getArmor() { return armor != null ? armor : 0; }
    public int getAttack() { return attack != null ? attack : 0; }
    public int getSpeed() { return speed != null ? speed : 0; }
}