// --- DOM elements ---
const uploadBtn     = document.getElementById("upload-btn");
const fileName      = document.getElementById("file-nme")
const findPathBtn   = document.getElementById("find-path-btn");
const fileInput     = document.getElementById("file-input");
const canvas        = document.getElementById("tsp-canvas");
const visualFrame   = document.getElementById("visual-frame");
const processingTxt = document.getElementById("processing-txt")
const skipBtn       = document.getElementById("skip-btn");

let ctx = null;
let locations = [];
let tspPath = null;
let animationId = null;

let isAnimating = false;
let animPoints = null;
let animPath = null;

// ---------- Canvas setup ----------
function resizeCanvasToFrame() {
  const rect = visualFrame.getBoundingClientRect();
  canvas.width  = rect.width;
  canvas.height = rect.height;
  if (!ctx) ctx = canvas.getContext("2d");
  drawScene(locations, tspPath);
}

function clearCanvas() {
  if (!ctx) return;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = "#ffffff";
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.strokeStyle = "#c0c0c0";
  ctx.strokeRect(0, 0, canvas.width, canvas.height);
}

// ---------- CSV parsing ----------
function parseCsv(text) {
  const lines = text
    .split(/\r?\n/)
    .map(line => line.trim())
    .filter(line => line.length > 0);

  const parsed = [];

  for (const line of lines) {
    const parts = line.split(",");
    if (parts.length < 2) continue;

    const y = parseFloat(parts[parts.length - 1]);
    const x = parseFloat(parts[parts.length - 2]);
    if (Number.isNaN(x) || Number.isNaN(y)) continue;

    const id = (parts.length > 2)
      ? parts.slice(0, parts.length - 2).join(",")
      : String(parsed.length);

    parsed.push({ id, x, y });
  }

  return parsed;
}

// ---------- Coordinate transform ----------
function computeTransform(points) {
  let minX = Infinity, maxX = -Infinity;
  let minY = Infinity, maxY = -Infinity;

  for (const p of points) {
    if (p.x < minX) minX = p.x;
    if (p.x > maxX) maxX = p.x;
    if (p.y < minY) minY = p.y;
    if (p.y > maxY) maxY = p.y;
  }

  const rangeX = maxX - minX || 1;
  const rangeY = maxY - minY || 1;

  const padding = 40;
  const drawableWidth  = canvas.width  - 2 * padding;
  const drawableHeight = canvas.height - 2 * padding;

  const scale = Math.min(drawableWidth / rangeX, drawableHeight / rangeY);

  const usedWidth  = rangeX * scale;
  const usedHeight = rangeY * scale;

  const offsetX = padding + (drawableWidth  - usedWidth)  / 2;
  const offsetY = padding + (drawableHeight - usedHeight) / 2;

  return { minX, minY, scale, offsetX, offsetY };
}

// ---------- Draw scene ----------
function drawScene(points, pathIndices, edgesToDraw) {
  if (!ctx) return;
  clearCanvas();
  if (!points || points.length === 0) return;

  const { minX, minY, scale, offsetX, offsetY } = computeTransform(points);

  const screenPoints = points.map(p => ({
    x: offsetX + (p.x - minX) * scale,
    y: offsetY + (p.y - minY) * scale
  }));

  // Draw path + partial path animation support
  if (pathIndices && pathIndices.length > 1) {
    const totalEdgesWithoutClosing = pathIndices.length - 1;
    const limit = typeof edgesToDraw === "number"
      ? Math.min(edgesToDraw, totalEdgesWithoutClosing)
      : totalEdgesWithoutClosing;

    const closeTour = typeof edgesToDraw !== "number";

    ctx.strokeStyle = "#0077cc";
    ctx.lineWidth = 1.5;
    ctx.beginPath();

    const first = screenPoints[pathIndices[0]];
    ctx.moveTo(first.x, first.y);

    for (let i = 1; i <= limit; i++) {
      const pt = screenPoints[pathIndices[i]];
      ctx.lineTo(pt.x, pt.y);
    }

    if (closeTour) {
      const lastIdx = pathIndices[pathIndices.length - 1];
      const last = screenPoints[lastIdx];
      ctx.lineTo(last.x, last.y);
      ctx.lineTo(first.x, first.y);
    }

    ctx.stroke();
  }

  // Draw points
  ctx.fillStyle = "#000000";
  for (const pt of screenPoints) {
    ctx.beginPath();
    ctx.arc(pt.x, pt.y, 1, 0, Math.PI * 2);
    ctx.fill();
  }
}

// ---------- TSP solver ----------
function enhancedNearestNeighbour(points) {
  const n = points.length;
  if (n === 0) return { path: [], distance: 0 };

  function dist(i, j) {
    const dx = points[i].x - points[j].x;
    const dy = points[i].y - points[j].y;
    return Math.hypot(dx, dy);
  }

  function totalDistance(order) {
    let d = 0;
    for (let i = 0; i < n - 1; i++) d += dist(order[i], order[i + 1]);
    d += dist(order[n - 1], order[0]);
    return d;
  }

  function nearestNeighbourFrom(start) {
    const visited = new Array(n).fill(false);
    const order = new Array(n);
    order[0] = start;
    visited[start] = true;

    for (let i = 1; i < n; i++) {
      const current = order[i - 1];
      let best = -1;
      let bestD = Infinity;

      for (let j = 0; j < n; j++) {
        if (!visited[j]) {
          const d = dist(current, j);
          if (d < bestD) {
            bestD = d;
            best = j;
          }
        }
      }
      order[i] = best;
      visited[best] = true;
    }

    return { order, distance: totalDistance(order) };
  }

  function twoOpt(order) {
    let improved = true;
    while (improved) {
      improved = false;
      for (let i = 1; i < n - 1; i++) {
        for (let j = i + 1; j < n; j++) {
          const a = order[i - 1];
          const b = order[i];
          const c = order[j];
          const d = order[(j + 1) % n];

          const curr = dist(a, b) + dist(c, d);
          const next = dist(a, c) + dist(b, d);

          if (next < curr) {
            for (let l = i, r = j; l < r; l++, r--) {
              [order[l], order[r]] = [order[r], order[l]];
            }
            improved = true;
          }
        }
      }
    }
    return order;
  }

  let bestOrder = null;
  let bestDist = Infinity;

  for (let start = 0; start < n; start++) {
    const { order, distance } = nearestNeighbourFrom(start);
    if (distance < bestDist) {
      bestDist = distance;
      bestOrder = order;
    }
  }

  bestOrder = twoOpt(bestOrder);
  bestDist = totalDistance(bestOrder);

  return { path: bestOrder, distance: bestDist };
}

// ---------- Animation ----------
function animatePath(points, pathIndices) {
  if (!points || !pathIndices || pathIndices.length === 0) return;

  if (animationId !== null) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }

  let edgesToDraw = 0;
  const totalEdges = pathIndices.length;

  // activate skip UI
  isAnimating = true;
  animPoints = points;
  animPath = pathIndices;
  skipBtn.style.display = "inline-block";

  function step() {
    if (!isAnimating) return; // skip was clicked

    drawScene(points, pathIndices, edgesToDraw);
    edgesToDraw += 1;  // ❗ 1 edge per frame (your original speed)

    if (edgesToDraw <= totalEdges) {
      animationId = requestAnimationFrame(step);
    } else {
      animationId = null;
      isAnimating = false;
      skipBtn.style.display = "none";
      drawScene(points, pathIndices);
    }
  }

  step();
}

// ---------- Skip button ----------
skipBtn.addEventListener("click", () => {
  if (!isAnimating) return;

  if (animationId !== null) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }

  isAnimating = false;
  skipBtn.style.display = "none";

  if (animPoints && animPath) {
    drawScene(animPoints, animPath);
  }
});

// ---------- File handling ----------
function handleFileChange(event) {
  const file = event.target.files[0];
  if (!file) return;

  if (!file.name.toLowerCase().endsWith(".csv")) {
    alert("Please upload a CSV file.");
    return;
  }

  const reader = new FileReader();
  reader.onload = e => {
    const text = e.target.result;
    const parsed = parseCsv(text);
    if (parsed.length === 0) {
      alert("No valid coordinates found in the CSV file.");
      return;
    }

    fullName = file.name;
    if (fullName.length > 16) {
        extension = "." + fullName.split(".").slice(-1);
        shortenedName = fullName.substring(0, 10);
        fullName = shortenedName + ".." + extension;
    }
    fileName.innerText = "File uploaded: " + fullName;

    locations = parsed;
    tspPath = null;

    // stop animation & hide skip
    if (animationId !== null) {
      cancelAnimationFrame(animationId);
      animationId = null;
    }
    isAnimating = false;
    skipBtn.style.display = "none";

    drawScene(locations, tspPath);
  };
  reader.readAsText(file);
}

// ---------- Event listeners ----------
uploadBtn.addEventListener("click", () => fileInput.click());
fileInput.addEventListener("change", handleFileChange);

findPathBtn.addEventListener("click", () => {
  if (locations.length === 0) {
    alert("Please upload a CSV file first.");
    return;
  }

  processingTxt.style.display = "inline-block";

  // stop animation & hide skip
  if (animationId !== null) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }
  isAnimating = false;
  skipBtn.style.display = "none";

  setTimeout(() => {
    const result = enhancedNearestNeighbour(locations);
    tspPath = result.path;
    console.log("Total tour distance:", result.distance.toFixed(2));

    // hide text and start animation
    processingTxt.style.display = "none";
    animatePath(locations, tspPath);
  }, 0);
});

window.addEventListener("resize", resizeCanvasToFrame);
window.addEventListener("DOMContentLoaded", resizeCanvasToFrame);
