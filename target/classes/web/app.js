const state = {
  data: null,
  selectedRoomId: null,
  selectedItemId: null
};

const qs = id => document.getElementById(id);

const playerHp = qs("playerHp");
const playerAtk = qs("playerAtk");
const inventoryCount = qs("inventoryCount");
const roomName = qs("roomName");
const roomMeta = qs("roomMeta");
const mapMeta = qs("mapMeta");
const monsterMeta = qs("monsterMeta");
const statusBanner = qs("statusBanner");
const encounterPanel = qs("encounterPanel");
const monsterOutput = qs("monsterOutput");
const monsterLookupOutput = qs("monsterLookupOutput");
const inventoryList = qs("inventoryList");
const roomList = qs("roomList");
const roomDetails = qs("roomDetails");
const pathOutput = qs("pathOutput");
const activityLog = qs("activityLog");
const catalogSelect = qs("catalogSelect");
const targetRoomSelect = qs("targetRoomSelect");
const toast = qs("toast");

async function fetchJson(url, options) {
  const response = await fetch(url, options);
  return response.json();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;");
}

function selectedRoom() {
  if (!state.data) return null;
  return state.data.rooms.find(room => room.id === state.selectedRoomId) || state.data.currentRoom;
}

function selectedItem() {
  if (!state.data) return null;
  return state.data.inventory.find(item => item.id === state.selectedItemId) || null;
}

function fillItemForm(item) {
  qs("itemIdField").value = item?.id ?? "";
  qs("itemNameField").value = item?.name ?? "";
  qs("itemTypeField").value = item?.type ?? "MISC";
  qs("itemValueField").value = item?.value ?? "";
}

function renderOverview(data) {
  qs("metricHero").textContent = data.player.name;
  qs("metricScore").textContent = data.game.score;
  qs("metricStatus").textContent = data.game.escaped
    ? "Escaped"
    : data.player.alive ? "Exploring" : "Defeated";

  playerHp.textContent = data.player.hp;
  playerAtk.textContent = `ATK ${data.player.atk}`;
  inventoryCount.textContent = `${data.player.inventorySize} items`;
  roomName.textContent = `Room ${data.currentRoom.id} - ${data.currentRoom.name}`;
  roomMeta.textContent = data.currentRoom.exit
    ? "Exit room"
    : data.currentRoom.treasure ? "Treasure room" : "Standard room";
  mapMeta.textContent = `${data.game.roomCount} rooms`;
  monsterMeta.textContent = `${data.game.monsterCount} tracked monsters`;

  const adjacent = data.currentRoom.neighbors.map(n => `Room ${n.toRoomId}`).join(", ") || "No neighbors";
  statusBanner.innerHTML = `
    <strong>${escapeHtml(data.player.name)}</strong> is standing in
    <strong>${escapeHtml(data.currentRoom.name)}</strong>.
    Adjacent paths: ${escapeHtml(adjacent)}.
  `;
}

function renderEncounter(data) {
  const encounter = data.encounter;
  if (!encounter) {
    encounterPanel.innerHTML = `
      <strong>Calm room.</strong>
      <p>No active monster is blocking this chamber. This is a good time to claim treasure, heal, or plan the next route.</p>
      <div class="badge-row">
        <span class="micro-badge success">No encounter</span>
        <span class="micro-badge">${data.currentRoom.treasure ? "Treasure available" : "Treasure cleared"}</span>
      </div>
    `;
    monsterOutput.textContent = "No active monster in this room.";
    return;
  }

  encounterPanel.innerHTML = `
    <strong>${escapeHtml(encounter.name)}</strong> is guarding this room.
    <p>Combat is turn-based in a lightweight flow: attack once, receive counter damage if the monster survives, then use potion or continue.</p>
    <div class="badge-row">
      <span class="micro-badge danger">HP ${encounter.hp}</span>
      <span class="micro-badge">ATK ${encounter.atk}</span>
      <span class="micro-badge">Level ${encounter.level}</span>
      <span class="micro-badge">Drop item ${encounter.dropItemId}</span>
    </div>
  `;

  monsterOutput.textContent =
    `Encounter Report\n\n` +
    `Monster ID: ${encounter.id}\n` +
    `Name: ${encounter.name}\n` +
    `Level: ${encounter.level}\n` +
    `HP: ${encounter.hp}\n` +
    `ATK: ${encounter.atk}\n` +
    `Drop Item ID: ${encounter.dropItemId}`;
}

function renderInventory(data) {
  catalogSelect.innerHTML = data.catalog.map(item =>
    `<option value="${item.id}">${item.id} - ${escapeHtml(item.name)} [${escapeHtml(item.type)}]</option>`
  ).join("");

  inventoryList.innerHTML = data.inventory.length
    ? data.inventory.map(item => `
      <article class="list-item inventory ${item.id === state.selectedItemId ? "active" : ""}" data-item-id="${item.id}">
        <strong>ID ${item.id} - ${escapeHtml(item.name)}</strong>
        <span class="item-meta">${escapeHtml(item.type)} | value ${item.value}</span>
      </article>
    `).join("")
    : `<article class="list-item">Inventory empty</article>`;

  if (!selectedItem()) {
    state.selectedItemId = data.inventory[0]?.id ?? null;
  }
  fillItemForm(selectedItem());
}

function renderRooms(data) {
  targetRoomSelect.innerHTML = data.rooms.map(room =>
    `<option value="${room.id}" ${room.id === data.currentRoom.id ? "selected" : ""}>Room ${room.id} - ${escapeHtml(room.name)}</option>`
  ).join("");

  roomList.innerHTML = data.rooms.map(room => {
    const tags = [];
    if (room.id === data.currentRoom.id) tags.push("Current");
    if (room.exit) tags.push("Exit");
    if (room.treasure) tags.push("Treasure");
    if (room.monsterId > 0) tags.push(`Monster ${room.monsterId}`);

    return `
      <article class="list-item room ${room.id === state.selectedRoomId ? "active" : ""}" data-room-id="${room.id}">
        <strong>Room ${room.id} - ${escapeHtml(room.name)}</strong>
        <span class="room-meta">${escapeHtml(tags.join(" | ") || "Quiet room")}</span>
      </article>
    `;
  }).join("");

  renderSelectedRoom();
}

function renderSelectedRoom() {
  const room = selectedRoom();
  if (!room) return;

  roomDetails.textContent =
    `Room ${room.id} - ${room.name}\n\n` +
    `${room.description}\n\n` +
    `Exit: ${room.exit}\n` +
    `Treasure: ${room.treasure}\n` +
    `Monster ID: ${room.monsterId}\n` +
    `Neighbors: ` +
    (room.neighbors.length
      ? room.neighbors.map(n => `${n.toRoomId} (${n.name}) w=${n.weight}`).join(", ")
      : "None");
}

function renderLog(data) {
  activityLog.innerHTML = data.activityLog.length
    ? data.activityLog.map(entry => `<article class="list-item">${escapeHtml(entry)}</article>`).join("")
    : `<article class="list-item">No actions yet.</article>`;
}

function renderState(data) {
  state.data = data;
  if (state.selectedRoomId == null) {
    state.selectedRoomId = data.currentRoom?.id ?? null;
  }
  renderOverview(data);
  renderEncounter(data);
  renderInventory(data);
  renderRooms(data);
  renderLog(data);
}

function renderLookup(payload) {
  if (!payload.found) {
    monsterLookupOutput.textContent = payload.message;
    return;
  }
  const m = payload.monster;
  monsterLookupOutput.textContent =
    `BST Lookup Result\n\n` +
    `Monster ID: ${m.id}\n` +
    `Name: ${m.name}\n` +
    `Level: ${m.level}\n` +
    `HP: ${m.hp}\n` +
    `ATK: ${m.atk}\n` +
    `Drop Item ID: ${m.dropItemId}`;
}

function renderPath(payload) {
  if (payload.pathLabel !== undefined) {
    pathOutput.textContent = payload.reachable
      ? `${payload.title}\n\nPath: ${payload.pathLabel}\nCost: ${payload.totalCost}\n\nRooms:\n` +
        payload.rooms.map(room => `- ${room.id}: ${room.name}`).join("\n")
      : `${payload.title}\n\nNo path found.`;
    return;
  }

  pathOutput.textContent =
    `${payload.title}\n\n` +
    (payload.rooms.length
      ? payload.rooms.map((room, index) => `${index + 1}. Room ${room.id} - ${room.name}`).join("\n")
      : "No rooms visited.");
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.remove("hidden");
  window.clearTimeout(showToast._timer);
  showToast._timer = window.setTimeout(() => toast.classList.add("hidden"), 2400);
}

async function refreshState() {
  renderState(await fetchJson("/api/state"));
}

async function handleMutation(url) {
  const payload = await fetchJson(url, { method: "POST" });
  renderState(payload.state);
  showToast(payload.message);
}

document.addEventListener("click", async event => {
  const action = event.target.dataset.action;
  if (!action) return;

  if (action === "findInventory") {
    const name = qs("inventorySearch").value.trim();
    const payload = await fetchJson(`/api/inventory/find?name=${encodeURIComponent(name)}`);
    if (payload.found) {
      state.selectedItemId = payload.item.id;
      fillItemForm(payload.item);
    }
    showToast(payload.found ? `Found ${payload.item.name}` : payload.message);
  }

  if (action === "addCatalogItem") {
    await handleMutation(`/api/inventory/add?itemId=${catalogSelect.value}`);
  }

  if (action === "createItem") {
    await handleMutation(
      `/api/inventory/create?name=${encodeURIComponent(qs("itemNameField").value.trim())}` +
      `&type=${encodeURIComponent(qs("itemTypeField").value)}` +
      `&value=${encodeURIComponent(qs("itemValueField").value || 0)}`
    );
  }

  if (action === "updateItem") {
    await handleMutation(
      `/api/inventory/update?itemId=${encodeURIComponent(qs("itemIdField").value)}` +
      `&name=${encodeURIComponent(qs("itemNameField").value.trim())}` +
      `&type=${encodeURIComponent(qs("itemTypeField").value)}` +
      `&value=${encodeURIComponent(qs("itemValueField").value || 0)}`
    );
  }

  if (action === "deleteItem") {
    await handleMutation(`/api/inventory/remove?itemId=${encodeURIComponent(qs("itemIdField").value)}`);
  }

  if (action === "usePotion") {
    await handleMutation("/api/inventory/use-potion");
  }

  if (action === "findMonster") {
    renderLookup(await fetchJson(`/api/monster?id=${encodeURIComponent(qs("monsterId").value.trim())}`));
  }

  if (action === "inspectMonster") {
    renderLookup(await fetchJson("/api/monster/current"));
  }

  if (action === "attackMonster") {
    await handleMutation("/api/game/attack");
  }

  if (action === "claimTreasure") {
    await handleMutation("/api/game/claim-treasure");
  }

  if (action === "pathExit") {
    renderPath(await fetchJson("/api/path/exit"));
  }

  if (action === "pathTreasure") {
    renderPath(await fetchJson("/api/path/treasure"));
  }

  if (action === "pathDijkstra") {
    renderPath(await fetchJson(`/api/path/dijkstra?targetRoomId=${encodeURIComponent(targetRoomSelect.value)}`));
  }

  if (action === "pathDfs") {
    renderPath(await fetchJson("/api/path/dfs"));
  }

  if (action === "moveHero") {
    await handleMutation(`/api/player/move?roomId=${encodeURIComponent(targetRoomSelect.value)}`);
  }
});

inventoryList.addEventListener("click", event => {
  const itemNode = event.target.closest("[data-item-id]");
  if (!itemNode || !state.data) return;
  state.selectedItemId = Number(itemNode.dataset.itemId);
  fillItemForm(selectedItem());
  renderInventory(state.data);
});

roomList.addEventListener("click", event => {
  const roomNode = event.target.closest("[data-room-id]");
  if (!roomNode || !state.data) return;
  state.selectedRoomId = Number(roomNode.dataset.roomId);
  renderRooms(state.data);
});

refreshState().then(async () => {
  renderLookup(await fetchJson("/api/monster/current"));
  pathOutput.textContent = "Choose a route action to visualize BFS, DFS, or Dijkstra from the current room.";
});
