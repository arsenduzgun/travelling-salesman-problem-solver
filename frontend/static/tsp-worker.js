// tsp-worker.js

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
    for (let i = 0; i < n - 1; i++) {
      d += dist(order[i], order[i + 1]);
    }
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
            // reverse segment [i, j]
            for (let l = i, r = j; l < r; l++, r--) {
              const tmp = order[l];
              order[l] = order[r];
              order[r] = tmp;
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

self.onmessage = (e) => {
  const { points } = e.data;
  const result = enhancedNearestNeighbour(points);
  // send back to main thread
  self.postMessage(result);
};
