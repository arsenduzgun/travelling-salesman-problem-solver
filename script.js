// --- DOM elements ---
const uploadBtn     = document.getElementById("upload-btn");
const fileName      = document.getElementById("file-nme");
const findPathBtn   = document.getElementById("find-path-btn");
const fileInput     = document.getElementById("file-input");
const canvas        = document.getElementById("tsp-canvas");
const visualFrame   = document.getElementById("visual-frame");
const processingTxt = document.getElementById("processing-txt");
const skipBtn       = document.getElementById("skip-btn");


let processingAnimation = null;

let ctx = null;
let locations = [];
let tspPath = null;
let animationId = null;

let isAnimating = false;
let animPoints = null;
let animPath = null;

// --- Web Worker setup ---
let tspWorker = null;
let workerRunning = false;

if (window.Worker) {
  tspWorker = new Worker("tsp-worker.js");

  tspWorker.onmessage = (e) => {
    stopProcessingAnimation();

    const { path, distance } = e.data;

    workerRunning = false;
    processingTxt.style.display = "none";

    tspPath = path;
    console.log("Total tour distance:", distance.toFixed(2));

    // start animation once worker is done
    animatePath(locations, tspPath);
  };

  tspWorker.onerror = (e) => {
    console.error("TSP worker error:", e.message);
    workerRunning = false;
    processingTxt.style.display = "none";
    alert("An error occurred while solving the path.");
  };
} else {
  console.warn("Web Workers are not supported in this browser.");
}

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
    edgesToDraw += 1;  // 1 edge per frame

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

    let fullName = file.name;
    if (fullName.length > 16) {
      const extension = "." + fullName.split(".").slice(-1);
      const shortenedName = fullName.substring(0, 10);
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

function startProcessingAnimation() {
  let dots = 1;

  processingTxt.style.display = "inline-block";
  processingTxt.innerText = "Processing.";

  processingAnimation = setInterval(() => {
    dots = (dots % 3) + 1;  // cycles 0 → 1 → 2 → 3 → back to 0
    processingTxt.innerText = "Processing" + ".".repeat(dots);
  }, 500); // update every 500 ms (smooth timing)
}

function stopProcessingAnimation() {
  clearInterval(processingAnimation);
  processingAnimation = null;
  processingTxt.style.display = "none";
}


findPathBtn.addEventListener("click", () => {
  if (locations.length === 0) {
    alert("Please upload a CSV file first.");
    return;
  }

  if (!tspWorker) {
    alert("Your browser does not support Web Workers. The solver may freeze the UI.");
    return;
  }

  if (workerRunning) {
    // (Optional: later you can turn this into a "Stop" button)
    console.log("Solver already running.");
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

  startProcessingAnimation();

  workerRunning = true;
  tspWorker.postMessage({ points: locations });
});

window.addEventListener("resize", resizeCanvasToFrame);
window.addEventListener("DOMContentLoaded", resizeCanvasToFrame);
