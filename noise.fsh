/*
precision highp float;
uniform vec2      resolution;
uniform float     time;

float rand(vec2 n) {
  return fract(cos(dot(n, vec2(2.9898, 20.1414))) * 5.5453);
}

float noise(vec2 n) {
  const vec2 d = vec2(0.0, 1.0);
  vec2 b = floor(n), f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
  return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

float fbm(vec2 n){
  float total=0.,amplitude=1.5;
  for(int i=0;i<18;i++){
    total+=noise(n)*amplitude;
    n+=n;
    amplitude*=.45;
  }
  return total;
}


void main(void){
  const vec3 c1=vec3(0.802, 0.1059, 0.01059);
  const vec3 c2=vec3(167./255.,96./255.,110./255.);
  const vec3 c3=vec3(0.4902, 0.2333, 0.2902);
  const vec3 c4=vec3(0.4118, 0.1451, 0.2706);
  const vec3 c5=vec3(0.4176, 0.2549, 0.1);
  const vec3 c6=vec3(0.8, 0.3569, 0.3569);

  vec2 p=gl_FragCoord.xy*5./resolution.xx;
  float q=fbm(p-time*.05);
  vec2 r=vec2(fbm(p+q+time*0.1-p.x-p.y),fbm(p+q-time*0.1));
  vec3 c=mix(c1,c2,fbm(p+r))+mix(c3,c4,r.x)-mix(c5,c6,r.y);
  float grad=gl_FragCoord.y/resolution.y;
  gl_FragColor=vec4(c*cos(1.0*gl_FragCoord.y/resolution.y),1.5);
  gl_FragColor.xyz*=1.15-grad;
}
  */

#ifdef GL_ES
precision mediump float;
#endif

#extension GL_OES_standard_derivatives : enable

#define NUM_OCTAVES 16

uniform float time;
uniform vec2 resolution;

mat3 rotX(float a) {
  float c = cos(a);
  float s = sin(a);
  return mat3(
  1, 0, 0,
  0, c, -s,
  0, s, c
  );
}
mat3 rotY(float a) {
  float c = cos(a);
  float s = sin(a);
  return mat3(
  c, 0, -s,
  0, 1, 0,
  s, 0, c
  );
}

float random(vec2 pos) {
  return fract(sin(dot(pos.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise(vec2 pos) {
  vec2 i = floor(pos);
  vec2 f = fract(pos);
  float a = random(i + vec2(0.0, 0.0));
  float b = random(i + vec2(1.0, 0.0));
  float c = random(i + vec2(0.0, 1.0));
  float d = random(i + vec2(1.0, 1.0));
  vec2 u = f * f * (3.0 - 2.0 * f);
  return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos) {
  float v = 0.0;
  float a = 0.5;
  vec2 shift = vec2(100.0);
  mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
  for (int i=0; i<NUM_OCTAVES; i++) {
    v += a * noise(pos);
    pos = rot * pos * 2.0 + shift;
    a *= 0.5;
  }
  return v;
}

void main(void) {
  vec2 p = (gl_FragCoord.xy * 2.0 - resolution.xy) / min(resolution.x, resolution.y);

  float t = 0.0, d;

  float time2 = 3.0 * time / 2.0;

  vec2 q = vec2(0.0);
  q.x = fbm(p + 0.00 * time2);
  q.y = fbm(p + vec2(1.0));
  vec2 r = vec2(0.0);
  r.x = fbm(p + 1.0 * q + vec2(1.7, 9.2) + 0.15 * time2);
  r.y = fbm(p + 1.0 * q + vec2(8.3, 2.8) + 0.126 * time2);
  float f = fbm(p + r);
  vec3 color = mix(
  vec3(0.101961, 0.866667, 0.319608),
  vec3(.666667, 0.598039, 0.366667),
  clamp((f * f) * 4.0, 0.0, 1.0)
  );

  color = mix(
  color,
  vec3(0.34509803921, 0.06666666666, 0.83137254902),
  clamp(length(q), 0.0, 1.0)
  );


  color = mix(
  color,
  vec3(0.1, -0.5, 0.1),
  clamp(length(r.x), 0.0, 1.0)
  );

  color = (f *f * f + 0.6 * f * f + 0.5 * f) * color;

  gl_FragColor = vec4(color, 1.0);
}
