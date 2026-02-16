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
 * 1. 戦闘開始処理 (API呼び出し)
 */
async function startBattle() {
    try {
        const response = await fetch("/battle/start", { method: "POST" });
        if (!response.ok) throw new Error("API通信に失敗しました");
        
        currentContext = await response.json();
        
        // デバッグ用: サーバーからのデータをコンソールで確認
        console.log("Battle Data Received:", currentContext);
        
        renderBattle();
    } catch (e) {
        console.error(e);
        // 項目4: メッセージエリアにエラーを表示
        const logArea = document.getElementById("log-message-area");
        logArea.innerHTML = `<div style="color:#ff6b6b">【エラー】戦闘データを取得できませんでした。サーバーのログを確認してください。</div>`;
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
        // 敵は基本クリック不可（技選択後のターゲット指定時のみJSでイベントを付与）
        enemyArea.appendChild(createUnitBox(unit, index, true));
    });

    // --- 味方エリアの描画 ---
    const allyArea = document.getElementById("ally-area");
    allyArea.innerHTML = "";
    currentContext.playerParty.party.forEach((unit, index) => {
        const allyBox = createUnitBox(unit, index, false);
        
        // 項目1: 味方をクリックして行動を選択する
        if (unit.alive) {
            allyBox.onclick = () => selectAlly(index);
        }
        
        // 選択中の味方に強調クラス（selected-unit）を付与
        if (selectedAllyIndex === index) {
            allyBox.classList.add("selected-unit");
        }
        
        allyArea.appendChild(allyBox);
    });

    // --- ログの更新 ---
    const logArea = document.getElementById("log-message-area");
    if (currentContext.logs && currentContext.logs.length > 0) {
        logArea.innerHTML = currentContext.logs.map(log => `<div>${log}</div>`).join("");
        logArea.scrollTop = logArea.scrollHeight;
    }

    // --- コマンドパネル（操作ボタン）の表示制御 ---
    const actionOverlay = document.getElementById("action-overlay");
    if (currentContext.battleOver) {
        actionOverlay.classList.add("hidden");
        showResultModal();
    } else if (selectedAllyIndex !== null) {
        // 味方を選択している時だけ「技」「ステータス確認」を表示
        actionOverlay.classList.remove("hidden");
    } else {
        actionOverlay.classList.add("hidden");
    }
}

/**
 * 項目3: 背景画像の切り替えロジック
 * 仕様: 1,2層=floor1, 3,4層=floor2, 5,6層=floor3...
 */
function updateBackground() {
    const screen = document.getElementById("battle-screen");
    const currentFloor = currentContext.currentFloor || 1;
    
    // 2層ごとにカウントアップ (Math.ceil(1/2)=1, Math.ceil(2/2)=1, Math.ceil(3/2)=2...)
    const bgIndex = Math.ceil(currentFloor / 2);
    
    // 画像パス: static/images/floor/floorX.png
    const bgUrl = `/images/floor/floor${bgIndex}.png`;
    
    screen.style.backgroundImage = `url('${bgUrl}')`;
    screen.style.backgroundSize = "cover";
    screen.style.backgroundPosition = "center";
}

/**
 * モンスター個別の表示パーツ生成
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
 * 項目1: 味方を選択した時の処理
 */
function selectAlly(index) {
    selectedAllyIndex = index;
    selectedSkillId = null; // 他の味方へ切り替えた際は技選択をリセット
    renderBattle();
}

/**
 * 3. コマンドアクション処理
 */

// 「技」ボタン：スキル選択窓を開く
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
                
                // ターゲット選択を促すメッセージ表示
                const logArea = document.getElementById("log-message-area");
                logArea.innerHTML += `<div style="color:#00ffff">${actor.name}の「${skill.skillMaster.name}」発動準備。対象の敵を選んでください。</div>`;
                logArea.scrollTop = logArea.scrollHeight;
                
                // 敵モンスターをクリック可能にする
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
 * 技選択後、敵に攻撃対象カーソルを出す
 */
function enableEnemyTargeting() {
    const enemies = document.querySelectorAll(".unit-box.enemy");
    enemies.forEach((el, index) => {
        if (!el.classList.contains("dead")) {
            el.style.cursor = "crosshair";
            el.style.filter = "drop-shadow(0 0 15px #ff4757)";
            
            // 敵をクリックして技を実行
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
    
    const info = `
【${unit.name}のステータス】
HP: ${unit.currentHp} / ${unit.maxHp}
攻撃力: ${unit.currentAttack}
素早さ: ${unit.currentSpeed}
アーマー: ${unit.currentArmor}
    `;
    alert(info); // 将来的に専用モーダルに置き換え可能
}

/**
 * 4. サーバーへ行動データを送信
 */
async function executeSkill(skillId, targetIndex) {
    try {
        // 二重送信防止
        document.getElementById("action-overlay").classList.add("hidden");

        const response = await fetch(`/battle/action?skillId=${skillId}&targetIndex=${targetIndex}`, { 
            method: "POST" 
        });
        currentContext = await response.json();
        
        // ターン終了後に選択状態をクリア
        selectedAllyIndex = null;
        selectedSkillId = null;
        
        renderBattle();
    } catch (e) {
        console.error("Action Error:", e);
    }
}

/**
 * 5. リザルト処理
 */
function showResultModal() {
    const modal = document.getElementById("result-modal");
    modal.classList.remove("hidden");

    const title = document.getElementById("result-title");
    const body = document.getElementById("result-body");

    if (currentContext.victory) {
        title.innerText = "VICTORY!!";
        title.style.color = "#f1c40f";
        body.innerText = "敵を全滅させた！";
        
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
        body.innerText = "パーティーが力尽きた...";
        document.getElementById("title-btn").classList.remove("hidden");
    }
}

// 仲間にする処理
async function recruitEnemy() {
    try {
        const response = await fetch("/battle/recruit", { method: "POST" });
        currentContext = await response.json();
        document.getElementById("recruit-area").classList.add("hidden");
        document.getElementById("next-floor-btn").classList.remove("hidden");
        renderBattle();
    } catch (e) { console.error(e); }
}

// 戦闘を完全に終了して次のシーン（rest.html等）へ
async function finishBattle() {
    try {
        const response = await fetch("/battle/finish", { method: "POST" });
        const success = await response.json();
        if (success) location.href = "/rest";
    } catch (e) { console.error(e); }
}