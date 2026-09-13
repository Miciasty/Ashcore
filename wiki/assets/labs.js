/* Interactive explanations, using the shared WIKI figure and camera conventions.
   Geometry and sampling are independent of the camera. No external dependencies. */
(() => {
  'use strict';
  const M = window.WikiLabMath, NS = 'http://www.w3.org/2000/svg';
  let sequence = 0;
  const clamp = (v,a,b)=>Math.max(a,Math.min(b,v));
  const fmt = (v,n=2)=>(Math.abs(v)<.5*10**-n?0:v).toFixed(n);
  const tuple = p=>'('+[p.x,p.y,p.z].map(v=>fmt(v)).join(', ')+')';
  const button = (text,attrs='')=>'<button type="button" class="lab-button" '+attrs+'>'+text+'</button>';
  const choices = (key,label,items)=>'<div class="lab-choices" role="group" aria-label="'+label+'">'+items.map(([v,t])=>button(t,'data-choice="'+key+'" data-option="'+v+'" aria-pressed="false"')).join('')+'</div>';
  const range = (id,key,label,min,max,step,value)=>'<div class="lab-control"><div><label for="'+id+'-'+key+'">'+label+'</label><output for="'+id+'-'+key+'" data-value="'+key+'"></output></div><input type="range" id="'+id+'-'+key+'" data-control="'+key+'" min="'+min+'" max="'+max+'" step="'+step+'" value="'+value+'"></div>';
  const seedControl = id=>'<div class="lab-control"><label for="'+id+'-seed">Seed · SplitMix64</label><div class="lab-seed-row"><input type="number" id="'+id+'-seed" data-seed min="0" max="999999" step="1" value="1337">'+button('Next seed','data-next-seed')+'</div></div>';
  const cameraControls = '<div class="lab-camera print:hidden"><span>View only · drag or use arrow keys to orbit</span><div role="group" aria-label="Camera controls">'+button('−','data-camera="out" aria-label="Zoom out"')+button('+','data-camera="in" aria-label="Zoom in"')+button('Reset view','data-camera="reset"')+'</div></div>';
  const svgMarkup = '<svg class="lab-scene" role="img"></svg>';
  function add(parent,tag,attrs={},text) {
    const node=document.createElementNS(NS,tag);
    for (const [key,value] of Object.entries(attrs)) node.setAttribute(key,value);
    if (text!==undefined) node.textContent=text;
    parent.appendChild(node); return node;
  }
  const line = (svg,a,b,cls='lab-grid',attrs={})=>add(svg,'line',{x1:a.x,y1:a.y,x2:b.x,y2:b.y,class:cls,...attrs});
  const label = (svg,x,y,text,cls='lab-label',anchor='start')=>add(svg,'text',{x,y,class:cls,'text-anchor':anchor},text);
  function path(svg,points,cls,close=false,attrs={}) {
    return add(svg,'path',{d:points.map((p,i)=>(i?'L':'M')+p.x.toFixed(2)+','+p.y.toFixed(2)).join(' ')+(close?'Z':''),class:cls,...attrs});
  }
  function base(svg,id,title,description) {
    svg.replaceChildren();
    add(svg,'title',{id:id+'-title'},title);
    add(svg,'desc',{id:id+'-desc'},description);
    svg.setAttribute('aria-labelledby',id+'-title '+id+'-desc');
  }
  function frame(host,config) {
    const id='ashcore-lab-'+(++sequence), listeners=[];
    host.innerHTML='<figure class="diagram-component lab mx-0 my-[26px] overflow-hidden rounded-xl border border-line bg-surface font-sans text-[13px] leading-normal text-foreground print:break-inside-avoid" aria-label="'+config.title+'">'+
      '<div class="lab-heading"><strong>'+config.title+'</strong><span>'+config.badge+'</span></div>'+
      '<div class="lab-toolbar print:hidden">'+config.toolbar+'</div>'+config.plot+
      '<div class="lab-readouts" aria-live="polite" aria-atomic="true">'+config.stats.map(([key,name])=>'<div><span>'+name+'</span><output data-stat="'+key+'"></output></div>').join('')+'</div>'+
      '<div class="lab-controls print:hidden">'+config.controls(id)+'</div>'+
      '<div class="lab-actions print:hidden">'+(config.actions||'')+button('Reset example','data-reset')+'</div>'+
      '<figcaption class="lab-caption">'+config.caption+'</figcaption></figure>';
    return {id,host,figure:host.querySelector('figure'),
      on(target,event,fn,options) {
        const node=typeof target==='string'?host.querySelector(target):target;
        node.addEventListener(event,fn,options);
        listeners.push(()=>node.removeEventListener(event,fn,options));
      },
      stat(key,value) { host.querySelector('[data-stat="'+key+'"]').textContent=value; },
      value(key,value) {
        host.querySelector('[data-value="'+key+'"]').textContent=value;
        host.querySelector('[data-control="'+key+'"]').setAttribute('aria-valuetext',value);
      },
      selected(state) {
        host.querySelectorAll('[data-choice]').forEach(b=>b.setAttribute('aria-pressed',String(state[b.dataset.choice]===b.dataset.option)));
      },
      dispose() { listeners.forEach(remove=>remove()); }
    };
  }
  function inputControls(f,state,update) {
    f.host.querySelectorAll('[data-control]').forEach(input=>f.on(input,'input',()=>{
      state[input.dataset.control]=Number(input.value);update();
    }));
    f.host.querySelectorAll('[data-choice]').forEach(b=>f.on(b,'click',()=>{
      state[b.dataset.choice]=b.dataset.option;update();
    }));
    const seed=f.host.querySelector('[data-seed]');
    if(seed) {
      f.on(seed,'change',()=>{
        const value=seed.valueAsNumber;
        if(Number.isFinite(value)) state.seed=clamp(Math.trunc(value),0,999999);
        seed.value=state.seed;update();
      });
      f.on('[data-next-seed]','click',()=>{state.seed=(state.seed+1)%1000000;seed.value=state.seed;update();});
    }
  }
  function syncInputs(f,state) {
    f.host.querySelectorAll('[data-control]').forEach(input=>{input.value=state[input.dataset.control];});
    const seed=f.host.querySelector('[data-seed]'); if(seed) seed.value=state.seed;
  }
  // Orthographic projection keeps a sphere's silhouette circular at any angle.
  function scene(f,svg,draw,options={}) {
    const initial={yaw:24,pitch:18,zoom:1}, camera={...initial};
    let size={width:640,height:350}, scheduled=0, disposed=false, drag=null;
    const is3D=()=>options.is3D?options.is3D():false;
    function render() {
      const yaw=camera.yaw*Math.PI/180, pitch=camera.pitch*Math.PI/180;
      const cy=Math.cos(yaw),sy=Math.sin(yaw),cp=Math.cos(pitch),sp=Math.sin(pitch);
      const setting=key=>typeof options[key]==='function'?options[key]():options[key];
      const scale=Math.min(size.width/(setting('extent')||6),size.height/(setting('vertical')||4.7))*camera.zoom;
      const center=setting('center')||{x:0,y:0,z:0};
      const project=p=>{
        const x=p.x-center.x,y=p.y-center.y,z=p.z-center.z;
        return is3D()?{x:size.width/2+(cy*x-sy*z)*scale,y:size.height*.51+(-cp*y+sp*(sy*x+cy*z))*scale,depth:cp*(sy*x+cy*z)+sp*y}:
          {x:size.width/2+x*scale,y:size.height*.51-y*scale,depth:z};
      };
      svg.setAttribute('viewBox','0 0 '+size.width+' '+size.height);
      svg.setAttribute('tabindex',is3D()?'0':'-1');
      svg.classList.toggle('lab-orbit',is3D());
      svg.setAttribute('aria-keyshortcuts',is3D()?'ArrowLeft ArrowRight ArrowUp ArrowDown + - Home':'');
      f.host.querySelectorAll('[data-camera]').forEach(b=>{b.disabled=!is3D();});
      f.host.querySelector('.lab-camera').hidden=!is3D();
      draw({svg,width:size.width,height:size.height,project,scale});
    }
    function schedule() {
      if(!scheduled&&!disposed) scheduled=requestAnimationFrame(()=>{scheduled=0;if(!disposed) render();});
    }
    function action(value) {
      if(value==='reset') Object.assign(camera,initial);
      else camera.zoom=clamp(camera.zoom*(value==='in'?1.12:1/1.12),.7,1.35);
      schedule();
    }
    f.host.querySelectorAll('[data-camera]').forEach(b=>f.on(b,'click',()=>action(b.dataset.camera)));
    f.on(svg,'pointerdown',e=>{
      if(!is3D()||e.button!==0||!e.isPrimary)return;
      svg.focus({preventScroll:true});drag={id:e.pointerId,x:e.clientX,y:e.clientY};
      svg.setPointerCapture(e.pointerId);
    });
    f.on(svg,'pointermove',e=>{
      if(!drag||drag.id!==e.pointerId)return;
      camera.yaw-=(e.clientX-drag.x)*.4;
      camera.pitch=clamp(camera.pitch+(e.clientY-drag.y)*.35,-65,75);
      drag.x=e.clientX;drag.y=e.clientY;schedule();
    });
    function end(e) {
      if(!drag||drag.id!==e.pointerId)return;
      drag=null;if(svg.hasPointerCapture(e.pointerId))svg.releasePointerCapture(e.pointerId);
    }
    ['pointerup','pointercancel','lostpointercapture'].forEach(event=>f.on(svg,event,end));
    f.on(svg,'keydown',e=>{
      if(!is3D()||e.ctrlKey||e.metaKey||e.altKey||!['ArrowLeft','ArrowRight','ArrowUp','ArrowDown','+','=','-','Home'].includes(e.key))return;
      e.preventDefault();
      if(e.key==='ArrowLeft')camera.yaw-=8;if(e.key==='ArrowRight')camera.yaw+=8;
      if(e.key==='ArrowUp')camera.pitch=clamp(camera.pitch+6,-65,75);
      if(e.key==='ArrowDown')camera.pitch=clamp(camera.pitch-6,-65,75);
      if(e.key==='+'||e.key==='=')action('in');if(e.key==='-')action('out');if(e.key==='Home')action('reset');
      schedule();
    });
    const resize=new ResizeObserver(entries=>{
      const r=entries[0].contentRect;
      if(r.width>0&&r.height>0){size={width:r.width,height:r.height};schedule();}
    });
    resize.observe(svg);
    const theme=new MutationObserver(schedule);
    theme.observe(document.documentElement,{attributes:true,attributeFilter:['data-theme']});
    schedule();
    return {redraw:schedule,reset:()=>action('reset'),dispose() {
      disposed=true;if(scheduled)cancelAnimationFrame(scheduled);
      if(drag&&svg.hasPointerCapture(drag.id))svg.releasePointerCapture(drag.id);
      resize.disconnect();theme.disconnect();
    }};
  }
  function axes(svg,project,extent=2,slice=false) {
    for(let i=-extent;i<=extent;i++) {
      if(slice) {
        line(svg,project({x:i,y:-extent,z:0}),project({x:i,y:extent,z:0}));
        line(svg,project({x:-extent,y:i,z:0}),project({x:extent,y:i,z:0}));
      } else {
        line(svg,project({x:i,y:-1,z:-extent}),project({x:i,y:-1,z:extent}));
        line(svg,project({x:-extent,y:-1,z:i}),project({x:extent,y:-1,z:i}));
      }
    }
    const origin=project({x:0,y:0,z:0});
    for(const [axis,end] of [['X',{x:1,y:0,z:0}],['Y',{x:0,y:1,z:0}],['Z',{x:0,y:0,z:1}]]) {
      if(slice&&axis==='Z')continue;
      const p=project(end);line(svg,origin,p,'lab-axis lab-'+axis.toLowerCase());
      label(svg,p.x+7,p.y-5,axis,'lab-label lab-axis-label');
    }
  }
  function contactFigure(host) {
    const defaults={x:1.5,y:0,view:'3d',shift:false}, state={...defaults};
    const f=frame(host,{
      title:'Explore sphere contact',badge:'Radius 1 block · Y-up',
      toolbar:choices('view','Contact view',[['3d','3D view'],['slice','XY slice']])+button('Show shift of A','data-shift aria-pressed="false"'),
      plot:svgMarkup+'<div class="lab-legend"><span class="lab-key-a">● Sphere A · origin</span><span class="lab-key-b">◆ Sphere B · movable</span><span>● pointA / ◆ pointB · surface witnesses</span></div>'+cameraControls,
      stats:[['status','Contact result'],['depth','Penetration depth'],['distance','Center distance'],['normal','Normal · A toward B'],['a','pointA · blocks'],['b','pointB · blocks']],
      controls:id=>range(id,'x','B center · X',0,3,.05,1.5)+range(id,'y','B center · Y',-1.5,1.5,.05,0),
      actions:'<div role="group" aria-label="Contact examples">'+[['touch','Touching'],['miss','Separated'],['coincident','Same center']].map(([v,t])=>button(t,'data-preset="'+v+'"')).join('')+'</div>',
      caption:'Both spheres have radius 1. The normal points from A to B; coincident centers choose +X. The dashed sphere shows A translated by −normal × depth, with B fixed. This is a geometric shift to tangency, not a physics simulation. Camera controls change only the view.'
    });
    let result=M.contact(state.x,state.y);
    const view=scene(f,host.querySelector('svg'),({svg,width,height,project,scale})=>{
      base(svg,f.id,'Two sphere surfaces and contact witnesses','A is centered at the origin. B is at '+tuple({x:state.x,y:state.y,z:0})+'. '+(result.hit?'Contact depth '+fmt(result.depth)+' blocks.':'No contact.'));
      axes(svg,project,3,state.view==='slice');
      const shapes=[{center:{x:0,y:0,z:0},name:'A',cls:'lab-sphere-a'},{center:{x:state.x,y:state.y,z:0},name:'B',cls:'lab-sphere-b'}];
      const pa=project(shapes[0].center),pb=project(shapes[1].center);
      const closeLabels=Math.abs(pa.x-pb.x)<30&&Math.abs(pa.y-pb.y)<25;
      shapes.sort((a,b)=>project(a.center).depth-project(b.center).depth);
      for(const shape of shapes) {
        const p=project(shape.center);
        add(svg,'circle',{cx:p.x,cy:p.y,r:scale,class:shape.cls});
        if(state.view==='3d') for(const plane of ['xy','xz','yz']) {
          const points=Array.from({length:65},(_,i)=>{
            const a=i*Math.PI/32,c=Math.cos(a),s=Math.sin(a),q={...shape.center};
            q[plane[0]]+=c;q[plane[1]]+=s;return project(q);
          });
          path(svg,points,shape.cls+' lab-wire');
        }
        add(svg,'circle',{cx:p.x,cy:p.y,r:3,class:'lab-center'});
        label(svg,p.x+(closeLabels?(shape.name==='A'?-12:12):0),Math.max(42,p.y-scale-13),shape.name,'lab-label lab-strong','middle');
      }
      line(svg,project({x:0,y:0,z:0}),project({x:state.x,y:state.y,z:0}),'lab-dashed');
      if(result.hit) {
        if(state.shift) {
          const p=project({x:-result.normal.x*result.depth,y:-result.normal.y*result.depth,z:0});
          add(svg,'circle',{cx:p.x,cy:p.y,r:scale,class:'lab-shift'});
        }
        const a=project(result.pointA),b=project(result.pointB);
        line(svg,a,b,'lab-witness-link');
        add(svg,'circle',{cx:a.x,cy:a.y,r:5,class:'lab-witness-a'});
        path(svg,[{x:b.x,y:b.y-6},{x:b.x+6,y:b.y},{x:b.x,y:b.y+6},{x:b.x-6,y:b.y}],'lab-witness-b',true);
        const n=result.normal, start=project({x:0,y:-1.5,z:0}),end=project({x:n.x,y:-1.5+n.y,z:0});
        line(svg,start,end,'lab-normal');
        const dx=end.x-start.x,dy=end.y-start.y,l=Math.hypot(dx,dy)||1;
        path(svg,[{x:end.x-dx/l*9-dy/l*4,y:end.y-dy/l*9+dx/l*4},end,{x:end.x-dx/l*9+dy/l*4,y:end.y-dy/l*9-dx/l*4}],'lab-normal');
        label(svg,18,height-18,state.shift?'Arrow: normal · dashed outline: shifted A':'Arrow: contact normal','lab-muted');
      } else label(svg,18,height-18,'No contact → no normal or witnesses','lab-muted');
      label(svg,width-16,24,state.view==='3d'?'Orthographic 3D':'XY slice · Z = 0','lab-muted','end');
    },{is3D:()=>state.view==='3d',center:()=>({x:state.shift ? .5 : 1,y:0,z:0}),extent:()=>state.shift?8:6.3,vertical:()=>state.shift?6.5:6});
    function update() {
      result=M.contact(state.x,state.y);f.selected(state);
      f.stat('status',result.hit?(result.depth===0?'Touching':'Overlapping'):'Separated');
      f.stat('depth',result.hit?fmt(result.depth,3)+' blocks':'−∞ · miss');
      f.stat('distance',fmt(result.distance,3)+' blocks');
      f.stat('normal',result.hit?tuple(result.normal):'null');
      f.stat('a',result.hit?tuple(result.pointA):'null');f.stat('b',result.hit?tuple(result.pointB):'null');
      f.value('x',fmt(state.x)+' blocks');f.value('y',fmt(state.y)+' blocks');
      host.querySelector('[data-shift]').disabled=!result.hit;
      host.querySelector('[data-shift]').setAttribute('aria-pressed',String(state.shift));
      view.redraw();
    }
    inputControls(f,state,update);
    f.on('[data-shift]','click',()=>{state.shift=!state.shift;update();});
    host.querySelectorAll('[data-preset]').forEach(b=>f.on(b,'click',()=>{
      state.x={touch:2,miss:2.6,coincident:0}[b.dataset.preset];state.y=0;syncInputs(f,state);update();
    }));
    f.on('[data-reset]','click',()=>{Object.assign(state,defaults);syncInputs(f,state);view.reset();update();});
    update();return ()=>{view.dispose();f.dispose();};
  }
  function samplingFigure(host) {
    const defaults={count:64,seed:1337,domain:'square'},state={...defaults};
    const f=frame(host,{
      title:'Compare sample coverage',badge:'Equal sample counts · 2D',
      toolbar:choices('domain','Sample domain',[['square','Unit square'],['disk','Concentric disk']]),
      plot:'<div class="lab-pair">'+['SplitMix64','Halton · bases 2 and 3'].map(t=>'<div><h3>'+t+'</h3><svg class="lab-samples" viewBox="0 0 320 320" role="img"></svg></div>').join('')+'</div><div class="lab-legend"><span>● Samples in generation order</span><span class="lab-key-b">◎ Last sample</span></div>',
      stats:[['count','Samples in each panel'],['draws','SplitMix64 uniform draws'],['random','Last random point'],['halton','Last Halton point']],
      controls:id=>range(id,'count','Sample count',1,256,1,64)+seedControl(id),
      caption:'SplitMix64 uses two consecutive uniform draws per point. Halton evaluates indices 1…N with bases 2 and 3; it uses no RNG. Increasing N keeps each existing prefix. Changing the seed affects only the random panel. The disk view maps both sets through mapToConcentricDisk. Coverage is a visual comparison, not a guarantee of minimum spacing.'
    });
    function update() {
      const points=M.samples(state.seed,state.count,state.domain);f.selected(state);f.value('count',String(state.count));
      const min=state.domain==='disk'?-1:0,span=state.domain==='disk'?2:1;
      const project=p=>({x:32+(p.x-min)/span*256,y:288-(p.y-min)/span*256});
      host.querySelectorAll('svg').forEach((svg,i)=>{
        const key=i?'halton':'random';
        base(svg,f.id+'-'+key,(i?'Halton':'SplitMix64')+' samples',state.count+' points in the unit '+state.domain+'. Last point '+fmt(points[key].at(-1).x,4)+', '+fmt(points[key].at(-1).y,4)+'.');
        for(let j=0;j<=4;j++) {
          const pos=32+j*64;line(svg,{x:pos,y:32},{x:pos,y:288});line(svg,{x:32,y:pos},{x:288,y:pos});
        }
        if(state.domain==='disk') add(svg,'circle',{cx:160,cy:160,r:128,class:'lab-domain'});
        else add(svg,'rect',{x:32,y:32,width:256,height:256,class:'lab-domain'});
        points[key].forEach((p,n)=>{
          const at=project(p);add(svg,'circle',{cx:at.x,cy:at.y,r:state.count>128?2.2:2.8,class:'lab-sample'});
          if(n===points[key].length-1)add(svg,'circle',{cx:at.x,cy:at.y,r:6,class:'lab-last-sample'});
        });
        label(svg,32,308,String(min),'lab-muted','middle');label(svg,288,308,'1','lab-muted','middle');
        label(svg,17,36,'1','lab-muted','middle');label(svg,17,288,String(min),'lab-muted','middle');
        label(svg,160,310,'X','lab-muted','middle');label(svg,16,165,'Y','lab-muted','middle');
      });
      const pair=p=>'('+fmt(p.x,4)+', '+fmt(p.y,4)+')';
      f.stat('count',state.count);f.stat('draws',state.count*2);f.stat('random',pair(points.random.at(-1)));f.stat('halton',pair(points.halton.at(-1)));
    }
    inputControls(f,state,update);
    f.on('[data-reset]','click',()=>{Object.assign(state,defaults);syncInputs(f,state);update();});
    update();return ()=>f.dispose();
  }
  function noiseFigure(host) {
    const defaults={frequency:.02,octaves:5,gain:.5,seed:1337,view:'map'},state={...defaults};
    const f=frame(host,{
      title:'Explore Perlin terrain',badge:'64 × 64 block patch · lacunarity 2',
      toolbar:choices('view','Noise view',[['map','2D value map'],['3d','3D height surface']]),
      plot:svgMarkup+'<div class="lab-color-key"><span>−2</span><i aria-hidden="true"></i><span>0</span><i aria-hidden="true"></i><span>+2</span><span>Raw fBm · fixed color scale</span></div>'+cameraControls,
      stats:[['sample','Sample at X = 128, Z = 64'],['height','floor(64 + value × 12)'],['range','Sampled patch · raw min / max'],['lattice','Blocks per base lattice unit']],
      controls:id=>range(id,'frequency','Frequency · per block',.005,.04,.005,.02)+range(id,'octaves','Octaves',0,5,1,5)+range(id,'gain','Gain per octave',0,1,.05,.5)+seedControl(id),
      caption:'The same 2D Perlin function supplies both views. X spans 96…160 blocks and Z spans 32…96; the marker is the code example at (128, 64). Heights use floor(64 + raw fBm × 12). The 3D mesh joins a 65 × 65 sample grid; it is not a Minecraft block map. Colors clip values outside −2…2, while readouts and heights retain the raw sum. Changing the camera leaves all samples unchanged.'
    });
    let cachedSeed=null,noise=null,data=null;
    function rebuild() {
      if(cachedSeed!==state.seed){noise=M.perlin(state.seed);cachedSeed=state.seed;}
      const sample=(x,z)=>M.fbm(noise,x*state.frequency,z*state.frequency,state.octaves,2,state.gain);
      const vertices=[];
      for(let z=0;z<=64;z++)for(let x=0;x<=64;x++) {
        const value=sample(96+x,32+z);
        vertices.push({x:(x-32)/8,y:(Math.floor(64+value*12)-64)/8,z:(z-32)/8,value});
      }
      const values=vertices.map(v=>v.value);
      data={vertices,value:sample(128,64),min:Math.min(...values),max:Math.max(...values)};
    }
    function color(value) {
      const stops=[[35,78,115],[55,163,190],[231,219,169]];
      const t=clamp((value+2)/2,0,2),i=Math.min(1,Math.floor(t)),a=t-i;
      return 'rgb('+stops[i].map((v,j)=>Math.round(v+(stops[i+1][j]-v)*a)).join(',')+')';
    }
    const view=scene(f,host.querySelector('svg'),({svg,width,height,project,scale})=>{
      if(!data)return;
      base(svg,f.id,'Perlin fBm '+(state.view==='map'?'value map':'height surface'),'Seed '+state.seed+', frequency '+state.frequency+', '+state.octaves+' octaves. At (128,64), raw value '+fmt(data.value,4)+', height '+Math.floor(64+data.value*12)+' blocks.');
      const vertices=data.vertices,fragment=document.createDocumentFragment();
      if(state.view==='map') {
        const side=Math.min(width-82,height-54),left=(width-side)/2,top=21,cell=side/64;
        for(let z=0;z<64;z++)for(let x=0;x<64;x++) {
          const v=vertices[z*65+x].value;
          add(fragment,'rect',{x:left+x*cell,y:top+z*cell,width:cell,height:cell,fill:color(v),'shape-rendering':'crispEdges'});
        }
        svg.appendChild(fragment);
        add(svg,'rect',{x:left,y:top,width:side,height:side,class:'lab-domain'});
        label(svg,left,top+side+23,'96','lab-muted','middle');label(svg,left+side,top+side+23,'160','lab-muted','middle');
        label(svg,left+side/2,top+side+23,'X · blocks','lab-muted','middle');
        label(svg,left-10,top+5,'32','lab-muted','end');label(svg,left-10,top+side,'96','lab-muted','end');
        label(svg,left-36,top+side/2,'Z','lab-muted');
        const cx=left+side/2,cy=top+side/2;
        line(svg,{x:cx-9,y:cy},{x:cx+9,y:cy},'lab-probe');line(svg,{x:cx,y:cy-9},{x:cx,y:cy+9},'lab-probe');
      } else {
        axes(svg,project,4);
        const projected=vertices.map(project),faces=[];
        for(let z=0;z<64;z++)for(let x=0;x<64;x++) {
          const i=z*65+x,ids=[i,i+1,i+66,i+65],points=ids.map(j=>projected[j]);
          faces.push({points,depth:points.reduce((a,p)=>a+p.depth,0)/4,value:ids.reduce((a,j)=>a+vertices[j].value,0)/4});
        }
        faces.sort((a,b)=>a.depth-b.depth);
        for(const face of faces)path(fragment,face.points,'',true,{fill:color(face.value),stroke:color(face.value),'stroke-width':.35});
        svg.appendChild(fragment);
        const at=project(vertices[32*65+32]);
        add(svg,'circle',{cx:at.x,cy:at.y,r:5,class:'lab-probe-point'});
        const origin=project({x:0,y:0,z:0}),corner={x:width-53,y:height-49};
        for(const [axis,end] of [['X',{x:1,y:0,z:0}],['Y',{x:0,y:1,z:0}],['Z',{x:0,y:0,z:1}]]) {
          const p=project(end),tip={x:corner.x+(p.x-origin.x)/scale*27,y:corner.y+(p.y-origin.y)/scale*27};
          line(svg,corner,tip,'lab-axis lab-'+axis.toLowerCase());
          label(svg,tip.x+(tip.x<corner.x?-7:7),tip.y,axis,'lab-label lab-axis-label','middle');
        }
        label(svg,14,24,'Y = height · X/Z = block coordinates','lab-muted');
        label(svg,14,height-17,'Marker: (128, '+Math.floor(64+data.value*12)+', 64)','lab-muted');
      }
    },{is3D:()=>state.view==='3d',extent:12,vertical:9});
    function update(recompute=true) {
      if(recompute)rebuild();
      f.selected(state);f.value('frequency',fmt(state.frequency,3));f.value('octaves',String(state.octaves));f.value('gain',fmt(state.gain));
      f.stat('sample',fmt(data.value,5));f.stat('height',Math.floor(64+data.value*12)+' blocks');
      f.stat('range',fmt(data.min,3)+' / '+fmt(data.max,3));f.stat('lattice',fmt(1/state.frequency,1)+' blocks');
      view.redraw();
    }
    inputControls(f,state,update);
    f.on('[data-reset]','click',()=>{Object.assign(state,defaults);syncInputs(f,state);view.reset();update();});
    update();return ()=>{view.dispose();f.dispose();};
  }
  window.WikiLabs = Object.freeze({contacts:contactFigure,sampling:samplingFigure,noise:noiseFigure});
})();
