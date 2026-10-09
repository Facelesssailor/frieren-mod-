import sys,os
from PIL import Image,ImageDraw
src,start,end,step,out,cols=sys.argv[1],float(sys.argv[2]),float(sys.argv[3]),float(sys.argv[4]),sys.argv[5],int(sys.argv[6])
base=885.0
ts=[];t=start
while t<=end+1e-6: ts.append(t); t+=step
ims=[]
for t in ts:
    n=int(round((t-base)/0.25))+1
    p=os.path.join(src,'f_%03d.jpg'%n)
    if os.path.exists(p): ims.append((t,Image.open(p)))
w,h=ims[0][1].size
rows=(len(ims)+cols-1)//cols
sh=Image.new('RGB',(cols*w,rows*h),'black')
d=ImageDraw.Draw(sh)
for i,(t,im) in enumerate(ims):
    x,y=(i%cols)*w,(i//cols)*h
    sh.paste(im,(x,y))
    m=int(t//60); s=t-60*m
    d.rectangle([x,y,x+90,y+18],fill='black'); d.text((x+4,y+3),'%d:%05.2f'%(m,s),fill='yellow')
sh.save(out,quality=88)
print(out,sh.size,len(ims))
