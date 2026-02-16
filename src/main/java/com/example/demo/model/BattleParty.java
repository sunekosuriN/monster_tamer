package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BattleParty {

	private List<BattleMonster> party = new ArrayList<>();

    // パーティにモンスターを追加
    public void addParty(BattleMonster monster) {
        this.party.add(monster);
    }

    // 全員死亡しているか
    public boolean isAllDead() {
    	// 修正ポイント：リストが空（敵が生成されていない）なら全滅とはみなさない
        if (party == null || party.isEmpty()) {
            return false;
        }
        // 全員が死亡(isAlive == false)しているかチェック
        return party.stream().allMatch(m -> !m.isAlive());
    }

    // 生きているメンバーのみ取得
    public List<BattleMonster> getLivingMembers() {
        return party.stream()
                      .filter(BattleMonster::isAlive)
                      .toList();
    }

    // 特定のIDのモンスターを探す（ターゲット選択時などに便利）
    public Optional<BattleMonster> getMonsterById(int id) {
        return party.stream()
                      .filter(m -> m.getId() == id)
                      .findFirst();
    }		
}
