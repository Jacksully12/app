#!/usr/bin/env python3
import json,glob,sys,math
bad=[]
for path in glob.glob('app/src/main/assets/*_pumps.json'):
 data=json.load(open(path,encoding='utf-8'));ids=set();rows=data.get('records',[])
 for r in rows:
  rid=r.get('id')
  if not rid or rid in ids:bad.append(f"{path}: duplicate/missing id {rid}")
  ids.add(rid)
  if not r.get('normalizedCategory'):bad.append(f"{path}: missing normalizedCategory {r.get('model')}")
  if not r.get('phase'):bad.append(f"{path}: missing phase {r.get('model')}")
  if r.get('normalizedCategory')=='MOTOR':continue
  pts=sorted(r.get('curve',[]),key=lambda p:p[0])
  if not r.get('selectable',True):continue
  if len(pts)<2:bad.append(f"{path}: {r.get('model')} has <2 curve points")
  for a,b in zip(pts,pts[1:]):
   if b[1]>a[1]*1.03+60:bad.append(f"{path}: non-monotonic curve {r.get('model')}");break
  kw=r.get('kw');hp=r.get('hp')
  if kw and hp:
   ratio=hp*0.746/kw
   if not 0.60<=ratio<=1.40:bad.append(f"{path}: implausible HP/kW {r.get('model')} {hp}/{kw}")
 if path.endswith('ksb_pumps.json'):
  m=data.get('metadata',{})
  if len(rows)!=1273:bad.append(f"KSB expected 1273 records, got {len(rows)}")
  if m.get('reviewCount')!=47:bad.append(f"KSB expected 47 review records")
  names=[r.get('model','').upper() for r in rows]
  for model in ['OPAL 05 I','OPAL 10 I','OPAL 10CX I','OPAL 15 I']:
   if model not in names:bad.append(f"KSB missing split model {model}")
  fixtures={r.get('model'):r for r in rows}
  if fixtures.get('MR(S) 10CX 10M CABLE',{}).get('phase')!='S':bad.append('KSB MR(S) phase incorrect')
  if fixtures.get('MULTI-SUB PLUS 10-08',{}).get('phase')!='S':bad.append('KSB MULTI-SUB phase incorrect')
 print(path,len(rows),'records')
if bad:
 print('\n'.join(bad[:200]));sys.exit(1)
print('Data validation passed')
