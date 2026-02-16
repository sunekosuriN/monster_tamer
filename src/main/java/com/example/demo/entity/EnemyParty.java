package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name="enemy_parties")
@Data
public class EnemyParty {
	//|id |floor|monster_left_id
	//|monster_center_id|monster_right_id|weight|
	@Id
	private int id;
	
	private int floor;
	
	// ★修正：int から Integer に変更（NULLを許容するため）
    @Column(name = "monster_left_id")
    private Integer monsterLeftId;

    @Column(name = "monster_center_id")
    private Integer monsterCenterId;

    @Column(name = "monster_right_id")
    private Integer monsterRightId;
	
	private int weight;
	
}
