#!/usr/bin/env python3
import json, glob, sys, math
bad=[]
for path in glob.glob('app/src/main/assets/*_pumps.json'):
    data=json.load(open(path,encoding='utf-8')); ids=set(); records=data.get('records',[])
    for r in records:
        rid=r.get('id')
        if not rid or rid in ids: bad.append(f"{path}: duplicate/missing id {rid}")
        ids.add(rid)
        normalized=r.get('normalizedCategory','')
        if not normalized: bad.append(f"{path}: missing normalizedCategory {r.get('model')}")
        if normalized=='MOTOR':
            if not r.get('rpm'): bad.append(f"{path}: motor missing RPM {r.get('model')}")
            if not r.get('frameSize'): bad.append(f"{path}: motor missing frame size {r.get('model')}")
            if not r.get('insulationClass'): bad.append(f"{path}: motor missing insulation class {r.get('model')}")
            continue
        pts=sorted(r.get('curve',[]),key=lambda p:p[0])
        if not r.get('selectable',True): continue
        if len(pts)<2: bad.append(f"{path}: {r.get('model')} has <2 curve points")
        for a,b in zip(pts,pts[1:]):
            if b[1]>a[1]*1.02+5: bad.append(f"{path}: non-monotonic curve {r.get('model')}")
    print(path,len(records),'records')
if bad:
    print('\n'.join(bad[:150])); sys.exit(1)
print('Data validation passed')
