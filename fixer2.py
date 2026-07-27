import os, re

d = 'backend/src/main/java/com/sbvia/backend/entity'

for f in os.listdir(d):
    if not f.endswith('.java'):
        continue
    p = os.path.join(d, f)
    with open(p, 'r', encoding='utf-8') as file:
        lines = file.readlines()
        
    for i in range(len(lines)):
        line = lines[i]
        if '"\x01"' in line:
            # We need to look ahead to find the variable name to deduce the column name
            var_name = None
            for j in range(i+1, min(i+5, len(lines))):
                m = re.search(r'private\s+[A-Za-z0-9_]+\s+([a-zA-Z0-9_]+)\s*;', lines[j])
                if m:
                    var_name = m.group(1)
                    break
            if var_name:
                # Deduce the column name. e.g. idUsuario -> id_Usuario, rol -> id_Rol
                if var_name.startswith('id'):
                    col = 'id_' + var_name[2:]
                else:
                    col = 'id_' + var_name[0].upper() + var_name[1:]
                
                lines[i] = line.replace('"\x01"', f'"\\"{col}\\""')
                
    with open(p, 'w', encoding='utf-8') as file:
        file.writelines(lines)

print("Fixed!")
