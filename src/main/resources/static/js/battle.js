/**
 * 魔物使いのダンジョン - 戦闘ロジック (battle.js)
 */

// グローバル変数
let currentContext = null;      // サーバーから受け取った戦闘状況
let selectedAllyIndex = null;   // 現在選択している味方の番号
let selectedSkillId = null;     // 選択した技のID

// ページ読み込み完了時に実行
document.addEventListener("DOMContentLoaded", () => {
    startBattle();
});

/**
 * 1. 戦闘開始処理
 */
async function startBattle() {
    try {
        const response = await fetch("/battle/start", { method: "POST" });
        if (!response.ok) throw new Error("Network Response Error");
        
        currentContext = await response.json();
        
        // ログエリアを初期化し、戦闘データを描画
        console.log("Battle Started:", currentContext);
        renderBattle();
        
    } catch (e) {
        console.error(e);
        // 項目4: エラー時のメッセージ表示を親切に
        const logArea = document.getElementById("log-message-area");
        logArea.innerHTML = `<div style="color:#ff6b6b">【システムエラー】戦闘データを取得できませんでした。サーバーの起動状態やDBを確認してください。</div>`;
    }
}

/**
 * 2. 画面描画 (Render)
 */
function renderBattle() {
    if (!currentContext) return;

    // 項目3: 背景画像の更新 (2層ごとに切り替え)
    updateBackground();

    // --- 敵エリアの描画 ---
    const enemyArea = document.getElementById("enemy-area");
    enemyArea.innerHTML = "";
    currentContext.enemyParty.party.forEach((unit, index) => {
        enemyArea.appendChild(createUnitBox(unit, index, true));
    });

    // --- 味方エリアの描画 ---
    const allyArea = document.getElementById("ally-area");
    allyArea.innerHTML = "";
    currentContext.playerParty.party.forEach((unit, index) => {
        const allyBox = createUnitBox(unit, index, false);
        
        // 項目1: 味方をクリックして選択状態にする
        if (unit.alive) {
            allyBox.onclick = () => selectAlly(index);
        }
        
        // 選択中の味方を強調
        if (selectedAllyIndex === index) {
            allyBox.classList.add("selected");
        }
        
        allyArea.appendChild(allyBox);
    });

    // --- ログの更新 ---
    const logArea = document.getElementById("log-message-area");
    if (currentContext.logs && currentContext.logs.length > 0) {
        logArea.innerHTML = currentContext.logs.map(log => `<div>${log}</div>`).join("");
        logArea.scrollTop = logArea.scrollHeight;
    }

    // --- コマンドパネルの表示制御 ---
    const actionOverlay = document.getElementById("action-overlay");
    if (currentContext.battleOver) {
        actionOverlay.classList.add("hidden");
        showResultModal();
    } else if (selectedAllyIndex !== null) {
        // 味方を選択している場合のみ、操作パネル（技・ステータス）を表示
        actionOverlay.classList.remove("hidden");
    } else {
        actionOverlay.classList.add("hidden");
    }
}

/**
 * 項目3: 背景画像の更新ロジック
 * 仕様: 1,2層=floor1, 3,4層=floor2, 5,6層=floor3... 
 */
function updateBackground() {
    const screen = document.getElementById("battle-screen");
    const currentFloor = currentContext.currentFloor || 1;
    
    // 2層ごとに1、2、3...と増加する数値を計算
    const bgIndex = Math.ceil(currentFloor / 2);
    
    // 背景画像のパスを設定
    const bgUrl = `/images/floor/floor${bgIndex}.png`;
    
    screen.style.backgroundImage = `url('${bgUrl}')`;
    screen.style.backgroundSize = "cover";
    screen.style.backgroundPosition = "center";
}

/**
 * モンスターの表示用ボックス作成
 */
function createUnitBox(unit, index, isEnemy) {
    const div = document.createElement("div");
    div.className = `unit-box ${isEnemy ? 'enemy' : 'ally'} ${unit.alive ? '' : 'dead'}`;
    
    const hpPercent = (unit.currentHp / unit.maxHp) * 100;

    div.innerHTML = `
        <div class="unit-name">${unit.name}</div>
        <div class="img-container">
            <img src="${unit.imageUrl || '/images/monsters/default.png'}" class="unit-img">
        </div>
        <div class="hp-bar-container">
            <div class="hp-bar-fill" style="width: ${hpPercent}%"></div>
        </div>
        <div class="hp-text">${unit.currentHp} / ${unit.maxHp}</div>
    `;
    return div;
}

/**
 * 味方をクリックした時の選択処理
 */
function selectAlly(index) {
    selectedAllyIndex = index;
    selectedSkillId = null; // 技の選択をリセット
    renderBattle();
}

/**
 * 3. コマンドアクション
 */

// 「技」ボタン
function openSkillWindow() {
    if (selectedAllyIndex === null) return;
    
    const skillList = document.getElementById("skill-list");
    skillList.innerHTML = "";
    
    const actor = currentContext.playerParty.party[selectedAllyIndex];
    
    actor.skills.forEach(skill => {
        const btn = document.createElement("button");
        btn.className = "skill-btn";
        const uses = skill.skillMaster.maxUses === -1 ? "∞" : `${skill.remainingUses}/${skill.skillMaster.maxUses}`;
        
        btn.innerHTML = `
            <div class="s-name">${skill.skillMaster.name}</div>
            <div class="s-uses">回数: ${uses}</div>
        `;

        if (skill.remainingUses !== 0) {
            btn.onclick = () => {
                selectedSkillId = skill.skillMaster.id;
                closeSkillWindow();
                // ログに対象選択を促すメッセージを表示
                const logArea = document.getElementById("log-message-area");
                logArea.innerHTML += `<div style="color:#00ffff">${actor.name}の「${skill.skillMaster.name}」！ 対象の敵を選んでください。</div>`;
                logArea.scrollTop = logArea.scrollHeight;
                
                enableEnemyTargeting();
            };
        } else {
            btn.disabled = true;
            btn.style.opacity = "0.5";
        }
        skillList.appendChild(btn);
    });

    document.getElementById("skill-window").classList.remove("hidden");
}

function closeSkillWindow() {
    document.getElementById("skill-window").classList.add("hidden");
}

/**
 * 技選択後に敵をクリックできるようにする処理
 */
function enableEnemyTargeting() {
    const enemies = document.querySelectorAll(".unit-box.enemy");
    enemies.forEach((el, index) => {
        if (!el.classList.contains("dead")) {
            el.style.cursor = "crosshair";
            el.style.filter = "drop-shadow(0 0 15px #ff4757)";
            
            // 敵をクリックしたら技を実行
            el.onclick = () => executeSkill(selectedSkillId, index);
        }
    });
}

/**
 * 項目2: 「ステータス確認」ボタン
 */
function openStatusWindow() {
    if (selectedAllyIndex === null) return;
    const unit = currentContext.playerParty.party[selectedAllyIndex];
    
    // ステータス情報をアラートで表示（または専用モーダル）
    const info = `
【${unit.name}の能力】
HP: ${unit.currentHp} / ${unit.maxHp}
攻撃力: ${unit.currentAttack}
素早さ: ${unit.currentSpeed}
アーマー: ${unit.currentArmor}
    `;
    alert(info);
}

/**
 * 4. サーバーへ行動データを送信
 */
async function executeSkill(skillId, targetIndex) {
    try {
        // 二重送信を防ぐために操作を隠す
        document.getElementById("action-overlay").classList.add("hidden");

        const response = await fetch(`/battle/action?skillId=${skillId}&targetIndex=${targetIndex || 0}`, { 
            method: "POST" 
        });
        currentContext = await response.json();
        
        // ターンの終了後に選択状態をリセット
        selectedAllyIndex = null;
        selectedSkillId = null;
        
        renderBattle();
    } catch (e) {
        console.error("Action Error:", e);
    }
}

/**
 * 5. 戦闘結果の表示
 */
function showResultModal() {
    const modal = document.getElementById("result-modal");
    modal.classList.remove("hidden");

    const title = document.getElementById("result-title");
    const body = document.getElementById("result-body");

    if (currentContext.victory) {
        title.innerText = "VICTORY!!";
        title.style.color = "#f1c40f";
        body.innerText = "敵を全て倒した！探索を続けますか？";
        
        if (currentContext.tamed) {
            document.getElementById("recruit-area").classList.remove("hidden");
            document.getElementById("next-floor-btn").classList.add("hidden");
        } else {
            document.getElementById("recruit-area").classList.add("hidden");
            document.getElementById("next-floor-btn").classList.remove("hidden");
        }
    } else {
        title.innerText = "DEFEAT...";
        title.style.color = "#ff4757";
        body.innerText = "味方が全滅してしまった...";
        document.getElementById("title-btn").classList.remove("hidden");
    }
}

// 勧誘処理
async function recruitEnemy() {
    try {
        const response = await fetch("/battle/recruit", { method: "POST" });
        currentContext = await response.json();
        document.getElementById("recruit-area").classList.add("hidden");
        document.getElementById("next-floor-btn").classList.remove("hidden");
        renderBattle();
    } catch (e) { console.error(e); }
}

// 終了して次へ
async function finishBattle() {
    try {
        const response = await fetch("/battle/finish", { method: "POST" });
        const success = await response.json();
        if (success) location.href = "/rest";
    } catch (e) { console.error(e); }
}