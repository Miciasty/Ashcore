/* Standalone documentation figures. No plugin runtime or external libraries. */
(function () {
  'use strict';

  const NS = 'http://www.w3.org/2000/svg';
  const mounted = new WeakMap();
  let sequence = 0;

  function element(name, attributes, text) {
    const node = document.createElementNS(NS, name);
    Object.entries(attributes || {}).forEach(([key, value]) => node.setAttribute(key, value));
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function append(parent, name, attributes, text) {
    const node = element(name, attributes, text);
    parent.appendChild(node);
    return node;
  }

  function rotateY(point, degrees, origin) {
    const angle = degrees * Math.PI / 180;
    return {
      x: Math.cos(angle) * point.x + Math.sin(angle) * point.z + origin.x,
      y: point.y + origin.y,
      z: -Math.sin(angle) * point.x + Math.cos(angle) * point.z + origin.z
    };
  }

  function number(value) {
    return (Math.abs(value) < 0.005 ? 0 : value).toFixed(2);
  }

  function pointText(point) {
    return `(${number(point.x)}, ${number(point.y)}, ${number(point.z)})`;
  }

  function connect(parent, a, b, attributes) {
    return append(parent, 'line', Object.assign({ x1: a.x, y1: a.y, x2: b.x, y2: b.y }, attributes));
  }

  function svgBase(svg, id, title, description) {
    svg.replaceChildren();
    append(svg, 'title', { id: `${id}-title` }, title);
    append(svg, 'desc', { id: `${id}-description` }, description);
    svg.setAttribute('aria-labelledby', `${id}-title ${id}-description`);
    const defs = append(svg, 'defs');
    ['x', 'y', 'z', 'ray'].forEach((axis) => {
      const marker = append(defs, 'marker', {
        id: `${id}-arrow-${axis}`, viewBox: '0 0 10 10', refX: 8, refY: 5,
        markerWidth: 5, markerHeight: 5, orient: 'auto-start-reverse'
      });
      append(marker, 'path', { d: 'M 1 1 L 9 5 L 1 9 Z', class: `diagram-fill-${axis}` });
    });
  }

  function bind(host, target, event, listener, cleanups) {
    const node = typeof target === 'string' ? host.querySelector(target) : target;
    node.addEventListener(event, listener);
    cleanups.push(() => node.removeEventListener(event, listener));
  }

  function separateCoordinateLabels(svg) {
    const occupied = [];
    const shifts = [[0, 0], [0, -18], [0, 18], [18, 0], [-18, 0], [0, -36], [0, 36], [24, -18], [-24, -18], [0, -54], [0, 54], [48, 0], [-48, 0], [0, -72], [0, 72], [48, -36], [-48, -36]];
    svg.querySelectorAll('text').forEach((label) => {
      if (typeof label.getBBox !== 'function') return;
      const original = { x: Number(label.getAttribute('x')), y: Number(label.getAttribute('y')) };
      for (const [dx, dy] of shifts) {
        label.setAttribute('x', original.x + dx);
        label.setAttribute('y', original.y + dy);
        const box = label.getBBox();
        const inFrame = box.x >= 88 && box.y >= 58 && box.x + box.width <= 752 && box.y + box.height <= 382;
        const collision = occupied.some((other) =>
          box.x < other.x + other.width + 5 && box.x + box.width + 5 > other.x &&
          box.y < other.y + other.height + 5 && box.y + box.height + 5 > other.y);
        if (inFrame && !collision) break;
      }
      occupied.push(label.getBBox());
    });
  }

  function coordinateFigure(host) {
    const id = `wiki-coordinates-${++sequence}`;
    const cleanups = [];
    const state = { space: 'world', angle: 30, offset: 2 };
    const local = { x: 2, y: 1, z: 1 };
    host.innerHTML = `
      <figure class="diagram-component diagram-coordinates mx-0 my-[26px] overflow-hidden rounded-xl border border-line bg-surface font-sans text-[13px] leading-normal text-foreground print:break-inside-avoid">
        <div class="diagram-toolbar flex items-center justify-between gap-3 border-b border-line px-[18px] py-3.5 max-[620px]:flex-wrap max-[620px]:p-3">
          <div class="diagram-segmented inline-flex items-center rounded-[7px] border border-line bg-page p-[3px]" role="group" aria-label="Coordinate readout">
            <button type="button" class="cursor-pointer rounded border-0 bg-transparent px-3 py-1.5 text-xs font-[550] leading-[1.4] text-muted hover:text-foreground aria-pressed:bg-[var(--diagram-muted-surface)] aria-pressed:text-foreground aria-pressed:shadow-[0_1px_2px_#00000012]" data-space="world" aria-pressed="true">World space</button>
            <button type="button" class="cursor-pointer rounded border-0 bg-transparent px-3 py-1.5 text-xs font-[550] leading-[1.4] text-muted hover:text-foreground aria-pressed:bg-[var(--diagram-muted-surface)] aria-pressed:text-foreground aria-pressed:shadow-[0_1px_2px_#00000012]" data-space="local" aria-pressed="false">Local space</button>
          </div>
          <span class="diagram-plane-label whitespace-nowrap text-[11px] text-muted">Y-up · right-handed</span>
        </div>
        <svg class="diagram-scene block h-auto w-full overflow-visible bg-[var(--diagram-plot)]" viewBox="80 50 680 340" role="img"></svg>
        <div class="diagram-readout grid grid-cols-[1fr_auto] items-center gap-x-4 gap-y-[5px] border-y border-line px-5 py-[15px] max-[620px]:grid-cols-1 max-[620px]:gap-2 max-[620px]:p-3.5" aria-live="polite" aria-atomic="true">
          <div class="diagram-readout-label block text-[11px] text-muted" data-readout-label>Point in world space</div>
          <output class="diagram-vector col-start-2 row-span-2 row-start-1 flex gap-6 font-mono text-[15px] leading-normal tabular-nums max-[620px]:col-start-1 max-[620px]:row-span-1 max-[620px]:row-start-2 max-[620px]:gap-5 max-[620px]:text-sm" data-vector></output>
          <div class="diagram-readout-note text-[11px] text-muted max-[620px]:row-start-3" data-origin></div>
        </div>
        <div class="diagram-controls grid grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] items-center gap-[26px] px-5 pt-[19px] pb-[15px] max-[620px]:grid-cols-2 max-[620px]:gap-[18px] max-[620px]:px-3.5 max-[620px]:pt-[17px] max-[620px]:pb-3.5 print:hidden">
          <div class="diagram-control">
            <div class="diagram-control-label mb-[9px] flex items-baseline justify-between gap-2"><label class="text-xs leading-normal text-foreground" for="${id}-rotation">Y rotation</label><output class="whitespace-nowrap font-mono text-[11px] text-muted tabular-nums" for="${id}-rotation" data-angle-value>30°</output></div>
            <input id="${id}-rotation" class="m-0 block h-[18px] w-full cursor-pointer p-0 accent-accent" type="range" min="0" max="180" step="1" value="30" data-angle>
            <div class="diagram-range-labels mt-[5px] flex justify-between text-[10px] leading-[1.2] text-muted" aria-hidden="true"><span>0°</span><span>180°</span></div>
          </div>
          <div class="diagram-control">
            <div class="diagram-control-label mb-[9px] flex items-baseline justify-between gap-2"><label class="text-xs leading-normal text-foreground" for="${id}-offset">Origin X offset</label><output class="whitespace-nowrap font-mono text-[11px] text-muted tabular-nums" for="${id}-offset" data-offset-value>2.0 blocks</output></div>
            <input id="${id}-offset" class="m-0 block h-[18px] w-full cursor-pointer p-0 accent-accent" type="range" min="-2" max="4" step="0.1" value="2" data-offset>
            <div class="diagram-range-labels mt-[5px] flex justify-between text-[10px] leading-[1.2] text-muted" aria-hidden="true"><span>−2 blocks</span><span>4 blocks</span></div>
          </div>
          <button type="button" class="diagram-reset cursor-pointer appearance-none rounded-md border border-line bg-transparent px-3 py-[7px] text-[11px] leading-[1.4] text-muted hover:bg-[var(--diagram-muted-surface)] hover:text-foreground max-[620px]:col-span-full max-[620px]:justify-self-start print:hidden" data-reset>Reset</button>
        </div>
        <figcaption class="diagram-caption px-5 pt-px pb-[18px] text-[11px] leading-[1.7] text-muted max-[620px]:px-3.5 max-[620px]:pt-0 max-[620px]:pb-4">The local point stays at (2, 1, 1). Rotate or move its frame to change the world coordinates. This illustration uses a Y-up, right-handed convention. 1 unit equals 1 block.</figcaption>
      </figure>`;

    const svg = host.querySelector('svg');
    const project = (point) => ({
      x: 382 + (point.x - point.z) * 31 * Math.sqrt(3) / 2,
      y: 210 + (point.x + point.z) * 31 * 0.4 - point.y * 31 * 0.95
    });

    function draw() {
      const origin = { x: state.offset, y: 0, z: 0 };
      const world = rotateY(local, state.angle, origin);
      const selected = state.space === 'world' ? world : local;
      const transform = (point) => rotateY(point, state.angle, origin);
      svgBase(svg, id, 'World and local coordinate frames',
        `A Y-up, right-handed coordinate system in blocks. The local origin is ${pointText(origin)}. ` +
        `The frame rotates ${state.angle} degrees about positive Y. Local point ${pointText(local)} maps to world point ${pointText(world)}.`);

      const grid = append(svg, 'g', { class: 'diagram-grid' });
      for (let i = -4; i <= 7; i++) {
        connect(grid, project({ x: i, y: 0, z: -4 }), project({ x: i, y: 0, z: 6 }));
      }
      for (let i = -4; i <= 6; i++) {
        connect(grid, project({ x: -4, y: 0, z: i }), project({ x: 7, y: 0, z: i }));
      }

      const zero = project({ x: 0, y: 0, z: 0 });
      const worldAxes = append(svg, 'g', { class: `diagram-world-axes ${state.space === 'local' ? 'diagram-secondary-frame' : ''}` });
      const axes = [
        ['x', { x: 6.6, y: 0, z: 0 }, 'X', 12, 5],
        ['y', { x: 0, y: 4.2, z: 0 }, 'Y', 0, -12],
        ['z', { x: 0, y: 0, z: 5.8 }, 'Z', -15, 7]
      ];
      axes.forEach(([axis, endpoint, label, dx, dy]) => {
        const end = project(endpoint);
        connect(worldAxes, zero, end, { class: `diagram-axis diagram-stroke-${axis}`, 'marker-end': `url(#${id}-arrow-${axis})` });
        append(worldAxes, 'text', { x: end.x + dx, y: end.y + dy, class: `diagram-axis-label diagram-fill-${axis}`, 'text-anchor': 'middle' }, label);
      });
      append(svg, 'circle', { cx: zero.x, cy: zero.y, r: 3.5, class: 'diagram-world-origin' });
      append(svg, 'text', { x: zero.x - 18, y: zero.y + 32, class: 'diagram-origin-label', 'text-anchor': 'end' }, 'World origin');

      const corners = [
        { x: 0, y: 0, z: 0 }, { x: 2, y: 0, z: 0 }, { x: 2, y: 0, z: 1 }, { x: 0, y: 0, z: 1 },
        { x: 0, y: 1, z: 0 }, { x: 2, y: 1, z: 0 }, { x: 2, y: 1, z: 1 }, { x: 0, y: 1, z: 1 }
      ].map(transform);
      const faces = [[0, 1, 2, 3], [0, 1, 5, 4], [1, 2, 6, 5], [2, 3, 7, 6], [3, 0, 4, 7], [4, 5, 6, 7]];
      faces.sort((a, b) => {
        const depth = (face) => face.reduce((sum, index) => sum + corners[index].x + corners[index].z + corners[index].y, 0);
        return depth(a) - depth(b);
      });
      const box = append(svg, 'g', { class: 'diagram-local-box' });
      faces.forEach((face) => append(box, 'polygon', {
        points: face.map((index) => { const p = project(corners[index]); return `${p.x},${p.y}`; }).join(' ')
      }));

      const projectedWorld = project(world);
      const base = project({ x: world.x, y: 0, z: world.z });
      const projections = append(svg, 'g', { class: 'diagram-projections' });
      connect(projections, projectedWorld, base);
      connect(projections, base, project({ x: world.x, y: 0, z: 0 }));
      connect(projections, base, project({ x: 0, y: 0, z: world.z }));
      append(projections, 'circle', { cx: base.x, cy: base.y, r: 3, class: 'diagram-ground-point' });

      const localOrigin = project(origin);
      const localAxes = append(svg, 'g', { class: `diagram-local-axes ${state.space === 'world' ? 'diagram-secondary-frame' : ''}` });
      const localEndpoints = [
        ['x', { x: 3, y: 0, z: 0 }, 'x′', 12, 13],
        ['y', { x: 0, y: 2.2, z: 0 }, 'y′', 11, -6],
        ['z', { x: 0, y: 0, z: 2.3 }, 'z′', -10, 15]
      ];
      localEndpoints.forEach(([axis, endpoint, label, dx, dy]) => {
        const end = project(transform(endpoint));
        connect(localAxes, localOrigin, end, { class: `diagram-axis diagram-stroke-${axis}`, 'marker-end': `url(#${id}-arrow-${axis})` });
        append(localAxes, 'text', { x: end.x + dx, y: end.y + dy, class: `diagram-axis-label diagram-fill-${axis}`, 'text-anchor': 'middle' }, label);
      });
      append(svg, 'circle', { cx: localOrigin.x, cy: localOrigin.y, r: 4, class: 'diagram-local-origin' });
      append(svg, 'text', { x: localOrigin.x + 12, y: localOrigin.y + 20, class: 'diagram-origin-label' }, 'Local origin');
      append(svg, 'circle', { cx: projectedWorld.x, cy: projectedWorld.y, r: 11, class: 'diagram-point-halo' });
      append(svg, 'circle', { cx: projectedWorld.x, cy: projectedWorld.y, r: 5, class: 'diagram-point' });
      append(svg, 'text', { x: projectedWorld.x + 15, y: projectedWorld.y - 21, class: 'diagram-point-label' }, 'Point P');
      separateCoordinateLabels(svg);

      host.querySelector('[data-readout-label]').textContent = `Point in ${state.space} space`;
      host.querySelector('[data-vector]').innerHTML = ['x', 'y', 'z'].map((axis) =>
        `<span class="min-w-[59px] whitespace-nowrap"><span class="diagram-value-axis diagram-fill-${axis} mr-2 inline-block text-[11px] font-semibold">${axis.toUpperCase()}</span>${number(selected[axis])}</span>`).join('');
      host.querySelector('[data-origin]').textContent = `Local origin in world space: ${pointText(origin)}`;
      host.querySelector('[data-angle-value]').textContent = `${state.angle}°`;
      host.querySelector('[data-offset-value]').textContent = `${state.offset.toFixed(1)} blocks`;
      host.querySelector('[data-angle]').setAttribute('aria-valuetext', `${state.angle} degrees`);
      host.querySelector('[data-offset]').setAttribute('aria-valuetext', `${state.offset.toFixed(1)} blocks`);
      host.querySelectorAll('[data-space]').forEach((button) => button.setAttribute('aria-pressed', String(button.dataset.space === state.space)));
    }

    host.querySelectorAll('[data-space]').forEach((button) => bind(host, button, 'click', () => {
      state.space = button.dataset.space;
      draw();
    }, cleanups));
    bind(host, '[data-angle]', 'input', (event) => { state.angle = Number(event.target.value); draw(); }, cleanups);
    bind(host, '[data-offset]', 'input', (event) => { state.offset = Number(event.target.value); draw(); }, cleanups);
    bind(host, '[data-reset]', 'click', () => {
      Object.assign(state, { space: 'world', angle: 30, offset: 2 });
      host.querySelector('[data-angle]').value = '30';
      host.querySelector('[data-offset]').value = '2';
      draw();
    }, cleanups);
    draw();
    return () => cleanups.forEach((cleanup) => cleanup());
  }

  const obstacles = [
    { name: 'A', minX: 3.2, maxX: 4.8, minZ: 1.2, maxZ: 2.8 },
    { name: 'B', minX: 6.2, maxX: 7.8, minZ: 2.2, maxZ: 3.8 }
  ];

  // Closed AABBs and a closed finite ray: tangent and maximum-distance hits count.
  function rayIntersections(origin, direction, length, boxes) {
    const epsilon = 1e-10;
    return boxes.map((box) => {
      let enter = 0;
      let leave = length;
      for (const axis of ['X', 'Z']) {
        const value = origin[axis.toLowerCase()];
        const delta = direction[axis.toLowerCase()];
        const low = box[`min${axis}`];
        const high = box[`max${axis}`];
        if (Math.abs(delta) < epsilon) {
          if (value < low - epsilon || value > high + epsilon) return null;
        } else {
          const a = (low - value) / delta;
          const b = (high - value) / delta;
          enter = Math.max(enter, Math.min(a, b));
          leave = Math.min(leave, Math.max(a, b));
          if (enter > leave + epsilon) return null;
        }
      }
      if (enter > length + epsilon || leave < -epsilon) return null;
      const distance = Math.max(0, Math.min(length, enter));
      return { box, distance, x: origin.x + direction.x * distance, z: origin.z + direction.z * distance };
    }).filter(Boolean).sort((a, b) => a.distance - b.distance);
  }

  function raycastFigure(host) {
    const id = `wiki-raycast-${++sequence}`;
    const cleanups = [];
    const state = { mode: 'first', angle: 25, length: 10 };
    host.innerHTML = `
      <figure class="diagram-component diagram-raycast mx-0 my-[26px] overflow-hidden rounded-xl border border-line bg-surface font-sans text-[13px] leading-normal text-foreground print:break-inside-avoid">
        <div class="diagram-toolbar flex items-center justify-between gap-3 border-b border-line px-[18px] py-3.5 max-[620px]:flex-wrap max-[620px]:p-3">
          <div class="diagram-segmented inline-flex items-center rounded-[7px] border border-line bg-page p-[3px]" role="group" aria-label="Raycast result mode">
            <button type="button" class="cursor-pointer rounded border-0 bg-transparent px-3 py-1.5 text-xs font-[550] leading-[1.4] text-muted hover:text-foreground aria-pressed:bg-[var(--diagram-muted-surface)] aria-pressed:text-foreground aria-pressed:shadow-[0_1px_2px_#00000012]" data-mode="first" aria-pressed="true">First hit</button>
            <button type="button" class="cursor-pointer rounded border-0 bg-transparent px-3 py-1.5 text-xs font-[550] leading-[1.4] text-muted hover:text-foreground aria-pressed:bg-[var(--diagram-muted-surface)] aria-pressed:text-foreground aria-pressed:shadow-[0_1px_2px_#00000012]" data-mode="all" aria-pressed="false">All hits</button>
          </div>
          <span class="diagram-plane-label whitespace-nowrap text-[11px] text-muted">XZ plane · top view</span>
        </div>
        <svg class="diagram-scene block h-auto w-full overflow-visible bg-[var(--diagram-plot)]" viewBox="0 0 840 500" role="img"></svg>
        <div class="diagram-hit-readout grid grid-cols-[1fr_1fr_1.15fr] gap-[15px] border-y border-line px-5 py-[15px] max-[620px]:grid-cols-2 max-[620px]:p-3.5" aria-live="polite" aria-atomic="true">
          <div><span class="diagram-readout-label block text-[11px] text-muted">Result</span><output class="diagram-hit-status mt-[5px] block text-xs leading-normal tabular-nums" data-status></output></div>
          <div><span class="diagram-readout-label block text-[11px] text-muted">First hit distance</span><output class="diagram-hit-value mt-[5px] block font-mono text-xs leading-normal tabular-nums" data-distance></output></div>
          <div class="max-[620px]:col-span-full"><span class="diagram-readout-label block text-[11px] text-muted">Hit point (X, Z)</span><output class="diagram-hit-value mt-[5px] block font-mono text-xs leading-normal tabular-nums" data-hit-point></output></div>
        </div>
        <div class="diagram-controls grid grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] items-center gap-[26px] px-5 pt-[19px] pb-[15px] max-[620px]:grid-cols-2 max-[620px]:gap-[18px] max-[620px]:px-3.5 max-[620px]:pt-[17px] max-[620px]:pb-3.5 print:hidden">
          <div class="diagram-control">
            <div class="diagram-control-label mb-[9px] flex items-baseline justify-between gap-2"><label class="text-xs leading-normal text-foreground" for="${id}-angle">Ray angle</label><output class="whitespace-nowrap font-mono text-[11px] text-muted tabular-nums" for="${id}-angle" data-angle-value>25°</output></div>
            <input id="${id}-angle" class="m-0 block h-[18px] w-full cursor-pointer p-0 accent-accent" type="range" min="0" max="45" step="1" value="25" data-angle>
            <div class="diagram-range-labels mt-[5px] flex justify-between text-[10px] leading-[1.2] text-muted" aria-hidden="true"><span>0°</span><span>45°</span></div>
          </div>
          <div class="diagram-control">
            <div class="diagram-control-label mb-[9px] flex items-baseline justify-between gap-2"><label class="text-xs leading-normal text-foreground" for="${id}-length">Max distance</label><output class="whitespace-nowrap font-mono text-[11px] text-muted tabular-nums" for="${id}-length" data-length-value>10.0 blocks</output></div>
            <input id="${id}-length" class="m-0 block h-[18px] w-full cursor-pointer p-0 accent-accent" type="range" min="0" max="12" step="0.1" value="10" data-length>
            <div class="diagram-range-labels mt-[5px] flex justify-between text-[10px] leading-[1.2] text-muted" aria-hidden="true"><span>0 blocks</span><span>12 blocks</span></div>
          </div>
          <button type="button" class="diagram-reset cursor-pointer appearance-none rounded-md border border-line bg-transparent px-3 py-[7px] text-[11px] leading-[1.4] text-muted hover:bg-[var(--diagram-muted-surface)] hover:text-foreground max-[620px]:col-span-full max-[620px]:justify-self-start print:hidden" data-reset>Reset</button>
        </div>
        <figcaption class="diagram-caption px-5 pt-px pb-[18px] text-[11px] leading-[1.7] text-muted max-[620px]:px-3.5 max-[620px]:pt-0 max-[620px]:pb-4">A finite ray starts at (0, 0). Its angle runs from +X toward +Z. 1 grid square equals 1 block. A hit on an obstacle boundary or exactly at the maximum distance counts. All hits returns the entry point of each obstacle.</figcaption>
      </figure>`;

    const svg = host.querySelector('svg');
    const project = (x, z) => ({ x: 82 + x * 45, y: 57 + z * 45 });

    function draw() {
      const angle = state.angle * Math.PI / 180;
      const direction = { x: Math.cos(angle), z: Math.sin(angle) };
      const hits = rayIntersections({ x: 0, z: 0 }, direction, state.length, obstacles);
      const visibleHits = state.mode === 'first' ? hits.slice(0, 1) : hits;
      const first = hits[0];
      const origin = project(0, 0);
      const endpoint = project(direction.x * state.length, direction.z * state.length);
      const hitDescription = visibleHits.length ? visibleHits.map((hit) =>
        `Obstacle ${hit.box.name} at (${number(hit.x)}, ${number(hit.z)}), distance ${number(hit.distance)} blocks`).join('. ') : 'No hit';
      svgBase(svg, id, 'Ray intersection in the XZ plane',
        `Top view. A ray starts at (0, 0), has angle ${state.angle} degrees from positive X toward positive Z, and maximum distance ${state.length} blocks. ` +
        `Obstacle A spans X 3.2 to 4.8 and Z 1.2 to 2.8. Obstacle B spans X 6.2 to 7.8 and Z 2.2 to 3.8. ${hitDescription}.`);

      const grid = append(svg, 'g', { class: 'diagram-grid' });
      for (let x = 0; x <= 15; x++) connect(grid, project(x, -0.4), project(x, 9));
      for (let z = 0; z <= 9; z++) connect(grid, project(-0.4, z), project(15, z));
      connect(svg, origin, project(15, 0), { class: 'diagram-axis diagram-stroke-x', 'marker-end': `url(#${id}-arrow-x)` });
      connect(svg, origin, project(0, 9), { class: 'diagram-axis diagram-stroke-z', 'marker-end': `url(#${id}-arrow-z)` });
      append(svg, 'text', { x: 776, y: 63, class: 'diagram-axis-label diagram-fill-x' }, 'X');
      append(svg, 'text', { x: 63, y: 469, class: 'diagram-axis-label diagram-fill-z' }, 'Z');
      for (let x = 2; x <= 14; x += 2) {
        const p = project(x, 0);
        append(svg, 'text', { x: p.x, y: p.y - 15, class: 'diagram-tick-label', 'text-anchor': 'middle' }, x);
      }
      for (let z = 2; z <= 8; z += 2) {
        const p = project(0, z);
        append(svg, 'text', { x: p.x - 18, y: p.y + 4, class: 'diagram-tick-label', 'text-anchor': 'end' }, z);
      }

      obstacles.forEach((box) => {
        const corner = project(box.minX, box.minZ);
        const isHit = visibleHits.some((hit) => hit.box === box);
        append(svg, 'rect', {
          x: corner.x, y: corner.y, width: (box.maxX - box.minX) * 45, height: (box.maxZ - box.minZ) * 45,
          rx: 2, class: `diagram-obstacle${isHit ? ' diagram-obstacle-hit' : ''}`
        });
        append(svg, 'text', { x: corner.x + 36, y: corner.y + 42, class: 'diagram-obstacle-label', 'text-anchor': 'middle' }, box.name);
        append(svg, 'text', { x: corner.x + 36, y: corner.y + 91, class: 'diagram-obstacle-caption', 'text-anchor': 'middle' }, `Obstacle ${box.name}`);
      });

      if (state.length > 0) {
        const end = state.mode === 'first' && first ? project(first.x, first.z) : endpoint;
        if (state.mode === 'first' && first && first.distance < state.length) {
          connect(svg, end, endpoint, { class: 'diagram-ray-remainder' });
        }
        connect(svg, origin, end, { class: 'diagram-ray', 'marker-end': first && state.mode === 'first' ? '' : `url(#${id}-arrow-ray)` });
        append(svg, 'circle', { cx: endpoint.x, cy: endpoint.y, r: 4, class: 'diagram-ray-endpoint' });
        append(svg, 'text', { x: endpoint.x + 12, y: endpoint.y + 6, class: 'diagram-endpoint-label' }, 'Max distance');
      }

      visibleHits.forEach((hit, index) => {
        const p = project(hit.x, hit.z);
        append(svg, 'circle', { cx: p.x, cy: p.y, r: 10, class: 'diagram-point-halo' });
        append(svg, 'circle', { cx: p.x, cy: p.y, r: 5, class: 'diagram-point' });
        append(svg, 'text', { x: p.x - 10, y: p.y - 15, class: 'diagram-hit-label', 'text-anchor': 'end' }, state.mode === 'all' ? `Hit ${index + 1}` : 'First hit');
      });
      append(svg, 'circle', { cx: origin.x, cy: origin.y, r: 5, class: 'diagram-world-origin' });
      append(svg, 'text', { x: origin.x + 10, y: origin.y - 16, class: 'diagram-origin-label' }, 'Origin (0, 0)');

      const status = host.querySelector('[data-status]');
      status.textContent = first ? (state.mode === 'first' ? `Hit · obstacle ${first.box.name}` : `${hits.length} ${hits.length === 1 ? 'hit' : 'hits'}`) : 'No hit';
      status.dataset.hit = String(Boolean(first));
      host.querySelector('[data-distance]').textContent = first ? `${number(first.distance)} blocks` : '—';
      host.querySelector('[data-hit-point]').textContent = first ? `(${number(first.x)}, ${number(first.z)})` : '—';
      host.querySelector('[data-angle-value]').textContent = `${state.angle}°`;
      host.querySelector('[data-length-value]').textContent = `${state.length.toFixed(1)} blocks`;
      host.querySelector('[data-angle]').setAttribute('aria-valuetext', `${state.angle} degrees`);
      host.querySelector('[data-length]').setAttribute('aria-valuetext', `${state.length.toFixed(1)} blocks`);
      host.querySelectorAll('[data-mode]').forEach((button) => button.setAttribute('aria-pressed', String(button.dataset.mode === state.mode)));
    }

    host.querySelectorAll('[data-mode]').forEach((button) => bind(host, button, 'click', () => {
      state.mode = button.dataset.mode;
      draw();
    }, cleanups));
    bind(host, '[data-angle]', 'input', (event) => { state.angle = Number(event.target.value); draw(); }, cleanups);
    bind(host, '[data-length]', 'input', (event) => { state.length = Number(event.target.value); draw(); }, cleanups);
    bind(host, '[data-reset]', 'click', () => {
      Object.assign(state, { mode: 'first', angle: 25, length: 10 });
      host.querySelector('[data-angle]').value = '25';
      host.querySelector('[data-length]').value = '10';
      draw();
    }, cleanups);
    draw();
    return () => cleanups.forEach((cleanup) => cleanup());
  }

  function mount(root) {
    const container = root || document;
    const hosts = Array.from(container.querySelectorAll('[data-diagram]'));
    if (container.matches && container.matches('[data-diagram]')) hosts.unshift(container);
    const cleanups = hosts.map((host) => {
      const previous = mounted.get(host);
      if (previous) previous();
      const create = { coordinates: coordinateFigure, raycast: raycastFigure,
        contacts: window.WikiLabs?.contacts, sampling: window.WikiLabs?.sampling,
        noise: window.WikiLabs?.noise }[host.dataset.diagram];
      if (!create) return () => {};
      const cleanup = create(host);
      mounted.set(host, cleanup);
      return () => {
        if (mounted.get(host) === cleanup) {
          cleanup();
          mounted.delete(host);
        }
      };
    });
    return () => cleanups.forEach((cleanup) => cleanup());
  }

  window.WikiDiagrams = Object.freeze({ mount });
})();
