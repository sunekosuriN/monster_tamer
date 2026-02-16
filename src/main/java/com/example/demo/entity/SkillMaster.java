package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name="skills_master")
@Data
public class SkillMaster {
	//id |name   |effect_type    |damage_value|damage_multiplier
	//|change_atk|change_speed|change_armor|target_type|max_uses
	//|description  
	@Id
	private int id;
	
	private String name;
	
	@Column(name="effect_type")
	private String effectType ;
	
	@Column(name="damage_value")
	private int damageValue;
	
	@Column(name="damage_multiplier")
	private int damageMultiplier;
	
	@Column(name="change_atk")
	private int changeAtk;
	
	@Column(name="change_speed")
	private int changeSpeed;
	
	@Column(name="change_armor")
	private int changeArmor;
	
	@Column(name="target_type")
	private String targetType;
	
	@Column(name="max_uses")
	private int maxUses;
	
	private String description;
}
