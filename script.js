// --- DOM elements ---
const uploadBtn = document.getElementById("upload-btn");
const fileNameEl = document.getElementById("file-name");
const startStopBtn = document.getElementById("start-stop-btn");
const fileInput = document.getElementById("file-input");
const canvas = document.getElementById("tsp-canvas");
const visualFrame = document.getElementById("visual-frame");
const processingLoader = document.getElementById("processing-loader");
const skipBtn = document.getElementById("skip-btn");
const exportBtn = document.getElementById("export-btn");
const distanceTxt = document.getElementById("distance");

// --- Canvas & state ---
let ctx = null;
let locations = [];
let tspPath = null;

let animationId = null;
let isAnimating = false;
let animPoints = null;
let animPath = null;

let tspWorker = null;
let workerRunning = false;

let originalFileName = null;   // real uploaded filename
let totalTourDistance = null;  // total tour length returned by worker

// ---------------------------------------------------
// Control state helper
// upload disabled when:   workerRunning || isAnimating
// start/stop disabled when: !hasFile || isAnimating
// export enabled when:    !isAnimating && tspPath != null
// ---------------------------------------------------
function updateControls() {
  const hasFile = locations.length > 0;

  uploadBtn.disabled    = workerRunning || isAnimating;
  startStopBtn.disabled = !hasFile      || isAnimating;
  exportBtn.disabled    = isAnimating   || !tspPath;
}

// ---------------------------------------------------
// Distance display helpers
// ---------------------------------------------------
function showDistance() {
  if (totalTourDistance == null || Number.isNaN(totalTourDistance)) return;
  distanceTxt.style.display = "inline-block";
  distanceTxt.textContent = "Total distance: " + totalTourDistance.toFixed(2);
}

function hideDistance() {
  distanceTxt.style.display = "none";
}

// ---------------------------------------------------
// Worker setup
// ---------------------------------------------------
function createTspWorker() {
  if (!window.Worker) {
    console.warn("Web Workers are not supported in this browser.");
    return null;
  }

  const worker = new Worker("tsp-worker.js");

  worker.onmessage = (e) => {
    stopProcessingAnimation();
    workerRunning = false;
    startStopBtn.textContent = "Start";

    const { path, distance } = e.data || {};
    if (!path || !Array.isArray(path) || path.length === 0) {
      tspPath = null;
      totalTourDistance = null;
      hideDistance();
      updateControls();
      alert("No valid path found.");
      return;
    }

    tspPath = path;
    totalTourDistance = (typeof distance === "number" ? distance : null);
    console.log("Total tour distance:", distance?.toFixed?.(2) ?? distance);

    // Start animation when worker finishes
    animatePath(locations, tspPath);
  };

  worker.onerror = (e) => {
    console.error("TSP worker error:", e.message);
    workerRunning = false;
    startStopBtn.textContent = "Start";
    stopProcessingAnimation();
    tspPath = null;
    totalTourDistance = null;
    hideDistance();
    updateControls();
    alert("An error occurred while solving the path.");
  };

  return worker;
}

function ensureWorker() {
  if (!tspWorker) {
    tspWorker = createTspWorker();
  }
}

// ---------------------------------------------------
// Canvas setup
// ---------------------------------------------------
function resizeCanvasToFrame() {
  const rect = visualFrame.getBoundingClientRect();
  canvas.width  = rect.width;
  canvas.height = rect.height;

  if (!ctx) {
    ctx = canvas.getContext("2d");
  }

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

// ---------------------------------------------------
// CSV parsing
// ---------------------------------------------------
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

// ---------------------------------------------------
// Coordinate transform
// ---------------------------------------------------
function computeTransform(points) {
  let minX = Infinity, maxX = -Infinity;
  let minY = Infinity, maxY = -Infinity;

  for (const p of points) {
    if (p.x < minX) minX = p.x;
    if (p.x > maxX) maxX = p.x;
    if (p.y < minY) minY = p.y;
    if (p.y > maxY) maxY = p.y;
  }

  const rangeX = (maxX - minX) || 1;
  const rangeY = (maxY - minY) || 1;

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

// ---------------------------------------------------
// Drawing
// ---------------------------------------------------
function drawScene(points, pathIndices, edgesToDraw) {
  if (!ctx) return;
  clearCanvas();

  if (!points || points.length === 0) return;

  const { minX, minY, scale, offsetX, offsetY } = computeTransform(points);

  const screenPoints = points.map(p => ({
    x: offsetX + (p.x - minX) * scale,
    y: offsetY + (p.y - minY) * scale
  }));

  // Draw path (with optional partial edges)
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

// ---------------------------------------------------
// Animation
// ---------------------------------------------------
function stopAnimation() {
  if (animationId !== null) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }
  isAnimating = false;
  skipBtn.style.display = "none";
}

function animatePath(points, pathIndices) {
  if (!points || !pathIndices || pathIndices.length === 0) return;

  stopAnimation(); // ensure clean start
  hideDistance();  // don't show previous result while animating

  let edgesToDraw = 0;
  const totalEdges = pathIndices.length;

  isAnimating = true;
  animPoints = points;
  animPath = pathIndices;
  skipBtn.style.display = "inline-block";
  updateControls(); // disable upload & start/stop, export

  function step() {
    if (!isAnimating) return;

    drawScene(points, pathIndices, edgesToDraw);
    edgesToDraw += 1;

    if (edgesToDraw <= totalEdges) {
      animationId = requestAnimationFrame(step);
    } else {
      animationId = null;
      isAnimating = false;
      skipBtn.style.display = "none";
      drawScene(points, pathIndices); // final full path
      showDistance();                 // ✅ show total distance at the end
      updateControls();
    }
  }

  step();
}

// Skip button
skipBtn.addEventListener("click", () => {
  if (!isAnimating) return;

  stopAnimation();

  if (animPoints && animPath) {
    drawScene(animPoints, animPath);
  }

  showDistance();   // ✅ show distance when user skips animation
  updateControls();
});

// ---------------------------------------------------
// Processing loader
// ---------------------------------------------------
function startProcessingAnimation() {
  processingLoader.style.display = "flex";
  hideDistance(); // hide any previous distance while processing
}

function stopProcessingAnimation() {
  processingLoader.style.display = "none";
}

// ---------------------------------------------------
// File handling
// ---------------------------------------------------
function resetStateForNewFile() {
  tspPath = null;
  totalTourDistance = null;
  stopAnimation();
  hideDistance();
  clearCanvas();
  updateControls();
}

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

    originalFileName = file.name;

    let displayName = file.name;
    if (displayName.length > 16) {
      const parts = displayName.split(".");
      const ext   = parts.length > 1 ? "." + parts.pop() : "";
      const short = displayName.substring(0, 10);
      displayName = short + ".." + ext;
    }

    fileNameEl.innerText = "File uploaded: " + displayName;

    locations = parsed;
    resetStateForNewFile();   // keeps canvas blank until Start is clicked

    updateControls(); // file uploaded → enable start, keep export disabled
  };
  reader.readAsText(file);
}

// Upload & file input
uploadBtn.addEventListener("click", () => fileInput.click());
fileInput.addEventListener("change", handleFileChange);

// ---------------------------------------------------
// Start/Stop button
// ---------------------------------------------------
startStopBtn.addEventListener("click", () => {
  ensureWorker();

  if (!tspWorker) {
    alert("Your browser does not support Web Workers. The solver may freeze the UI.");
    return;
  }

  // ---- START mode ----
  if (!workerRunning) {
    if (locations.length === 0) {
      alert("Please upload a CSV file first.");
      return;
    }
    if (isAnimating) return; // should be disabled already

    stopAnimation();
    tspPath = null;
    totalTourDistance = null;
    hideDistance();

    // Show points for the first time now
    drawScene(locations, null);

    startProcessingAnimation();
    workerRunning = true;
    startStopBtn.textContent = "Stop";
    updateControls();

    tspWorker.postMessage({ points: locations });
    return;
  }

  // ---- STOP mode ----
  workerRunning = false;
  stopProcessingAnimation();
  startStopBtn.textContent = "Start";

  if (tspWorker) {
    tspWorker.terminate();
    tspWorker = null;
  }

  tspPath = null;
  totalTourDistance = null;
  hideDistance();
  clearCanvas();
  updateControls();
});

// ---------------------------------------------------
// Export button
// ---------------------------------------------------
exportBtn.addEventListener("click", () => {
  if (!tspPath || isAnimating) return;

  const header = "id,x,y";
  const rows = tspPath.map(idx => {
    const p = locations[idx];
    return `${p.id},${p.x},${p.y}`;
  });

  const csvContent = [header, ...rows].join("\r\n");
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const url  = URL.createObjectURL(blob);

  let base = originalFileName || "tsp";
  base = base.replace(/\.[^/.]+$/, "");  // strip extension
  const finalName = base + "_path.csv";

  const a = document.createElement("a");
  a.href = url;
  a.download = finalName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  URL.revokeObjectURL(url);
});

// ---------------------------------------------------
// Init
// ---------------------------------------------------
window.addEventListener("resize", resizeCanvasToFrame);
window.addEventListener("DOMContentLoaded", () => {
  resizeCanvasToFrame();
  ensureWorker();
  updateControls();  // initial state: no file, no path
});
