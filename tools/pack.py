import zipfile, os, sys
orig, root, out = sys.argv[1], sys.argv[2], sys.argv[3]
seen = set()
with zipfile.ZipFile(orig) as zi, zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as zo:
    for info in zi.infolist():
        p = os.path.join(root, info.filename)
        if info.filename.endswith('/'):
            if os.path.isdir(p):
                zo.writestr(zipfile.ZipInfo(info.filename, date_time=info.date_time), b'')
                seen.add(info.filename)
            continue
        if os.path.isfile(p):
            zinfo = zipfile.ZipInfo(info.filename, date_time=info.date_time)
            zinfo.compress_type = zipfile.ZIP_DEFLATED
            zinfo.external_attr = info.external_attr
            with open(p, 'rb') as f:
                zo.writestr(zinfo, f.read())
            seen.add(info.filename)
    extra = []
    for d, _, fs in os.walk(root):
        for fn in fs:
            rel = os.path.relpath(os.path.join(d, fn), root).replace(os.sep, '/')
            if rel not in seen:
                extra.append(rel)
    for rel in sorted(extra):
        zinfo = zipfile.ZipInfo(rel, date_time=(2026, 10, 9, 0, 0, 0))
        zinfo.compress_type = zipfile.ZIP_STORED if rel.endswith('.ogg') else zipfile.ZIP_DEFLATED
        zinfo.external_attr = 0o644 << 16
        with open(os.path.join(root, rel), 'rb') as f:
            zo.writestr(zinfo, f.read())
print('wrote', out, 'kept', len(seen), 'added', len(extra))
