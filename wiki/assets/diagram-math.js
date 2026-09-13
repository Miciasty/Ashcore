/* Numerical models for the WIKI figures, checked against Ashcore 1.2.0.
   Only the finite, bounded input ranges exposed by the figures are supported. */
(() => {
  'use strict';
  const point = (x, y, z = 0) => ({x, y, z});
  function contact(x, y) {
    const scale = Math.max(Math.abs(x), Math.abs(y), 1);
    const scaledDistance = Math.hypot(x / scale, y / scale);
    const distance = Math.hypot(x, y);
    if (scaledDistance > 2 / scale) return {hit:false, depth:-Infinity, distance, normal:null, pointA:null, pointB:null};
    const largest = Math.max(Math.abs(x), Math.abs(y));
    const length = largest === 0 ? 1 : Math.sqrt((x / largest) ** 2 + (y / largest) ** 2);
    const normal = largest === 0 ? point(1, 0) : point(x / largest / length, y / largest / length);
    const depth = Math.max(0, 1 / scale + 1 / scale - scaledDistance) * scale;
    return {hit:true, depth, distance, normal, pointA:{...normal}, pointB:point(x-normal.x, y-normal.y)};
  }
  function splitMix64(seed) {
    let state = BigInt.asUintN(64, BigInt(seed));
    return () => {
      state = BigInt.asUintN(64, state + 0x9E3779B97F4A7C15n);
      let z = state;
      z = BigInt.asUintN(64, (z ^ (z >> 30n)) * 0xBF58476D1CE4E5B9n);
      z = BigInt.asUintN(64, (z ^ (z >> 27n)) * 0x94D049BB133111EBn);
      return Number((z ^ (z >> 31n)) >> 11n) / 9007199254740992;
    };
  }
  function halton(index, base) {
    let fraction = 1, result = 0;
    while (index > 0) {
      fraction /= base;
      result += fraction * (index % base);
      index = Math.floor(index / base);
    }
    return result;
  }
  function disk(u, v) {
    const x = 2*u-1, y = 2*v-1;
    if (x === 0 && y === 0) return point(0, 0);
    const useX = Math.abs(x) > Math.abs(y);
    const r = useX ? x : y;
    const theta = useX ? Math.PI/4*(y/x) : Math.PI/2-Math.PI/4*(x/y);
    return point(r*Math.cos(theta), r*Math.sin(theta));
  }
  function samples(seed, count, domain = 'square') {
    const random = splitMix64(seed), result = {random:[], halton:[]};
    const map = domain === 'disk' ? disk : point;
    for (let i=1; i<=count; i++) {
      result.random.push(map(random(), random()));
      result.halton.push(map(halton(i,2), halton(i,3)));
    }
    return result;
  }
  function perlin(seed) {
    const random = splitMix64(seed), p = Array.from({length:256}, (_,i)=>i);
    for (let i=255; i>0; i--) {
      const j = Math.floor(random()*(i+1));
      [p[i],p[j]] = [p[j],p[i]];
    }
    const perm = Array.from({length:512}, (_,i)=>p[i&255]);
    const fade = t=>t*t*t*(t*(t*6-15)+10);
    const lerp = (a,b,t)=>a+t*(b-a);
    const grad = (hash,x,y)=>[x+y,x-y,-x+y,-x-y,x,-x,y,-y][hash&7];
    return (x,y)=>{
      const X = Math.floor(x)&255, Y = Math.floor(y)&255;
      const xf=x-Math.floor(x), yf=y-Math.floor(y), u=fade(xf), v=fade(yf);
      const aa=perm[X]+Y, ab=aa+1, ba=perm[X+1]+Y, bb=ba+1;
      return lerp(lerp(grad(perm[aa],xf,yf),grad(perm[ba],xf-1,yf),u),
        lerp(grad(perm[ab],xf,yf-1),grad(perm[bb],xf-1,yf-1),u),v);
    };
  }
  function fbm(noise, x, y, octaves, lacunarity=2, gain=.5) {
    let sum=0, amplitude=1;
    for (let i=0; i<octaves; i++) {
      sum += noise(x,y)*amplitude;
      x *= lacunarity; y *= lacunarity; amplitude *= gain;
    }
    return sum;
  }
  window.WikiLabMath = Object.freeze({contact, splitMix64, halton, disk, samples, perlin, fbm});
})();
